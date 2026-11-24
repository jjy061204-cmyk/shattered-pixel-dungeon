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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

//뱀파이어 갑옷 능력 2: 진조의 왕관 (성직자의 승천자의 형상처럼)
// 핏빛 왕관을 쓰고 15턴 동안 진조의 힘을 깨움
//  공격 피해 +30%, 준 피해의 20%만큼 회복, 받는 피해 -25%, 이동 속도 1.5배, 성배 기술 생명력 소모 절반,
//  맞힐 때마다 성배에 생명력 0.5
//  왕관을 쓰는 동안 핏빛 성배에 '왕관 기술'이 열림: 진조의 칙령(기본) + 특성으로 권속 소환·핏빛 낙창·피의 옥좌
public class CrimsonCrown extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	public static final float DURATION = 15f;

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		float dur = DURATION;
		Crown c = Buff.affect( hero, Crown.class, dur );
		c.total = dur;
		c.extended = false;
		GLog.p( Messages.get( this, "awaken" ) );

		hero.sprite.operate( hero.pos );
		//은빛 왕관이 위에서 내려와 머리에 얹히며 둘레에 은빛 별이 반짝임
		VampireFX.onBig( hero.sprite, VampireFX.B_SPARKLE, 0.45f, 1.0f ).offset( 0, -2f );
		GameScene.flash( 0x60CC1030 );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		Sample.INSTANCE.play( Assets.Sounds.LEVELUP, 0.6f, 0.8f );

		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();
		Invisibility.dispel();
		hero.spendAndNext( Actor.TICK );
	}

	public static boolean active( Char ch ){
		return ch != null && ch.buff( Crown.class ) != null;
	}

	//공격 피해 +30%, 준 피해의 20%만큼 회복
	public static int onAttack( Hero hero, Char enemy, int damage ){
		if (!active( hero ) || damage <= 0) return damage;
		damage = Math.round( damage * 1.3f );
		int heal = Math.min( Math.max( 1, Math.round( damage * 0.2f ) ), hero.HT - hero.HP );
		if (heal > 0){
			hero.HP += heal;
			hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
		}
		//맞힐 때마다 성배에 생명력 0.5 (왕관 기술의 밑천)
		CrimsonGrail.gainLife( hero, 0.5f );
		return damage;
	}

	//받는 피해 -25%
	public static int reduceDamage( Char ch, int dmg ){
		if (!active( ch ) || dmg <= 0) return dmg;
		return Math.max( 1, Math.round( dmg * 0.75f ) );
	}

	//피의 옥좌: 왕관을 한 번 쓸 때마다 한 번만 지속 시간을 늘림
	public static boolean extend( Char ch, float turns ){
		Crown c = ch.buff( Crown.class );
		if (c == null || c.extended) return false;
		c.extended = true;
		c.total += turns;
		Buff.prolong( ch, Crown.class, c.cooldown() + turns );
		return true;
	}

	public static float speedMultiplier( Char ch ){
		return active( ch ) ? 1.5f : 1f;
	}

	//성배 기술 생명력 소모 절반
	public static float costMultiplier( Char ch ){
		return active( ch ) ? 0.5f : 1f;
	}

	@Override
	public int icon() {
		return HeroIcon.CRIMSON_CROWN;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.CROWN_RETINUE, Talent.CROWN_CATACLYSM, Talent.CROWN_THRONE, Talent.HEROIC_ENERGY };
	}

	public static class Crown extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		public float total = DURATION;
		public boolean extended = false;

		private VampireFX.CrownImage image;

		@Override
		public int icon() {
			return BuffIndicator.UPGRADE;
		}

		@Override
		public void tintIcon( Image icon ) {
			icon.hardlight( 1f, 0.25f, 0.3f );
		}

		@Override
		public float iconFadePercent() {
			return Math.max( 0, (total - visualcooldown()) / total );
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc", dispTurns() );
		}

		@Override
		public void fx( boolean on ) {
			if (on){
				ensureFx( target.sprite );
			} else if (image != null){
				image.killAndErase();
				image = null;
			}
		}

		//왕관 그림이 지금 영웅 그림을 따라다니는지 확인하고, 없으면 얹음
		// (층을 옮기거나 불러오면 영웅 그림이 새로 만들어지므로 HeroSprite.update에서도 부름)
		public boolean isImage( VampireFX.CrownImage img ){
			return image == img;
		}

		//게임 진행과 화면 그리기가 동시에 부르면 왕관이 두 개 생길 수 있어 한 번에 하나만
		public static synchronized void ensureFx( CharSprite s ){
			if (s == null || s.ch == null || s.parent == null) return;
			Crown c = s.ch.buff( Crown.class );
			if (c == null) return;
			if (c.image != null && c.image.parent != null && c.image.follows( s )) return;
			boolean first = c.image == null;
			if (c.image != null) c.image.killAndErase();
			c.image = VampireFX.CrownImage.on( s, first && c.cooldown() > c.total - 1f );
		}

		private static final String TOTAL = "total";
		private static final String EXTENDED = "extended";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( TOTAL, total );
			bundle.put( EXTENDED, extended );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			if (bundle.contains( TOTAL )) total = bundle.getFloat( TOTAL );
			extended = bundle.getBoolean( EXTENDED );
		}
	}
}
