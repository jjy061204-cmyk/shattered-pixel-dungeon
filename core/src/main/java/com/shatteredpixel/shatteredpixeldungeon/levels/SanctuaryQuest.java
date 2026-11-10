/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 퀘스트 진행 상태
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

//20층 임프가 드워프 금고에서 찾은 이차원 게이트 장치를 상점 옆에 설치해 주면(given) 성소로 드나들 수 있음.
//성소 2층 투기장의 거대 골렘을 쓰러뜨리면(bossDefeated) 봉인된 엑스칼리버를 얻음
public class SanctuaryQuest {

	public static final int DEPTH = 20;
	public static final int REWARD_GOLD = 2000;

	public static boolean given = false;
	public static boolean bossDefeated = false;
	public static boolean rewarded = false;
	//보스를 쓰러뜨리고 엑스칼리버를 주움: 이제 게이트로 자유롭게 드나들 수 있음
	public static boolean completed = false;

	public static void reset(){
		given = false;
		bossDefeated = false;
		rewarded = false;
		completed = false;
	}

	//성소 1층 입구(차원 문)로 이동
	public static void travel( int branch ){
		Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
		Level.beforeTransition();
		Invisibility.dispel();
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		InterlevelScene.returnDepth = DEPTH;
		InterlevelScene.returnBranch = branch;
		InterlevelScene.returnPos = -1;
		Game.switchScene( InterlevelScene.class );
	}

	private static final String NODE = "sanctuary";

	public static void storeInBundle( Bundle bundle ){
		Bundle node = new Bundle();
		node.put( "given", given );
		node.put( "boss", bossDefeated );
		node.put( "rewarded", rewarded );
		node.put( "completed", completed );
		bundle.put( NODE, node );
	}

	public static void restoreFromBundle( Bundle bundle ){
		Bundle node = bundle.getBundle( NODE );
		if (node.isNull()){
			reset();
			return;
		}
		given = node.getBoolean( "given" );
		bossDefeated = node.getBoolean( "boss" );
		rewarded = node.getBoolean( "rewarded" );
		completed = node.contains( "completed" ) ? node.getBoolean( "completed" ) : bossDefeated;
	}
}
