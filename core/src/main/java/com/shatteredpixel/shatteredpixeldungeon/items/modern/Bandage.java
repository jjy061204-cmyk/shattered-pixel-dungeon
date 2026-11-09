/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Bandage extends ModernConsumable {

	{
		image = ItemSpriteSheet.BANDAGE;
	}

	@Override
	protected void apply( Hero hero ) {
		Buff.detach( hero, Bleeding.class );
		ModernEffects.heal( hero, 0.15f );
	}

	@Override
	public int value() {
		return 10 * quantity;
	}
}
