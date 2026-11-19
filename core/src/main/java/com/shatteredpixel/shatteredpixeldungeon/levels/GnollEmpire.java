/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 갈림길 루트 '놀 제국'에서 여러 곳이 함께 쓰는 판정과 장치 처리
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollCenturion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGatekeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollJavelineer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollSapper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollScout;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollWarlord;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ScrapGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WarDrum;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;

public class GnollEmpire {

	//북소리가 닿는 거리
	public static final int DRUM_RANGE = 10;

	//지금 고른 광산 루트가 놀 제국인지
	public static boolean active(){
		return Routes.caves() == Routes.Caves.GNOLL_EMPIRE;
	}

	public static boolean isEmpireLevel( Level level ){
		return level instanceof GnollEmpireLevel || level instanceof GnollEmpireBossLevel;
	}

	//놀 제국에 속한 놀인지 (북소리·백부장의 지휘가 닿는 대상)
	public static boolean isGnoll( Char ch ){
		return ch instanceof Gnoll || ch instanceof Brute || ch instanceof Shaman
				|| ch instanceof GnollScout || ch instanceof GnollJavelineer || ch instanceof ScrapGnoll
				|| ch instanceof GnollCenturion || ch instanceof GnollGatekeeper || ch instanceof GnollWarlord
				|| ch instanceof GnollGuard || ch instanceof GnollSapper || ch instanceof GnollGeomancer;
	}

	//pos에서 가장 가까운, 아직 멀쩡한 경보 북 (range칸 안)
	public static WarDrum nearestDrum( int pos, int range ){
		if (Dungeon.level == null) return null;
		WarDrum best = null;
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m instanceof WarDrum && m.isAlive()){
				int d = Dungeon.level.distance( pos, m.pos );
				if (d <= range && (best == null || d < Dungeon.level.distance( pos, best.pos ))){
					best = (WarDrum) m;
				}
			}
		}
		return best;
	}

	//북 둘레에서 서서 칠 수 있는 칸 (from에서 가장 가까운 곳)
	public static int drumSpot( WarDrum drum, int from ){
		int best = -1;
		for (int n : PathFinder.NEIGHBOURS8){
			int c = drum.pos + n;
			if (c == from) return from;
			if (Dungeon.level.passable[c] && !Dungeon.level.avoid[c] && Actor.findChar( c ) == null){
				if (best == -1 || Dungeon.level.trueDistance( from, c ) < Dungeon.level.trueDistance( from, best )){
					best = c;
				}
			}
		}
		return best;
	}

	//불에 탄 나무 방책이 무너질 때: 방책에 붙어 있던 이들도 불에 휘말림
	public static void palisadeCollapsed( Level level, int pos ){
		if (Dungeon.level != level) return;
		if (level.heroFOV != null && pos < level.heroFOV.length && level.heroFOV[pos]){
			CellEmitter.get( pos ).burst( Speck.factory( Speck.WOOL ), 6 );
			CellEmitter.get( pos ).burst( FlameParticle.FACTORY, 10 );
			Sample.INSTANCE.play( Assets.Sounds.BURNING );
		}
		for (int n : PathFinder.NEIGHBOURS8){
			Char ch = Actor.findChar( pos + n );
			if (ch != null && ch.isAlive() && !ch.isImmune( Fire.class )){
				Buff.affect( ch, Burning.class ).reignite( ch );
			}
		}
	}

	//모닥불·용광로(특수 석상 칸)에 늘 타오르는 불꽃
	public static void addFireVisuals( Level level, Group group ){
		for (int i = 0; i < level.length(); i++){
			if (level.map[i] == Terrain.STATUE_SP){
				group.add( new Brazier( i ) );
			}
		}
	}

	private static class Brazier extends Emitter {

		private final int pos;

		Brazier( int pos ){
			super();
			this.pos = pos;
			PointF p = DungeonTilemap.tileCenterToWorld( pos );
			pos( p.x - 4, p.y - 10, 8, 6 );
			pour( FlameParticle.FACTORY, 0.12f );
		}

		@Override
		public void update() {
			if (Dungeon.level == null || pos >= Dungeon.level.length() || Dungeon.level.map[pos] != Terrain.STATUE_SP){
				on = false;
				killAndErase();
				return;
			}
			visible = Dungeon.level.heroFOV[pos];
			super.update();
		}
	}

	//지형 이름·설명 (일반 층과 보스 층이 함께 씀). 해당 없으면 null
	public static String tileName( int tile ){
		switch (tile){
			case Terrain.BARRICADE:      return Messages.get( GnollEmpire.class, "palisade_name" );
			case Terrain.HIGH_GRASS:     return Messages.get( GnollEmpire.class, "tent_name" );
			case Terrain.FURROWED_GRASS: return Messages.get( GnollEmpire.class, "tent_name" );
			case Terrain.GRASS:          return Messages.get( GnollEmpire.class, "straw_name" );
			case Terrain.STATUE:         return Messages.get( GnollEmpire.class, "totem_name" );
			case Terrain.STATUE_SP:      return Messages.get( GnollEmpire.class, "fire_name" );
			case Terrain.REGION_DECO:
			case Terrain.REGION_DECO_ALT:return Messages.get( GnollEmpire.class, "scrap_name" );
			case Terrain.WALL_DECO:      return Messages.get( GnollEmpire.class, "banner_wall_name" );
			case Terrain.EMPTY_SP:       return Messages.get( GnollEmpire.class, "planks_name" );
			default: return null;
		}
	}

	public static String tileDesc( int tile ){
		switch (tile){
			case Terrain.BARRICADE:      return Messages.get( GnollEmpire.class, "palisade_desc" );
			case Terrain.HIGH_GRASS:     return Messages.get( GnollEmpire.class, "tent_desc" );
			case Terrain.FURROWED_GRASS: return Messages.get( GnollEmpire.class, "tent_desc" );
			case Terrain.STATUE:         return Messages.get( GnollEmpire.class, "totem_desc" );
			case Terrain.STATUE_SP:      return Messages.get( GnollEmpire.class, "fire_desc" );
			case Terrain.REGION_DECO:
			case Terrain.REGION_DECO_ALT:return Messages.get( GnollEmpire.class, "scrap_desc" );
			case Terrain.WALL_DECO:      return Messages.get( GnollEmpire.class, "banner_wall_desc" );
			default: return null;
		}
	}
}
