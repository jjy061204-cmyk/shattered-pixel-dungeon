/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 - 석상 병사
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SanctuaryGolemSprite;
import com.watabou.utils.Random;

//검과 방패를 든 돌 석상. 드워프 도시의 구울·수도승 정도의 힘
public class StoneSoldier extends Mob {

	{
		spriteClass = SanctuaryGolemSprite.Soldier.class;

		HP = HT = 80;
		defenseSkill = 20;

		EXP = 10;
		maxLvl = 26;

		loot = Generator.Category.WEAPON;
		lootChance = 0.1f;

		properties.add( Property.INORGANIC );
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 16, 24 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 28;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 8 );
	}
}
