/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사 갑옷 능력 2: 정령 에너지 활성화
 *  10턴 동안 숲의 완드에 정령 에너지가 넘쳐흐름: 정령 탄을 두 배 빠르게 쏘고, 탄 피해 +25%, 정령 능력 +50%
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.elementalist;

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpiritRite;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

public class SpiritSurge extends ArmorAbility {

	{
		baseChargeUse = 40f;
	}

	public static final float BASE_DURATION = 10f;
	public static final float SPEED = 2f;        //쏘는 빠르기 배율
	public static final float DMG = 1.25f;       //탄 피해 배율
	public static final float SPIRIT = 1.5f;     //정령 능력 배율

	//넘치는 기운: 지속 +3/6/9/12턴
	public static float duration( Hero hero ){
		return BASE_DURATION + 3f * hero.pointsInTalent( Talent.SURGE_DURATION );
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();
		Invisibility.dispel();

		Buff.prolong( hero, Surge.class, duration( hero ) );
		hero.sprite.operate( hero.pos );
		hero.sprite.centerEmitter().burst( RainbowParticle.BURST, 20 );
		new Flare( 8, 26 ).color( 0x9CFFC8, true ).show( hero.sprite, 0.9f );
		ForestWand fw = hero.belongings.getItem( ForestWand.class );
		ForestWand.Spirit act = fw != null ? fw.active() : null;
		SpiritRite.surge( hero.sprite, act != null ? act.color : SpiritRite.MINT );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		GLog.p( Messages.get( this, "activated" ) );
		hero.spendAndNext( Actor.TICK );
	}

	public static boolean active( Char ch ){
		return ch != null && ch.buff( Surge.class ) != null;
	}

	//정령 쇄도: 넘쳐흐르는 동안 정령 탄으로 적을 쓰러뜨리면 지속 +1/2/3/4턴
	public static void onKill( Hero hero ){
		Surge s = hero.buff( Surge.class );
		int pts = hero.pointsInTalent( Talent.SURGE_FRENZY );
		if (s != null && pts > 0){
			Buff.prolong( hero, Surge.class, s.cooldown() + pts );
			if (hero.sprite != null) hero.sprite.centerEmitter().burst( RainbowParticle.BURST, 6 );
		}
	}

	public static class Surge extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		private Emitter aura;
		private SpiritRite.SurgeSigil sigil;

		@Override
		public int icon() {
			return BuffIndicator.RECHARGING;
		}

		@Override
		public void tintIcon( Image icon ) {
			icon.hardlight( 0x9CFFC8 );
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
				if (aura != null) aura.on = false;
				aura = target.sprite.emitter();
				ForestWand w = Dungeon.hero != null ? Dungeon.hero.belongings.getItem( ForestWand.class ) : null;
				ForestWand.Spirit s = w != null ? w.active() : null;
				aura.pour( s != null ? s.particle() : RainbowParticle.BURST, 0.12f );
				if (sigil != null) sigil.stop();
				sigil = new SpiritRite.SurgeSigil( target.sprite, s != null ? s.color : SpiritRite.MINT, 26 );
				GameScene.groundEffect( sigil );
			} else if (!on && aura != null){
				aura.on = false;
				aura = null;
				if (sigil != null){ sigil.stop(); sigil = null; }
			}
		}

		@Override
		public String desc() {
			Hero h = Dungeon.hero;
			int res = h != null ? 25 * h.pointsInTalent( Talent.SURGE_RESONANCE ) : 0;
			String d = Messages.get( this, "desc", dispTurns() );
			if (res > 0) d += "\n\n" + Messages.get( this, "desc_res", res );
			return d;
		}
	}

	@Override
	public int icon() {
		return HeroIcon.SPIRIT_SURGE;
	}

	@Override
	public String desc() {
		String d = Messages.get( this, "desc" );
		if (Dungeon.hero != null) d += "\n\n" + Messages.get( this, "dur", (int) duration( Dungeon.hero ) );
		return d + "\n\n" + Messages.get( this, "cost", (int) baseChargeUse );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.SURGE_DURATION, Talent.SURGE_RESONANCE, Talent.SURGE_FRENZY, Talent.HEROIC_ENERGY };
	}
}
