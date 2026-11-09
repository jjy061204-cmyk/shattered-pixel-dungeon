/*
 * Shattered Pixel Dungeon 모드
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AdrenalineSurge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

//바나나 우유: 배고픔 50 해소, 100턴 동안 힘 +1
public class BananaMilk extends Food {

	public static final float FOOD_VALUE = 50f;
	public static final int   STR_BOOST  = 1;
	public static final float BOOST_TURNS = 100f;

	{
		image = ItemSpriteSheet.BANANA_MILK;
		energy = FOOD_VALUE;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		//이미 더 강한 힘 증가(아드레날린)가 있으면 덮어쓰지 않음
		AdrenalineSurge surge = hero.buff( AdrenalineSurge.class );
		if (surge == null || surge.boost() <= STR_BOOST) {
			Buff.affect( hero, AdrenalineSurge.class ).reset( STR_BOOST, BOOST_TURNS );
		}
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
