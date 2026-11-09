/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class EyeDrops extends ModernConsumable {

	{
		image = ItemSpriteSheet.EYE_DROPS;
	}

	@Override
	protected void apply( Hero hero ) {
		Buff.detach( hero, Blindness.class );
		Dungeon.observe();
		GameScene.updateFog();
		//눈이 맑아져서 주변을 꼼꼼히 살펴봄 (숨겨진 문이나 함정 찾기)
		hero.search( true );
	}

	@Override
	public int value() {
		return 14 * quantity;
	}
}
