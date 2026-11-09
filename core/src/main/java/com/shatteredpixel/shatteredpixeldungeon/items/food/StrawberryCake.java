/*
 * Shattered Pixel Dungeon 모드: 딸기 케이크
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//호화로운 딸기 케이크
public class StrawberryCake extends Food {

	//배고픔 250 해소
	public static final float FOOD_VALUE  = 250f;
	//배부름 50턴
	public static final int   WELLFED_TURNS = 50;
	//10턴에 걸쳐 최대 체력의 50% 회복
	public static final float HEAL_FACTOR = 0.5f;
	public static final int   HEAL_TURNS  = 10;

	{
		image = ItemSpriteSheet.STRAWBERRY_CAKE;
		energy = FOOD_VALUE;

		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );

		Buff.affect( hero, WellFed.class ).setTurns( WELLFED_TURNS );

		int amount = Math.round( hero.HT * HEAL_FACTOR );
		if (amount > 0) {
			int perTurn = (int)Math.ceil( amount / (float)HEAL_TURNS );
			Buff.affect( hero, Healing.class ).setHeal( amount, 0f, perTurn );
		}
	}

	@Override
	public int value() {
		return 40 * quantity;
	}
}
