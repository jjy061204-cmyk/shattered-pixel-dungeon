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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class ComicBook extends ModernConsumable {

	{
		image = ItemSpriteSheet.COMIC_BOOK;
	}

	@Override
	protected void apply( Hero hero ) {
		Buff.detach( hero, Terror.class );
		Buff.detach( hero, Dread.class );
		Buff.detach( hero, Amok.class );
		Buff.detach( hero, Charm.class );
		Buff.affect( hero, Bless.class, 5f );
	}

	@Override
	protected String useSound() {
		return Assets.Sounds.READ;
	}

	@Override
	public int value() {
		return 15 * quantity;
	}
}
