/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 갈림길 테마: 흡혈귀의 성 (11~14층) — 햇빛을 피해 광산에 숨어든 흡혈 군주가 동굴 생물을 감염시키고 세운 성
 *  - 11~12층: 성 아래 지하 묘지 (감옥의 방 모양: 묘실·기둥·관이 늘어선 방). 13~14층: 성의 홀 (드워프 도시의 방 모양)
 *  - 물 대신 핏물 웅덩이. 촛대·화로를 켜면 둘레가 밝아져 흡혈 몹의 회복·흡혈을 막음
 *  - 관: 12·13층 1개, 14층 2개. 관이 있는 층은 자연 리스폰 대신 어둠 속 관에서 피의 하수인이 나옴
 *  - 14층 연회장 샹들리에, 13층 성배 제단(특수방 하나를 대신)
 *  - 방·몹·아이템 수는 원작 광산과 같은 예산. 트롤 대장장이 퀘스트는 그대로
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NightBlood;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CastleLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Chandelier;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Coffin;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GrailAltar;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.CastlePainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class VampireCastleLevel extends CavesLevel {

	{
		color1 = 0x3a1a22;
		color2 = 0xc0303c;
	}

	private boolean castle(){
		return Dungeon.depth >= 13;
	}

	@Override
	public String tilesTex() {
		return castle() ? Assets.Environment.TILES_VAMPIRE_CASTLE : Assets.Environment.TILES_VAMPIRE_CRYPT;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_BLOOD;
	}

	//핏물은 타일과 같은 32x32 밀도로 찍음
	@Override
	public int waterTexScale() {
		return 2;
	}

	@Override
	protected boolean veinSparkles() {
		return false;
	}

	@Override
	public String entryText() {
		return Dungeon.depth == 11 ? Messages.get( this, "entry" ) : null;
	}

	@Override
	protected Painter painter() {
		return new CastlePainter()
				.setWater( feeling == Feeling.WATER ? 0.85f : 0.30f, 5 )
				.setGrass( feeling == Feeling.GRASS ? 0.75f : 0.20f, 3 )
				.setTraps( nTraps(), trapClasses(), trapChances() );
	}

	@Override
	protected ArrayList<Room> initRooms() {
		//11·12층은 지하 묘지 (감옥의 방 모양), 13·14층은 성의 홀 (드워프 도시의 방 모양)
		StandardRoom.depthOverride = castle() ? 17 : 7;
		try {
			return super.initRooms();
		} finally {
			StandardRoom.depthOverride = 0;
		}
	}

	@Override
	protected int specialRooms( boolean forceMax ) {
		int n = super.specialRooms( forceMax );
		//13층: 성배 제단이 특수방 하나를 대신함
		if (Dungeon.depth == 13) n = Math.max( 1, n - 1 );
		return n;
	}

	// ── 장치 ──

	private int coffinCount(){
		switch (Dungeon.depth){
			case 12: case 13: return 1;
			case 14:          return 2;
			default:          return 0;
		}
	}

	@Override
	protected void createMobs() {

		//입구의 꺼지지 않는 화로
		if (roomEntrance != null){
			for (int tries = 0; tries < 40; tries++){
				int cell = pointToCell( roomEntrance.random( 1 ) );
				int d = distance( cell, entrance() );
				if (d >= 2 && d <= 3 && openCell( cell )){
					CastleLight l = CastleLight.brazier( true );
					l.permanent = true;
					if (addProp( l, cell )) break;
				}
			}
		}

		//방마다 촛대나 화로 (일부는 켜져 있음)
		for (Room r : rooms){
			if (!(r instanceof StandardRoom) || r == roomEntrance || r instanceof BlacksmithRoom) continue;
			if (r.width() < 5 || r.height() < 5) continue;
			if (Random.Float() > 0.6f) continue;
			for (int tries = 0; tries < 10; tries++){
				int cell = pointToCell( r.random( 1 ) );
				if (!openCell( cell )) continue;
				boolean lit = Random.Int( 3 ) == 0;
				CastleLight l = (castle() && Random.Int( 3 ) == 0) ? CastleLight.brazier( lit ) : CastleLight.candelabra( lit );
				if (addProp( l, cell )) break;
			}
		}

		//관
		int want = coffinCount();
		for (int tries = 0; tries < 80 && want > 0; tries++){
			int cell = randomPropCell( 8 );
			if (cell == -1 || VampireCastle.isLitAt( this, cell )) continue;
			if (addProp( new Coffin(), cell )) want--;
		}

		//14층: 큰 방 천장의 샹들리에
		if (Dungeon.depth == 14){
			int chandeliers = 1 + Random.Int( 2 );
			for (int tries = 0; tries < 60 && chandeliers > 0; tries++){
				Room r = Random.element( rooms );
				if (!(r instanceof StandardRoom) || r == roomEntrance || r == roomExit || r instanceof BlacksmithRoom) continue;
				if (r.width() < 7 || r.height() < 7) continue;
				int cell = pointToCell( r.center() );
				if (openCell( cell ) && addProp( new Chandelier(), cell )) chandeliers--;
			}
		}

		//13층: 성배 제단
		if (Dungeon.depth == 13){
			for (int tries = 0; tries < 60; tries++){
				int cell = randomPropCell( 6 );
				if (cell != -1 && addProp( GrailAltar.create(), cell )) break;
			}
		}

		super.createMobs();
	}

	@Override
	public Mob createMob() {
		Mob m = super.createMob();
		//박쥐는 원작 그대로, 밤의 피만 얹음
		if (m instanceof Bat) NightBlood.grant( m );
		return m;
	}

	private boolean addProp( Mob m, int cell ){
		if (cell < 0 || cell >= length()) return false;
		if (!passable[cell] || solid[cell] || findMob( cell ) != null) return false;
		if (map[cell] == Terrain.EXIT || map[cell] == Terrain.ENTRANCE || map[cell] == Terrain.WATER) return false;
		if (traps.get( cell ) != null || plants.get( cell ) != null || heaps.get( cell ) != null) return false;
		m.pos = cell;
		mobs.add( m );
		return true;
	}

	//사방이 트여 있어 길을 막지 않는 칸
	private boolean openCell( int cell ){
		for (int n : PathFinder.NEIGHBOURS4){
			int c = cell + n;
			if (!passable[c] || map[c] == Terrain.DOOR || map[c] == Terrain.OPEN_DOOR) return false;
		}
		return map[cell] == Terrain.EMPTY || map[cell] == Terrain.EMPTY_DECO || map[cell] == Terrain.GRASS;
	}

	//일반 방의 안쪽 칸 (입구에서 minDist칸 이상)
	private int randomPropCell( int minDist ){
		Room r = Random.element( rooms );
		if (!(r instanceof StandardRoom) || r == roomEntrance || r == roomExit || r instanceof BlacksmithRoom){
			return -1;
		}
		if (r.width() < 5 || r.height() < 5) return -1;
		int cell = pointToCell( r.random( 2 ) );
		if (!openCell( cell )) return -1;
		if (distance( cell, entrance() ) < minDist) return -1;
		return cell;
	}

	// ── 관에서 나오는 하수인 ──

	@Override
	public boolean spawnMob( int disLimit ) {
		ArrayList<Coffin> coffins = new ArrayList<>();
		boolean any = false;
		for (Mob m : mobs){
			if (m instanceof Coffin && m.isAlive()){
				any = true;
				if (((Coffin) m).dormant() && distance( m.pos, Dungeon.hero.pos ) >= 4) coffins.add( (Coffin) m );
			}
		}
		//관을 모두 부수면 원작 리스폰
		if (!any) return super.spawnMob( disLimit );
		//남은 관이 모두 빛 안에 있으면 아무도 나오지 않음
		if (coffins.isEmpty()) return false;
		return Random.element( coffins ).release();
	}

	@Override
	public String tileName( int tile ) {
		String key = tileKey( tile );
		if (key != null) return Messages.get( VampireCastleLevel.class, key + "_name" );
		String n = VampireCastle.tileName( tile );
		return n != null ? n : super.tileName( tile );
	}

	@Override
	public String tileDesc( int tile ) {
		String key = tileKey( tile );
		if (key != null) return Messages.get( VampireCastleLevel.class, key + "_desc" );
		String d = VampireCastle.tileDesc( tile );
		return d != null ? d : super.tileDesc( tile );
	}

	//성(13~14층)과 지하 묘지(11~12층)가 같은 지형을 다르게 그림
	private String tileKey( int tile ){
		String p = castle() ? "castle_" : "crypt_";
		switch (tile){
			case Terrain.WALL_DECO:       return p + "wall_deco";
			case Terrain.EMPTY_DECO:      return p + "floor_deco";
			case Terrain.EMPTY_SP:        return p + "floor_sp";
			case Terrain.GRASS:           return p + "grass";
			case Terrain.HIGH_GRASS:      return p + "high_grass";
			case Terrain.FURROWED_GRASS:  return p + "furrowed_grass";
			case Terrain.BOOKSHELF:       return p + "bookshelf";
			case Terrain.STATUE:          return p + "statue";
			case Terrain.STATUE_SP:       return "lord_statue";
			case Terrain.REGION_DECO:     return p + "region_deco";
			case Terrain.REGION_DECO_ALT: return p + "region_deco_alt";
			case Terrain.ENTRANCE:
			case Terrain.ENTRANCE_SP:     return "entrance";
			case Terrain.EXIT:            return "exit";
			default:                      return null;
		}
	}
}
