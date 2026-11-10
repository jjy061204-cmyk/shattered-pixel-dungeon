/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 2층 (절차 생성 + 수호자의 투기장)
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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SanctuaryColossus;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.SanctuaryPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sanctuary.SanctuaryArenaRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.entrance.EntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlashingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.RockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WeakeningTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

//이차원의 성소 2층: 일반 층처럼 방을 이어 붙이되, 출구 방 대신 수호자의 투기장이 하나 붙어 있음.
//투기장 끝 왕좌에 성소의 거대 골렘이 잠들어 있고, 영웅이 투기장에 들어서면 문이 돌로 막히며 싸움이 시작됨.
//이기면 문이 다시 열리고 투기장 가운데 룬 원 위에 20층으로 돌아가는 게이트가 열림
public class SanctuaryBossLevel extends RegularLevel {

	public static final int BRANCH = 8;

	{
		color1 = 0x4f7a46;
		color2 = 0x9ff3e8;
	}

	private int throne = -1, door = -1, center = -1;
	private final ArrayList<Integer> summon = new ArrayList<>();
	private boolean fightStarted = false;
	private boolean bossDefeated = false;
	private boolean gateOpened = false;

	private SanctuaryArenaRoom arena(){
		if (rooms == null) return null;
		for (Room r : rooms){
			if (r instanceof SanctuaryArenaRoom) return (SanctuaryArenaRoom) r;
		}
		return null;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_SANCTUARY;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_SANCTUARY;
	}

	@Override
	public void playLevelMusic() {
		if (fightStarted && !bossDefeated){
			Music.INSTANCE.play( Assets.Music.CITY_BOSS, true );
		} else if (bossDefeated){
			Music.INSTANCE.end();
		} else {
			Music.INSTANCE.playTracks( CavesLevel.CAVES_TRACK_LIST, CavesLevel.CAVES_TRACK_CHANCES, false );
		}
	}

	//==== 방 구성 ====

	@Override
	protected ArrayList<Room> initRooms() {
		ArrayList<Room> rooms = new ArrayList<>();
		rooms.add( roomEntrance = EntranceRoom.createEntrance() );
		rooms.add( new SanctuaryArenaRoom() );
		SanctuaryLevel.addStandards( rooms, standardRooms( false ) );
		return rooms;
	}

	@Override
	protected int standardRooms( boolean forceMax ) {
		if (forceMax) return 5;
		return 3 + Random.chances( new float[]{1, 2, 1} );
	}

	@Override
	protected Painter painter() {
		return new SanctuaryPainter()
				.setWater( 0.20f, 4 )
				.setGrass( 0.30f, 3 )
				.setTraps( nTraps(), trapClasses(), trapChances() );
	}

	@Override
	protected int nTraps() {
		return Random.NormalIntRange( 1, 3 );
	}

	@Override
	protected Class<?>[] trapClasses() {
		return new Class[]{ RockfallTrap.class, GuardianTrap.class, FlashingTrap.class, WeakeningTrap.class };
	}

	@Override
	protected float[] trapChances() {
		return new float[]{ 3, 2, 2, 2 };
	}

	@Override
	protected boolean build() {
		if (!super.build()) return false;

		//위쪽 계단은 성소 1층의 북쪽 계단으로
		for (LevelTransition t : transitions){
			if (t.type == LevelTransition.Type.REGULAR_ENTRANCE){
				t.destDepth = SanctuaryQuest.DEPTH;
				t.destBranch = SanctuaryLevel.BRANCH;
				t.destType = LevelTransition.Type.REGULAR_EXIT;
			}
		}

		SanctuaryArenaRoom a = arena();
		if (a == null || a.throne < 0) return false;
		throne = a.throne;
		door = a.door;
		center = a.center;
		summon.clear();
		summon.addAll( a.summon );
		return true;
	}

	public ArrayList<Integer> summonCells(){
		return summon;
	}

	//==== 몬스터 ====

	@Override
	public int mobLimit() {
		return 4 + Random.Int( 2 );
	}

	@Override
	public Mob createMob() {
		return SanctuaryLevel.randomGolem();
	}

	@Override
	protected void createMobs() {
		super.createMobs();
		SanctuaryColossus boss = new SanctuaryColossus();
		boss.pos = throne;
		mobs.add( boss );
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		//싸움 중이거나 투기장 안으로는 다시 나타나지 않음
		if (fightStarted && !bossDefeated) return -1;
		return super.randomRespawnCell( ch );
	}

	//==== 아이템 ====

