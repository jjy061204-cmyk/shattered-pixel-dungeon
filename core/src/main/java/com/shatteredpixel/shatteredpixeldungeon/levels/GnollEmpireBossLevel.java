/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 15층: 놀 대족장의 원형 방책 투기장
 *
 *   # 바위벽   = 나무 방책(불에 탐)   T 왕좌 단   B 전투 깃발   G 증원 성문   f 모닥불   E 입구
 *   투기장 위쪽 막힌 통로 너머에 아래층 계단이 있고, 대족장을 쓰러뜨리면 열림
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollWarlord;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WarBanner;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollEmpireBossLevel extends Level {

	{
		color1 = 0x4b3324;
		color2 = 0xd98c3a;
	}

	private static final int W = 33;

	//'#' 바위, '=' 방책, '.' 바닥, ',' 짓밟힌 흙, '_' 왕좌 단, 'T' 왕좌(대족장 자리), 'B' 깃발,
	//'G' 증원 성문(골방), 'g' 방책의 성문 틈, 'f' 모닥불, 'S' 승리하면 열리는 통로(바위), 'P' 승리하면 열리는 방책,
	//'<' 입구, '>' 출구
	private static final String[] MAP = {
			"#################################",
			"############f.......f############",
			"############....>....############",
			"############..,...,..############",
			"############%.......%############",
			"###############SSS###############",
			"##===========##PPP##===========##",
			"##=...........__T__...........=##",
			"##=....,......_____......,....=##",
			"##=..B.....................B..=##",
			"#Gg.........,.......,.........gG#",
			"##=......f......,......f......=##",
			"##=...........................=##",
			"##=...,...%...........%...,...=##",
			"##=.,.......................,.=##",
			"##=......f.............f......=##",
			"#Gg.............,.............gG#",
			"##=..B........,...,........B..=##",
			"##=........,.........,........=##",
			"##=...........................=##",
			"##============#...#============##",
			"################.################",
			"################.################",
			"##############.....##############",
			"##############..<..##############",
			"##############.....##############",
			"#################################"
	};

	//투기장 안쪽 (방책 안)
	private static final int ARENA_L = 3, ARENA_R = 29, ARENA_T = 7, ARENA_B = 19;

	private int throne = -1;
	private int sealCell = -1;
	private final ArrayList<Integer> banners = new ArrayList<>();
	private final ArrayList<Integer> gateAlcoves = new ArrayList<>();
	private final ArrayList<Integer> victoryPath = new ArrayList<>();

	private boolean fightStarted = false;
	private boolean bossDefeated = false;

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_GNOLL_EMPIRE;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CAVES;
	}

	@Override
	public String entryText() {
		return bossDefeated ? null : Messages.get( this, "entry" );
	}

	@Override
	public void playLevelMusic() {
		if (locked){
			if (BossHealthBar.isBleeding()){
				Music.INSTANCE.play( Assets.Music.CAVES_BOSS_FINALE, true );
			} else {
				Music.INSTANCE.play( Assets.Music.CAVES_BOSS, true );
			}
		} else if (!bossDefeated){
			Music.INSTANCE.end();
		} else {
			Music.INSTANCE.playTracks( CavesLevel.CAVES_TRACK_LIST, CavesLevel.CAVES_TRACK_CHANCES, false );
		}
	}

	@Override
	protected boolean build() {
		setSize( W, MAP.length );

		banners.clear();
		gateAlcoves.clear();
		victoryPath.clear();

		int entrance = -1, exit = -1;
		for (int y = 0; y < MAP.length; y++){
			String row = MAP[y];
			for (int x = 0; x < W; x++){
				int cell = x + y * W;
				char ch = row.charAt( x );
				int t;
				switch (ch){
					case '#': t = Terrain.WALL; break;
					case '=': t = Terrain.BARRICADE; break;
					case ',': t = Terrain.EMPTY_DECO; break;
					case '_': t = Terrain.EMPTY_SP; break;
					case 'T': t = Terrain.EMPTY_SP; throne = cell; break;
					case 'B': t = Terrain.EMPTY; banners.add( cell ); break;
					case 'G': t = Terrain.EMPTY_DECO; gateAlcoves.add( cell ); break;
					case 'g': t = Terrain.EMPTY; break;
					case 'f': t = Terrain.STATUE_SP; break;
					case '%': t = Terrain.REGION_DECO; break;
					case 'S': t = Terrain.WALL; victoryPath.add( cell ); break;
					case 'P': t = Terrain.BARRICADE; victoryPath.add( cell ); break;
					case '<': t = Terrain.ENTRANCE; entrance = cell; break;
					case '>': t = Terrain.EXIT; exit = cell; break;
					case '.': default: t = Terrain.EMPTY; break;
				}
				map[cell] = t;
			}
		}

		//입구 바로 위 통로 칸: 싸움이 시작되면 바위로 막힘
		sealCell = 16 + 21 * W;

		transitions.add( new LevelTransition( this, entrance, LevelTransition.Type.REGULAR_ENTRANCE ) );
		transitions.add( new LevelTransition( this, exit, LevelTransition.Type.REGULAR_EXIT ) );

		feeling = Feeling.NONE;
		return true;
	}

	public ArrayList<Integer> gateCells(){
		return gateAlcoves;
	}

	private boolean inArena( int cell ){
		int x = cell % width(), y = cell / width();
		return x >= ARENA_L && x <= ARENA_R && y >= ARENA_T && y <= ARENA_B;
	}

	// ── 몹·아이템 ──

	@Override
	protected void createMobs() {
		GnollWarlord boss = new GnollWarlord();
		boss.pos = throne;
		mobs.add( boss );
		for (int b : banners){
			WarBanner banner = new WarBanner();
			banner.pos = b;
			mobs.add( banner );
		}
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createItems() {
		Random.pushGenerator( Random.Long() );
			ArrayList<Item> bonesItems = Bones.get();
			if (bonesItems != null){
				int pos;
				do {
					pos = randomRespawnCell( null );
				} while (pos == entrance());
				if (pos != -1){
					for (Item i : bonesItems){
						drop( i, pos ).setHauntedIfCursed().type = Heap.Type.REMAINS;
					}
				}
			}
		Random.popGenerator();
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int i : PathFinder.NEIGHBOURS8){
			int cell = entrance() + i;
			if (passable[cell] && Actor.findChar( cell ) == null){
				candidates.add( cell );
			}
		}
		return candidates.isEmpty() ? -1 : Random.element( candidates );
	}

	// ── 싸움 ──

	@Override
	public void occupyCell( Char ch ) {
		super.occupyCell( ch );
		if (ch instanceof Hero && !bossDefeated && inArena( ch.pos )){
			if (!fightStarted){
				for (Mob m : mobs){
					if (m instanceof GnollWarlord){
						((GnollWarlord) m).awaken();
						break;
					}
				}
			}
			if (fightStarted && !locked){
				seal();
			}
		}
	}

	//대족장이 깨어남 (영웅이 투기장에 들어섰거나, 밖에서 대족장을 공격함)
	public void onFightStart(){
		if (fightStarted) return;
		fightStarted = true;
		if (inArena( Dungeon.hero.pos )){
			seal();
		}
		playLevelMusicLater();
	}

	@Override
	public void seal() {
		if (locked) return;
		super.seal();
		Statistics.qualifiedForBossChallengeBadge = true;

		//입구 통로를 바위로 막음
		if (Actor.findChar( sealCell ) == null){
			Heap heap = heaps.get( sealCell );
			if (heap != null){
				drop( heap.pickUp(), sealCell - width() ).seen = heap.seen;
			}
			set( sealCell, Terrain.WALL );
			GameScene.updateMap( sealCell );
			CellEmitter.get( sealCell ).start( Speck.factory( Speck.ROCK ), 0.07f, 10 );
			PixelScene.shake( 3, 0.7f );
			Sample.INSTANCE.play( Assets.Sounds.ROCKS );
			Dungeon.observe();
		}
		playLevelMusicLater();
	}

	@Override
	public void unseal() {
		super.unseal();
		if (map[sealCell] == Terrain.WALL){
			set( sealCell, Terrain.EMPTY );
			GameScene.updateMap( sealCell );
		}
		Dungeon.observe();
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				Music.INSTANCE.fadeOut( 5f, new Callback() {
					@Override
					public void call() {
						Music.INSTANCE.end();
					}
				} );
			}
		} );
	}

	private void playLevelMusicLater(){
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				playLevelMusic();
			}
		} );
	}

	//방책 전부에 불을 붙임 (최후의 함성)
	public void ignitePalisades(){
		for (int i = 0; i < length(); i++){
			if (map[i] == Terrain.BARRICADE && !victoryPath.contains( i )){
				GameScene.add( Blob.seed( i, 4, Fire.class ) );
			}
		}
	}

	//좁혀 오는 불길: 방책 안쪽 가장자리에서 ring칸까지
	public void spreadFlames( int ring ){
		for (int y = ARENA_T; y <= ARENA_B; y++){
			for (int x = ARENA_L; x <= ARENA_R; x++){
				int cell = x + y * width();
				int d = Math.min( Math.min( x - ARENA_L, ARENA_R - x ), Math.min( y - ARENA_T, ARENA_B - y ) );
				if (d < ring && !solid[cell]){
					GameScene.add( Blob.seed( cell, 1, WarlordFlames.class ) );
				}
			}
		}
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
		PixelScene.shake( 1, 0.5f );
	}

	//다음 불길이 덮칠 자리에 불씨를 튀겨 한 턴 앞서 알림
	public void warnFlames( int ring ){
		for (int y = ARENA_T; y <= ARENA_B; y++){
			for (int x = ARENA_L; x <= ARENA_R; x++){
				int cell = x + y * width();
				int d = Math.min( Math.min( x - ARENA_L, ARENA_R - x ), Math.min( y - ARENA_T, ARENA_B - y ) );
				if (d == ring - 1 && !solid[cell] && heroFOV[cell]){
					CellEmitter.get( cell ).burst( FlameParticle.FACTORY, 2 );
					if (Random.Int( 3 ) == 0) GameScene.targetedCell( cell, 1f );
				}
			}
		}
	}

	//투창이 꽂힌 자국: 바닥에 잠깐 남았다가 사라짐
	public void stuckJavelin( int cell ){
		if (visuals == null || cell < 0 || cell >= length() || solid[cell] || !heroFOV[cell]) return;
		int S = com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.SIZE;
		final com.watabou.noosa.Image img = arenaImage( Random.Int( 2 ) == 0 ? 128 : 144, 128, 16, 16,
				(cell % width()) * S, (cell / width()) * S );
		visuals.add( img );
		visuals.add( new com.watabou.noosa.tweeners.AlphaTweener( img, 0, 4f ){
			@Override
			protected void onComplete() {
				img.killAndErase();
				killAndErase();
			}
		} );
		CellEmitter.get( cell ).burst( Speck.factory( Speck.DUST ), 3 );
	}

	public void onBossDefeated(){
		if (bossDefeated) return;
		bossDefeated = true;

		Blob flames = blobs.get( WarlordFlames.class );
		if (flames != null) flames.fullyClear();
		Blob fire = blobs.get( Fire.class );
		if (fire != null) fire.fullyClear();

		unseal();
		breakThrone();
		PixelScene.shake( 3, 1.2f );
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		GLog.h( Messages.get( this, "victory" ) );

		//왕좌 뒤 큰 문이 한 줄씩 무너지며 아래층 계단 방이 드러남
		Actor.add( new GateCollapse() );
	}

	//방책 성문 → 바위벽 순서로 한 턴에 한 줄씩 무너뜨림
	private void collapseRow( int row ){
		for (int c : victoryPath){
			if (c / width() != row) continue;
			set( c, Terrain.EMPTY_DECO );
			GameScene.updateMap( c );
			CellEmitter.get( c ).start( Speck.factory( Speck.ROCK ), 0.06f, 8 );
			CellEmitter.get( c ).burst( Speck.factory( Speck.DUST ), 6 );
		}
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		PixelScene.shake( 2, 0.6f );
		Dungeon.observe();
	}

	public static class GateCollapse extends Actor {
		{
			actPriority = BUFF_PRIO;
		}
		private int step = 0;

		@Override
		protected boolean act() {
			if (!(Dungeon.level instanceof GnollEmpireBossLevel)){
				Actor.remove( this );
				return true;
			}
			GnollEmpireBossLevel l = (GnollEmpireBossLevel) Dungeon.level;
			int lowest = -1, highest = -1;
			for (int c : l.victoryPath){
				int r = c / l.width();
				if (lowest == -1 || r > lowest) lowest = r;
				if (highest == -1 || r < highest) highest = r;
			}
			int row = lowest - step;
			if (step == 0) GLog.w( Messages.get( GnollEmpireBossLevel.class, "gate_cracks" ) );
			if (row >= highest){
				l.collapseRow( row );
				step++;
				spend( 1f );
				return true;
			}
			GLog.p( Messages.get( GnollEmpireBossLevel.class, "path_open" ) );
			Actor.remove( this );
			return true;
		}
	}

	@Override
	public boolean invalidHeroPos( int tile ) {
		//승리 전에는 출구 쪽 방에 놓이지 않음
		if (!bossDefeated && tile / width() <= 4) return true;
		return super.invalidHeroPos( tile );
	}

	@Override
	public void destroy( int pos ) {
		boolean palisade = map[pos] == Terrain.BARRICADE;
		if (palisade && victoryPath.contains( pos ) && !bossDefeated) return; //출구 쪽 방책은 승리 전엔 타지 않음
		super.destroy( pos );
		if (palisade){
			GnollEmpire.palisadeCollapsed( this, pos );
			for (int anchor : new ArrayList<>( gatePosts.keySet() )){
				if (anchor == pos || anchor - width() == pos){
					com.watabou.noosa.Image post = gatePosts.remove( anchor );
					if (post != null) post.killAndErase();
					CellEmitter.get( anchor ).burst( Speck.factory( Speck.DUST ), 6 );
				}
			}
		}
	}

	// ── 투기장 장식 그림 (gnoll_arena.png) ──
	//  왕좌 단: 아래층(5칸x4줄, 6~9행)은 몹 아래, 위층(5행)은 벽 위에 그려 왕좌가 벽 너머로 솟아 보이게
	//  가운데 전쟁 문장, 바닥 장식, 성문 해골 기둥, 입구 아치

	private static final int THRONE_X = 13, THRONE_Y = 6;
	private static final int[][] DECALS = {
			{6,8,0}, {25,12,1}, {8,17,2}, {27,14,3}, {21,18,4}, {5,12,5}, {12,16,6}, {20,9,0}, {10,19,3}, {24,8,6}
	};
	//성문 틈 (x, y) : 해골 기둥은 그 위 방책 두 칸에 세움
	private static final int[][] GATE_GAPS = { {2,10}, {2,16}, {30,10}, {30,16} };

	private com.watabou.noosa.Image throneLow, throneUp;
	private final java.util.HashMap<Integer, com.watabou.noosa.Image> gatePosts = new java.util.HashMap<>();

	private static com.watabou.noosa.Image arenaImage( int tx, int ty, int w, int h, float x, float y ){
		com.watabou.noosa.Image img = new com.watabou.noosa.Image( Assets.Environment.GNOLL_ARENA, tx, ty, w, h );
		img.x = x;
		img.y = y;
		return img;
	}

	@Override
	public Group addVisuals() {
		super.addVisuals();
		int S = com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.SIZE;

		//바닥에 칠한 전쟁 문장과 흩어진 뼈·투창·핏자국
		visuals.add( arenaImage( 0, 80, 112, 80, 13*S, 11*S ) );
		for (int[] d : DECALS){
			visuals.add( arenaImage( 112 + 16*d[2], 128, 16, 16, d[0]*S, d[1]*S ) );
		}

		//왕좌 단 (쓰러뜨린 뒤에는 부서진 왕좌)
		throneLow = arenaImage( bossDefeated ? 112 : 0, 0, 112, 64, THRONE_X*S, THRONE_Y*S );
		visuals.add( throneLow );

		//성문 해골 기둥: 그 자리 방책이 타 버렸으면 세우지 않음
		gatePosts.clear();
		for (int[] g : GATE_GAPS){
			int anchor = g[0] + (g[1]-1)*width();
			if (map[anchor] != Terrain.BARRICADE) continue;
			com.watabou.noosa.Image post = arenaImage( g[0] < W/2 ? 112 : 128, 80, 16, 32, g[0]*S, (g[1]-2)*S );
			visuals.add( post );
			gatePosts.put( anchor, post );
		}

		GnollEmpire.addFireVisuals( this, visuals );
		return visuals;
	}

	@Override
	public Group addWallVisuals() {
		super.addWallVisuals();
		int S = com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.SIZE;
		//왕좌 위층: 해골 왕관·창끝·깃대 꼭대기가 뒤 바위벽 위로 솟음
		throneUp = arenaImage( bossDefeated ? 112 : 0, 64, 112, 16, THRONE_X*S, (THRONE_Y-1)*S );
		wallVisuals.add( throneUp );
		//입구 아치 (지나갈 때 머리 위로 지나가게 벽 위 층)
		wallVisuals.add( arenaImage( 144, 80, 80, 40, 14*S, 21*S - 40 ) );
		return wallVisuals;
	}

	//대족장이 쓰러지면 왕좌가 무너짐
	private void breakThrone(){
		if (throneLow != null) throneLow.frame( 112, 0, 112, 64 );
		if (throneUp != null)  throneUp.frame( 112, 64, 112, 16 );
		for (int x = THRONE_X + 1; x <= THRONE_X + 5; x++){
			int c = x + (THRONE_Y + 1) * width();
			CellEmitter.get( c ).burst( Speck.factory( Speck.ROCK ), 4 );
			CellEmitter.get( c - width() ).burst( Speck.factory( Speck.DUST ), 4 );
		}
	}

	@Override
	public String tileName( int tile ) {
		if (tile == Terrain.EMPTY_SP) return Messages.get( this, "throne_name" );
		String n = GnollEmpire.tileName( tile );
		return n != null ? n : super.tileName( tile );
	}

	@Override
	public String tileDesc( int tile ) {
		if (tile == Terrain.EMPTY_SP) return Messages.get( this, "throne_desc" );
		if (tile == Terrain.ENTRANCE) return Messages.get( CavesLevel.class, "entrance_desc" );
		if (tile == Terrain.EXIT) return Messages.get( CavesLevel.class, "exit_desc" );
		String d = GnollEmpire.tileDesc( tile );
		return d != null ? d : super.tileDesc( tile );
	}

	// ── 저장 ──

	private static final String THRONE = "throne";
	private static final String SEAL = "seal_cell";
	private static final String BANNERS = "banners";
	private static final String GATES = "gate_alcoves";
	private static final String PATH = "victory_path";
	private static final String FIGHT = "fight_started";
	private static final String DEFEATED = "boss_defeated";

	private static int[] toArray( ArrayList<Integer> list ){
		int[] a = new int[list.size()];
		for (int i = 0; i < a.length; i++) a[i] = list.get( i );
		return a;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( THRONE, throne );
		bundle.put( SEAL, sealCell );
		bundle.put( BANNERS, toArray( banners ) );
		bundle.put( GATES, toArray( gateAlcoves ) );
		bundle.put( PATH, toArray( victoryPath ) );
		bundle.put( FIGHT, fightStarted );
		bundle.put( DEFEATED, bossDefeated );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		throne = bundle.getInt( THRONE );
		sealCell = bundle.getInt( SEAL );
		banners.clear();
		gateAlcoves.clear();
		victoryPath.clear();
		for (int c : bundle.getIntArray( BANNERS )) banners.add( c );
		for (int c : bundle.getIntArray( GATES )) gateAlcoves.add( c );
		for (int c : bundle.getIntArray( PATH )) victoryPath.add( c );
		fightStarted = bundle.getBoolean( FIGHT );
		bossDefeated = bundle.getBoolean( DEFEATED );
		if (bossDefeated){
			for (int c : victoryPath){
				if (map[c] != Terrain.EMPTY_DECO) map[c] = Terrain.EMPTY_DECO;
			}
			buildFlagMaps();
		}
	}

	//최후의 함성 때 투기장 가장자리부터 좁혀 오는 불길: 그 위에 선 이(대족장 제외)는 불이 붙음
	public static class WarlordFlames extends Blob {

		@Override
		protected void evolve() {
			for (int cell = 0; cell < Dungeon.level.length(); cell++){
				if (!Dungeon.level.insideMap( cell )) continue;
				off[cell] = cur[cell];
				volume += off[cell];
				if (off[cell] > 0){
					Char ch = Actor.findChar( cell );
					if (ch != null && !(ch instanceof GnollWarlord) && !(ch instanceof WarBanner) && !ch.isImmune( Fire.class )){
						Buff.affect( ch, Burning.class ).reignite( ch );
						if (ch == Dungeon.hero){
							Statistics.qualifiedForBossChallengeBadge = false;
							Statistics.bossScores[2] -= 20;
						}
					}
					if (Dungeon.level.flamable[cell]){
						Dungeon.level.destroy( cell );
						GameScene.updateMap( cell );
					}
				}
			}
		}

		@Override
		public void use( BlobEmitter emitter ) {
			super.use( emitter );
			emitter.pour( FlameParticle.FACTORY, 0.05f );
		}

		@Override
		public String tileDesc() {
			return Messages.get( GnollEmpireBossLevel.class, "flames_desc" );
		}
	}
}
