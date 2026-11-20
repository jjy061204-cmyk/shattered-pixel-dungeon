/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 15층 보스: 놀 대족장 (원작 DM-300과 같은 예산: 체력, 무적 구간 2번, 3단계, 전리품, 보스 점수)
 *  1단계 (100~66%): 왕좌에서 '투창 세례'. 영웅이 선 칸과 둘레 두 칸을 1턴 전에 표시하고 투창 세 자루를 던짐 (표시된 칸만 피하면 됨)
 *  결집 (66%·33%): 가까운 전투 깃발로 달려가 깃발이 서 있는 동안 무적. 깃발과 대족장 사이에 붉은 빛줄기가 이어짐. 깃발을 부수면 풀림
 *  2단계 (66~33%): 왕좌에서 내려와 드릴 도끼로 싸움.
 *     돌진: 2턴 예고(가는 길 전체 표시) 뒤 직선 돌진, 벽·방책에 박히면 2턴 기절
 *     회오리: 붙어서 2턴 넘게 싸우면 둘레 8칸을 1턴 전에 표시하고 드릴 도끼를 크게 돌림
 *  3단계 (33% 아래): 최후의 함성. 방책에 불이 붙고 5턴마다 불길이 한 칸씩 좁혀 옴(최대 3칸, 1턴 전에 불씨로 예고). 대족장은 빨라짐
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpireBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollWarlordSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollWarlord extends Mob {

	{
		spriteClass = GnollWarlordSprite.class;

		HP = HT = Dungeon.isChallenged( Challenges.STRONGER_BOSSES ) ? 400 : 300;
		EXP = 30;
		defenseSkill = 15;

		properties.add( Property.BOSS );

		HUNTING = new WarlordHunting();
		state = PASSIVE; //왕좌에서 기다림
	}

	public int ralliesDone = 0;
	public boolean rallying = false;
	private int bannerId = -1;
	public boolean onThrone = true;
	public boolean finalStand = false;
	public boolean awake = false;

	private int javelinCooldown = 2;
	private int[] volley = null;       //투창 세례로 표시해 둔 칸들
	private int whirlCooldown = 4;
	private boolean whirlAimed = false;
	private int adjacentTurns = 0;
	private boolean whirlWarned = false;
	private int chargeCooldown = 3;
	private int chargeAim = -1;

	//전투 시계 (보스 전용 버프가 매 턴 올림)
	private int reinforceTimer = 6;
	private int ringTimer = 0;
	public int ringLevel = 0;

	private boolean invulnWarned = false;
	private boolean javelinWarned = false;
	private boolean chargeWarned = false;

	public int totalRallies(){
		return Dungeon.isChallenged( Challenges.STRONGER_BOSSES ) ? 3 : 2;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 15, 25 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 20;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 10 );
	}

	@Override
	public float speed() {
		return super.speed() * (finalStand ? 1.25f : 1f);
	}

	//왕좌에 앉아 있거나, 깃발 옆에서 결집 중이면 자리를 지킴
	private boolean holdingPosition(){
		if (onThrone) return true;
		if (rallying){
			Char b = banner();
			return b != null && Dungeon.level.adjacent( pos, b.pos );
		}
		return false;
	}

	private WarBanner banner(){
		if (bannerId == -1) return null;
		Actor a = Actor.findById( bannerId );
		return (a instanceof WarBanner && ((WarBanner) a).isAlive()) ? (WarBanner) a : null;
	}

	// ── 싸움 시작 ──

	public void awaken(){
		if (awake) return;
		awake = true;
		state = HUNTING;
		notice();
		Buff.affect( this, BattleClock.class );
		if (Dungeon.level instanceof GnollEmpireBossLevel){
			((GnollEmpireBossLevel) Dungeon.level).onFightStart();
		}
	}

	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()){
			BossHealthBar.assignBoss( this );
			yell( Messages.get( this, "notice" ) );
			for (Char ch : Actor.chars()){
				if (ch instanceof DriedRose.GhostHero){
					((DriedRose.GhostHero) ch).sayBoss();
				}
			}
		}
	}

	@Override
	protected boolean act() {
		if (!awake){
			return super.act();
		}
		if (paralysed > 0){
			return super.act();
		}

		//예고해 둔 공격부터
		if (volley != null) return throwVolley();
		if (chargeAim != -1) return charge();
		if (whirlAimed) return whirl();

		if (javelinCooldown > 0) javelinCooldown--;
		if (chargeCooldown > 0) chargeCooldown--;
		if (whirlCooldown > 0) whirlCooldown--;

		return super.act();
	}

	@Override
	protected boolean getCloser( int target ) {
		if (onThrone) return false;
		if (rallying){
			WarBanner b = banner();
			if (b != null){
				if (Dungeon.level.adjacent( pos, b.pos )) return false;
				int spot = -1;
				for (int n : PathFinder.NEIGHBOURS8){
					int c = b.pos + n;
					if (Dungeon.level.passable[c] && Actor.findChar( c ) == null
							&& (spot == -1 || Dungeon.level.trueDistance( pos, c ) < Dungeon.level.trueDistance( pos, spot ))){
						spot = c;
					}
				}
				if (spot != -1) return super.getCloser( spot );
			}
		}
		return super.getCloser( target );
	}

	private class WarlordHunting extends Mob.Hunting {

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;

			//결집하러 깃발로 달려가는 중
			if (rallying && !holdingPosition()){
				int oldPos = pos;
				if (getCloser( pos )){
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				}
			}

			if (enemyInFOV && !isCharmedBy( enemy )){
				if (holdingPosition()){
					//왕좌·깃발 자리: 붙어 오면 베고, 아니면 투창
					if (canAttack( enemy )){
						target = enemy.pos;
						return doAttack( enemy );
					}
					if (javelinCooldown <= 0){
						aimVolley( enemy );
						return true;
					}
					spend( TICK );
					return true;
				}

				//2·3단계: 붙어서 오래 버티면 회오리
				if (Dungeon.level.adjacent( pos, enemy.pos )){
					adjacentTurns++;
					if (whirlCooldown <= 0 && adjacentTurns >= 2){
						aimWhirl();
						return true;
					}
				} else {
					adjacentTurns = 0;
				}

				//2·3단계: 거리가 있으면 돌진
				if (chargeCooldown <= 0 && !rooted
						&& Dungeon.level.distance( pos, enemy.pos ) >= 2 && Dungeon.level.distance( pos, enemy.pos ) <= 6){
					if (aimCharge( enemy )) return true;
				}
			}

			return super.act( enemyInFOV, justAlerted );
		}
	}

	// ── 투창 세례: 영웅이 선 칸과 둘레 두 칸 ──

	private void aimVolley( Char target ){
		ArrayList<Integer> cells = new ArrayList<>();
		cells.add( target.pos );
		ArrayList<Integer> around = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int c = target.pos + n;
			if (c != pos && Dungeon.level.passable[c]) around.add( c );
		}
		Random.shuffle( around );
		for (int i = 0; i < Math.min( 2, around.size() ); i++) cells.add( around.get( i ) );

		//실제로 창이 닿는 칸을 표시 (도중에 막히면 막힌 자리)
		ArrayList<Integer> marks = new ArrayList<>();
		for (int c : cells){
			int hit = new Ballistica( pos, c, Ballistica.PROJECTILE ).collisionPos;
			if (!marks.contains( hit )) marks.add( hit );
		}
		volley = new int[marks.size()];
		for (int i = 0; i < volley.length; i++){
			volley[i] = marks.get( i );
			GameScene.targetedCell( volley[i], 1f );
		}
		if (sprite != null){
			sprite.turnTo( pos, target.pos );
			sprite.showStatus( CharSprite.WARNING, Messages.get( this, "volley" ) );
			if (sprite instanceof GnollWarlordSprite) ((GnollWarlordSprite) sprite).aim();
		}
		if (!javelinWarned && Dungeon.level.heroFOV[pos]){
			javelinWarned = true;
			GLog.w( Messages.get( this, "javelin_warn" ) );
		}
		Dungeon.hero.interrupt();
		spend( 1f );
	}

	private boolean throwVolley(){
		final int[] cells = volley;
		volley = null;
		javelinCooldown = 3;
		spend( attackDelay() );

		boolean seen = sprite != null && sprite.visible;
		for (int c : cells) seen |= Dungeon.level.heroFOV[c];
		if (sprite instanceof GnollWarlordSprite && seen){
			final int[] left = { cells.length };
			((GnollWarlordSprite) sprite).throwVolley( cells, new GnollWarlordSprite.Landing() {
				@Override
				public void landed( int cell ) {
					javelinLands( cell );
					left[0]--;
					if (left[0] == 0) next();
				}
			} );
			return false;
		}
		for (int c : cells) javelinLands( c );
		return true;
	}

	private void javelinLands( int cell ){
		if (Dungeon.level instanceof GnollEmpireBossLevel){
			((GnollEmpireBossLevel) Dungeon.level).stuckJavelin( cell );
		}
		Char ch = Actor.findChar( cell );
		if (ch == null || ch == this || !ch.isAlive() || ch instanceof WarBanner) return;
		if (ch.alignment == alignment) return; //아군 놀은 맞히지 않음
		if (hit( this, ch, Char.INFINITE_ACCURACY, false )){
			int dmg = Random.NormalIntRange( 12, 22 ) - ch.drRoll();
			ch.damage( Math.max( 1, dmg ), this );
			Sample.INSTANCE.play( Assets.Sounds.HIT_STAB );
			if (ch == Dungeon.hero){
				Statistics.qualifiedForBossChallengeBadge = false;
				Statistics.bossScores[2] -= 100;
				if (!ch.isAlive()){
					Dungeon.fail( this );
					GLog.n( Messages.get( this, "javelin_kill" ) );
				}
			}
		} else {
			ch.sprite.showStatus( CharSprite.NEUTRAL, ch.defenseVerb() );
		}
	}

	// ── 돌진 ──

	private boolean aimCharge( Char target ){
		Ballistica b = new Ballistica( pos, target.pos, Ballistica.STOP_SOLID );
		if (b.dist < 2) return false;
		chargeAim = b.collisionPos;
		for (int c : b.subPath( 1, b.dist )){
			GameScene.targetedCell( c, 2f );
		}
		if (sprite != null){
			sprite.turnTo( pos, target.pos );
			sprite.showStatus( CharSprite.WARNING, Messages.get( this, "revving" ) );
			if (sprite instanceof GnollWarlordSprite) ((GnollWarlordSprite) sprite).rev();
		}
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 1f, 0.8f );
		if (!chargeWarned && Dungeon.level.heroFOV[pos]){
			chargeWarned = true;
			GLog.w( Messages.get( this, "charge_warn" ) );
		}
		Dungeon.hero.interrupt();
		spend( 2f );
		return true;
	}

	private boolean charge(){
		final int aim = chargeAim;
		chargeAim = -1;
		chargeCooldown = finalStand ? 4 : 6;

		Ballistica b = new Ballistica( pos, aim, Ballistica.STOP_SOLID | Ballistica.STOP_CHARS );
		int coll = b.collisionPos;
		Char c = Actor.findChar( coll );
		final Char victim;
		final int end;
		final int wall;
		if (c != null && c != this){
			//길 위의 캐릭터에 부딪힘: 그 앞 칸에서 멈춤
			victim = c;
			end = b.path.get( Math.max( 0, b.dist - 1 ) );
			wall = -1;
		} else if (Dungeon.level.solid[coll]){
			//닫힌 문처럼 지나갈 수 있는 단단한 칸
			victim = null;
			end = b.path.get( Math.max( 0, b.dist - 1 ) );
			wall = coll;
		} else {
			//벽 바로 앞 칸까지 달림. 벽은 경로의 다음 칸
			victim = null;
			end = coll;
			wall = b.dist + 1 < b.path.size() ? b.path.get( b.dist + 1 ) : -1;
		}
		final boolean hitWall = victim == null && wall != -1 && Dungeon.level.solid[wall];
		final int impact = hitWall ? wall : coll;

		spend( 1f );
		if (end == pos){
			land( end, victim, hitWall, impact );
			return true;
		}
		if (sprite != null && (sprite.visible || Dungeon.level.heroFOV[end])){
			sprite.jump( pos, end, 0, 0.06f * Math.max( 1, b.dist ), new Callback() {
				@Override
				public void call() {
					land( end, victim, hitWall, impact );
					next();
				}
			} );
			return false;
		}
		land( end, victim, hitWall, impact );
		return true;
	}

	private void land( int end, Char victim, boolean hitWall, int coll ){
		pos = end;
		if (sprite != null){
			sprite.place( end );
			sprite.idle();
		}
		Dungeon.level.occupyCell( this );

		if (victim != null && victim.isAlive() && victim.alignment != alignment && !(victim instanceof WarBanner)){
			if (hit( this, victim, Char.INFINITE_ACCURACY, false )){
				int dmg = Random.NormalIntRange( 20, 32 ) - victim.drRoll();
				victim.damage( Math.max( 1, dmg ), this );
				if (victim.sprite != null) victim.sprite.flash();
				Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG );
				PixelScene.shake( 2, 0.3f );
				if (victim == Dungeon.hero){
					Statistics.qualifiedForBossChallengeBadge = false;
					Statistics.bossScores[2] -= 150;
					if (!victim.isAlive()){
						Dungeon.fail( this );
						GLog.n( Messages.get( this, "charge_kill" ) );
					}
				}
			} else if (victim.sprite != null){
				victim.sprite.showStatus( CharSprite.NEUTRAL, victim.defenseVerb() );
			}
		} else if (hitWall){
			//벽이나 방책에 박힘: 잠시 기절
			Sample.INSTANCE.play( Assets.Sounds.ROCKS );
			PixelScene.shake( 3, 0.5f );
			if (Dungeon.level.heroFOV[coll]){
				CellEmitter.get( coll ).burst( Speck.factory( Speck.ROCK ), 8 );
			}
			if (Dungeon.level.map[coll] == Terrain.BARRICADE){
				//드릴 도끼에 방책이 부서짐
				Dungeon.level.destroy( coll );
				GameScene.updateMap( coll );
				Dungeon.observe();
			}
			Buff.prolong( this, Paralysis.class, 2f );
			if (sprite != null) sprite.showStatus( CharSprite.NEUTRAL, Messages.get( this, "stunned" ) );
		}
	}

	// ── 드릴 회오리: 둘레 8칸 ──

	private void aimWhirl(){
		whirlAimed = true;
		for (int n : PathFinder.NEIGHBOURS8){
			int c = pos + n;
			if (!Dungeon.level.solid[c]) GameScene.targetedCell( c, 1f );
		}
		if (sprite != null){
			sprite.showStatus( CharSprite.WARNING, Messages.get( this, "whirl" ) );
			if (sprite instanceof GnollWarlordSprite) ((GnollWarlordSprite) sprite).rev();
		}
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 1f, 1.2f );
		if (!whirlWarned && Dungeon.level.heroFOV[pos]){
			whirlWarned = true;
			GLog.w( Messages.get( this, "whirl_warn" ) );
		}
		Dungeon.hero.interrupt();
		spend( 1f );
	}

	private boolean whirl(){
		whirlAimed = false;
		whirlCooldown = finalStand ? 5 : 7;
		adjacentTurns = 0;
		spend( attackDelay() );
		if (sprite instanceof GnollWarlordSprite) ((GnollWarlordSprite) sprite).spin();
		Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG, 1f, 0.8f );
		PixelScene.shake( 2, 0.4f );
		for (int n : PathFinder.NEIGHBOURS8){
			int c = pos + n;
			if (Dungeon.level.heroFOV[c] && !Dungeon.level.solid[c]){
				CellEmitter.center( c ).burst( SparkParticle.FACTORY, 3 );
			}
			Char ch = Actor.findChar( c );
			if (ch == null || !ch.isAlive() || ch.alignment == alignment || ch instanceof WarBanner) continue;
			if (hit( this, ch, Char.INFINITE_ACCURACY, false )){
				int dmg = Random.NormalIntRange( 14, 24 ) - ch.drRoll();
				ch.damage( Math.max( 1, dmg ), this );
				if (ch.sprite != null) ch.sprite.flash();
				if (ch == Dungeon.hero){
					Statistics.qualifiedForBossChallengeBadge = false;
					Statistics.bossScores[2] -= 100;
					if (!ch.isAlive()){
						Dungeon.fail( this );
						GLog.n( Messages.get( this, "whirl_kill" ) );
					}
				}
			} else if (ch.sprite != null){
				ch.sprite.showStatus( CharSprite.NEUTRAL, ch.defenseVerb() );
			}
		}
		return true;
	}

	// ── 결집 ──

	@Override
	public void damage( int dmg, Object src ) {
		if (!awake){
			awaken();
		}
		if (!BossHealthBar.isAssigned()){
			notice();
		}

		int preHP = HP;
		super.damage( dmg, src );
		if (isInvulnerable( src.getClass() )){
			return;
		}

		int dmgTaken = preHP - HP;
		if (dmgTaken > 0){
			LockedFloor lock = Dungeon.hero.buff( LockedFloor.class );
			if (lock != null && !isImmune( src.getClass() )){
				if (Dungeon.isChallenged( Challenges.STRONGER_BOSSES )) lock.addTime( dmgTaken/2f );
				else                                                    lock.addTime( dmgTaken );
			}
		}

		int threshold;
		if (Dungeon.isChallenged( Challenges.STRONGER_BOSSES )){
			threshold = HT / 4 * (3 - ralliesDone);
		} else {
			threshold = HT / 3 * (2 - ralliesDone);
		}

		if (HP <= threshold && threshold > 0){
			HP = threshold;
			startRally();
		}
	}

	@Override
	public boolean isAlive() {
		return super.isAlive() || ralliesDone < totalRallies();
	}

	@Override
	public boolean isInvulnerable( Class effect ) {
		boolean inv = rallying && banner() != null;
		if (inv && !invulnWarned){
			invulnWarned = true;
			GLog.w( Messages.get( this, "rally_hint" ) );
		}
		return inv || super.isInvulnerable( effect );
	}

	private void startRally(){
		ralliesDone++;
		volley = null;
		chargeAim = -1;
		whirlAimed = false;

		WarBanner best = null;
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m instanceof WarBanner && m.isAlive() && !((WarBanner) m).active()){
				if (best == null || Dungeon.level.trueDistance( pos, m.pos ) < Dungeon.level.trueDistance( pos, best.pos )){
					best = (WarBanner) m;
				}
			}
		}

		onThrone = false;
		if (best == null){
			//깃발이 남아 있지 않으면 결집 없이 다음 단계로
			afterRally();
			return;
		}

		rallying = true;
		bannerId = best.id();
		best.activate();
		invulnWarned = false;

		yell( Messages.get( this, ralliesDone >= totalRallies() ? "rally_last" : "rally" ) );
		sprite.showStatus( CharSprite.POSITIVE, Messages.get( this, "invulnerable" ) );
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE, 1f, 0.8f );
		if (sprite instanceof GnollWarlordSprite) ((GnollWarlordSprite) sprite).roar();
		javelinCooldown = 2;
		reinforce( 1, 2 );
		spend( 1f );
	}

	public void bannerFallen( WarBanner b ){
		if (!rallying || b.id() != bannerId) return;
		rallying = false;
		bannerId = -1;
		yell( Messages.get( this, "rally_broken" ) );
		afterRally();
	}

	private void afterRally(){
		if (ralliesDone >= totalRallies() && !finalStand){
			startFinalStand();
		}
	}

	private void startFinalStand(){
		finalStand = true;
		ringLevel = 0;
		ringTimer = 0;
		chargeCooldown = 2;
		yell( Messages.get( this, "final_stand" ) );
		BossHealthBar.bleed( true );
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
		PixelScene.shake( 2, 1f );
		if (Dungeon.level instanceof GnollEmpireBossLevel){
			((GnollEmpireBossLevel) Dungeon.level).ignitePalisades();
		}
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				Music.INSTANCE.fadeOut( 0.5f, new Callback() {
					@Override
					public void call() {
						Music.INSTANCE.play( Assets.Music.CAVES_BOSS_FINALE, true );
					}
				} );
			}
		} );
	}

	// ── 전투 시계: 증원과 좁혀 오는 불길 (매 턴) ──

	private void tick(){
		if (!isAlive() || !awake) return;

		//결집 중: 깃발과 대족장 사이에 붉은 빛줄기 (깃발이 힘을 주고 있다는 표시)
		if (rallying){
			WarBanner b = banner();
			if (b != null && sprite != null && b.sprite != null && sprite.parent != null
					&& (sprite.visible || b.sprite.visible)){
				com.shatteredpixel.shatteredpixeldungeon.effects.Beam beam =
						new com.shatteredpixel.shatteredpixeldungeon.effects.Beam.LightRay( b.sprite.center(), sprite.center() );
				beam.hardlight( 1f, 0.3f, 0.15f );
				sprite.parent.add( beam );
			}
		}

		if (finalStand){
			if (ringLevel < 3){
				ringTimer++;
				if (ringTimer == 4 && Dungeon.level instanceof GnollEmpireBossLevel){
					//한 턴 앞서 불씨로 다음 불길 자리를 알림
					((GnollEmpireBossLevel) Dungeon.level).warnFlames( ringLevel + 1 );
					GLog.w( Messages.get( this, "flames_warn" ) );
				}
				if (ringTimer >= 5){
					ringTimer = 0;
					ringLevel++;
					if (Dungeon.level instanceof GnollEmpireBossLevel){
						((GnollEmpireBossLevel) Dungeon.level).spreadFlames( ringLevel );
					}
					GLog.w( Messages.get( this, "flames_close" ) );
				}
			}
			return;
		}

		if (--reinforceTimer <= 0){
			reinforceTimer = rallying ? 7 : (onThrone ? 9 : 14);
			reinforce( 1, (onThrone || rallying) ? 2 : 1 );
		}
	}

	private int minionsAlive(){
		int n = 0;
		for (Mob m : Dungeon.level.mobs){
			if (m instanceof GnollScout && ((GnollScout) m).minion && m.isAlive()) n++;
		}
		return n;
	}

	//성문에서 놀 증원병이 들어옴 (동시에 cap 마리까지)
	private void reinforce( int count, int cap ){
		if (!(Dungeon.level instanceof GnollEmpireBossLevel)) return;
		int n = Math.min( count, cap - minionsAlive() );
		if (n <= 0) return;

		ArrayList<Integer> gates = new ArrayList<>( ((GnollEmpireBossLevel) Dungeon.level).gateCells() );
		Random.shuffle( gates );
		boolean shown = false;
		for (int g : gates){
			if (n <= 0) break;
			if (Actor.findChar( g ) != null) continue;
			GnollScout s = new GnollScout();
			s.makeMinion();
			s.pos = g;
			s.state = s.HUNTING;
			GameScene.add( s, 1f );
			Dungeon.level.occupyCell( s );
			s.beckon( Dungeon.hero.pos );
			if (Dungeon.level.heroFOV[g]){
				CellEmitter.get( g ).burst( Speck.factory( Speck.DUST ), 6 );
				if (sprite != null && sprite.parent != null){
					new com.shatteredpixel.shatteredpixeldungeon.effects.Flare( 5, 12 ).color( 0xFF4422, true )
							.show( sprite.parent, com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.tileCenterToWorld( g ), 0.8f );
				}
				shown = true;
			}
			n--;
		}
		if (shown){
			GLog.w( Messages.get( this, "reinforce" ) );
		}
	}

	// ── 죽음 ──

	@Override
	public void die( Object cause ) {

		super.die( cause );

		GameScene.bossSlain();

		//남은 증원병과 깃발도 함께 무너짐
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if ((m instanceof GnollScout && ((GnollScout) m).minion) || m instanceof WarBanner){
				m.die( null );
			}
		}

		if (Dungeon.level instanceof GnollEmpireBossLevel){
			((GnollEmpireBossLevel) Dungeon.level).onBossDefeated();
		}

		//DM-300과 같은 전리품: 금속 조각 2~4개 (평균 2.5)
		int shards = Random.chances( new float[]{0, 0, 6, 3, 1} );
		for (int i = 0; i < shards; i++){
			int ofs;
			int tries = 20;
			do {
				ofs = PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			} while (!Dungeon.level.passable[pos + ofs] && --tries > 0);
			if (!Dungeon.level.passable[pos + ofs]) ofs = 0;
			Dungeon.level.drop( new MetalShard(), pos + ofs ).sprite.drop( pos );
		}

		Badges.validateBossSlain();
		if (Statistics.qualifiedForBossChallengeBadge){
			Badges.validateBossChallengeCompleted();
		}
		Statistics.bossScores[2] += 3000;

		LloydsBeacon beacon = Dungeon.hero.belongings.getItem( LloydsBeacon.class );
		if (beacon != null){
			beacon.upgrade();
		}

		yell( Messages.get( this, "defeated" ) );
	}

	@Override
	public String description() {
		String desc = super.description();
		if (rallying) desc += "\n\n" + Messages.get( this, "desc_rally" );
		else if (finalStand) desc += "\n\n" + Messages.get( this, "desc_final" );
		return desc;
	}

	{
		immunities.add( Sleep.class );

		resistances.add( Terror.class );
		resistances.add( Charm.class );
		resistances.add( Vertigo.class );
		resistances.add( Cripple.class );
		resistances.add( Chill.class );
		resistances.add( Frost.class );
		resistances.add( Roots.class );
		resistances.add( Slow.class );
		resistances.add( Burning.class );
		resistances.add( Fire.class );
	}

	// ── 저장 ──

	private static final String RALLIES = "rallies";
	private static final String RALLYING = "rallying";
	private static final String BANNER = "banner";
	private static final String THRONE = "on_throne";
	private static final String FINAL = "final_stand";
	private static final String AWAKE = "awake";
	private static final String JAV_CD = "javelin_cd";
	private static final String VOLLEY = "volley";
	private static final String WHIRL_CD = "whirl_cd";
	private static final String WHIRL_AIM = "whirl_aim";
	private static final String ADJ = "adjacent_turns";
	private static final String CHG_CD = "charge_cd";
	private static final String CHG_AIM = "charge_aim";
	private static final String REINF = "reinforce";
	private static final String RING_T = "ring_timer";
	private static final String RING = "ring_level";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( RALLIES, ralliesDone );
		bundle.put( RALLYING, rallying );
		bundle.put( BANNER, bannerId );
		bundle.put( THRONE, onThrone );
		bundle.put( FINAL, finalStand );
		bundle.put( AWAKE, awake );
		bundle.put( JAV_CD, javelinCooldown );
		if (volley != null) bundle.put( VOLLEY, volley );
		bundle.put( WHIRL_CD, whirlCooldown );
		bundle.put( WHIRL_AIM, whirlAimed );
		bundle.put( ADJ, adjacentTurns );
		bundle.put( CHG_CD, chargeCooldown );
		bundle.put( CHG_AIM, chargeAim );
		bundle.put( REINF, reinforceTimer );
		bundle.put( RING_T, ringTimer );
		bundle.put( RING, ringLevel );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		ralliesDone = bundle.getInt( RALLIES );
		rallying = bundle.getBoolean( RALLYING );
		bannerId = bundle.getInt( BANNER );
		onThrone = bundle.getBoolean( THRONE );
		finalStand = bundle.getBoolean( FINAL );
		awake = bundle.getBoolean( AWAKE );
		javelinCooldown = bundle.getInt( JAV_CD );
		volley = bundle.contains( VOLLEY ) ? bundle.getIntArray( VOLLEY ) : null;
		whirlCooldown = bundle.getInt( WHIRL_CD );
		whirlAimed = bundle.getBoolean( WHIRL_AIM );
		adjacentTurns = bundle.getInt( ADJ );
		chargeCooldown = bundle.getInt( CHG_CD );
		chargeAim = bundle.getInt( CHG_AIM );
		reinforceTimer = bundle.getInt( REINF );
		ringTimer = bundle.getInt( RING_T );
		ringLevel = bundle.getInt( RING );

		if (awake){
			BossHealthBar.assignBoss( this );
			if (finalStand) BossHealthBar.bleed( true );
		}
	}

	//매 턴 보스의 전투 시계를 돌리는 숨은 버프
	public static class BattleClock extends Buff {

		@Override
		public boolean act() {
			if (target instanceof GnollWarlord){
				((GnollWarlord) target).tick();
			}
			spend( TICK );
			return true;
		}
	}
}
