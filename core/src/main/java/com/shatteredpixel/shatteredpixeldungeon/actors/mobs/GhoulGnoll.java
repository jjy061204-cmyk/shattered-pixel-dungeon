/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 몹: 구울 놀 (놀 브루트 자리) — 어둠 속에서 쓰러지면 3턴 뒤 한 번 되살아남. 쓰러진 동안 다시 치거나, 빛 안에서 죽거나 불타면 못 살아남
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NightBlood;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GhoulGnoll extends Mob {

	{
		spriteClass = VampireCastleSprites.GhoulGnoll.class;

		HP = HT = 40;
		defenseSkill = 15;

		EXP = 8;
		maxLvl = 16;

		loot = Gold.class;
		lootChance = 0.5f;

		properties.add( Property.UNDEAD );
	}

	//쓰러져 있는 남은 턴 (-1이면 서 있음)
	private int fallen = -1;
	private boolean revived = false;

	@Override
	protected void onAdd() {
		super.onAdd();
		NightBlood.grant( this );
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 6, 24 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 20;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 8 );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return fallen >= 0 ? 0 : super.defenseSkill( enemy );
	}

	public boolean isFallen(){
		return fallen >= 0;
	}

	@Override
	public void die( Object cause ) {
		if (fallen < 0 && !revived && !VampireCastle.isLit( pos )
				&& buff( Burning.class ) == null && !(cause instanceof Burning)){
			//쓰러지지만 아직 죽지 않음
			revived = true;
			fallen = 3;
			HP = 1;
			if (sprite instanceof VampireCastleSprites.GhoulGnoll){
				((VampireCastleSprites.GhoulGnoll) sprite).crumple();
			}
			if (Dungeon.level.heroFOV[pos]){
				GLog.w( Messages.get( GhoulGnoll.class, "fallen" ) );
			}
			return;
		}
		fallen = -1;
		super.die( cause );
	}

	@Override
	protected boolean act() {
		if (fallen >= 0){
			if (VampireCastle.isLit( pos ) || buff( Burning.class ) != null){
				if (Dungeon.level.heroFOV[pos]){
					CellEmitter.get( pos ).burst( ShadowParticle.UP, 8 );
					GLog.p( Messages.get( GhoulGnoll.class, "ashes" ) );
				}
				die( this );
				return true;
			}
			fallen--;
			if (fallen <= 0){
				fallen = -1;
				HP = Math.max( HP, HT / 2 );
				if (sprite != null) sprite.idle();
				if (Dungeon.level.heroFOV[pos]){
					CellEmitter.get( pos ).burst( ShadowParticle.CURSE, 8 );
					GLog.w( Messages.get( GhoulGnoll.class, "rises" ) );
				}
			}
			spend( TICK );
			return true;
		}
		return super.act();
	}

	@Override
	public String description() {
		String d = super.description();
		if (fallen >= 0) d += "\n\n" + Messages.get( GhoulGnoll.class, "desc_fallen" );
		return d;
	}

	private static final String FALLEN = "fallen";
	private static final String REVIVED = "revived";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( FALLEN, fallen );
		bundle.put( REVIVED, revived );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		fallen = bundle.contains( FALLEN ) ? bundle.getInt( FALLEN ) : -1;
		revived = bundle.getBoolean( REVIVED );
	}
}
