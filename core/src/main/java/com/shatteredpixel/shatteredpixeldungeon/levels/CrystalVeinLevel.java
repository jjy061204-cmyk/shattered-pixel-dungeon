/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 갈림길 테마: 수정 광맥 (11~14층) — 드워프들이 캐다 깨운 수정 생명체가 뒤덮은 광맥
 *  - 서늘한 잿빛 보라 암반, 벽을 뚫고 자란 보라·푸른 수정, 드워프 갱도의 나무 지지대(불에 탐)
 *  - 긴 풀 대신 결정 덤불, 풀 대신 빛 이끼, 바위 무더기 대신 수정 기둥, 맑은 지하수
 *  - 방·몹·아이템 수는 원작 광산과 같은 예산. 트롤 대장장이 퀘스트는 그대로
 *  - 아직 타일·지형만: 몹(수정 위습·결정화된 놀·프리즘 술사…), 마법 탄 반사, 수정 DM-300은 차례로 채울 예정
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class CrystalVeinLevel extends CavesLevel {

	{
		color1 = 0x2a2440;
		color2 = 0x8a70d8;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CRYSTAL_VEIN;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CRYSTAL;
	}

	//지하수는 타일과 같은 24x24 밀도로 찍음
	@Override
	public float waterTexScale() {
		return 1.5f;
	}

	@Override
	public String entryText() {
		return Dungeon.depth == 11 ? Messages.get( this, "entry" ) : null;
	}

	@Override
	public String tileName( int tile ) {
		String key = tileKey( tile );
		return key != null ? Messages.get( CrystalVeinLevel.class, key + "_name" ) : super.tileName( tile );
	}

	@Override
	public String tileDesc( int tile ) {
		String key = tileKey( tile );
		return key != null ? Messages.get( CrystalVeinLevel.class, key + "_desc" ) : super.tileDesc( tile );
	}

	private static String tileKey( int tile ){
		switch (tile){
			case Terrain.WALL_DECO:       return "wall_deco";
			case Terrain.EMPTY_DECO:      return "floor_deco";
			case Terrain.EMPTY_SP:        return "floor_sp";
			case Terrain.GRASS:           return "grass";
			case Terrain.HIGH_GRASS:      return "high_grass";
			case Terrain.FURROWED_GRASS:  return "furrowed_grass";
			case Terrain.BOOKSHELF:       return "bookshelf";
			case Terrain.STATUE:          return "statue";
			case Terrain.STATUE_SP:       return "statue_sp";
			case Terrain.REGION_DECO:     return "region_deco";
			case Terrain.REGION_DECO_ALT: return "region_deco_alt";
			case Terrain.WATER:           return "water";
			case Terrain.ENTRANCE:
			case Terrain.ENTRANCE_SP:     return "entrance";
			case Terrain.EXIT:            return "exit";
			default:                      return null;
		}
	}
}
