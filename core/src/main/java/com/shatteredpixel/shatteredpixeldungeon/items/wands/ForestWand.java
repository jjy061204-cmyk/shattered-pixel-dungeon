/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사의 숲의 완드
 *  - 영혼의 활처럼 끝없이 정령 탄을 쏘는 원거리 무기 (영웅 레벨에 따라 위력이 오름)
 *  - 지형지물을 촉매로 정령을 깃들게 함. 한 번에 3개체까지 담고, 가운데 칸의 정령 능력만 탄에 실림 (양옆은 저장)
 *  - 깃든 정령은 '정령 부여'로 다른 장비(무기·갑옷)에 옮겨 전용 인챈트가 되거나, '소환 해제'하기 전까지 사라지지 않음
 */

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
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
		if (s != null) damage = spiritHit( s, attacker, defender, damage, buffedLvl() );
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

	//맞은 대상에게 정령 능력을 걸고, 바뀐 피해량을 돌려줌
	public static int spiritHit( Spirit s, Char attacker, Char defender, int damage, int power ){
		if (s == Spirit.CHAOS){
			Spirit[] pool = { Spirit.FLAME, Spirit.FROST, Spirit.SHOCK, Spirit.ROCK, Spirit.WIND, Spirit.NATURE };
			Spirit pick = Random.element( pool );
			if (defender.sprite != null) defender.sprite.centerEmitter().burst( RainbowParticle.BURST, 6 );
			if (Random.Int( 6 ) == 0 && defender.isAlive()) Buff.prolong( defender, Paralysis.class, 1f );
			if (Random.Int( 6 ) == 0 && defender.isAlive()) Buff.prolong( defender, Vertigo.class, 3f );
			if (Random.Int( 6 ) == 0 && defender.isAlive()) Buff.prolong( defender, Blindness.class, 2f );
			return spiritHit( pick, attacker, defender, damage, power );
		}
		switch (s){
			case WATER:
				Buff.detach( defender, Burning.class );
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( FlowParticle.FACTORY, 6 );
				knockAway( attacker, defender, 1, false );
				break;
			case WIND:
				knockAway( attacker, defender, 2, true );
				break;
			case NATURE:
				if (Random.Int( 3 ) == 0 && !defender.flying) Buff.prolong( defender, Roots.class, 2f );
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( LeafParticle.GENERAL, 5 );
				break;
			case FIRE:
				if (Random.Int( 3 ) == 0) Buff.affect( defender, Burning.class ).reignite( defender, 4f );
				break;
			case EARTH:
				if (Random.Int( 2 ) == 0) Buff.prolong( defender, Cripple.class, 3f );
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( EarthParticle.SMALL, 5 );
				break;
			case ROCK:
				damage = Math.round( damage * 1.3f );
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( EarthParticle.FACTORY, 4 );
				break;
			case FLAME:
				Buff.affect( defender, Burning.class ).reignite( defender );
				for (int n : PathFinder.NEIGHBOURS8){
					Char other = Actor.findChar( defender.pos + n );
					if (other != null && other != attacker && other.alignment != attacker.alignment){
						other.damage( Math.max( 1, damage/3 ), ForestWand.class );
						CellEmitter.get( other.pos ).burst( FlameParticle.FACTORY, 4 );
					}
				}
				break;
			case FROST:
				Buff.prolong( defender, Chill.class, 3f );
				if (Random.Int( 4 ) == 0) Buff.prolong( defender, Frost.class, 2f );
				break;
			case SHOCK:
				ArrayList<Char> affected = new ArrayList<>();
				ArrayList<Lightning.Arc> arcs = new ArrayList<>();
				Shocking.arc( attacker, defender, 2, affected, arcs );
				affected.remove( defender );
				int hits = 0;
				for (Char ch : affected){
					if (ch.alignment != attacker.alignment && hits < 2){
						ch.damage( Math.max( 1, damage/2 ), ForestWand.class );
						hits++;
					}
				}
				if (attacker.sprite != null && attacker.sprite.parent != null) attacker.sprite.parent.addToFront( new Lightning( arcs, null ) );
				Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				break;
			default: break;
		}
		return damage;
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
		if (active() != null) info += "\n\n" + Messages.get( ForestWand.class, "shot_" + active().name().toLowerCase() );
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
