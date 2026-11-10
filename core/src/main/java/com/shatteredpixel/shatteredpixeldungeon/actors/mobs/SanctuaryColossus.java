/*
 * Shattered Pixel Dungeon 모드: 성소의 수호 거상 (이차원의 성소 2층 보스)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.GoldSparkle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Excalibur;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ColossusSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//성소의 수호 거상. 봉인된 엑스칼리버를 지키는 마지막 수호자.
//
// 1단계 (체력 2/3 이상) 각성
//   - 묵직한 주먹질
//   - 대지 강타: 두 팔을 들어 내려치면, 영웅 둘레 표시된 칸에 1~3턴 뒤 돌이 떨어짐 (한 칸은 항상 비어 있음)
// 2단계 (2/3 ~ 1/3) 핵 개방
//   - 들어설 때 받침대에서 룬 감시석 둘을 불러냄
//   - 룬 광선: 한 턴 동안 핵에 빛을 모으며(청록 표시) 일직선을 겨눈 뒤 발사. 줄 위의 모두가 맞음
//     모으는 동안은 핵이 드러나 받는 피해 1.5배
// 3단계 (1/3 이하) 폭주
//   - 들어설 때 석상 병사 둘을 불러냄, 더 빠르게 공격·이동
//   - 대지 강타 때 자기 둘레 2칸에도 충격파 돌이 떨어짐 (틈 있음), 기술 대기시간 짧아짐
public class SanctuaryColossus extends Mob {

	{
		spriteClass = ColossusSprite.class;

		HP = HT = 500;
		defenseSkill = 20;

		EXP = 60;
		maxLvl = -2;

		state = PASSIVE;

		properties.add( Property.BOSS );
		properties.add( Property.INORGANIC );
	}

	private boolean awake = false;
	private int phase = 1;
	private int quakeCD = 3;
	private int beamCD = 2;
	private boolean beamCharging = false;
	private int beamTarget = -1;
	private int lastSkill = 0;   //1: 강타, 2: 광선
	private Ballistica beamPath;

	@Override
	public int damageRoll() {
		return phase >= 3 ? Random.NormalIntRange( 28, 42 ) : Random.NormalIntRange( 25, 38 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 36;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 14 );
	}

	@Override
	public float attackDelay() {
		return super.attackDelay() * (phase >= 3 ? 0.7f : 1f);
	}

	@Override
	public float speed() {
		return super.speed() * (phase >= 3 ? 1.25f : 1f);
	}

	public boolean isAwake(){
		return awake;
	}

	public boolean isCharging(){
		return beamCharging;
	}

	public int phase(){
		return phase;
	}

	//투기장에 들어온 영웅을 보고 깨어남
	public void awaken(){
		if (awake) return;
		awake = true;
		state = HUNTING;
		enemy = Dungeon.hero;
		target = Dungeon.hero.pos;
		BossHealthBar.assignBoss( this );
		yell( Messages.get( this, "notice" ) );
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		PixelScene.shake( 2, 0.8f );
		if (sprite != null){
			sprite.emitter().burst( GoldSparkle.BURST, 20 );
			sprite.emitter().burst( RuneMote.FACTORY, 16 );
		}
		if (Dungeon.level instanceof SanctuaryBossLevel){
			((SanctuaryBossLevel) Dungeon.level).onFightStart();
		}
	}

	@Override
	protected boolean act() {
		if (!awake){
			spend( TICK );
			return true;
		}

		if (beamCharging){
			return fireBeam();
		}

		if (quakeCD > 0) quakeCD--;
		if (beamCD > 0) beamCD--;

		Char hero = Dungeon.hero;
		boolean sees = hero.isAlive() && hero.invisible <= 0 && Dungeon.level.heroFOV[pos];
		int dist = Dungeon.level.distance( pos, hero.pos );

		boolean beamReady = phase >= 2 && beamCD <= 0 && sees && dist >= 2;
		boolean quakeReady = quakeCD <= 0 && sees && dist <= 7;

		if (beamReady && (!quakeReady || lastSkill != 2)){
			chargeBeam( hero.pos );
			return true;
		}
		if (quakeReady){
			quake();
			return true;
		}
		return super.act();
	}

	//==== 대지 강타 ====

	private void quake(){
		lastSkill = 1;
		quakeCD = phase >= 3 ? 4 : 6;

		Char hero = Dungeon.hero;
		int center = hero.pos;

		//영웅 둘레 2칸: 가까울수록 많이 떨어짐. 한 칸은 반드시 비워 둠
		int safe;
		int tries = 20;
		do {
			safe = center + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
		} while (tries-- > 0 && (Dungeon.level.solid[safe] || safe == pos));

		ArrayList<Integer> cells = new ArrayList<>();
		for (int i = 0; i < Dungeon.level.length(); i++){
			if (Dungeon.level.solid[i] || i == pos || i == safe) continue;
			int d = Dungeon.level.distance( center, i );
			if (d <= 2 && Random.Int( 1 + d ) == 0){
				cells.add( i );
			}
		}
		//폭주: 자기 둘레 2칸에도 충격파 (한 칸 걸러 하나씩)
		if (phase >= 3){
			int k = 0;
			for (int i = 0; i < Dungeon.level.length(); i++){
				if (Dungeon.level.solid[i] || cells.contains( i ) || i == safe) continue;
				if (Dungeon.level.distance( pos, i ) == 2 && (k++ % 2 == 0)){
					cells.add( i );
				}
			}
		}

		float delay = GameMath.gate( 2 * TICK, (float)Math.ceil( hero.cooldown() ) + TICK, 3 * TICK );
		for (int c : cells){
			GameScene.targetedCell( c, delay );
		}
		Buff.append( this, QuakeRocks.class, delay ).setRockPositions( cells );

		if (sprite != null) ((ColossusSprite) sprite).slam( center );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 0.6f, 0.6f );
		GLog.w( Messages.get( this, "quake" ) );
		Dungeon.hero.interrupt();

		spend( TICK );
	}

	//2턴 뒤 떨어지는 돌
	public static class QuakeRocks extends DelayedRockFall {
		@Override
		public void affectChar( Char ch ) {
			if (ch instanceof SanctuaryColossus || ch instanceof StoneSoldier
					|| ch instanceof StoneWarden || ch instanceof RuneSentinel) return;
			ch.damage( Random.NormalIntRange( 14, 22 ), this );
			if (ch.isAlive()){
				Buff.prolong( ch, Cripple.class, 3f );
			} else if (ch == Dungeon.hero){
				Dungeon.fail( target );
				GLog.n( Messages.get( SanctuaryColossus.class, "rock_kill" ) );
			}
		}
	}

	//==== 룬 광선 ====

	public static class RuneBeam {}

	private void chargeBeam( int target ){
		lastSkill = 2;
		beamCharging = true;
		beamTarget = target;

		Ballistica aim = new Ballistica( pos, target, Ballistica.STOP_SOLID );
		float delay = GameMath.gate( TICK, (float)Math.ceil( Dungeon.hero.cooldown() ), 2 * TICK );
		for (int c : aim.subPath( 1, aim.dist )){
			TargetedCell t = GameScene.targetedCell( c, delay );
			if (t != null) t.hardlight( 0x40E8D8 );
		}

		if (sprite != null){
			((ColossusSprite) sprite).charge( target );
			sprite.showStatus( CharSprite.WARNING, Messages.get( this, "core_open" ) );
		}
		GLog.w( Messages.get( this, "beam_charge" ) );
		Dungeon.hero.interrupt();
		spend( delay );
	}

	private boolean fireBeam(){
		beamCharging = false;
		beamCD = phase >= 3 ? 4 : 6;
		beamPath = new Ballistica( pos, beamTarget, Ballistica.STOP_SOLID );
		spend( attackDelay() );

		if (sprite != null && sprite.visible
				&& (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[beamPath.collisionPos])){
			((ColossusSprite) sprite).blast( beamPath.collisionPos );
			return false;
		} else {
			if (sprite != null) sprite.idle();
			resolveBeam();
			return true;
		}
	}

	//광선이 지나간 줄 위의 모두에게 피해 (골렘은 제외)
	public void resolveBeam(){
		if (beamPath == null) return;
		for (int c : beamPath.subPath( 1, beamPath.dist )){
			if (Dungeon.level.heroFOV[c]){
				CellEmitter.center( c ).burst( RuneMote.FACTORY, 3 );
			}
			Char ch = Actor.findChar( c );
			if (ch == null || ch == this || ch instanceof StoneSoldier
					|| ch instanceof StoneWarden || ch instanceof RuneSentinel) continue;

			ch.damage( Random.NormalIntRange( 26, 38 ), new RuneBeam() );
			if (ch.sprite != null && Dungeon.level.heroFOV[c]) ch.sprite.flash();
			if (ch == Dungeon.hero && !ch.isAlive()){
				Dungeon.fail( this );
				GLog.n( Messages.get( this, "beam_kill" ) );
			}
		}
		beamPath = null;
		beamTarget = -1;
	}

	//==== 피해와 단계 ====

	@Override
	public void damage( int dmg, Object src ) {
		if (!awake) awaken();
		//빛을 모으는 동안은 핵이 드러나 있음
		if (beamCharging && dmg > 0){
			dmg = Math.round( dmg * 1.5f );
		}
		super.damage( dmg, src );
		if (!isAlive()) return;
		if (phase == 1 && HP <= HT * 2 / 3){
			enterPhase( 2 );
		}
		if (phase == 2 && HP <= HT / 3){
			enterPhase( 3 );
		}
	}

	private void enterPhase( int p ){
		phase = p;
		if (p == 2){
			yell( Messages.get( this, "phase2" ) );
			beamCD = Math.min( beamCD, 1 );
			summon( 2, true );
		} else if (p == 3){
			yell( Messages.get( this, "phase3" ) );
			quakeCD = Math.min( quakeCD, 2 );
			summon( 2, false );
			BossHealthBar.bleed( true );
		}
		PixelScene.shake( 3, 0.8f );
		GameScene.flash( 0x3040E8D8 );
		if (sprite != null){
			sprite.emitter().burst( RuneMote.FACTORY, 30 );
			sprite.emitter().burst( GoldSparkle.BURST, 16 );
		}
	}

	//받침대에서 석상을 불러냄
	private void summon( int count, boolean sentinels ){
		ArrayList<Integer> spots = new ArrayList<>();
		if (Dungeon.level instanceof SanctuaryBossLevel){
			for (int c : ((SanctuaryBossLevel) Dungeon.level).summonCells()){
				if (Actor.findChar( c ) == null && Dungeon.level.passable[c]) spots.add( c );
			}
		}
		if (spots.isEmpty()){
			for (int n : PathFinder.NEIGHBOURS8){
				int c = pos + n;
				if (Actor.findChar( c ) == null && Dungeon.level.passable[c]) spots.add( c );
			}
		}
		for (int i = 0; i < count && !spots.isEmpty(); i++){
			int c = spots.remove( Random.Int( spots.size() ) );
			Mob m = sentinels ? new RuneSentinel() : new StoneSoldier();
			m.pos = c;
			m.state = m.HUNTING;
			GameScene.add( m, 1f );
			CellEmitter.get( c ).burst( Speck.factory( Speck.ROCK ), 8 );
			if (m.sprite != null) m.sprite.emitter().burst( GoldSparkle.BURST, 8 );
		}
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
	}

	@Override
	public void die( Object cause ) {
		super.die( cause );

		//보상: 왕좌를 지키던 봉인된 엑스칼리버 (쓰러진 자리) + 성소의 보물
		ArrayList<Item> drops = new ArrayList<>();
		drops.add( new Excalibur() );
		drops.add( Generator.random( Random.Int( 2 ) == 0 ? Generator.Category.RING : Generator.Category.WAND ) );
		drops.add( Generator.random( Generator.Category.ARTIFACT ) );
		for (Item it : drops){
			if (it == null) continue;
			int cell = pos;
			for (int t = 0; t < 10 && !(it instanceof Excalibur); t++){
				int c = pos + PathFinder.NEIGHBOURS9[Random.Int( 9 )];
				if (Dungeon.level.passable[c]) { cell = c; break; }
			}
			Dungeon.level.drop( it, cell ).sprite.drop( pos );
		}

		//남아 있는 석상들도 함께 무너짐
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m != this && (m instanceof StoneSoldier || m instanceof RuneSentinel || m instanceof StoneWarden)){
				m.die( null );
			}
		}

		yell( Messages.get( this, "defeated" ) );
		GameScene.bossSlain();
		GLog.p( Messages.get( this, "excalibur" ) );
		if (Dungeon.level instanceof SanctuaryBossLevel){
			((SanctuaryBossLevel) Dungeon.level).onBossDefeated();
		}
	}

	@Override
	public void notice() {
		if (awake) super.notice();
	}

	private static final String AWAKE = "awake";
	private static final String PHASE = "phase";
	private static final String QUAKE = "quake_cd";
	private static final String BEAM_CD = "beam_cd";
	private static final String CHARGING = "charging";
	private static final String BEAM_TARGET = "beam_target";
	private static final String LAST = "last_skill";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( AWAKE, awake );
		bundle.put( PHASE, phase );
		bundle.put( QUAKE, quakeCD );
		bundle.put( BEAM_CD, beamCD );
		bundle.put( CHARGING, beamCharging );
		bundle.put( BEAM_TARGET, beamTarget );
		bundle.put( LAST, lastSkill );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		awake = bundle.getBoolean( AWAKE );
		phase = Math.max( 1, bundle.getInt( PHASE ) );
		quakeCD = bundle.getInt( QUAKE );
		beamCD = bundle.getInt( BEAM_CD );
		beamCharging = bundle.getBoolean( CHARGING );
		beamTarget = bundle.contains( BEAM_TARGET ) ? bundle.getInt( BEAM_TARGET ) : -1;
		lastSkill = bundle.getInt( LAST );
		if (beamCharging && beamTarget < 0) beamCharging = false;
		if (awake){
			BossHealthBar.assignBoss( this );
			if (phase >= 3) BossHealthBar.bleed( true );
		}
	}
}
