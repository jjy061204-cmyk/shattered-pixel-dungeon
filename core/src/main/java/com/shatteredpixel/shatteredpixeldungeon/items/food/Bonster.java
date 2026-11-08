/*
 * Shattered Pixel Dungeon 모드: 본스터 (에너지 음료)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BonsterRush;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

//다른 차원에서 흘러들어온 에너지 음료
public class Bonster extends Food {

	//배고픔 50 해소
	public static final float FOOD_VALUE = 50f;

	{
		image = ItemSpriteSheet.BONSTER;
		energy = FOOD_VALUE;

		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		//다 마신 뒤(먹는 시간이 지난 뒤)부터 효과가 시작되도록 기다릴 시간을 넘겨줌
		Buff.affect( hero, BonsterRush.class ).set( eatingTime() );
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
