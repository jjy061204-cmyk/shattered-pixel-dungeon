/*
 * Shattered Pixel Dungeon 모드: 자판기 음료
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;

//캔커피: 졸음을 쫓고 잠깐 빨라짐
public class CanCoffee extends Food {

	{
		image = ItemSpriteSheet.CAN_COFFEE;
		energy = 20f;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		Buff.detach( hero, Drowsy.class );
		Buff.prolong( hero, Haste.class, 3f );
	}

	@Override
	protected void eatSFX() {
		Sample.INSTANCE.play( Assets.Sounds.DRINK );
	}

	@Override
	public int value() {
		return 10 * quantity;
	}
}
