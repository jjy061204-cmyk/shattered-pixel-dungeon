/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 약탈 캠프 (11x11 이상): 나무 방책으로 두르고, 안에 천막 4~6칸·모닥불·포로 우리·전리품 더미·경보 북
 *  - 방책의 출입구는 방의 문 바로 안쪽
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollCampRoom extends StandardRoom {

	public int drum = -1;
	public int cage = -1;
	public java.util.ArrayList<Integer> tents = new java.util.ArrayList<>();
	private int fire = -1;

	@Override
	public int minWidth() {
		return Math.max( super.minWidth(), 11 );
	}

	@Override
	public int minHeight() {
		return Math.max( super.minHeight(), 11 );
	}

	@Override
	public float[] sizeCatProbs() {
		return new float[]{0, 1, 0};
	}

	@Override
	public void paint( Level level ) {
		tents.clear();
		Painter.fill( level, this, Terrain.WALL );
		//방 안쪽 첫 줄은 방책, 그 안은 캠프
		Painter.fill( level, this, 1, Terrain.BARRICADE );
		Painter.fill( level, this, 2, Terrain.EMPTY );

		for (Room.Door door : connected.values()){
			door.set( Room.Door.Type.REGULAR );
			Painter.drawInside( level, this, door, 1, Terrain.EMPTY );
		}

		Point c = center();
		fire = level.pointToCell( c );
		Painter.set( level, fire, Terrain.STATUE_SP );
		//모닥불 둘레는 짓밟힌 짚
		for (int dx = -1; dx <= 1; dx++){
			for (int dy = -1; dy <= 1; dy++){
				if (dx != 0 || dy != 0) Painter.set( level, c.x + dx, c.y + dy, Terrain.EMPTY_DECO );
			}
		}

		//안쪽 칸 (방책 안, 모닥불 둘레 제외)
		int il = left + 2, ir = right - 2, it = top + 2, ib = bottom - 2;

		//천막: 안쪽 모서리 쪽에 2칸짜리 천막 2~3개 (출입구 앞은 피함)
		ArrayList<Point> corners = new ArrayList<>();
		corners.add( new Point( il, it ) );
		corners.add( new Point( ir, it ) );
		corners.add( new Point( il, ib ) );
		corners.add( new Point( ir, ib ) );
		Random.shuffle( corners );
		int tents = 2 + Random.Int( 2 );
		for (Point p : corners){
			if (tents <= 0) break;
			int dx = p.x == il ? 1 : -1;
			int dy = p.y == it ? 1 : -1;
			Point second = Random.Int( 2 ) == 0 ? new Point( p.x + dx, p.y ) : new Point( p.x, p.y + dy );
			if (nearGap( level, p ) || nearGap( level, second )) continue;
			Painter.set( level, p, Terrain.HIGH_GRASS );
			Painter.set( level, second, Terrain.HIGH_GRASS );
			this.tents.add( level.pointToCell( p ) );
			this.tents.add( level.pointToCell( second ) );
			tents--;
		}

		//포로 우리·경보 북·전리품 더미는 남은 안쪽 칸 중에서
		cage = pickCell( level, il, ir, it, ib, true );
		drum = pickCell( level, il, ir, it, ib, false );

		Item prize = level.findPrizeItem();
		if (prize != null){
			int cell = -1;
			for (int tries = 0; tries < 20; tries++){
				int cand = level.pointToCell( new Point( c.x + Random.IntRange( -1, 1 ), c.y + Random.IntRange( -1, 1 ) ) );
				if (cand != fire && cand != drum && cand != cage && level.map[cand] == Terrain.EMPTY_DECO && level.heaps.get( cand ) == null){
					cell = cand;
					break;
				}
			}
			if (cell != -1) level.drop( prize, cell );
			else level.addItemToSpawn( prize );
		}
	}

	//방책 출입구(문 바로 안쪽 칸) 둘레인지
	private boolean nearGap( Level level, Point p ){
		for (Room.Door door : connected.values()){
			Point gap = new Point( door );
			if (door.x == left) gap.x++;
			else if (door.x == right) gap.x--;
			else if (door.y == top) gap.y++;
			else if (door.y == bottom) gap.y--;
			if (Math.abs( gap.x - p.x ) <= 1 && Math.abs( gap.y - p.y ) <= 1) return true;
		}
		return false;
	}

	//벽 쪽(edge=true) 또는 아무 데나, 비어 있고 출입구 앞이 아닌 칸
	private int pickCell( Level level, int il, int ir, int it, int ib, boolean edge ){
		for (int tries = 0; tries < 50; tries++){
			Point p = new Point( Random.IntRange( il, ir ), Random.IntRange( it, ib ) );
			if (edge && p.x != il && p.x != ir && p.y != it && p.y != ib) continue;
			int cell = level.pointToCell( p );
			if (level.map[cell] != Terrain.EMPTY || cell == drum || cell == cage) continue;
			if (Math.abs( p.x - level.cellToPoint( fire ).x ) <= 1 && Math.abs( p.y - level.cellToPoint( fire ).y ) <= 1) continue;
			if (nearGap( level, p )) continue;
			return cell;
		}
		return -1;
	}

	private boolean inside( Point p, int m ){
		return p.x >= left + m && p.x <= right - m && p.y >= top + m && p.y <= bottom - m;
	}

	@Override
	public boolean canPlaceWater( Point p ) {
		return !inside( p, 1 );
	}

	@Override
	public boolean canPlaceGrass( Point p ) {
		return !inside( p, 1 );
	}

	@Override
	public boolean canPlaceTrap( Point p ) {
		return false;
	}

	@Override
	public boolean canPlaceCharacter( Point p, Level l ) {
		int cell = l.pointToCell( p );
		return inside( p, 2 ) && cell != drum && cell != cage && cell != fire
				&& l.map[cell] != Terrain.BARRICADE;
	}

	@Override
	public boolean canPlaceItem( Point p, Level l ) {
		int cell = l.pointToCell( p );
		return inside( p, 2 ) && cell != drum && cell != cage && cell != fire;
	}

	@Override
	public boolean canMerge( Level l, Room other, Point p, int mergeTerrain ) {
		return false;
	}
}
