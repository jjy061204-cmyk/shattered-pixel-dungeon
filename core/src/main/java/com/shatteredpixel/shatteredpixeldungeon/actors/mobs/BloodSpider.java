/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 몹: 혈사 거미 (동굴 거미 자리) — 거미줄은 그대로, 물면 독 대신 약한 출혈
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NightBlood;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;

public class BloodSpider extends Spinner {

	{
		spriteClass = VampireCastleSprites.BloodSpider.class;
	}

	@Override
	protected void onAdd() {
		super.onAdd();
		NightBlood.grant( this );
	}

	@Override
	protected void venom( Char enemy, int duration ) {
		Buff.affect( enemy, Bleeding.class ).set( Math.round( duration * 0.7f ) );
	}
}
