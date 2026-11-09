/*
 * Shattered Pixel Dungeon 모드: 나무 검
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//1티어 나무 검. 가벼워서 빠르게 휘두르지만 날이 없어 피해는 조금 낮음.
//결투가 능력은 낡은 소검과 같은 '가르기'.
public class WoodenSword extends WornShortsword {

	{
		image = ItemSpriteSheet.WOODEN_SWORD;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1.4f;

		tier = 1;
		DLY = 0.8f;       //공격 속도 25% 빠름

		bones = true;
	}

	//최대 피해 8 + 강화당 2 (보통 1티어는 10 + 2)
	@Override
	public int max( int lvl ) {
		return 4 * (tier + 1) + lvl * (tier + 1);
	}
}
