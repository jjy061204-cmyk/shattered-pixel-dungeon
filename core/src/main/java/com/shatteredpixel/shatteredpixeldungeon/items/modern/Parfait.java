/*
 * Shattered Pixel Dungeon 모드: 현대 물품 (실험용 32x32 도트)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//딸기 파르페. 그림은 32x32로 그려서 일반 아이템보다 두 배 촘촘하게 보임 (ItemSprite 참고)
public class Parfait extends Food {

	{
		image = ItemSpriteSheet.PARFAIT;
		energy = 150f;
		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );
		ModernEffects.heal( hero, 0.20f );
		Buff.affect( hero, WellFed.class ).setTurns( 20 );
	}

	@Override
	public int value() {
		return 25 * quantity;
	}
}
