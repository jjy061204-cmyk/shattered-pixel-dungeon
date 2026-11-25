/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 수정 광맥 꾸미기: 원작 광산 꾸미기에, 살아 있는 수정 광맥이 광산 전체를 뒤덮은 모습을 더함
 *  - 금맥 대신 통째로 수정인 수정맥 벽이 원작 금맥보다 자주, 옆으로 바닥을 마주한 벽에도 지나감
 *  - 넓은 방 안쪽 모서리에 수정 기둥이 돋음 (원작 광산이 모서리를 바위로 메우는 것과 같은 조건이라 길을 막지 않음)
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
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CrystalVeinPainter extends CavesPainter {

	@Override
	protected void generateGold(Level level, ArrayList<Room> rooms) {
		int w = level.width();
		int l = level.length();
		int[] map = level.map;

		//바닥을 마주한 벽은 셋 중 하나가 수정맥 (원작 금맥은 넷 중 하나)
		for (int i = 0; i < l - w; i++) {
			if (map[i] == Terrain.WALL && DungeonTileSheet.floorTile(map[i + w]) && Random.Int(3) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}

		//옆으로 바닥을 마주한 벽에도 수정맥이 지나감 (위에서 보면 벽 테두리가 수정)
		for (int i = w; i < l - w; i++) {
			int x = i % w;
			if (map[i] == Terrain.WALL && x > 0 && x < w - 1
					&& (DungeonTileSheet.floorTile(map[i - 1]) || DungeonTileSheet.floorTile(map[i + 1]))
					&& Random.Int(5) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}

		//넓은 방 안쪽 모서리에 수정 기둥
		for (Room room : rooms) {
			if (!(room instanceof StandardRoom) || room instanceof BlacksmithRoom) continue;
			if (room.width() <= 4 || room.height() <= 4) continue;

			int[][] corners = {
					{room.left + 1, room.top + 1, -1, -1},
					{room.right - 1, room.top + 1, 1, -1},
					{room.left + 1, room.bottom - 1, -1, 1},
					{room.right - 1, room.bottom - 1, 1, 1}
			};
			for (int[] c : corners) {
				if (Random.Int(3) != 0) continue;
				int cell = c[0] + c[1] * w;
				int side = cell + c[2];
				int vert = cell + c[3] * w;
				if ((map[cell] == Terrain.EMPTY || map[cell] == Terrain.EMPTY_DECO)
						&& (map[side] == Terrain.WALL || map[side] == Terrain.WALL_DECO)
						&& !room.connected.containsValue(level.cellToPoint(side))
						&& (map[vert] == Terrain.WALL || map[vert] == Terrain.WALL_DECO)
						&& !room.connected.containsValue(level.cellToPoint(vert))
						&& map[cell - c[2]] != Terrain.TRAP && map[cell - c[3] * w] != Terrain.TRAP
						&& level.traps.get(cell) == null) {
					map[cell] = Terrain.REGION_DECO;
				}
			}
		}

		//수정맥·수정 기둥 둘레 바닥의 수정 번짐은 지형을 바꾸지 않고 그림(바닥 가장자리 번짐)으로 표현
	}
}
