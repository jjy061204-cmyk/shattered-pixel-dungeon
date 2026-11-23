/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 에테르 기사의 오로라 참격 이펙트: 영웅을 축으로 오로라빛 초승달 참격이 부채꼴로 크게 휘둘러지고,
 * 초록·하늘·보랏빛 잔상이 뒤따른 뒤 커튼처럼 일렁이며 사라짐. 부채꼴 안에 별가루가 흩날림
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

public class AuroraSlash extends Group {

	private static final float SWEEP = 0.16f;   //휘둘러지는 시간
	private static final float HOLD  = 0.10f;
	private static final float FADE  = 0.55f;
	private static final float TEX_RADIUS = 88f; //텍스처 초승달 바깥 반지름

	private static class Layer extends Image {
		final float lag, maxAlpha, from, to;
		float time = 0;
		Layer( float cx, float cy, float from, float to, float lag, float maxAlpha, float scale, int tint ){
			super( Assets.Effects.AURORA_SLASH );
			this.lag = lag; this.maxAlpha = maxAlpha; this.from = from; this.to = to;
			origin.set( 0, height / 2f );
			x = cx;
			y = cy - height / 2f;
			angle = from;
			this.scale.set( scale );
			if (tint != -1) hardlight( tint );
			alpha( 0 );
		}
		@Override
		public void update() {
			super.update();
			time += Game.elapsed;
			float t = time - lag;
			float base = scale.x;
			if (t < 0){
				alpha( 0 );
			} else if (t < SWEEP){
				float p = t / SWEEP;
				p = 1f - (1f - p) * (1f - p);
				angle = from + (to - from) * p;
				alpha( maxAlpha * Math.min( 1f, p * 2f ) );
			} else if (t < SWEEP + HOLD){
				angle = to;
				alpha( maxAlpha );
			} else if (t < SWEEP + HOLD + FADE){
				float p = (t - SWEEP - HOLD) / FADE;
				angle = to + 6f * p * Math.signum( to - from );
				//커튼처럼 일렁이며 사라짐
				float wave = 0.85f + 0.15f * (float) Math.sin( p * 18f );
				alpha( maxAlpha * (1f - p) * wave );
				scale.set( base * (1f + 0.0025f * Game.elapsed * 60f) );
			} else {
				killAndErase();
			}
		}
	}

	private float life = 0;

	private AuroraSlash( float cx, float cy, float aim, float scale ){
		super();
		//위에서 아래로 크게 베어 내리는 방향 (오른쪽을 볼 때 시계 방향)
		boolean right = Math.cos( Math.toRadians( aim ) ) >= 0;
		float from = aim + (right ? -75f : 75f);
		add( new Layer( cx, cy, from, aim, 0.09f, 0.45f, scale * 0.94f, 0xC8A8FF ) );   //보랏빛 잔상
		add( new Layer( cx, cy, from, aim, 0.05f, 0.60f, scale * 0.97f, 0x6EFFAA ) );   //초록빛 잔상
		add( new Layer( cx, cy, from, aim, 0f,    1f,    scale, -1 ) );                 //본 참격
	}

	@Override
	public void update() {
		super.update();
		life += Game.elapsed;
		if (life > SWEEP + HOLD + FADE + 0.2f) killAndErase();
	}

	@Override
	public void draw() {
		Blending.setLightMode();
		super.draw();
		Blending.setNormalMode();
	}

	public static void play( CharSprite from, int cell, float range, float degrees ){
		if (from == null || from.parent == null) return;
		PointF c = from.center();
		PointF t = DungeonTilemap.tileCenterToWorld( cell );
		float aim = (float) Math.toDegrees( Math.atan2( t.y - c.y, t.x - c.x ) );
		float radius = (range + 0.5f) * DungeonTilemap.SIZE;
		GameScene.effectGizmo( new AuroraSlash( c.x, c.y, aim, radius / TEX_RADIUS ) );

		//가운데에서 터지는 빛
		new Flare( 8, 28 ).color( 0xFFF0C0, true ).show( from, 0.6f );

		//부채꼴 안에 흩날리는 별가루
		if (Dungeon.level != null){
			Ballistica core = new Ballistica( from.ch != null ? from.ch.pos : cell, cell, Ballistica.WONT_STOP );
			ConeAOE cone = new ConeAOE( core, range, degrees, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET );
			for (int cc : cone.cells){
				if (Dungeon.level.heroFOV[cc]) CellEmitter.center( cc ).burst( StarlightParticle.BURST, 2 );
			}
		}
	}
}
