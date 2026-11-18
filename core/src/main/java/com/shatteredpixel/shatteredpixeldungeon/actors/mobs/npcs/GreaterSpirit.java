/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령 계약자가 숲의 완드에 담은 상급 정령을 실체화시킨 모습: 영웅을 따라다니며 함께 싸움
 *  - 운디네: 물줄기로 공격하고, 다친 영웅을 이따금 치유
 *  - 샐러맨더: 불꽃으로 공격해 적을 불태움, 불에 면역
 *  - 노움: 단단하고 체력이 많으며, 공격한 적의 발을 묶음
 *  - 실피드: 빠르게 날아다니며 잘 피하고, 공격한 적을 밀쳐 냄
 * 쓰러지면 정령계로 돌아가 사라지고, 숲의 완드로 다시 거둘 수 있음. 위력은 영웅 레벨을 따름
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GreaterSpiritSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public abstract class GreaterSpirit extends DirectableAlly {

	{
		HP = HT = 10;
		flying = false;
		immunities.add( AllyBuff.class );
	}

	public abstract ForestWand.Spirit spirit();

	//체력·피해·명중·회피 배율 (정령마다 다름)
	protected float hpMul(){ return 1f; }
	protected float dmgMul(){ return 1f; }
	protected float evaMul(){ return 1f; }
	protected int armor(){ return lvl()/4; }

	protected static int lvl(){
		return Dungeon.hero != null ? Dungeon.hero.lvl : 1;
	}

	//영웅 레벨에 맞춰 체력을 다시 셈 (잃은 체력 비율은 유지)
	//상급 정령의 활력: 체력 +20/40/60%, 피해 +10/20/30%
	protected static int vitality(){
		return Dungeon.hero != null ? Dungeon.hero.pointsInTalent( Talent.GREATER_VITALITY ) : 0;
	}

	public void adjustStats(){
		int newHT = Math.round( (10 + 3*lvl()) * hpMul() * (1f + 0.2f*vitality()) );
		if (newHT != HT){
			float frac = HT > 0 ? HP / (float)HT : 1f;
			HT = newHT;
			HP = Math.max( 1, Math.min( HT, Math.round( HT * frac ) ) );
		}
	}

	public static GreaterSpirit create( ForestWand.Spirit s ){
		GreaterSpirit g;
		switch (s){
			case UNDINE:     g = new Undine(); break;
			case SALAMANDER: g = new Salamander(); break;
			case GNOME:      g = new Gnome(); break;
			case SYLPH: default: g = new Sylph(); break;
		}
		g.adjustStats();
		g.HP = g.HT;
		return g;
	}

	@Override
	protected boolean act() {
		adjustStats();
		return super.act();
	}

	@Override
	public int attackSkill( Char target ) {
		return 10 + Math.round( lvl() * 1.5f );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return Math.round( (4 + lvl()) * evaMul() );
	}

	@Override
	public int damageRoll() {
		int l = lvl();
		return Math.round( Random.NormalIntRange( 1 + l/3, 4 + l*2/3 ) * dmgMul() * (1f + 0.1f*vitality()) );
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, armor() );
	}

	@Override
	public void die( Object cause ) {
		flying = false;
		ForestWand.Spirit s = spirit();
		super.die( cause );
		//영혼 연결: 1/3, 2/3, 3/3 확률로 정령계 대신 숲의 완드로 돌아옴
		Hero h = Dungeon.hero;
		if (h != null && h.isAlive() && Random.Float() < h.pointsInTalent( Talent.SPIRIT_LINK ) / 3f){
			ForestWand wand = h.belongings.getItem( ForestWand.class );
			int slot = wand != null ? wand.freeSlot() : -1;
			if (slot >= 0){
				wand.slots[slot] = s;
				Item.updateQuickslot();
				if (h.sprite != null) h.sprite.centerEmitter().burst( s.particle(), 10 );
				GLog.p( Messages.get( GreaterSpirit.class, "linked_back", s.title() ) );
				return;
			}
		}
		GLog.w( Messages.get( GreaterSpirit.class, "returned_realm", s.title() ) );
	}

	@Override
	public String name() {
		return spirit().title();
	}

	@Override
	public String description() {
		return Messages.get( this, "desc" ) + "\n\n" + Messages.get( GreaterSpirit.class, "desc_common" );
	}

	// ── 운디네 ──
	public static class Undine extends GreaterSpirit {
		{
			spriteClass = GreaterSpiritSprite.Undine.class;
			immunities.add( Burning.class );
		}
		private int healCooldown = 0;

		@Override public ForestWand.Spirit spirit(){ return ForestWand.Spirit.UNDINE; }
		@Override protected float dmgMul(){ return 0.9f; }

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			ForestWand.elemHit( ForestWand.Spirit.WATER, enemy, 0.3f );
			return damage;
		}

		@Override
		protected boolean act() {
			//치유의 물방울: 5턴마다, 가까이 있는 다친 영웅을 치유
			if (healCooldown > 0) healCooldown--;
			Hero h = Dungeon.hero;
			if (healCooldown <= 0 && h != null && h.isAlive() && h.HP < h.HT
					&& Dungeon.level.distance( pos, h.pos ) <= 4){
				int heal = Math.min( 1 + lvl()/4, h.HT - h.HP );
				h.HP += heal;
				if (h.sprite != null){
					h.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 2 );
					h.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
				}
				healCooldown = 5;
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

	// ── 샐러맨더 ──
	public static class Salamander extends GreaterSpirit {
		{
			spriteClass = GreaterSpiritSprite.Salamander.class;
			immunities.add( Burning.class );
			immunities.add( Fire.class );
		}
		@Override public ForestWand.Spirit spirit(){ return ForestWand.Spirit.SALAMANDER; }
		@Override protected float hpMul(){ return 0.9f; }
		@Override protected float dmgMul(){ return 1.15f; }

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			ForestWand.elemHit( ForestWand.Spirit.FIRE, enemy, 0.3f );
			if (enemy.isAlive()) Buff.affect( enemy, Burning.class ).reignite( enemy, 3f );
			return damage;
		}
	}

	// ── 노움 ──
	public static class Gnome extends GreaterSpirit {
		{
			spriteClass = GreaterSpiritSprite.Gnome.class;
		}
		@Override public ForestWand.Spirit spirit(){ return ForestWand.Spirit.GNOME; }
		@Override protected float hpMul(){ return 1.6f; }
		@Override protected float dmgMul(){ return 0.8f; }
		@Override protected int armor(){ return 2 + lvl()/2; }

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			ForestWand.elemHit( ForestWand.Spirit.EARTH, enemy, 0.25f );
			if (enemy.isAlive()) Buff.prolong( enemy, Cripple.class, 2f );
			return damage;
		}
	}

	// ── 실피드 ──
	public static class Sylph extends GreaterSpirit {
		{
			spriteClass = GreaterSpiritSprite.Sylph.class;
			flying = true;
			baseSpeed = 2f;
		}
		@Override public ForestWand.Spirit spirit(){ return ForestWand.Spirit.SYLPH; }
		@Override protected float hpMul(){ return 0.7f; }
		@Override protected float dmgMul(){ return 0.85f; }
		@Override protected float evaMul(){ return 1.6f; }

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			ForestWand.elemHit( ForestWand.Spirit.WIND, enemy, 0.25f );
			ForestWand.knockAway( this, enemy, 1, true );
			return damage;
		}
	}
}
