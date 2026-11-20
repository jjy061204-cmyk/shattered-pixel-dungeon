/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 꾸미기
 *  - 바닥 장식(깨진 판석·핏자국, 지하 묘지는 흩어진 뼈)은 방 구석일수록 잦음
 *  - 방을 바라보는 벽면 다섯에 하나꼴로 창·태피스트리(지하 묘지는 해골 벽감·비문)
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
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CastlePainter extends RegularPainter {

	@Override
	protected void decorate( Level level, ArrayList<Room> rooms ) {

		int w = level.width();
		int l = level.length();
		int[] map = level.map;

		for (int i = w + 1; i < l - w - 1; i++){
			if (map[i] != Terrain.EMPTY) continue;
			float c = 0.04f;
			if (map[i + 1] == Terrain.WALL && map[i + w] == Terrain.WALL) c += 0.15f;
			if (map[i - 1] == Terrain.WALL && map[i + w] == Terrain.WALL) c += 0.15f;
			if (map[i + 1] == Terrain.WALL && map[i - w] == Terrain.WALL) c += 0.15f;
			if (map[i - 1] == Terrain.WALL && map[i - w] == Terrain.WALL) c += 0.15f;
			if (Random.Float() < c){
				map[i] = Terrain.EMPTY_DECO;
			}
		}

		for (int i = w; i < l - w; i++){
			if (map[i] == Terrain.WALL && map[i - w] == Terrain.WALL
					&& (map[i + w] == Terrain.EMPTY || map[i + w] == Terrain.EMPTY_SP || map[i + w] == Terrain.EMPTY_DECO)
					&& Random.Int( 5 ) == 0){
				map[i] = Terrain.WALL_DECO;
			}
		}
	}
}
