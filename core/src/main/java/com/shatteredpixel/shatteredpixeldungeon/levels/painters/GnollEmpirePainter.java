/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 층 꾸미기: 동굴과 같되, 벽의 금맥 대신 놀 깃발·해골 장식이 드문드문 걸림
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.painters;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollEmpirePainter extends CavesPainter {

	@Override
	protected void generateGold( Level level, ArrayList<Room> rooms ) {
		int w = level.width();
		int l = level.length();
		int[] map = level.map;

		for (int i = 0; i < l - w; i++){
			if (map[i] == Terrain.WALL
					&& DungeonTileSheet.floorTile( map[i + w] )
					&& Random.Int( 9 ) == 0){
				map[i] = Terrain.WALL_DECO;
			}
		}
	}
}
