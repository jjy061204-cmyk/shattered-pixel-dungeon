/*
 * Shattered Pixel Dungeon 모드: 현대 세계 가게 상품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//트렌치코트: 입으면 캐릭터 도트의 11번째 줄(현대 옷 그림)을 씀
public class TrenchCoat extends Armor {

	{
		image = ItemSpriteSheet.TRENCH_COAT;
	}

	@Override
	public int visualTier() {
		return 11;
	}

	public TrenchCoat() {
		super( 3 );
	}
}