	@Override
	protected void createItems() {
		int n = 2 + Random.Int( 2 );
		for (int i = 0; i < n; i++){
			int cell = randomDropCell();
			if (cell < 0) continue;
			if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
				map[cell] = Terrain.GRASS;
				losBlocking[cell] = false;
			}
			drop( SanctuaryLevel.loot( null ), cell ).type = Random.Int( 4 ) == 0 ? Heap.Type.CHEST : Heap.Type.HEAP;
		}
		int cell = randomDropCell();
		if (cell >= 0) drop( new Gold().random(), cell );
		cell = randomDropCell();
		if (cell >= 0) drop( Generator.random( Generator.Category.FOOD ), cell );
		for (Item item : itemsToSpawn){
			cell = randomDropCell();
			if (cell >= 0) drop( item, cell );
		}
		itemsToSpawn.clear();
	}

	//==== 보스전 ====

	//투기장 안쪽 (벽·문 제외)
	private boolean inArena( int cell ){
		SanctuaryArenaRoom a = arena();
		return a != null && a.interior( cellToPoint( cell ) );
	}

	@Override
	public void occupyCell( Char ch ) {
		super.occupyCell( ch );
		if (ch != Dungeon.hero || bossDefeated) return;

		if (!fightStarted && inArena( ch.pos )){
			for (Mob m : mobs){
				if (m instanceof SanctuaryColossus){
					((SanctuaryColossus) m).awaken();
					break;
				}
			}
		} else if (fightStarted && !inArena( ch.pos ) && door >= 0 && map[door] == Terrain.WALL){
			//순간이동 등으로 투기장 밖에 나가 버리면 다시 들어올 수 있게 문을 엶
			openDoor();
		}
	}

	//보스가 깨어남: 층이 잠기고, 영웅이 투기장 안에 있으면 문이 돌로 막힘
	public void onFightStart(){
		if (fightStarted) return;
		fightStarted = true;
		seal();
		if (door >= 0 && inArena( Dungeon.hero.pos ) && Actor.findChar( door ) == null && heaps.get( door ) == null){
			set( door, Terrain.WALL );
			GameScene.updateMap( door );
			CellEmitter.get( door ).burst( Speck.factory( Speck.ROCK ), 10 );
			Sample.INSTANCE.play( Assets.Sounds.ROCKS );
			Dungeon.observe();
			GLog.w( Messages.get( this, "sealed" ) );
		}
		playLevelMusic();
	}

	private void openDoor(){
		if (door < 0) return;
		set( door, Terrain.DOOR );
		GameScene.updateMap( door );
		CellEmitter.get( door ).burst( Speck.factory( Speck.ROCK ), 6 );
		Dungeon.observe();
	}

	//보스를 쓰러뜨림: 문이 열리고 층 잠금이 풀림. 돌아가는 게이트는 엑스칼리버를 주워야 열림
	public void onBossDefeated(){
		if (bossDefeated) return;
		bossDefeated = true;
		SanctuaryQuest.bossDefeated = true;
		unseal();
		if (door >= 0 && map[door] == Terrain.WALL) openDoor();
		GLog.p( Messages.get( this, "boss_down" ) );
		playLevelMusic();
	}

	//왕좌 위의 엑스칼리버를 주움: 투기장 가운데 룬 원 위에 20층으로 돌아가는 게이트가 열림
	public void onExcaliburTaken(){
		if (!bossDefeated || gateOpened) return;
		gateOpened = true;
		SanctuaryQuest.completed = true;

		if (center >= 0){
			transitions.add( new LevelTransition( this, center, LevelTransition.Type.BRANCH_ENTRANCE,
					SanctuaryQuest.DEPTH, 0, LevelTransition.Type.BRANCH_EXIT ) );
			SanctuaryLevel.Gate g = new SanctuaryLevel.Gate();
			g.pos( center % width(), center / width() );
			customTiles.add( g );
			GameScene.add( g, false );
			SanctuaryLevel.GateTop top = new SanctuaryLevel.GateTop();
			top.pos( center % width(), center / width() - 1 );
			customWalls.add( top );
			GameScene.add( top, true );
			CellEmitter.get( center ).burst( Speck.factory( Speck.LIGHT ), 14 );
			CellEmitter.get( center ).pour( com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote.FACTORY, 0.25f );
			Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
		}
		GLog.p( Messages.get( this, "portal_open" ) );
	}

	@Override
	public Group addVisuals() {
		super.addVisuals();
		SanctuaryLevel.addSanctuaryVisuals( this, visuals );
		return visuals;
	}

	@Override
	public String tileName( int tile ) {
		return SanctuaryLevel.name( tile, super.tileName( tile ) );
	}

	@Override
	public String tileDesc( int tile ) {
		if (tile == Terrain.ENTRANCE) return Messages.get( this, "entrance_desc" );
		return SanctuaryLevel.desc( tile, super.tileDesc( tile ) );
	}

	private static final String THRONE = "throne";
	private static final String DOOR = "door";
	private static final String CENTER = "center";
	private static final String SUMMON = "summon";
	private static final String FIGHT = "fight";
	private static final String DEFEATED = "defeated";
	private static final String GATE = "gate_opened";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( THRONE, throne );
		bundle.put( DOOR, door );
		bundle.put( CENTER, center );
		int[] s = new int[summon.size()];
		for (int i = 0; i < s.length; i++) s[i] = summon.get( i );
		bundle.put( SUMMON, s );
		bundle.put( FIGHT, fightStarted );
		bundle.put( DEFEATED, bossDefeated );
		bundle.put( GATE, gateOpened );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		throne = bundle.getInt( THRONE );
		door = bundle.getInt( DOOR );
		center = bundle.getInt( CENTER );
		summon.clear();
		if (bundle.contains( SUMMON )) for (int i : bundle.getIntArray( SUMMON )) summon.add( i );
		fightStarted = bundle.getBoolean( FIGHT );
		bossDefeated = bundle.getBoolean( DEFEATED );
		//옛 저장: 이미 게이트가 열려 있었으면 그대로
		gateOpened = bundle.contains( GATE ) ? bundle.getBoolean( GATE ) : bossDefeated;
	}
}
