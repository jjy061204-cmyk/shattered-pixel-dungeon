/*
 * Shattered Pixel Dungeon 모드: 수호의 반지 마법 갑옷
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.RectF;

//영웅의 모습은 그대로 두고, 그 위에 한 겹 크게 반투명한 마법 갑옷을 씌움.
//갑옷 그림은 영웅 프레임(12x15)과 같은 순서의 16x19 칸이라 걷기·공격 동작을 그대로 따라감
public class AegisShell extends Image {

	public static final int W = 16, H = 19, MARGIN = 2;
	private static final int HERO_W = 12;

	private final CharSprite target;
	private TextureFilm film;
	private float time = 0;
	private int lastIndex = -1;

	//세밀한 도트 영웅용: 영웅 그림과 같은 64x64 칸·같은 순서로 그린 맞춤 껍데기
	private boolean hd = false;

	public AegisShell( CharSprite target, String tex ){
		super( tex );
		this.target = target;
		film = new TextureFilm( texture, W, H );
		frame( film.get( 0 ) );
	}

	public AegisShell( CharSprite target, String tex, boolean hd ){
		super( tex );
		this.target = target;
		this.hd = hd;
		if (hd){
			film = new TextureFilm( texture, com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.HD_WIDTH,
					com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.HD_HEIGHT );
			com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.smooth( texture );
		} else {
			film = new TextureFilm( texture, W, H );
		}
		frame( film.get( 0 ) );
	}

	public CharSprite target(){
		return target;
	}

	public void setLook( String tex ){
		texture( tex );
		film = new TextureFilm( texture, W, H );
		lastIndex = -1;
	}

	private int heroIndex(){
		RectF f = target.frame();
		if (f == null || target.texture == null) return 0;
		float w = f.right - f.left;
		return w <= 0 ? 0 : Math.round( f.left / w );
	}

	//위치·프레임 맞추기. 영웅의 이동 트위너가 이 그림보다 늦게 갱신될 수 있어,
	//그리기 직전에도 한 번 더 맞춰 한 프레임씩 뒤처지지 않게 함
	private void sync(){
		if (hd){
			//영웅 그림의 지금 칸과 같은 칸을 같은 크기·같은 자리에 씌움
			int idx = heroIndex();
			visible = target.visible;
			if (!visible) return;
			if (idx != lastIndex && film.get( idx ) != null){
				frame( film.get( idx ) );
				lastIndex = idx;
			}
			flipHorizontal = target.flipHorizontal;
			scale.set( target.scale );
			origin.set( target.origin );
			angle = target.angle;
			x = target.x;
			y = target.y;
			return;
		}
		boolean big = target.width() > HERO_W * 1.2f;
		//세밀한 큰 도트 영웅은 동작 순서가 달라 갑옷은 서 있는 모습 하나로 씌움
		int idx = big ? 0 : heroIndex();
		//쓰러지는 프레임(8~12)에서는 갑옷도 흩어진 뒤라 보이지 않음
		boolean dying = !big && idx >= 8 && idx <= 12;
		visible = target.visible && !dying;
		if (!visible) return;
		if (idx != lastIndex && film.get( idx ) != null){
			frame( film.get( idx ) );
			lastIndex = idx;
		}
		flipHorizontal = target.flipHorizontal;
		if (target.width() > HERO_W * 1.2f){
			//세밀한 큰 도트 영웅: 갑옷을 영웅 키에 맞춰 늘려 감쌈
			float k = target.height() * 1.08f / H;
			scale.set( k * 0.95f, k );
			x = target.x + target.width() / 2f - W * k * 0.95f / 2f;
			y = target.y + target.height() - H * k;
		} else {
			scale.set( 1f );
			x = target.x - MARGIN;
			y = target.y - MARGIN;
		}
	}

	@Override
	public void update() {
		super.update();
		if (target == null || target.parent == null){
			killAndErase();
			return;
		}
		time += Game.elapsed;
		sync();
		if (!visible) return;
		//숨 쉬듯 은은하게 밝아졌다 어두워짐. 투명해진 영웅에게는 그만큼 흐리게
		float pulse = 0.82f + 0.12f * (float) Math.sin( time * 2.6f );
		alpha( pulse * Math.min( 1f, target.alpha() ) );
	}

	@Override
	public void draw() {
		if (target == null || target.parent == null) return;
		sync();
		if (!visible) return;
		super.draw();
	}
}
