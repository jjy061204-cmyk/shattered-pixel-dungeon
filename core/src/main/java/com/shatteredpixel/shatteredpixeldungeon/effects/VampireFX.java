/*
 * Shattered Pixel Dungeon 모드: 영웅 고유 공격 이펙트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

//뱀파이어의 성배 기술 이펙트 (effects/vampire_fx.png, 48x48 칸, 한 줄에 8장)
// 한 줄이 한 가지 이펙트. 칸 위(at)나 캐릭터 위(on)에 한 번 재생되고 사라짐
public class VampireFX extends Image {

	public static final int BITE = 0, WINGS = 1, SLAM = 2, MIST = 3, MEND = 4, CHARM = 5, BATS = 6, SPLASH = 7, VEIL = 8,
			DREAD = 9, NOVA = 10, JAWS = 11, BATBURST = 12, SIPHON = 13, BLADE = 14, INFUSE = 15, SHACKLE = 16, PROJ = 17;

	public static final String SHEET = "effects/vampire_fx.png";
	private static final int SIZE = 48, FRAMES = 8;

	private final TextureFilm film;
	private final int row;
	private final float dur;
	private float t = 0, delay = 0;
	private float size0, size1;
	private boolean reverse = false;

	private CharSprite follow;
	private PointF center;
	private float offX = 0, offY = 0, rise = 0;

	private VampireFX( int row, CharSprite follow, PointF center, float size, float dur ){
		super( SHEET );
		film = new TextureFilm( texture, SIZE, SIZE );
		this.row = row;
		this.follow = follow;
		this.center = center;
		this.size0 = this.size1 = size;
		this.dur = dur;
		origin.set( SIZE / 2f, SIZE / 2f );
		frame( film.get( row * FRAMES ) );
		visible = false;
	}

	//칸 가운데에 재생 (size 1 = 3칸 너비)
	public static VampireFX at( int cell, int row, float size, float dur ){
		VampireFX fx = new VampireFX( row, null, DungeonTilemap.raisedTileCenterToWorld( cell ), size, dur );
		GameScene.effect( fx );
		return fx;
	}

	//캐릭터를 따라다니며 재생
	public static VampireFX on( CharSprite sprite, int row, float size, float dur ){
		VampireFX fx = new VampireFX( row, sprite, sprite != null ? sprite.center() : new PointF(), size, dur );
		if (sprite != null) GameScene.effect( fx );
		return fx;
	}

	public VampireFX delay( float d ){ delay = d; return this; }
	public VampireFX reversed(){ reverse = true; return this; }
	public VampireFX grow( float to ){ size1 = to; return this; }
	public VampireFX offset( float dx, float dy ){ offX = dx; offY = dy; return this; }
	public VampireFX rise( float px ){ rise = px; return this; }

	@Override
	public void update() {
		super.update();
		t += Game.elapsed;
		if (t < delay) return;
		float p = (t - delay) / dur;
		if (p >= 1f){
			killAndErase();
			return;
		}
		visible = true;
		int f = Math.min( FRAMES - 1, (int)(p * FRAMES) );
		if (reverse) f = FRAMES - 1 - f;
		frame( film.get( row * FRAMES + f ) );
		scale.set( size0 + (size1 - size0) * p );
		if (follow != null && follow.parent != null) center = follow.center();
		x = center.x + offX - SIZE / 2f;
		y = center.y + offY - rise * p - SIZE / 2f;
	}

	//날아가는 것: 피의 창(날아가는 쪽으로 돌려 그림), 박쥐(날갯짓하며 날아감). 닿으면 callback
	public static class Projectile extends Image {

		public static final int LANCE = 0, BAT = 1;

		private final TextureFilm film;
		private final int kind;
		private final PointF from, to;
		private final float dur;
		private final Callback callback;
		private float t = 0, delay;

		public Projectile( int kind, PointF from, PointF to, float delay, Callback callback ){
			super( SHEET );
			film = new TextureFilm( texture, SIZE, SIZE );
			this.kind = kind;
			this.from = from;
			this.to = to;
			this.delay = delay;
			this.callback = callback;
			float dist = (float) Math.hypot( to.x - from.x, to.y - from.y );
			dur = Math.max( 0.12f, dist / (kind == LANCE ? 320f : 190f) );
			origin.set( SIZE / 2f, SIZE / 2f );
			if (kind == LANCE){
				angle = (float)(Math.atan2( to.y - from.y, to.x - from.x ) * 180 / Math.PI);
				scale.set( 0.6f );
				frame( film.get( PROJ * FRAMES ) );
			} else {
				scale.set( 0.5f );
				frame( film.get( PROJ * FRAMES + 1 ) );
			}
			visible = false;
		}

		@Override
		public void update() {
			super.update();
			t += Game.elapsed;
			if (t < delay) return;
			float p = (t - delay) / dur;
			if (p >= 1f){
				killAndErase();
				if (callback != null) callback.call();
				return;
			}
			visible = true;
			x = from.x + (to.x - from.x) * p - SIZE / 2f;
			y = from.y + (to.y - from.y) * p - SIZE / 2f;
			if (kind == BAT){
				y += (float) Math.sin( p * Math.PI * 3 ) * 2f;
				frame( film.get( PROJ * FRAMES + 1 + ((int)(t / 0.07f) % 2) ) );
			}
		}
	}

	public static void shoot( int kind, PointF from, PointF to, float delay, Callback callback ){
		GameScene.effect( new Projectile( kind, from, to, delay, callback ) );
	}
}
