/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 허수아비: 밀짚모자를 쓴 자루 머리, 낡은 초록 윗옷, 밀짚 손. 바람에 머리가 살짝 흔들림
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class TrainingDummySprite extends MobSprite {

	public TrainingDummySprite() {
		super();

		texture( Assets.Sprites.TRAINING_DUMMY );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 1, 0 );

		run = idle.clone();

		attack = new Animation( 8, false );
		attack.frames( frames, 0 );

		die = new Animation( 8, false );
		die.frames( frames, 2, 3, 4 );

		play( idle );
	}

	@Override
	public int blood() {
		return 0xFFD8BC8A;   //밀짚 부스러기
	}
}
