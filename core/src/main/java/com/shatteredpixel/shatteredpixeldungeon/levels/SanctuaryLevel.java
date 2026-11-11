/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 1층 (절차 생성)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RuneSentinel;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.StoneSoldier;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.StoneWarden;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.SanctuaryPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sanctuary.SanctuaryGateRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ArmoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CryptRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.GardenRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.LibraryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicWellRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PoolRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.RunestoneRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SacrificeRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SentryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.StatueRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.StorageRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.TreasuryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.AquariumRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.CircleWallRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EmptyRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.HallwayRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.LibraryRingRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.PillarsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.PlantsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.RingRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.RitualRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.RuinsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.SegmentedLibraryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.SegmentedRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.SkullsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StatuesRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StripedRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StudyRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.WaterBridgeRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.exit.ExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.CorrosionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlashingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FrostTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.RockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WeakeningTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

//이차원의 성소 1층: 드워프와는 아무 관계 없는 아주 오래된 유적.
//일반 층처럼 방을 이어 붙여 만들고, 입구에는 20층으로 돌아가는 고대 게이트가 서 있음.
//강화 주문서·힘의 물약은 나오지 않음
public class SanctuaryLevel extends RegularLevel {

	public static final int BRANCH = 7;

	{
		color1 = 0x4f7a46;
		color2 = 0x9ff3e8;
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
		Music.INSTANCE.playTracks( CavesLevel.CAVES_TRACK_LIST, CavesLevel.CAVES_TRACK_CHANCES, false );
	}

	//==== 방 구성 ====

	//성소 분위기에 맞는 일반 방 (낭떠러지가 생기는 방은 뺌: 떨어지면 엉뚱한 층으로 가므로)
	private static final Class<?>[] STD_ROOMS = {
			StatuesRoom.class, RuinsRoom.class, RitualRoom.class, PillarsRoom.class,
			RingRoom.class, HallwayRoom.class, CircleWallRoom.class, SegmentedRoom.class,
			LibraryRingRoom.class, SegmentedLibraryRoom.class, AquariumRoom.class, PlantsRoom.class,
			WaterBridgeRoom.class, StudyRoom.class, SkullsRoom.class, StripedRoom.class, EmptyRoom.class
	};
	private static final float[] STD_CHANCES = {
			9, 8, 6, 6,
			5, 5, 4, 4,
			3, 2, 3, 4,
			2, 2, 2, 2, 3
	};

	//열쇠 문이 있어도 같은 층(20층) 열쇠로 열림. 구덩이·함정방·수정 열쇠 방은 뺌
	private static final Class<?>[] SPECIALS = {
			GardenRoom.class, LibraryRoom.class, MagicWellRoom.class, RunestoneRoom.class,
			StatueRoom.class, TreasuryRoom.class, StorageRoom.class, PoolRoom.class,
			SentryRoom.class, CryptRoom.class, ArmoryRoom.class, SacrificeRoom.class
	};

	public static StandardRoom randomStandard(){
		return (StandardRoom) Reflection.newInstance( STD_ROOMS[Random.chances( STD_CHANCES )] );
	}

	protected static void addStandards( ArrayList<Room> rooms, int count ){
		for (int i = 0; i < count; i++) {
			StandardRoom s;
			do {
				s = randomStandard();
			} while (!s.setSizeCat( count - i ));
			i += s.sizeFactor() - 1;
			rooms.add( s );
		}
	}

	@Override
	protected ArrayList<Room> initRooms() {
		ArrayList<Room> rooms = new ArrayList<>();
		rooms.add( roomEntrance = new SanctuaryGateRoom() );
		rooms.add( roomExit = ExitRoom.createExit() );

		addStandards( rooms, standardRooms( false ) );

		ArrayList<Class<?>> specials = new ArrayList<>( Arrays.asList( SPECIALS ) );
		Random.shuffle( specials );
		int n = specialRooms( false );
		for (int i = 0; i < n && !specials.isEmpty(); i++){
			rooms.add( (Room) Reflection.newInstance( specials.remove( 0 ) ) );
		}
		return rooms;
	}

