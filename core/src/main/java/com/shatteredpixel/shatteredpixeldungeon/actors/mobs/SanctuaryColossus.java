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
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.GoldSparkle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ColossusSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//성소의 수호 거상. 금고의 엑스칼리버를 지키는 마지막 수호자. 부하 없이 1대 1로 싸움.
//
// 기믹 - 봉인의 갑주: 평소에는 받는 피해가 1/4로 줄어듦.
//   거상이 룬 광선을 모을 때(겨눈 줄은 고정됨) 그 줄 끝에 '룬 거울 수호상'이 있으면, 광선이 되돌아와 핵이 과부하됨.
//   과부하된 동안(8턴)은 무릎을 꿇고 움직이지 못하며 피해를 온전히 받음.
//   → 거상과 거울 사이에 서서 광선을 끌어낸 뒤 옆으로 피하는 것이 공략법
//
// 공격
//   - 내려찍기: 맞으면 멀리 튕겨 나감 (붙어서 버티기 어렵게)
//   - 룬 광선: 한 턴 겨눈 뒤 일직선 관통. 체력 절반 이하에서는 세 갈래 부채꼴
//   - 바위 던지기: 바닥의 바위를 뽑아 두 턴 뒤 표시한 칸에 던짐 (가운데 큰 피해, 둘레 절반). 절반 이하에서는 두 개
public class SanctuaryColossus extends Mob {

	{
		spriteClass = ColossusSprite.class;

		HP = HT = 450;
		defenseSkill = 18;

		EXP = 60;
		maxLvl = -2;

		state = PASSIVE;

		properties.add( Property.BOSS );
		properties.add( Property.INORGANIC );
	}

	private static final int OVERLOAD_TURNS = 8;

	private boolean awake = false;
	private int phase = 1;
	private int beamCD = 3;
	private int rockCD = 1;
	private int lastSkill = 0;   //1: 바위, 2: 광선
	private boolean beamCharging = false;
	private int[] beamTargets = new int[0];
	private boolean rockHeld = false;
	private int[] rockTargets = new int[0];
	private int overload = 0;

