/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 1층 입구 (고대 게이트가 서 있는 방)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sanctuary;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;

//가운데 룬 원 위에 20층으로 돌아가는 게이트가 서 있음
public class SanctuaryGateRoom extends StandardRoom {

	@Override
	public float[] sizeCatProbs() {
		return new float[]{1, 0, 0};
	}

	@Override
	public int minWidth() {
		return Math.max( super.minWidth(), 7 );
	}

	@Override
	public int minHeight() {
		return Math.max( super.minHeight(), 7 );
	}

	@Override
	public boolean isEntrance() {
		return true;
	}

	@Override
	public boolean canMerge( Level l, Room other, Point p, int mergeTerrain ) {
		return false;
	}

	@Override
	public boolean canPlaceTrap( Point p ) {
		return false;
	}

	@Override
	public boolean canPlaceWater( Point p ) {
		Point c = center();
		return Math.abs( p.x - c.x ) > 1 || Math.abs( p.y - c.y ) > 1;
	}

	@Override
	public boolean canPlaceGrass( Point p ) {
		return canPlaceWater( p );
	}

	@Override
	public boolean canPlaceCharacter( Point p, Level l ) {
		return super.canPlaceCharacter( p, l ) && l.pointToCell( p ) != l.entrance();
	}

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}

		Point c = center();
		Painter.fill( level, c.x - 1, c.y - 1, 3, 3, Terrain.EMPTY_SP );

		//안쪽 네 모서리에 수호상 (문 바로 앞은 비워 둠)
		Point[] corners = { new Point( left + 1, top + 1 ), new Point( right - 1, top + 1 ),
				new Point( left + 1, bottom - 1 ), new Point( right - 1, bottom - 1 ) };
		for (Point p : corners){
			if (Math.abs( p.x - c.x ) < 2 || Math.abs( p.y - c.y ) < 2) continue;
			boolean nearDoor = false;
			for (Door d : connected.values()){
				if (Math.abs( d.x - p.x ) <= 1 && Math.abs( d.y - p.y ) <= 1) nearDoor = true;
			}
			if (!nearDoor) Painter.set( level, p, Terrain.STATUE_SP );
		}

		int gate = level.pointToCell( c );
		level.transitions.add( new LevelTransition( level, gate, LevelTransition.Type.REGULAR_ENTRANCE,
				SanctuaryQuest.DEPTH, 0, LevelTransition.Type.BRANCH_EXIT ) );

		SanctuaryLevel.addRune( level, gate );
		SanctuaryLevel.addGate( level, gate );
	}
}
