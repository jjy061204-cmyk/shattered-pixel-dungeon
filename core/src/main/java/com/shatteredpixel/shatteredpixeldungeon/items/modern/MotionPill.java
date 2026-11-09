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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;

//멀미약: 어지러움과 멍함을 가라앉힘
public class MotionPill extends ModernConsumable {

	{
		image = ItemSpriteSheet.MOTION_PILL;
	}

	@Override
	protected void apply( Hero hero ) {
		Buff.detach( hero, Vertigo.class );
		Buff.detach( hero, Daze.class );
	}

	@Override
	protected String useSound() {
		return Assets.Sounds.DRINK;
	}

	@Override
	public int value() {
		return 12 * quantity;
	}
}
