/*
 * Shattered Pixel Dungeon 모드: 룬 성갑 (전신 갑옷 형태)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

//실제 능력치 계산은 Hero 쪽에서 이 버프가 있는지로 판단
public class RuneAegisForm extends FlavourBuff {

	public static final float DURATION = 40f;

	public static final float ACCURACY = 1.3f;       //정확성 +30%
	public static final float DAMAGE_TAKEN = 0.6f;   //받는 모든 피해 40% 감소
	public static final int STR_BONUS = 4;           //힘 +4
	public static final int DR_BONUS = 10;           //방어력 +5~10

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.ARMOR;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 0.25f, 0.9f, 0.85f );
	}

	@Override
	public float iconFadePercent() {
		return Math.max( 0, (DURATION - visualcooldown()) / DURATION );
	}

	@Override
	public void detach() {
		Char ch = target;
		super.detach();
		if (ch instanceof Hero && ch.sprite instanceof HeroSprite && ch.isAlive()){
			((HeroSprite) ch.sprite).updateArmor();
			ch.sprite.place( ch.pos );
			ch.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 10 );
			ch.sprite.emitter().burst( RuneMote.FACTORY, 16 );
			Sample.INSTANCE.play( Assets.Sounds.SHATTER, 0.6f, 1.3f );
			GLog.w( Messages.get( this, "ended" ) );
		}
	}
}
