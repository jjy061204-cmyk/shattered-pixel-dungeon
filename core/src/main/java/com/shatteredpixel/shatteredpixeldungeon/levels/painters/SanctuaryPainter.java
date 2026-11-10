/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 장식
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
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sanctuary.SanctuaryArenaRoom;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;

//오래된 유적 느낌: 금 간 바닥, 룬이 새겨진 벽
public class SanctuaryPainter extends RegularPainter {

	//20층 기준이면 문이 전부 숨겨지므로 성소는 따로 낮춤
	@Override
	public float hiddenDoorChance( Level l ) {
		return 0.15f;
	}

	@Override
	protected void decorate( Level level, ArrayList<Room> rooms ) {

		int[] map = level.map;
		int w = level.width();
		int l = level.length();

		for (int i = w; i < l - w; i++) {

			if (inArena( level, rooms, i )) continue;

			if (map[i] == Terrain.EMPTY && Random.Int( 9 ) == 0) {
				map[i] = Terrain.EMPTY_DECO;

			} else if (map[i] == Terrain.WALL
					&& !DungeonTileSheet.wallStitcheable( map[i + w] )
					&& Random.Int( 7 ) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}
	}

	private static boolean inArena( Level level, ArrayList<Room> rooms, int cell ){
		for (Room r : rooms){
			if (r instanceof SanctuaryArenaRoom && r.inside( level.cellToPoint( cell ) )) return true;
		}
		return false;
	}
}
