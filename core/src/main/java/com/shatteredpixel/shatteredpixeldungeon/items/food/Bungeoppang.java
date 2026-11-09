/*
 * Shattered Pixel Dungeon 모드: 붕어빵
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

//따끈한 붕어빵. 먹으면 따뜻한 기운이 보호막이 되어 몸을 감쌈.
//가끔 꼬리까지 팥이 꽉 찬 붕어빵이 걸리면 보호막이 2배.
public class Bungeoppang extends Food {

	//배고픔 150 해소
	public static final float FOOD_VALUE    = 150f;
	//최대 체력의 15%만큼 보호막
	public static final float SHIELD_FACTOR = 0.15f;
	//꼬리까지 꽉 찬 붕어빵이 걸릴 확률
	public static final float FULL_TAIL_CHANCE = 0.25f;

	{
		image = ItemSpriteSheet.BUNGEOPPANG;
		energy = FOOD_VALUE;

		//여러 개를 한 칸에 겹쳐 들 수 있음 (Food 기본값)
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );

		int shield = Math.max(1, Math.round( hero.HT * SHIELD_FACTOR ));
		if (Random.Float() < FULL_TAIL_CHANCE) {
			shield *= 2;
			GLog.p( Messages.get(this, "full_tail") );
		}
		Buff.affect( hero, Barrier.class ).setShield( shield );
	}

	@Override
	public int value() {
		return 8 * quantity;
	}
}
