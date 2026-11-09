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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;

//화상 연고: 불을 끄지는 못하지만 화상을 진정시킴
public class BurnOintment extends ModernConsumable {

	{
		image = ItemSpriteSheet.BURN_OINTMENT;
	}

	@Override
	protected void apply( Hero hero ) {
		Buff.detach( hero, Burning.class );
		ModernEffects.heal( hero, 0.10f );
	}

	@Override
	protected String useSound() {
		return Assets.Sounds.ITEM;
	}

	@Override
	public int value() {
		return 15 * quantity;
	}
}
