/*
 * Shattered Pixel Dungeon 모드: 테스터 리모컨 (층 이동 + 몬스터·보스 소환)
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

//테스터 전용: 원하는 층으로 바로 이동하고, 원하는 몬스터나 보스를 옆에 불러냄
public class DevRemote extends Item {

	public static final String AC_TRAVEL = "TRAVEL";
	public static final String AC_SUMMON = "SUMMON";

	{
		image = ItemSpriteSheet.DEV_REMOTE;
		defaultAction = AC_TRAVEL;
		unique = true;
		bones = false;
	}

	//지역별 일반 몬스터
	private static final Class<?>[][] MOBS = {
		{ Rat.class, Albino.class, Snake.class, Gnoll.class, Swarm.class, Crab.class, Slime.class, CausticSlime.class },
		{ Skeleton.class, Thief.class, Bandit.class, DM100.class, Guard.class, Necromancer.class, SpectralNecromancer.class },
		{ Bat.class, Brute.class, ArmoredBrute.class, Shaman.RedShaman.class, Shaman.BlueShaman.class, Shaman.PurpleShaman.class, Spinner.class, DM200.class, DM201.class },
		{ Ghoul.class, Elemental.FireElemental.class, Elemental.FrostElemental.class, Elemental.ShockElemental.class, Elemental.ChaosElemental.class, Warlock.class, Monk.class, Senior.class, Golem.class },
		{ Succubus.class, Eye.class, Scorpio.class, Acidic.class, RipperDemon.class, DemonSpawner.class },
		//특수 몬스터와 중간 보스
		{ FetidRat.class, GnollTrickster.class, GreatCrab.class, Mimic.class, GoldenMimic.class, CrystalMimic.class, EbonyMimic.class, Statue.class, ArmoredStatue.class, Wraith.class, TormentedSpirit.class, RotLasher.class },
		//이차원의 성소
		{ StoneSoldier.class, StoneWarden.class, RuneSentinel.class }
	};

	//보스: 그 자리 소환이 되는 보스(구)와, 전용 무대가 있어야 하는 보스(그 층으로 이동)
	private static final Class<?>[] BOSSES = { Goo.class, Tengu.class, DM300.class, DwarfKing.class, YogDzewa.class, SanctuaryColossus.class };
	private static final int[] BOSS_DEPTH = { 5, 10, 15, 20, 25, -1 };   //-1: 이차원의 성소 2층

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_TRAVEL );
		actions.add( AC_SUMMON );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_TRAVEL )){
			//금고 안에서 나가 버리면 맡겨 둔 소지품이 꼬이므로 막음
			if (inVault()) return;
			chooseRegion( true );
		} else if (action.equals( AC_SUMMON )){
			chooseRegion( false );
		}
	}

	//금고 안에서 나가 버리면 맡겨 둔 소지품이 꼬이므로 층 이동은 막음
	private static boolean inVault(){
		if (Dungeon.branch == 1 && Dungeon.depth >= 16 && Dungeon.depth <= 19){
			GLog.w( txt( "vault_locked" ) );
			return true;
		}
		return false;
	}

	private static String txt( String key, Object... args ){
		return Messages.get( DevRemote.class, key, args );
	}

	//1단계: 지역 고르기
	private void chooseRegion( final boolean travel ){
		String[] regions;
		if (travel){
			regions = new String[]{ txt( "region_0" ), txt( "region_1" ), txt( "region_2" ), txt( "region_3" ), txt( "region_4" ), txt( "depth_26" ), txt( "sanctuary" ) };
		} else {
			regions = new String[]{ txt( "region_0" ), txt( "region_1" ), txt( "region_2" ), txt( "region_3" ), txt( "region_4" ), txt( "special" ), txt( "sanctuary" ), txt( "bosses" ) };
		}
		GameScene.show( new WndOptions( new ItemSprite( this ), txt( travel ? "travel_title" : "summon_title" ),
				txt( travel ? "travel_msg" : "summon_msg", Dungeon.depth ), regions ){
			@Override
			protected void onSelect( int index ) {
				if (travel){
					if (index == 5) travel( 26 );
					else if (index == 6) chooseSanctuary();
					else chooseDepth( index );
				} else {
					if (index == 7) chooseBoss();
					else chooseMob( index );
				}
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

	private void travel( int depth ){
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

	//2단계(소환): 그 지역 몬스터 중 고르기
	private void chooseMob( final int group ){
		final Class<?>[] list = MOBS[group];
		String[] names = new String[list.length];
		for (int i = 0; i < list.length; i++){
			names[i] = Messages.titleCase( ((Mob) Reflection.newInstance( list[i] )).name() );
		}
		GameScene.show( new WndOptions( new ItemSprite( this ), group < 5 ? txt( "region_" + group ) : (group == 5 ? txt( "special" ) : txt( "sanctuary" )), txt( "pick_mob" ), names ){
			@Override
			protected void onSelect( int index ) {
				summon( list[index] );
			}
		});
	}

	private void chooseBoss(){
		String[] names = new String[BOSSES.length];
		for (int i = 0; i < BOSSES.length; i++){
			String n = Messages.titleCase( ((Mob) Reflection.newInstance( BOSSES[i] )).name() );
			names[i] = i == 0 ? txt( "boss_here", n ) : (BOSS_DEPTH[i] < 0 ? txt( "boss_sanctuary", n ) : txt( "boss_floor", n, BOSS_DEPTH[i] ));
		}
		GameScene.show( new WndOptions( new ItemSprite( this ), txt( "bosses" ), txt( "boss_msg" ), names ){
			@Override
			protected void onSelect( int index ) {
				if (index == 0) summon( BOSSES[0] );
				else if (inVault()) return;
				else if (BOSS_DEPTH[index] < 0) com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.travel(
						com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel.BRANCH );
				else travel( BOSS_DEPTH[index] );   //텐구·DM-300·드워프 왕·요그는 전용 무대가 있어야 싸움이 됨
			}
		});
	}

	//영웅 옆 빈칸에 불러냄
	private void summon( Class<?> cls ){
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		ArrayList<Integer> cells = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int c = hero.pos + n;
			if (c >= 0 && c < Dungeon.level.length() && Dungeon.level.passable[c] && Actor.findChar( c ) == null){
				cells.add( c );
			}
		}
		if (cells.isEmpty()){
			GLog.w( txt( "no_space" ) );
			return;
		}
		Mob m;
		if (Mimic.class.isAssignableFrom( cls )){
			//미믹은 층 깊이에 맞춘 능력치와 보상을 함께 만들어야 함
			m = Mimic.spawnAt( cells.get( 0 ), cls, false );
		} else {
			m = (Mob) Reflection.newInstance( cls );
			if (m == null) return;
			m.pos = cells.get( 0 );
			if (m instanceof Wraith) ((Wraith) m).adjustStats( Dungeon.scalingDepth() );
			//석상은 들고 있을 무기(갑옷 석상은 갑옷까지)를 만들어 줘야 함. 없으면 공격·사망 때 멈춤
			if (m instanceof Statue) ((Statue) m).createWeapon( false );
			m.state = m.HUNTING;
		}
		GameScene.add( m );
		Dungeon.level.occupyCell( m );
		CellEmitter.get( m.pos ).burst( Speck.factory( Speck.LIGHT ), 6 );
		Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
		GLog.i( txt( "summoned", Messages.titleCase( m.name() ) ) );
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
