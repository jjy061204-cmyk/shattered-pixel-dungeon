/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 고철 공방: 가운데 기계 잔해를 녹이는 용광로(불), 벽을 따라 쌓인 고철 더미,
 * 용광로 둘레를 막은 고철 바리케이드(공격 두 번에 무너짐)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ScrapWorkshopRoom extends StandardRoom {

	//고철 바리케이드를 세울 칸
	public ArrayList<Integer> barricades = new ArrayList<>();

	@Override
	public int minWidth() {
		return Math.max( super.minWidth(), 8 );
	}

	@Override
	public int minHeight() {
		return Math.max( super.minHeight(), 8 );
	}

	@Override
	public float[] sizeCatProbs() {
		return new float[]{2, 1, 0};
	}

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		Painter.fill( level, this, 2, Terrain.EMPTY_SP );
		for (Room.Door door : connected.values()){
			door.set( Room.Door.Type.REGULAR );
		}

		//용광로: 가운데 2칸
		Point c = center();
		boolean horizontal = width() >= height();
		Point f2 = horizontal ? new Point( c.x + 1, c.y ) : new Point( c.x, c.y + 1 );
		Painter.set( level, c, Terrain.STATUE_SP );
		Painter.set( level, f2, Terrain.STATUE_SP );

		//벽을 따라 고철 더미 (문 앞과 모서리 통로는 비움)
		int piles = 3 + Random.Int( 3 );
		for (int tries = 0; tries < 60 && piles > 0; tries++){
			Point p = random( 1 );
			boolean wallSide = p.x == left + 1 || p.x == right - 1 || p.y == top + 1 || p.y == bottom - 1;
			if (!wallSide) continue;
			int cell = level.pointToCell( p );
			if (level.map[cell] != Terrain.EMPTY) continue;
			if (nearDoor( p )) continue;
			Painter.set( level, cell, Random.Int( 3 ) == 0 ? Terrain.REGION_DECO_ALT : Terrain.REGION_DECO );
			piles--;
		}

		//용광로 둘레 바닥에 고철 바리케이드 1~2개
		barricades.clear();
		int want = 1 + Random.Int( 2 );
		for (int tries = 0; tries < 30 && barricades.size() < want; tries++){
			Point p = new Point( c.x + Random.IntRange( -2, horizontal ? 3 : 2 ), c.y + Random.IntRange( -2, horizontal ? 2 : 3 ) );
			if (!inside( p )) continue;
			int cell = level.pointToCell( p );
			if (level.map[cell] != Terrain.EMPTY_SP || barricades.contains( cell )) continue;
			if (Math.abs( p.x - c.x ) <= 1 && Math.abs( p.y - c.y ) <= 1) continue;
			if (Math.abs( p.x - f2.x ) <= 1 && Math.abs( p.y - f2.y ) <= 1) continue;
			barricades.add( cell );
		}
	}

	private boolean nearDoor( Point p ){
		for (Room.Door door : connected.values()){
			if (Math.abs( door.x - p.x ) <= 1 && Math.abs( door.y - p.y ) <= 1) return true;
		}
		return false;
	}

	@Override
	public boolean canPlaceGrass( Point p ) {
		return false;
	}

	@Override
	public boolean canPlaceWater( Point p ) {
		return false;
	}

	@Override
	public boolean canPlaceCharacter( Point p, Level l ) {
		return super.canPlaceCharacter( p, l ) && !barricades.contains( l.pointToCell( p ) );
	}

	@Override
	public boolean canMerge( Level l, Room other, Point p, int mergeTerrain ) {
		return false;
	}
}
