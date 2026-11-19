/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 대족장 투기장의 전투 깃발 (원작 DM-300의 기둥과 같은 역할)
 *  - 평소에는 꽂혀 있기만 함. 대족장이 결집하면 깃발이 일어서고, 그 깃발이 서 있는 동안 대족장은 무적
 *  - 일어선 깃발 둘레 3칸의 놀은 공격력 +20%
 *  - 깃발을 부수면 결집이 풀림
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.watabou.noosa.audio.Sample;

public class WarBanner extends Mob {

	{
		spriteClass = GnollEmpireSprites.Banner.class;

		HP = HT = Dungeon.isChallenged( Challenges.STRONGER_BOSSES ) ? 80 : 50;

		EXP = 0;
		maxLvl = -2;

		properties.add( Property.MINIBOSS );
		properties.add( Property.BOSS_MINION );
		properties.add( Property.INORGANIC );
		properties.add( Property.IMMOVABLE );
		properties.add( Property.STATIC );

		state = PASSIVE;
		alignment = Alignment.NEUTRAL;
	}

	public boolean active(){
		return alignment != Alignment.NEUTRAL;
	}

	@Override
	protected boolean act() {
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );
		throwItems();

		if (active()){
			GnollCenturion.command( this, 3 );
		}
		spend( TICK );
		return true;
	}

	public void activate(){
		alignment = Alignment.ENEMY;
		state = HUNTING; //아군이 이 깃발을 공격하도록
		if (sprite instanceof GnollEmpireSprites.Banner) ((GnollEmpireSprites.Banner) sprite).activate();
		if (Dungeon.level.heroFOV[pos]){
			CellEmitter.center( pos ).burst( Speck.factory( Speck.UP ), 6 );
		}
	}

	@Override
	public CharSprite sprite() {
		CharSprite s = super.sprite();
		if (active() && s instanceof GnollEmpireSprites.Banner) ((GnollEmpireSprites.Banner) s).activate();
		return s;
	}

	@Override
	public void beckon( int cell ) {
	}

	@Override
	public boolean interact( Char c ) {
		return true;
	}

	@Override
	public boolean add( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff ) {
		return active() && super.add( buff );
	}

	@Override
	public boolean isInvulnerable( Class effect ) {
		return !active() || super.isInvulnerable( effect );
	}

	@Override
	public void damage( int dmg, Object src ) {
		//한 번에 큰 피해는 줄여 받음 (원작 기둥과 같음)
		if (dmg >= 15){
			dmg = 14 + (int)(Math.sqrt( 8*(dmg - 14) + 1 ) - 1)/2;
		}
		LockedFloor lock = Dungeon.hero.buff( LockedFloor.class );
		if (lock != null && active() && !isImmune( src.getClass() ) && !isInvulnerable( src.getClass() )){
			if (Dungeon.isChallenged( Challenges.STRONGER_BOSSES )) lock.addTime( dmg/2f );
			else                                                    lock.addTime( dmg );
		}
		super.damage( dmg, src );
	}

	@Override
	public void die( Object cause ) {
		super.die( cause );
		Sample.INSTANCE.play( Assets.Sounds.TRAMPLE, 1f, 0.8f );
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m instanceof GnollWarlord){
				((GnollWarlord) m).bannerFallen( this );
			}
		}
	}

	@Override
	public String description() {
		return Messages.get( this, active() ? "desc_active" : "desc_inactive" );
	}

	{
		immunities.add( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class );
	}
}
