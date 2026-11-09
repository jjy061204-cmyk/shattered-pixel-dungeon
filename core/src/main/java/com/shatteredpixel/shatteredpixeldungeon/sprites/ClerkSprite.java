/*
 * Shattered Pixel Dungeon 모드
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

//현대 세계 점원: 상인 그림을 초록 앞치마로 바꾼 버전
public class ClerkSprite extends ShopkeeperSprite {

	public ClerkSprite() {
		super();

		texture( Assets.Sprites.CLERK );
		TextureFilm film = new TextureFilm( texture, 14, 14 );

		idle = new Animation( 10, true );
		idle.frames( film, 1, 1, 1, 1, 1, 0, 0, 0, 0 );

		die = new Animation( 20, false );
		die.frames( film, 0 );

		run = idle.clone();

		attack = idle.clone();

		idle();
	}
}
