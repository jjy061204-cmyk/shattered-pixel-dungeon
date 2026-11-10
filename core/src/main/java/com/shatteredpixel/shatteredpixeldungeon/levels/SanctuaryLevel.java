/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 1층 (외곽 회랑)
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
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

//20층 임프의 퀘스트로 들어오는 이차원의 성소 1층.
//남쪽 도착실(차원 문) → 대전당(공허 웅덩이) → 북쪽 관문실(2층 계단)
//서쪽 성유물실(받침대), 동쪽 공허 정원(상자), 북서·북동 서고, 남서·남동 무너진 제단
//석상 골렘들이 지키며, 강화 주문서·힘의 물약을 뺀 아이템이 곳곳에 놓여 있음
public class SanctuaryLevel extends Level {

	public static final int BRANCH = 7;

	{
		color1 = 0x6a4bd8;
		color2 = 0x88e8ff;
	}

	private static final String[] MAP = {
		"#########################################",
		"#########################################",
		"#############...............#############",
		"##BBBBBBBB###.S...__>__...S.###BBBBBBBB##",
		"##........###....._____.....###........##",
		"##........+...S..._____...S...+........##",
		"##........###...............###........##",
		"##........###.S...........S.###........##",
		"##........###...............###........##",
		"##........#########...#########........##",
		"###################.+.###################",
		"##########...,.,.,.,.,.,.,.,...##########",
		"##########..S...............S..##########",
		"#........#.....................#````````#",
		"#........#.........._..........#``````C`#",
		"#.P____P.#..S..._________...S..#``~~~~``#",
		"#..____..#......_~~~~~~~_......#``~~~~``#",
		"#.P____P.#......_~~~~~~~_......#``~~~~``#",
		"#..____..+..S..._~~~~~~~_...S..+````````#",
		"#.P____P.#......_~~~~~~~_......#````````#",
		"#..____..#......_~~~~~~~_......#`hhh````#",
		"#.P____P.#..S..._________...S..#`hhh````#",
		"#........#.........._..........#`hhh``C`#",
		"#........#.....................#````````#",
		"##########..S...............S..##########",
		"##########.....................##########",
		"###########+.######...######.+###########",
		"###########..######...######..###########",
		"###,,,,,,,,,,######...######,,,,,,,,,,###",
		"###,,,,,,,,,,##S.........S##,,,,,,,,,,###",
		"###,,......,,##...........##,,......,,###",
		"###,,..S...,,##..._____...##,,...S..,,###",
		"###,,..._..,,....._____...##,,.._...,,###",
		"###,,......,,.....__O__.....,,......,,###",
		"###,,,,,,,,,,##..._____...##,,,,,,,,,,###",
		"###,,,,,,,,,,##..._____...##,,,,,,,,,,###",
		"###############S.........S###############",
		"#########################################",
		"#########################################"
	};

