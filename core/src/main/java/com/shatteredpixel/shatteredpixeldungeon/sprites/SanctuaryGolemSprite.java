/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 석상 골렘 스프라이트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RuneSentinel;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

//sanctuary_golems.png: 16x18 칸, 줄마다 한 종류 (0 석상 병사, 1 석상 수호병, 2 룬 감시석)
//칸 순서: 0-1 대기, 2-5 걷기, 6-8 공격, 9-13 무너짐
public abstract class SanctuaryGolemSprite extends MobSprite {

	protected SanctuaryGolemSprite( int row, float runFps ){
		super();
		texture( Assets.Sprites.SANCTUARY_GOLEMS );
		TextureFilm frames = new TextureFilm( texture, 16, 18 );
		int o = row * 16;

		idle = new Animation( 2, true );
		idle.frames( frames, o, o, o, o + 1 );

		run = new Animation( runFps, true );
		run.frames( frames, o + 2, o + 3, o + 4, o + 5 );

		attack = new Animation( 12, false );
		attack.frames( frames, o + 6, o + 7, o + 8, o );

		zap = attack.clone();

		die = new Animation( 10, false );
		die.frames( frames, o + 9, o + 10, o + 11, o + 12, o + 13 );

		play( idle );
	}

	@Override
	public int blood() {
		return 0xFF9C98AC;   //돌가루
	}

	public static class Soldier extends SanctuaryGolemSprite {
		public Soldier(){ super( 0, 10 ); }
	}

	public static class Warden extends SanctuaryGolemSprite {
		public Warden(){ super( 1, 7 ); }
	}

	//룬 감시석: 눈에서 룬 광탄을 쏨
	public static class Sentinel extends SanctuaryGolemSprite {
		public Sentinel(){ super( 2, 8 ); }

		@Override
		public void zap( int cell ) {
			super.zap( cell );
			MagicMissile.boltFromChar( parent, MagicMissile.SHAMAN_BLUE, this, cell, new Callback() {
				@Override
				public void call() {
					((RuneSentinel) ch).onZapComplete();
				}
			} );
			Sample.INSTANCE.play( Assets.Sounds.ZAP );
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == zap) {
				idle();
			}
			super.onComplete( anim );
		}
	}
}
