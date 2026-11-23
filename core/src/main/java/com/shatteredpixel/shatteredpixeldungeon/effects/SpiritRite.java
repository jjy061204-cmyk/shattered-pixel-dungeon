/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사 갑옷 능력 이펙트 모음
 *  - 땅에 눕혀 그리는 정령 마법진(속성 색), 솟구치는 빛기둥, 퍼지는 파동 고리, 반짝임
 *  - 정령 투사: 영웅 발밑 대 마법진 → 정령이 솟는 칸마다 작은 마법진과 빛기둥 → 적중마다 파동 → 마지막에 수렴 폭발
 *  - 정령 에너지 활성화: 네 속성 구슬이 나선을 그리며 영웅에게 빨려 들어가 빛기둥과 함께 터지고,
 *    지속되는 동안 발밑에 천천히 도는 마법진이 맥동
 *  - 정령왕 소환: 내려앉을 칸에 큰 마법진 → 거대한 빛기둥 → 겹 파동
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class SpiritRite {

	public static final String TEX = "effects/spirit_rite.png";

	//속성 구슬 색 (물·불·바람·땅)
	public static final int[] ELEMENTS = { 0x5AB4FF, 0xFF7A2A, 0x7CF0A8, 0xE0A850 };
	public static final int MINT = 0x9CFFC8;

	//텍스처 안 그림 위치
	private static final int[] SIGIL = { 0, 0, 64, 64 };
	private static final int[] RING  = { 64, 0, 64, 64 };
	private static final int[] PILLAR= { 128, 0, 16, 64 };
	private static final int[] STAR  = { 144, 0, 16, 16 };
	private static final int[] ORB   = { 144, 16, 16, 16 };

	//빛을 더해 그리는(가산) 그림 한 장. 시간에 따라 크기·투명도·회전이 바뀜
	public static class Glyph extends Image {

		float t = 0, delay, life, fadeIn, s0, s1, spin, flat, a0;
		float cx, cy;
		boolean pulse = false;

		Glyph( int[] f, float cx, float cy, int color, float s0, float s1, float life ){
			super( TEX, f[0], f[1], f[2], f[3] );
			this.cx = cx; this.cy = cy;
			this.s0 = s0; this.s1 = s1; this.life = life;
			fadeIn = Math.min( 0.12f, life * 0.25f );
			flat = 1f; a0 = 1f;
			origin.set( width / 2f, height / 2f );
			hardlight( color );
			alpha( 0 );
			place( s0 );
		}

		Glyph delay( float d ){ delay = d; return this; }
		Glyph spin( float degPerSec ){ spin = degPerSec; angle = Random.Float( 360 ); return this; }
		//땅에 눕힌 듯 납작하게 (위에서 비스듬히 본 원)
		Glyph flat( float f ){ flat = f; place( s0 ); return this; }
		Glyph peak( float a ){ a0 = a; return this; }

		private void place( float s ){
			scale.set( s, s * flat );
			x = cx - width / 2f;
			y = cy - height / 2f;
		}

		@Override
		public void update() {
			super.update();
			t += Game.elapsed;
			float u = t - delay;
			if (u < 0){ alpha( 0 ); return; }
			if (u >= life){ killAndErase(); return; }
			float p = u / life;
			float e = 1f - (1f - p) * (1f - p);
			place( s0 + (s1 - s0) * e );
			angle += spin * Game.elapsed;
			float a = u < fadeIn ? u / fadeIn : 1f - (u - fadeIn) / (life - fadeIn);
			alpha( a0 * Math.max( 0, a ) );
		}

		@Override
		public void draw() {
			Blending.setLightMode();
			super.draw();
			Blending.setNormalMode();
		}
	}

	//아래(발밑)를 기준으로 위로 솟구치는 빛기둥
	public static class Pillar extends Image {
		float t = 0, delay, life, h, w;
		float bx, by;
		Pillar( float bx, float by, int color, float w, float h, float life ){
			super( TEX, PILLAR[0], PILLAR[1], PILLAR[2], PILLAR[3] );
			this.bx = bx; this.by = by; this.w = w; this.h = h; this.life = life;
			origin.set( width / 2f, height );
			hardlight( color );
			alpha( 0 );
		}
		Pillar delay( float d ){ delay = d; return this; }
		@Override
		public void update() {
			super.update();
			t += Game.elapsed;
			float u = t - delay;
			if (u < 0){ alpha( 0 ); return; }
			if (u >= life){ killAndErase(); return; }
			float p = u / life;
			float rise = Math.min( 1f, p / 0.25f );
			//솟을 땐 가늘고 높게, 사라질 땐 가늘어지며 흐려짐
			scale.set( w / width * (p < 0.25f ? 0.6f + 0.4f * rise : 1f - 0.7f * (p - 0.25f) / 0.75f), h / height * (0.3f + 0.7f * rise) );
			x = bx - width / 2f;
			y = by - height;
			alpha( p < 0.25f ? rise : 1f - (p - 0.25f) / 0.75f );
		}
		@Override
		public void draw() {
			Blending.setLightMode();
			super.draw();
			Blending.setNormalMode();
		}
	}

	//나선을 그리며 중심으로 빨려 드는 속성 구슬
	public static class Orbiter extends Image {
		float t = 0, delay, life, r0, a0, turns;
		final CharSprite center;
		final Callback onArrive;
		final int color;
		boolean arrived = false;
		Orbiter( CharSprite center, int color, float r0, float a0, float turns, float life, float delay, Callback onArrive ){
			super( TEX, ORB[0], ORB[1], ORB[2], ORB[3] );
			this.center = center; this.color = color; this.r0 = r0; this.a0 = a0; this.turns = turns;
			this.life = life; this.delay = delay; this.onArrive = onArrive;
			origin.set( width / 2f, height / 2f );
			hardlight( color );
			alpha( 0 );
		}
		@Override
		public void update() {
			super.update();
			t += Game.elapsed;
			float u = t - delay;
			if (u < 0){ alpha( 0 ); return; }
			float p = Math.min( 1f, u / life );
			PointF c = center.center();
			float r = r0 * (1f - p * p);
			float a = a0 + turns * 6.2832f * p * p;
			float px = c.x + (float) Math.cos( a ) * r;
			float py = c.y + (float) Math.sin( a ) * r * 0.6f;
			scale.set( 0.55f + 0.35f * (1f - p) );
			x = px - width / 2f;
			y = py - height / 2f;
			alpha( Math.min( 1f, u / 0.1f ) );
			//꼬리처럼 남는 작은 반짝임
			if (Random.Float() < 0.5f){
				Glyph g = new Glyph( STAR, px, py, color, 0.45f, 0.1f, 0.25f );
				if (parent != null) parent.add( g );
			}
			if (p >= 1f && !arrived){
				arrived = true;
				killAndErase();
				if (onArrive != null) onArrive.call();
			}
		}
		@Override
		public void draw() {
			Blending.setLightMode();
			super.draw();
			Blending.setNormalMode();
		}
	}

	//정령 에너지가 넘치는 동안 영웅 발밑에서 천천히 돌며 맥동하는 마법진
	public static class SurgeSigil extends Image {
		final CharSprite owner;
		float t = 0, fade = -1;
		final float size;
		public SurgeSigil( CharSprite owner, int color, float size ){
			super( TEX, SIGIL[0], SIGIL[1], SIGIL[2], SIGIL[3] );
			this.owner = owner; this.size = size;
			origin.set( width / 2f, height / 2f );
			hardlight( color );
			alpha( 0 );
		}
		public void stop(){ if (fade < 0) fade = 0; }
		@Override
		public void update() {
			super.update();
			t += Game.elapsed;
			if (owner.parent == null){ killAndErase(); return; }
			angle += 40f * Game.elapsed;
			float s = size / width * (1f + 0.04f * (float) Math.sin( t * 5f ));
			scale.set( s, s * 0.5f );
			PointF c = owner.center();
			x = c.x - width / 2f;
			y = owner.y + owner.height() - height / 2f - 1f;
			visible = owner.visible;
			float a = (0.45f + 0.25f * (float) Math.sin( t * 4f )) * Math.min( 1f, t / 0.3f );
			if (fade >= 0){
				fade += Game.elapsed;
				a *= Math.max( 0, 1f - fade / 0.4f );
				if (fade >= 0.4f){ killAndErase(); return; }
			}
			alpha( a );
		}
		@Override
		public void draw() {
			Blending.setLightMode();
			super.draw();
			Blending.setNormalMode();
		}
	}

	// ---------- 기본 조각 ----------

	public static PointF feet( CharSprite s ){
		return new PointF( s.center().x, s.y + s.height() - 1f );
	}

	public static PointF cellFeet( int cell ){
		PointF p = DungeonTilemap.tileCenterToWorld( cell );
		return new PointF( p.x, p.y + 4f );
	}

	//땅에 눕힌 마법진 (지름 size 픽셀)
	public static Glyph sigil( PointF c, int color, float size, float life, float delay ){
		Glyph g = new Glyph( SIGIL, c.x, c.y, color, size / 64f * 0.35f, size / 64f, life ).flat( 0.5f ).spin( 90f ).delay( delay );
		GameScene.groundEffect( g );
		return g;
	}

	public static Glyph ring( PointF c, int color, float from, float to, float life, float delay ){
		Glyph g = new Glyph( RING, c.x, c.y, color, from / 64f, to / 64f, life ).delay( delay );
		GameScene.effect( g );
		return g;
	}

	public static Glyph groundRing( PointF c, int color, float from, float to, float life, float delay ){
		Glyph g = new Glyph( RING, c.x, c.y, color, from / 64f, to / 64f, life ).flat( 0.5f ).delay( delay );
		GameScene.groundEffect( g );
		return g;
	}

	public static Pillar pillar( PointF base, int color, float w, float h, float life, float delay ){
		Pillar p = new Pillar( base.x, base.y, color, w, h, life ).delay( delay );
		GameScene.effect( p );
		return p;
	}

	public static Glyph star( PointF c, int color, float size, float life, float delay ){
		Glyph g = new Glyph( STAR, c.x, c.y, color, size / 16f, size / 16f * 0.3f, life ).spin( 180f ).delay( delay );
		GameScene.effect( g );
		return g;
	}

	// ---------- 능력별 연출 ----------

	//정령 투사 시작: 영웅 발밑에 큰 마법진, 바깥으로 퍼지는 고리
	public static void volleyCast( CharSprite hero ){
		PointF f = feet( hero );
		sigil( f, MINT, 40, 1.1f, 0 );
		groundRing( f, MINT, 10, 56, 0.6f, 0.05f );
		star( hero.center(), 0xFFFFFF, 12, 0.35f, 0 );
	}

	//정령이 땅에서 솟는 칸: 작은 마법진 + 가는 빛기둥
	public static void emerge( PointF ground, int color ){
		PointF f = new PointF( ground.x, ground.y + 4f );
		sigil( f, color, 14, 0.55f, 0 );
		pillar( f, color, 5, 22, 0.4f, 0 );
	}

	//정령 하나가 적중: 속성 색 파동 + 반짝임
	public static void hit( CharSprite target, int color ){
		PointF c = target.center();
		ring( c, color, 4, 18, 0.28f, 0 );
		star( new PointF( c.x + Random.Float( -3, 3 ), c.y + Random.Float( -3, 3 ) ), color, 9, 0.22f, 0 );
	}

	//마지막 정령까지 닿으면: 네 속성 고리가 겹쳐 퍼지고, 흰 섬광과 함께 땅의 마법진이 터짐
	public static void volleyFinale( PointF c, PointF ground ){
		for (int i = 0; i < ELEMENTS.length; i++){
			ring( c, ELEMENTS[i], 6, 34 + i * 4, 0.45f, i * 0.05f );
		}
		star( c, 0xFFFFFF, 22, 0.4f, 0 );
		sigil( ground, MINT, 30, 0.7f, 0 );
		pillar( ground, MINT, 10, 34, 0.5f, 0.02f );
		PixelScene.shake( 1.5f, 0.25f );
	}

	//정령 에너지 활성화: 네 속성 구슬이 나선으로 빨려 들어 터짐
	public static void surge( final CharSprite hero, final int main ){
		final PointF f = feet( hero );
		sigil( f, main, 44, 1.3f, 0 );
		final int[] left = { ELEMENTS.length };
		for (int i = 0; i < ELEMENTS.length; i++){
			Orbiter o = new Orbiter( hero, ELEMENTS[i], 26, i * 1.5708f, 1.25f, 0.55f, i * 0.04f, new Callback() {
				@Override
				public void call() {
					left[0]--;
					if (left[0] > 0) return;
					PointF ff = feet( hero );
					pillar( ff, main, 14, 46, 0.6f, 0 );
					pillar( ff, 0xFFFFFF, 6, 40, 0.45f, 0 );
					ring( hero.center(), main, 6, 44, 0.5f, 0 );
					groundRing( ff, main, 8, 64, 0.6f, 0.05f );
					star( hero.center(), 0xFFFFFF, 20, 0.4f, 0 );
					PixelScene.shake( 1f, 0.2f );
				}
			} );
			GameScene.effect( o );
		}
	}

	//정령왕 강림: 큰 마법진이 먼저 깔리고, 거대한 빛기둥과 겹 파동
	public static void kingDescends( int cell, int color ){
		PointF f = cellFeet( cell );
		sigil( f, color, 52, 1.4f, 0 );
		sigil( f, 0xFFFFFF, 30, 1.0f, 0.1f ).peak( 0.6f );
		pillar( f, color, 18, 64, 0.8f, 0.15f );
		pillar( f, 0xFFFFFF, 7, 58, 0.6f, 0.18f );
		groundRing( f, color, 10, 80, 0.7f, 0.2f );
		groundRing( f, color, 10, 60, 0.6f, 0.35f );
		star( new PointF( f.x, f.y - 12 ), 0xFFFFFF, 26, 0.5f, 0.2f );
	}
}