	private int portal = -1, exitCell = -1;
	private final ArrayList<Integer> pedestals = new ArrayList<>();
	private final ArrayList<Integer> chests = new ArrayList<>();

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
		Music.INSTANCE.playTracks( HallsLevel.HALLS_TRACK_LIST, HallsLevel.HALLS_TRACK_CHANCES, false );
	}

	@Override
	protected boolean build() {
		parse( this, MAP, pedestals, chests );
		for (int i = 0; i < length(); i++){
			char c = MAP[i / width()].charAt( i % width() );
			if (c == 'O') portal = i;
			if (c == '>') exitCell = i;
		}

		//차원 문: 20층 입구로 돌아감
		transitions.add( new LevelTransition( this, portal, LevelTransition.Type.REGULAR_ENTRANCE,
				SanctuaryQuest.DEPTH, 0, LevelTransition.Type.REGULAR_ENTRANCE ) );
		//북쪽 계단: 2층 수호자의 방
		transitions.add( new LevelTransition( this, exitCell, LevelTransition.Type.REGULAR_EXIT,
				SanctuaryQuest.DEPTH, SanctuaryBossLevel.BRANCH, LevelTransition.Type.REGULAR_ENTRANCE ) );

		addRune( this, portal );
		addPortal( this, portal );

		feeling = Feeling.NONE;
		return true;
	}

	//글자 지도 → 지형 (두 층이 같이 씀)
	static void parse( Level level, String[] map, ArrayList<Integer> pedestals, ArrayList<Integer> chests ){
		level.setSize( map[0].length(), map.length );
		for (int y = 0; y < map.length; y++){
			for (int x = 0; x < map[y].length(); x++){
				int cell = x + y * level.width();
				int t;
				switch (map[y].charAt( x )){
					case '#': default: t = Terrain.WALL; break;
					case '.': t = Terrain.EMPTY; break;
					case ',': t = Terrain.EMPTY_DECO; break;
					case '_': case 'O': case 'o': case 'T': t = Terrain.EMPTY_SP; break;
					case 'C': t = Terrain.EMPTY_SP; if (chests != null) chests.add( cell ); break;
					case '~': t = Terrain.WATER; break;
					case 'S': t = Terrain.STATUE; break;
					case 'P': t = Terrain.PEDESTAL; if (pedestals != null) pedestals.add( cell ); break;
					case '+': t = Terrain.DOOR; break;
					case '`': t = Terrain.GRASS; break;
					case 'h': t = Terrain.HIGH_GRASS; break;
					case 'B': t = Terrain.BOOKSHELF; break;
					case '>': t = Terrain.EXIT; break;
					case '<': t = Terrain.ENTRANCE; break;
				}
				level.map[cell] = t;
			}
		}
	}

	//가운데 3x3 룬 원 그림
	static void addRune( Level level, int center ){
		RuneCircle rune = new RuneCircle();
		rune.setRect( center % level.width() - 1, center / level.width() - 1, 3, 3 );
		level.customTiles.add( rune );
	}

	//세로로 찢어진 차원 문 그림 (아래 칸은 바닥 위, 위 칸은 캐릭터보다 위)
	static void addPortal( Level level, int cell ){
		Portal p = new Portal();
		p.pos( cell % level.width(), cell / level.width() );
		level.customTiles.add( p );
		PortalTop top = new PortalTop();
		top.pos( cell % level.width(), cell / level.width() - 1 );
		level.customWalls.add( top );
	}

	//==== 몬스터 ====

	@Override
	public int mobLimit() {
		return 9;
	}

	@Override
	public Mob createMob() {
		int r = Random.Int( 20 );
		if (r < 9) return new StoneSoldier();
		if (r < 14) return new StoneWarden();
		return new RuneSentinel();
	}

	//도착실 안이나 차원 문 가까이에는 나타나지 않음
	private boolean spawnable( int cell ){
		if (!passable[cell] || solid[cell] || Actor.findChar( cell ) != null) return false;
		if (portal >= 0 && distance( cell, portal ) <= 6) return false;
		return true;
	}

	@Override
	protected void createMobs() {
		int n = mobLimit();
		int tries = 0;
		while (n > 0 && tries++ < 2000){
			int cell = Random.Int( length() );
			if (!spawnable( cell )) continue;
			Mob m = createMob();
			m.pos = cell;
			mobs.add( m );
			n--;
		}
	}

	@Override
	public int randomRespawnCell( com.shatteredpixel.shatteredpixeldungeon.actors.Char ch ) {
		for (int tries = 0; tries < 300; tries++){
			int cell = Random.Int( length() );
			if (spawnable( cell ) && !heroFOV[cell]) return cell;
		}
		return -1;
	}

	//==== 아이템 (강화 주문서·힘의 물약 제외) ====

	public static Item loot( Generator.Category cat ){
		Item it;
		do {
			it = cat == null ? Generator.random() : Generator.random( cat );
		} while (it == null || it instanceof ScrollOfUpgrade || it instanceof PotionOfStrength);
		return it;
	}

	private int freeCell( int x0, int y0, int x1, int y1 ){
		for (int t = 0; t < 200; t++){
			int cell = Random.IntRange( x0, x1 ) + Random.IntRange( y0, y1 ) * width();
			if (passable[cell] && map[cell] != Terrain.PEDESTAL && heaps.get( cell ) == null) return cell;
		}
		return -1;
	}

	private void place( Item it, int cell ){
		if (it != null && cell >= 0) drop( it, cell );
	}

	@Override
	protected void createItems() {
		//성유물실 받침대 8개 중 4개에 귀한 물건
		ArrayList<Integer> ped = new ArrayList<>( pedestals );
		Generator.Category[] relic = { Generator.Category.WAND, Generator.Category.RING,
				Generator.Category.ARTIFACT, Generator.Category.WEAPON, Generator.Category.ARMOR };
		for (int i = 0; i < 4 && !ped.isEmpty(); i++){
			int cell = ped.remove( Random.Int( ped.size() ) );
			place( loot( relic[Random.Int( relic.length )] ), cell );
		}
		//공허 정원의 상자 2개
		for (int c : chests){
			drop( loot( null ), c ).type = Heap.Type.CHEST;
		}
		//서고 두 곳
		place( loot( Generator.Category.SCROLL ), freeCell( 2, 4, 9, 9 ) );
		place( loot( Generator.Category.SCROLL ), freeCell( 31, 4, 38, 9 ) );
		//무너진 제단 두 곳
		place( loot( Generator.Category.POTION ), freeCell( 5, 30, 10, 33 ) );
		place( loot( Generator.Category.POTION ), freeCell( 30, 30, 35, 33 ) );
		//대전당과 관문실 곳곳
		for (int i = 0; i < 6; i++){
			place( loot( null ), i < 4 ? freeCell( 10, 11, 30, 25 ) : freeCell( 13, 2, 27, 8 ) );
		}
		for (int i = 0; i < 3; i++){
			place( new Gold().random(), freeCell( 10, 11, 30, 25 ) );
		}
	}

	//==== 그림 효과 ====

	@Override
	public Group addVisuals() {
		super.addVisuals();
		addVoidMotes( this, visuals );
		return visuals;
	}

	static void addVoidMotes( Level level, Group group ){
		for (int i = 0; i < level.length(); i++){
			if (level.map[i] == Terrain.WATER && Random.Int( 3 ) == 0){
				Emitter e = new Emitter();
				e.pos( (i % level.width()) * DungeonTilemap.SIZE, (i / level.width()) * DungeonTilemap.SIZE,
						DungeonTilemap.SIZE, DungeonTilemap.SIZE );
				e.pour( RuneMote.FACTORY, Random.Float( 0.5f, 0.9f ) );
				group.add( e );
			}
		}
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
			case Terrain.GRASS: case Terrain.HIGH_GRASS: return Messages.get( SanctuaryLevel.class, "grass_name" );
			default: return fallback;
		}
	}

	static String desc( int tile, String fallback ){
		switch (tile){
			case Terrain.WATER: return Messages.get( SanctuaryLevel.class, "water_desc" );
			case Terrain.STATUE: case Terrain.STATUE_SP: return Messages.get( SanctuaryLevel.class, "statue_desc" );
			case Terrain.BOOKSHELF: return Messages.get( SanctuaryLevel.class, "bookshelf_desc" );
			case Terrain.GRASS: case Terrain.HIGH_GRASS: return Messages.get( SanctuaryLevel.class, "grass_desc" );
			case Terrain.EMPTY_SP: return Messages.get( SanctuaryLevel.class, "floor_sp_desc" );
			case Terrain.EMPTY_DECO: return Messages.get( SanctuaryLevel.class, "deco_desc" );
			case Terrain.PEDESTAL: return Messages.get( SanctuaryLevel.class, "pedestal_desc" );
			case Terrain.EXIT: return Messages.get( SanctuaryLevel.class, "exit_desc" );
			case Terrain.ENTRANCE: return Messages.get( SanctuaryLevel.class, "entrance_desc" );
			default: return fallback;
		}
	}

	//==== 그림 타일 ====

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

	public static class Portal extends CustomTilemap {
		{
			texture = Assets.Environment.DIMENSION_RIFT;
		}
		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( new int[]{1}, 1 );
			return v;
		}
		@Override
		public Image image( int tileX, int tileY ) {
			return new Image( texture, 0, 0, 16, 32 );
		}
		@Override
		public String name( int tileX, int tileY ) {
			return Messages.get( SanctuaryLevel.class, "portal_name" );
		}
		@Override
		public String desc( int tileX, int tileY ) {
			return Messages.get( SanctuaryLevel.class, "portal_desc" );
		}
	}

	public static class PortalTop extends CustomTilemap {
		{
			texture = Assets.Environment.DIMENSION_RIFT;
		}
		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( new int[]{0}, 1 );
			return v;
		}
		@Override
		public Image image( int tileX, int tileY ) {
			return null;
		}
	}

	private static final String PORTAL = "portal";
	private static final String EXIT_CELL = "exit_cell";
	private static final String PEDESTALS = "pedestals";
	private static final String CHESTS = "chests";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( PORTAL, portal );
		bundle.put( EXIT_CELL, exitCell );
		int[] p = new int[pedestals.size()];
		for (int i = 0; i < p.length; i++) p[i] = pedestals.get( i );
		bundle.put( PEDESTALS, p );
		int[] c = new int[chests.size()];
		for (int i = 0; i < c.length; i++) c[i] = chests.get( i );
		bundle.put( CHESTS, c );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		portal = bundle.getInt( PORTAL );
		exitCell = bundle.getInt( EXIT_CELL );
		pedestals.clear();
		if (bundle.contains( PEDESTALS )) for (int i : bundle.getIntArray( PEDESTALS )) pedestals.add( i );
		chests.clear();
		if (bundle.contains( CHESTS )) for (int i : bundle.getIntArray( CHESTS )) chests.add( i );
	}
}
