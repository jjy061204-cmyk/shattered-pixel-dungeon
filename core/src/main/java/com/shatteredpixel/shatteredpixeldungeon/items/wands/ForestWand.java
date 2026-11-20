/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사의 숲의 완드
 *  - 영혼의 활처럼 끝없이 정령 탄을 쏘는 원거리 무기 (영웅 레벨에 따라 위력이 오름)
 *  - 지형지물을 촉매로 정령을 깃들게 함. 한 번에 3개체까지 담고, 가운데 칸의 정령 능력만 탄에 실림 (양옆은 저장)
 *  - 깃든 정령은 '정령 부여'로 다른 장비(무기·갑옷)에 옮겨 전용 인챈트가 되거나, '소환 해제'하기 전까지 사라지지 않음
 */

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.ModernLevel;
import com.shatteredpixel.shatteredpixeldungeon.actors.elemental.ElementalDamage;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.SpiritEnchant;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.SpiritGlyph;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GreaterSpirit;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
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
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.StraightTrail;
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
import com.watabou.noosa.Image;
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
	public static final String AC_CONTRACT = "CONTRACT";   //정령 계약자: 정령계에서 상급 정령 소환

	//촉매에서 정령을 불러올 수 있는 거리
	public static final int IMBUE_RANGE = 3;
	//촉매 감응: 깃들이기 거리 +1/+2
	public static int imbueRange( Hero hero ){
		return IMBUE_RANGE + (hero != null ? hero.pointsInTalent( Talent.CATALYST_SENSE ) : 0);
	}
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
		GOLD   ( false, 0xFFD54A ),   //바닥에 떨어진 금화
		DARK   ( false, 0x8A66CC ),   //낭떠러지의 어둠
		FLAME  ( true,  0xFF3311 ),
		FROST  ( true,  0x99E6FF ),
		SHOCK  ( true,  0xFFF566 ),
		CHAOS  ( true,  0xD077FF ),
		RUNE   ( true,  0x3FE0C8 ),   //고대의 성소에 새겨진 룬 마법
		//정령 계약자가 정령계에서 불러오는 상급 정령 (각각 하나뿐)
		UNDINE     ( true, 0x3FA8FF, true ),
		SALAMANDER ( true, 0xFF5A1E, true ),
		GNOME      ( true, 0xD9A55A, true ),
		SYLPH      ( true, 0x9CFFC8, true ),
		//서리 물약·서리 함정의 서리 구름 (서리 정령의 약한 형제. 순서를 지키려고 맨 끝에 둠)
		ICE    ( false, 0xBFEFFF );

		public final boolean strong;
		public final boolean greater;
		public final int color;

		Spirit( boolean strong, int color ){
			this( strong, color, false );
		}

		Spirit( boolean strong, int color, boolean greater ){
			this.strong = strong;
			this.color = color;
			this.greater = greater;
		}

		public String title(){
			return Messages.get(ForestWand.class, "spirit_" + name().toLowerCase());
		}

		public ItemSprite.Glowing glowing(){
			return glowing( null );
		}

		//모드: 장비에 깃든 정령의 빛. 늘 은은하게 물들어 있고, 정령이 둘이면 두 색이 번갈아 빛남
		public ItemSprite.Glowing glowing( Spirit other ){
			ItemSprite.Glowing g = new ItemSprite.Glowing( color, strong ? 0.7f : 1.0f );
			g.min = strong ? 0.22f : 0.15f;
			g.max = strong ? 0.75f : 0.65f;
			if (other != null && other != this) g.color2 = other.color;
			return g;
		}

		//작은 정령 아이콘 (퀵슬롯·정령 관리 창·정령 바꾸기 버튼)
		public int icon(){
			return ItemSpriteSheet.Icons.spirit( ordinal() );
		}

		public Emitter.Factory particle(){
			switch (this){
				case WATER: case UNDINE:  return FlowParticle.FACTORY;
				case WIND: case SYLPH:    return WindParticle.FACTORY;
				case NATURE: return LeafParticle.GENERAL;
				case FIRE: case FLAME: case SALAMANDER: return FlameParticle.FACTORY;
				case EARTH: case ROCK: case GNOME: return EarthParticle.SMALL;
				case FROST: case ICE: return SnowParticle.FACTORY;
				case SHOCK:  return SparkParticle.STATIC;
				case GOLD:   return Speck.factory( Speck.YELLOW_LIGHT );
				case DARK:   return ShadowParticle.UP;
				case RUNE:   return MagicMissile.WardParticle.FACTORY;
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
	//모드: 정령 관리 창에서 고른 빈칸. 깃들이기·되돌리기·실체 거두기가 그 칸부터 채움.
	//한 번 쓰면 -1로 돌아가며 저장하지 않음
	public int preferredSlot = -1;

	public int freeSlot(){
		if (preferredSlot >= 0 && preferredSlot < SLOTS && slots[preferredSlot] == null) return preferredSlot;
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
		actions.remove( AC_THROW );   //완드를 던질 일은 없으니 버튼 자리를 비움
		actions.add( AC_SHOOT );
		actions.add( AC_IMBUE );
		actions.add( AC_SPIRITS );
		if (hero.subClass == HeroSubClass.SPIRIT_CONTRACTOR) actions.add( AC_CONTRACT );
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
			//현대 세계에는 깃들 정령이 없음
			if (Dungeon.level instanceof ModernLevel){
				GLog.w( Messages.get( ForestWand.class, "no_spirits_modern" ) );
				return;
			}
			if (freeSlot() < 0){
				GLog.w( Messages.get( ForestWand.class, "full" ) );
				return;
			}
			curUser = hero;
			curItem = this;
			GameScene.selectCell( imbuer );

		} else if (action.equals( AC_SPIRITS )) {
			GameScene.show( new WndForestWand( this ) );

		} else if (action.equals( AC_CONTRACT )) {
			contract( hero );
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
		//퀵슬롯 오른쪽 위에 발현 중인 정령 아이콘이 뜸
		icon = active() != null ? active().icon() : -1;
		return spiritCount() + "/" + SLOTS;
	}

	//정령 바꾸기: 가운데(발현) → 오른쪽 → 왼쪽 순서로 한 칸씩 돌림 (빈 칸은 건너뜀)
	public Spirit rotate(){
		int[] order = { CENTER, 2, 0 };
		ArrayList<Integer> filled = new ArrayList<>();
		for (int i : order) if (slots[i] != null) filled.add( i );
		if (slots[CENTER] == null && !filled.isEmpty()){
			//가운데가 비어 있으면 저장된 첫 정령을 가운데로
			int f = filled.get( 0 );
			slots[CENTER] = slots[f]; slots[f] = null;
			updateQuickslot();
			return active();
		}
		if (filled.size() < 2) return active();
		Spirit[] vals = new Spirit[filled.size()];
		for (int k = 0; k < vals.length; k++) vals[k] = slots[filled.get( k )];
		//가운데에 다음 정령이 오도록 한 칸씩 당김
		for (int k = 0; k < vals.length; k++) slots[filled.get( k )] = vals[(k + 1) % vals.length];
		updateQuickslot();
		return active();
	}

	//다음에 가운데로 올 정령 (정령 바꾸기 버튼에 보여 줌)
	public Spirit nextSpirit(){
		int[] order = { CENTER, 2, 0 };
		ArrayList<Integer> filled = new ArrayList<>();
		for (int i : order) if (slots[i] != null) filled.add( i );
		if (slots[CENTER] == null) return filled.isEmpty() ? null : slots[filled.get( 0 )];
		if (filled.size() < 2) return null;
		return slots[filled.get( 1 )];
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
		float mul = SHOT_MUL;
		//정령의 식사: 음식을 먹은 뒤 몇 발은 정령 능력이 1.5배
		Talent.SpiritedMeal meal = attacker.buff( Talent.SpiritedMeal.class );
		if (meal != null && s != null){
			mul *= 1.5f;
			meal.use();
		}
		if (s != null) damage = spiritHit( s, attacker, defender, damage, mul );
		//정령 전이: 정령 탄이 맞으면 손에 든 무기의 정령도 15/30/45% 확률로 발동
		if (attacker instanceof Hero && ((Hero) attacker).hasTalent( Talent.SPIRIT_CONDUIT )
				&& ((Hero) attacker).belongings.weapon() instanceof Weapon
				&& ((Weapon) ((Hero) attacker).belongings.weapon()).enchantment instanceof SpiritEnchant){
			SpiritEnchant e = (SpiritEnchant) ((Weapon) ((Hero) attacker).belongings.weapon()).enchantment;
			float chance = 0.15f * ((Hero) attacker).pointsInTalent( Talent.SPIRIT_CONDUIT );
			float emul = SpiritEnchant.enchanter( attacker ) ? 1.3f : 1f;
			if (defender.isAlive() && Random.Float() < chance) damage = spiritHit( e.spirit, attacker, defender, damage, emul );
			if (e.spirit2 != null && defender.isAlive() && Random.Float() < chance) damage = spiritHit( e.spirit2, attacker, defender, damage, emul );
		}
		//정령 공명: 양옆 칸 정령도 15/30% 확률로 절반의 힘을 보탬
		if (attacker instanceof Hero && ((Hero) attacker).hasTalent( Talent.SPIRIT_RESONANCE )){
			float chance = 0.15f * ((Hero) attacker).pointsInTalent( Talent.SPIRIT_RESONANCE );
			for (int i = 0; i < SLOTS; i++){
				if (i == CENTER || slots[i] == null || !defender.isAlive()) continue;
				if (Random.Float() < chance) damage = spiritHit( slots[i], attacker, defender, damage, SHOT_MUL*0.5f );
			}
		}
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

		//탄 그림: 깃든 정령마다 다른 모양 (불덩이·물방울·얼음 창·번개·나뭇잎·바위·룬 등)
		@Override
		public int image() {
			Spirit s = active();
			return s != null ? ItemSpriteSheet.spiritBolt( s.ordinal() ) : ItemSpriteSheet.FOREST_BOLT;
		}

		//궤도: 정령 고유 입자를 탄 그림의 한가운데에서 뿌리되, 뿌린 자리에 멈춰 두어 지나간 길을 따라 곧게 남김
		@Override
		public Emitter emitter() {
			Emitter e = new StraightTrail();
			e.fillTarget = false;
			Spirit s = active();
			if (s == null) e.pos( 5, 5 );
			else           e.pos( 6, 6 );
			e.pour( s != null ? s.particle() : LeafParticle.GENERAL, s != null && s.strong ? 0.015f : 0.025f );
			return e;
		}

		//탄 그림에 이미 정령 빛깔이 있으니 덧칠하지 않음
		@Override
		public ItemSprite.Glowing glowing() {
			return null;
		}

		//날아가는 동안 도는 빠르기 (0이면 날아가는 쪽을 향해 곧게)
		public int spin(){
			Spirit s = active();
			if (s == null) return 720;
			switch (s){
				case EARTH: case ROCK: case GNOME: case NATURE: return 540;
				case GOLD:  return 900;
				case CHAOS: return 720;
				case RUNE:  return 360;
				default:    return 0;
			}
		}

		//쏘는 순간 완드 끝에서 정령 입자가 조금 튐
		@Override
		public void cast( Hero user, int dst ) {
			Spirit s = active();
			if (s != null && user.sprite != null) user.sprite.centerEmitter().burst( s.particle(), 3 );
			super.cast( user, dst );
		}

		//닿은 자리에서 정령 빛깔로 터짐 (맞든 빗나가든)
		private void impactFx( int cell ){
			if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length() || !Dungeon.level.heroFOV[cell]) return;
			Spirit s = active();
			int col = s != null ? s.color : 0x9CE07A;
			boolean big = s != null && s.strong;
			Splash.at( cell, 0xCC000000 | col, big ? 7 : 4 );
			CellEmitter.center( cell ).burst( s != null ? s.particle() : LeafParticle.GENERAL, big ? 8 : 5 );
			if (curUser != null && curUser.sprite != null && curUser.sprite.parent != null){
				new Flare( 6, big ? 14 : 10 ).color( col, true )
						.show( curUser.sprite.parent, DungeonTilemap.tileCenterToWorld( cell ), 0.35f );
			}
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
			impactFx( cell );
			Char enemy = Actor.findChar( cell );
			//마법사 퀘스트: 갓 소환된 화염 정령은 정령 탄을 맞으면 싸우지 않고 길들여짐
			if (enemy instanceof Elemental.NewbornFireElemental && enemy.alignment == Char.Alignment.ENEMY){
				tame( curUser, (Elemental.NewbornFireElemental) enemy );
				return;
			}
			if (enemy == null || enemy == curUser) {
				parent = null;
			} else {
				curUser.shoot( enemy, this );
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

	//속성 피해를 입히고 실제 들어간 양을 돌려줌 (면역·저항 반영, 물은 불의 적에게, 번개는 물에 선 적에게 1.5배)
	public static int elemHit( Spirit s, Char target, float mul ){
		if (target == null || !target.isAlive() || mul <= 0) return 0;
		ElementalDamage src = ElementalDamage.of( s );
		Class rc = src.legacyResist();
		if (target.isImmune( src.getClass() )) return 0;
		if (rc != null && target.isImmune( rc )) return 0;
		float dmg = elemBase() * mul;
		if (rc != null) dmg *= target.resist( rc );
		dmg *= target.resist( src.getClass() );
		if (s == Spirit.WATER && Char.hasProp( target, Char.Property.FIERY )) dmg *= 1.5f;
		if (s == Spirit.SHOCK && Dungeon.level.water[target.pos] && !target.flying) dmg *= 1.5f;
		//원소 숙련: 정령사 편이 입히는 속성 피해 +15/30/45%
		if (Dungeon.hero != null && target != Dungeon.hero && target.alignment != Char.Alignment.ALLY){
			dmg *= 1f + 0.15f * Dungeon.hero.pointsInTalent( Talent.ELEMENTAL_MASTERY );
		}
		int d = Math.max( 1, Math.round( dmg ) );
		target.damage( d, src );
		return d;
	}

	//룬 각인: 10턴 동안 남고, 셋이 쌓이면 터짐
	public static class RuneMark extends FlavourBuff {
		{ type = buffType.NEGATIVE; announced = false; }
		public int runes = 0;
		@Override public int icon() { return BuffIndicator.MARK; }
		@Override public void tintIcon( Image icon ) { icon.hardlight( 0.25f, 0.9f, 0.8f ); }
		@Override public String iconTextDisplay() { return Integer.toString( runes ); }
		@Override public String desc() { return Messages.get( this, "desc", runes, dispTurns() ); }
		@Override public void storeInBundle( Bundle b ) { super.storeInBundle( b ); b.put( "runes", runes ); }
		@Override public void restoreFromBundle( Bundle b ) { super.restoreFromBundle( b ); runes = b.getInt( "runes" ); }
	}

	public static void runeMark( Spirit s, Char attacker, Char target, float mul ){
		RuneMark m = Buff.prolong( target, RuneMark.class, 10f );
		if (m == null) return;
		m.runes++;
		if (m.runes < 3){
			fx( target, Spirit.RUNE, "rune_" + m.runes );
			Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 0.4f, 1.4f + 0.2f*m.runes );
			return;
		}
		//룬 폭발
		m.detach();
		fx( target, Spirit.RUNE, "rune_burst" );
		Sample.INSTANCE.play( Assets.Sounds.BLAST, 0.8f, 1.3f );
		for (int n : PathFinder.NEIGHBOURS9){
			int c = target.pos + n;
			if (!Dungeon.level.solid[c]) CellEmitter.center( c ).burst( MagicMissile.WardParticle.FACTORY, 6 );
		}
		if (target.sprite != null && target.sprite.visible) PixelScene.shake( 1, 0.15f );
		elemHit( Spirit.RUNE, target, mul*1.5f );
		for (int n : PathFinder.NEIGHBOURS8){
			Char other = Actor.findChar( target.pos + n );
			if (foe( attacker, other )) elemHit( Spirit.RUNE, other, mul*0.5f );
		}
	}

	//황금의 정령에 물든 적: 이 동안 쓰러지면 금화를 떨어뜨림 (Mob.die에서 확인)
	public static class Gilded extends FlavourBuff {
		{ type = buffType.NEUTRAL; announced = false; }
	}
	public static void onMobDeath( Mob mob ){
		if (mob.buff( Gilded.class ) == null || Dungeon.level == null) return;
		int amount = Random.IntRange( 2, 4 + Dungeon.scalingDepth() );
		Dungeon.level.drop( new Gold( amount ), mob.pos ).sprite.drop();
		CellEmitter.center( mob.pos ).burst( Speck.factory( Speck.COIN ), 6 );
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

			//얼음: 얼음 조각이 박혀 차갑게 함. 이미 차가운 적은 더 오래 차가워지고, 발밑 물이 얼어 미끄러져 넘어짐(1턴 불구)
			case ICE: {
				burst( defender, SnowParticle.FACTORY, 8 );
				boolean chilled = defender.buff( Chill.class ) != null;
				fx( defender, s, chilled ? "ice_deep" : "ice" );
				elemHit( s, defender, mul*0.5f );
				if (defender.isAlive()){
					Buff.prolong( defender, Chill.class, chilled ? 5f : 3f );
					if (Dungeon.level.water[defender.pos] && !defender.flying) Buff.prolong( defender, Cripple.class, 2f );
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

			//운디네: 물의 힘이 훨씬 거세고, 준 피해의 1/3만큼 쓰는 이를 치유
			case UNDINE: {
				fx( defender, s, "undine" );
				int before = defender.HP;
				damage = spiritHit( Spirit.WATER, attacker, defender, damage, mul*1.5f );
				heal( attacker, Math.max( 1, (before - Math.max( 0, defender.HP ))/3 ) );
				break;
			}

			//샐러맨더: 화염 폭발이 더 거세게 번지고, 타고 있는 적은 불길이 거세짐
			case SALAMANDER:
				fx( defender, s, "salamander" );
				if (defender.buff( Burning.class ) != null) elemHit( Spirit.FIRE, defender, mul*0.5f );
				damage = spiritHit( Spirit.FLAME, attacker, defender, damage, mul*1.25f );
				break;

			//노움: 바위의 일격과 대지 결속이 한꺼번에
			case GNOME:
				fx( defender, s, "gnome" );
				damage = spiritHit( Spirit.ROCK, attacker, defender, damage, mul );
				if (defender.isAlive()) damage = spiritHit( Spirit.EARTH, attacker, defender, damage, mul );
				break;

			//실피드: 거센 돌풍으로 날려 보내고, 둘레의 적도 칼바람에 베임
			case SYLPH:
				fx( defender, s, "sylph" );
				for (int n : PathFinder.NEIGHBOURS8){
					Char other = Actor.findChar( defender.pos + n );
					if (foe( attacker, other ) && other != defender){
						elemHit( Spirit.WIND, other, mul*0.4f );
						if (other.sprite != null) other.sprite.centerEmitter().burst( WindParticle.FACTORY, 5 );
					}
				}
				damage = spiritHit( Spirit.WIND, attacker, defender, damage, mul*1.5f );
				if (attacker != null && attacker.isAlive()) Buff.prolong( attacker, Haste.class, 3f );
				break;

			//황금: 금빛 파편이 박히고, 금빛에 물든 적은 쓰러질 때 금화를 떨어뜨림
			case GOLD:
				burst( defender, Speck.factory( Speck.YELLOW_LIGHT ), 8 );
				CellEmitter.center( defender.pos ).burst( Speck.factory( Speck.COIN ), 4 );
				Sample.INSTANCE.play( Assets.Sounds.GOLD, 0.6f, 1.2f );
				fx( defender, s, "gold" );
				elemHit( s, defender, mul*0.5f );
				if (defender.isAlive() && defender instanceof Mob && defender.alignment == Char.Alignment.ENEMY){
					Buff.prolong( defender, Gilded.class, 3f );
				}
				break;

			//어둠: 낭떠러지의 어둠이 눈을 가리고, 가끔 적이 공포에 질려 달아남
			case DARK:
				burst( defender, ShadowParticle.UP, 12 );
				fx( defender, s, "dark" );
				elemHit( s, defender, mul*0.6f );
				if (defender.isAlive()){
					Buff.prolong( defender, Blindness.class, 2f );
					if (Random.Int( 3 ) == 0 && !defender.properties().contains( Char.Property.BOSS )
							&& !defender.properties().contains( Char.Property.MINIBOSS )){
						Buff.affect( defender, Terror.class, 3f ).object = attacker != null ? attacker.id() : 0;
						fx( defender, s, "dark_terror" );
					}
				}
				break;

			//룬 마법: 룬을 새기고, 룬이 셋 쌓이면 폭발해 대상과 둘레의 적에게 큰 피해
			case RUNE:
				burst( defender, MagicMissile.WardParticle.FACTORY, 8 );
				elemHit( s, defender, mul*0.5f );
				if (defender.isAlive()) runeMark( s, attacker, defender, mul );
				break;

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

	//장비 설명 줄: "물의 정령이 깃들어 있습니다." / "물의 정령과 불의 정령이 깃들어 있습니다."
	public static String dwells( Spirit a, Spirit b ){
		if (b == null) return Messages.get( Weapon.class, "spirit_dwells", a.title() );
		return Messages.get( Weapon.class, "spirit_dwells2", a.title(), b.title() );
	}

	//설명용: 지금 깊이의 속성 피해 기준
	public static String elemBaseDesc(){
		int d = Dungeon.scalingDepth();
		return Messages.get( ForestWand.class, "elem_base", 5 + d, 10 + d*2 );
	}

	// ── 정령 계약자: 정령계에서 상급 정령을 불러오고, 실체화시켜 함께 싸움 ──

	public static final float CONTRACT_COOLDOWN = 100f;
	public static final Spirit[] GREATER = { Spirit.UNDINE, Spirit.SALAMANDER, Spirit.GNOME, Spirit.SYLPH };

	//정령계의 문이 다시 열리기까지
	public static class ContractCooldown extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}
		@Override public int icon() { return BuffIndicator.TIME; }
		@Override public void tintIcon( Image icon ) { icon.hardlight( 0.65f, 0.45f, 1f ); }
		@Override public float iconFadePercent() { return Math.max( 0, visualcooldown() / CONTRACT_COOLDOWN ); }
	}

	//상급 정령은 각각 하나뿐: 완드·장비·실체화한 정령 중에 이미 있는지
	public static boolean greaterExists( Hero hero, Spirit s ){
		for (Item i : hero.belongings){
			if (i instanceof ForestWand){
				for (Spirit w : ((ForestWand) i).slots) if (w == s) return true;
			} else if (i instanceof Weapon && ((Weapon) i).enchantment instanceof SpiritEnchant){
				SpiritEnchant e = (SpiritEnchant) ((Weapon) i).enchantment;
				if (e.spirit == s || e.spirit2 == s) return true;
			} else if (i instanceof Armor && ((Armor) i).glyph instanceof SpiritGlyph){
				SpiritGlyph g = (SpiritGlyph) ((Armor) i).glyph;
				if (g.spirit == s || g.spirit2 == s) return true;
			}
		}
		for (Char ch : Actor.chars()){
			if (ch instanceof GreaterSpirit && ((GreaterSpirit) ch).spirit() == s) return true;
		}
		return false;
	}

	private void contract( final Hero hero ){
		ContractCooldown cd = hero.buff( ContractCooldown.class );
		if (cd != null){
			GLog.w( Messages.get( ForestWand.class, "contract_cooldown", (int)Math.ceil( cd.visualcooldown() ) ) );
			return;
		}
		if (freeSlot() < 0){
			GLog.w( Messages.get( ForestWand.class, "full" ) );
			return;
		}
		String[] opts = new String[GREATER.length];
		for (int i = 0; i < GREATER.length; i++) opts[i] = GREATER[i].title();
		GameScene.show( new WndOptions( new ItemSprite( this ),
				Messages.get( ForestWand.class, "contract_title" ),
				Messages.get( ForestWand.class, "contract_body" ), opts ){
			@Override
			protected boolean enabled( int index ) {
				return !greaterExists( hero, GREATER[index] );
			}
			@Override
			protected void onSelect( int index ) {
				summonGreater( hero, GREATER[index] );
			}
		} );
	}

	private void summonGreater( Hero hero, Spirit s ){
		int slot = freeSlot();
		if (slot < 0 || greaterExists( hero, s )) return;
		slots[slot] = s;
		updateQuickslot();
		//정령계의 문: 대기 100 → 75/50/25턴
		Buff.affect( hero, ContractCooldown.class, CONTRACT_COOLDOWN - 25f * hero.pointsInTalent( Talent.SPIRIT_GATE ) );
		if (hero.sprite != null){
			hero.sprite.operate( hero.pos );
			hero.sprite.centerEmitter().burst( s.particle(), 24 );
			hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 10 );
			hero.sprite.showStatus( s.color, s.title() );
		}
		imbueFx( hero.pos, s );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		Sample.INSTANCE.play( Assets.Sounds.TELEPORT, 0.6f );
		GLog.p( Messages.get( ForestWand.class, "contracted", s.title() ) );
		hero.spendAndNext( 1f );
	}

	//완드의 상급 정령을 실체화: 영웅 곁에 나타나 함께 싸움
	public boolean materialize( Hero hero, int slot ){
		Spirit s = slots[slot];
		if (s == null || !s.greater) return false;
		ArrayList<Integer> spots = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int p = hero.pos + n;
			if (Actor.findChar( p ) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) spots.add( p );
		}
		if (spots.isEmpty()){
			GLog.w( Messages.get( ForestWand.class, "no_space" ) );
			return false;
		}
		GreaterSpirit ally = GreaterSpirit.create( s );
		ally.pos = Random.element( spots );
		GameScene.add( ally );
		ScrollOfTeleportation.appear( ally, ally.pos );
		if (ally.sprite != null){
			ally.sprite.centerEmitter().burst( s.particle(), 16 );
			ally.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 6 );
		}
		slots[slot] = null;
		updateQuickslot();
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		GLog.p( Messages.get( ForestWand.class, "materialized", s.title() ) );
		if (hero.sprite != null) hero.sprite.zap( ally.pos );
		hero.spendAndNext( 1f );
		return true;
	}

	public static ArrayList<GreaterSpirit> materializedSpirits(){
		ArrayList<GreaterSpirit> list = new ArrayList<>();
		for (Char ch : Actor.chars()){
			if (ch instanceof GreaterSpirit && ch.isAlive()) list.add( (GreaterSpirit) ch );
		}
		return list;
	}

	//실체화한 정령을 모두 완드로 거둠 (빈 칸이 있는 만큼)
	public void recallMaterialized( Hero hero ){
		int n = 0;
		for (GreaterSpirit g : materializedSpirits()){
			int slot = freeSlot();
			if (slot < 0){
				GLog.w( Messages.get( ForestWand.class, "full" ) );
				break;
			}
			slots[slot] = g.spirit();
			if (g.sprite != null){
				CellEmitter.get( g.pos ).burst( g.spirit().particle(), 12 );
				CellEmitter.get( g.pos ).burst( Speck.factory( Speck.LIGHT ), 4 );
			}
			g.destroy();
			if (g.sprite != null) g.sprite.killAndErase();
			GLog.i( Messages.get( ForestWand.class, "unmaterialized", g.spirit().title() ) );
			n++;
		}
		preferredSlot = -1;
		if (n > 0){
			updateQuickslot();
			Sample.INSTANCE.play( Assets.Sounds.MELD );
			if (hero.sprite != null) hero.sprite.operate( hero.pos );
			hero.spendAndNext( 1f );
		}
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

	//블롭이 그 칸에 남아 있는지.
	//불러온 판에서는 다 사라진 블롭이 칸 배열(cur) 없이 남아 있을 수 있어 꼭 부피와 배열부터 확인함
	private static boolean blobAt( Blob b, int cell ){
		return b != null && b.volume > 0 && b.cur != null
				&& cell >= 0 && cell < b.cur.length && b.cur[cell] > 0;
	}

	//칸에 있는 촉매에서 얻을 수 있는 정령 (없으면 null)
	public static Spirit catalystAt( int cell ){
		Level level = Dungeon.level;

		for (Class<? extends SpiritRemnant> c : SpiritRemnant.TYPES){
			Blob b = level.blobs.get( c );
			if (blobAt( b, cell )){
				return ((SpiritRemnant) b).spirit();
			}
		}

		Blob fire = level.blobs.get( Fire.class );
		if (blobAt( fire, cell )) return Spirit.FIRE;

		//서리 물약·서리 함정의 서리 구름
		Blob ice = level.blobs.get( com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing.class );
		if (blobAt( ice, cell )) return Spirit.ICE;

		//바닥에 떨어진 금화
		if (goldAt( cell ) != null) return Spirit.GOLD;

		int t = level.map[cell];
		//고대의 성소: 룬이 새겨진 벽·모자이크 바닥·제단석·고대의 유물에서 룬 마법 정령
		if ((level instanceof SanctuaryLevel || level instanceof SanctuaryBossLevel)
				&& (t == Terrain.WALL_DECO || t == Terrain.EMPTY_SP || t == Terrain.PEDESTAL
					|| t == Terrain.REGION_DECO || t == Terrain.REGION_DECO_ALT)){
			return Spirit.RUNE;
		}
		//낭떠러지의 어둠
		if (t == Terrain.CHASM) return Spirit.DARK;
		if (t == Terrain.WALL_DECO && level instanceof PrisonLevel) return Spirit.FIRE;   //감옥 벽의 횃불
		//놀 제국의 모닥불·용광로
		if (t == Terrain.STATUE_SP && com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpire.isEmpireLevel( level )) return Spirit.FIRE;
		if (level.water[cell] || t == Terrain.WATER) return Spirit.WATER;
		if (t == Terrain.HIGH_GRASS || t == Terrain.FURROWED_GRASS) return Random.Int(2) == 0 ? Spirit.WIND : Spirit.NATURE;
		//맨땅: 땅의 정령 또는 바위의 정령
		if (t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMPTY_SP || t == Terrain.GRASS || t == Terrain.EMBERS){
			return Random.Int(2) == 0 ? Spirit.EARTH : Spirit.ROCK;
		}

		return null;
	}

	private static Gold goldAt( int cell ){
		Heap h = Dungeon.level.heaps.get( cell );
		if (h == null || h.type != Heap.Type.HEAP) return null;
		for (Item i : h.items) if (i instanceof Gold) return (Gold) i;
		return null;
	}

	//촉매를 쓰고 난 뒤 지형 변화 (긴 풀은 짓밟히고, 불·잔류 정령은 사라짐)
	private static void consumeCatalyst( int cell, Spirit s ){
		Level level = Dungeon.level;
		for (Class<? extends SpiritRemnant> c : SpiritRemnant.TYPES){
			Blob b = level.blobs.get( c );
			if (blobAt( b, cell )) { b.clear( cell ); return; }
		}
		if (s == Spirit.GOLD){
			//금화는 정령이 되어 사라짐
			Gold g = goldAt( cell );
			if (g != null) level.heaps.get( cell ).remove( g );
			CellEmitter.get( cell ).burst( Speck.factory( Speck.COIN ), 8 );
			return;
		}
		if (s == Spirit.ICE){
			Blob ice = level.blobs.get( com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing.class );
			if (blobAt( ice, cell )) ice.clear( cell );
		} else if (s == Spirit.FIRE){
			Blob fire = level.blobs.get( Fire.class );
			if (blobAt( fire, cell )) fire.clear( cell );
		} else if ((s == Spirit.WIND || s == Spirit.NATURE)
				&& (level.map[cell] == Terrain.HIGH_GRASS || level.map[cell] == Terrain.FURROWED_GRASS)){
			Level.set( cell, Terrain.GRASS );
			GameScene.updateMap( cell );
		}
	}

	public static void imbueFx( int from, Spirit s ){
		switch (s){
			case WATER: case UNDINE: CellEmitter.get( from ).burst( Speck.factory( Speck.STEAM ), 6 ); break;
			case SYLPH:  CellEmitter.get( from ).burst( Speck.factory( Speck.JET ), 8 ); break;
			case GNOME:  CellEmitter.get( from ).burst( EarthParticle.FACTORY, 14 ); break;
			case SALAMANDER: CellEmitter.get( from ).burst( FlameParticle.FACTORY, 14 ); break;
			case WIND:   CellEmitter.get( from ).burst( Speck.factory( Speck.JET ), 6 ); break;
			case NATURE: CellEmitter.get( from ).burst( LeafParticle.GENERAL, 10 ); break;
			case EARTH: case ROCK: CellEmitter.get( from ).burst( EarthParticle.FACTORY, 10 ); break;
			case FIRE: case FLAME: CellEmitter.get( from ).burst( FlameParticle.FACTORY, 10 ); break;
			case FROST: case ICE: CellEmitter.get( from ).burst( SnowParticle.FACTORY, 12 ); break;
			case SHOCK:  CellEmitter.center( from ).burst( SparkParticle.FACTORY, 10 ); break;
			case CHAOS:  CellEmitter.center( from ).burst( RainbowParticle.BURST, 12 ); break;
			case GOLD:   CellEmitter.center( from ).burst( Speck.factory( Speck.YELLOW_LIGHT ), 10 ); break;
			case DARK:   CellEmitter.get( from ).burst( ShadowParticle.UP, 14 ); break;
			case RUNE:   CellEmitter.center( from ).burst( MagicMissile.WardParticle.FACTORY, 14 ); break;
			default: break;
		}
	}

	private static int missileFor( Spirit s ){
		switch (s){
			case FIRE: case FLAME: return MagicMissile.FIRE;
			case FROST: case WATER: case ICE: return MagicMissile.FROST;
			case WIND: return MagicMissile.FORCE;
			case EARTH: case ROCK: return MagicMissile.EARTH;
			case SHOCK: return MagicMissile.SHAMAN_BLUE;
			case UNDINE: return MagicMissile.FROST;
			case SALAMANDER: return MagicMissile.FIRE;
			case GNOME: return MagicMissile.EARTH;
			case SYLPH: return MagicMissile.FORCE;
			case CHAOS: return MagicMissile.RAINBOW;
			case GOLD: return MagicMissile.LIGHT_MISSILE;
			case DARK: return MagicMissile.SHADOW;
			case RUNE: return MagicMissile.WARD;
			default: return MagicMissile.FOLIAGE;
		}
	}

	protected static CellSelector.Listener imbuer = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (!(curItem instanceof ForestWand)) return;
			final ForestWand wand = (ForestWand) curItem;
			//고른 빈칸은 이번 한 번만 씀
			final int pref = wand.preferredSlot;
			wand.preferredSlot = -1;
			if (target == null) return;
			final Hero hero = curUser;
			final int cell = target;

			if (!Dungeon.level.heroFOV[cell] || Dungeon.level.distance( hero.pos, cell ) > imbueRange( hero )){
				GLog.w( Messages.get( ForestWand.class, "too_far", imbueRange( hero ) ) );
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

			wand.preferredSlot = pref;
			final int slot = wand.freeSlot();
			wand.preferredSlot = -1;
			if (slot < 0){
				GLog.w( Messages.get( ForestWand.class, "full" ) );
				return;
			}

			final Spirit s = catalystAt( cell );
			if (s == null){
				GLog.w( Messages.get( ForestWand.class, "no_catalyst" ) );
				return;
			}

			consumeCatalyst( cell, s );

			//정령이 완드에 들어옴 (연출이 실패해도 정령은 반드시 들어오게)
			final Callback done = new Callback() {
				private boolean called = false;
				@Override
				public void call() {
					if (called) return;
					called = true;
					wand.slots[slot] = s;
					updateQuickslot();
					if (hero.sprite != null){
						hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), s.strong ? 8 : 5 );
						hero.sprite.showStatus( s.color, s.title() );
					}
					GLog.p( Messages.get( ForestWand.class, slot == CENTER ? "imbued_center" : "imbued_stored", s.title() ) );
					//촉매 감응 +2: 깃들이기에 반 턴만 듦
					hero.spendAndNext( hero.pointsInTalent( Talent.CATALYST_SENSE ) >= 2 ? 0.5f : 1f );
				}
			};

			hero.busy();
			try {
				hero.sprite.operate( cell );
				imbueFx( cell, s );
				Sample.INSTANCE.play( s.strong ? Assets.Sounds.CHARGEUP : Assets.Sounds.MELD );
				if (cell == hero.pos){
					//발밑의 기운은 날아올 거리가 없으므로 바로 깃듦
					done.call();
				} else {
					((MagicMissile) hero.sprite.parent.recycle( MagicMissile.class )).reset(
							missileFor( s ), cell, hero.pos, done );
				}
			} catch (Exception e){
				ShatteredPixelDungeon.reportException( e );
				done.call();
			}
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

		if (active() != null){
			info += "\n\n" + Messages.get( ForestWand.class, "shot_" + active().name().toLowerCase() );
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
		info += "\n\n" + Messages.get( ForestWand.class, "catalysts", imbueRange( Dungeon.hero ) );
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
