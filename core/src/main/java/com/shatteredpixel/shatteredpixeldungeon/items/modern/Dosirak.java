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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;

//편의점 도시락: 한 끼 식사. 먹는 데 오래 걸림
public class Dosirak extends Food {

	{
		image = ItemSpriteSheet.DOSIRAK;
		energy = 300f;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		ModernEffects.heal( hero, 0.10f );
		Buff.affect( hero, WellFed.class ).setTurns( 50 );
	}

	@Override
	protected float eatingTime() {
		return super.eatingTime() + 3f;
	}

	@Override
	public int value() {
		return 25 * quantity;
	}
}
