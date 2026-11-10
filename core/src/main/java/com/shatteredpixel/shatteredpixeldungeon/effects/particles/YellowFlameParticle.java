/*
 * Shattered Pixel Dungeon 모드: 엑스칼리버의 노란 불꽃 입자
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;

//일반 불꽃 입자와 같은 움직임에 노란~흰 금빛
public class YellowFlameParticle extends PixelParticle.Shrinking {

	public static final Emitter.Factory FACTORY = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((YellowFlameParticle)emitter.recycle( YellowFlameParticle.class )).reset( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	public YellowFlameParticle() {
		super();
		lifespan = 0.6f;
		acc.set( 0, -80 );
	}

	public void reset( float x, float y ) {
		revive();
		this.x = x;
		this.y = y;
		left = lifespan;
		size = 4;
		speed.set( 0 );
	}

	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		am = p > 0.8f ? (1 - p) * 5 : 1;
		color( ColorMath.interpolate( 0xFFB020, 0xFFF4A0, p ) );
	}
}
