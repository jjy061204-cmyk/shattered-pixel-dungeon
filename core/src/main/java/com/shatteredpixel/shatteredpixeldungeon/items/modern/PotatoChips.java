/*
 * Shattered Pixel Dungeon 모드: 현대 세계 가게 상품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

//감자칩: 가벼운 간식
public class PotatoChips extends Food {

	{
		image = ItemSpriteSheet.POTATO_CHIPS;
		energy = 60f;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		ModernEffects.heal( hero, 0.03f );
	}

	@Override
	protected float eatingTime() {
		return 1f;
	}

	@Override
	public int value() {
		return 8 * quantity;
	}
}
