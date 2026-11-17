/*
 * Shattered Pixel Dungeon 모드: 영웅 고유 공격 이펙트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;

//공격 대상 칸에 겹쳐 그리는 6장짜리 이펙트 (effects/hero_fx.png, 48x48 칸)
// 0줄: 뱀파이어의 은빛 장검 참격, 1줄: 유물수집가의 보랏빛 연금 폭발, 2줄: 정령사의 초록 정령빛과 잎사귀
public class HeroAttackFX extends Image {

	public static final int SLASH = 0, ALCHEMY = 1, LEAF = 2;
	private static final int SIZE = 48, FRAMES = 6;
	private static final float DUR = 0.32f;
	private final float delay;

	private final TextureFilm film;
	private final int row;
	private float t = 0;

	public HeroAttackFX( CharSprite from, int cell, int row ){
		super( "effects/hero_fx.png" );
		film = new TextureFilm( texture, SIZE, SIZE );
		this.row = row;
		//칼을 내려베는 프레임(공격 3~4번째 장)에 맞춤
		delay = row == SLASH ? 0.12f : row == LEAF ? 0.2f : 0.07f;   //정령사: 완드를 겨눠 터뜨리는 5번째 장면에 맞춤
		frame( film.get( row * FRAMES ) );
		origin.set( SIZE / 2f, SIZE / 2f );
		scale.set( 0.56f );
		PointF c = DungeonTilemap.raisedTileCenterToWorld( cell );
		x = c.x - SIZE / 2f;
		y = c.y - SIZE / 2f;
		flipHorizontal = from.flipHorizontal;
		visible = false;
	}

	@Override
	public void update() {
		super.update();
		t += Game.elapsed;
		if (t < delay) return;
		visible = true;
		float p = (t - delay) / DUR;
		if (p >= 1f){
			killAndErase();
			return;
		}
		frame( film.get( row * FRAMES + Math.min( FRAMES - 1, (int)(p * FRAMES) ) ) );
		//마지막에 조금 커지며 사라짐
		scale.set( 0.56f + 0.08f * p );
	}
}
