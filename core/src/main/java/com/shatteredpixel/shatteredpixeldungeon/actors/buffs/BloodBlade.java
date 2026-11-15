/*
 * Shattered Pixel Dungeon 모드: 피의 지배자의 '핏빛 검'
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

//걸려 있는 동안 근접 공격에 추가 피해가 붙고, 맞힐 때마다 성배에 생명력이 고임 (CrimsonGrail.onHeroAttack)
public class BloodBlade extends FlavourBuff {

	public static final float TURNS = 12f;

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.WEAPON;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 1f, 0.2f, 0.3f );
	}

	@Override
	public float iconFadePercent() {
		return Math.max( 0, (TURNS - visualcooldown()) / TURNS );
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc", dispTurns() );
	}
}
