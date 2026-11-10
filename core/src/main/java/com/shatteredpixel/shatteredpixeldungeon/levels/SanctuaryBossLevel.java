/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 2층 (수호자의 방)
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
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

//이차원의 성소 2층: 남쪽 전실(1층으로 가는 계단) → 통로 → 둥근 투기장.
//투기장 북쪽 왕좌에 성소의 거대 골렘이 잠들어 있고, 영웅이 투기장에 들어서면 문이 닫히며 싸움이 시작됨.
//이기면 문이 다시 열리고 투기장 가운데 룬 원 위에 20층으로 돌아가는 차원 문이 열림
public class SanctuaryBossLevel extends Level {

	public static final int BRANCH = 8;

	{
		color1 = 0x6a4bd8;
		color2 = 0x88e8ff;
	}

	private static final String[] MAP = {
		"###########################",
		"###########################",
		"###########################",
		"#########,,_____,,#########",
		"#######,,,.__T__.,,,#######",
		"######,,.P.......P.,,######",
		"#####,...............,#####",
		"####,.................,####",
		"###,,..S...........S..,,###",
		"###,......_______......,###",
		"##,,......_______......,,##",
		"##,......._______.......,##",
		"##,.P.....___o___.....P.,##",
		"##,......._______.......,##",
		"##,,......_______......,,##",
		"###,......_______......,###",
		"###,,..S...........S..,,###",
		"####,.................,####",
		"#####,...............,#####",
		"######,,...........,,######",
		"#######,,,.......,,,#######",
		"#############+#############",
		"############___############",
		"############___############",
		"############___############",
		"#########.........#########",
		"#########.S.....S.#########",
		"#########...___...#########",
		"#########...___...#########",
		"#########....<....#########",
		"#########.S.....S.#########",
		"#########.........#########",
		"###########################"
	};

	private int entranceCell = -1, throne = -1, door = -1, center = -1;
	private final ArrayList<Integer> summon = new ArrayList<>();
	private boolean fightStarted = false;
	private boolean bossDefeated = false;

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
			Music.INSTANCE.playTracks( HallsLevel.HALLS_TRACK_LIST, HallsLevel.HALLS_TRACK_CHANCES, false );
		}
	}

	@Override
	protected boolean build() {
		SanctuaryLevel.parse( this, MAP, summon, null );
		for (int i = 0; i < length(); i++){
			char c = MAP[i / width()].charAt( i % width() );
			if (c == '<') entranceCell = i;
			if (c == 'T') throne = i;
			if (c == '+') door = i;
			if (c == 'o') center = i;
		}
		transitions.add( new LevelTransition( this, entranceCell, LevelTransition.Type.REGULAR_ENTRANCE,
				SanctuaryQuest.DEPTH, SanctuaryLevel.BRANCH, LevelTransition.Type.REGULAR_EXIT ) );
		SanctuaryLevel.addRune( this, center );
		feeling = Feeling.NONE;
		return true;
	}

	public ArrayList<Integer> summonCells(){
		return summon;
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createMobs() {
		SanctuaryColossus boss = new SanctuaryColossus();
		boss.pos = throne;
		mobs.add( boss );
	}

	@Override
	protected void createItems() {
		//전실에 떨어진 약간의 금화
		drop( new Gold().random(), entranceCell - width() * 2 - 2 );
	}

	//투기장 안 (문보다 위쪽)
	private boolean inArena( int cell ){
		return door >= 0 && cell / width() < door / width() - 1;
	}

	@Override
	public void occupyCell( Char ch ) {
		super.occupyCell( ch );
		if (ch == Dungeon.hero && !fightStarted && !bossDefeated && inArena( ch.pos )){
			for (Mob m : mobs){
				if (m instanceof SanctuaryColossus){
					((SanctuaryColossus) m).awaken();
					break;
				}
			}
		}
	}

	//보스가 깨어남: 문이 돌로 막히고 층이 잠김
	public void onFightStart(){
		if (fightStarted) return;
		fightStarted = true;
		seal();
		set( door, Terrain.WALL );
		GameScene.updateMap( door );
		CellEmitter.get( door ).burst( Speck.factory( Speck.ROCK ), 10 );
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		Dungeon.observe();
		GLog.w( Messages.get( this, "sealed" ) );
		playLevelMusic();
	}

	//보스를 쓰러뜨림: 문이 열리고 투기장 가운데에 차원 문이 생김
	public void onBossDefeated(){
		bossDefeated = true;
		SanctuaryQuest.bossDefeated = true;
		unseal();
		set( door, Terrain.DOOR );
		GameScene.updateMap( door );
		Dungeon.observe();

		transitions.add( new LevelTransition( this, center, LevelTransition.Type.BRANCH_ENTRANCE,
				SanctuaryQuest.DEPTH, 0, LevelTransition.Type.REGULAR_ENTRANCE ) );
		SanctuaryLevel.Portal p = new SanctuaryLevel.Portal();
		p.pos( center % width(), center / width() );
		customTiles.add( p );
		GameScene.add( p, false );
		SanctuaryLevel.PortalTop top = new SanctuaryLevel.PortalTop();
		top.pos( center % width(), center / width() - 1 );
		customWalls.add( top );
		GameScene.add( top, true );

		GLog.p( Messages.get( this, "portal_open" ) );
		playLevelMusic();
	}

	@Override
	public Group addVisuals() {
		super.addVisuals();
		SanctuaryLevel.addVoidMotes( this, visuals );
		return visuals;
	}

	@Override
	public String tileName( int tile ) {
		return SanctuaryLevel.name( tile, super.tileName( tile ) );
	}

	@Override
	public String tileDesc( int tile ) {
		return SanctuaryLevel.desc( tile, super.tileDesc( tile ) );
	}

	private static final String ENTRANCE_CELL = "entrance_cell";
	private static final String THRONE = "throne";
	private static final String DOOR = "door";
	private static final String CENTER = "center";
	private static final String SUMMON = "summon";
	private static final String FIGHT = "fight";
	private static final String DEFEATED = "defeated";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ENTRANCE_CELL, entranceCell );
		bundle.put( THRONE, throne );
		bundle.put( DOOR, door );
		bundle.put( CENTER, center );
		int[] s = new int[summon.size()];
		for (int i = 0; i < s.length; i++) s[i] = summon.get( i );
		bundle.put( SUMMON, s );
		bundle.put( FIGHT, fightStarted );
		bundle.put( DEFEATED, bossDefeated );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		entranceCell = bundle.getInt( ENTRANCE_CELL );
		throne = bundle.getInt( THRONE );
		door = bundle.getInt( DOOR );
		center = bundle.getInt( CENTER );
		summon.clear();
		if (bundle.contains( SUMMON )) for (int i : bundle.getIntArray( SUMMON )) summon.add( i );
		fightStarted = bundle.getBoolean( FIGHT );
		bossDefeated = bundle.getBoolean( DEFEATED );
	}
}
