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
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

//낱말 퍼즐: 문제집보다 가볍게 머리를 씀
public class Crossword extends ModernConsumable {

	{
		image = ItemSpriteSheet.CROSSWORD;
	}

	@Override
	protected void apply( Hero hero ) {
		hero.earnExp( Math.max( 1, hero.maxExp() / 6 ), getClass() );
	}

	@Override
	protected String useSound() {
		return Assets.Sounds.READ;
	}

	@Override
	public int value() {
		return 12 * quantity;
	}
}
