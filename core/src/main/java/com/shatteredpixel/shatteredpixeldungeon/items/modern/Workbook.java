/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Workbook extends ModernConsumable {

	{
		image = ItemSpriteSheet.WORKBOOK;
	}

	@Override
	protected void apply( Hero hero ) {
		hero.earnExp( Math.max( 1, hero.maxExp() / 3 ), getClass() );
	}

	@Override
	protected String useSound() {
		return Assets.Sounds.READ;
	}

	@Override
	public int value() {
		return 30 * quantity;
	}
}
