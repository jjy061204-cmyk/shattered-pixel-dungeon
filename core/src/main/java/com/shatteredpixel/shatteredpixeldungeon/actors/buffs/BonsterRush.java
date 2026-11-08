/*
 * Shattered Pixel Dungeon 모드: 본스터 각성 버프
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

//본스터를 다 마신 뒤 10턴 동안 공격 속도와 이동 속도 2배.
//끝나면 약화 5턴, 취약 5턴, 지연된 피해 5.
public class BonsterRush extends Buff {

	public static final float DURATION       = 10f;
	public static final float SPEED_FACTOR   = 2f;

	public static final float WEAKNESS_TURNS = 5f;
	public static final float VULNERABLE_TURNS = 5f;
	public static final int   CRASH_DAMAGE   = 5;

	{
		type = buffType.POSITIVE;
	}

	//먹는 동안 기다리는 턴 수 (먹고 나서부터 효과 시작)
	private float delay = 0;
	//효과 남은 턴 수
	private float left = DURATION;

	public void set( float delay ){
		this.delay = delay;
		this.left = DURATION;
	}

	//효과가 실제로 켜져 있는지 (먹는 중에는 꺼져 있음)
	public boolean active(){
		return delay <= 0 && left > 0;
	}

	//영웅의 이동/공격 계산에서 사용
	public static boolean isActive( Hero hero ){
		BonsterRush rush = hero.buff(BonsterRush.class);
		return rush != null && rush.active();
	}

	@Override
	public boolean act() {
		if (delay > 0){
			delay -= TICK;
			if (delay <= 0 && target instanceof Hero){
				GLog.p( Messages.get(this, "start") );
			}
		} else {
			left -= TICK;
		}

		if (delay <= 0 && left <= 0){
			crash();
			detach();
		} else {
			spend( TICK );
		}
		return true;
	}

	//효과가 끝난 뒤의 반동
	private void crash(){
		Buff.affect( target, Weakness.class, WEAKNESS_TURNS );
		Buff.affect( target, Vulnerable.class, VULNERABLE_TURNS );
		Buff.affect( target, Viscosity.DeferedDamage.class ).extend( CRASH_DAMAGE );
		if (target instanceof Hero){
			GLog.w( Messages.get(this, "crash") );
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.HASTE;
	}

	@Override
	public void tintIcon( Image icon ) {
		if (active()) {
			icon.hardlight(0.45f, 1f, 0f);
		} else {
			icon.hardlight(0.3f, 0.45f, 0.3f);
		}
	}

	@Override
	public float iconFadePercent() {
		if (!active()) return 0;
		return Math.max(0, (DURATION - left) / DURATION);
	}

	@Override
	public String iconTextDisplay() {
		return active() ? Integer.toString((int)left) : "";
	}

	@Override
	public String desc() {
		if (active()) {
			return Messages.get(this, "desc", dispTurns(left));
		} else {
			return Messages.get(this, "desc_wait");
		}
	}

	private static final String DELAY = "delay";
	private static final String LEFT  = "left";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DELAY, delay );
		bundle.put( LEFT, left );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		delay = bundle.getFloat( DELAY );
		left = bundle.getFloat( LEFT );
	}
}
