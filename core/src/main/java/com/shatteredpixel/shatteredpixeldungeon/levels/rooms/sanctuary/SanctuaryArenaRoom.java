/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 2층 수호자의 투기장
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sanctuary;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;

import java.util.ArrayList;

//문이 하나뿐인 23x23 수호자의 대전당.
//문에서 모자이크 길이 가운데 룬 원을 지나 단상(왕좌)까지 이어지고, 양옆으로 룬샘 수로와 돌기둥이 늘어섬.
//네 곳의 룬 거울 수호상은 거상의 광선을 되쏘는 장치. 단상 뒤 벽 너머에는 엑스칼리버가 꽂힌 제단이 있는 금고가 숨어 있음
public class SanctuaryArenaRoom extends StandardRoom {

	public static final int SIZE = 23;
	private static final int HALF = SIZE / 2;

	//칠해진 뒤 레벨이 읽어 가는 칸들
	public int throne = -1;
	public int center = -1;
	public int door = -1;
	public int vaultDoor = -1;
	public int altar = -1;
	public final ArrayList<Integer> mirrors = new ArrayList<>();

	@Override
	public float[] sizeCatProbs() {
		return new float[]{0, 0, 1};
	}

	@Override public int minWidth()  { return SIZE; }
	@Override public int maxWidth()  { return SIZE; }
	@Override public int minHeight() { return SIZE; }
	@Override public int maxHeight() { return SIZE; }

	@Override
	public int maxConnections( int direction ) {
		if (direction == ALL) return 1;
		return super.maxConnections( direction );
	}

	//문은 각 변의 가운데 근처(±1)에만
	@Override
	public boolean canConnect( Point p ) {
		if (!super.canConnect( p )) return false;
		if (p.x == left || p.x == right) return Math.abs( p.y - (top + HALF) ) <= 1;
		return Math.abs( p.x - (left + HALF) ) <= 1;
	}

	//입구 방과 바로 붙지는 않게
	@Override
	public boolean canConnect( Room r ) {
		if (r.isEntrance()) return false;
		return super.canConnect( r );
	}

	@Override
	public boolean canMerge( Level l, Room other, Point p, int mergeTerrain ) {
		return false;
	}

	@Override
	public int mobSpawnWeight() {
		return 0;
	}

	@Override public boolean canPlaceWater( Point p ) { return false; }
	@Override public boolean canPlaceGrass( Point p ) { return false; }
	@Override public boolean canPlaceTrap( Point p )  { return false; }
	@Override public boolean canPlaceItem( Point p, Level l ) { return false; }
	@Override public boolean canPlaceCharacter( Point p, Level l ) { return false; }

	//방 안쪽 (벽 제외)인지
	public boolean interior( Point p ){
		return p.x > left && p.x < right && p.y > top && p.y < bottom;
	}

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );

		Door d = null;
		for (Door dd : connected.values()){ d = dd; }
		int side = 0; //0: 아래, 1: 위, 2: 오른쪽, 3: 왼쪽
		if (d != null){
			if (d.y == bottom) side = 0;
			else if (d.y == top) side = 1;
			else if (d.x == right) side = 2;
			else side = 3;
			d.set( Door.Type.UNLOCKED );
		}

		int cx = left + HALF, cy = top + HALF;
		mirrors.clear();

		//문이 아래쪽에 있다고 보고 그린 뒤 돌려서 찍음
		for (int oy = -HALF + 1; oy <= HALF - 1; oy++){
			for (int ox = -HALF + 1; ox <= HALF - 1; ox++){
				int t = tileAt( ox, oy );
				Point p = rotate( ox, oy, side );
				Painter.set( level, cx + p.x, cy + p.y, t );
				if (t == Terrain.STATUE_SP){
					mirrors.add( level.pointToCell( new Point( cx + p.x, cy + p.y ) ) );
				}
			}
		}

		//문에서 안쪽으로 길 뚫기
		if (d != null){
			Point step = new Point( d.x == left ? 1 : d.x == right ? -1 : 0, d.y == top ? 1 : d.y == bottom ? -1 : 0 );
			Point c = new Point( d.x + step.x, d.y + step.y );
			for (int i = 0; i < 3 && interior( c ); i++){
				if (level.map[level.pointToCell( c )] == Terrain.WALL) Painter.set( level, c, Terrain.EMPTY_SP );
				c.offset( step );
			}
			door = level.pointToCell( d );
		}

		throne = cellAt( level, cx, cy, 0, -4, side );
		center = cellAt( level, cx, cy, 0, 2, side );
		vaultDoor = cellAt( level, cx, cy, 0, -7, side );
		altar = cellAt( level, cx, cy, 0, -9, side );

		SanctuaryLevel.addRune( level, center );
	}

	private static int cellAt( Level level, int cx, int cy, int x, int y, int side ){
		Point p = rotate( x, y, side );
		return level.pointToCell( new Point( cx + p.x, cy + p.y ) );
	}

	//문이 아래(+y)에 있을 때의 모양
	private static int tileAt( int dx, int dy ){
		int ax = Math.abs( dx );

		//---- 단상 뒤 금고 (벽 한 줄로 막혀 있음) ----
		if (dy <= -8){
			if (ax > 3) return Terrain.WALL;
			if (dx == 0 && dy == -9) return Terrain.PEDESTAL;          //엑스칼리버 제단
			if (ax == 3 && dy == -10) return Terrain.REGION_DECO_ALT;  //화로
			if (ax == 3 && dy == -8) return Terrain.REGION_DECO;       //오벨리스크
			return Terrain.EMPTY_SP;
		}
		if (dy == -7) return Terrain.WALL;

		//---- 대전당 (모서리를 깎은 팔각) ----
		int d = ax + Math.abs( dy - 2 );
		if (d > 15) return Terrain.WALL;

		//단상
		if (dy <= -4 && ax <= 4){
			if (ax == 4 && dy == -6) return Terrain.REGION_DECO;       //오벨리스크
			if (ax == 2 && dy == -6) return Terrain.REGION_DECO_ALT;   //화로
			return Terrain.EMPTY_SP;
		}
		//가운데 길 (문 → 룬 원 → 단상)
		if (ax <= 1) return Terrain.EMPTY_SP;
		//룬 거울 수호상 넷
		if (ax == 6 && (dy == -2 || dy == 6)) return Terrain.STATUE_SP;
		//돌기둥 열
		if (ax == 9 && (dy == -1 || dy == 2 || dy == 5)) return Terrain.WALL;
		if (ax == 4 && dy == 9) return Terrain.WALL;
		//길 양옆 룬샘 수로
		if (ax == 3 && dy >= 4 && dy <= 8) return Terrain.WATER;
		//길을 따라 선 화로
		if (ax == 3 && (dy == 3 || dy == 9)) return Terrain.REGION_DECO_ALT;
		//가장자리 부서진 바닥
		if (d == 15) return Terrain.EMPTY_DECO;
		return Terrain.EMPTY;
	}

	private static Point rotate( int x, int y, int side ){
		switch (side){
			case 0: default: return new Point( x, y );
			case 1: return new Point( x, -y );
			case 2: return new Point( y, x );
			case 3: return new Point( -y, x );
		}
	}
}
