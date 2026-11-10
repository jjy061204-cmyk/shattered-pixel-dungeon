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

//사선 베기: 검이 위에서 아래로 비스듬히 내리그어지고(잔상 포함), 적 몸에 하얀 사선 칼자국이 남음
public class SwordSwing {

	private static final float SWING = 0.10f;   //내리긋는 시간
	private static final float HOLD  = 0.05f;   //다 그은 자세로 멈춤
	private static final float FADE  = 0.12f;   //사라지는 시간

	//검 그림 (손잡이를 축으로 회전). lag 만큼 늦게 따라오는 잔상도 같은 클래스로 그림
	private static class Blade extends ItemSprite {
		private final float from, to, lag, maxAlpha;
		private float time = 0;
		Blade( int image, float hiltX, float hiltY, float cx, float cy, float from, float to, float lag, float maxAlpha ){
			super( image, null );
			this.from = from; this.to = to; this.lag = lag; this.maxAlpha = maxAlpha;
			origin.set( hiltX, hiltY );
			x = cx - hiltX;
			y = cy - hiltY;
			angle = from;
			scale.set( 1.15f );     //실제 휘두를 때는 조금 크게
			alpha( 0 );
			if (lag > 0) tint( 0.75f, 0.85f, 1f, 0.5f );   //잔상은 푸르스름하게
		}
		@Override
		public void update() {
			super.update();
			time += Game.elapsed;
			float t = time - lag;
			if (t < 0){
				alpha( 0 );
			} else if (t < SWING){
				float p = t / SWING;
				p = p * p * (3f - 2f * p);          //부드럽게 가속·감속
				angle = from + (to - from) * p;
				alpha( maxAlpha );
			} else if (t < SWING + HOLD){
				angle = to;
				alpha( maxAlpha );
			} else if (t < SWING + HOLD + FADE){
				angle = to;
				alpha( maxAlpha * (1f - (t - SWING - HOLD) / FADE) );
			} else {
				killAndErase();
			}
		}
	}

	//적 몸을 가로지르는 사선 칼자국 (한쪽 끝에서 쭉 그어짐)
	private static class Cut extends Image {
		private float time = 0;
		private final float delay;
		Cut( float cx, float cy, float angle, float delay ){
			super( Assets.Effects.SWORD_SLASH );
			this.delay = delay;
			origin.set( 0, height / 2f );
			//칼자국 가운데가 적 중심에 오도록 시작점을 뒤로 뺌
			double r = Math.toRadians( angle );
			x = cx - (float) Math.cos( r ) * width / 2f;
			y = cy - (float) Math.sin( r ) * width / 2f - height / 2f;
			this.angle = angle;
			scale.set( 0, 1 );
			alpha( 0 );
		}
		@Override
		public void update() {
			super.update();
			time += Game.elapsed;
			float t = time - delay;
			if (t < 0) return;
			if (t < 0.06f){
				scale.set( t / 0.06f, 1f );
				alpha( 0.9f );
			} else if (t < 0.26f){
				float p = (t - 0.06f) / 0.2f;
				scale.set( 1f + 0.1f * p, 1f - 0.6f * p );
				alpha( 0.9f * (1f - p) );
			} else {
				killAndErase();
			}
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
		float aim = dir - bladeAngle;

		//항상 위에서 아래로 내리긋도록, 적이 왼쪽이면 방향을 뒤집음
		boolean right = Math.cos( Math.toRadians( dir ) ) >= -0.01;
		float from = right ? aim - 105f : aim + 105f;
		float to   = right ? aim + 35f  : aim - 35f;

		//잔상 2개 + 본체
		GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, from, to, 0.035f, 0.25f ) );
		GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, from, to, 0.018f, 0.45f ) );
		GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, from, to, 0f, 1f ) );

		//검이 적을 지나가는 순간 사선 칼자국 (오른쪽 적: 왼쪽 위→오른쪽 아래)
		float cutAngle = right ? dir + 55f : dir - 55f;
		GameScene.effect( new Cut( b.x, b.y, cutAngle, SWING * 0.55f ) );
	}
}