	@Override
	protected int standardRooms( boolean forceMax ) {
		if (forceMax) return 7;
		return 5 + Random.chances( new float[]{1, 3, 1} );
	}

	@Override
	protected int specialRooms( boolean forceMax ) {
		if (forceMax) return 3;
		return 2 + Random.chances( new float[]{2, 1} );
	}

	@Override
	protected Painter painter() {
		return new SanctuaryPainter()
				.setWater( 0.25f, 4 )
				.setGrass( 0.30f, 3 )
				.setTraps( nTraps(), trapClasses(), trapChances() );
	}

	@Override
	protected int nTraps() {
		return Random.NormalIntRange( 2, 4 );
	}

	@Override
	protected Class<?>[] trapClasses() {
		return new Class[]{ RockfallTrap.class, GuardianTrap.class, FlashingTrap.class, DisintegrationTrap.class,
				FrostTrap.class, StormTrap.class, CorrosionTrap.class, WeakeningTrap.class,
				SummoningTrap.class, WarpingTrap.class };
	}

	@Override
	protected float[] trapChances() {
		return new float[]{ 4, 4, 3, 3,
				2, 2, 2, 2,
				1, 1 };
	}

	@Override
	protected boolean build() {
		if (!super.build()) return false;
		//북쪽 계단은 성소 2층으로
		for (LevelTransition t : transitions){
			if (t.type == LevelTransition.Type.REGULAR_EXIT){
				t.destDepth = SanctuaryQuest.DEPTH;
				t.destBranch = SanctuaryBossLevel.BRANCH;
				t.destType = LevelTransition.Type.REGULAR_ENTRANCE;
			}
		}
		return true;
	}

	//성소에 들어온 뒤에는 수호자를 쓰러뜨리고 엑스칼리버를 손에 넣기 전까지 게이트가 닫혀 있음
	@Override
	public boolean activateTransition( com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero, LevelTransition transition ) {
		if (transition.type == LevelTransition.Type.REGULAR_ENTRANCE && !SanctuaryQuest.completed){
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w( Messages.get( SanctuaryLevel.class, "gate_sealed" ) );
			return false;
		}
		return super.activateTransition( hero, transition );
	}

	//1층 게이트가 아직 꺼져 있는지
	public static boolean gateDormant(){
		return Dungeon.level instanceof SanctuaryLevel && !SanctuaryQuest.completed;
	}

	//==== 몬스터 ====

	@Override
	public int mobLimit() {
		return 7 + Random.Int( 3 );
	}

	@Override
	public Mob createMob() {
		return randomGolem();
	}

	public static Mob randomGolem(){
		int r = Random.Int( 20 );
		if (r < 9) return new StoneSoldier();
		if (r < 14) return new StoneWarden();
		return new RuneSentinel();
	}

	//==== 아이템 (강화 주문서·힘의 물약 제외) ====

	public static Item loot( Generator.Category cat ){
		Item it;
		do {
			it = cat == null ? Generator.random() : Generator.random( cat );
		} while (it == null || it instanceof ScrollOfUpgrade || it instanceof PotionOfStrength);
		return it;
	}

