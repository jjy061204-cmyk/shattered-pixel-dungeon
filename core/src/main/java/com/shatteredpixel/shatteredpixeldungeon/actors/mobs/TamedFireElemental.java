/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사가 숲의 완드로 길들인 갓 태어난 화염 정령: 동료가 되어 영웅을 따라다니며 층을 옮겨도 함께 감
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ElementalSprite;

public class TamedFireElemental extends Elemental.FireElemental {

	{
		spriteClass = ElementalSprite.NewbornFire.class;

		alignment = Alignment.ALLY;
		intelligentAlly = true;
		WANDERING = new Following();
		state = WANDERING;

		EXP = 0;
		loot = null;
		lootChance = 0;

		//영웅보다 먼저 움직여 길을 막지 않게
		actPriority = MOB_PRIO + 1;

		setSummonedALly();
		HP = HT;
	}

	@Override
	public boolean reset() {
		return true;
	}

	//적이 없으면 언제나 영웅 곁으로 다가감
	private class Following extends Mob.Wandering {

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV) {
				enemySeen = true;
				notice();
				alerted = true;
				state = HUNTING;
				target = enemy.pos;
			} else {
				enemySeen = false;
				int oldPos = pos;
				target = Dungeon.hero.pos;
				if (Dungeon.level.distance( pos, target ) > 1 && getCloser( target )) {
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				} else {
					spend( TICK );
				}
			}
			return true;
		}
	}
}
