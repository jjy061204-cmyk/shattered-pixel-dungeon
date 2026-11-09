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

//어묵꼬치: 따끈한 국물
public class Odeng extends Food {

	{
		image = ItemSpriteSheet.ODENG;
		energy = 80f;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		Buff.detach( hero, Chill.class );
		ModernEffects.heal( hero, 0.10f );
	}

	@Override
	protected float eatingTime() {
		return 1f;
	}

	@Override
	public int value() {
		return 6 * quantity;
	}
}
