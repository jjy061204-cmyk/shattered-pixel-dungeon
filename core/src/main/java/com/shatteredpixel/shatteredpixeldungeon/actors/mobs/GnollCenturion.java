/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 놀 백부장 (원작 14층 희귀 몹 구울 자리, 2.5%)
 *  - 깃발을 든 엘리트. 4칸 안에 보이는 다른 놀의 공격력 +20% (지휘)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpire;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Random;

public class GnollCenturion extends Mob {

	public static final int COMMAND_RANGE = 4;

	{
		spriteClass = GnollEmpireSprites.Centurion.class;

		HP = HT = 45;
		defenseSkill = 20;

		EXP = 10;
		maxLvl = 20;

		loot = Gold.class;
		lootChance = 0.2f;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 15, 21 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 24;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 5 );
	}

	@Override
	protected boolean act() {
		boolean result = super.act();
		if (isAlive() && alignment == Alignment.ENEMY && state != SLEEPING){
			command( this, COMMAND_RANGE );
		}
		return result;
	}

	//둘레의 놀(자기 제외)에게 지휘 효과를 걸어 줌. 놀 대족장의 결집에서도 씀
	public static void command( Mob source, int range ){
		if (source.fieldOfView == null) return;
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m != source && m.isAlive() && m.alignment == source.alignment && GnollEmpire.isGnoll( m )
					&& Dungeon.level.distance( source.pos, m.pos ) <= range
					&& m.pos < source.fieldOfView.length && source.fieldOfView[m.pos]){
				Buff.prolong( m, Command.class, 2f );
			}
		}
	}

	//지휘를 받는 놀: 공격력 +20% (Char.attack에서 반영)
	public static class Command extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.UPGRADE;
		}

		@Override
		public void tintIcon( com.watabou.noosa.Image icon ) {
			icon.hardlight( 1f, 0.35f, 0.2f );
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc" );
		}
	}
}
