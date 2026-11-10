/*
 * Shattered Pixel Dungeon 모드: 엑스칼리버의 노란 불꽃 (성화)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.YellowFlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

//적의 몸만 태우는 노란 불꽃. 일반 불처럼 바닥·풀·아이템으로 번지지 않음
public class HolyFlame extends Buff {

	public static final float DURATION = 3f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	private float left;
	private int min, max;

	private transient Emitter emitter;

	public void set( float duration, int min, int max ){
		left = Math.max( left, duration );
		this.min = Math.max( this.min, min );
		this.max = Math.max( this.max, max );
	}

	@Override
	public boolean act() {
		if (target.isAlive()){
			int dmg = Random.NormalIntRange( min, max );
			target.damage( Math.round( dmg * target.resist( Burning.class ) ), this );
		}
		left -= TICK;
		if (left <= 0 || !target.isAlive()){
			detach();
		}
		spend( TICK );
		return true;
	}

	@Override
	public int icon() {
		return BuffIndicator.FIRE;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 1f, 0.9f, 0.3f );
	}

	@Override
	public float iconFadePercent() {
		return Math.max( 0, (DURATION - left) / DURATION );
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString( (int) Math.ceil( left ) );
	}

	@Override
	public void fx( boolean on ) {
		if (on){
			if (target.sprite != null){
				emitter = target.sprite.emitter();
				emitter.pour( YellowFlameParticle.FACTORY, 0.06f );
			}
		} else if (emitter != null){
			emitter.on = false;
			emitter = null;
		}
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc", (int) Math.ceil( left ), min, max );
	}

	private static final String LEFT = "left";
	private static final String MIN  = "min";
	private static final String MAX  = "max";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );
		bundle.put( MIN, min );
		bundle.put( MAX, max );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		left = bundle.getFloat( LEFT );
		min = bundle.getInt( MIN );
		max = bundle.getInt( MAX );
	}
}
