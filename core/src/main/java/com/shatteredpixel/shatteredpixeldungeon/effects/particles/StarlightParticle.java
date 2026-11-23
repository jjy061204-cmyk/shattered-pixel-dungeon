/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 별빛 입자: 하양·하늘·연보라·연두 빛 작은 별가루.
 *  ATTRACT - 바깥에서 가운데로 모여듦 (오로라 참격을 모을 때)
 *  BURST   - 가운데에서 사방으로 흩어짐
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class StarlightParticle extends PixelParticle {

	private static final int[] COLORS = { 0xFFFFFF, 0xB8E0FF, 0xC8A8FF, 0x9CF0D8, 0xFFF0A0 };

	public static final Emitter.Factory ATTRACT = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((StarlightParticle) emitter.recycle( StarlightParticle.class )).resetAttract( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	public static final Emitter.Factory BURST = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((StarlightParticle) emitter.recycle( StarlightParticle.class )).resetBurst( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	private boolean attract;

	public StarlightParticle() {
		super();
		lifespan = 0.7f;
	}

	private void pickColor(){
		color( COLORS[Random.Int( COLORS.length )] );
	}

	public void resetAttract( float x, float y ) {
		revive();
		attract = true;
		pickColor();
		lifespan = Random.Float( 0.5f, 0.8f );
		left = lifespan;
		speed.polar( Random.Float( PointF.PI2 ), Random.Float( 20, 34 ) );
		this.x = x - speed.x * lifespan;
		this.y = y - speed.y * lifespan;
	}

	public void resetBurst( float x, float y ) {
		revive();
		attract = false;
		pickColor();
		lifespan = Random.Float( 0.4f, 0.7f );
		left = lifespan;
		speed.polar( Random.Float( PointF.PI2 ), Random.Float( 16, 40 ) );
		this.x = x;
		this.y = y;
	}

	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		if (attract){
			am = p < 0.5f ? p * p * 4 : (1 - p) * 2;
			size( 1 + Random.Float( 2.5f * p ) );
		} else {
			am = p;
			size( 1 + 2.5f * p );
			speed.scale( 0.94f );
		}
	}
}
