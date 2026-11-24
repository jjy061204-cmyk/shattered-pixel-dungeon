/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 핏빛 권속: 박쥐 그림을 진홍빛·금빛 눈으로 다시 칠한 것 (sprites/crown_bat.png)
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.watabou.noosa.TextureFilm;

public class CrownBatSprite extends MobSprite {

	public CrownBatSprite() {
		super();

		texture( "sprites/crown_bat.png" );

		TextureFilm frames = new TextureFilm( texture, 15, 15 );

		idle = new Animation( 10, true );
		idle.frames( frames, 0, 1 );

		run = new Animation( 14, true );
		run.frames( frames, 0, 1 );

		attack = new Animation( 14, false );
		attack.frames( frames, 2, 3, 0, 1 );

		die = new Animation( 12, false );
		die.frames( frames, 4, 5, 6 );

		play( idle );
	}
}
