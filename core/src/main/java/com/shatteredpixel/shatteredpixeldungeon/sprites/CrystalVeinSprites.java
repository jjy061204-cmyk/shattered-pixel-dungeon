/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 수정 광맥 몹 그림: 원작 동굴 몹이 수정에 잠식된 모습
 *  - 원작 그림의 틀(프레임 순서·동작)은 그대로, 몸의 일부가 원작 채굴 퀘스트 수정 색으로 굳고 결정이 자라 나옴
 *  - 머리·등 위로 자란 결정이 잘리지 않도록 몇몇 몹은 칸을 넓혔음 (좌우는 같게, 위만 늘려서 서 있는 자리는 그대로)
 *     브루트 12x16 → 14x19 · 주술사 12x15 → 14x18 · DM-200/201 21x18 → 21x21
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.noosa.MovieClip.Animation;
import com.watabou.noosa.TextureFilm;

public class CrystalVeinSprites {

	//박쥐: 날개막·날개뼈가 수정으로 굳음
	public static class Bat extends BatSprite {
		public Bat(){
			super();
			texture( Assets.Sprites.CV_BAT );
			play( idle, true );
		}
	}

	//브루트: 투구 뒤쪽이 수정으로 굳고 뒤통수에서 결정이 자람
	public static class Brute extends BruteSprite {
		public Brute(){
			super();
			texture( Assets.Sprites.CV_BRUTE );
			TextureFilm frames = new TextureFilm( texture, 14, 19 );
			idle = new Animation( 2, true );
			idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );
			run = new Animation( 12, true );
			run.frames( frames, 4, 5, 6, 7 );
			attack = new Animation( 12, false );
			attack.frames( frames, 2, 3, 0 );
			die = new Animation( 12, false );
			die.frames( frames, 8, 9, 10 );
			play( idle, true );
		}
	}

	//갑주 브루트: 붉은 갑주가 붉은 수정으로
	public static class ArmoredBrute extends ShieldedSprite {
		public ArmoredBrute(){
			super();
			texture( Assets.Sprites.CV_BRUTE );
			TextureFilm frames = new TextureFilm( texture, 14, 19 );
			idle = new Animation( 2, true );
			idle.frames( frames, 21, 21, 21, 22, 21, 21, 22, 22 );
			run = new Animation( 12, true );
			run.frames( frames, 25, 26, 27, 28 );
			attack = new Animation( 12, false );
			attack.frames( frames, 23, 24 );
			die = new Animation( 12, false );
			die.frames( frames, 29, 30, 31 );
			play( idle, true );
		}
	}

	//주술사: 가면이 수정 가면으로 굳고 가면 위로 같은 색 결정이 솟음
	private static void shamanFrames( ShamanSprite s, int c ){
		s.texture( Assets.Sprites.CV_SHAMAN );
		TextureFilm frames = new TextureFilm( s.texture, 14, 18 );
		s.idle = new Animation( 2, true );
		s.idle.frames( frames, c+0, c+0, c+0, c+1, c+0, c+0, c+1, c+1 );
		s.run = new Animation( 12, true );
		s.run.frames( frames, c+4, c+5, c+6, c+7 );
		s.attack = new Animation( 12, false );
		s.attack.frames( frames, c+2, c+3, c+0 );
		s.zap = s.attack.clone();
		s.die = new Animation( 12, false );
		s.die.frames( frames, c+8, c+9, c+10 );
		s.play( s.idle, true );
	}

	public static class RedShaman extends ShamanSprite.Red {
		public RedShaman(){ super(); shamanFrames( this, 0 ); }
	}
	public static class BlueShaman extends ShamanSprite.Blue {
		public BlueShaman(){ super(); shamanFrames( this, 21 ); }
	}
	public static class PurpleShaman extends ShamanSprite.Purple {
		public PurpleShaman(){ super(); shamanFrames( this, 42 ); }
	}

	//거미: 배가 수정으로 굳고 등에 결정이 돋음
	public static class Spinner extends SpinnerSprite {
		public Spinner(){
			super();
			texture( Assets.Sprites.CV_SPINNER );
			play( idle, true );
		}
	}

	//DM-200: 녹슨 자리마다 수정이 번지고 덮개를 뚫고 푸른 결정 무리가 솟음
	public static class DM200 extends DM200Sprite {
		public DM200(){
			super();
			texture( Assets.Sprites.CV_DM200 );
			TextureFilm frames = new TextureFilm( texture, 21, 21 );
			idle = new Animation( 10, true );
			idle.frames( frames, 0, 1 );
			run = new Animation( 10, true );
			run.frames( frames, 2, 3 );
			attack = new Animation( 15, false );
			attack.frames( frames, 4, 5, 6 );
			zap = new Animation( 15, false );
			zap.frames( frames, 7, 8, 8, 7 );
			die = new Animation( 8, false );
			die.frames( frames, 9, 10, 11 );
			play( idle, true );
		}
	}

	//DM-201: 부식 가스를 뿜는 기계답게 초록 수정
	public static class DM201 extends DM201Sprite {
		public DM201(){
			super();
			texture( Assets.Sprites.CV_DM200 );
			TextureFilm frames = new TextureFilm( texture, 21, 21 );
			int c = 12;
			idle = new Animation( 2, true );
			idle.frames( frames, c+0, c+1 );
			run = idle.clone();
			attack = new Animation( 15, false );
			attack.frames( frames, c+4, c+5, c+6 );
			zap = new Animation( 15, false );
			zap.frames( frames, c+7, c+8, c+8, c+7 );
			die = new Animation( 8, false );
			die.frames( frames, c+9, c+10, c+11 );
			play( idle, true );
		}
	}

	//수정 DM-300: 덮개를 뚫고 솟은 수정 무리, 녹 자리마다 수정 (칸 25x22 → 25x26, 위로만 늘림)
	public static class DM300 extends DM300Sprite {
		public DM300(){
			super();
			texture( Assets.Sprites.CV_DM300 );
			rebuildFrames();
		}

		@Override
		protected int frameHeight() {
			//원작 시트(높이 64)일 때는 22, 수정 시트(높이 52)일 때는 26
			return texture != null && texture.height == 52 ? 26 : 22;
		}

		//독가스 대신 수정빛 탄
		@Override
		public void zap( int cell ) {
			turnTo( ch.pos, cell );
			play( zap );
			MagicMissile.boltFromChar( parent, MagicMissile.FROST, this, cell, new Callback() {
				@Override
				public void call() {
					((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300)ch).onZapComplete();
				}
			} );
			Sample.INSTANCE.play( Assets.Sounds.ZAP );
		}
	}
}
