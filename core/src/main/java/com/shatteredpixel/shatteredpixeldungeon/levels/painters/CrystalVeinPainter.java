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

		//수정맥은 흩뿌리지 않고 몇 군데에 몰려 있음: 바닥을 마주한 벽 중 몇 곳에서 시작해 가로로 1~3칸 이어짐
		for (int i = w; i < l - w; i++) {
			if (map[i] == Terrain.WALL && DungeonTileSheet.floorTile(map[i + w]) && Random.Int(9) == 0) {
				map[i] = Terrain.WALL_DECO;
				int x = i % w;
				int left = Random.Int(3), right = Random.Int(3);
				for (int k = 1; k <= left && x - k > 0; k++) {
					int c = i - k;
					if (map[c] != Terrain.WALL || !DungeonTileSheet.floorTile(map[c + w])) break;
					map[c] = Terrain.WALL_DECO;
				}
				for (int k = 1; k <= right && x + k < w - 1; k++) {
					int c = i + k;
					if (map[c] != Terrain.WALL || !DungeonTileSheet.floorTile(map[c + w])) break;
					map[c] = Terrain.WALL_DECO;
				}
			}
		}

		//옆으로 바닥을 마주한 벽에도 가끔 수정맥이 지나감
		for (int i = w; i < l - w; i++) {
			int x = i % w;
			if (map[i] == Terrain.WALL && x > 0 && x < w - 1
					&& (DungeonTileSheet.floorTile(map[i - 1]) || DungeonTileSheet.floorTile(map[i + 1]))
					&& Random.Int(12) == 0) {
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

		//수정맥·수정 기둥 둘레에서 멀어질수록 옅어지게: 가까운 바닥엔 수정 새싹·돋아난 수정·수정 덤불
		//  (일반 방과 통로만, 특수 방은 건드리지 않음)
		int[] dist = new int[l];
		java.util.Arrays.fill(dist, 99);
		for (int i = 0; i < l; i++) {
			int t = map[i];
			if (t == Terrain.WALL_DECO || t == Terrain.REGION_DECO || t == Terrain.REGION_DECO_ALT) dist[i] = 0;
		}
		for (int pass = 1; pass <= 3; pass++) {
			for (int i = w + 1; i < l - w - 1; i++) {
				if (dist[i] != 99) continue;
				for (int n : new int[]{-w - 1, -w, -w + 1, -1, 1, w - 1, w, w + 1}) {
					if (dist[i + n] == pass - 1) { dist[i] = pass; break; }
				}
			}
		}
		for (Room room : rooms) {
			if (!(room instanceof StandardRoom) && !(room instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.connection.ConnectionRoom)) continue;
			if (room instanceof BlacksmithRoom) continue;
			for (int y = room.top + 1; y < room.bottom; y++) {
				for (int x = room.left + 1; x < room.right; x++) {
					int i = x + y * w;
					if (map[i] != Terrain.EMPTY || level.traps.get(i) != null) continue;
					switch (dist[i]) {
						case 1:
							if (Random.Int(10) < 3) map[i] = Terrain.GRASS;
							else if (Random.Int(10) < 2) map[i] = Terrain.EMPTY_DECO;
							break;
						case 2:
							if (Random.Int(10) < 2) map[i] = Terrain.GRASS;
							else if (Random.Int(12) == 0) map[i] = Terrain.HIGH_GRASS;
							break;
						case 3:
							if (Random.Int(10) == 0) map[i] = Terrain.GRASS;
							break;
					}
				}
			}
		}
	}
}
