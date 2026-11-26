/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 마법 기사 갑옷 능력 1: 에테르 갑주
 *  일시적으로 에테르 점토를 갑옷에 둘러 방어력을 높임
 *  - 물리 방어력 증가 = (에테르 점토 강화 수치 ÷ 3, 버림) × 지금 입은 갑옷이 1강마다 얻는 방어력
 *  - 마법 방어력은 처음엔 없고, '에테르 차폐' 특성을 찍으면 같은 값으로 생김
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.magicknight;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.elemental.ElementalDamage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherClay;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

public class EtherArmor extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	public static final float BASE_DURATION = 20f;
	public static final int COLOR = 0xC8A0FF;

	//겹겹의 점토: 지속 +5/10/15/20턴
	public static float duration( Hero hero ){
		return BASE_DURATION + 5f * hero.pointsInTalent( Talent.CLAY_LAYERS );
	}

	//방어력 계산에 쓰는 '강화 단계' = 에테르 점토 강화 수치 ÷ 3 (버림)
	public static int steps( Hero hero ){
		if (hero == null) return 0;
		EtherClay clay = hero.belongings.getItem( EtherClay.class );
		if (clay == null) return 0;
		return Math.max( 0, clay.buffedLvl() ) / 3;
	}

	//지금 입은 갑옷이 1강마다 얻는 방어력 (최소, 최대)
	private static int perMin( Armor a ){
		int l = a.buffedLvl();
		return Math.max( 0, a.DRMin( l + 1 ) - a.DRMin( l ) );
	}
	private static int perMax( Armor a ){
		int l = a.buffedLvl();
		return Math.max( 0, a.DRMax( l + 1 ) - a.DRMax( l ) );
	}

	public static int bonusMin( Hero hero ){
		Armor a = hero.belongings.armor();
		return a == null ? 0 : steps( hero ) * perMin( a );
	}
	public static int bonusMax( Hero hero ){
		Armor a = hero.belongings.armor();
		return a == null ? 0 : steps( hero ) * perMax( a );
	}

	public static boolean active( Char ch ){
		return ch != null && ch.buff( Plating.class ) != null;
	}

	//물리 방어력 증가 (Hero.drRoll)
	public static int drBonus( Hero hero ){
		if (!active( hero )) return 0;
		int mx = bonusMax( hero );
		if (mx <= 0) return 0;
		return Random.NormalIntRange( Math.min( bonusMin( hero ), mx ), mx );
	}

	//에테르 차폐: 마법 방어력 = 물리 증가분 × 100/133/167/200%
	public static float wardFactor( Hero hero ){
		int p = hero.pointsInTalent( Talent.ETHER_WARD );
		return p <= 0 ? 0f : 1f + (p - 1) / 3f;
	}

	//마법 피해 감소 (Char.damage의 마법 피해 처리)
	public static int magicBlock( Hero hero ){
		if (!active( hero )) return 0;
		float f = wardFactor( hero );
		if (f <= 0) return 0;
		int mx = Math.round( bonusMax( hero ) * f );
		if (mx <= 0) return 0;
		int mn = Math.min( Math.round( bonusMin( hero ) * f ), mx );
		return Random.NormalIntRange( mn, mx );
	}

	//에테르 가시: 근접 공격을 받으면 공격자에게 물리 증가분 × 25/50/75/100%의 에테르 피해
	public static void onHeroDefended( Hero hero, Char enemy ){
		int pts = hero.pointsInTalent( Talent.ETHER_THORNS );
		if (pts <= 0 || !active( hero ) || enemy == null || !enemy.isAlive()) return;
		if (!Dungeon.level.adjacent( hero.pos, enemy.pos )) return;
		int mx = bonusMax( hero );
		int roll = mx > 0 ? Random.NormalIntRange( Math.min( bonusMin( hero ), mx ), mx ) : 0;
		float dmg = Math.max( pts, roll * 0.25f * pts );
		ElementalDamage.etherHit( enemy, dmg );
		if (enemy.sprite != null) enemy.sprite.emitter().burst( StarlightParticle.BURST, 2 + pts );
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		//점토가 +3보다 약하면 갑옷을 감쌀 만큼 굳지 못함 (충전은 쓰지 않음)
		if (steps( hero ) <= 0){
			GLog.w( Messages.get( this, "weak" ) );
			return;
		}
		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();
		Invisibility.dispel();

		Buff.prolong( hero, Plating.class, duration( hero ) );
		hero.sprite.operate( hero.pos );
		hero.sprite.centerEmitter().burst( StarlightParticle.BURST, 18 );
		new Flare( 6, 24 ).color( COLOR, true ).show( hero.sprite, 0.8f );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		GLog.p( Messages.get( this, "activated" ) );
		hero.spendAndNext( Actor.TICK );
	}

	public static class Plating extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		private Emitter aura;

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon( Image icon ) {
			icon.hardlight( COLOR );
		}

		@Override
		public float iconFadePercent() {
			Hero h = target instanceof Hero ? (Hero) target : Dungeon.hero;
			float d = h != null ? duration( h ) : BASE_DURATION;
			return Math.max( 0, (d - visualcooldown()) / d );
		}

		@Override
		public void fx( boolean on ) {
			if (on && target.sprite != null){
				target.sprite.add( CharSprite.State.SHIELDED );
				if (aura != null) aura.on = false;
				aura = target.sprite.emitter();
				aura.pour( StarlightParticle.ATTRACT, 0.25f );
			} else if (!on){
				if (target.sprite != null) target.sprite.remove( CharSprite.State.SHIELDED );
				if (aura != null){ aura.on = false; aura = null; }
			}
		}

		@Override
		public String desc() {
			Hero h = target instanceof Hero ? (Hero) target : Dungeon.hero;
			String d = Messages.get( this, "desc", dispTurns() );
			if (h != null){
				d += "\n\n" + Messages.get( this, "desc_dr", bonusMin( h ), bonusMax( h ) );
				float f = wardFactor( h );
				if (f > 0) d += "\n" + Messages.get( this, "desc_mr",
						Math.round( bonusMin( h ) * f ), Math.round( bonusMax( h ) * f ) );
			}
			return d;
		}
	}

	@Override
	public int icon() {
		return HeroIcon.ETHER_ARMOR;
	}

	@Override
	public String desc() {
		String d = Messages.get( this, "desc" );
		Hero h = Dungeon.hero;
		if (h != null){
			d += "\n\n" + Messages.get( this, "now", bonusMin( h ), bonusMax( h ) );
			d += "\n" + Messages.get( this, "dur", (int) duration( h ) );
		}
		return d + "\n\n" + Messages.get( this, "cost", (int) baseChargeUse );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.CLAY_LAYERS, Talent.ETHER_WARD, Talent.ETHER_THORNS, Talent.HEROIC_ENERGY };
	}
}
