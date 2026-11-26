/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 마법 기사 갑옷 능력 2: 에테르 심화
 *  에테르와의 연결을 깊게 해, 15턴 동안 근접 무기로 적을 맞히면
 *  그 공격 피해의 40%만큼 전용 속성 피해인 '에테르 피해'를 더 입힘
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
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PathFinder;

public class EtherDeepen extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	public static final float DURATION = 15f;
	public static final float BASE_RATIO = 0.40f;
	public static final int COLOR = 0x9CF4FF;

	//에테르 증폭: 에테르 피해 비율 40% → +15%씩 (최대 100%)
	public static float ratio( Hero hero ){
		return BASE_RATIO + 0.15f * hero.pointsInTalent( Talent.ETHER_INTENSITY );
	}

	public static boolean active( Char ch ){
		return ch != null && ch.buff( Deepened.class ) != null;
	}

	//근접 무기로 적을 맞힐 때 (Hero.attackProc)
	public static void onAttack( Hero hero, Char enemy, int damage, KindOfWeapon wep ){
		if (!active( hero ) || enemy == null || !enemy.isAlive()) return;
		if (!(wep instanceof MeleeWeapon) || damage <= 0) return;
		float amt = damage * ratio( hero );
		int dealt = ElementalDamage.etherHit( enemy, amt );
		if (enemy.sprite != null && enemy.sprite.visible){
			enemy.sprite.emitter().burst( StarlightParticle.BURST, 4 );
		}
		//에테르 파문: 맞힌 적 둘레 8칸의 다른 적에게 20/40/60/80%가 번짐
		int pts = hero.pointsInTalent( Talent.ETHER_RIPPLE );
		if (pts > 0 && dealt > 0){
			for (int o : PathFinder.NEIGHBOURS8){
				Char c = Actor.findChar( enemy.pos + o );
				if (c != null && c != hero && c.alignment == Char.Alignment.ENEMY && c.isAlive()){
					ElementalDamage.etherHit( c, dealt * 0.2f * pts );
					if (c.sprite != null && c.sprite.visible) c.sprite.emitter().burst( StarlightParticle.BURST, 2 );
				}
			}
		}
	}

	//끊이지 않는 연결: 심화 중 근접 공격으로 적을 쓰러뜨리면 지속 +1/2/3/4턴
	public static void onKill( Hero hero ){
		Deepened d = hero.buff( Deepened.class );
		int pts = hero.pointsInTalent( Talent.ETHER_LINGER );
		if (d != null && pts > 0){
			Buff.prolong( hero, Deepened.class, d.cooldown() + pts );
			if (hero.sprite != null) hero.sprite.centerEmitter().burst( StarlightParticle.BURST, 6 );
		}
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();
		Invisibility.dispel();

		Buff.prolong( hero, Deepened.class, DURATION );
		hero.sprite.operate( hero.pos );
		hero.sprite.centerEmitter().burst( StarlightParticle.BURST, 24 );
		new Flare( 8, 28 ).color( COLOR, true ).show( hero.sprite, 1f );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		GLog.p( Messages.get( this, "activated" ) );
		hero.spendAndNext( Actor.TICK );
	}

	public static class Deepened extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		private Emitter aura;

		@Override
		public int icon() {
			return BuffIndicator.WEAPON;
		}

		@Override
		public void tintIcon( Image icon ) {
			icon.hardlight( COLOR );
		}

		@Override
		public float iconFadePercent() {
			return Math.max( 0, (DURATION - visualcooldown()) / DURATION );
		}

		@Override
		public void fx( boolean on ) {
			if (on && target.sprite != null){
				if (aura != null) aura.on = false;
				aura = target.sprite.emitter();
				aura.pour( StarlightParticle.BURST, 0.15f );
			} else if (!on && aura != null){
				aura.on = false;
				aura = null;
			}
		}

		@Override
		public String desc() {
			Hero h = target instanceof Hero ? (Hero) target : Dungeon.hero;
			int pct = Math.round( (h != null ? ratio( h ) : BASE_RATIO) * 100 );
			return Messages.get( this, "desc", pct, dispTurns() );
		}
	}

	@Override
	public int icon() {
		return HeroIcon.ETHER_DEEPEN;
	}

	@Override
	public String desc() {
		String d = Messages.get( this, "desc" );
		Hero h = Dungeon.hero;
		if (h != null) d += "\n\n" + Messages.get( this, "now", Math.round( ratio( h ) * 100 ) );
		return d + "\n\n" + Messages.get( this, "cost", (int) baseChargeUse );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.ETHER_INTENSITY, Talent.ETHER_RIPPLE, Talent.ETHER_LINGER, Talent.HEROIC_ENERGY };
	}
}
