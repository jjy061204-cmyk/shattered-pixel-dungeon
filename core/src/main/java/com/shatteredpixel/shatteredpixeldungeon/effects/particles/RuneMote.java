/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 공허 웅덩이 위로 떠오르는 빛 알갱이
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
import com.watabou.utils.Random;

public class RuneMote extends PixelParticle {

	public static final Emitter.Factory FACTORY = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((RuneMote)emitter.recycle( RuneMote.class )).reset( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	private int c1, c2;

	public RuneMote() {
		super();
	}

	public void reset( float x, float y ) {
		revive();
		this.x = x;
		this.y = y;
		speed.set( Random.Float( -1.5f, 1.5f ), Random.Float( -7, -3 ) );
		lifespan = Random.Float( 1.2f, 2.2f );
		left = lifespan;
		//고대 룬의 빛: 대부분 청록, 가끔 금빛
		if (Random.Int( 4 ) == 0){ c1 = 0x907020; c2 = 0xFFF0B0; }
		else { c1 = 0x1F7F78; c2 = 0xC8FFF4; }
	}

	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		am = (p > 0.8f ? (1 - p) * 5 : p) * 0.8f;
		size( 1 + p );
		color( ColorMath.interpolate( c1, c2, p ) );
	}
}
