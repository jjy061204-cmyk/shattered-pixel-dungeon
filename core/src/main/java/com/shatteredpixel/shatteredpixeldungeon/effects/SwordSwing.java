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
		private final float x0, y0, dx, dy;   //찌르기용 이동량
		private float time = 0;
		Blade( int image, float hiltX, float hiltY, float cx, float cy, float from, float to, float lag, float maxAlpha ){
			this( image, hiltX, hiltY, cx, cy, from, to, lag, maxAlpha, 0, 0, 0, 0 );
		}
		Blade( int image, float hiltX, float hiltY, float cx, float cy, float from, float to, float lag, float maxAlpha,
		       float offX, float offY, float moveX, float moveY ){
			super( image, null );
			this.from = from; this.to = to; this.lag = lag; this.maxAlpha = maxAlpha;
			origin.set( hiltX, hiltY );
			x0 = cx - hiltX + offX;
			y0 = cy - hiltY + offY;
			dx = moveX; dy = moveY;
			x = x0;
			y = y0;
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
				x = x0 + dx * p;
				y = y0 + dy * p;
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
		private final float size;
		Cut( float cx, float cy, float angle, float delay ){
			this( cx, cy, angle, delay, 1f );
		}
		Cut( float cx, float cy, float angle, float delay, float size ){
			super( Assets.Effects.SWORD_SLASH );
			this.delay = delay;
			this.size = size;
			origin.set( 0, height / 2f );
			//칼자국 가운데가 적 중심에 오도록 시작점을 뒤로 뺌
			double r = Math.toRadians( angle );
			x = cx - (float) Math.cos( r ) * width * size / 2f;
			y = cy - (float) Math.sin( r ) * width * size / 2f - height / 2f;
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
				scale.set( size * t / 0.06f, size );
				alpha( 0.9f );
			} else if (t < 0.26f){
				float p = (t - 0.06f) / 0.2f;
				scale.set( size * (1f + 0.1f * p), size * (1f - 0.6f * p) );
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
		play( attacker, target, image, hiltX, hiltY, bladeAngle, 0 );
	}

	/**
	 * @param step 연속 공격 순서. 0 = 1타 내려베기, 1 = 2타 올려베기, 2 = 3타 찌르기
	 */
	public static void play( CharSprite attacker, CharSprite target, int image, float hiltX, float hiltY, float bladeAngle, int step ){
		if (attacker == null || target == null || attacker.parent == null || !attacker.visible) return;
		PointF a = attacker.center();
		PointF b = target.center();
		float dir = (float) Math.toDegrees( Math.atan2( b.y - a.y, b.x - a.x ) );
		float aim = dir - bladeAngle;

		//적이 왼쪽이면 좌우를 뒤집어 위/아래 방향이 항상 같게 보이도록
		boolean right = Math.cos( Math.toRadians( dir ) ) >= -0.01;
		float side = right ? 1f : -1f;

		switch (step % 3){
			case 0: default: {
				//1타: 위에서 아래로 내려베기 (왼쪽 위 → 오른쪽 아래 칼자국)
				swing( image, hiltX, hiltY, a, aim - 105f * side, aim + 35f * side );
				GameScene.effect( new Cut( b.x, b.y, dir + 55f * side, SWING * 0.55f ) );
				break;
			}
			case 1: {
				//2타: 아래에서 위로 올려베기 (왼쪽 아래 → 오른쪽 위 칼자국)
				swing( image, hiltX, hiltY, a, aim + 75f * side, aim - 65f * side );
				GameScene.effect( new Cut( b.x, b.y, dir - 50f * side, SWING * 0.55f ) );
				break;
			}
			case 2: {
				//3타: 뒤로 당겼다가 적을 향해 깊게 찌르기 (적을 꿰뚫는 굵은 일자 칼자국)
				double r = Math.toRadians( dir );
				float cos = (float) Math.cos( r ), sin = (float) Math.sin( r );
				float back = -7f, fwd = 13f;
				GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, aim, aim, 0.035f, 0.25f, cos * back, sin * back, cos * (fwd - back), sin * (fwd - back) ) );
				GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, aim, aim, 0.018f, 0.45f, cos * back, sin * back, cos * (fwd - back), sin * (fwd - back) ) );
				GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, aim, aim, 0f, 1f, cos * back, sin * back, cos * (fwd - back), sin * (fwd - back) ) );
				//칼자국은 적 몸을 지나 뒤쪽으로 뻗게 (영웅을 덮지 않도록 앞으로 밀어 둠)
				GameScene.effect( new Cut( b.x + cos * 10f, b.y + sin * 10f, dir, SWING * 0.6f, 0.9f ) );
				break;
			}
		}
	}

	//잔상 2개 + 본체
	private static void swing( int image, float hiltX, float hiltY, PointF a, float from, float to ){
		GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, from, to, 0.035f, 0.25f ) );
		GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, from, to, 0.018f, 0.45f ) );
		GameScene.effect( new Blade( image, hiltX, hiltY, a.x, a.y, from, to, 0f, 1f ) );
	}
}
