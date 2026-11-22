/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 보스 층 (15층): 흡혈 군주의 옥좌실
 *  - 달빛 창 세 개 아래 세로 줄은 늘 밝음 (관 제단 둘레는 군주의 어둠이 감싸 빛이 닿지 않음)
 *  - 네 귀퉁이의 화로는 꺼져 있고, 영웅이 켤 수 있음
 *  - 가운데 관 제단을 핏물이 둘러쌈. 군주를 쓰러뜨리면 관이 부서지며 아래로 내려가는 계단이 드러남
 *  - 핏빛 달(3단계) 동안 달빛 창이 붉게 물들고 빛을 잃음
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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CastleLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.VampireLord;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class VampireCastleBossLevel extends Level {

	{
		color1 = 0x3a1a22;
		color2 = 0xc0303c;
	}

	private static final int W = 23;

	//'#' 벽, 'w' 달빛 창이 난 벽, 'L' 화로, 'T' 옥좌, '_' 진홍 카펫, 'C' 관 제단, '~' 핏물, 'P' 군주의 석상, '<' 입구
	private static final String[] MAP = {
			"#######################",
			"#####w#####w#####w#####",
			"#L.......__T__.......L#",
			"#..P.....~~~~~.....P..#",
			"#.........~~~.........#",
			"#..P.......C.......P..#",
			"#.........~~~.........#",
			"#..P.....~~~~~.....P..#",
			"#L...................L#",
			"##########...##########",
			"##########...##########",
			"#########.....#########",
			"#########..<..#########",
			"#########.....#########",
			"#######################"
	};

	private static final int[] WINDOWS = { 5, 11, 17 };
	private static final int ARENA_T = 2, ARENA_B = 8;
	private static final int SEAL_ROW = 9;

	private int throne = -1;
	private int coffin = -1;
	private final ArrayList<Integer> braziers = new ArrayList<>();

	private boolean fightStarted = false;
	private boolean bossDefeated = false;
	private boolean bloodMoon = false;

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_VAMPIRE_CASTLE;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_BLOOD;
	}

	@Override
	public int waterTexScale() {
		return 2;
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
		braziers.clear();

		int entrance = -1;
		for (int y = 0; y < MAP.length; y++){
			String row = MAP[y];
			for (int x = 0; x < W; x++){
				int cell = x + y * W;
				int t;
				switch (row.charAt( x )){
					case '#': case 'w': t = Terrain.WALL; break;
					case 'L': t = Terrain.EMPTY_SP; braziers.add( cell ); break;
					case 'T': t = Terrain.EMPTY_SP; throne = cell; break;
					case 'C': t = Terrain.EMPTY_SP; coffin = cell; break;
					case '~': t = Terrain.WATER; break;
					case 'P': t = Terrain.STATUE_SP; break;
					case '_': t = Terrain.EMPTY_SP; break;
					case '<': t = Terrain.ENTRANCE; entrance = cell; break;
					case '.': default: t = Terrain.EMPTY; break;
				}
				map[cell] = t;
			}
		}

		transitions.add( new LevelTransition( this, entrance, LevelTransition.Type.REGULAR_ENTRANCE ) );
		feeling = Feeling.NONE;
		return true;
	}

	public int coffinCell(){
		return coffin;
	}

	public boolean inArena( int cell ){
		int y = cell / width();
		return y >= ARENA_T && y <= ARENA_B;
	}

	//달빛 창 아래 세로 줄 (핏빛 달이 뜨면 빛을 잃음, 관 제단 둘레는 늘 어두움)
	public boolean moonLit( int cell ){
		if (bloodMoon) return false;
		int x = cell % width(), y = cell / width();
		if (y < ARENA_T || y > ARENA_B) return false;
		boolean col = false;
		for (int wx : WINDOWS) if (wx == x) col = true;
		return col && distance( cell, coffin ) > 1;
	}

	// ── 몹 ──

	@Override
	protected void createMobs() {
		VampireLord boss = new VampireLord();
		boss.pos = throne;
		mobs.add( boss );
		for (int b : braziers){
			CastleLight l = CastleLight.brazier( false );
			l.pos = b;
			mobs.add( l );
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

	private VampireLord lord(){
		for (Mob m : mobs){
			if (m instanceof VampireLord) return (VampireLord) m;
		}
		return null;
	}

	// ── 싸움 ──

	@Override
	public void occupyCell( Char ch ) {
		super.occupyCell( ch );
		if (ch instanceof Hero && !bossDefeated && inArena( ch.pos )){
			if (!fightStarted){
				VampireLord l = lord();
				if (l != null) l.awaken();
			}
			if (fightStarted && !locked){
				seal();
			}
		}
	}

	public void onFightStart(){
		if (fightStarted) return;
		fightStarted = true;
		if (inArena( Dungeon.hero.pos )){
			seal();
		}
		playLevelMusicLater();
	}

	//화로가 켜지거나 꺼질 때
	public void onLightChanged( CastleLight light ){
		VampireLord l = lord();
		if (l != null && light.lit) l.checkPull();
	}

	public void bloodMoon(){
		bloodMoon = true;
		GameScene.flash( 0x660000 );
		Sample.INSTANCE.play( Assets.Sounds.CURSED );
		playLevelMusicLater();
	}

	@Override
	public void seal() {
		if (locked) return;
		super.seal();
		Statistics.qualifiedForBossChallengeBadge = true;

		//입구 통로를 바위로 막음
		for (int x = 10; x <= 12; x++){
			int c = x + SEAL_ROW * width();
			if (Actor.findChar( c ) != null) continue;
			Heap heap = heaps.get( c );
			if (heap != null){
				drop( heap.pickUp(), c - width() ).seen = heap.seen;
			}
			set( c, Terrain.WALL );
			GameScene.updateMap( c );
			CellEmitter.get( c ).start( Speck.factory( Speck.ROCK ), 0.07f, 10 );
		}
		PixelScene.shake( 3, 0.7f );
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		Dungeon.observe();
		playLevelMusicLater();
	}

	@Override
	public void unseal() {
		super.unseal();
		for (int x = 10; x <= 12; x++){
			int c = x + SEAL_ROW * width();
			if (map[c] == Terrain.WALL){
				set( c, Terrain.EMPTY );
				GameScene.updateMap( c );
			}
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

	public void onBossDefeated(){
		if (bossDefeated) return;
		bossDefeated = true;
		bloodMoon = false;

		unseal();

		//관이 부서지며 아래로 내려가는 계단이 드러남
		set( coffin, Terrain.EXIT );
		GameScene.updateMap( coffin );
		transitions.add( new LevelTransition( this, coffin, LevelTransition.Type.REGULAR_EXIT ) );
		CellEmitter.get( coffin ).burst( Speck.factory( Speck.WOOL ), 10 );
		CellEmitter.get( coffin ).burst( ShadowParticle.UP, 14 );
		PixelScene.shake( 2, 1f );
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		GLog.h( Messages.get( this, "victory" ) );

		for (Mob m : mobs.toArray( new Mob[0] )){
			if (m instanceof CastleLight) ((CastleLight) m).light();
		}
	}

	// ── 장식 ──

	@Override
	public Group addVisuals() {
		super.addVisuals();
		int S = DungeonTilemap.SIZE;

		//달빛 줄기
		for (int wx : WINDOWS){
			for (int y = ARENA_T; y <= ARENA_B; y++){
				final int cell = wx + y * width();
				if (distance( cell, coffin ) <= 1) continue;
				ColorBlock shaft = new ColorBlock( S, S, 0xFFB8D0FF ){
					@Override
					public void update() {
						super.update();
						visible = moonLit( cell );
					}
				};
				shaft.alpha( 0.13f );
				shaft.x = wx * S;
				shaft.y = y * S;
				visuals.add( shaft );
			}
		}

		//옥좌와 군주의 관
		visuals.add( place( VampireCastleSprites.tile( 20 ), throne ) );
		Image c = new Image( VampireCastleSprites.tile( 16 ) ){
			@Override
			public void update() {
				super.update();
				visible = !bossDefeated;
			}
		};
		visuals.add( place( c, coffin ) );
		return visuals;
	}

	@Override
	public Group addWallVisuals() {
		super.addWallVisuals();
		for (int wx : WINDOWS){
			//32x32로 촘촘히 찍은 창 (벽 타일과 같은 해상도), 핏빛 달이 뜨면 붉은 창으로
			Image win = new Image( Assets.Sprites.VC_WINDOW_HD, 0, 0, 32, 32 ){
				private boolean red = false;
				@Override
				public void update() {
					super.update();
					if (red != bloodMoon){
						red = bloodMoon;
						frame( red ? 32 : 0, 0, 32, 32 );
					}
				}
			};
			win.scale.set( 0.5f );
			wallVisuals.add( place( win, wx + width() ) );
		}
		return wallVisuals;
	}

	private Image place( Image img, int cell ){
		img.x = (cell % width()) * DungeonTilemap.SIZE;
		img.y = (cell / width()) * DungeonTilemap.SIZE;
		return img;
	}

	@Override
	public String tileName( int tile ) {
		switch (tile){
			case Terrain.EMPTY_SP: return Messages.get( this, "dais_name" );
			case Terrain.EMPTY_DECO: return Messages.get( VampireCastleLevel.class, "castle_floor_deco_name" );
			case Terrain.STATUE_SP:return Messages.get( this, "statue_name" );
			case Terrain.WALL:     return Messages.get( this, "wall_name" );
		}
		String n = VampireCastle.tileName( tile );
		return n != null ? n : super.tileName( tile );
	}

	@Override
	public String tileDesc( int tile ) {
		switch (tile){
			case Terrain.EMPTY_SP: return Messages.get( this, "dais_desc" );
			case Terrain.EMPTY_DECO: return Messages.get( VampireCastleLevel.class, "castle_floor_deco_desc" );
			case Terrain.STATUE_SP:return Messages.get( this, "statue_desc" );
			case Terrain.WALL:     return Messages.get( this, "wall_desc" );
			case Terrain.EXIT:     return Messages.get( this, "exit_desc" );
		}
		String d = VampireCastle.tileDesc( tile );
		return d != null ? d : super.tileDesc( tile );
	}

	private static final String THRONE = "throne";
	private static final String COFFIN = "coffin";
	private static final String BRAZIERS = "braziers";
	private static final String FIGHT = "fight";
	private static final String DEFEATED = "defeated";
	private static final String MOON = "blood_moon";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( THRONE, throne );
		bundle.put( COFFIN, coffin );
		int[] b = new int[braziers.size()];
		for (int i = 0; i < b.length; i++) b[i] = braziers.get( i );
		bundle.put( BRAZIERS, b );
		bundle.put( FIGHT, fightStarted );
		bundle.put( DEFEATED, bossDefeated );
		bundle.put( MOON, bloodMoon );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		throne = bundle.getInt( THRONE );
		coffin = bundle.getInt( COFFIN );
		braziers.clear();
		for (int c : bundle.getIntArray( BRAZIERS )) braziers.add( c );
		fightStarted = bundle.getBoolean( FIGHT );
		bossDefeated = bundle.getBoolean( DEFEATED );
		bloodMoon = bundle.getBoolean( MOON );
	}
}
