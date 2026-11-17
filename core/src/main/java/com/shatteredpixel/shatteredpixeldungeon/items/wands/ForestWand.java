/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사의 숲의 완드: 지형지물을 촉매로 정령을 깃들게 하고, 깃든 정령에 따라 다른 정령 마법을 씀
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TamedFireElemental;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ForestWand extends DamageWand {

	public static final String AC_IMBUE = "IMBUE";

	//촉매에서 정령을 불러올 수 있는 거리
	public static final int IMBUE_RANGE = 3;

	//깃든 정령: 강한 정령(드워프 층 원소 정령에게서 얻음)은 더 오래 머묾
	public enum Spirit {
		NONE   ( false, 0x9CE07A, MagicMissile.FOLIAGE ),
		WATER  ( false, 0x55B8FF, MagicMissile.FROST ),
		WIND   ( false, 0xD8FFF0, MagicMissile.FORCE ),
		NATURE ( false, 0x55DD33, MagicMissile.FOLIAGE ),
		FIRE   ( false, 0xFF8833, MagicMissile.FIRE ),
		FLAME  ( true,  0xFF3311, MagicMissile.FIRE ),
		FROST  ( true,  0x99E6FF, MagicMissile.FROST ),
		SHOCK  ( true,  0xFFF566, MagicMissile.SHAMAN_BLUE ),
		CHAOS  ( true,  0xD077FF, MagicMissile.RAINBOW );

		public final boolean strong;
		public final int color;
		public final int missile;

		Spirit( boolean strong, int color, int missile ){
			this.strong = strong;
			this.color = color;
			this.missile = missile;
		}

		public int uses(){
			return strong ? 8 : 6;
		}

		public String title(){
			return Messages.get(ForestWand.class, "spirit_" + name().toLowerCase());
		}
	}

	{
		image = ItemSpriteSheet.FOREST_WAND;

		unique = true;
		bones = false;

		//정령 마법은 대상에 닿으면 멈춤
		collisionProperties = Ballistica.MAGIC_BOLT;
	}

	public Spirit spirit = Spirit.NONE;
	public int spiritUses = 0;

	@Override
	public int initialCharges() {
		return 3;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_IMBUE );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_IMBUE )) {
			curUser = hero;
			curItem = this;
			GameScene.selectCell( imbuer );
		}
	}

	@Override
	public String name() {
		if (spirit == Spirit.NONE) return super.name();
		return Messages.get(this, "name_spirit", super.name(), spirit.title());
	}

	@Override
	public ItemSprite.Glowing glowing() {
		if (spirit == Spirit.NONE) return super.glowing();
		return new ItemSprite.Glowing( spirit.color, spirit.strong ? 1f : 2f );
	}

	//영웅 레벨에 따라 위력이 함께 커짐 (영혼의 활처럼)
	private int power( int lvl ){
		int heroBonus = Dungeon.hero != null ? Dungeon.hero.lvl / 5 : 0;
		return lvl + heroBonus;
	}

	@Override
	public int min( int lvl ) {
		int p = power(lvl);
		switch (spirit){
			default: case NONE: case WIND: return 1 + p;
			case WATER: case NATURE: case FIRE: return 2 + p;
			case FROST: case SHOCK: return 3 + p;
			case FLAME: case CHAOS: return 4 + p;
		}
	}

	@Override
	public int max( int lvl ) {
		int p = power(lvl);
		switch (spirit){
			default: case NONE: case WIND: return 4 + 2*p;
			case NATURE: return 5 + 2*p;
			case WATER: return 6 + 2*p;
			case FIRE: return 7 + 2*p;
			case SHOCK: return 8 + 2*p;
			case FROST: return 9 + 2*p;
			case FLAME: return 10 + 3*p;
			case CHAOS: return 11 + 3*p;
		}
	}

	// ── 정령 깃들이기 ──

	public void setSpirit( Spirit s ){
		spirit = s;
		spiritUses = s == Spirit.NONE ? 0 : s.uses();
		updateQuickslot();
	}

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
		} else if (s == Spirit.WIND || s == Spirit.NATURE){
			Level.set( cell, Terrain.GRASS );
			GameScene.updateMap( cell );
		}
	}

	private static void imbueFx( int from, Spirit s ){
		switch (s){
			case WATER:  CellEmitter.get( from ).burst( Speck.factory( Speck.STEAM ), 6 ); break;
			case WIND:   CellEmitter.get( from ).burst( Speck.factory( Speck.JET ), 6 ); break;
			case NATURE: CellEmitter.get( from ).burst( LeafParticle.GENERAL, 10 ); break;
			case FIRE: case FLAME: CellEmitter.get( from ).burst( FlameParticle.FACTORY, 10 ); break;
			case FROST:  CellEmitter.get( from ).burst( SnowParticle.FACTORY, 12 ); break;
			case SHOCK:  CellEmitter.center( from ).burst( SparkParticle.FACTORY, 10 ); break;
			case CHAOS:  CellEmitter.center( from ).burst( RainbowParticle.BURST, 12 ); break;
			default: break;
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
					s.missile, cell, hero.pos, new Callback() {
						@Override
						public void call() {
							wand.setSpirit( s );
							hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), s.strong ? 8 : 5 );
							hero.sprite.showStatus( s.color, s.title() );
							GLog.p( Messages.get( ForestWand.class, s.strong ? "imbued_strong" : "imbued", s.title() ) );
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

	// ── 정령 마법 ──

	private final ArrayList<Char> shockTargets = new ArrayList<>();

	@Override
	public void fx( Ballistica bolt, Callback callback ) {
		if (spirit == Spirit.SHOCK){
			shockTargets.clear();
			ArrayList<Lightning.Arc> arcs = new ArrayList<>();
			Char ch = Actor.findChar( bolt.collisionPos );
			if (ch != null){
				shockTargets.add( ch );
				arcs.add( new Lightning.Arc( curUser.sprite.center(), ch.sprite.center() ) );
				//둘레 2칸 안의 다른 적에게 번개가 옮겨 붙음 (최대 3명)
				PathFinder.buildDistanceMap( ch.pos, BArray.not( Dungeon.level.solid, null ), 2 );
				for (int i = 0; i < PathFinder.distance.length && shockTargets.size() < 4; i++){
					if (PathFinder.distance[i] == Integer.MAX_VALUE) continue;
					Char n = Actor.findChar( i );
					if (n != null && n != curUser && n.alignment != Char.Alignment.ALLY && !shockTargets.contains( n )){
						shockTargets.add( n );
						arcs.add( new Lightning.Arc( ch.sprite.center(), n.sprite.center() ) );
					}
				}
			} else {
				arcs.add( new Lightning.Arc( curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( bolt.collisionPos ) ) );
				CellEmitter.center( bolt.collisionPos ).burst( SparkParticle.FACTORY, 3 );
			}
			curUser.sprite.parent.addToFront( new Lightning( arcs, null ) );
			Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
			callback.call();
			return;
		}

		MagicMissile.boltFromChar( curUser.sprite.parent, spirit.missile, curUser.sprite, bolt.collisionPos, callback );
		switch (spirit){
			case FIRE: case FLAME: Sample.INSTANCE.play( Assets.Sounds.BURNING ); break;
			case WIND: case WATER: Sample.INSTANCE.play( Assets.Sounds.BLAST, 0.6f, 1.3f ); break;
			default: Sample.INSTANCE.play( Assets.Sounds.ZAP ); break;
		}
	}

	@Override
	public void onZap( Ballistica bolt ) {
		int cell = bolt.collisionPos;
		Char ch = Actor.findChar( cell );

		if (ch instanceof Elemental.NewbornFireElemental && ch.alignment == Char.Alignment.ENEMY){
			tame( curUser, (Elemental.NewbornFireElemental) ch );
			return;
		}

		Spirit used = spirit;
		switch (used){
			default: case NONE:
				if (ch != null) hit( ch );
				break;

			case WATER:
				//물줄기: 맞히고 밀쳐 내며, 둘레의 불을 끔
				for (int n : PathFinder.NEIGHBOURS9){
					Blob fire = Dungeon.level.blobs.get( Fire.class );
					if (fire != null) fire.clear( cell + n );
				}
				CellEmitter.get( cell ).burst( Speck.factory( Speck.STEAM ), 6 );
				if (ch != null){
					Buff.detach( ch, Burning.class );
					hit( ch );
					knock( ch, bolt, 2, false );
				}
				break;

			case WIND:
				//돌풍: 멀리 날려 벽에 부딪히게 함
				CellEmitter.get( cell ).burst( Speck.factory( Speck.JET ), 8 );
				if (ch != null){
					hit( ch );
					knock( ch, bolt, 3 + buffedLvl()/3, true );
				}
				break;

			case NATURE:
				//뿌리: 맞힌 대상을 묶고 둘레에 풀이 돋아남
				for (int n : PathFinder.NEIGHBOURS9){
					int c = cell + n;
					int t = Dungeon.level.map[c];
					if (t == Terrain.EMPTY || t == Terrain.EMBERS || t == Terrain.EMPTY_DECO){
						Level.set( c, Terrain.GRASS );
						GameScene.updateMap( c );
					}
				}
				CellEmitter.get( cell ).burst( LeafParticle.GENERAL, 10 );
				if (ch != null){
					hit( ch );
					if (ch.isAlive() && !ch.flying) Buff.prolong( ch, Roots.class, 3f + buffedLvl()/2 );
				}
				break;

			case FIRE:
				if (ch != null){
					hit( ch );
					if (ch.isAlive()) Buff.affect( ch, Burning.class ).reignite( ch, 4f );
				}
				break;

			case FLAME:
				//화염 폭발: 대상과 둘레를 태움 (영웅이 선 칸은 비켜 감)
				for (int n : PathFinder.NEIGHBOURS9){
					int c = cell + n;
					if (Dungeon.level.solid[c] || c == curUser.pos) continue;
					CellEmitter.get( c ).burst( FlameParticle.FACTORY, 6 );
					if (!Dungeon.level.water[c] && Dungeon.level.distance( c, curUser.pos ) > 1){
						GameScene.add( Blob.seed( c, 4, Fire.class ) );
					}
					Char other = Actor.findChar( c );
					if (other != null && other != ch && other != curUser && other.alignment != Char.Alignment.ALLY){
						other.damage( damageRoll()/2, this );
						if (other.isAlive()) Buff.affect( other, Burning.class ).reignite( other );
					}
				}
				if (ch != null){
					hit( ch );
					if (ch.isAlive()) Buff.affect( ch, Burning.class ).reignite( ch );
				}
				Sample.INSTANCE.play( Assets.Sounds.BLAST );
				break;

			case FROST:
				//서리: 대상을 얼리고 둘레를 차갑게 함
				for (int n : PathFinder.NEIGHBOURS8){
					Char other = Actor.findChar( cell + n );
					if (other != null && other != curUser && other.alignment != Char.Alignment.ALLY){
						Buff.prolong( other, Chill.class, 4f );
					}
					CellEmitter.get( cell + n ).burst( SnowParticle.FACTORY, 3 );
				}
				if (ch != null){
					hit( ch );
					if (ch.isAlive()) Buff.prolong( ch, Frost.class, 2f + buffedLvl()/2 );
				}
				break;

			case SHOCK:
				for (Char t : shockTargets){
					if (t == null || !t.isAlive()) continue;
					int dmg = t == ch ? damageRoll() : Math.round( damageRoll()*0.7f );
					wandProc( t, chargesPerCast() );
					t.damage( dmg, this );
					t.sprite.centerEmitter().burst( SparkParticle.FACTORY, 3 );
					t.sprite.flash();
				}
				shockTargets.clear();
				break;

			case CHAOS:
				//혼돈: 강한 피해와 함께 무작위 효과
				CellEmitter.center( cell ).burst( RainbowParticle.BURST, 14 );
				if (ch != null){
					hit( ch );
					if (ch.isAlive()){
						switch (Random.Int( 6 )){
							case 0: Buff.affect( ch, Burning.class ).reignite( ch ); break;
							case 1: Buff.prolong( ch, Frost.class, 3f ); break;
							case 2: Buff.prolong( ch, Paralysis.class, 2f ); break;
							case 3: Buff.prolong( ch, Vertigo.class, 5f ); break;
							case 4: Buff.prolong( ch, Weakness.class, 8f ); break;
							default: Buff.prolong( ch, Blindness.class, 4f ); break;
						}
					}
				}
				break;
		}

		if (ch == null) Dungeon.level.pressCell( cell );

		//정령의 힘이 다하면 떠남
		if (used != Spirit.NONE){
			spiritUses--;
			if (spiritUses <= 0){
				GLog.i( Messages.get( this, "spirit_left", used.title() ) );
				curUser.sprite.showStatus( CharSprite.NEUTRAL, Messages.get( this, "spirit_left_short" ) );
				setSpirit( Spirit.NONE );
			}
		}
		updateQuickslot();
	}

	private void hit( Char ch ){
		wandProc( ch, chargesPerCast() );
		ch.damage( damageRoll(), this );
		if (ch.sprite != null) ch.sprite.burst( 0xFF000000 | spirit.color, 3 + buffedLvl()/2 );
	}

	private void knock( Char ch, Ballistica bolt, int power, boolean collide ){
		if (!ch.isAlive() || ch.properties().contains( Char.Property.IMMOVABLE )) return;
		if (ch.pos == bolt.collisionPos && bolt.path.size() > bolt.dist + 1){
			Ballistica trajectory = new Ballistica( ch.pos, bolt.path.get( bolt.dist + 1 ), Ballistica.MAGIC_BOLT );
			WandOfBlastWave.throwChar( ch, trajectory, power, true, collide, this );
		}
	}

	@Override
	public void onHit( MagesStaff staff, Char attacker, Char defender, int damage ) {
		//지팡이에는 넣을 수 없음
	}

	@Override
	public String statsDesc() {
		String s = super.statsDesc();
		s += "\n\n" + Messages.get( this, "spirit_desc_" + spirit.name().toLowerCase() );
		if (spirit != Spirit.NONE) s += "\n\n" + Messages.get( this, "spirit_left_uses", spiritUses );
		return s;
	}

	@Override
	public String info() {
		return super.info() + "\n\n" + Messages.get( this, "catalysts", IMBUE_RANGE );
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public Item random() {
		return this;
	}

	private static final String SPIRIT = "spirit";
	private static final String SPIRIT_USES = "spirit_uses";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SPIRIT, spirit.name() );
		bundle.put( SPIRIT_USES, spiritUses );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( SPIRIT )){
			try {
				spirit = Spirit.valueOf( bundle.getString( SPIRIT ) );
			} catch (Exception e){
				spirit = Spirit.NONE;
			}
			spiritUses = bundle.getInt( SPIRIT_USES );
		}
	}
}
