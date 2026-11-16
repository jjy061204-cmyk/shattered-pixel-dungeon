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

//뱀파이어 갑옷 능력 2: 진조의 왕관
// 핏빛 왕관을 쓰고 짧은 시간 동안 여러 강력한 힘을 한꺼번에 얻음
//  공격 피해 +30%, 준 피해의 20%만큼 회복, 받는 피해 -25%, 이동 속도 1.5배, 성배 기술 생명력 소모 절반
public class CrimsonCrown extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	public static final float DURATION = 10f;

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		float dur = DURATION + 2f * hero.pointsInTalent( Talent.ENDLESS_REIGN );
		Crown c = Buff.affect( hero, Crown.class, dur );
		c.total = dur;

		//왕의 위엄: 4칸 안의 보이는 적이 2/4/6/8턴 동안 약화
		int pts = hero.pointsInTalent( Talent.ROYAL_PRESENCE );
		if (pts > 0){
			for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )){
				if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]
						&& Dungeon.level.distance( hero.pos, mob.pos ) <= 4){
					Buff.prolong( mob, Weakness.class, 2f * pts );
					VampireFX.on( mob.sprite, VampireFX.DREAD, 0.36f, 0.5f ).offset( 0, -11f ).delay( 0.2f );
				}
			}
		}

		hero.sprite.operate( hero.pos );
		//발밑에 문양이 돌고, 두 줄기 핏빛 나선이 머리 위로 솟아 왕관으로 맺힘
		VampireFX.onBig( hero.sprite, VampireFX.B_SIGIL, 0.55f, 1.0f ).offset( 0, -6f );
		VampireFX.onBig( hero.sprite, VampireFX.B_CROWN, 0.5f, 0.85f ).offset( 0, -5f );
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

	//공격 피해 +30%, 준 피해의 20%만큼 회복. 피의 공물: 맞힐 때마다 성배에 생명력 0.25/0.5/0.75/1
	public static int onAttack( Hero hero, Char enemy, int damage ){
		if (!active( hero ) || damage <= 0) return damage;
		damage = Math.round( damage * 1.3f );
		int heal = Math.min( Math.max( 1, Math.round( damage * 0.2f ) ), hero.HT - hero.HP );
		if (heal > 0){
			hero.HP += heal;
			hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
		}
		int pts = hero.pointsInTalent( Talent.BLOOD_TRIBUTE );
		if (pts > 0) CrimsonGrail.gainLife( hero, 0.25f * pts );
		return damage;
	}

	//받는 피해 -25%
	public static int reduceDamage( Char ch, int dmg ){
		if (!active( ch ) || dmg <= 0) return dmg;
		return Math.max( 1, Math.round( dmg * 0.75f ) );
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
		return new Talent[]{ Talent.ROYAL_PRESENCE, Talent.ENDLESS_REIGN, Talent.BLOOD_TRIBUTE, Talent.HEROIC_ENERGY };
	}

	public static class Crown extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		public float total = DURATION;

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
		public static void ensureFx( CharSprite s ){
			if (s == null || s.ch == null || s.parent == null) return;
			Crown c = s.ch.buff( Crown.class );
			if (c == null) return;
			if (c.image != null && c.image.parent != null && c.image.follows( s )) return;
			if (c.image != null) c.image.killAndErase();
			c.image = VampireFX.CrownImage.on( s );
		}

		private static final String TOTAL = "total";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( TOTAL, total );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			if (bundle.contains( TOTAL )) total = bundle.getFloat( TOTAL );
		}
	}
}