	private ArrayList<Ballistica> beamPaths;

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 22, 34 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 36;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 10 );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return overload > 0 ? 0 : super.defenseSkill( enemy );
	}

	public boolean isAwake(){ return awake; }
	public boolean isCharging(){ return beamCharging; }
	public boolean isHoldingRock(){ return rockHeld; }
	public boolean isOverloaded(){ return overload > 0; }
	public int phase(){ return phase; }

	private ColossusSprite spr(){
		return sprite instanceof ColossusSprite ? (ColossusSprite) sprite : null;
	}

	//대전당에 들어온 영웅을 보고 깨어남
	public void awaken(){
		if (awake) return;
		awake = true;
		state = HUNTING;
		enemy = Dungeon.hero;
		target = Dungeon.hero.pos;
		BossHealthBar.assignBoss( this );
		yell( Messages.get( this, "notice" ) );
		GLog.w( Messages.get( this, "hint" ) );
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

		if (overload > 0){
			overload--;
			if (overload == 0){
				if (spr() != null) spr().forceIdle();
				if (sprite != null) sprite.showStatus( CharSprite.WARNING, Messages.get( this, "recovered" ) );
				beamCD = Math.max( beamCD, 2 );
			}
			spend( TICK );
			return true;
		}

		if (beamCharging) return fireBeam();
		if (rockHeld) return throwRocks();

		if (beamCD > 0) beamCD--;
		if (rockCD > 0) rockCD--;

		Char hero = Dungeon.hero;
		boolean sees = hero.isAlive() && hero.invisible <= 0
				&& fieldOfView != null && fieldOfView.length == Dungeon.level.length() && fieldOfView[hero.pos];
		int dist = Dungeon.level.distance( pos, hero.pos );

		boolean beamReady = beamCD <= 0 && sees && dist >= 2;
		boolean rockReady = rockCD <= 0 && sees && dist >= 2;

		if (beamReady && (!rockReady || lastSkill != 2)){
			chargeBeam( hero.pos );
			return true;
		}
		if (rockReady){
			liftRocks( hero.pos );
			return true;
		}
		return super.act();
	}

	//==== 내려찍기: 멀리 튕겨 냄 ====

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (enemy.isAlive()){
			Ballistica away = new Ballistica( enemy.pos, enemy.pos + (enemy.pos - pos), Ballistica.MAGIC_BOLT );
			WandOfBlastWave.throwChar( enemy, away, 3, false, false, this );
		}
		return damage;
	}

	//==== 룬 광선 ====

	public static class RuneBeam {}

	private void chargeBeam( int target ){
		lastSkill = 2;
		beamCharging = true;

		ArrayList<Integer> targets = new ArrayList<>();
		targets.add( target );
		if (phase >= 2){
			int w = Dungeon.level.width();
			double ang = Math.atan2( target / w - pos / w, target % w - pos % w );
			for (double off : new double[]{ -0.45, 0.45 }){
				int tx = pos % w + (int)Math.round( Math.cos( ang + off ) * 7 );
				int ty = pos / w + (int)Math.round( Math.sin( ang + off ) * 7 );
				if (tx < 1 || ty < 1 || tx >= w - 1 || ty >= Dungeon.level.height() - 1) continue;
				int c = tx + ty * w;
				if (c != pos && !targets.contains( c )) targets.add( c );
			}
		}
		beamTargets = new int[targets.size()];
		for (int i = 0; i < beamTargets.length; i++) beamTargets[i] = targets.get( i );

		float delay = GameMath.gate( TICK, (float)Math.ceil( Dungeon.hero.cooldown() ), 2 * TICK );
		for (int t : beamTargets){
			Ballistica aim = new Ballistica( pos, t, Ballistica.STOP_SOLID );
			for (int c : aim.subPath( 1, aim.dist )){
				TargetedCell tc = GameScene.targetedCell( c, delay );
				if (tc != null) tc.hardlight( 0x40E8D8 );
			}
			//줄 끝이 룬 거울이면 거울이 반짝임
			int m = mirrorBehind( aim );
			if (m >= 0) CellEmitter.center( m ).burst( GoldSparkle.BURST, 8 );
		}

		if (spr() != null) spr().charge( target );
		GLog.w( Messages.get( this, "beam_charge" ) );
		Dungeon.hero.interrupt();
		spend( delay );
	}

	private int mirrorBehind( Ballistica b ){
		if (!(Dungeon.level instanceof SanctuaryBossLevel)) return -1;
		if (b.path.size() > b.dist + 1){
			int c = b.path.get( b.dist + 1 );
			if (((SanctuaryBossLevel) Dungeon.level).isMirror( c )) return c;
		}
		return -1;
	}

	private boolean fireBeam(){
		beamCharging = false;
		beamCD = phase >= 2 ? 4 : 5;
		beamPaths = new ArrayList<>();
		int[] ends = new int[beamTargets.length];
		boolean seen = Dungeon.level.heroFOV[pos];
		for (int i = 0; i < beamTargets.length; i++){
			Ballistica b = new Ballistica( pos, beamTargets[i], Ballistica.STOP_SOLID );
			beamPaths.add( b );
			ends[i] = b.collisionPos;
			seen = seen || Dungeon.level.heroFOV[b.collisionPos];
		}
		spend( TICK );

		if (spr() != null && sprite.visible && seen){
			spr().blast( ends );
			return false;
		} else {
			resolveBeam();
			return true;
		}
	}

	//광선이 지나간 줄 위의 모두에게 피해. 룬 거울에 닿은 광선은 되돌아와 핵을 과부하시킴
	public void resolveBeam(){
		if (beamPaths == null) return;
		int mirror = -1;
		for (Ballistica b : beamPaths){
			for (int c : b.subPath( 1, b.dist )){
				if (Dungeon.level.heroFOV[c]) CellEmitter.center( c ).burst( RuneMote.FACTORY, 3 );
				Char ch = Actor.findChar( c );
				if (ch == null || ch == this) continue;
				ch.damage( Random.NormalIntRange( 30, 42 ), new RuneBeam() );
				if (ch.sprite != null && Dungeon.level.heroFOV[c]) ch.sprite.flash();
				if (ch == Dungeon.hero && !ch.isAlive()){
					Dungeon.fail( this );
					GLog.n( Messages.get( this, "beam_kill" ) );
				}
			}
			int m = mirrorBehind( b );
			if (m >= 0) mirror = m;
		}
		beamPaths = null;
		beamTargets = new int[0];
		if (mirror >= 0 && isAlive()) startOverload( mirror );
	}

	private void startOverload( int mirror ){
		overload = OVERLOAD_TURNS;
		rockHeld = false;
		beamCharging = false;
		CellEmitter.center( mirror ).burst( GoldSparkle.BURST, 14 );
		Sample.INSTANCE.play( Assets.Sounds.SHATTER );
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		GameScene.flash( 0x4040E8D8 );
		if (spr() != null){
			spr().reflect( mirror );
			spr().overload();
			sprite.showStatus( CharSprite.POSITIVE, Messages.get( this, "overload" ) );
		}
		yell( Messages.get( this, "overload_yell" ) );
		GLog.p( Messages.get( this, "overload_log" ) );
	}

	//==== 바위 던지기 ====

	private void liftRocks( int target ){
		lastSkill = 1;
		rockHeld = true;

		ArrayList<Integer> targets = new ArrayList<>();
		targets.add( target );
		if (phase >= 2){
			ArrayList<Integer> around = new ArrayList<>();
			for (int i = 0; i < Dungeon.level.length(); i++){
				if (Dungeon.level.distance( target, i ) == 3 && Dungeon.level.passable[i] && i != pos) around.add( i );
			}
			if (!around.isEmpty()) targets.add( Random.element( around ) );
		}
		rockTargets = new int[targets.size()];
		for (int i = 0; i < rockTargets.length; i++) rockTargets[i] = targets.get( i );

		//두 턴 동안 들고 버팀: 그 사이 두 걸음이면 표시된 3x3 밖으로 벗어날 수 있음
		float delay = 2 * TICK;
		for (int t : rockTargets){
			GameScene.targetedCell( t, delay );
			for (int n : PathFinder.NEIGHBOURS8){
				if (!Dungeon.level.solid[t + n]){
					TargetedCell tc = GameScene.targetedCell( t + n, delay );
					if (tc != null) tc.alpha( 0.5f );
				}
			}
		}

		if (spr() != null) spr().liftRock( target );
		GLog.w( Messages.get( this, "rock_lift" ) );
		Dungeon.hero.interrupt();
		spend( delay );
	}

	private boolean throwRocks(){
		rockHeld = false;
		rockCD = phase >= 2 ? 3 : 4;
		spend( TICK );

		final int[] targets = rockTargets;
		rockTargets = new int[0];
		if (targets.length == 0) return true;

		if (spr() != null && sprite.visible && sprite.parent != null){
			spr().hurlRock( targets[0] );
			Sample.INSTANCE.play( Assets.Sounds.MISS, 1f, 0.7f );
			for (int i = 0; i < targets.length; i++){
				final boolean last = i == targets.length - 1;
				final int cell = targets[i];
				((MissileSprite) sprite.parent.recycle( MissileSprite.class ))
						.reset( sprite, cell, new GnollGeomancer.Boulder(), new Callback() {
							@Override
							public void call() {
								landRock( cell );
								if (last) next();
							}
						} );
			}
			return false;
		} else {
			for (int c : targets) landRock( c );
			return true;
		}
	}

	private void landRock( int cell ){
		Splash.at( cell, 0x8C8E78, 20 );
		CellEmitter.get( cell ).burst( Speck.factory( Speck.ROCK ), 8 );
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		PixelScene.shake( 3, 0.5f );

		for (int n : PathFinder.NEIGHBOURS9){
			Char ch = Actor.findChar( cell + n );
			if (ch == null || ch == this) continue;
			int dmg = Random.NormalIntRange( 26, 38 );
			if (n != 0) dmg /= 2;
			ch.damage( dmg, new GnollGeomancer.Boulder() );
			if (ch == Dungeon.hero && !ch.isAlive()){
				Dungeon.fail( this );
				GLog.n( Messages.get( this, "rock_kill" ) );
			}
		}
	}

	//==== 피해와 단계 ====

	@Override
	public void damage( int dmg, Object src ) {
		if (!awake) awaken();
		if (overload <= 0 && dmg > 0){
			//봉인의 갑주
			dmg = Math.max( 1, Math.round( dmg * 0.25f ) );
			if (sprite != null && Random.Int( 3 ) == 0){
				sprite.showStatus( CharSprite.NEUTRAL, Messages.get( this, "armored" ) );
			}
		}
		super.damage( dmg, src );
		if (!isAlive()) return;
		if (phase == 1 && HP <= HT / 2){
			phase = 2;
			yell( Messages.get( this, "phase2" ) );
			BossHealthBar.bleed( true );
			PixelScene.shake( 3, 0.8f );
			GameScene.flash( 0x3040E8D8 );
			if (sprite != null){
				sprite.emitter().burst( RuneMote.FACTORY, 30 );
				sprite.emitter().burst( GoldSparkle.BURST, 16 );
			}
		}
	}

	@Override
	public void die( Object cause ) {
		super.die( cause );

		//성소의 보물 (엑스칼리버는 열린 금고의 제단에)
		ArrayList<Item> drops = new ArrayList<>();
		drops.add( Generator.random( Random.Int( 2 ) == 0 ? Generator.Category.RING : Generator.Category.WAND ) );
		drops.add( Generator.random( Generator.Category.ARTIFACT ) );
		drops.add( new com.shatteredpixel.shatteredpixeldungeon.items.quest.GolemHeart() );
		for (Item it : drops){
			if (it == null) continue;
			int cell = pos;
			for (int t = 0; t < 10; t++){
				int c = pos + PathFinder.NEIGHBOURS9[Random.Int( 9 )];
				if (Dungeon.level.passable[c]) { cell = c; break; }
			}
			Dungeon.level.drop( it, cell ).sprite.drop( pos );
		}

		yell( Messages.get( this, "defeated" ) );
		GameScene.bossSlain();
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
	private static final String BEAM_CD = "beam_cd";
	private static final String ROCK_CD = "rock_cd";
	private static final String CHARGING = "charging";
	private static final String BEAM_TARGETS = "beam_targets";
	private static final String ROCK_HELD = "rock_held";
	private static final String ROCK_TARGETS = "rock_targets";
	private static final String OVERLOAD = "overload";
	private static final String LAST = "last_skill";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( AWAKE, awake );
		bundle.put( PHASE, phase );
		bundle.put( BEAM_CD, beamCD );
		bundle.put( ROCK_CD, rockCD );
		bundle.put( CHARGING, beamCharging );
		bundle.put( BEAM_TARGETS, beamTargets );
		bundle.put( ROCK_HELD, rockHeld );
		bundle.put( ROCK_TARGETS, rockTargets );
		bundle.put( OVERLOAD, overload );
		bundle.put( LAST, lastSkill );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		awake = bundle.getBoolean( AWAKE );
		phase = Math.max( 1, Math.min( 2, bundle.getInt( PHASE ) ) );
		beamCD = bundle.getInt( BEAM_CD );
		rockCD = bundle.getInt( ROCK_CD );
		beamCharging = bundle.getBoolean( CHARGING );
		beamTargets = bundle.contains( BEAM_TARGETS ) ? bundle.getIntArray( BEAM_TARGETS ) : new int[0];
		rockHeld = bundle.getBoolean( ROCK_HELD );
		rockTargets = bundle.contains( ROCK_TARGETS ) ? bundle.getIntArray( ROCK_TARGETS ) : new int[0];
		overload = bundle.getInt( OVERLOAD );
		lastSkill = bundle.getInt( LAST );
		if (beamCharging && beamTargets.length == 0) beamCharging = false;
		if (rockHeld && rockTargets.length == 0) rockHeld = false;
		if (awake){
			BossHealthBar.assignBoss( this );
			if (phase >= 2) BossHealthBar.bleed( true );
		}
	}
}
