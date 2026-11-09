/*
 * Shattered Pixel Dungeon 모드: 음식 바구니 (음식 전용 가방)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//모든 영웅이 처음부터 들고 시작하는 음식 전용 가방. 음식과 음료(Food 계열)만 담김
public class FoodBag extends Bag {

	{
		image = ItemSpriteSheet.FOOD_BAG;
	}

	@Override
	public boolean canHold( Item item ) {
		if (item instanceof Food){
			return super.canHold( item );
		} else {
			return false;
		}
	}

	public int capacity(){
		return 19;
	}

	@Override
	public int value() {
		return 30;
	}
}
