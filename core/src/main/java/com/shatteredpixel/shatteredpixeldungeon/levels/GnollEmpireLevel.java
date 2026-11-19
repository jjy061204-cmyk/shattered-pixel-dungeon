/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 갈림길 테마: 놀 제국 (11~14층) — 놀 부족들이 대족장 아래 뭉쳐 DM-300과 기계들을 몰아내고 세운 목책 요새
 *  - 11층 변방 초소: 초소 방 2개(둥근 방책 안 경보 북), 첫 북은 입구 방에
 *  - 12층 약탈 캠프: 캠프 방 1~2개(천막·모닥불·포로 우리·전리품 더미·경보 북)
 *  - 13층 고철 공방: 용광로와 고철 바리케이드가 있는 공방, 놀에게 붙잡힌 대장장이의 노역장
 *  - 14층 요새 외곽: 출구를 두른 방책 성벽과 성문 2곳(문지기), 망루, 문지기 열쇠로 여는 전리품 창고
 *  - 층마다 경보 북 2~3개. 방 개수·몹 수·아이템 수는 원작 광산과 같은 예산
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollCage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGatekeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ScrapBarricade;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WarDrum;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.GnollEmpirePainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll.GnollCampRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll.GnollFortressRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll.GnollOutpostRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll.LootStoreRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.gnoll.ScrapWorkshopRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollEmpireLevel extends CavesLevel {

	{
		color1 = 0x4b3324;
		color2 = 0xd98c3a;
	}

	//망루 칸 (서 있는 쪽은 시야 +2)
	public ArrayList<Integer> towers = new ArrayList<>();
	//천막 칸 (긴 풀 지형, 캠프 방에만 있음)
	public ArrayList<Integer> tents = new ArrayList<>();
	//아직 닫혀 있는 요새 성문
	public ArrayList<Integer> gates = new ArrayList<>();
	//문지기가 전리품 창고 열쇠를 가지고 있는지, 이미 떨어뜨렸는지
	private boolean keyHeld = false;
	private boolean keyDropped = false;
	private boolean towerHint = false;
	private boolean gateHint = false;

	//이번 층에 넣을 놀 제국 구조물 (층을 만드는 동안만 씀)
	private ArrayList<Room> plan = null;

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_GNOLL_EMPIRE;
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
		return new GnollEmpirePainter()
				.setWater( feeling == Feeling.WATER ? 0.85f : 0.30f, 6 )
				.setGrass( feeling == Feeling.GRASS ? 0.65f : 0.15f, 3 )
				.setTraps( nTraps(), trapClasses(), trapChances() );
	}

	// ── 방 구성 ──

	private ArrayList<Room> structures(){
		ArrayList<Room> list = new ArrayList<>();
		switch (Dungeon.depth){
			case 11:
				list.add( new GnollOutpostRoom() );
				list.add( new GnollOutpostRoom() );
				break;
			case 12:
				list.add( new GnollCampRoom() );
				if (Random.Int( 2 ) == 0) list.add( new GnollCampRoom() );
				break;
			case 13:
				list.add( new ScrapWorkshopRoom() );
				if (Random.Int( 2 ) == 0) list.add( new GnollOutpostRoom() );
				break;
			case 14:
				list.add( new GnollCampRoom() );
				break;
		}
		return list;
	}

	@Override
	protected ArrayList<Room> initRooms() {
		plan = structures();
		ArrayList<Room> rooms = super.initRooms();
		rooms.addAll( plan );
		plan = null;

		if (Dungeon.depth == 14){
			//출구 방은 요새, 특수방 하나는 전리품 창고
			rooms.remove( roomExit );
			roomExit = new GnollFortressRoom();
			rooms.add( roomExit );
			rooms.add( new LootStoreRoom() );
		}
		return rooms;
	}

	@Override
	protected int standardRooms( boolean forceMax ) {
		int n = super.standardRooms( forceMax );
		if (plan != null){
			for (Room r : plan){
				if (r instanceof StandardRoom) n -= ((StandardRoom) r).sizeFactor();
			}
		}
		return Math.max( 2, n );
	}

	@Override
	protected int specialRooms( boolean forceMax ) {
		int n = super.specialRooms( forceMax );
		if (Dungeon.depth == 14) n = Math.max( 1, n - 1 );
		return n;
	}

	// ── 몹과 장치 ──

	@Override
	protected void createMobs() {
		int drums = 0;
		boolean store = false;
		boolean guards = false;

		for (Room r : rooms){
			if (r instanceof GnollOutpostRoom){
				if (addProp( new WarDrum(), ((GnollOutpostRoom) r).drum )) drums++;
			} else if (r instanceof GnollCampRoom){
				if (addProp( new WarDrum(), ((GnollCampRoom) r).drum )) drums++;
				addProp( GnollCage.random(), ((GnollCampRoom) r).cage );
				tents.addAll( ((GnollCampRoom) r).tents );
			} else if (r instanceof ScrapWorkshopRoom){
				for (int c : ((ScrapWorkshopRoom) r).barricades){
					addProp( new ScrapBarricade(), c );
				}
			} else if (r instanceof GnollFortressRoom){
				GnollFortressRoom f = (GnollFortressRoom) r;
				towers.addAll( f.towers );
				for (int i = 0; i < 2; i++){
					if (f.gates[i] == -1) continue;
					gates.add( f.gates[i] );
					GnollGatekeeper g = new GnollGatekeeper();
					g.post = f.posts[i];
					g.gate = f.gates[i];
					if (addProp( g, f.posts[i] )) guards = true;
					else gates.remove( Integer.valueOf( f.gates[i] ) );
				}
			} else if (r instanceof LootStoreRoom){
				store = true;
			}
		}
		applyGateFlags();

		//11층: 첫 북은 입구에서 보이는 자리에
		if (Dungeon.depth == 11 && roomEntrance != null){
			for (int tries = 0; tries < 40; tries++){
				int cell = pointToCell( roomEntrance.random( 1 ) );
				int d = distance( cell, entrance() );
				if (d >= 2 && d <= 4 && openCell( cell ) && addProp( new WarDrum(), cell )){
					drums++;
					break;
				}
			}
		}

		//층마다 경보 북 2~3개
		int want = 2 + Random.Int( 2 );
		for (int tries = 0; tries < 60 && drums < want; tries++){
			int cell = randomPropCell();
			if (cell != -1 && addProp( new WarDrum(), cell )) drums++;
		}

		//13·14층: 반쯤의 확률로 포로 우리 하나 더
		if ((Dungeon.depth == 13 || Dungeon.depth == 14) && Random.Int( 2 ) == 0){
			for (int tries = 0; tries < 30; tries++){
				int cell = randomPropCell();
				if (cell != -1 && addProp( GnollCage.random(), cell )) break;
			}
		}

		//전리품 창고 열쇠: 문지기가 있으면 문지기가, 없으면 원작처럼 바닥 어딘가에
		if (store){
			if (guards) keyHeld = true;
			else addItemToSpawn( new IronKey( Dungeon.depth ) );
		}

		super.createMobs();
	}

	private boolean addProp( Mob m, int cell ){
		if (cell < 0 || cell >= length()) return false;
		if (!passable[cell] || solid[cell] || findMob( cell ) != null) return false;
		if (map[cell] == Terrain.EXIT || map[cell] == Terrain.ENTRANCE) return false;
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

	//놀 제국 구조물이 아닌 일반 방의 안쪽 칸
	private int randomPropCell(){
		Room r = Random.element( rooms );
		if (!(r instanceof StandardRoom) || r == roomEntrance || r == roomExit
				|| r instanceof GnollOutpostRoom || r instanceof GnollCampRoom || r instanceof ScrapWorkshopRoom
				|| r instanceof BlacksmithRoom){
			return -1;
		}
		if (r.width() < 5 || r.height() < 5) return -1;
		int cell = pointToCell( r.random( 2 ) );
		if (!openCell( cell )) return -1;
		if (distance( cell, entrance() ) < 5) return -1;
		return cell;
	}

	// ── 요새 성문 ──

	private void applyGateFlags(){
		for (int g : gates){
			if (g >= 0 && g < length() && map[g] == Terrain.DOOR){
				passable[g] = false;
				avoid[g] = false;
			}
		}
	}

	@Override
	public void buildFlagMaps() {
		super.buildFlagMaps();
		applyGateFlags();
	}

	@Override
	public void updateCellFlags( int cell ) {
		super.updateCellFlags( cell );
		if (gates.contains( cell ) && map[cell] == Terrain.DOOR){
			passable[cell] = false;
			avoid[cell] = false;
		}
	}

	public void openGate( int gate ){
		if (!gates.contains( gate )) return;
		gates.remove( Integer.valueOf( gate ) );
		if (map[gate] == Terrain.DOOR){
			set( gate, Terrain.OPEN_DOOR );
			GameScene.updateMap( gate );
			Dungeon.observe();
		} else {
			updateCellFlags( gate );
		}
		Sample.INSTANCE.play( Assets.Sounds.OPEN );
		if (heroFOV[gate]){
			CellEmitter.get( gate ).burst( Speck.factory( Speck.WOOL ), 6 );
		}
		GLog.p( Messages.get( this, "gate_open" ) );
	}

	//처음 쓰러진 문지기가 전리품 창고 열쇠를 떨어뜨림
	public void dropStoreKey( int pos ){
		if (!keyHeld || keyDropped) return;
		keyDropped = true;
		int cell = pos;
		if (pit[cell] || !passable[cell]){
			cell = -1;
			for (int n : PathFinder.NEIGHBOURS8){
				int c = pos + n;
				if (passable[c] && !pit[c]){
					cell = c;
					break;
				}
			}
			if (cell == -1) cell = Dungeon.hero.pos;
		}
		drop( new IronKey( Dungeon.depth ), cell ).sprite.drop();
		GLog.i( Messages.get( this, "key_dropped" ) );
	}

	public static boolean isTower( int pos ){
		return Dungeon.level instanceof GnollEmpireLevel && ((GnollEmpireLevel) Dungeon.level).towers.contains( pos );
	}

	// ── 지형 반응 ──

	@Override
	public void destroy( int pos ) {
		boolean palisade = map[pos] == Terrain.BARRICADE;
		boolean gate = gates.contains( pos );
		super.destroy( pos );
		if (gate){
			gates.remove( Integer.valueOf( pos ) );
			updateCellFlags( pos );
		}
		if (palisade || gate){
			GnollEmpire.palisadeCollapsed( this, pos );
		}
	}

	@Override
	public void updateFieldOfView( Char c, boolean[] fieldOfView ) {
		if (towers.contains( c.pos )){
			int old = c.viewDistance;
			c.viewDistance = old + 2;
			super.updateFieldOfView( c, fieldOfView );
			c.viewDistance = old;
		} else {
			super.updateFieldOfView( c, fieldOfView );
		}
	}

	@Override
	public void occupyCell( Char ch ) {
		super.occupyCell( ch );
		if (ch instanceof Hero){
			if (!towerHint && towers.contains( ch.pos )){
				towerHint = true;
				GLog.p( Messages.get( this, "tower_hint" ) );
			}
			if (!gateHint){
				for (int g : gates){
					if (adjacent( ch.pos, g )){
						gateHint = true;
						GLog.w( Messages.get( this, "gate_hint" ) );
						break;
					}
				}
			}
		}
	}

	@Override
	public Group addVisuals() {
		super.addVisuals();
		GnollEmpire.addFireVisuals( this, visuals );
		return visuals;
	}

	@Override
	public String tileName( int tile ) {
		String n = GnollEmpire.tileName( tile );
		return n != null ? n : super.tileName( tile );
	}

	@Override
	public String tileDesc( int tile ) {
		String d = GnollEmpire.tileDesc( tile );
		return d != null ? d : super.tileDesc( tile );
	}

	// ── 저장 ──

	private static final String TENTS = "tents";
	private static final String TOWERS = "towers";
	private static final String GATES = "gates";
	private static final String KEY_HELD = "key_held";
	private static final String KEY_DROPPED = "key_dropped";
	private static final String TOWER_HINT = "tower_hint";
	private static final String GATE_HINT = "gate_hint";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		int[] t = new int[towers.size()];
		for (int i = 0; i < t.length; i++) t[i] = towers.get( i );
		bundle.put( TOWERS, t );
		int[] tn = new int[tents.size()];
		for (int i = 0; i < tn.length; i++) tn[i] = tents.get( i );
		bundle.put( TENTS, tn );
		int[] g = new int[gates.size()];
		for (int i = 0; i < g.length; i++) g[i] = gates.get( i );
		bundle.put( GATES, g );
		bundle.put( KEY_HELD, keyHeld );
		bundle.put( KEY_DROPPED, keyDropped );
		bundle.put( TOWER_HINT, towerHint );
		bundle.put( GATE_HINT, gateHint );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		//성문 목록을 먼저 읽어 두어야 지형 판정(buildFlagMaps)에 반영됨
		towers.clear();
		gates.clear();
		tents.clear();
		if (bundle.contains( TENTS )) for (int t : bundle.getIntArray( TENTS )) tents.add( t );
		if (bundle.contains( TOWERS )) for (int t : bundle.getIntArray( TOWERS )) towers.add( t );
		if (bundle.contains( GATES )) for (int g : bundle.getIntArray( GATES )) gates.add( g );
		keyHeld = bundle.getBoolean( KEY_HELD );
		keyDropped = bundle.getBoolean( KEY_DROPPED );
		towerHint = bundle.getBoolean( TOWER_HINT );
		gateHint = bundle.getBoolean( GATE_HINT );
		super.restoreFromBundle( bundle );
	}
}
