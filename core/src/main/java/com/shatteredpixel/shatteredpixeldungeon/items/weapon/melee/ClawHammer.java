/*
 * Shattered Pixel Dungeon 모드: 현대 세계 가게 상품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//장도리: 철물점에서 파는 3단계 무기
public class ClawHammer extends MeleeWeapon {

	{
		image = ItemSpriteSheet.CLAW_HAMMER;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 0.95f;

		tier = 3;
	}
}
