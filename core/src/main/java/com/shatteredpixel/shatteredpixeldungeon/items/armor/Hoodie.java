/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//옷가게 상품: 1티어 방어구
public class Hoodie extends Armor {

	{
		image = ItemSpriteSheet.HOODIE;
	}

	//입으면 캐릭터 도트의 7번째 줄(현대 옷 그림)을 씀
	@Override
	public int visualTier() {
		return 7;
	}

	public Hoodie() {
		super( 1 );
	}
}
