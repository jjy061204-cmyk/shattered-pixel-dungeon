/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 비검 기사가 던진 에테르 점토가 바닥에 칼끝을 박고 서 있는 모습. 은은하게 빛나며 별가루가 피어오름
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class ClayMarker extends ItemSprite {

	private final int cell;
	private float time = 0, nextSpark = 0.3f;

	public ClayMarker( int image, int cell ){
		super( image, null );
		this.cell = cell;
		origin.set( width / 2f, height / 2f );
		angle = 135f;   //그림의 칼끝(오른쪽 위)이 아래를 향하게: 땅에 꽂힌 모습
		PointF c = DungeonTilemap.tileCenterToWorld( cell );
		x = c.x - width / 2f;
		y = c.y - height / 2f - 4f;
		scale.set( 0.9f );
	}

	@Override
	public void update() {
		super.update();
		time += Game.elapsed;
		boolean seen = Dungeon.level != null && cell < Dungeon.level.length() && Dungeon.level.heroFOV[cell];
		visible = seen;
		//은은하게 숨쉬듯 빛남
		float p = 0.5f + 0.5f * (float) Math.sin( time * 3f );
		alpha( 0.8f + 0.2f * p );
		tint( 0.75f, 0.7f, 1f, 0.15f + 0.2f * p );
		if (seen){
			nextSpark -= Game.elapsed;
			if (nextSpark <= 0){
				nextSpark = Random.Float( 0.25f, 0.5f );
				CellEmitter.center( cell ).burst( StarlightParticle.BURST, 1 );
			}
		}
	}
}
