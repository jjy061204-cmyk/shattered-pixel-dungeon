/*
 * Shattered Pixel Dungeon 모드: 컵 커피
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

//다른 차원에서 흘러들어온 컵 커피
public class CupCoffee extends Food {

	//배고픔 50 해소
	public static final float FOOD_VALUE = 50f;
	//신속 10턴
	public static final float HASTE_TURNS = 10f;
	//축복 2턴
	public static final float BLESS_TURNS = 2f;

	{
		image = ItemSpriteSheet.CUP_COFFEE;
		energy = FOOD_VALUE;

		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		Buff.affect( hero, Haste.class, HASTE_TURNS );
		Buff.affect( hero, Bless.class, BLESS_TURNS );
	}

	//음식 대신 물약 마시는 소리
	@Override
	protected void eatSFX() {
		Sample.INSTANCE.play( Assets.Sounds.DRINK );
	}

	@Override
	public int value() {
		return 15 * quantity;
	}
}
