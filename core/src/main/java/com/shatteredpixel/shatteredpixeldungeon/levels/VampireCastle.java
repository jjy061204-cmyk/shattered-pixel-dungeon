/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 갈림길 테마: 흡혈귀의 성 (11~15층) 공용 규칙 — 빛과 어둠
 *  - 켜진 촛대·화로·샹들리에의 반경 안, 불이 붙은 칸, (보스 층) 달빛 창 아래 세로 줄은 밝은 칸
 *  - 흡혈 몹(밤의 피)은 어두운 칸에서 회복하고, 밝은 칸에서는 회복·흡혈이 막히고 명중이 20% 떨어짐
 *  - 플레이어의 시야는 줄지 않음
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CastleLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class VampireCastle {

	//지금 고른 광산 루트가 흡혈귀의 성인지
	public static boolean active(){
		return Routes.caves() == Routes.Caves.VAMPIRE_CASTLE;
	}

	public static boolean isCastleLevel( Level level ){
		return level instanceof VampireCastleLevel || level instanceof VampireCastleBossLevel;
	}

	//밝은 칸인지 (흡혈귀의 성이 아닌 층에서는 늘 false)
	public static boolean isLit( int cell ){
		return isLitAt( Dungeon.level, cell );
	}

	public static boolean isLitAt( Level level, int cell ){
		if (level == null || !isCastleLevel( level ) || cell < 0 || cell >= level.length()) return false;

		Blob fire = level.blobs.get( Fire.class );
		if (fire != null && fire.volume > 0 && fire.cur != null && fire.cur[cell] > 0) return true;

		if (level instanceof VampireCastleBossLevel && ((VampireCastleBossLevel) level).moonLit( cell )) return true;

		for (Mob m : level.mobs){
			if (m instanceof CastleLight && m.isAlive() && ((CastleLight) m).lit
					&& level.distance( cell, m.pos ) <= ((CastleLight) m).radius()){
				return true;
			}
		}
		return false;
	}

	//핏물 웅덩이 위인지
	public static boolean inBlood( int cell ){
		Level level = Dungeon.level;
		return level != null && isCastleLevel( level ) && cell >= 0 && cell < level.length()
				&& level.map[cell] == Terrain.WATER;
	}

	//지형 이름·설명 (일반 층과 보스 층이 함께 씀). 해당 없으면 null
	public static String tileName( int tile ){
		switch (tile){
			case Terrain.WATER: return Messages.get( VampireCastle.class, "blood_name" );
			default: return null;
		}
	}

	public static String tileDesc( int tile ){
		switch (tile){
			case Terrain.WATER: return Messages.get( VampireCastle.class, "blood_desc" );
			default: return null;
		}
	}
}
