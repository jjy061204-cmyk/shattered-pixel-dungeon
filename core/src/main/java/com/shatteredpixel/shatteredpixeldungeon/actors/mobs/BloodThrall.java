/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 몹: 피의 하수인 — 관에서 기어 나오는 약한 하인. 보이는 불(촛대·화로)을 끄러 다님
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NightBlood;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.watabou.utils.Random;

public class BloodThrall extends Mob {

	{
		spriteClass = VampireCastleSprites.BloodThrall.class;

		HP = HT = 25;
		defenseSkill = 12;

		EXP = 4;
		maxLvl = 15;

		properties.add( Property.UNDEAD );
	}

	@Override
	protected void onAdd() {
		super.onAdd();
		NightBlood.grant( this );
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 5, 12 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 18;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 3 );
	}

	//가까이 보이는, 끌 수 있는 불
	private CastleLight targetLight(){
		CastleLight best = null;
		for (Mob m : Dungeon.level.mobs){
			if (!(m instanceof CastleLight) || !m.isAlive()) continue;
			CastleLight l = (CastleLight) m;
			if (!l.lit || l.permanent) continue;
			int d = Dungeon.level.distance( pos, l.pos );
			if (d > 8 || !fieldOfView[l.pos]) continue;
			if (best == null || d < Dungeon.level.distance( pos, best.pos )) best = l;
		}
		return best;
	}

	@Override
	protected boolean act() {
		//떠도는 동안은 불부터 끄러 감
		if (state == WANDERING && paralysed <= 0){
			CastleLight l = targetLight();
			if (l != null){
				if (Dungeon.level.adjacent( pos, l.pos )){
					if (sprite != null && Dungeon.level.heroFOV[pos]) sprite.operate( l.pos );
					l.snuff( this );
					spend( TICK );
					return true;
				}
				target = l.pos;
			}
		}
		return super.act();
	}
}
