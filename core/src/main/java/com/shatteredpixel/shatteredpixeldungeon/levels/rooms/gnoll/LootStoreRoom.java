/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 14층의 전리품 창고 (원작의 잠긴 무기고 하나를 대신함)
 *  - 놀이 약탈한 무기·갑옷·폭탄·투척 무기가 2~3개. 원작 무기고와 같은 수
 *  - 열쇠는 바닥에 생기지 않고 요새의 문지기가 가지고 있음 (층이 처리)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class LootStoreRoom extends SpecialRoom {

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );

		Door entrance = entrance();

		//구석에 고철·약탈품 더미 (문에서 먼 쪽)
		Point[] corners = {
				new Point( left + 1, top + 1 ), new Point( right - 1, top + 1 ),
				new Point( left + 1, bottom - 1 ), new Point( right - 1, bottom - 1 )
		};
		for (Point p : corners){
			if (Math.abs( p.x - entrance.x ) + Math.abs( p.y - entrance.y ) > 3 && Random.Int( 2 ) == 0){
				Painter.set( level, p, Terrain.REGION_DECO );
			}
		}

		int n = Random.IntRange( 2, 3 );
		prizeCats = new float[]{1, 1, 1, 1};
		for (int i = 0; i < n; i++){
			int pos;
			do {
				pos = level.pointToCell( random() );
			} while (level.map[pos] != Terrain.EMPTY_SP || level.heaps.get( pos ) != null);
			level.drop( prize(), pos );
		}

		Item cata = level.findPrizeItem( TrinketCatalyst.class );
		if (cata != null){
			int pos;
			do {
				pos = level.pointToCell( random() );
			} while (level.map[pos] != Terrain.EMPTY_SP || level.heaps.get( pos ) != null);
			level.drop( cata, pos );
		}

		entrance.set( Door.Type.LOCKED );
	}

	private static float[] prizeCats;

	private static Item prize(){
		int index = Random.chances( prizeCats );
		prizeCats[index] = 0;
		switch (index){
			case 0:
				return new Bomb().random();
			case 1:
				return Generator.randomWeapon();
			case 2:
				return Generator.randomArmor();
			case 3: default:
				return Generator.randomMissile();
		}
	}
}
