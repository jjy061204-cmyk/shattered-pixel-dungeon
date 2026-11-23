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

//공격 대상 칸에 겹쳐 그리는 6장짜리 이펙트 (48x48 칸)
// effects/hero_fx.png
//  0줄: 뱀파이어의 은빛 장검 참격, 1줄: 유물수집가의 보랏빛 연금 폭발, 2줄: 정령사의 초록 정령빛과 잎사귀
//  3줄: 마법 기사의 반투명한 에테르 초승달 참격
// effects/form_fx.png : 형상 기사의 무기 종류별 기본 공격 (EtherForms.Type 순서)
// effects/art_fx.png  : 에테르 기사의 기술 (0 에테르 탄 착탄, 1 섬광 교차 참격, 2 에테르 폭발 고리, 3 에테르 탄 본체)
//
//방향이 있는 이펙트(참격·찌르기)는 그림이 오른쪽(공격 방향)을 보도록 그려져 있어,
//영웅 → 대상 방향으로 돌려 그림. 왼쪽을 향할 때는 좌우를 뒤집어 휘두르는 방향(위→아래)이 늘 같게 보임.
//그리고 영웅 쪽에서 대상 쪽으로 살짝 밀려 나가며 휘둘러지는 느낌을 줌
public class HeroAttackFX extends Image {

	public static final int SLASH = 0, ALCHEMY = 1, LEAF = 2, ETHER = 3;
	public static final String HERO_FX = "effects/hero_fx.png";
	public static final String FORM_FX = "effects/form_fx.png";
	public static final String ART_FX  = "effects/art_fx.png";

	private static final int SIZE = 48, FRAMES = 6;
	private final float dur;
	private final float delay;
	private final float baseScale;

	private final TextureFilm film;
	private final int row;
	private float t = 0;

	private final boolean directional;
	private float cx, cy, dx, dy;   //가운데 자리와 공격 방향(단위 벡터)

	public HeroAttackFX( CharSprite from, int cell, int row ){
		this( from, cell, HERO_FX, row,
				//칼을 내려베는 프레임(공격 3~4번째 장)에 맞춤. 정령사: 완드를 겨눠 터뜨리는 5번째 장면, 마법 기사: 마력이 가장 크게 타오르는 장면
				row == SLASH ? 0.12f : row == LEAF ? 0.2f : row == ETHER ? 0.17f : 0.07f,
				row == SLASH || row == ETHER, 0.56f, 0.32f );
	}

	public HeroAttackFX( CharSprite from, int cell, String tex, int row, float delay, boolean directional, float scale, float dur ){
		super( tex );
		film = new TextureFilm( texture, SIZE, SIZE );
		this.row = row;
		this.delay = delay;
		this.dur = dur;
		this.baseScale = scale;
		this.directional = directional;
		frame( film.get( row * FRAMES ) );
		origin.set( SIZE / 2f, SIZE / 2f );
		this.scale.set( scale );

		PointF c = DungeonTilemap.raisedTileCenterToWorld( cell );
		cx = c.x; cy = c.y;
		PointF a = from != null ? from.center() : c;
		float vx = c.x - a.x, vy = c.y - a.y;
		float len = (float) Math.sqrt( vx*vx + vy*vy );
		if (len < 0.01f){
			//제자리: 바라보는 쪽
			vx = from != null && from.flipHorizontal ? -1 : 1; vy = 0; len = 1;
		}
		dx = vx / len; dy = vy / len;

		if (directional){
			float dir = (float) Math.toDegrees( Math.atan2( dy, dx ) );
			//곧장 위·아래일 때는 영웅이 바라보는 쪽을 따름
			boolean right = Math.abs( dx ) < 0.05f ? !(from != null && from.flipHorizontal) : dx > 0;
			flipHorizontal = !right;
			float ang = right ? dir : dir - 180f;
			while (ang > 180f) ang -= 360f;
			while (ang < -180f) ang += 360f;
			angle = ang;
		} else {
			flipHorizontal = from != null && from.flipHorizontal;
		}
		place( 0 );
		visible = false;
	}

	//p: 0 → 1 동안 영웅 쪽 4칸(px)에서 대상 너머 2칸(px)으로 밀려 나감
	private void place( float p ){
		float off = directional ? (-4f + 6f * p) : 0f;
		x = cx + dx * off - SIZE / 2f;
		y = cy + dy * off - SIZE / 2f;
	}

	@Override
	public void update() {
		super.update();
		t += Game.elapsed;
		if (t < delay) return;
		visible = true;
		float p = (t - delay) / dur;
		if (p >= 1f){
			killAndErase();
			return;
		}
		frame( film.get( row * FRAMES + Math.min( FRAMES - 1, (int)(p * FRAMES) ) ) );
		place( p );
		//마지막에 조금 커지며 사라짐
		scale.set( baseScale * (1f + 0.14f * p) );
	}
}
