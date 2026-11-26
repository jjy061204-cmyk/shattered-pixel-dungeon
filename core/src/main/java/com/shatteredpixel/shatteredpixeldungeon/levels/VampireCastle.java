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

	// ───── 뱀파이어 영웅: 고향 같은 성 ─────
	//  흡혈귀의 성에 있는 동안 자연 재생 +20%, 핏빛 성배 충전 +20%, 적의 흡혈량 30% 감소

	public static final float HOME_REGEN = 1.2f;
	public static final float HOME_GRAIL = 1.2f;
	public static final float HOME_DRAIN = 0.7f;

	public static boolean vampireHome(){
		return Dungeon.hero != null && Dungeon.hero.isAlive()
				&& Dungeon.hero.heroClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.VAMPIRE
				&& isCastleLevel( Dungeon.level );
	}

	//적이 빨아들이는 양 (뱀파이어 영웅이 성에 있으면 줄어듦)
	public static int enemyDrain( int amount ){
		if (amount <= 0 || !vampireHome()) return amount;
		return Math.max( 0, Math.round( amount * HOME_DRAIN ) );
	}

	//버프 칸에 '성의 혈족'을 띄움 (성 밖으로 나가면 저절로 사라짐)
	public static void updateKinship(){
		if (vampireHome() && Dungeon.hero.buff( Kinship.class ) == null){
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect( Dungeon.hero, Kinship.class );
		}
	}

	public static class Kinship extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff {
		{
			type = buffType.POSITIVE;
			announced = false;
		}
		@Override
		public boolean act() {
			if (!vampireHome()) detach();
			spend( TICK );
			return true;
		}
		@Override
		public int icon() {
			return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.HEART;
		}
		@Override
		public void tintIcon( com.watabou.noosa.Image icon ) {
			icon.hardlight( 0.75f, 0.1f, 0.2f );
		}
		@Override
		public String desc() {
			return Messages.get( this, "desc" );
		}
	}
}
