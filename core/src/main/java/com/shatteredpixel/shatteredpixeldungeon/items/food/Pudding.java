/*
 * Shattered Pixel Dungeon 모드
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//푸딩: 배고픔 100 해소, 먹으면 해로운 상태이상(독, 불구, 약화, 취약, 출혈, 실명, 졸음, 둔화, 현기증)을 모두 없앰
public class Pudding extends Food {

	public static final float FOOD_VALUE = 100f;

	{
		image = ItemSpriteSheet.PUDDING;
		energy = FOOD_VALUE;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		PotionOfHealing.cure( hero );
	}

	@Override
	public int value() {
		return 15 * quantity;
	}
}
