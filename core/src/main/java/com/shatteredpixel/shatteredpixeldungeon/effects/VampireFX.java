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
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

//뱀파이어의 성배 기술 이펙트 (effects/vampire_fx.png, 48x48 칸, 한 줄에 8장)
// 한 줄이 한 가지 이펙트. 칸 위(at)나 캐릭터 위(on)에 한 번 재생되고 사라짐
// 반투명한 붉은 에너지로 보이도록 빛 섞기(더하기)로 그림
public class VampireFX extends Image {

	public static final int BITE = 0, WINGS = 1, SLAM = 2, MIST = 3, MEND = 4, CHARM = 5, BATS = 6, SPLASH = 7, VEIL = 8,
			DREAD = 9, NOVA = 10, JAWS = 11, BATBURST = 12, SIPHON = 13, BLADE = 14, INFUSE = 15, SHACKLE = 16, PROJ = 17, AURA = 18;

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
	public void draw() {
		Blending.setLightMode();
		super.draw();
		Blending.setNormalMode();
	}

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
		public void draw() {
			Blending.setLightMode();
			super.draw();
			Blending.setNormalMode();
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

	//비행 버프(핏빛 날개): 캐릭터 등 뒤에서 퍼덕이는 에너지 날개. 캐릭터 그림이 사라지거나 kill되면 같이 사라짐
	public static class Wings extends Image {

		private final TextureFilm film;
		private final CharSprite follow;
		private float t = 0;

		public Wings( CharSprite follow ){
			super( SHEET );
			film = new TextureFilm( texture, SIZE, SIZE );
			this.follow = follow;
			origin.set( SIZE / 2f, SIZE / 2f );
			scale.set( 0.62f );
			frame( film.get( AURA * FRAMES ) );
			place();
		}

		public static Wings on( CharSprite sprite ){
			if (sprite == null || sprite.parent == null) return null;
			Wings w = new Wings( sprite );
			//캐릭터들 뒤, 바닥 위에 그림
			sprite.parent.addToBack( w );
			return w;
		}

		public boolean follows( CharSprite s ){
			return follow == s;
		}

		private void place(){
			PointF c = follow.center();
			x = c.x - SIZE / 2f;
			y = c.y - 3f - SIZE / 2f;
		}

		@Override
		public void draw() {
			Blending.setLightMode();
			super.draw();
			Blending.setNormalMode();
		}

		@Override
		public void update() {
			super.update();
			if (follow.parent == null || follow.ch == null || !follow.ch.isAlive()
					|| follow.ch.buffs( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BloodWings.class ).isEmpty()){
				killAndErase();
				return;
			}
			t += Game.elapsed;
			frame( film.get( AURA * FRAMES + ((int)(t * 8f)) % 4 ) );
			place();
			visible = follow.visible;
			alpha( follow.alpha() );
		}
	}

	//성배 기술의 조준점: 고른 적 위에 붉은 조준 표시가 따라다님
	public static class Mark extends Image {

		private final CharSprite follow;
		private float t = 0;

		public Mark( CharSprite follow ){
			super();
			copy( Icons.TARGET.get() );
			this.follow = follow;
			hardlight( 1f, 0.25f, 0.3f );
			point( follow.center( this ) );
		}

		public static Mark on( CharSprite sprite ){
			if (sprite == null || sprite.parent == null) return null;
			Mark m = new Mark( sprite );
			sprite.parent.addToFront( m );
			return m;
		}

		@Override
		public void update() {
			super.update();
			if (follow.parent == null){
				killAndErase();
				return;
			}
			t += Game.elapsed;
			point( follow.center( this ) );
			alpha( 0.75f + 0.25f * (float) Math.sin( t * 8f ) );
		}
	}

	public static void shootScaled( int kind, PointF from, PointF to, float delay, float scale, Callback callback ){
		Projectile p = new Projectile( kind, from, to, delay, callback );
		p.scale.set( scale );
		GameScene.effect( p );
	}

	//진조의 왕관: 영웅 머리 위에 얹힌 금빛 왕관 (effects/vampire_crown.png, 11x9 두 장, 이따금 보석이 반짝임)
	public static class CrownImage extends Image {

		private final TextureFilm film;
		private final CharSprite follow;
		private float t = 0;

		public CrownImage( CharSprite follow ){
			super( "effects/vampire_crown.png" );
			film = new TextureFilm( texture, 11, 9 );
			this.follow = follow;
			frame( film.get( 0 ) );
			place();
		}

		public boolean follows( CharSprite s ){
			return follow == s;
		}

		public static CrownImage on( CharSprite sprite ){
			if (sprite == null) return null;
			CrownImage c = new CrownImage( sprite );
			GameScene.effect( c );
			return c;
		}

		private void place(){
			//64칸 그림에서 머리 꼭대기(약 9칸 아래)에 얹음, 좌우를 뒤집으면 머리 가운데도 따라 옮김
			float sx = follow.scale.x, sy = follow.scale.y;
			float headX = follow.flipHorizontal ? 33.5f : 30.5f;
			x = follow.x + headX * sx - width / 2f;
			y = follow.y + 9f * sy - height;
		}

		@Override
		public void update() {
			super.update();
			if (follow.parent == null || follow.ch == null || !follow.ch.isAlive()
					|| follow.ch.buff( com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire.CrimsonCrown.Crown.class ) == null){
				killAndErase();
				return;
			}
			t += Game.elapsed;
			frame( film.get( (t % 2.4f) < 0.25f ? 1 : 0 ) );
			place();
			visible = follow.visible;
			alpha( follow.alpha() );
		}
	}

	public static void shoot( int kind, PointF from, PointF to, float delay, Callback callback ){
		GameScene.effect( new Projectile( kind, from, to, delay, callback ) );
	}
}
