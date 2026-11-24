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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.util.HashSet;

public class DungeonWallsTilemap extends DungeonTilemap {

	public static HashSet<Integer> skipCells = new HashSet<>();

	public DungeonWallsTilemap(){
		super(Dungeon.level.tilesTex());
		skipCells.clear();
		map( Dungeon.level.map, Dungeon.level.width() );
	}

	//모드: 큰 그림(세밀한 도트 영웅·정령·정령왕)이 선 칸이 바뀌면 그 위 두 칸을 다시 그림
	private final java.util.HashSet<Integer> shown = new java.util.HashSet<>();

	@Override
	public void update() {
		super.update();
		java.util.HashSet<Integer> now = RaisedTerrainTilemap.collectTall();
		if (!now.equals( shown )){
			java.util.HashSet<Integer> changed = new java.util.HashSet<>( shown );
			changed.addAll( now );
			for (Integer c : now) if (shown.contains( c )) changed.remove( c );
			shown.clear(); shown.addAll( now );
			for (int c : changed){
				if (c >= mapWidth && c < size) updateMapCell( c - mapWidth );
				if (c >= 2*mapWidth && c < size) updateMapCell( c - 2*mapWidth );
			}
		}
	}

	@Override
	protected int getTileVisual(int pos, int tile, boolean flat){

		if (flat) return -1;

		//모드: 큰 그림(영웅·정령·정령왕)이 한 칸보다 커서, 바로 위 칸에 그려지는 풀·문틀 윗부분(덮개)이
		// 머리를 가리던 문제. 큰 그림이 선 칸 바로 위의 (벽이 아닌) 덮개는 그리지 않음
		if (shown.contains( pos + mapWidth ) && !DungeonTileSheet.wallStitcheable(tile)){
			return -1;
		}
		//영웅 바로 위 칸의 풀은 숨기므로(RaisedTerrainTilemap), 그 풀이 한 칸 더 위로 뻗는 풀잎 끝도 함께 숨김
		if (RaisedTerrainTilemap.hidesGrassAbove( pos + mapWidth ) && !DungeonTileSheet.wallStitcheable(tile)){
			return -1;
		}

		if (DungeonTileSheet.wallStitcheable(tile)) {
			if (pos + mapWidth < size && !DungeonTileSheet.wallStitcheable(map[pos + mapWidth])){

				if (map[pos + mapWidth] == Terrain.DOOR){
					return DungeonTileSheet.DOOR_SIDEWAYS;
				} else if (map[pos + mapWidth] == Terrain.LOCKED_DOOR) {
					return DungeonTileSheet.DOOR_SIDEWAYS_LOCKED;
				} else if (map[pos + mapWidth] == Terrain.HERO_LKD_DR){
					return DungeonTileSheet.DOOR_SIDEWAYS_LOCKED;
				} else if (map[pos + mapWidth] == Terrain.CRYSTAL_DOOR){
					return DungeonTileSheet.DOOR_SIDEWAYS_CRYSTAL;
				} else if (map[pos + mapWidth] == Terrain.OPEN_DOOR){
					return DungeonTileSheet.NULL_TILE;
				}

			} else {
				return DungeonTileSheet.stitchInternalWallTile(
						tile,
						(pos+1) % mapWidth != 0 ?                           map[pos + 1] : -1,
						(pos+1) % mapWidth != 0 && pos + mapWidth < size ?  map[pos + 1 + mapWidth] : -1,
						pos + mapWidth < size ?                             map[pos + mapWidth] : -1,
						pos % mapWidth != 0 && pos + mapWidth < size ?      map[pos - 1 + mapWidth] : -1,
						pos % mapWidth != 0 ?                               map[pos - 1] : -1
				);
			}

		}

		if (skipCells.contains(pos)){
			return -1;
		}

		if (map[pos] == Terrain.LOCKED_EXIT || map[pos] == Terrain.UNLOCKED_EXIT){
			return DungeonTileSheet.EXIT_UNDERHANG;
		} else if (pos + mapWidth < size && DungeonTileSheet.wallStitcheable(map[pos+mapWidth])) {

			return DungeonTileSheet.stitchWallOverhangTile(
					tile,
					(pos+1) % mapWidth != 0 ?   map[pos + 1 + mapWidth] : -1,
												map[pos + mapWidth],
					pos % mapWidth != 0 ?       map[pos - 1 + mapWidth] : -1
			);

		} else if (Dungeon.level.insideMap(pos) && map[pos+mapWidth] == Terrain.DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG;
		} else if (Dungeon.level.insideMap(pos) && map[pos+mapWidth] == Terrain.LOCKED_DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG;
		} else if (Dungeon.level.insideMap(pos) && map[pos+mapWidth] == Terrain.HERO_LKD_DR ) {
			return DungeonTileSheet.DOOR_OVERHANG;
		} else if (Dungeon.level.insideMap(pos) && map[pos+mapWidth] == Terrain.OPEN_DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG_OPEN;
		} else if (Dungeon.level.insideMap(pos) && map[pos+mapWidth] == Terrain.CRYSTAL_DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG_CRYSTAL;
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.STATUE){
			return DungeonTileSheet.STATUE_OVERHANG;
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.STATUE_SP){
			return DungeonTileSheet.STATUE_SP_OVERHANG;
		} else if (pos + mapWidth < size && (map[pos+mapWidth] == Terrain.REGION_DECO || map[pos+mapWidth] == Terrain.REGION_DECO_ALT)
				&& com.shatteredpixel.shatteredpixeldungeon.levels.CrystalVeinLevel.isVein()){
			//모드: 수정 광맥의 수정 기둥 윗부분 (기둥과 같은 색)
			return com.shatteredpixel.shatteredpixeldungeon.levels.CrystalVeinLevel.pillarOverhang( pos + mapWidth );
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.REGION_DECO){
			return DungeonTileSheet.REGION_DECO_OVERHANG;
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.REGION_DECO_ALT){
			return DungeonTileSheet.REGION_DECO_ALT_OVERHANG;
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.MINE_CRYSTAL){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.MINE_CRYSTAL_OVERHANG_BLUE, pos + mapWidth);
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.MINE_BOULDER){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.MINE_BOULDER_OVERHANG, pos + mapWidth);
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.ALCHEMY){
			return DungeonTileSheet.ALCHEMY_POT_OVERHANG;
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.BARRICADE){
			return DungeonTileSheet.BARRICADE_OVERHANG;
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.HIGH_GRASS
				&& com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpire.isTent(pos + mapWidth)){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.MINE_BOULDER_OVERHANG, pos + mapWidth);
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.HIGH_GRASS){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.HIGH_GRASS_OVERHANG, pos + mapWidth);
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.FURROWED_GRASS){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.FURROWED_OVERHANG, pos + mapWidth);
		}

		return -1;
	}

	@Override
	public boolean overlapsPoint( float x, float y ) {
		return true;
	}

	@Override
	public boolean overlapsScreenPoint( int x, int y ) {
		return true;
	}
	
}
