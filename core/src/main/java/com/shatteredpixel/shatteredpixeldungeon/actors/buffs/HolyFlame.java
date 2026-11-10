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
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

//성광의 낙인: 엑스칼리버에 베인 적의 몸에 새겨지는 신성한 빛.
//불 계열이 아니라 화염 면역인 적에게도 통하고, 바닥·풀·아이템으로 번지지 않음
public class HolyFlame extends Buff {

	public static final float DURATION = 3f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	private float left;
	private int min, max;

	private transient Emitter emitter;

	//낙인이 남아 있을 때 또 베면 남은 턴이 늘어남 (악마에게는 더 많이)
	public static final float EXTEND = 2f;
	public static final float EXTEND_DEMON = 3f;
	public static final float MAX_LEFT = 10f;
	public static final float DEMON_DMG = 2f;

	public void set( float duration, int min, int max ){
		if (left > 0){
			left = Math.min( MAX_LEFT, left + (isDemon( target ) ? EXTEND_DEMON : EXTEND) );
		} else {
			left = duration;
		}
		this.min = Math.max( this.min, min );
		this.max = Math.max( this.max, max );
	}

	//악마의 소굴 악마들과 요그-조스(와 그 주먹, 애벌레)
	public static boolean isDemon( Char ch ){
		return ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DemonSpawner
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa.Larva
				|| ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
	}

	@Override
	public boolean act() {
		if (target.isAlive()){
			int dmg = Random.NormalIntRange( min, max );
			//악마에게는 두 배로 타오름
			if (isDemon( target )) dmg = Math.round( dmg * DEMON_DMG );
			//불이 아닌 신성한 빛이라 화염 면역·저항과 상관없이 통함
			target.damage( dmg, this );
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
		return BuffIndicator.RADIANT_BRAND;
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
