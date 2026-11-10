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

//문이 하나뿐인 15x15 둥근 투기장. 문 맞은편 끝에 왕좌(보스 자리)가 있음.
//절차 생성 맵의 한 방에 가지처럼 붙음 (출구 방 대신)
public class SanctuaryArenaRoom extends StandardRoom {

	public static final int SIZE = 15;

	//칠해진 뒤 레벨이 읽어 가는 칸들
	public int throne = -1;
	public int center = -1;
	public int door = -1;
	public final ArrayList<Integer> summon = new ArrayList<>();

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

	//문은 각 변의 가운데 근처(±2)에만
	@Override
	public boolean canConnect( Point p ) {
		if (!super.canConnect( p )) return false;
		int mid = SIZE / 2;
		if (p.x == left || p.x == right) return Math.abs( p.y - (top + mid) ) <= 2;
		return Math.abs( p.x - (left + mid) ) <= 2;
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

		int cx = left + SIZE / 2, cy = top + SIZE / 2;
		summon.clear();

		//문이 아래쪽에 있다고 보고 그린 뒤 돌려서 찍음
		for (int oy = -6; oy <= 6; oy++){
			for (int ox = -6; ox <= 6; ox++){
				int t = tileAt( ox, oy );
				Point p = rotate( ox, oy, side );
				Painter.set( level, cx + p.x, cy + p.y, t );
			}
		}

		//문에서 안쪽으로 길 뚫기
		if (d != null){
			Point step = new Point( d.x == left ? 1 : d.x == right ? -1 : 0, d.y == top ? 1 : d.y == bottom ? -1 : 0 );
			Point c = new Point( d.x + step.x, d.y + step.y );
			for (int i = 0; i < 3 && interior( c ); i++){
				if (level.map[level.pointToCell( c )] == Terrain.WALL) Painter.set( level, c, Terrain.EMPTY );
				c.offset( step );
			}
			door = level.pointToCell( d );
		}

		Point tp = rotate( 0, -5, side );
		throne = level.pointToCell( new Point( cx + tp.x, cy + tp.y ) );
		center = level.pointToCell( new Point( cx, cy ) );
		int[][] sp = { {-4, -1}, {4, -1}, {-4, 3}, {4, 3} };
		for (int[] s : sp){
			Point q = rotate( s[0], s[1], side );
			summon.add( level.pointToCell( new Point( cx + q.x, cy + q.y ) ) );
		}

		SanctuaryLevel.addRune( level, center );
	}

	//문이 아래(+y)에 있을 때의 모양
	private static int tileAt( int dx, int dy ){
		int ax = Math.abs( dx ), ay = Math.abs( dy );
		if (ax + ay > 9) return Terrain.WALL;
		//왕좌와 그 둘레
		if (dx == 0 && dy == -5) return Terrain.EMPTY_SP;
		if (ay <= 1 && ax <= 1) return Terrain.EMPTY_SP;               //가운데 룬 원
		if (ax == 4 && ay == 4) return Terrain.STATUE_SP;              //네 모서리 수호상
		if ((ax == 4 && dy == -1) || (ax == 4 && dy == 3)) return Terrain.PEDESTAL; //소환 받침대
		if (dy == -6 && ax == 2) return Terrain.REGION_DECO;           //왕좌 양옆 뒤 오벨리스크
		double r = Math.sqrt( dx * dx + dy * dy );
		if (r >= 3.5 && r < 4.5) return Terrain.EMPTY_SP;              //고리 모양 길
		if (dy == -5 && ax <= 2) return Terrain.EMPTY_SP;
		if (ax + ay == 9) return Terrain.EMPTY_DECO;                   //가장자리 부서진 바닥
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
