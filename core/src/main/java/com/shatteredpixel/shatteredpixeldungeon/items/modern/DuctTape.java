/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class DuctTape extends ModernConsumable {

	{
		image = ItemSpriteSheet.DUCT_TAPE;
	}

	@Override
	protected void apply( Hero hero ) {
		Buff.affect( hero, Barrier.class ).setShield( Math.max( 1, Math.round( hero.HT * 0.2f ) ) );
	}

	@Override
	public int value() {
		return 15 * quantity;
	}
}
