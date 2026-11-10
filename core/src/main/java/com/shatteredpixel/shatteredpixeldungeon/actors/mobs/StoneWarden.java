/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 - 석상 수호병
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

//커다란 주먹의 돌 골렘. 느리지만 단단하고 아프게 때림 (드워프 도시 골렘보다 조금 약함)
public class StoneWarden extends Mob {

	{
		spriteClass = SanctuaryGolemSprite.Warden.class;

		HP = HT = 130;
		defenseSkill = 12;
		baseSpeed = 0.75f;

		EXP = 13;
		maxLvl = 26;

		loot = Generator.Category.ARMOR;
		lootChance = 0.1f;

		properties.add( Property.INORGANIC );
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 22, 30 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 26;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 14 );
	}
}
