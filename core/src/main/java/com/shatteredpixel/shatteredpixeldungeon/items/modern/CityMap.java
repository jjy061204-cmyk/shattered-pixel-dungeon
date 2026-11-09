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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.ModernLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CityMap extends ModernConsumable {

	{
		image = ItemSpriteSheet.CITY_MAP;
	}

	//현대 세계의 진짜 지도: 현대 세계 안에서만 길을 찾을 수 있음
	@Override
	protected boolean canUse( Hero hero ) {
		if (!(Dungeon.level instanceof ModernLevel)) {
			GLog.w( Messages.get( this, "no_dungeon" ) );
			return false;
		}
		return true;
	}

	@Override
	protected void apply( Hero hero ) {
		int length = Dungeon.level.length();
		for (int i = 0; i < length; i++) {
			if (Dungeon.level.discoverable[i]) {
				Dungeon.level.mapped[i] = true;
				int terr = Dungeon.level.map[i];
				if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {
					Dungeon.level.discover( i );
					if (Dungeon.level.heroFOV[i]) GameScene.discoverTile( i, terr );
				}
			}
		}
		GameScene.updateFog();

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
