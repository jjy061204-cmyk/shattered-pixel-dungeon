/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드): 테스터 소환 기기
 *  - 지역별 몬스터·특수 몬스터·갈림길 몬스터·보스를 아이콘 목록에서 골라 바로 옆에 불러냄
 *  - 미믹은 안에 넣을 아이템을 가방에서 골라 넣을 수 있음
 *  - 죽지 않는 허수아비(공격 시험용): 한 층에 하나, 치울 수 있음
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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDevSummon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class DevSummoner extends Item {

	public static final String AC_SUMMON = "SUMMON";

	{
		image = ItemSpriteSheet.DEV_SUMMONER;
		defaultAction = AC_SUMMON;
		unique = true;
		bones = false;
	}

	//지역별 일반 몬스터
	public static final Class<?>[][] MOBS = {
		{ Rat.class, Albino.class, Snake.class, Gnoll.class, Swarm.class, Crab.class, Slime.class, CausticSlime.class },
		{ Skeleton.class, Thief.class, Bandit.class, DM100.class, Guard.class, Necromancer.class, SpectralNecromancer.class },
		{ Bat.class, Brute.class, ArmoredBrute.class, Shaman.RedShaman.class, Shaman.BlueShaman.class, Shaman.PurpleShaman.class, Spinner.class, DM200.class, DM201.class },
		{ Ghoul.class, Elemental.FireElemental.class, Elemental.FrostElemental.class, Elemental.ShockElemental.class, Elemental.ChaosElemental.class, Warlock.class, Monk.class, Senior.class, Golem.class },
		{ Succubus.class, Eye.class, Scorpio.class, Acidic.class, RipperDemon.class, DemonSpawner.class },
		//특수 몬스터와 중간 보스
		{ FetidRat.class, GnollTrickster.class, GreatCrab.class, Mimic.class, GoldenMimic.class, CrystalMimic.class, EbonyMimic.class, Statue.class, ArmoredStatue.class, Wraith.class, TormentedSpirit.class, RotLasher.class },
		//이차원의 성소
		{ StoneSoldier.class, StoneWarden.class, RuneSentinel.class },
		//갈림길: 놀 제국
		{ GnollScout.class, GnollJavelineer.class, ScrapGnoll.class, GnollCenturion.class, GnollGatekeeper.class },
		//갈림길: 흡혈귀의 성
		{ com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GhoulGnoll.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BloodPriest.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BloodSpider.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CastleKnight.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.VampireBride.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BloodThrall.class },
		//갈림길: 수정 광맥
		{ com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalBat.class, com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalBrute.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalArmoredBrute.class, com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalRedShaman.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalBlueShaman.class, com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalPurpleShaman.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalSpinner.class, com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalDM200.class,
		  com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalVeinMobs.CrystalDM201.class }
	};

	//보스: 그 자리 소환이 되는 보스(구)와, 전용 무대가 있어야 하는 보스(그 층으로 이동)
	public static final Class<?>[] BOSSES = { Goo.class, Tengu.class, DM300.class, DwarfKing.class, YogDzewa.class, SanctuaryColossus.class, GnollWarlord.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.VampireLord.class, com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalDM300.class };
	public static final int[] BOSS_DEPTH = { 5, 10, 15, 20, 25, -1, 15, 15, 15 };   //-1: 이차원의 성소 2층

	//광산 보스는 그 갈림길 루트의 15층에만 있음
	public static com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves bossRoute( Class<?> boss ){
		if (boss == DM300.class) return com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves.MINES;
		if (boss == GnollWarlord.class) return com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves.GNOLL_EMPIRE;
		if (boss == com.shatteredpixel.shatteredpixeldungeon.actors.mobs.VampireLord.class) return com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves.VAMPIRE_CASTLE;
		if (boss == com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalDM300.class) return com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves.CRYSTAL_VEIN;
		return null;
	}

	//분류 이름 (MOBS 순서와 같음, 마지막은 보스)
	public static String groupName( int group ){
		switch (group){
			case 0: case 1: case 2: case 3: case 4: return DevRemote.txt( "region_" + group );
			case 5:  return DevRemote.txt( "special" );
			case 6:  return DevRemote.txt( "sanctuary" );
			case 7:  return DevRemote.txt( "gnoll_empire" );
			case 8:  return DevRemote.txt( "vampire_castle" );
			case 9:  return DevRemote.txt( "crystal_vein" );
			default: return DevRemote.txt( "bosses" );
		}
	}
	public static int groupCount(){
		return MOBS.length + 1;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_SUMMON );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_SUMMON )){
			GameScene.show( new WndDevSummon( this ) );
		}
	}

	//영웅 옆 빈칸 (없으면 -1)
	public static int freeCell(){
		Hero hero = Dungeon.hero;
		if (hero == null) return -1;
		ArrayList<Integer> cells = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int c = hero.pos + n;
			if (c >= 0 && c < Dungeon.level.length() && Dungeon.level.passable[c] && Actor.findChar( c ) == null){
				cells.add( c );
			}
		}
		return cells.isEmpty() ? -1 : cells.get( 0 );
	}

	//영웅 옆 빈칸에 불러냄
	public static void summon( Class<?> cls ){
		int cell = freeCell();
		if (cell < 0){
			GLog.w( DevRemote.txt( "no_space" ) );
			return;
		}
		Mob m;
		if (Mimic.class.isAssignableFrom( cls )){
			//미믹은 층 깊이에 맞춘 능력치와 보상을 함께 만들어야 함
			m = Mimic.spawnAt( cell, cls, false );
		} else {
			m = (Mob) Reflection.newInstance( cls );
			if (m == null) return;
			m.pos = cell;
			if (m instanceof Wraith) ((Wraith) m).adjustStats( Dungeon.scalingDepth() );
			//석상은 들고 있을 무기(갑옷 석상은 갑옷까지)를 만들어 줘야 함. 없으면 공격·사망 때 멈춤
			if (m instanceof Statue) ((Statue) m).createWeapon( false );
			//제자리에 붙박인 채 가만히 있는 몹(악마 소환수 등)은 원래 상태 그대로. 덤벼들게 하면 공격 동작에서 멈춤
			if (!(m.properties().contains( Char.Property.IMMOVABLE ) && m.state == m.PASSIVE)){
				m.state = m.HUNTING;
			}
		}
		place( m );
	}

	//미믹: 고른 아이템만 품게 해서 불러냄 (고른 게 없으면 원래 보상)
	public static void summonMimic( Class<?> cls, ArrayList<Item> inside ){
		int cell = freeCell();
		if (cell < 0){
			GLog.w( DevRemote.txt( "no_space" ) );
			for (Item i : inside) if (!i.collect()) Dungeon.level.drop( i, Dungeon.hero.pos );
			return;
		}
		Mimic m = Mimic.spawnAt( cell, cls, false );
		if (!inside.isEmpty()) m.items = new ArrayList<>( inside );
		place( m );
	}

	private static void place( Mob m ){
		GameScene.add( m );
		Dungeon.level.occupyCell( m );
		CellEmitter.get( m.pos ).burst( Speck.factory( Speck.LIGHT ), 6 );
		Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
		GLog.i( DevRemote.txt( "summoned", Messages.titleCase( m.name() ) ) );
	}

	//보스: 구는 이 자리에, 나머지는 전용 무대(새로 만든 보스 층)로 이동
	public static void boss( int index ){
		if (index == 0){ summon( BOSSES[0] ); return; }
		if (DevRemote.inVault()) return;
		if (BOSS_DEPTH[index] < 0){
			//이미 성소 2층이면 그 자리에서 왕좌에 다시 불러냄
			if (Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel){
				boolean ok = ((com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel) Dungeon.level).devRespawnBoss();
				GLog.i( DevRemote.txt( ok ? "boss_respawned" : "boss_exists" ) );
			} else {
				com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.travel(
						com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel.BRANCH );
			}
		} else if (bossRoute( BOSSES[index] ) != null){
			//광산 보스: 그 루트가 아니면 못 감, 루트를 아직 안 골랐으면 그 루트로 정함
			com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves need = bossRoute( BOSSES[index] );
			com.shatteredpixel.shatteredpixeldungeon.levels.Routes.Caves r = com.shatteredpixel.shatteredpixeldungeon.levels.Routes.caves;
			if (r != null && r != need){
				GLog.w( DevRemote.txt( "other_route" ) );
			} else {
				com.shatteredpixel.shatteredpixeldungeon.levels.Routes.caves = need;
				DevRemote.travelFresh( 15 );
			}
		} else {
			DevRemote.travelFresh( BOSS_DEPTH[index] );   //텐구·드워프 왕·요그는 전용 무대가 있어야 싸움이 됨
		}
	}

	public static String bossLabel( int index ){
		String n = Messages.titleCase( ((Mob) Reflection.newInstance( BOSSES[index] )).name() );
		if (index == 0) return DevRemote.txt( "boss_here", n );
		if (BOSS_DEPTH[index] < 0) return DevRemote.txt( "boss_sanctuary", n );
		return DevRemote.txt( "boss_floor", n, BOSS_DEPTH[index] );
	}

	// ── 허수아비 ──
	public static TrainingDummy dummyHere(){
		if (Dungeon.level == null) return null;
		for (Mob m : Dungeon.level.mobs){
			if (m instanceof TrainingDummy && m.isAlive()) return (TrainingDummy) m;
		}
		return null;
	}

	public static void summonDummy(){
		if (dummyHere() != null){
			GLog.w( Messages.get( DevSummoner.class, "dummy_exists" ) );
			return;
		}
		int cell = freeCell();
		if (cell < 0){
			GLog.w( DevRemote.txt( "no_space" ) );
			return;
		}
		TrainingDummy d = new TrainingDummy();
		d.pos = cell;
		place( d );
	}

	public static void removeDummy(){
		TrainingDummy d = dummyHere();
		if (d == null){
			GLog.w( Messages.get( DevSummoner.class, "dummy_none" ) );
			return;
		}
		d.dismiss();
		GLog.i( Messages.get( DevSummoner.class, "dummy_removed" ) );
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
