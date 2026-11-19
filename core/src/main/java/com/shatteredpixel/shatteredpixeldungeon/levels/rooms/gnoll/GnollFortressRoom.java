/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 14층의 요새 외곽 (출구 방)
 *  - 둘레 두 칸은 마당, 그 안을 나무 방책 성벽이 두르고, 마주 보는 성문 두 곳을 문지기 놀이 지킴
 *  - 성벽 안 가운데에 아래층 계단, 네 모서리에 망루(서 있는 쪽은 누구든 시야 +2, 투척병은 사거리 +1)
 *  - 성문은 문지기를 쓰러뜨리면 열리고, 방책을 불태워 길을 낼 수도 있음
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.exit.ExitRoom;
import com.watabou.utils.Point;

import java.util.ArrayList;

public class GnollFortressRoom extends ExitRoom {

	public int[] gates = new int[]{ -1, -1 };
	public int[] posts = new int[]{ -1, -1 };
	public ArrayList<Integer> towers = new ArrayList<>();
	private int exitCell = -1;

	@Override
	public int minWidth() {
		return Math.max( super.minWidth(), 15 );
	}

	@Override
	public int minHeight() {
		return Math.max( super.minHeight(), 13 );
	}

	@Override
	public float[] sizeCatProbs() {
		return new float[]{0, 0, 1};
	}

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		for (Room.Door door : connected.values()){
			door.set( Room.Door.Type.REGULAR );
		}

		//성벽 (방 안쪽 세 번째 줄)
		Painter.fill( level, this, 3, Terrain.BARRICADE );
		Painter.fill( level, this, 4, Terrain.EMPTY );

		int il = left + 4, ir = right - 4, it = top + 4, ib = bottom - 4;
		Point c = new Point( (il + ir) / 2, (it + ib) / 2 );

		//성문 두 곳: 긴 쪽 양끝
		if (width() >= height()){
			gates[0] = level.pointToCell( new Point( left + 3, c.y ) );
			gates[1] = level.pointToCell( new Point( right - 3, c.y ) );
			posts[0] = level.pointToCell( new Point( left + 2, c.y ) );
			posts[1] = level.pointToCell( new Point( right - 2, c.y ) );
		} else {
			gates[0] = level.pointToCell( new Point( c.x, top + 3 ) );
			gates[1] = level.pointToCell( new Point( c.x, bottom - 3 ) );
			posts[0] = level.pointToCell( new Point( c.x, top + 2 ) );
			posts[1] = level.pointToCell( new Point( c.x, bottom - 2 ) );
		}
		Painter.set( level, gates[0], Terrain.DOOR );
		Painter.set( level, gates[1], Terrain.DOOR );

		//망루: 성벽 안 네 모서리
		towers.clear();
		int[][] corners = { {il, it}, {ir, it}, {il, ib}, {ir, ib} };
		for (int[] p : corners){
			int cell = level.pointToCell( new Point( p[0], p[1] ) );
			Painter.set( level, cell, Terrain.EMPTY_SP );
			towers.add( cell );
		}

		//아래층 계단
		exitCell = level.pointToCell( c );
		Painter.set( level, exitCell, Terrain.EXIT );
		level.transitions.add( new LevelTransition( level, exitCell, LevelTransition.Type.REGULAR_EXIT ) );
	}

	private boolean inner( Point p, int m ){
		return p.x >= left + m && p.x <= right - m && p.y >= top + m && p.y <= bottom - m;
	}

	@Override
	public boolean canPlaceGrass( Point p ) {
		return !inner( p, 3 );
	}

	@Override
	public boolean canPlaceWater( Point p ) {
		return !inner( p, 2 );
	}

	@Override
	public boolean canPlaceTrap( Point p ) {
		//마당 바깥 줄과 성벽 안쪽에만 (성문 앞 길목은 비움)
		return !inner( p, 2 ) || inner( p, 4 );
	}

	@Override
	public boolean canPlaceCharacter( Point p, Level l ) {
		int cell = l.pointToCell( p );
		if (cell == gates[0] || cell == gates[1] || cell == posts[0] || cell == posts[1]) return false;
		return super.canPlaceCharacter( p, l ) && l.map[cell] != Terrain.BARRICADE;
	}

	@Override
	public boolean canPlaceItem( Point p, Level l ) {
		int cell = l.pointToCell( p );
		return super.canPlaceItem( p, l ) && l.map[cell] != Terrain.BARRICADE && cell != exitCell;
	}
}
