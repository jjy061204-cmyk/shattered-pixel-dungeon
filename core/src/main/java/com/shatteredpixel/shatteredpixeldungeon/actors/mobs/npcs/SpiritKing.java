/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령왕: 정령사의 갑옷 능력 '정령왕 소환'으로 나타나 한동안 함께 싸우는 정령계의 왕 (영웅 레벨을 따라 강해짐)
 *  - 불의 정령왕: 주먹에 불을 실어 적을 불태우고, 세 번째 공격마다 주변을 불꽃으로 휩쓺. 불에 면역
 *  - 물의 정령왕: 물의 창을 멀리서 던지고(1칸 밀침), 이따금 다친 영웅을 치유
 *  - 바람의 정령왕: 빠르게 날아다니며 잘 피하고, 공격이 대상 곁의 적까지 베며 2칸 날려 보냄
 *  - 땅의 정령왕: 단단하고 체력이 많으며, 공격한 적을 묶고 주변 적의 이목을 끎
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpiritKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class SpiritKing extends DirectableAlly {

	{
		HP = HT = 20;
		immunities.add( AllyBuff.class );
		properties.add( Property.INORGANIC );
	}

	public enum Element {
		FIRE  ( ForestWand.Spirit.FIRE,  ForestWand.Spirit.SALAMANDER ),
		WATER ( ForestWand.Spirit.WATER, ForestWand.Spirit.UNDINE ),
		WIND  ( ForestWand.Spirit.WIND,  ForestWand.Spirit.SYLPH ),
		EARTH ( ForestWand.Spirit.EARTH, ForestWand.Spirit.GNOME );

		public final ForestWand.Spirit spirit, greater;
		Element( ForestWand.Spirit spirit, ForestWand.Spirit greater ){
			this.spirit = spirit;
			this.greater = greater;
		}

		public String title(){
			return Messages.get( SpiritKing.class, name().toLowerCase() + "_name" );
		}

		//같은 속성으로 치는 일반 정령
		public boolean matches( ForestWand.Spirit s ){
			if (s == null || s.greater) return false;
			switch (this){
				case FIRE:  return s == ForestWand.Spirit.FIRE || s == ForestWand.Spirit.FLAME;
				case WATER: return s == ForestWand.Spirit.WATER || s == ForestWand.Spirit.FROST || s == ForestWand.Spirit.ICE;
				case WIND:  return s == ForestWand.Spirit.WIND || s == ForestWand.Spirit.SHOCK;
				case EARTH: default: return s == ForestWand.Spirit.EARTH || s == ForestWand.Spirit.ROCK || s == ForestWand.Spirit.NATURE;
			}
		}

		public SpiritKing create(){
			switch (this){
				case FIRE:  return new FireKing();
				case WATER: return new WaterKing();
				case WIND:  return new WindKing();
				case EARTH: default: return new EarthKing();
			}
		}
	}

	public abstract Element element();

	public static final float BASE_TIME = 60f;
	protected float timeLeft = BASE_TIME;

	//긴 통치: 머무는 시간 +20/40/60/80턴
	public static float lifetime( Hero hero ){
		return BASE_TIME + 20f * (hero != null ? hero.pointsInTalent( Talent.KING_REIGN ) : 0);
	}

	public void setLifetime( float t ){
		timeLeft = t;
	}

	protected static int lvl(){
		return Dungeon.hero != null ? Dungeon.hero.lvl : 1;
	}

	//왕의 위엄: 체력·피해 +15/30/45/60%
	protected static float majesty(){
		return 1f + 0.15f * (Dungeon.hero != null ? Dungeon.hero.pointsInTalent( Talent.KING_MAJESTY ) : 0);
	}

	protected float hpMul(){ return 1f; }
	protected float dmgMul(){ return 1f; }
	protected float evaMul(){ return 1f; }
	protected int armor(){ return lvl() / 3; }

	public void adjustStats(){
		int newHT = Math.round( (20 + 5*lvl()) * hpMul() * majesty() );
		if (newHT != HT){
			float frac = HT > 0 ? HP / (float) HT : 1f;
			HT = newHT;
			HP = Math.max( 1, Math.min( HT, Math.round( HT * frac ) ) );
		}
	}

	@Override
	public int attackSkill( Char target ) {
		return 12 + 2 * lvl();
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return Math.round( (5 + lvl()) * evaMul() );
	}

	@Override
	public int damageRoll() {
		int l = lvl();
		return Math.round( Random.NormalIntRange( 2 + l/2, 6 + l ) * dmgMul() * majesty() );
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, armor() );
	}

	@Override
	protected boolean act() {
		adjustStats();
		if (timeLeft <= 0){
			GLog.i( Messages.get( SpiritKing.class, "leaves", name() ) );
			die( null );
			return true;
		}
		return super.act();
	}

	@Override
	protected void spend( float time ) {
		super.spend( time );
		timeLeft -= time;
	}

	@Override
	public void die( Object cause ) {
		flying = false;
		if (sprite != null){
			CellEmitter.center( pos ).burst( element().spirit.particle(), 20 );
			CellEmitter.get( pos ).burst( Speck.factory( Speck.LIGHT ), 6 );
		}
		super.die( cause );
	}

	@Override
	public String name() {
		return element().title();
	}

	@Override
	public String description() {
		String d = Messages.get( this, "desc" );
		d += "\n\n" + Messages.get( SpiritKing.class, "desc_time", (int) Math.ceil( Math.max( 0, timeLeft ) ) );
		return d;
	}

	@Override
	public void defendPos( int cell ) {
		GLog.i( Messages.get( SpiritKing.class, "direct_defend", name() ) );
		super.defendPos( cell );
	}

	@Override
	public void followHero() {
		GLog.i( Messages.get( SpiritKing.class, "direct_follow", name() ) );
		super.followHero();
	}

	@Override
	public void targetChar( Char ch ) {
		GLog.i( Messages.get( SpiritKing.class, "direct_attack", name() ) );
		super.targetChar( ch );
	}

	public static SpiritKing find(){
		for (Char ch : Actor.chars()){
			if (ch instanceof SpiritKing && ch.isAlive()) return (SpiritKing) ch;
		}
		return null;
	}

	private static final String TIME = "time_left";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( TIME, timeLeft );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		timeLeft = bundle.getFloat( TIME );
	}

	// ── 불의 정령왕 ──
	public static class FireKing extends SpiritKing {
		{
			spriteClass = SpiritKingSprite.Fire.class;
			immunities.add( Burning.class );
			immunities.add( Fire.class );
		}
		private int swings = 0;

		@Override public Element element(){ return Element.FIRE; }
		@Override protected float dmgMul(){ return 1.2f; }

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			ForestWand.elemHit( ForestWand.Spirit.FIRE, enemy, 0.35f );
			if (enemy.isAlive()) Buff.affect( enemy, Burning.class ).reignite( enemy, 4f );
			//세 번째 공격마다 주변을 불꽃으로 휩쓺
			if (++swings >= 3){
				swings = 0;
				if (sprite != null) sprite.centerEmitter().burst( FlameParticle.FACTORY, 24 );
				Sample.INSTANCE.play( Assets.Sounds.BURNING );
				for (int n : PathFinder.NEIGHBOURS8){
					Char ch = Actor.findChar( pos + n );
					if (ch != null && ch != enemy && ch.alignment == Alignment.ENEMY && ch.isAlive()){
						ForestWand.elemHit( ForestWand.Spirit.FIRE, ch, 0.3f );
						if (ch.isAlive()) Buff.affect( ch, Burning.class ).reignite( ch, 3f );
					}
					CellEmitter.get( pos + n ).burst( FlameParticle.FACTORY, 4 );
				}
			}
			return damage;
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( "swings", swings );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			swings = bundle.getInt( "swings" );
		}
	}

	// ── 물의 정령왕 ──
	public static class WaterKing extends SpiritKing {
		{
			spriteClass = SpiritKingSprite.Water.class;
			immunities.add( Burning.class );
		}
		private int healCooldown = 0;
		public static final int RANGE = 4;

		@Override public Element element(){ return Element.WATER; }
		@Override protected float dmgMul(){ return 0.9f; }

		@Override
		protected boolean canAttack( Char enemy ) {
			return super.canAttack( enemy )
					|| (Dungeon.level.distance( pos, enemy.pos ) <= RANGE
						&& new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos == enemy.pos);
		}

		@Override
		protected boolean doAttack( Char enemy ) {
			if (Dungeon.level.adjacent( pos, enemy.pos )){
				return super.doAttack( enemy );
			}
			if (sprite != null && (sprite.visible || enemy.sprite.visible)){
				sprite.zap( enemy.pos );
				return false;
			}
			zapHit();
			return true;
		}

		public void onZapComplete(){
			zapHit();
			next();
		}

		private void zapHit(){
			spend( attackDelay() );
			Invisibility.dispel( this );
			Char enemy = this.enemy;
			if (enemy == null) return;
			if (hit( this, enemy, true )){
				int dmg = damageRoll();
				enemy.damage( Math.max( 0, dmg - enemy.drRoll() ), this );
				ForestWand.elemHit( ForestWand.Spirit.WATER, enemy, 0.3f );
				if (enemy.isAlive()) ForestWand.knockAway( this, enemy, 1, false );
			} else if (enemy.sprite != null){
				enemy.sprite.showStatus( CharSprite.NEUTRAL, enemy.defenseVerb() );
			}
		}

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			ForestWand.elemHit( ForestWand.Spirit.WATER, enemy, 0.3f );
			return damage;
		}

		@Override
		protected boolean act() {
			//치유의 물결: 4턴마다, 5칸 안의 다친 영웅을 치유
			if (healCooldown > 0) healCooldown--;
			Hero h = Dungeon.hero;
			if (healCooldown <= 0 && h != null && h.isAlive() && h.HP < h.HT
					&& Dungeon.level.distance( pos, h.pos ) <= 5){
				int heal = Math.min( 2 + lvl()/3, h.HT - h.HP );
				h.HP += heal;
				if (h.sprite != null){
					h.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 3 );
					h.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
				}
				healCooldown = 4;
			}
			return super.act();
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( "heal_cd", healCooldown );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			healCooldown = bundle.getInt( "heal_cd" );
		}
	}

	// ── 바람의 정령왕 ──
	public static class WindKing extends SpiritKing {
		{
			spriteClass = SpiritKingSprite.Wind.class;
			flying = true;
			baseSpeed = 2f;
		}
		@Override public Element element(){ return Element.WIND; }
		@Override protected float hpMul(){ return 0.8f; }
		@Override protected float evaMul(){ return 1.8f; }

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			ForestWand.elemHit( ForestWand.Spirit.WIND, enemy, 0.3f );
			//칼바람이 대상 곁의 다른 적도 벰
			for (int n : PathFinder.NEIGHBOURS8){
				Char ch = Actor.findChar( enemy.pos + n );
				if (ch != null && ch != this && ch != enemy && ch.alignment == Alignment.ENEMY && ch.isAlive()){
					ch.damage( Math.max( 1, damage / 2 ), this );
					if (ch.sprite != null) ch.sprite.centerEmitter().burst( element().spirit.particle(), 5 );
					break;
				}
			}
			ForestWand.knockAway( this, enemy, 2, true );
			return damage;
		}
	}

	// ── 땅의 정령왕 ──
	public static class EarthKing extends SpiritKing {
		{
			spriteClass = SpiritKingSprite.Earth.class;
		}
		@Override public Element element(){ return Element.EARTH; }
		@Override protected float hpMul(){ return 1.7f; }
		@Override protected float dmgMul(){ return 0.9f; }
		@Override protected float evaMul(){ return 0.6f; }
		@Override protected int armor(){ return 3 + lvl(); }

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			ForestWand.elemHit( ForestWand.Spirit.EARTH, enemy, 0.3f );
			if (enemy.isAlive() && !enemy.flying) Buff.prolong( enemy, Roots.class, 2f );
			return damage;
		}

		@Override
		protected boolean act() {
			//왕의 존재감: 3칸 안의 적이 이쪽을 노림
			for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
				if (m.alignment == Alignment.ENEMY && m.isAlive() && Dungeon.level.distance( pos, m.pos ) <= 3
						&& m.state != m.SLEEPING){
					m.aggro( this );
				}
			}
			return super.act();
		}
	}
}
