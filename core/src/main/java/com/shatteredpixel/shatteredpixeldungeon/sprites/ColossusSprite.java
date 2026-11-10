/*
 * Shattered Pixel Dungeon 모드: 성소의 거대 골렘 스프라이트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

//sanctuary_colossus.png: 32x32 칸. 0-1 대기, 2-5 걷기, 6-8 내려치기, 9-13 무너짐
public class ColossusSprite extends MobSprite {

	public ColossusSprite() {
		super();
		texture( Assets.Sprites.SANCTUARY_COLOSSUS );
		TextureFilm frames = new TextureFilm( texture, 32, 32 );

		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1 );

		run = new Animation( 6, true );
		run.frames( frames, 2, 3, 4, 5 );

		attack = new Animation( 9, false );
		attack.frames( frames, 6, 7, 8, 8, 0 );

		zap = attack.clone();

		die = new Animation( 8, false );
		die.frames( frames, 9, 10, 11, 12, 13 );

		play( idle );
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}

	@Override
	public int blood() {
		return 0xFF8C8E78;
	}
}
