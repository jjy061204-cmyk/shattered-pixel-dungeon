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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;

//순대: 든든함
public class Sundae extends Food {

	{
		image = ItemSpriteSheet.SUNDAE;
		energy = 200f;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		Buff.affect( hero, WellFed.class ).setTurns( 20 );
	}

	@Override
	public int value() {
		return 14 * quantity;
	}
}
