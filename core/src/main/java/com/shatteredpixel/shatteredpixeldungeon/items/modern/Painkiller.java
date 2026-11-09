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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Painkiller extends ModernConsumable {

	{
		image = ItemSpriteSheet.PAINKILLER;
	}

	@Override
	protected void apply( Hero hero ) {
		int amount = Math.max( 1, Math.round( hero.HT * 0.35f ) );
		Buff.affect( hero, Healing.class ).setHeal( amount, 0f, (int)Math.ceil( amount / 10f ) );
	}

	@Override
	protected String useSound() {
		return Assets.Sounds.DRINK;
	}

	@Override
	public int value() {
		return 20 * quantity;
	}
}
