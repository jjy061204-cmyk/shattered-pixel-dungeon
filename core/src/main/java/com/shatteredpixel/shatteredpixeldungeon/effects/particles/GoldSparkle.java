/*
 * Shattered Pixel Dungeon 모드: 은은한 금빛 반짝임 입자
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
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

//금빛 점이 천천히 떠오르며 깜빡이다 사라짐
public class GoldSparkle extends PixelParticle {

	//천천히 새어 나오는 반짝임 (바닥에 떨어진 무기 등)
	public static final Emitter.Factory SHIMMER = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((GoldSparkle)emitter.recycle( GoldSparkle.class )).reset( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	//한 번에 퍼지는 금빛 (봉인 해제 등)
	public static final Emitter.Factory BURST = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((GoldSparkle)emitter.recycle( GoldSparkle.class )).resetBurst( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	public GoldSparkle() {
		super();
		lifespan = 1f;
	}

	public void reset( float x, float y ) {
		revive();
		this.x = x;
		this.y = y;
		speed.set( Random.Float( -2, 2 ), Random.Float( -8, -4 ) );
		lifespan = Random.Float( 0.7f, 1.2f );
		left = lifespan;
	}

	public void resetBurst( float x, float y ) {
		revive();
		this.x = x;
		this.y = y;
		speed.polar( Random.Float( PointF.PI2 ), Random.Float( 12, 36 ) );
		lifespan = Random.Float( 0.5f, 0.9f );
		left = lifespan;
	}

	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		//살짝 깜빡이며 옅어짐
		am = p * (0.6f + 0.4f * (float) Math.abs( Math.sin( left * 18f ) ));
		size( 1 + p );
		color( ColorMath.interpolate( 0xC08020, 0xFFF0A0, p ) );
	}
}
