/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 지역 정령의 입자
 *  - 생명: 피에서 깨어난 장밋빛 생명의 빛이 천천히 떠오름
 *  - 수정: 수정 파편이 사방으로 튀었다가 떨어짐 (푸른·초록·붉은)
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class RegionSpiritParticle extends PixelParticle.Shrinking {

	public static final Emitter.Factory LIFE = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((RegionSpiritParticle) emitter.recycle( RegionSpiritParticle.class )).resetLife( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	public static final Emitter.Factory CRYSTAL_BLUE  = crystal( 0x6FB8FF, 0xCFF2FF );
	public static final Emitter.Factory CRYSTAL_GREEN = crystal( 0x5FD884, 0xC8FFD8 );
	public static final Emitter.Factory CRYSTAL_RED   = crystal( 0xFF6464, 0xFFD0C8 );

	private static Emitter.Factory crystal( final int c1, final int c2 ){
		return new Emitter.Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((RegionSpiritParticle) emitter.recycle( RegionSpiritParticle.class )).resetShard( x, y, c1, c2 );
			}
		};
	}

	public void resetLife( float x, float y ){
		revive();
		this.x = x + Random.Float( -2, 2 );
		this.y = y + Random.Float( -2, 2 );
		color( ColorMath.random( 0xFF5A7E, 0xFFC0CE ) );
		speed.set( Random.Float( -3, 3 ), -Random.Float( 6, 12 ) );
		acc.set( 0, 0 );
		left = lifespan = Random.Float( 0.7f, 1.1f );
		size = Random.Float( 2f, 3f );
	}

	public void resetShard( float x, float y, int c1, int c2 ){
		revive();
		this.x = x;
		this.y = y;
		color( ColorMath.random( c1, c2 ) );
		speed.polar( Random.Float( 6.283f ), Random.Float( 12, 26 ) );
		acc.set( 0, 40 );
		left = lifespan = Random.Float( 0.4f, 0.7f );
		size = Random.Float( 1.5f, 2.5f );
	}
}
