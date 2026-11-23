/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 에테르 기사의 '에테르 기운': 적을 치거나 시간이 지나면 몸에 쌓이는 에테르 (최대 150%, 에테르 저장고로 220%까지).
 * 모인 에테르로 네 가지 에테르 기술을 씀
 *  - 에테르 탄(10%): 직선으로 에테르 탄을 쏨
 *  - 섬광 도약(25%): 4칸 안의 적에게 뛰어들어 벰
 *  - 에테르 폭발(50%): 주변 8칸의 적을 베고 밀쳐 냄
 *  - 오로라 참격(100%): 1턴 동안 별빛을 모은 뒤, 앞쪽 넓은 부채꼴로 오로라빛 거대한 참격을 방출
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.AuroraSlash;
import com.shatteredpixel.shatteredpixeldungeon.effects.EtherBolt;
import com.shatteredpixel.shatteredpixeldungeon.effects.HeroAttackFX;
import com.shatteredpixel.shatteredpixeldungeon.effects.RiftSlash;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEtherArts;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class StarEther extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public enum Art {
		SPARK   (10,  0xB8E0FF, HeroIcon.ART_SPARK),
		FLASH   (25,  0x9CF0D8, HeroIcon.ART_FLASH),
		NOVA    (50,  0xC8A8FF, HeroIcon.ART_NOVA),
		AURORA  (100, 0xFFF0A0, HeroIcon.ART_AURORA);

		public final int cost, color, icon;
		Art( int cost, int color, int icon ){ this.cost = cost; this.color = color; this.icon = icon; }

		public String title(){
			return Messages.get( StarEther.class, name().toLowerCase() + "_name" );
		}
		public String desc(){
			return Messages.get( StarEther.class, name().toLowerCase() + "_desc" );
		}
	}

	public static final float BASE_MAX = 150f;
	public static final float HIT_GAIN = 10f;
	public static final float TURN_GAIN = 1f;

	//에테르 기술의 피해 배율 (영웅의 기본 공격 피해 기준)
	public static final float SPARK_DMG  = 0.6f;
	public static final float FLASH_DMG  = 1.4f;
	public static final float NOVA_DMG   = 1.0f;
	public static final float AURORA_DMG = 2.5f;
	public static final int FLASH_RANGE  = 4;
	public static final float AURORA_RANGE = 5f;
	public static final float AURORA_ANGLE = 120f;

	private float energy = 0;

	//에테르 저장고: 150 → 170 / 195 / 220
	public static float maxEnergy( Hero hero ){
		switch (hero == null ? 0 : hero.pointsInTalent( Talent.ETHER_RESERVOIR )){
			case 0: default: return BASE_MAX;
			case 1: return 170f;
			case 2: return 195f;
			case 3: return 220f;
		}
	}

	//별빛의 흐름: 치기 10 → 13/16/19, 턴마다 1 → 1.5/2/2.5
	public static float hitGain( Hero hero ){
		return HIT_GAIN * (1f + 0.3f*hero.pointsInTalent( Talent.STARLIGHT_FLOW ));
	}

	public static float turnGain( Hero hero ){
		return TURN_GAIN * (1f + 0.5f*hero.pointsInTalent( Talent.STARLIGHT_FLOW ));
	}

	private Hero hero(){
		return target instanceof Hero ? (Hero) target : null;
	}

	public int percent(){
		return (int) energy;
	}

	public boolean canUse( Art art ){
		return energy >= art.cost;
	}

	public void gain( float amt ){
		Hero hero = hero();
		float before = energy;
		energy = Math.min( maxEnergy( hero ), energy + amt );
		//100%가 막 찼으면 알려 줌
		if (before < Art.AURORA.cost && energy >= Art.AURORA.cost && hero != null && hero.sprite != null){
			hero.sprite.showStatus( 0xFFF0A0, Messages.get( this, "full" ) );
			hero.sprite.emitter().burst( StarlightParticle.BURST, 8 );
		}
		refreshButton();
		BuffIndicator.refreshHero();
	}

	//근접 공격으로 적을 맞혔을 때: 공격이 느린 무기일수록 한 번에 많이 쌓임
	public void onHit( Hero hero ){
		float delay = Math.max( 0.5f, Math.min( 2f, hero.attackDelay() ) );
		gain( hitGain( hero ) * delay );
	}

	public static void onHeroHit( Hero hero, Char enemy ){
		if (hero.subClass != HeroSubClass.STARLIGHT_KNIGHT) return;
		Buff.affect( hero, StarEther.class ).onHit( hero );
	}

	private void spendEnergy( Art art ){
		energy = Math.max( 0, energy - art.cost );
		refreshButton();
		BuffIndicator.refreshHero();
	}

	@Override
	public boolean act() {
		Hero hero = hero();
		if (hero != null && hero.isAlive()){
			energy = Math.min( maxEnergy( hero ), energy + turnGain( hero ) );
		}
		refreshButton();
		spend( TICK );
		return true;
	}

	//행동 버튼: 에테르 기사가 되면 늘 띄움
	private boolean wantsButton(){
		Hero hero = hero();
		//전직한 뒤로는 늘 떠 있음 (모자란 기술은 창에서 흐리게)
		return hero != null && hero == Dungeon.hero && hero.isAlive() && hero.subClass == HeroSubClass.STARLIGHT_KNIGHT;
	}

	public void refreshButton(){
		if (wantsButton()){
			if (ActionIndicator.action != this) ActionIndicator.setAction( this );
			else ActionIndicator.refresh();
		} else if (ActionIndicator.action == this){
			ActionIndicator.clearAction( this );
		}
	}

	@Override
	public void fx( boolean on ) {
		if (on) refreshButton();
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction( this );
	}

	// ---- 버프 칸 ----

	@Override
	public int icon() {
		return BuffIndicator.MONK_ENERGY;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( energy >= Art.AURORA.cost ? 0xFFF0A0 : 0xB8A4FF );
	}

	@Override
	public float iconFadePercent() {
		return Math.max( 0, 1f - energy / maxEnergy( hero() ) );
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString( percent() );
	}

	@Override
	public String desc() {
		Hero hero = hero();
		return Messages.get( this, "desc", percent(), (int) maxEnergy( hero ),
				Messages.decimalFormat( "#.#", hitGain( hero ) ), Messages.decimalFormat( "#.#", turnGain( hero ) ) );
	}

	// ---- 행동 버튼 ----

	@Override
	public String actionName() {
		return Messages.get( this, "action_name" );
	}

	@Override
	public int actionIcon() {
		return HeroIcon.ETHER_ARTS;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText( PixelScene.pixelFont );
		txt.text( percent() + "%" );
		txt.hardlight( energy >= Art.AURORA.cost ? 0xFFF0A0 : CharSprite.POSITIVE );
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return energy >= Art.AURORA.cost ? 0x8C7AD0 : 0x5E5490;
	}

	@Override
	public void doAction() {
		GameScene.show( new WndEtherArts( this ) );
	}

	// ---- 기술 사용 ----

	private static Art artBeingUsed;

	public void use( Art art ){
		Hero hero = hero();
		if (hero == null || !canUse( art )) return;
		if (art == Art.NOVA){
			nova( hero );
		} else {
			artBeingUsed = art;
			GameScene.selectCell( listener );
		}
	}

	private final com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener listener =
			new com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			if (cell == null || artBeingUsed == null) return;
			Hero hero = hero();
			if (hero == null || !canUse( artBeingUsed )) return;
			switch (artBeingUsed){
				case SPARK:  spark( hero, cell ); break;
				case FLASH:  flash( hero, cell ); break;
				case AURORA: aurora( hero, cell ); break;
				default: break;
			}
		}

		@Override
		public String prompt() {
			return artBeingUsed == Art.AURORA ? Messages.get( StarEther.class, "prompt_aurora" )
					: Messages.get( StarEther.class, "prompt" );
		}
	};

	private static int roll( Hero hero, float mult ){
		return Math.max( 1, Math.round( hero.damageRoll() * mult ) );
	}

	//기술을 쓴 뒤: 잔광 특성
	private void afterArt( Hero hero ){
		if (hero.hasTalent( Talent.AFTERGLOW )){
			Buff.prolong( hero, Afterglow.class, Afterglow.DURATION );
		}
		Invisibility.dispel();
	}

	// 에테르 탄 (10%)
	private void spark( final Hero hero, int cell ){
		if (cell == hero.pos){
			GLog.w( Messages.get( this, "bad_target" ) );
			return;
		}
		final Ballistica bolt = new Ballistica( hero.pos, cell, Ballistica.MAGIC_BOLT );
		spendEnergy( Art.SPARK );
		hero.busy();
		hero.sprite.zap( bolt.collisionPos );
		Sample.INSTANCE.play( Assets.Sounds.ZAP, 1, 1.25f );
		EtherBolt.shoot( hero.sprite, bolt.collisionPos, new Callback() {
					@Override
					public void call() {
						Char ch = Actor.findChar( bolt.collisionPos );
						if (ch != null && ch != hero){
							int dmg = roll( hero, SPARK_DMG );
							ch.damage( dmg, StarEther.this );
							if (ch.sprite != null) ch.sprite.flash();
							Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.15f );
						}
						afterArt( hero );
						hero.spendAndNext( Actor.TICK );
					}
				} );
	}

	// 섬광 도약 (25%)
	private void flash( final Hero hero, final int cell ){
		final Char enemy = Actor.findChar( cell );
		if (enemy == null || enemy == hero || !Dungeon.level.heroFOV[cell] || hero.isCharmedBy( enemy )){
			GLog.w( Messages.get( this, "bad_target" ) );
			return;
		}
		if (hero.canAttack( enemy )){
			spendEnergy( Art.FLASH );
			hero.busy();
			hero.sprite.attack( cell, new Callback() {
				@Override
				public void call() { flashStrike( hero, enemy ); }
			} );
			return;
		}
		if (Dungeon.level.distance( hero.pos, enemy.pos ) > FLASH_RANGE){
			GLog.w( Messages.get( this, "too_far", FLASH_RANGE ) );
			return;
		}
		Ballistica c = new Ballistica( hero.pos, enemy.pos, Ballistica.PROJECTILE );
		if (c.collisionPos != enemy.pos || c.dist < 1){
			GLog.w( Messages.get( this, "bad_target" ) );
			return;
		}
		final int leapPos = c.path.get( c.dist - 1 );
		if (!Dungeon.level.passable[leapPos] && !(hero.flying && Dungeon.level.avoid[leapPos])
				|| (Actor.findChar( leapPos ) != null && leapPos != hero.pos)){
			GLog.w( Messages.get( this, "bad_target" ) );
			return;
		}
		if (hero.rooted){
			PixelScene.shake( 1, 1f );
			GLog.w( Messages.get( this, "rooted" ) );
			return;
		}
		spendEnergy( Art.FLASH );
		hero.busy();
		hero.sprite.emitter().burst( StarlightParticle.BURST, 6 );
		Sample.INSTANCE.play( Assets.Sounds.MISS, 1, 0.8f );
		//지나가는 길에 남는 섬광 자취
		for (int p : c.subPath( 0, c.dist - 1 )){
			if (Dungeon.level.heroFOV[p]) com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.center( p ).burst( StarlightParticle.BURST, 3 );
		}
		hero.sprite.jump( hero.pos, leapPos, new Callback() {
			@Override
			public void call() {
				hero.move( leapPos );
				Dungeon.level.occupyCell( hero );
				Dungeon.observe();
				GameScene.updateFog();
				hero.sprite.attack( cell, new Callback() {
					@Override
					public void call() { flashStrike( hero, enemy ); }
				} );
			}
		} );
	}

	private void flashStrike( Hero hero, Char enemy ){
		if (hero.sprite != null) hero.sprite.idle();
		AttackIndicator.target( enemy );
		if (hero.sprite != null && hero.sprite.parent != null){
			GameScene.effect( new HeroAttackFX( hero.sprite, enemy.pos, HeroAttackFX.ART_FX, 1, 0f, true, 0.66f, 0.32f ) );
		}
		if (hero.attack( enemy, FLASH_DMG, 0, Char.INFINITE_ACCURACY )){
			Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG );
		}
		afterArt( hero );
		hero.spendAndNext( hero.attackDelay() );
	}

	// 에테르 폭발 (50%)
	private void nova( Hero hero ){
		spendEnergy( Art.NOVA );
		hero.sprite.operate( hero.pos );
		GameScene.effect( new HeroAttackFX( null, hero.pos, HeroAttackFX.ART_FX, 2, 0f, false, 1.45f, 0.45f ) );
		hero.sprite.emitter().burst( StarlightParticle.BURST, 12 );
		Sample.INSTANCE.play( Assets.Sounds.BLAST, 1, 1.2f );
		ArrayList<Char> targets = new ArrayList<>();
		for (int i : PathFinder.NEIGHBOURS8){
			Char ch = Actor.findChar( hero.pos + i );
			if (ch != null && ch.alignment != hero.alignment && ch.isAlive()) targets.add( ch );
		}
		for (Char ch : targets){
			int oldPos = ch.pos;
			hero.attack( ch, NOVA_DMG, 0, Char.INFINITE_ACCURACY );
			if (ch.isAlive() && ch.pos == oldPos){
				Ballistica traj = new Ballistica( ch.pos, ch.pos + (ch.pos - hero.pos), Ballistica.MAGIC_BOLT );
				WandOfBlastWave.throwChar( ch, traj, 2, true, true, this );
			}
		}
		afterArt( hero );
		hero.spendAndNext( Actor.TICK );
	}

	// 오로라 참격 (100%): 이번 턴은 별빛을 모으고, 다음 턴 시작에 방출
	private void aurora( Hero hero, int cell ){
		if (cell == hero.pos){
			GLog.w( Messages.get( this, "bad_target" ) );
			return;
		}
		spendEnergy( Art.AURORA );
		AuroraCharge charge = Buff.affect( hero, AuroraCharge.class, Actor.TICK );
		charge.set( cell );
		hero.sprite.operate( hero.pos );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 1, 0.9f );
		GLog.p( Messages.get( this, "charging" ) );
		Invisibility.dispel();
		hero.spendAndNext( Actor.TICK );
	}

	public static class AuroraCharge extends FlavourBuff {

		{
			type = buffType.POSITIVE;
			actPriority = HERO_PRIO + 1;
		}

		private int targetCell = -1;
		private Emitter stars;

		public void set( int cell ){
			targetCell = cell;
		}

		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}

		@Override
		public void fx( boolean on ) {
			if (on && target.sprite != null){
				if (stars != null) stars.on = false;
				stars = target.sprite.centerEmitter();
				stars.pour( StarlightParticle.ATTRACT, 0.03f );
			} else if (!on && stars != null){
				stars.on = false;
				stars = null;
			}
		}

		@Override
		public boolean act() {
			final Hero hero = target instanceof Hero ? (Hero) target : null;
			if (hero == null || !hero.isAlive() || targetCell < 0){
				detach();
				return true;
			}
			//별빛이 모이는 모습이 잠깐 보인 뒤 방출
			if (target.sprite != null) target.sprite.centerEmitter().burst( StarlightParticle.ATTRACT, 16 );
			final int cell = targetCell;
			GameScene.effect( new Delay( 0.35f, new Callback() {
				@Override
				public void call() {
					detach();
					release( hero, cell );
					next();
				}
			} ) );
			return false;
		}

		private static final String TARGET = "target";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( TARGET, targetCell );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			targetCell = bundle.getInt( TARGET );
		}
	}

	private static void release( Hero hero, int cell ){
		Ballistica aim = new Ballistica( hero.pos, cell, Ballistica.WONT_STOP );
		ConeAOE cone = new ConeAOE( aim, AURORA_RANGE, AURORA_ANGLE, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET );

		if (hero.sprite != null){
			final CharSprite hs = hero.sprite;
			hs.attack( cell, new Callback() {
				@Override
				public void call() { hs.idle(); }
			} );
			AuroraSlash.play( hero.sprite, cell, AURORA_RANGE, AURORA_ANGLE );
		}
		Sample.INSTANCE.play( Assets.Sounds.RAY, 1, 0.8f );
		Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG, 1, 0.7f );
		PixelScene.shake( 2, 0.4f );

		StarEther se = hero.buff( StarEther.class );
		Object src = se != null ? se : hero;
		ArrayList<Char> hits = new ArrayList<>();
		for (int c : cone.cells){
			Char ch = Actor.findChar( c );
			if (ch != null && ch != hero && ch.alignment != hero.alignment) hits.add( ch );
		}
		for (Char ch : hits){
			int dmg = roll( hero, AURORA_DMG );
			ch.damage( dmg, src );
			if (ch.sprite != null){
				ch.sprite.flash();
				ch.sprite.emitter().burst( StarlightParticle.BURST, 6 );
				RiftSlash.at( ch.sprite, 1.1f );
			}
		}
		if (hero.hasTalent( Talent.AFTERGLOW )){
			Buff.prolong( hero, Afterglow.class, Afterglow.DURATION );
		}
		Dungeon.observe();
		GameScene.updateFog();
	}

	//잔광: 기술을 쓴 뒤 5턴 안의 다음 근접 공격 피해 +15 / 30 / 45%
	public static class Afterglow extends FlavourBuff {
		public static final float DURATION = 5f;
		{ type = buffType.POSITIVE; }

		@Override
		public int icon() {
			return BuffIndicator.UPGRADE;
		}

		@Override
		public void tintIcon( Image icon ) {
			icon.hardlight( 0xB8E0FF );
		}

		@Override
		public float iconFadePercent() {
			return Math.max( 0, (DURATION - visualcooldown()) / DURATION );
		}

		@Override
		public String desc() {
			Hero hero = Dungeon.hero;
			return Messages.get( this, "desc", 15*(hero != null ? hero.pointsInTalent( Talent.AFTERGLOW ) : 1), dispTurns() );
		}
	}

	//영웅의 공격 피해에 더할 몫 (Hero.attackProc에서 부름)
	public static int onAttackProc( Hero hero, Char enemy, int damage ){
		Afterglow ag = hero.buff( Afterglow.class );
		if (ag != null){
			ag.detach();
			damage = Math.round( damage * (1f + 0.15f*hero.pointsInTalent( Talent.AFTERGLOW )) );
			if (enemy.sprite != null) enemy.sprite.emitter().burst( StarlightParticle.BURST, 4 );
		}
		return damage;
	}

	//잠깐 기다렸다가 부르는 도우미
	public static class Delay extends Visual {
		private float left;
		private final Callback cb;
		public Delay( float time, Callback cb ){
			super( 0, 0, 0, 0 );
			this.left = time;
			this.cb = cb;
		}
		@Override
		public void update() {
			super.update();
			left -= Game.elapsed;
			if (left <= 0){
				killAndErase();
				if (cb != null) cb.call();
			}
		}
	}

	private static final String ENERGY = "energy";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ENERGY, energy );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		energy = bundle.getFloat( ENERGY );
	}
}
