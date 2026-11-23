/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 에테르 기사의 에테르 탄: 분홍·보라·하늘빛으로 빛나는 작은 별 모양 탄이 빙글 돌며 날아가고,
 * 지나간 자리에 별가루를 흘림. 닿으면 무지갯빛 별 폭발
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class EtherBolt extends Image {

	private static final int SIZE = 48;
	private static final float SPEED = 220f;

	private final TextureFilm film;
	private final PointF from, to;
	private final float time;
	private float t = 0;
	private final Callback callback;
	private final Emitter trail;
	private final int cell;

	private EtherBolt( PointF from, PointF to, int cell, Callback callback ){
		super( HeroAttackFX.ART_FX );
		film = new TextureFilm( texture, SIZE, SIZE );
		frame( film.get( 3 * 6 ) );
		origin.set( SIZE / 2f, SIZE / 2f );
		scale.set( 0.42f );
		this.from = from;
		this.to = to;
		this.cell = cell;
		this.callback = callback;
		time = Math.max( 0.06f, PointF.distance( from, to ) / SPEED );
		x = from.x - SIZE / 2f;
		y = from.y - SIZE / 2f;

		trail = new Emitter();
		trail.pos( from.x - 1, from.y - 1, 2, 2 );
		trail.pour( StarlightParticle.BURST, 0.02f );
		GameScene.effectGizmo( trail );
	}

	@Override
	public void update() {
		super.update();
		t += Game.elapsed;
		float p = Math.min( 1f, t / time );
		float px = from.x + (to.x - from.x) * p;
		float py = from.y + (to.y - from.y) * p;
		x = px - SIZE / 2f;
		y = py - SIZE / 2f;
		trail.pos( px - 1, py - 1, 2, 2 );
		frame( film.get( 3 * 6 + ((int)(t * 30)) % 6 ) );
		if (p >= 1f){
			trail.on = false;
			trail.autoKill = true;
			killAndErase();
			//닿은 자리의 별 폭발
			GameScene.effect( new HeroAttackFX( null, cell, HeroAttackFX.ART_FX, 0, 0f, false, 0.62f, 0.3f ) );
			if (callback != null) callback.call();
		}
	}

	public static void shoot( CharSprite from, int cell, Callback callback ){
		PointF a = from.center();
		PointF b = DungeonTilemap.raisedTileCenterToWorld( cell );
		GameScene.effect( new EtherBolt( a, b, cell, callback ) );
	}
}