	protected void dropAt( Item it, Heap.Type type ){
		int cell = randomDropCell();
		if (cell < 0 || it == null) return;
		if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
			map[cell] = Terrain.GRASS;
			losBlocking[cell] = false;
		}
		drop( it, cell ).type = type;
	}

	@Override
	protected void createItems() {
		int n = 5 + Random.chances( new float[]{3, 4, 2} );
		for (int i = 0; i < n; i++){
			Heap.Type type = Random.Int( 5 ) == 0 ? Heap.Type.CHEST
					: (Random.Int( 8 ) == 0 ? Heap.Type.SKELETON : Heap.Type.HEAP);
			dropAt( loot( null ), type );
		}
		dropAt( Generator.random( Generator.Category.FOOD ), Heap.Type.HEAP );
		//성소 고유 아이템: 룬 수정
		for (int i = 0; i < 2; i++){
			dropAt( new com.shatteredpixel.shatteredpixeldungeon.items.quest.RuneCrystal().quantity( Random.IntRange( 1, 2 ) ), Heap.Type.HEAP );
		}
		for (int i = 0; i < 2; i++){
			dropAt( new Gold().random(), Heap.Type.HEAP );
		}
		//특수 방이 남긴 열쇠 등
		for (Item item : itemsToSpawn){
			dropAt( item, Heap.Type.HEAP );
		}
		itemsToSpawn.clear();
	}

	//==== 그림 효과 ====

	@Override
	public Group addVisuals() {
		super.addVisuals();
		addSanctuaryVisuals( this, visuals );
		return visuals;
	}

	//물 위 빛 알갱이, 룬 벽의 희미한 빛, 게이트의 빛
	public static void addSanctuaryVisuals( Level level, Group group ){
		for (int i = 0; i < level.length(); i++){
			if (level.map[i] == Terrain.WATER && Random.Int( 4 ) == 0){
				Emitter e = new Emitter();
				e.pos( (i % level.width()) * DungeonTilemap.SIZE, (i / level.width()) * DungeonTilemap.SIZE,
						DungeonTilemap.SIZE, DungeonTilemap.SIZE );
				e.pour( RuneMote.FACTORY, Random.Float( 0.6f, 1.1f ) );
				group.add( e );
			} else if (level.map[i] == Terrain.WALL_DECO && i + level.width() < level.length()
					&& !com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.wallStitcheable( level.map[i + level.width()] )){
				Emitter e = new Emitter();
				PointF p = DungeonTilemap.tileCenterToWorld( i );
				e.pos( p.x - 3, p.y - 2, 6, 6 );
				e.pour( RuneMote.FACTORY, 1.4f );
				group.add( e );
			}
		}
		for (CustomTilemap t : level.customTiles){
			if (t instanceof Gate && !gateDormant()){
				addGateGlow( group, t.tileX + t.tileY * level.width() );
			}
		}
	}

	public static void addGateGlow( Group group, int cell ){
		Emitter e = new Emitter();
		PointF p = DungeonTilemap.tileCenterToWorld( cell );
		e.pos( p.x - 4, p.y - 12, 8, 14 );
		e.pour( RuneMote.FACTORY, 0.25f );
		group.add( e );
	}

	@Override
	public String tileName( int tile ) {
		return name( tile, super.tileName( tile ) );
	}

	@Override
	public String tileDesc( int tile ) {
		return desc( tile, super.tileDesc( tile ) );
	}

	static String name( int tile, String fallback ){
		switch (tile){
			case Terrain.WATER: return Messages.get( SanctuaryLevel.class, "water_name" );
			case Terrain.STATUE: case Terrain.STATUE_SP: return Messages.get( SanctuaryLevel.class, "statue_name" );
			case Terrain.BOOKSHELF: return Messages.get( SanctuaryLevel.class, "bookshelf_name" );
			case Terrain.GRASS: case Terrain.HIGH_GRASS: case Terrain.FURROWED_GRASS:
				return Messages.get( SanctuaryLevel.class, "grass_name" );
			case Terrain.REGION_DECO: case Terrain.REGION_DECO_ALT:
				return Messages.get( SanctuaryLevel.class, "region_deco_name" );
			case Terrain.WALL_DECO: return Messages.get( SanctuaryLevel.class, "wall_deco_name" );
			default: return fallback;
		}
	}

	static String desc( int tile, String fallback ){
		switch (tile){
			case Terrain.WATER: return Messages.get( SanctuaryLevel.class, "water_desc" );
			case Terrain.STATUE: case Terrain.STATUE_SP: return Messages.get( SanctuaryLevel.class, "statue_desc" );
			case Terrain.BOOKSHELF: return Messages.get( SanctuaryLevel.class, "bookshelf_desc" );
			case Terrain.GRASS: case Terrain.HIGH_GRASS: case Terrain.FURROWED_GRASS:
				return Messages.get( SanctuaryLevel.class, "grass_desc" );
			case Terrain.EMPTY_SP: return Messages.get( SanctuaryLevel.class, "floor_sp_desc" );
			case Terrain.EMPTY_DECO: return Messages.get( SanctuaryLevel.class, "deco_desc" );
			case Terrain.PEDESTAL: return Messages.get( SanctuaryLevel.class, "pedestal_desc" );
			case Terrain.EXIT: return Messages.get( SanctuaryLevel.class, "exit_desc" );
			case Terrain.ENTRANCE: case Terrain.ENTRANCE_SP: return Messages.get( SanctuaryLevel.class, "entrance_desc" );
			case Terrain.REGION_DECO: case Terrain.REGION_DECO_ALT:
				return Messages.get( SanctuaryLevel.class, "region_deco_desc" );
			case Terrain.WALL_DECO: return Messages.get( SanctuaryLevel.class, "wall_deco_desc" );
			default: return fallback;
		}
	}

	//==== 그림 타일 ====

	//가운데 3x3 룬 원
	public static void addRune( Level level, int center ){
		RuneCircle rune = new RuneCircle();
		rune.setRect( center % level.width() - 1, center / level.width() - 1, 3, 3 );
		level.customTiles.add( rune );
	}

	//고대 게이트 (아래 칸은 바닥 위, 위 칸 아치는 캐릭터보다 위)
	public static void addGate( Level level, int cell ){
		Gate g = new Gate();
		g.pos( cell % level.width(), cell / level.width() );
		level.customTiles.add( g );
		GateTop top = new GateTop();
		top.pos( cell % level.width(), cell / level.width() - 1 );
		level.customWalls.add( top );
	}

	public static class RuneCircle extends CustomTilemap {
		{
			texture = Assets.Environment.SANCTUARY_RUNE;
		}
		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( mapSimpleImage( 0, 0, 48 ), tileW );
			return v;
		}
		@Override
		public Image image( int tileX, int tileY ) {
			return null;
		}
	}

	//sanctuary_gate.png: 32x32, 왼쪽 열은 켜진 게이트, 오른쪽 열은 꺼진 게이트 (위 칸 아치 / 아래 칸)
	public static class Gate extends CustomTilemap {
		{
			texture = Assets.Environment.SANCTUARY_GATE;
		}
		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( new int[]{ gateDormant() ? 3 : 2 }, 1 );
			return v;
		}
		@Override
		public Image image( int tileX, int tileY ) {
			return new Image( texture, gateDormant() ? 16 : 0, 0, 16, 32 );
		}
		@Override
		public String name( int tileX, int tileY ) {
			return Messages.get( SanctuaryLevel.class, "gate_name" );
		}
		@Override
		public String desc( int tileX, int tileY ) {
			if (Dungeon.branch == 0) return Messages.get( SanctuaryLevel.class, "gate_desc_city" );
			return Messages.get( SanctuaryLevel.class, gateDormant() ? "gate_desc_sealed" : "gate_desc" );
		}
	}

	public static class GateTop extends CustomTilemap {
		{
			texture = Assets.Environment.SANCTUARY_GATE;
		}
		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( new int[]{ gateDormant() ? 1 : 0 }, 1 );
			return v;
		}
		@Override
		public Image image( int tileX, int tileY ) {
			return null;
		}
	}

	//옛 버전(손으로 그린 성소)의 저장 파일 호환용
	public static class Portal extends Gate {}
	public static class PortalTop extends GateTop {}
}
