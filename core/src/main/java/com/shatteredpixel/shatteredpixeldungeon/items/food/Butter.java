/*
 * Shattered Pixel Dungeon 모드
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//버터: 배고픔 300 해소(비상식량과 같음), 배부름 100턴
public class Butter extends Food {

	public static final float FOOD_VALUE    = 300f;
	public static final int   WELLFED_TURNS = 100;

	{
		image = ItemSpriteSheet.BUTTER;
		energy = FOOD_VALUE;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		Buff.affect( hero, WellFed.class ).setTurns( WELLFED_TURNS );
	}

	@Override
	public int value() {
		return 20 * quantity;
	}
}
