/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class PainPatch extends ModernConsumable {

	{
		image = ItemSpriteSheet.PAIN_PATCH;
	}

	@Override
	protected void apply( Hero hero ) {
		Buff.detach( hero, Cripple.class );
		Buff.detach( hero, Slow.class );
		Buff.detach( hero, Weakness.class );
		ModernEffects.heal( hero, 0.05f );
	}

	@Override
	public int value() {
		return 14 * quantity;
	}
}
