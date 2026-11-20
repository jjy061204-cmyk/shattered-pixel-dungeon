/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성: 촛대와 화로
 *  - 영웅이 바로 옆에서 1턴에 켤 수 있음. 켜지면 둘레(촛대 2칸, 화로 3칸)가 밝아짐
 *  - 피의 하수인이 찾아와 끔. 꺼지지 않는 불(permanent)도 있음
 *  - 부서지지 않음
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastleBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class CastleLight extends GnollProp {

	{
		spriteClass = VampireCastleSprites.Light.class;
		HP = HT = 1;
	}

	public boolean brazier = false;
	public boolean lit = false;
	//꺼지지 않는 불 (입구의 화로 등)
	public boolean permanent = false;

	public static CastleLight candelabra( boolean lit ){
		CastleLight l = new CastleLight();
		l.lit = lit;
		return l;
	}

	public static CastleLight brazier( boolean lit ){
		CastleLight l = new CastleLight();
		l.brazier = true;
		l.lit = lit;
		return l;
	}

	public int radius(){
		return brazier ? 3 : 2;
	}

	@Override
	protected boolean flammable() {
		return false;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//쇠붙이라 부서지지 않음
	}

	@Override
	public boolean isInvulnerable( Class effect ) {
		return true;
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean interact( Char c ) {
		if (c instanceof Hero){
			Hero hero = (Hero) c;
			if (lit){
				GLog.i( Messages.get( CastleLight.class, "already_lit" ) );
				return false;
			}
			hero.sprite.operate( pos );
			light();
			hero.spendAndNext( 1f );
			return false;
		}
		return true;
	}

	public void light(){
		if (lit) return;
		lit = true;
		if (Dungeon.level.heroFOV[pos]){
			CellEmitter.get( pos ).burst( FlameParticle.FACTORY, 8 );
			Sample.INSTANCE.play( Assets.Sounds.BURNING );
		}
		if (Dungeon.level instanceof VampireCastleBossLevel){
			((VampireCastleBossLevel) Dungeon.level).onLightChanged( this );
		}
	}

	//하수인·보스가 불을 끔
	public boolean snuff( Char by ){
		if (!lit || permanent) return false;
		lit = false;
		if (Dungeon.level.heroFOV[pos]){
			CellEmitter.get( pos ).burst( Speck.factory( Speck.SMOKE ), 6 );
			Sample.INSTANCE.play( Assets.Sounds.PUFF );
			if (by != null && by != this){
				GLog.w( Messages.get( CastleLight.class, "snuffed", by.name() ) );
			}
		}
		if (Dungeon.level instanceof VampireCastleBossLevel){
			((VampireCastleBossLevel) Dungeon.level).onLightChanged( this );
		}
		return true;
	}

	@Override
	public String name() {
		return Messages.get( CastleLight.class, brazier ? "brazier" : "candelabra" );
	}

	@Override
	public String description() {
		String d = Messages.get( CastleLight.class, brazier ? "desc_brazier" : "desc_candelabra" );
		d += "\n\n" + Messages.get( CastleLight.class, lit ? (permanent ? "state_eternal" : "state_lit") : "state_unlit" );
		return d;
	}

	private static final String BRAZIER = "brazier";
	private static final String LIT = "lit";
	private static final String PERM = "permanent";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( BRAZIER, brazier );
		bundle.put( LIT, lit );
		bundle.put( PERM, permanent );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		brazier = bundle.getBoolean( BRAZIER );
		lit = bundle.getBoolean( LIT );
		permanent = bundle.getBoolean( PERM );
	}
}
