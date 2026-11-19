/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 변방 초소: 5x5 둥근 나무 방책 안에 경보 북 하나. 방책에는 마주 보는 출입구 두 곳
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

public class GnollOutpostRoom extends StandardRoom {

	//경보 북을 둘 칸 (층이 몹을 만들 때 읽음)
	public int drum = -1;
	private Point c = null;

	@Override
	public int minWidth() {
		return Math.max( super.minWidth(), 9 );
	}

	@Override
	public int minHeight() {
		return Math.max( super.minHeight(), 9 );
	}

	@Override
	public float[] sizeCatProbs() {
		return new float[]{1, 0, 0};
	}

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		for (Room.Door door : connected.values()){
			door.set( Room.Door.Type.REGULAR );
		}

		c = center();
		for (int dx = -2; dx <= 2; dx++){
			for (int dy = -2; dy <= 2; dy++){
				boolean edge = Math.abs( dx ) == 2 || Math.abs( dy ) == 2;
				boolean corner = Math.abs( dx ) == 2 && Math.abs( dy ) == 2;
				if (edge && !corner){
					Painter.set( level, c.x + dx, c.y + dy, Terrain.BARRICADE );
				} else if (!edge){
					Painter.set( level, c.x + dx, c.y + dy, Terrain.EMPTY_SP );
				}
			}
		}

		//마주 보는 출입구 두 곳
		if (Random.Int( 2 ) == 0){
			Painter.set( level, c.x - 2, c.y, Terrain.EMPTY );
			Painter.set( level, c.x + 2, c.y, Terrain.EMPTY );
		} else {
			Painter.set( level, c.x, c.y - 2, Terrain.EMPTY );
			Painter.set( level, c.x, c.y + 2, Terrain.EMPTY );
		}

		drum = level.pointToCell( c );
	}

	private boolean inRing( Point p ){
		return c != null && Math.abs( p.x - c.x ) <= 2 && Math.abs( p.y - c.y ) <= 2;
	}

	@Override
	public boolean canPlaceWater( Point p ) {
		return !inRing( p );
	}

	@Override
	public boolean canPlaceGrass( Point p ) {
		return !inRing( p );
	}

	@Override
	public boolean canPlaceTrap( Point p ) {
		return !inRing( p );
	}

	@Override
	public boolean canPlaceCharacter( Point p, Level l ) {
		return super.canPlaceCharacter( p, l ) && l.pointToCell( p ) != drum;
	}

	@Override
	public boolean canPlaceItem( Point p, Level l ) {
		return super.canPlaceItem( p, l ) && l.pointToCell( p ) != drum;
	}

	@Override
	public boolean canMerge( Level l, Room other, Point p, int mergeTerrain ) {
		return false;
	}
}
