/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 비검 기사: 에테르 점토를 통째로 던지고, 되불러 오고, 점토가 박힌 곳으로 순간 이동함
 *  - 투척: 8칸 안으로 점토를 던져 처음 닿는 적에게 기본 공격 피해. 점토는 그 자리에 박힘 (그동안 맨손)
 *  - 회수: 점토가 날아 돌아오며 지나는 길의 모든 적에게 기본 공격의 60% 피해 (방어 무시)
 *  - 귀환 도약: 점토가 박힌 곳으로 순간 이동해 점토를 다시 쥐고, 그 자리의 적을 130% 피해로 벰
 *  점토가 박힌 칸을 밟으면 저절로 주움. 다른 층으로 가면 저절로 돌아옴
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
import com.shatteredpixel.shatteredpixeldungeon.effects.ClayMarker;
import com.shatteredpixel.shatteredpixeldungeon.effects.RiftSlash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherClay;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class FlyingClay extends Buff implements ActionIndicator.Action {

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	public static final int RANGE = 8;
	public static final float THROW_DMG = 1f;

	//회전 회수: 60 → 85 / 110 / 135%
	public static float recallMult( Hero hero ){
		return 0.6f + 0.25f * hero.pointsInTalent( Talent.WHIRLING_RECALL );
	}

	//섬광 귀환: 130 → 160 / 190 / 220%
	public static float blinkMult( Hero hero ){
		return 1.3f + 0.3f * hero.pointsInTalent( Talent.FLASH_RETURN );
	}

	private EtherClay clay = null;   //던져서 박혀 있는 점토 (손에 있으면 null)
	private int cell = -1;
	private int depth = -1, branch = 0;
	private ClayMarker marker;

	public boolean isOut(){
		return clay != null;
	}

	private Hero hero(){
		return target instanceof Hero ? (Hero) target : null;
	}

	private static EtherClay held( Hero hero ){
		return hero.belongings.weapon instanceof EtherClay ? (EtherClay) hero.belongings.weapon : null;
	}

	@Override
	public boolean act() {
		Hero hero = hero();
		if (hero != null && clay != null){
			if (depth != Dungeon.depth || branch != Dungeon.branch){
				returnClay( hero );
			} else if (hero.pos == cell){
				returnClay( hero );
				GLog.i( Messages.get( this, "picked" ) );
				if (hero.sprite != null) hero.sprite.emitter().burst( StarlightParticle.BURST, 6 );
			}
		}
		refreshButton();
		spend( TICK );
		return true;
	}

	//점토를 다시 손에 쥠 (손에 다른 무기가 있으면 가방으로)
	private void returnClay( Hero hero ){
		EtherClay c = clay;
		clay = null;
		cell = -1;
		removeMarker();
		if (c == null) return;
		if (hero.belongings.weapon == null){
			hero.belongings.weapon = c;
			c.activate( hero );
		} else if (!c.collect( hero.belongings.backpack )){
			Dungeon.level.drop( c, hero.pos ).sprite.drop();
		}
		Item.updateQuickslot();
		refreshButton();
		BuffIndicator.refreshHero();
	}

	// ---- 투척 ----

	private final CellSelector.Listener throwListener = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) throwClay( hero(), target );
		}
		@Override
		public String prompt() {
			return Messages.get( FlyingClay.class, "prompt" );
		}
	};

	private void throwClay( final Hero hero, int target ){
		final EtherClay c = hero == null ? null : held( hero );
		if (c == null){
			GLog.w( Messages.get( this, "no_clay" ) );
			return;
		}
		if (c.cursed){
			c.cursedKnown = true;
			GLog.w( Messages.get( this, "cursed" ) );
			return;
		}
		if (target == hero.pos){
			GLog.w( Messages.get( this, "bad_target" ) );
			return;
		}
		Ballistica b = new Ballistica( hero.pos, target, Ballistica.PROJECTILE );
		int idx = Math.min( b.dist, RANGE );
		while (idx > 0 && Dungeon.level.solid[b.path.get( idx )]) idx--;
		if (idx <= 0){
			GLog.w( Messages.get( this, "bad_target" ) );
			return;
		}
		final int land = b.path.get( idx );
		final Char enemy = Actor.findChar( land );
		final float delay = hero.attackDelay();

		hero.busy();
		hero.sprite.zap( land );
		Sample.INSTANCE.play( Assets.Sounds.MISS, 1, 0.85f );
		((MissileSprite) hero.sprite.parent.recycle( MissileSprite.class )).reset( hero.sprite, land, c, new Callback() {
			@Override
			public void call() {
				if (enemy != null && enemy != hero && enemy.isAlive() && enemy.alignment != hero.alignment){
					boolean hit = hero.attack( enemy, THROW_DMG, 0, 1f );
					if (hit){
						int pin = hero.pointsInTalent( Talent.PINNING_THROW );
						if (pin > 0 && enemy.isAlive()) Buff.prolong( enemy, Roots.class, pin );
						if (enemy.sprite != null) RiftSlash.at( enemy.sprite, 0.8f );
					}
				}
				//손을 떠나 그 자리에 박힘
				if (hero.belongings.weapon == c) hero.belongings.weapon = null;
				clay = c;
				cell = land;
				depth = Dungeon.depth;
				branch = Dungeon.branch;
				addMarker();
				Item.updateQuickslot();
				Invisibility.dispel();
				refreshButton();
				BuffIndicator.refreshHero();
				hero.spendAndNext( delay );
			}
		} );
	}

	// ---- 회수 ----

	private void recall( final Hero hero ){
		if (clay == null || hero == null) return;
		final int from = cell;
		final EtherClay c = clay;
		Ballistica path = new Ballistica( from, hero.pos, Ballistica.STOP_TARGET );
		final ArrayList<Char> hits = new ArrayList<>();
		for (int p : path.subPath( 0, path.dist )){
			Char ch = Actor.findChar( p );
			if (ch != null && ch != hero && ch.alignment != hero.alignment && !hits.contains( ch )) hits.add( ch );
		}
		removeMarker();
		hero.busy();
		Sample.INSTANCE.play( Assets.Sounds.MISS, 1, 1.2f );
		((MissileSprite) hero.sprite.parent.recycle( MissileSprite.class )).reset( from, hero.sprite, c, new Callback() {
			@Override
			public void call() {
				returnClay( hero );
				float mult = recallMult( hero );
				for (Char ch : hits){
					if (!ch.isAlive()) continue;
					int dmg = Math.max( 1, Math.round( hero.damageRoll() * mult ) );
					ch.damage( dmg, FlyingClay.this );
					if (ch.sprite != null){
						ch.sprite.flash();
						RiftSlash.at( ch.sprite, 0.7f );
					}
				}
				if (!hits.isEmpty()) Sample.INSTANCE.play( Assets.Sounds.HIT_SLASH, 1, 1.1f );
				if (hero.sprite != null) hero.sprite.emitter().burst( StarlightParticle.BURST, 5 );
				Invisibility.dispel();
				hero.spendAndNext( Actor.TICK );
			}
		} );
	}

	// ---- 귀환 도약 ----

	private void blink( final Hero hero ){
		if (clay == null || hero == null) return;
		int dest = -1;
		if (Actor.findChar( cell ) == null && (Dungeon.level.passable[cell] || Dungeon.level.avoid[cell])){
			dest = cell;
		} else {
			int best = Integer.MAX_VALUE;
			for (int i : PathFinder.NEIGHBOURS8){
				int n = cell + i;
				if (n < 0 || n >= Dungeon.level.length()) continue;
				if (n == hero.pos){ dest = n; break; }
				if (!Dungeon.level.passable[n] || Actor.findChar( n ) != null) continue;
				int d = Dungeon.level.distance( n, hero.pos );
				if (d < best){ best = d; dest = n; }
			}
		}
		if (dest == -1){
			GLog.w( Messages.get( this, "no_room" ) );
			return;
		}
		final Char enemy = Actor.findChar( cell );
		final int clayCell = cell;
		if (dest != hero.pos) ScrollOfTeleportation.appear( hero, dest );
		Dungeon.level.occupyCell( hero );
		Dungeon.observe();
		GameScene.updateFog();
		returnClay( hero );
		if (hero.sprite != null) hero.sprite.emitter().burst( StarlightParticle.BURST, 10 );
		Invisibility.dispel();

		if (enemy != null && enemy != hero && enemy.isAlive() && enemy.alignment != hero.alignment
				&& Dungeon.level.adjacent( hero.pos, enemy.pos )){
			hero.busy();
			hero.sprite.attack( clayCell, new Callback() {
				@Override
				public void call() {
					hero.sprite.idle();
					if (hero.attack( enemy, blinkMult( hero ), 0, Char.INFINITE_ACCURACY )){
						if (enemy.sprite != null) RiftSlash.at( enemy.sprite, 1f );
						Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG );
					}
					hero.spendAndNext( hero.attackDelay() );
				}
			} );
		} else {
			hero.spendAndNext( Actor.TICK );
		}
	}

	// ---- 점토 표시 ----

	private void addMarker(){
		removeMarker();
		if (clay != null && cell >= 0 && depth == Dungeon.depth && branch == Dungeon.branch){
			marker = new ClayMarker( clay.image(), cell );
			GameScene.effect( marker );
		}
	}

	private void removeMarker(){
		if (marker != null){
			marker.killAndErase();
			marker = null;
		}
	}

	@Override
	public void fx( boolean on ) {
		if (on){
			addMarker();
			refreshButton();
		} else {
			removeMarker();
		}
	}

	@Override
	public void detach() {
		super.detach();
		removeMarker();
		ActionIndicator.clearAction( this );
	}

	// ---- 행동 버튼 ----

	private boolean wantsButton(){
		Hero hero = hero();
		if (hero == null || hero != Dungeon.hero || !hero.isAlive() || hero.subClass != HeroSubClass.FLYING_BLADE) return false;
		if (clay != null) return true;
		return held( hero ) != null;
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
	public String actionName() {
		return Messages.get( this, clay != null ? "action_recall" : "action_throw" );
	}

	@Override
	public int actionIcon() {
		return clay != null ? HeroIcon.CLAY_RECALL : HeroIcon.CLAY_THROW;
	}

	@Override
	public int indicatorColor() {
		return clay != null ? 0x7A68C0 : 0x5E5490;
	}

	@Override
	public void doAction() {
		final Hero hero = hero();
		if (hero == null || !hero.ready) return;
		if (clay == null){
			GameScene.selectCell( throwListener );
			return;
		}
		GameScene.show( new WndOptions( new ItemSprite( clay ),
				Messages.get( this, "out_title" ),
				Messages.get( this, "out_desc", Math.round( recallMult( hero )*100 ), Math.round( blinkMult( hero )*100 ) ),
				Messages.get( this, "recall" ), Messages.get( this, "blink" ) ){
			@Override
			protected void onSelect( int index ) {
				if (!hero.ready || clay == null) return;
				if (index == 0) recall( hero );
				else if (index == 1) blink( hero );
			}
		} );
	}

	//길게 누르면 바로 회수
	@Override
	public boolean doLongAction() {
		Hero hero = hero();
		if (clay != null && hero != null && hero.ready){
			recall( hero );
			return true;
		}
		return false;
	}

	// ---- 버프 칸 ----

	@Override
	public int icon() {
		return clay != null ? BuffIndicator.THROWN_WEP : BuffIndicator.NONE;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 0xB8A4FF );
	}

	@Override
	public String desc() {
		Hero hero = hero();
		int dist = (hero != null && cell >= 0) ? Dungeon.level.distance( hero.pos, cell ) : 0;
		return Messages.get( this, "desc", dist );
	}

	private static final String CLAY = "clay";
	private static final String CELL = "cell";
	private static final String DEPTH = "depth";
	private static final String BRANCH = "branch";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if (clay != null) bundle.put( CLAY, clay );
		bundle.put( CELL, cell );
		bundle.put( DEPTH, depth );
		bundle.put( BRANCH, branch );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		clay = bundle.contains( CLAY ) ? (EtherClay) bundle.get( CLAY ) : null;
		cell = bundle.getInt( CELL );
		depth = bundle.getInt( DEPTH );
		branch = bundle.getInt( BRANCH );
	}

	//영웅이 쓰러지거나 할 때 대비: 점토를 쥐고 있지 않으면 되돌려 받음
	public static void forceReturn( Hero hero ){
		FlyingClay fc = hero.buff( FlyingClay.class );
		if (fc != null && fc.clay != null) fc.returnClay( hero );
	}
}
