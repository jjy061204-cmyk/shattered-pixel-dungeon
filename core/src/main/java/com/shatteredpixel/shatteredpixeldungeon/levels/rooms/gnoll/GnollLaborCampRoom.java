/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 13층의 노역장: 놀에게 붙잡힌 트롤 대장장이가 무기를 벼리는 대장간
 *  - 원작 대장간 그대로에 나무 울타리 기둥과 고철 더미를 더함 (문 앞은 비움)
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
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom;
import com.watabou.utils.Point;

public class GnollLaborCampRoom extends BlacksmithRoom {

	@Override
	public void paint( Level level ) {
		super.paint( level );

		//아래쪽 두 모서리에 울타리 기둥 (문이 바로 옆이면 비움)
		Point[] posts = {
				new Point( left + 1, bottom - 1 ),
				new Point( right - 1, bottom - 1 )
		};
		for (Point p : posts){
			int cell = level.pointToCell( p );
			if (level.map[cell] == Terrain.EMPTY && !nearDoor( p ) && level.heaps.get( cell ) == null){
				Painter.set( level, cell, Terrain.BARRICADE );
			}
		}
	}

	private boolean nearDoor( Point p ){
		for (Room.Door door : connected.values()){
			if (Math.abs( door.x - p.x ) <= 1 && Math.abs( door.y - p.y ) <= 1) return true;
		}
		return false;
	}
}
