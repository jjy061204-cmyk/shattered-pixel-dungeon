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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;

//군밤: 종이봉투 한가득
public class Chestnuts extends Food {

	{
		image = ItemSpriteSheet.CHESTNUTS;
		energy = 100f;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		Buff.detach( hero, Chill.class );
		ModernEffects.heal( hero, 0.05f );
	}

	@Override
	protected float eatingTime() {
		return 1f;
	}

	@Override
	public int value() {
		return 10 * quantity;
	}
}
