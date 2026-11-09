/*
 * Shattered Pixel Dungeon 모드: 먹자골목 노점 음식
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//호떡: 꿀이 든 달콤한 빵
public class Hotteok extends Food {

	{
		image = ItemSpriteSheet.HOTTEOK;
		energy = 120f;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		ModernEffects.heal( hero, 0.10f );
	}

	@Override
	public int value() {
		return 10 * quantity;
	}
}
