/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.util.HashSet;

public class RaisedTerrainTilemap extends DungeonTilemap {
	
	public RaisedTerrainTilemap() {
		super(Assets.Environment.RAISED_TERRAIN);
		skipCells.clear();
		map( Dungeon.level.map, Dungeon.level.width() );
	}

	public static HashSet<Integer> skipCells = new HashSet<>();

	//모드: 세밀한 도트 영웅은 한 칸보다 커서 머리가 바로 위 칸에 걸침. 그 칸의 키 큰 풀이
	// 영웅 뒤로 기둥처럼 솟아 보이던 문제 → 영웅 바로 위 칸의 풀잎(과 그 위로 뻗는 끝)을 그리지 않음
	public static boolean hidesGrassAbove( int pos ){
		if (Dungeon.hero == null || Dungeon.level == null || Dungeon.hero.heroClass.hdSheet() == null) return false;
		if (pos < 0 || pos >= Dungeon.level.length() || pos + Dungeon.level.width() != Dungeon.hero.pos) return false;
		int t = Dungeon.level.map[pos];
		return t == Terrain.HIGH_GRASS || t == Terrain.FURROWED_GRASS;
	}

	private int heroPos = -1;

	@Override
	public void update() {
		super.update();
		if (Dungeon.hero != null && Dungeon.hero.pos != heroPos){
			int old = heroPos;
			heroPos = Dungeon.hero.pos;
			if (old >= mapWidth && old < size) updateMapCell( old - mapWidth );
			if (heroPos >= mapWidth && heroPos < size) updateMapCell( heroPos - mapWidth );
		}
	}

	@Override
	protected int getTileVisual(int pos, int tile, boolean flat) {
		
		if (flat) return -1;

		if (skipCells.contains(pos) || hidesGrassAbove(pos)){
			return -1;
		}

		//모드: 흡혈귀의 성·지하 묘지는 장미 덤불·거미줄을 자체 타일에 그려 두었으므로 동굴 풀잎을 덧그리지 않음
		if (com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle.isCastleLevel( Dungeon.level )){
			return -1;
		}

		int region = (Dungeon.depth-1)/5;
		int regionOffset = region*4;

		if (tile == Terrain.HIGH_GRASS){
			if (DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.RAISED_HIGH_GRASS, pos) == DungeonTileSheet.RAISED_HIGH_GRASS_ALT){
				return regionOffset + 2;
			} else {
				return regionOffset;
			}
		} else if (tile == Terrain.FURROWED_GRASS){
			if (DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.RAISED_FURROWED_GRASS, pos) == DungeonTileSheet.RAISED_FURROWED_ALT){
				return regionOffset + 1 + 2;
			} else {
				return regionOffset + 1;
			}
		}
		
		return -1;
	}
}
