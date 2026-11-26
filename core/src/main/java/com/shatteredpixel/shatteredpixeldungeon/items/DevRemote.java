/*
 * Shattered Pixel Dungeon 모드: 테스터 리모컨 (층 이동 + 레벨 조정). 몬스터·보스 소환은 DevSummoner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

//테스터 전용: 원하는 층으로 바로 이동하고, 레벨을 바로 맞춤
public class DevRemote extends Item {

	public static final String AC_TRAVEL = "TRAVEL";
	public static final String AC_SUMMONER = "SUMMONER";   //예전 저장: 소환 기기가 없으면 받기
	public static final String AC_LEVEL = "LEVEL";

	{
		image = ItemSpriteSheet.DEV_REMOTE;
		defaultAction = AC_TRAVEL;
		unique = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_TRAVEL );
		actions.add( AC_LEVEL );
		if (hero.belongings.getItem( DevSummoner.class ) == null) actions.add( AC_SUMMONER );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_TRAVEL )){
			//금고 안에서 나가 버리면 맡겨 둔 소지품이 꼬이므로 막음
			if (inVault()) return;
			chooseRegion( true );
		} else if (action.equals( AC_SUMMONER )){
			if (hero.belongings.getItem( DevSummoner.class ) == null){
				DevSummoner d = new DevSummoner();
				if (d.collect()) GLog.p( txt( "got_summoner" ) );
			}
		} else if (action.equals( AC_LEVEL )){
			chooseLevel();
		}
	}

	//레벨 조정: 올리기/내리기/바로 지정
	private void chooseLevel(){
		final int[] delta = { 1, 5, -1, -5 };
		GameScene.show( new WndOptions( new ItemSprite( this ), txt( "level_title" ), txt( "level_msg", Dungeon.hero.lvl ),
				"+1", "+5", "-1", "-5", txt( "level_min" ), txt( "level_max" ) ){
			@Override
			protected void onSelect( int index ) {
				Hero h = Dungeon.hero;
				int to = index < 4 ? h.lvl + delta[index] : (index == 4 ? 1 : Hero.MAX_LEVEL);
				h.devSetLevel( to );
				GLog.p( txt( "level_set", h.lvl, h.HT ) );
				h.sprite.showStatus( com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE, txt( "level_status", h.lvl ) );
				if (index < 4) chooseLevel();
			}
		} );
	}

	//금고 안에서 나가 버리면 맡겨 둔 소지품이 꼬이므로 층 이동은 막음
	public static boolean inVault(){
		if (Dungeon.branch == 1 && Dungeon.depth >= 16 && Dungeon.depth <= 19){
			GLog.w( txt( "vault_locked" ) );
			return true;
		}
		return false;
	}

	public static String txt( String key, Object... args ){
		return Messages.get( DevRemote.class, key, args );
	}

	//1단계: 지역 고르기
	private void chooseRegion( final boolean travel ){
		String[] regions = new String[]{ txt( "region_0" ), txt( "region_1" ), txt( "region_2" ), txt( "region_3" ), txt( "region_4" ), txt( "depth_26" ), txt( "sanctuary" ) };
		GameScene.show( new WndOptions( new ItemSprite( this ), txt( "travel_title" ),
				txt( "travel_msg", Dungeon.depth ), regions ){
			@Override
			protected void onSelect( int index ) {
				if (index == 5) travel( 26 );
				else if (index == 6) chooseSanctuary();
				else if (index == 2 && com.shatteredpixel.shatteredpixeldungeon.levels.Routes.caves == null) chooseCavesRoute( index );
				else chooseDepth( index );
			}
		});
	}

	//2단계(이동): 그 지역의 다섯 층 중 고르기 (보스 층 표시)
	private void chooseDepth( final int region ){
		String[] floors = new String[5];
		for (int i = 0; i < 5; i++){
			int d = region * 5 + i + 1;
			floors[i] = (i == 4) ? txt( "depth_boss", d ) : txt( "depth", d );
		}
		GameScene.show( new WndOptions( new ItemSprite( this ), txt( "region_" + region ), txt( "pick_depth" ), floors ){
			@Override
			protected void onSelect( int index ) {
				travel( region * 5 + index + 1 );
			}
			@Override
			protected boolean enabled( int index ) {
				return region * 5 + index + 1 != Dungeon.depth || Dungeon.branch != 0;
			}
		});
	}

	//광산 층으로 처음 갈 때: 갈림길 테마부터 고름 (만들어진 길만)
	private void chooseCavesRoute( final int region ){
		final com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves[] all = com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves.values();
		String[] names = new String[all.length];
		for (int i = 0; i < all.length; i++){
			names[i] = all[i].ready ? all[i].title()
					: Messages.get( com.shatteredpixel.shatteredpixeldungeon.levels.Routes.class, "not_ready", all[i].title() );
		}
		GameScene.show( new WndOptions( new ItemSprite( this ), txt( "route_title" ), txt( "route_msg" ), names ){
			@Override
			protected void onSelect( int index ) {
				com.shatteredpixel.shatteredpixeldungeon.levels.Routes.caves = all[index];
				chooseDepth( region );
			}
			@Override
			protected boolean enabled( int index ) {
				return all[index].ready;
			}
		});
	}

	//이차원의 성소 1·2층으로 바로 이동 (테스트용)
	private void chooseSanctuary(){
		GameScene.show( new WndOptions( new ItemSprite( this ), txt( "sanctuary" ), txt( "pick_depth" ),
				txt( "sanctuary_1" ), txt( "sanctuary_2" ) ){
			@Override
			protected void onSelect( int index ) {
				if (Dungeon.level != null && Dungeon.level.locked){
					GLog.w( txt( "locked" ) );
					return;
				}
				com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.travel( index == 0
						? com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryLevel.BRANCH
						: com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel.BRANCH );
			}
		});
	}

	//보스 층을 새로 만들어서 이동 (이미 쓰러뜨렸거나 지금 그 층에 있어도 보스와 다시 싸울 수 있게)
	public static void travelFresh( int depth ){
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) return;
		if (Dungeon.level != null && Dungeon.level.locked){
			GLog.w( txt( "locked" ) );
			return;
		}
		Dungeon.generatedLevels.remove( (Integer)(depth) );
		GLog.i( txt( "boss_fresh" ) );
		travel( depth );
	}

	public static void travel( int depth ){
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) return;
		if (Dungeon.level != null && Dungeon.level.locked){
			GLog.w( txt( "locked" ) );
			return;
		}
		Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
		Level.beforeTransition();
		Invisibility.dispel();
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		InterlevelScene.returnDepth = depth;
		InterlevelScene.returnBranch = 0;
		InterlevelScene.returnPos = -1;   //그 층 입구에 도착
		Game.switchScene( InterlevelScene.class );
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
