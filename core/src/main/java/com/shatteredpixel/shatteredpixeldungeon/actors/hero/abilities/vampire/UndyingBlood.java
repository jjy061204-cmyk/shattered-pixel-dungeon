/*
 * Shattered Pixel Dungeon 모드: 영웅 고유 공격 이펙트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.VampireFX;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CrimsonGrail;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.watabou.utils.PathFinder;

//뱀파이어 갑옷 능력 3: 불멸의 피
// 생명력을 끓어오르게 해 곧바로 체력을 회복하고, 20턴 동안 매 턴 체력이 크게 차오르며 성배에도 생명력이 고임.
// 그동안 쓰러지면 한 번 되살아남
public class UndyingBlood extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	public static final float DURATION = 20f;

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		float dur = DURATION + 5f * hero.pointsInTalent( Talent.ENDLESS_NIGHT );
		Tracker t = Buff.affect( hero, Tracker.class );
		t.left = dur;
		t.total = dur;

		//곧바로 최대 체력의 25% 회복 (신선한 피: +10/20/30/40%)
		float pct = 0.25f + 0.1f * hero.pointsInTalent( Talent.FRESH_BLOOD );
		heal( hero, Math.round( hero.HT * pct ) );

		hero.sprite.operate( hero.pos );
		//커다란 심장이 두 번 고동치며 핏줄이 뻗어 나감
		VampireFX.onBig( hero.sprite, VampireFX.B_HEART, 0.42f, 0.9f ).offset( 0, -10f );
		VampireFX.onBig( hero.sprite, VampireFX.B_RISE, 0.45f, 0.9f ).offset( 0, -6f );
		Sample.INSTANCE.play( Assets.Sounds.DRINK );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 0.8f, 0.6f );

		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();
		Invisibility.dispel();
		hero.spendAndNext( Actor.TICK );
	}

	private static void heal( Hero hero, int amount ){
		amount = Math.min( amount, hero.HT - hero.HP );
		if (amount <= 0) return;
		hero.HP += amount;
		hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( amount ), FloatingText.HEALING );
	}

	//Hero.die에서 가장 먼저 부름. 능력이 이어지는 동안 쓰러지면 되살아나고 true
	public static boolean tryRevive( Hero hero ){
		Tracker t = hero.buff( Tracker.class );
		if (t == null) return false;
		t.detach();

		hero.interrupt();
		int pts = hero.pointsInTalent( Talent.BLOOD_RETURN );
		//최대 체력의 50%로 되살아남 (피의 귀환: +10/20/30/40%)
		hero.HP = Math.max( 1, Math.round( hero.HT * (0.5f + 0.1f * pts) ) );
		PotionOfHealing.cure( hero );
		Buff.prolong( hero, Invulnerability.class, Invulnerability.DURATION );

		//피의 귀환: 주변 적에게 피해, 성배에 생명력
		if (pts > 0){
			for (int n : PathFinder.NEIGHBOURS8){
				Char ch = Actor.findChar( hero.pos + n );
				if (ch != null && ch.alignment != Char.Alignment.ALLY && ch.isAlive()){
					ch.damage( Math.round( hero.HT * 0.1f * pts ), hero );
					if (ch.isAlive()) Buff.prolong( ch, Cripple.class, 3f );
				}
			}
			CrimsonGrail.gainLife( hero, 5f * pts );
		}

		if (hero.sprite != null){
			hero.sprite.idle();
			//빛기둥이 솟고 거대한 날개가 펼쳐지며 발밑에 고리가 퍼짐
			VampireFX.onBig( hero.sprite, VampireFX.B_REVIVE, 0.6f, 1.0f ).offset( 0, -6f );
			VampireFX.onBig( hero.sprite, VampireFX.B_HEART, 0.36f, 0.7f ).offset( 0, -14f ).delay( 0.3f );
			hero.sprite.showStatus( CharSprite.POSITIVE, Messages.get( UndyingBlood.class, "revived_status" ) );
		}
		GameScene.flash( 0x80CC1030 );
		PixelScene.shake( 2f, 0.4f );
		Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
		Sample.INSTANCE.play( Assets.Sounds.BLAST, 0.8f, 0.7f );
		GLog.p( Messages.get( UndyingBlood.class, "revive" ) );
		return true;
	}

	@Override
	public int icon() {
		return HeroIcon.UNDYING_BLOOD;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.FRESH_BLOOD, Talent.BLOOD_RETURN, Talent.ENDLESS_NIGHT, Talent.HEROIC_ENERGY };
	}

	//매 턴: 최대 체력의 4%씩 차오름(소수점은 모아 둠), 성배에 생명력 0.5
	public static class Tracker extends Buff {

		{
			type = buffType.POSITIVE;
			revivePersists = true;
		}

		public float left = DURATION;
		public float total = DURATION;
		private float partial = 0;

		@Override
		public boolean act() {
			if (target instanceof Hero && target.isAlive()){
				Hero hero = (Hero) target;
				if (Regeneration.regenOn() && hero.HP < hero.HT){
					partial += hero.HT * 0.04f;
					int whole = (int) partial;
					if (whole > 0){
						partial -= whole;
						heal( hero, whole );
					}
				}
				CrimsonGrail.gainLife( hero, 0.5f );
			}
			left -= TICK;
			if (left <= 0){
				detach();
			} else {
				spend( TICK );
			}
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.ANKH;
		}

		@Override
		public void tintIcon( Image icon ) {
			icon.hardlight( 1f, 0.15f, 0.25f );
		}

		@Override
		public float iconFadePercent() {
			return Math.max( 0, (total - left) / total );
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString( (int) left );
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc", (int) left );
		}

		private VampireFX.Pulse pulse;

		@Override
		public void fx( boolean on ) {
			if (on){
				ensureFx( target.sprite );
			} else if (pulse != null){
				pulse.killAndErase();
				pulse = null;
			}
		}

		//발밑 고동이 지금 영웅 그림을 따라다니는지 확인하고, 없으면 붙임 (HeroSprite.update에서도 부름)
		public static void ensureFx( CharSprite s ){
			if (s == null || s.ch == null || s.parent == null) return;
			Tracker t = s.ch.buff( Tracker.class );
			if (t == null) return;
			if (t.pulse != null && t.pulse.parent != null && t.pulse.follows( s )) return;
			if (t.pulse != null) t.pulse.killAndErase();
			t.pulse = VampireFX.Pulse.on( s );
		}

		private static final String LEFT = "left", TOTAL = "total", PARTIAL = "partial";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( LEFT, left );
			bundle.put( TOTAL, total );
			bundle.put( PARTIAL, partial );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			left = bundle.getFloat( LEFT );
			if (bundle.contains( TOTAL )) total = bundle.getFloat( TOTAL );
			partial = bundle.getFloat( PARTIAL );
		}
	}
}
