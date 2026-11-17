/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사의 숲의 완드
 *  - 영혼의 활처럼 끝없이 정령 탄을 쏘는 원거리 무기 (영웅 레벨에 따라 위력이 오름)
 *  - 지형지물을 촉매로 정령을 깃들게 함. 한 번에 3개체까지 담고, 가운데 칸의 정령 능력만 탄에 실림 (양옆은 저장)
 *  - 깃든 정령은 '정령 부여'로 다른 장비(무기·갑옷)에 옮겨 전용 인챈트가 되거나, '소환 해제'하기 전까지 사라지지 않음
 */

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SpiritRemnant;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TamedFireElemental;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WindParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndForestWand;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ForestWand extends Weapon {

	public static final String AC_SHOOT  = "SHOOT";
	public static final String AC_IMBUE  = "IMBUE";
	public static final String AC_SPIRITS = "SPIRITS";

	//촉매에서 정령을 불러올 수 있는 거리
	public static final int IMBUE_RANGE = 3;
	//완드에 담을 수 있는 정령 수와 능력이 발현되는 칸 (가운데)
	public static final int SLOTS = 3;
	public static final int CENTER = 1;

	public enum Spirit {
		WATER  ( false, 0x55B8FF ),
		WIND   ( false, 0xD8FFF0 ),
		NATURE ( false, 0x55DD33 ),
		FIRE   ( false, 0xFF8833 ),
		EARTH  ( false, 0xB08850 ),
		ROCK   ( false, 0x9A9AA6 ),
		FLAME  ( true,  0xFF3311 ),
		FROST  ( true,  0x99E6FF ),
		SHOCK  ( true,  0xFFF566 ),
		CHAOS  ( true,  0xD077FF );

		public final boolean strong;
		public final int color;

		Spirit( boolean strong, int color ){
			this.strong = strong;
			this.color = color;
		}

		public String title(){
			return Messages.get(ForestWand.class, "spirit_" + name().toLowerCase());
		}

		public ItemSprite.Glowing glowing(){
			return new ItemSprite.Glowing( color, strong ? 1f : 2f );
		}

		public Emitter.Factory particle(){
			switch (this){
				case WATER:  return FlowParticle.FACTORY;
				case WIND:   return WindParticle.FACTORY;
				case NATURE: return LeafParticle.GENERAL;
				case FIRE: case FLAME: return FlameParticle.FACTORY;
				case EARTH: case ROCK: return EarthParticle.SMALL;
				case FROST:  return SnowParticle.FACTORY;
				case SHOCK:  return SparkParticle.STATIC;
				default:     return RainbowParticle.BURST;
			}
		}
	}

	{
		image = ItemSpriteSheet.FOREST_WAND;

		defaultAction = AC_SHOOT;
		usesTargeting = true;

		unique = true;
		bones = false;
	}

	public Spirit[] slots = new Spirit[SLOTS];

	public Spirit active(){
		return slots[CENTER];
	}

	public int spiritCount(){
		int n = 0;
		for (Spirit s : slots) if (s != null) n++;
		return n;
	}

	//새 정령이 들어갈 칸: 가운데가 비었으면 가운데, 아니면 왼쪽·오른쪽 순
	public int freeSlot(){
		if (slots[CENTER] == null) return CENTER;
		for (int i = 0; i < SLOTS; i++) if (slots[i] == null) return i;
		return -1;
	}

	public void moveToCenter( int i ){
		Spirit t = slots[CENTER]; slots[CENTER] = slots[i]; slots[i] = t;
		updateQuickslot();
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.remove( AC_EQUIP );
		actions.add( AC_SHOOT );
		actions.add( AC_IMBUE );
		actions.add( AC_SPIRITS );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_SHOOT )) {
			curUser = hero;
			curItem = this;
			GameScene.selectCell( shooter );

		} else if (action.equals( AC_IMBUE )) {
			if (freeSlot() < 0){
				GLog.w( Messages.get( ForestWand.class, "full" ) );
				return;
			}
			curUser = hero;
			curItem = this;
			GameScene.selectCell( imbuer );

		} else if (action.equals( AC_SPIRITS )) {
			GameScene.show( new WndForestWand( this ) );
		}
	}

	@Override
	public String name() {
		Spirit s = active();
		if (s == null) return super.name();
		return Messages.get( this, "name_spirit", super.name(), s.title() );
	}

	@Override
	public String status() {
		return spiritCount() + "/" + SLOTS;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		if (active() != null) return active().glowing();
		return super.glowing();
	}

	// ── 위력: 영혼의 활처럼 영웅 레벨에 따라 오름 ──

	@Override
	public int STRReq( int lvl ) {
		return STRReq( 1, lvl );
	}

	@Override
	public int min( int lvl ) {
		return 1 + Dungeon.hero.lvl/5;
	}

	@Override
	public int max( int lvl ) {
		return 6 + (int)(Dungeon.hero.lvl/2.5f);
	}

	@Override
	public int level() {
		return Dungeon.hero == null ? 0 : Dungeon.hero.lvl/5;
	}

	@Override
	public int buffedLvl() {
		return level();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public int damageRoll( Char owner ) {
		int damage = augment.damageFactor( super.damageRoll( owner ) );
		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) damage += Hero.heroDamageIntRange( 0, exStr );
		}
		return damage;
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		Spirit s = active();
		if (s != null) damage = spiritHit( s, attacker, defender, damage, SHOT_MUL );
		return super.proc( attacker, defender, damage );
	}

	@Override
	public int targetingPos( Hero user, int dst ) {
		return knockBolt().targetingPos( user, dst );
	}

	public SpiritBolt knockBolt(){
		return new SpiritBolt();
	}

	// ── 정령 탄 ──
	public class SpiritBolt extends MissileWeapon {

		{
			image = ItemSpriteSheet.FOREST_BOLT;
			hitSound = Assets.Sounds.HIT_MAGIC;
			setID = 0;
		}

		@Override
		public ArrayList<String> actions( Hero hero ) {
			return new ArrayList<>();
		}

		@Override
		public String defaultAction() {
			return null;
		}

		@Override
		public int defaultQuantity() {
			return 1;
		}

		@Override
		public Emitter emitter() {
			Emitter e = new Emitter();
			e.pos( 5, 5 );
			e.fillTarget = false;
			Spirit s = active();
			e.pour( s != null ? s.particle() : LeafParticle.GENERAL, s != null && s.strong ? 0.015f : 0.025f );
			return e;
		}

		@Override
		public ItemSprite.Glowing glowing() {
			return ForestWand.this.glowing();
		}

		@Override
		public int damageRoll( Char owner ) {
			return ForestWand.this.damageRoll( owner );
		}

		@Override
		public boolean hasEnchant( Class<? extends Enchantment> type, Char owner ) {
			return ForestWand.this.hasEnchant( type, owner );
		}

		@Override
		public int proc( Char attacker, Char defender, int damage ) {
			return ForestWand.this.proc( attacker, defender, damage );
		}

		@Override
		public float delayFactor( Char user ) {
			return ForestWand.this.delayFactor( user );
		}

		@Override
		public int STRReq( int lvl ) {
			return ForestWand.this.STRReq();
		}

		@Override
		protected void onThrow( int cell ) {
			Char enemy = Actor.findChar( cell );
			//마법사 퀘스트: 갓 소환된 화염 정령은 정령 탄을 맞으면 싸우지 않고 길들여짐
			if (enemy instanceof Elemental.NewbornFireElemental && enemy.alignment == Char.Alignment.ENEMY){
				tame( curUser, (Elemental.NewbornFireElemental) enemy );
				return;
			}
			int col = active() != null ? active().color : 0x9CE07A;
			if (enemy == null || enemy == curUser) {
				parent = null;
				Splash.at( cell, 0xCC000000 | col, 1 );
			} else {
				if (!curUser.shoot( enemy, this )) {
					Splash.at( cell, 0xCC000000 | col, 1 );
				}
			}
		}

		@Override
		public Item split( int amount ) {
			return null;
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play( Assets.Sounds.ZAP, 1, Random.Float( 0.9f, 1.2f ) );
		}
	}

	private CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) knockBolt().cast( curUser, target );
		}
		@Override
		public String prompt() {
			return Messages.get( ForestWand.class, "shoot_prompt" );
		}
	};

	// ── 정령 능력 (탄·무기 인챈트에서 같이 씀) ──

	//정령 탄이 싣는 속성 피해 배율 (매번 나가므로 무기 인챈트보다 약하게)
	public static final float SHOT_MUL = 0.5f;

	//정령 능력이 나왔음을 눈에 띄게: 정령 색의 글자와 그 정령다운 입자
	public static void fx( Char ch, Spirit s, String key ){
		if (ch == null || ch.sprite == null || !ch.sprite.visible) return;
		ch.sprite.showStatus( s.color, Messages.get( ForestWand.class, "fx_" + key ) );
	}
	private static void burst( Char ch, Emitter.Factory f, int n ){
		if (ch != null && ch.sprite != null) ch.sprite.centerEmitter().burst( f, n );
	}

	// ── 속성 피해 ──
	//기준: 물 폭발(물 혼합물·간헐천 함정)이 화염 정령 같은 불의 적에게 주는 피해 (5+깊이 ~ 10+깊이×2)
	public static int elemBase(){
		int d = Dungeon.scalingDepth();
		return Random.NormalIntRange( 5 + d, 10 + d*2 );
	}

	//속성 피해의 출처: 피해 숫자 옆에 물·불·냉기·번개 아이콘이 뜸
	public static class ElemSource {
		public final Spirit spirit;
		public ElemSource( Spirit s ){ spirit = s; }
		public int icon(){
			switch (spirit){
				case WATER: return FloatingText.WATER;
				case FIRE: case FLAME: return FloatingText.BURNING;
				case FROST: return FloatingText.FROST;
				case SHOCK: return FloatingText.SHOCKING;
				default: return FloatingText.MAGIC_DMG;
			}
		}
	}

	private static Class resistClass( Spirit s ){
		switch (s){
			case FIRE: case FLAME: return Burning.class;
			case FROST: return Frost.class;
			case SHOCK: return Electricity.class;
			default: return null;
		}
	}

	//속성 피해를 입히고 실제 들어간 양을 돌려줌 (면역·저항 반영, 물은 불의 적에게, 번개는 물에 선 적에게 1.5배)
	public static int elemHit( Spirit s, Char target, float mul ){
		if (target == null || !target.isAlive() || mul <= 0) return 0;
		Class rc = resistClass( s );
		if (rc != null && target.isImmune( rc )) return 0;
		float dmg = elemBase() * mul;
		if (rc != null) dmg *= target.resist( rc );
		if (s == Spirit.WATER && Char.hasProp( target, Char.Property.FIERY )) dmg *= 1.5f;
		if (s == Spirit.SHOCK && Dungeon.level.water[target.pos] && !target.flying) dmg *= 1.5f;
		int d = Math.max( 1, Math.round( dmg ) );
		target.damage( d, new ElemSource( s ) );
		return d;
	}

	//얼리기는 맞는 순간 피해에 깨지므로, 공격이 끝난 직후에 얼림
	public static class FreezeNext extends Buff {
		@Override
		public boolean act() {
			if (target.isAlive()) Buff.prolong( target, Frost.class, 2f );
			detach();
			return true;
		}
	}

	private static void heal( Char ch, int amount ){
		if (ch == null || !ch.isAlive() || amount <= 0 || ch.HP >= ch.HT) return;
		amount = Math.min( amount, ch.HT - ch.HP );
		ch.HP += amount;
		if (ch.sprite != null){
			ch.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 2 );
			ch.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( amount ), FloatingText.HEALING );
		}
	}

	private static void shield( Char ch, int amount ){
		if (ch == null || !ch.isAlive() || amount <= 0) return;
		Buff.affect( ch, Barrier.class ).incShield( amount );
		if (ch.sprite != null) ch.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( amount ), FloatingText.SHIELDING );
	}

	private static boolean foe( Char attacker, Char other ){
		return other != null && other != attacker && other.isAlive()
				&& (attacker == null || other.alignment != attacker.alignment);
	}

	//맞은 대상에게 정령 능력을 걸고, 바뀐 피해량을 돌려줌. mul은 속성 피해 배율 (무기 1, 정령 탄 0.5)
	public static int spiritHit( Spirit s, Char attacker, Char defender, int damage, float mul ){
		if (defender == null) return damage;
		switch (s){

			//물: 물 폭발의 피해를 그대로. 불을 끄고, 발밑을 물바다로 만들고 1칸 밀어냄
			case WATER: {
				Buff.detach( defender, Burning.class );
				Splash.at( defender.sprite != null ? defender.sprite.center() : DungeonTilemap.tileCenterToWorld( defender.pos ), 0xFF55B8FF, 12 );
				burst( defender, FlowParticle.FACTORY, 12 );
				Sample.INSTANCE.play( Assets.Sounds.WATER, 0.9f, Random.Float( 0.9f, 1.1f ) );
				fx( defender, s, "water" );
				elemHit( s, defender, mul );
				Dungeon.level.setCellToWater( false, defender.pos );
				knockAway( attacker, defender, 1, false );
				break;
			}

			//바람: 칼바람 피해와 함께 2칸 날려 보내고(벽에 부딪히면 추가 피해), 쓰는 이는 순풍을 타 잠시 빨라짐
			case WIND:
				burst( defender, WindParticle.FACTORY, 12 );
				CellEmitter.get( defender.pos ).burst( Speck.factory( Speck.JET ), 5 );
				fx( defender, s, "wind" );
				elemHit( s, defender, mul*0.5f );
				knockAway( attacker, defender, 2, true );
				if (attacker != null && attacker.isAlive()) Buff.prolong( attacker, Haste.class, 2f );
				break;

			//자연: 가시 덩굴이 생기를 빨아들여 쓰는 이를 치유하고, 뿌리로 묶고, 발밑에 풀을 틔움
			case NATURE: {
				burst( defender, LeafParticle.GENERAL, 10 );
				CellEmitter.bottom( defender.pos ).burst( LeafParticle.GENERAL, 14 );
				int dealt = elemHit( s, defender, mul*0.5f );
				if (defender.isAlive() && !defender.flying && !defender.properties().contains( Char.Property.IMMOVABLE )){
					Buff.prolong( defender, Roots.class, 2f );
				}
				int t = Dungeon.level.map[defender.pos];
				if (t == Terrain.EMPTY || t == Terrain.EMBERS || t == Terrain.EMPTY_DECO){
					Level.set( defender.pos, Terrain.GRASS );
					GameScene.updateMap( defender.pos );
				}
				heal( attacker, Math.max( 1, dealt/2 ) );
				fx( defender, s, "nature" );
				break;
			}

			//불: 불을 붙임. 이미 타고 있는 적은 불길이 거세져 두 배로 탐
			case FIRE: {
				boolean burning = defender.buff( Burning.class ) != null;
				burst( defender, FlameParticle.FACTORY, burning ? 16 : 10 );
				fx( defender, s, burning ? "fire_blaze" : "fire" );
				elemHit( s, defender, mul*(burning ? 1f : 0.5f) );
				if (defender.isAlive()) Buff.affect( defender, Burning.class ).reignite( defender, 4f );
				break;
			}

			//땅: 흙더미가 발을 묶어 불구로 만들고, 떨어져 나온 흙이 쓰는 이를 감싸 보호막이 됨
			case EARTH: {
				burst( defender, EarthParticle.SMALL, 6 );
				CellEmitter.bottom( defender.pos ).burst( EarthParticle.FACTORY, 10 );
				int dealt = elemHit( s, defender, mul*0.5f );
				if (defender.isAlive()) Buff.prolong( defender, Cripple.class, 3f );
				shield( attacker, Math.max( 1, dealt/2 ) );
				fx( defender, s, "earth" );
				break;
			}

			//바위: 묵직한 일격(+20%)에 바위 파편 피해, 껍질을 부숴 3턴 동안 피해를 더 받게 함
			case ROCK:
				damage = Math.round( damage * 1.2f );
				CellEmitter.bottom( defender.pos ).burst( EarthParticle.FACTORY, 10 );
				burst( defender, EarthParticle.SMALL, 8 );
				if (defender.sprite != null && defender.sprite.visible) PixelScene.shake( 1, 0.15f );
				Sample.INSTANCE.play( Assets.Sounds.ROCKS, 0.7f );
				fx( defender, s, "rock" );
				elemHit( s, defender, mul*0.6f );
				if (defender.isAlive()) Buff.prolong( defender, Vulnerable.class, 3f );
				break;

			//화염: 대상이 폭발하듯 타오르고, 둘레의 적들도 불길에 휩싸임
			case FLAME:
				burst( defender, FlameParticle.FACTORY, 16 );
				fx( defender, s, "flame" );
				elemHit( s, defender, mul );
				if (defender.isAlive()) Buff.affect( defender, Burning.class ).reignite( defender );
				for (int n : PathFinder.NEIGHBOURS8){
					int c = defender.pos + n;
					if (!Dungeon.level.solid[c]) CellEmitter.get( c ).burst( FlameParticle.FACTORY, 6 );
					Char other = Actor.findChar( c );
					if (foe( attacker, other ) && other != defender){
						elemHit( s, other, mul*0.5f );
						if (other.isAlive()) Buff.affect( other, Burning.class ).reignite( other, 3f );
					}
				}
				if (defender.sprite != null && defender.sprite.visible) PixelScene.shake( 1, 0.1f );
				Sample.INSTANCE.play( Assets.Sounds.BURNING );
				break;

			//서리: 차갑게 하고, 이미 차가운 적은 얼리고, 얼어 있는 적은 산산조각 내 큰 피해
			case FROST: {
				burst( defender, SnowParticle.FACTORY, 12 );
				if (defender.buff( Frost.class ) != null){
					CellEmitter.get( defender.pos ).burst( Speck.factory( Speck.STEAM ), 8 );
					Sample.INSTANCE.play( Assets.Sounds.SHATTER );
					fx( defender, s, "frost_shatter" );
					elemHit( s, defender, mul*1.5f );
				} else if (defender.buff( Chill.class ) != null && !defender.isImmune( Frost.class )){
					fx( defender, s, "frost_freeze" );
					elemHit( s, defender, mul*0.75f );
					if (defender.isAlive()) Buff.affect( defender, FreezeNext.class );
				} else {
					fx( defender, s, "frost" );
					elemHit( s, defender, mul*0.75f );
					if (defender.isAlive()) Buff.prolong( defender, Chill.class, 3f );
				}
				break;
			}

			//전기: 번개가 둘레 2칸 안의 적 3명까지 옮겨 붙음. 물에 선 적은 감전되어 1.5배
			case SHOCK: {
				boolean wet = Dungeon.level.water[defender.pos] && !defender.flying;
				ArrayList<Char> affected = new ArrayList<>();
				ArrayList<Lightning.Arc> arcs = new ArrayList<>();
				Shocking.arc( attacker, defender, 2, affected, arcs );
				affected.remove( defender );
				elemHit( s, defender, mul*0.75f );
				int hits = 0;
				for (Char ch : affected){
					if (foe( attacker, ch ) && hits < 3){
						elemHit( s, ch, mul*0.4f );
						if (ch.sprite != null) ch.sprite.centerEmitter().burst( SparkParticle.FACTORY, 4 );
						hits++;
					}
				}
				if (attacker != null && attacker.sprite != null && attacker.sprite.parent != null && !arcs.isEmpty()){
					attacker.sprite.parent.addToFront( new Lightning( arcs, null ) );
				}
				burst( defender, SparkParticle.FACTORY, 10 );
				fx( defender, s, wet ? "shock_water" : hits > 0 ? "shock_chain" : "shock" );
				Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				break;
			}

			//혼돈: 서로 다른 두 정령의 힘이 한꺼번에 터지고, 가끔 정신을 흐리게 함
			case CHAOS: default: {
				Spirit[] pool = { Spirit.WATER, Spirit.WIND, Spirit.NATURE, Spirit.FIRE, Spirit.EARTH,
						Spirit.ROCK, Spirit.FLAME, Spirit.FROST, Spirit.SHOCK };
				Spirit a = Random.element( pool ), b;
				do { b = Random.element( pool ); } while (b == a);
				burst( defender, RainbowParticle.BURST, 14 );
				fx( defender, Spirit.CHAOS, "chaos" );
				if (Random.Int( 4 ) == 0 && defender.isAlive()){
					if (Random.Int( 2 ) == 0) Buff.prolong( defender, Vertigo.class, 3f );
					else                      Buff.prolong( defender, Blindness.class, 2f );
				}
				damage = spiritHit( a, attacker, defender, damage, mul*0.75f );
				if (defender.isAlive()) damage = spiritHit( b, attacker, defender, damage, mul*0.75f );
				break;
			}
		}
		return damage;
	}

	//설명용: 지금 깊이의 속성 피해 기준
	public static String elemBaseDesc(){
		int d = Dungeon.scalingDepth();
		return Messages.get( ForestWand.class, "elem_base", 5 + d, 10 + d*2 );
	}

	// ── 역소환: 장비에서 정령이 떨어져 나갈 때 (강화, 다른 정령·마법 부여) 알아서 숲의 완드로 돌아옴 ──
	public static void returnToWand( Spirit s ){
		Hero hero = Dungeon.hero;
		if (s == null || hero == null || !hero.isAlive()) return;
		ForestWand wand = hero.belongings.getItem( ForestWand.class );
		int slot = wand != null ? wand.freeSlot() : -1;
		if (slot >= 0){
			wand.slots[slot] = s;
			updateQuickslot();
			if (hero.sprite != null){
				hero.sprite.centerEmitter().burst( s.particle(), 8 );
				hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
			}
			GLog.i( Messages.get( ForestWand.class, "returned", s.title() ) );
		} else {
			if (hero.sprite != null) hero.sprite.centerEmitter().burst( s.particle(), 5 );
			GLog.w( Messages.get( ForestWand.class, "returned_lost", s.title() ) );
		}
	}

	//대상을 기준점 반대쪽으로 밀어냄
	public static void knockAway( Char from, Char target, int power, boolean collide ){
		if (from == null || !target.isAlive() || target.properties().contains( Char.Property.IMMOVABLE )) return;
		int w = Dungeon.level.width();
		int fx = from.pos % w, fy = from.pos / w, tx = target.pos % w, ty = target.pos / w;
		int dx = Integer.signum( tx - fx ), dy = Integer.signum( ty - fy );
		if (dx == 0 && dy == 0) return;
		int ex = Math.max( 0, Math.min( w - 1, tx + dx*8 ) );
		int ey = Math.max( 0, Math.min( Dungeon.level.height() - 1, ty + dy*8 ) );
		Ballistica trajectory = new Ballistica( target.pos, ex + ey*w, Ballistica.MAGIC_BOLT );
		if (trajectory.dist > 0){
			WandOfBlastWave.throwChar( target, trajectory, power, true, collide, ForestWand.class );
		}
	}

	// ── 정령 깃들이기 ──

	//칸에 있는 촉매에서 얻을 수 있는 정령 (없으면 null)
	public static Spirit catalystAt( int cell ){
		Level level = Dungeon.level;

		for (Class<? extends SpiritRemnant> c : SpiritRemnant.TYPES){
			Blob b = level.blobs.get( c );
			if (b != null && b.volume > 0 && b.cur[cell] > 0){
				return ((SpiritRemnant) b).spirit();
			}
		}

		Blob fire = level.blobs.get( Fire.class );
		if (fire != null && fire.volume > 0 && fire.cur[cell] > 0) return Spirit.FIRE;

		int t = level.map[cell];
		if (t == Terrain.WALL_DECO && level instanceof PrisonLevel) return Spirit.FIRE;   //감옥 벽의 횃불
		if (level.water[cell] || t == Terrain.WATER) return Spirit.WATER;
		if (t == Terrain.HIGH_GRASS || t == Terrain.FURROWED_GRASS) return Random.Int(2) == 0 ? Spirit.WIND : Spirit.NATURE;
		//맨땅: 땅의 정령 또는 바위의 정령
		if (t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMPTY_SP || t == Terrain.GRASS || t == Terrain.EMBERS){
			return Random.Int(2) == 0 ? Spirit.EARTH : Spirit.ROCK;
		}

		return null;
	}

	//촉매를 쓰고 난 뒤 지형 변화 (긴 풀은 짓밟히고, 불·잔류 정령은 사라짐)
	private static void consumeCatalyst( int cell, Spirit s ){
		Level level = Dungeon.level;
		for (Class<? extends SpiritRemnant> c : SpiritRemnant.TYPES){
			Blob b = level.blobs.get( c );
			if (b != null && b.cur[cell] > 0) { b.clear( cell ); return; }
		}
		if (s == Spirit.FIRE){
			Blob fire = level.blobs.get( Fire.class );
			if (fire != null && fire.cur[cell] > 0) fire.clear( cell );
		} else if ((s == Spirit.WIND || s == Spirit.NATURE)
				&& (level.map[cell] == Terrain.HIGH_GRASS || level.map[cell] == Terrain.FURROWED_GRASS)){
			Level.set( cell, Terrain.GRASS );
			GameScene.updateMap( cell );
		}
	}

	public static void imbueFx( int from, Spirit s ){
		switch (s){
			case WATER:  CellEmitter.get( from ).burst( Speck.factory( Speck.STEAM ), 6 ); break;
			case WIND:   CellEmitter.get( from ).burst( Speck.factory( Speck.JET ), 6 ); break;
			case NATURE: CellEmitter.get( from ).burst( LeafParticle.GENERAL, 10 ); break;
			case EARTH: case ROCK: CellEmitter.get( from ).burst( EarthParticle.FACTORY, 10 ); break;
			case FIRE: case FLAME: CellEmitter.get( from ).burst( FlameParticle.FACTORY, 10 ); break;
			case FROST:  CellEmitter.get( from ).burst( SnowParticle.FACTORY, 12 ); break;
			case SHOCK:  CellEmitter.center( from ).burst( SparkParticle.FACTORY, 10 ); break;
			case CHAOS:  CellEmitter.center( from ).burst( RainbowParticle.BURST, 12 ); break;
			default: break;
		}
	}

	private static int missileFor( Spirit s ){
		switch (s){
			case FIRE: case FLAME: return MagicMissile.FIRE;
			case FROST: case WATER: return MagicMissile.FROST;
			case WIND: return MagicMissile.FORCE;
			case EARTH: case ROCK: return MagicMissile.EARTH;
			case SHOCK: return MagicMissile.SHAMAN_BLUE;
			case CHAOS: return MagicMissile.RAINBOW;
			default: return MagicMissile.FOLIAGE;
		}
	}

	protected static CellSelector.Listener imbuer = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target == null || !(curItem instanceof ForestWand)) return;
			final ForestWand wand = (ForestWand) curItem;
			final Hero hero = curUser;
			final int cell = target;

			if (!Dungeon.level.heroFOV[cell] || Dungeon.level.distance( hero.pos, cell ) > IMBUE_RANGE){
				GLog.w( Messages.get( ForestWand.class, "too_far", IMBUE_RANGE ) );
				return;
			}

			//마법사 퀘스트: 갓 소환된 화염 정령은 싸우지 않고 길들일 수 있음
			Char ch = Actor.findChar( cell );
			if (ch instanceof Elemental.NewbornFireElemental && ch.alignment == Char.Alignment.ENEMY){
				hero.sprite.zap( cell );
				tame( hero, (Elemental.NewbornFireElemental) ch );
				hero.spendAndNext( 1f );
				return;
			}

			final int slot = wand.freeSlot();
			if (slot < 0){
				GLog.w( Messages.get( ForestWand.class, "full" ) );
				return;
			}

			final Spirit s = catalystAt( cell );
			if (s == null){
				GLog.w( Messages.get( ForestWand.class, "no_catalyst" ) );
				return;
			}

			hero.busy();
			hero.sprite.operate( cell );
			imbueFx( cell, s );
			consumeCatalyst( cell, s );
			Sample.INSTANCE.play( s.strong ? Assets.Sounds.CHARGEUP : Assets.Sounds.MELD );
			((MagicMissile) hero.sprite.parent.recycle( MagicMissile.class )).reset(
					missileFor( s ), cell, hero.pos, new Callback() {
						@Override
						public void call() {
							wand.slots[slot] = s;
							updateQuickslot();
							hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), s.strong ? 8 : 5 );
							hero.sprite.showStatus( s.color, s.title() );
							GLog.p( Messages.get( ForestWand.class, slot == CENTER ? "imbued_center" : "imbued_stored", s.title() ) );
							hero.spendAndNext( 1f );
						}
					} );
		}

		@Override
		public String prompt() {
			return Messages.get( ForestWand.class, "imbue_prompt" );
		}
	};

	//갓 태어난 화염 정령을 길들임: 동료가 되어 따라오고, 정령의 잉걸불 2개를 얻음
	public static void tame( Hero hero, Elemental.NewbornFireElemental el ){
		int pos = el.pos;
		CellEmitter.get( pos ).burst( ElmoParticle.FACTORY, 20 );
		CellEmitter.get( pos ).start( Speck.factory( Speck.HEART ), 0.2f, 4 );
		el.destroy();
		if (el.sprite != null) el.sprite.killAndErase();

		TamedFireElemental pet = new TamedFireElemental();
		pet.pos = pos;
		GameScene.add( pet );
		Sample.INSTANCE.play( Assets.Sounds.BURNING );

		for (int i = 0; i < 2; i++){
			Item e = new Embers();
			if (!e.collect()){
				Dungeon.level.drop( e, hero.pos ).sprite.drop();
			}
		}
		Statistics.questScores[1] += 2000;
		GLog.p( Messages.get( ForestWand.class, "tamed" ) );

		if (Dungeon.level instanceof PrisonLevel){
			((PrisonLevel) Dungeon.level).updateWandmakerQuestMusic();
		}
	}

	// ── 설명 ──

	@Override
	public String info() {
		String info = desc();
		info += "\n\n" + Messages.get( ForestWand.class, "stats",
				Math.round( augment.damageFactor( min() ) ), Math.round( augment.damageFactor( max() ) ), STRReq() );
		if (STRReq() > Dungeon.hero.STR()) {
			info += " " + Messages.get( Weapon.class, "too_heavy" );
		} else if (Dungeon.hero.STR() > STRReq()){
			info += " " + Messages.get( Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq() );
		}
		switch (augment) {
			case SPEED: info += "\n\n" + Messages.get( Weapon.class, "faster" ); break;
			case DAMAGE: info += "\n\n" + Messages.get( Weapon.class, "stronger" ); break;
			case NONE:
		}

		info += "\n\n" + Messages.get( ForestWand.class, "slots_title" );
		for (int i = 0; i < SLOTS; i++){
			String where = Messages.get( ForestWand.class, "slot_" + i );
			info += "\n" + where + ": " + (slots[i] == null ? Messages.get( ForestWand.class, "slot_empty" ) : slots[i].title());
		}
		if (active() != null){
			info += "\n\n" + Messages.get( ForestWand.class, "shot_" + active().name().toLowerCase() );
			info += "\n\n" + elemBaseDesc() + " " + Messages.get( ForestWand.class, "shot_mul" );
		}
		else info += "\n\n" + Messages.get( ForestWand.class, "shot_none" );

		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.capitalize( Messages.get( Weapon.class, "enchanted", enchantment.name() ) );
			info += " " + enchantment.desc();
		}
		if (cursed && isEquipped( Dungeon.hero )) {
			info += "\n\n" + Messages.get( Weapon.class, "cursed_worn" );
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get( Weapon.class, "cursed" );
		}
		info += "\n\n" + Messages.get( ForestWand.class, "catalysts", IMBUE_RANGE );
		return info;
	}

	@Override
	public int value() {
		return 0;
	}

	private static final String SLOT = "slot_";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		for (int i = 0; i < SLOTS; i++){
			if (slots[i] != null) bundle.put( SLOT + i, slots[i].name() );
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		for (int i = 0; i < SLOTS; i++){
			slots[i] = null;
			if (bundle.contains( SLOT + i )){
				try { slots[i] = Spirit.valueOf( bundle.getString( SLOT + i ) ); } catch (Exception e) { slots[i] = null; }
			}
		}
		//예전 저장(정령 하나만 담던 완드)에서 넘어온 경우
		if (spiritCount() == 0 && bundle.contains( "spirit" )){
			try { slots[CENTER] = Spirit.valueOf( bundle.getString( "spirit" ) ); } catch (Exception e) { slots[CENTER] = null; }
		}
	}
}
