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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

//생존 안내서: 읽고 나면 주변의 숨은 함정을 알아차림
public class SurvivalGuide extends ModernConsumable {

	{
		image = ItemSpriteSheet.SURVIVAL_GUIDE;
	}

	@Override
	protected void apply( Hero hero ) {
		int found = 0;
		for (Trap t : Dungeon.level.traps.valueList()){
			if (!t.visible && Dungeon.level.distance( hero.pos, t.pos ) <= 8){
				t.reveal();
				GameScene.updateMap( t.pos );
				found++;
			}
		}
		GLog.i( Messages.get( this, found > 0 ? "found" : "none", found ) );
	}

	@Override
	protected String useSound() {
		return Assets.Sounds.READ;
	}

	@Override
	public int value() {
		return 25 * quantity;
	}
}
