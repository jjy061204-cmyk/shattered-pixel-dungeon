/*
 * Shattered Pixel Dungeon 모드: 정령이 깃든 장비 아이콘 위에서 피어오르는 작은 정령 입자
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand.Spirit;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

//인벤토리·바닥의 장비 그림 위에서만 쓰는 작은 입자. 정령마다 움직임과 색이 다름
public class SpiritItemParticle extends PixelParticle {

	public static Emitter.Factory factory( final Spirit a, final Spirit b ){
		return new Emitter.Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				Spirit s = (b != null && (index & 1) == 1) ? b : a;
				((SpiritItemParticle) emitter.recycle( SpiritItemParticle.class )).reset( s, x, y );
			}
			@Override
			public boolean lightMode() {
				return !dim( a ) && (b == null || !dim( b ));
			}
		};
	}

	//어두운 색 정령은 빛 섞기(가산)를 쓰면 안 보여서 보통 그리기
	private static boolean dim( Spirit s ){
		switch (s){
			case DARK: case EARTH: case ROCK: case GNOME: return true;
			default: return false;
		}
	}

	private Spirit spirit;
	private int baseColor;
	private float maxSize;
	private float wobble;

	public void reset( Spirit s, float x, float y ){
		revive();
		spirit = s;
		baseColor = s.color;
		this.x = x;
		this.y = y;
		speed.set( 0 );
		acc.set( 0 );
		am = 1f;
		wobble = Random.Float( 6.28f );
		maxSize = s.strong ? 2f : 1.5f;
		left = lifespan = Random.Float( 0.6f, 1.0f );

		switch (s){
			case FIRE: case FLAME: case SALAMANDER:
				//불꽃: 노랗게 솟았다가 정령 색으로 식으며 위로
				speed.set( Random.Float( -1.5f, 1.5f ), -Random.Float( 8, 13 ) );
				break;
			case WATER: case UNDINE:
				//물: 위에서 똑똑 떨어지는 물방울
				speed.set( 0, Random.Float( 2, 4 ) );
				acc.set( 0, 22 );
				maxSize = 1.5f;
				break;
			case FROST: case ICE:
				//서리: 천천히 흔들리며 내려오는 눈송이
				speed.set( 0, Random.Float( 3, 5 ) );
				left = lifespan = Random.Float( 1.0f, 1.4f );
				break;
			case WIND: case SYLPH:
				//바람: 옆으로 휙 스치는 줄기
				speed.set( Random.Float( 14, 20 ), Random.Float( -2, 2 ) );
				left = lifespan = Random.Float( 0.4f, 0.6f );
				maxSize = 1f;
				break;
			case NATURE:
				//자연: 살랑이며 떠오르는 잎새 빛
				speed.set( 0, -Random.Float( 3, 5 ) );
				left = lifespan = Random.Float( 1.0f, 1.4f );
				break;
			case EARTH: case ROCK: case GNOME:
				//대지·바위: 툭 떨어지는 돌가루
				speed.set( Random.Float( -2, 2 ), -Random.Float( 2, 5 ) );
				acc.set( 0, 30 );
				maxSize = s == Spirit.ROCK ? 2f : 1.5f;
				break;
			case SHOCK:
				//번개: 제자리에서 번쩍이는 불꽃
				left = lifespan = Random.Float( 0.2f, 0.4f );
				maxSize = 2f;
				break;
			case GOLD:
				//금화: 반짝 커졌다 사라지는 별빛
				left = lifespan = Random.Float( 0.5f, 0.8f );
				maxSize = 2.5f;
				break;
			case DARK:
				//어둠: 느리게 스며 오르는 그림자
				speed.set( 0, -Random.Float( 2, 4 ) );
				left = lifespan = Random.Float( 1.0f, 1.5f );
				maxSize = 2f;
				break;
			case CHAOS:
				//혼돈: 아무 방향으로 튀는 무지갯빛
				speed.polar( Random.Float( 6.28f ), Random.Float( 5, 10 ) );
				baseColor = ColorMath.random( 0xFF4444, 0x44AAFF );
				break;
			case RUNE:
				//룬: 반듯하게 떠오르는 네모난 빛
				speed.set( 0, -Random.Float( 4, 6 ) );
				maxSize = 2f;
				break;
			default:
				speed.set( 0, -Random.Float( 3, 6 ) );
				break;
		}

		color( baseColor );
		size( maxSize );
	}

	@Override
	public void update() {
		super.update();
		if (!alive || spirit == null) return;
		float p = left / lifespan;   //1 → 0
		switch (spirit){
			case FIRE: case FLAME: case SALAMANDER:
				color( ColorMath.interpolate( baseColor, 0xFFEE88, p ) );
				size( maxSize * p );
				break;
			case FROST: case ICE: case NATURE:
				wobble += 0.15f;
				speed.x = (float) Math.sin( wobble ) * 4f;
				size( maxSize * Math.min( 1f, p * 2f ) );
				break;
			case SHOCK:
				am = Random.Int( 3 ) == 0 ? 0.2f : 1f;
				size( maxSize * p );
				break;
			case GOLD:
				//가운데에서 가장 크게 반짝임
				size( maxSize * (1f - Math.abs( p * 2f - 1f )) );
				break;
			case DARK:
				am = Math.min( 1f, p * 2f ) * 0.9f;
				size( maxSize * (0.5f + p * 0.5f) );
				break;
			default:
				size( maxSize * p );
				break;
		}
	}
}
