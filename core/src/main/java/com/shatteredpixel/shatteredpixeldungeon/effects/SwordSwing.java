/*
 * Shattered Pixel Dungeon 모드: 검 휘두르기 이펙트 (실험 기능)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

//공격자 몸 가운데를 축으로 검 그림이 반원을 그리며 휘둘러지고, 그 자리에 하얀 궤적이 남음
public class SwordSwing {

	private static final float SWING = 0.14f;   //휘두르는 시간
	private static final float FADE  = 0.12f;   //사라지는 시간
	private static final float ARC   = 120f;    //휘두르는 각도

	//검 그림 (손잡이를 축으로 회전)
	private static class Blade extends ItemSprite {
		private final float from, to;
		private float time = 0;
		Blade( int image, float hiltX, float hiltY, float cx, float cy, float from, float to ){
			super( image, null );
			this.from = from; this.to = to;
			origin.set( hiltX, hiltY );
			x = cx - hiltX;
			y = cy - hiltY;
			angle = from;
		}
		@Override
		public void update() {
			super.update();
			time += Game.elapsed;
			if (time < SWING){
				float p = time / SWING;
				p = 1f - (1f - p) * (1f - p);   //처음엔 빠르게, 끝에서 느려짐
				angle = from + (to - from) * p;
				alpha( 1f );
			} else if (time < SWING + FADE){
				angle = to;
				alpha( 1f - (time - SWING) / FADE );
			} else {
				killAndErase();
			}
		}
	}

	//휘두른 자리의 하얀 궤적
	private static class Trail extends Image {
		private float time = 0;
		Trail( float cx, float cy, float angle ){
			super( Assets.Effects.SWORD_SWING );
			origin.set( 0, height / 2f );
			x = cx;
			y = cy - height / 2f;
			this.angle = angle;
			alpha( 0 );
		}
		@Override
		public void update() {
			super.update();
			time += Game.elapsed;
			float total = SWING + FADE;
			if (time >= total){
				killAndErase();
				return;
			}
			if (time < SWING * 0.6f){
				alpha( 0.8f * time / (SWING * 0.6f) );
			} else {
				alpha( 0.8f * (1f - (time - SWING * 0.6f) / (total - SWING * 0.6f)) );
			}
			float s = 0.9f + 0.15f * Math.min( 1f, time / SWING );
			scale.set( s );
		}
	}

	/**
	 * @param image   ItemSpriteSheet 무기 그림 번호 (칼끝이 오른쪽 위를 향하는 기본 무기 그림)
	 * @param hiltX   그림 안 손잡이 위치 (회전 축)
	 * @param bladeAngle 그림 속 칼날이 향한 각도 (손잡이→칼끝, 화면 기준 도)
	 */
	public static void play( CharSprite attacker, CharSprite target, int image, float hiltX, float hiltY, float bladeAngle ){
		if (attacker == null || target == null || attacker.parent == null || !attacker.visible) return;
		PointF a = attacker.center();
		PointF b = target.center();
		float dir = (float) Math.toDegrees( Math.atan2( b.y - a.y, b.x - a.x ) );
		//칼날이 dir 을 향하도록 돌리는 각도, 그 앞뒤로 반원씩 휘두름
		float aim = dir - bladeAngle;
		float from = aim - ARC * 0.6f;
		float to   = aim + ARC * 0.4f;
		GameScene.effect( new Trail( a.x, a.y, dir + ARC * 0.1f - 5f ) );
		GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, from, to ) );
	}
}
