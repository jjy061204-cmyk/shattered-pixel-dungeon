/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ChaoticCenser;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.SaltCube;
import com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel;
import com.watabou.utils.Bundle;

public class Regeneration extends Buff {
	
	{
		//unlike other buffs, this one acts after the hero and takes priority against other effects
		//healing is much more useful if you get some of it off before taking damage
		actPriority = HERO_PRIO - 1;
	}

	private float partialRegen = 0f;

	private static final float REGENERATION_DELAY = 10; //1HP every 10 turns
	
	@Override
	public boolean act() {
		if (target.isAlive()) {

			//모드: 뱀파이어가 흡혈귀의 성에 있으면 '성의 혈족' 표시
			com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle.updateKinship();

			//if other trinkets ever get buffs like this should probably make the buff attaching
			// behaviour more like wands/rings/artifacts
			if (ChaoticCenser.averageTurnsUntilGas() != -1){
				Buff.affect(Dungeon.hero, ChaoticCenser.CenserGasTracker.class);
			}

			if (regenOn() && target.HP < regencap() && !((Hero)target).isStarving()) {
				boolean chaliceCursed = false;
				int chaliceLevel = -1;
				if (target.buff(MagicImmune.class) == null) {
					if (Dungeon.hero.buff(ChaliceOfBlood.chaliceRegen.class) != null) {
						chaliceCursed = Dungeon.hero.buff(ChaliceOfBlood.chaliceRegen.class).isCursed();
						chaliceLevel = Dungeon.hero.buff(ChaliceOfBlood.chaliceRegen.class).itemLevel();
					} else if (Dungeon.hero.buff(SpiritForm.SpiritFormBuff.class) != null
							&& Dungeon.hero.buff(SpiritForm.SpiritFormBuff.class).artifact() instanceof ChaliceOfBlood) {
						chaliceLevel = SpiritForm.artifactLevel();
					}
				}

				float delay = REGENERATION_DELAY;
				if (chaliceLevel != -1 && target.buff(MagicImmune.class) == null) {
					if (chaliceCursed) {
						delay *= 1.5f;
					} else {
						//15% boost at +0, scaling to a 500% boost at +10
						delay -= 1.33f + chaliceLevel*0.667f;
						delay /= RingOfEnergy.artifactChargeMultiplier(target);
						//모드: 유물수집가는 성배의 재생을 더 끌어냄
						delay /= com.shatteredpixel.shatteredpixeldungeon.actors.hero.CollectorBonus.chaliceRegenMultiplier(Dungeon.hero);
					}
				}

				//salt cube is turned off while regen is disabled.
				if (target.buff(LockedFloor.class) == null) {
					delay /= SaltCube.healthRegenMultiplier();
				}

				//모드: 뱀파이어는 타고난 생명력으로 자연 재생 +50%
				if (((Hero)target).heroClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.VAMPIRE) delay /= 1.5f;
				//모드: 흡혈귀의 성은 뱀파이어의 고향 같은 곳 - 자연 재생 +20%
				if (com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle.vampireHome()) delay /= com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle.HOME_REGEN;

				//모드: 진조 흡혈귀의 '불사의 육체' - 자연 재생 +25/50/75%
				delay /= 1f + 0.25f * Dungeon.hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.UNDYING_FLESH);

				partialRegen += 1f / delay;

				if (partialRegen >= 1) {
					target.HP += (int)partialRegen;
					partialRegen -= (int)partialRegen;
					if (target.HP >= regencap()) {
						target.HP = regencap();
						((Hero) target).resting = false;
					}
				}

			}

			spend( TICK );
			
		} else {
			
			diactivate();
			
		}
		
		return true;
	}
	
	public int regencap(){
		return target.HT;
	}

	public static boolean regenOn(){
		LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
		if (lock != null && !lock.regenOn()){
			return false;
		}
		return true;
	}

	public static final String PARTIAL_REGEN = "partial_regen";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PARTIAL_REGEN, partialRegen);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		partialRegen = bundle.getFloat(PARTIAL_REGEN);
	}
}
