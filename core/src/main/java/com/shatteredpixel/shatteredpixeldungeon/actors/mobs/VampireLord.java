/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 보스: 흡혈 군주 (15층 옥좌실). 체력·전리품·보스 점수는 DM-300과 같음
 *  - 1단계 (100~66%): 세 갈래 피의 화살(4턴마다)과 근접 흡혈. 어두운 칸에서 매 턴 조금씩 회복
 *  - 안개화 (66%와 33%): 안개가 되어 관 제단으로 돌아가 무적, 화로가 모두 꺼지고 박쥐 4~6마리.
 *      화로 2개에 불을 붙이면 빛에 끌려 나와 2턴 비틀거림
 *  - 2단계 (66~33%): 피의 사슬 — 3칸 안의 영웅을 묶고 매 턴 피를 빨아들임. 밝은 칸에 들어서면 끊김
 *  - 3단계 (33% 아래): 핏빛 달 — 달빛 창이 붉게 물들어 빛을 잃고, 화로가 5턴마다 하나씩 꺼짐. 어둠 속 회복 2배
 *  - 영웅이 뱀파이어면 등장·패배 대사만 다름
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NightBlood;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle;
import com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastleBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class VampireLord extends Mob {

	{
		spriteClass = VampireCastleSprites.VampireLord.class;

		HP = HT = Dungeon.isChallenged( Challenges.STRONGER_BOSSES ) ? 400 : 300;
		EXP = 30;
		defenseSkill = 15;

		properties.add( Property.BOSS );
		properties.add( Property.UNDEAD );

		state = PASSIVE; //옥좌에서 기다림
	}

	public boolean awake = false;
	public int mistsDone = 0;
	public boolean misted = false;

	private int boltCooldown = 2;
	private int chainCooldown = 4;
	private int moonTimer = 5;

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

	public int phase(){
		return mistsDone + 1;
	}

	private boolean vampireHero(){
		return Dungeon.hero != null && Dungeon.hero.heroClass == HeroClass.VAMPIRE;
	}

	// ── 싸움 시작 ──

	public void awaken(){
		if (awake) return;
		awake = true;
		state = HUNTING;
		NightBlood.grant( this );
		notice();
		if (Dungeon.level instanceof VampireCastleBossLevel){
			((VampireCastleBossLevel) Dungeon.level).onFightStart();
		}
	}

	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()){
			BossHealthBar.assignBoss( this );
			yell( Messages.get( this, vampireHero() ? "notice_vampire" : "notice" ) );
			for (Char ch : Actor.chars()){
				if (ch instanceof DriedRose.GhostHero){
					((DriedRose.GhostHero) ch).sayBoss();
				}
			}
		}
	}

	// ── 행동 ──

	@Override
	protected boolean act() {
		if (!awake){
			spend( TICK );
			return true;
		}

		if (misted){
			//안개가 된 동안은 관 위에 머묾
			spend( TICK );
			return true;
		}

		if (boltCooldown > 0) boltCooldown--;
		if (chainCooldown > 0) chainCooldown--;

		//3단계: 핏빛 달 아래 화로가 하나씩 꺼짐
		if (mistsDone >= 2 && --moonTimer <= 0){
			moonTimer = 5;
			ArrayList<CastleLight> litOnes = new ArrayList<>();
			for (Mob m : Dungeon.level.mobs){
				if (m instanceof CastleLight && ((CastleLight) m).lit && !((CastleLight) m).permanent) litOnes.add( (CastleLight) m );
			}
			if (!litOnes.isEmpty()){
				Random.element( litOnes ).snuff( null );
				GLog.w( Messages.get( this, "moon_snuff" ) );
			}
		}

		if (state == HUNTING && enemy != null && enemy.isAlive() && paralysed <= 0){
			boolean adjacent = Dungeon.level.adjacent( pos, enemy.pos );
			boolean los = new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos == enemy.pos;

			//2단계부터: 피의 사슬
			if (mistsDone >= 1 && chainCooldown <= 0 && los && enemy == Dungeon.hero
					&& Dungeon.level.distance( pos, enemy.pos ) <= 3 && !VampireCastle.isLit( enemy.pos )
					&& enemy.buff( BloodChain.class ) == null){
				chainCooldown = 8;
				BloodChain c = Buff.affect( enemy, BloodChain.class );
				c.lord = id();
				c.left = 6;
				tether( enemy );
				if (Dungeon.level.heroFOV[pos]) Sample.INSTANCE.play( Assets.Sounds.CHAINS );
				GLog.w( Messages.get( this, "chain" ) );
				spend( TICK );
				return true;
			}

			//세 갈래 피의 화살
			if (!adjacent && los && boltCooldown <= 0){
				boltCooldown = 4;
				return volley( enemy );
			}
		}

		return super.act();
	}

	private boolean volley( Char target ){
		int w = Dungeon.level.width();
		int dx = Integer.signum( target.pos % w - pos % w );
		int dy = Integer.signum( target.pos / w - pos / w );
		ArrayList<Integer> aims = new ArrayList<>();
		aims.add( target.pos );
		int[] sides = { target.pos + (-dy) + dx * w, target.pos + dy - dx * w };
		for (int s : sides){
			if (s >= 0 && s < Dungeon.level.length() && !Dungeon.level.solid[s] && s != pos && !aims.contains( s )) aims.add( s );
		}

		spend( TICK );
		if (sprite != null && (sprite.visible || target.sprite.visible)){
			sprite.turnTo( pos, target.pos );
			if (sprite instanceof VampireCastleSprites.VampireLord) ((VampireCastleSprites.VampireLord) sprite).cast( target.pos );
			Sample.INSTANCE.play( Assets.Sounds.ZAP );
			final int[] left = { aims.size() };
			for (int a : aims){
				final int land = new Ballistica( pos, a, Ballistica.MAGIC_BOLT ).collisionPos;
				((VampireCastleSprites.VampireLord) sprite).bolt( land, new Callback() {
					@Override
					public void call() {
						boltHit( land );
						if (--left[0] == 0) next();
					}
				} );
			}
			return false;
		} else {
			for (int a : aims){
				boltHit( new Ballistica( pos, a, Ballistica.MAGIC_BOLT ).collisionPos );
			}
			return true;
		}
	}

	private void boltHit( int cell ){
		Char ch = Actor.findChar( cell );
		if (ch == null || ch == this || !ch.isAlive() || ch.alignment == alignment) return;
		if (hit( this, ch, true )){
			int dmg = Random.NormalIntRange( 5, 10 );
			ch.damage( dmg, this );
			if (ch.sprite != null && ch.sprite.visible) ch.sprite.emitter().burst( BloodParticle.BURST, 6 );
			if (ch == Dungeon.hero && !ch.isAlive()){
				Dungeon.fail( this );
				GLog.n( Messages.get( this, "bolt_kill" ) );
			}
		} else if (ch.sprite != null){
			ch.sprite.showStatus( CharSprite.NEUTRAL, ch.defenseVerb() );
		}
	}

	public void tether( Char ch ){
		if (sprite != null && ch.sprite != null && sprite.parent != null && (sprite.visible || ch.sprite.visible)){
			Beam beam = new Beam.LightRay( sprite.center(), ch.sprite.center() );
			beam.hardlight( 0.9f, 0.1f, 0.15f );
			sprite.parent.add( beam );
		}
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		//근접 흡혈 (어둠 속에서만)
		if (!VampireCastle.isLit( pos )){
			int reg = Math.min( damage / 2, HT - HP );
			if (reg > 0){
				HP += reg;
				if (sprite != null) sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( reg ), FloatingText.HEALING );
			}
		}
		return damage;
	}

	// ── 안개화 ──

	@Override
	public void damage( int dmg, Object src ) {
		if (!awake) awaken();
		if (!BossHealthBar.isAssigned()) notice();

		int preHP = HP;
		super.damage( dmg, src );
		if (isInvulnerable( src.getClass() )) return;

		int dmgTaken = preHP - HP;
		if (dmgTaken > 0){
			LockedFloor lock = Dungeon.hero.buff( LockedFloor.class );
			if (lock != null && !isImmune( src.getClass() )){
				if (Dungeon.isChallenged( Challenges.STRONGER_BOSSES )) lock.addTime( dmgTaken / 2f );
				else                                                    lock.addTime( dmgTaken );
			}
		}

		int threshold = HT / 3 * (2 - mistsDone);
		if (mistsDone < 2 && HP <= threshold){
			HP = threshold;
			startMist();
		}
	}

	@Override
	public boolean isAlive() {
		return super.isAlive() || mistsDone < 2;
	}

	@Override
	public boolean isInvulnerable( Class effect ) {
		return misted || super.isInvulnerable( effect );
	}

	private void startMist(){
		mistsDone++;
		misted = true;

		//사슬은 풀림
		for (BloodChain c : Dungeon.hero.buffs( BloodChain.class )) c.detach();

		VampireCastleBossLevel lvl = Dungeon.level instanceof VampireCastleBossLevel ? (VampireCastleBossLevel) Dungeon.level : null;
		int home = lvl != null ? lvl.coffinCell() : pos;
		if (Actor.findChar( home ) != null && Actor.findChar( home ) != this){
			for (int n : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.passable[home + n] && Actor.findChar( home + n ) == null){
					home = home + n;
					break;
				}
			}
		}
		if (Dungeon.level.heroFOV[pos]) CellEmitter.get( pos ).burst( Speck.factory( Speck.SMOKE ), 10 );
		if (Actor.findChar( home ) == null || Actor.findChar( home ) == this){
			pos = home;
			if (sprite != null) sprite.place( pos );
			Dungeon.level.occupyCell( this );
		}
		CellEmitter.get( pos ).burst( ShadowParticle.CURSE, 12 );
		if (sprite instanceof VampireCastleSprites.VampireLord) ((VampireCastleSprites.VampireLord) sprite).mist( true );
		Sample.INSTANCE.play( Assets.Sounds.GHOST );

		//화로가 모두 꺼짐
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m instanceof CastleLight && !(m instanceof Chandelier)) ((CastleLight) m).snuff( null );
		}

		//박쥐 4~6마리
		int bats = 4 + Random.Int( 3 );
		ArrayList<Integer> cells = new ArrayList<>();
		for (int i = 0; i < Dungeon.level.length(); i++){
			if (Dungeon.level.passable[i] && Actor.findChar( i ) == null && Dungeon.level.distance( i, pos ) <= 3
					&& Dungeon.level.distance( i, pos ) >= 2 && (lvl == null || lvl.inArena( i ))){
				cells.add( i );
			}
		}
		Random.shuffle( cells );
		for (int i = 0; i < bats && i < cells.size(); i++){
			Bat b = new Bat();
			b.pos = cells.get( i );
			b.state = b.HUNTING;
			GameScene.add( b );
			NightBlood.grant( b );
			CellEmitter.get( b.pos ).burst( ShadowParticle.UP, 6 );
		}

		yell( Messages.get( this, "mist" ) );
		GLog.w( Messages.get( this, "mist_hint" ) );
	}

	//화로가 켜질 때마다 관 둘레의 빛을 셈
	public void checkPull(){
		if (!misted) return;
		int lit = 0;
		for (Mob m : Dungeon.level.mobs){
			if (m instanceof CastleLight && !(m instanceof Chandelier) && ((CastleLight) m).lit) lit++;
		}
		if (lit < 2){
			if (lit == 1) GLog.i( Messages.get( this, "pull_half" ) );
			return;
		}
		misted = false;
		//빛에 끌려 나오면 바로 모습을 드러내고 비틀거림 (마비 중에는 그림이 멈추므로 변신 동작 없이)
		if (sprite instanceof VampireCastleSprites.VampireLord) ((VampireCastleSprites.VampireLord) sprite).setMisted( false );
		if (sprite != null) CellEmitter.get( pos ).burst( ShadowParticle.UP, 10 );
		Buff.prolong( this, Paralysis.class, 2f );
		if (sprite != null) CellEmitter.get( pos ).burst( Speck.factory( Speck.LIGHT ), 8 );
		yell( Messages.get( this, "pulled" ) );

		if (mistsDone >= 2){
			//핏빛 달
			moonTimer = 5;
			NightBlood nb = buff( NightBlood.class );
			if (nb != null) nb.power = 2;
			if (Dungeon.level instanceof VampireCastleBossLevel) ((VampireCastleBossLevel) Dungeon.level).bloodMoon();
			BossHealthBar.bleed( true );
			GLog.n( Messages.get( this, "blood_moon" ) );
		} else {
			chainCooldown = 3;
		}
	}

	@Override
	public void updateSpriteState() {
		super.updateSpriteState();
		if (misted && sprite instanceof VampireCastleSprites.VampireLord){
			((VampireCastleSprites.VampireLord) sprite).setMisted( true );
		}
	}

	// ── 죽음 ──

	@Override
	public void die( Object cause ) {

		super.die( cause );

		GameScene.bossSlain();

		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m instanceof Bat && m.alignment == Alignment.ENEMY) m.die( null );
		}
		for (BloodChain c : Dungeon.hero.buffs( BloodChain.class )) c.detach();

		if (Dungeon.level instanceof VampireCastleBossLevel){
			((VampireCastleBossLevel) Dungeon.level).onBossDefeated();
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

		yell( Messages.get( this, vampireHero() ? "defeated_vampire" : "defeated" ) );
	}

	@Override
	public String description() {
		String desc = super.description();
		if (misted) desc += "\n\n" + Messages.get( this, "desc_mist" );
		else if (mistsDone >= 2) desc += "\n\n" + Messages.get( this, "desc_moon" );
		return desc;
	}

	{
		immunities.add( Sleep.class );
		resistances.add( Terror.class );
		resistances.add( Charm.class );
	}

	private static final String AWAKE = "awake";
	private static final String MISTS = "mists";
	private static final String MISTED = "misted";
	private static final String BOLT_CD = "bolt_cd";
	private static final String CHAIN_CD = "chain_cd";
	private static final String MOON_T = "moon_t";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( AWAKE, awake );
		bundle.put( MISTS, mistsDone );
		bundle.put( MISTED, misted );
		bundle.put( BOLT_CD, boltCooldown );
		bundle.put( CHAIN_CD, chainCooldown );
		bundle.put( MOON_T, moonTimer );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		awake = bundle.getBoolean( AWAKE );
		mistsDone = bundle.getInt( MISTS );
		misted = bundle.getBoolean( MISTED );
		boltCooldown = bundle.getInt( BOLT_CD );
		chainCooldown = bundle.getInt( CHAIN_CD );
		moonTimer = bundle.getInt( MOON_T );
		if (awake){
			BossHealthBar.assignBoss( this );
			if (mistsDone >= 2 && !misted) BossHealthBar.bleed( true );
		}
	}

	//피의 사슬: 매 턴 영웅의 피를 군주에게 빨아들임. 밝은 칸에 서거나 군주와 멀어지면 끊김
	public static class BloodChain extends Buff {

		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		public int lord = -1;
		public int left = 6;

		@Override
		public boolean act() {
			Actor a = Actor.findById( lord );
			if (!(a instanceof VampireLord) || !((VampireLord) a).isAlive() || ((VampireLord) a).misted
					|| VampireCastle.isLit( target.pos ) || Dungeon.level.distance( target.pos, ((VampireLord) a).pos ) > 5){
				if (target == Dungeon.hero && a instanceof VampireLord) GLog.p( Messages.get( VampireLord.class, "chain_broken" ) );
				detach();
				return true;
			}
			VampireLord l = (VampireLord) a;
			int dmg = 3;
			target.damage( dmg, l );
			if (target.sprite != null) target.sprite.emitter().burst( BloodParticle.BURST, 4 );
			l.HP = Math.min( l.HT, l.HP + dmg );
			l.tether( target );
			if (target == Dungeon.hero && !target.isAlive()){
				Dungeon.fail( l );
				GLog.n( Messages.get( VampireLord.class, "chain_kill" ) );
			}
			if (--left <= 0) detach();
			spend( TICK );
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.BLEEDING;
		}

		@Override
		public void tintIcon( com.watabou.noosa.Image icon ) {
			icon.hardlight( 0.6f, 0f, 0.1f );
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc", left );
		}

		private static final String LORD = "lord";
		private static final String LEFT = "left";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( LORD, lord );
			bundle.put( LEFT, left );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			lord = bundle.getInt( LORD );
			left = bundle.getInt( LEFT );
		}
	}
}
