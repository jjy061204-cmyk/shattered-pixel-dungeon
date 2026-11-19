/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 몹과 장치 그림
 *  - gnoll_empire.png: 12x16 칸, 줄마다 한 몹 (0 정지 0~1, 2~3 공격, 4~7 걷기, 8~10 쓰러짐)
 *      0 척후병, 1 투척병, 2 고철 갑주 놀, 3 백부장, 4 문지기
 *  - gnoll_empire_props.png: 16x16 칸
 *      0 경보 북(0 정지, 1~2 울림, 3~5 부서짐), 1 포로 우리(0~1 정지, 2~4 부서짐)
 *      2 고철 바리케이드(0 정지, 1 금 감, 2~4 무너짐), 3 전투 깃발(0 접힘, 1~3 펄럭임, 4~6 쓰러짐)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Javelin;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class GnollEmpireSprites {

	private static final int ROW = 16; //한 줄에 칸 16개

	//놀 몹 공통: row번째 줄
	private static abstract class EmpireGnoll extends MobSprite {

		protected abstract int row();

		public EmpireGnoll(){
			super();
			texture( Assets.Sprites.GNOLL_EMPIRE );
			TextureFilm frames = new TextureFilm( texture, 12, 16 );
			int c = row() * ROW;

			idle = new MovieClip.Animation( 2, true );
			idle.frames( frames, c, c, c, c+1, c, c, c+1, c+1 );

			run = new MovieClip.Animation( runFps(), true );
			run.frames( frames, c+4, c+5, c+6, c+7 );

			attack = new MovieClip.Animation( 12, false );
			attack.frames( frames, c+2, c+3, c );

			die = new MovieClip.Animation( 12, false );
			die.frames( frames, c+8, c+9, c+10 );

			play( idle );
		}

		protected int runFps(){
			return 12;
		}
	}

	public static class Scout extends EmpireGnoll {
		@Override protected int row() { return 0; }
		@Override protected int runFps() { return 16; }
	}

	public static class Javelineer extends EmpireGnoll {
		@Override protected int row() { return 1; }

		private Animation cast;

		public Javelineer(){
			super();
			cast = attack.clone();
		}

		//멀리 있는 적에게는 창을 던짐 (창이 닿을 때 공격이 끝남)
		@Override
		public void attack( int cell ) {
			if (!Dungeon.level.adjacent( cell, ch.pos )){
				((MissileSprite) parent.recycle( MissileSprite.class )).
						reset( this, cell, new Javelin(), new Callback() {
							@Override
							public void call() {
								ch.onAttackComplete();
							}
						} );
				play( cast );
				turnTo( ch.pos, cell );
			} else {
				super.attack( cell );
			}
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == cast){
				idle();
				return;
			}
			super.onComplete( anim );
		}
	}

	public static class ScrapGnoll extends EmpireGnoll {
		@Override protected int row() { return 2; }
		@Override protected int runFps() { return 9; }
	}

	public static class Centurion extends EmpireGnoll {
		@Override protected int row() { return 3; }
	}

	public static class Gatekeeper extends EmpireGnoll {
		@Override protected int row() { return 4; }
	}

	// ── 장치 ──

	private static abstract class Prop extends MobSprite {

		protected TextureFilm frames;

		public Prop(){
			super();
			texture( Assets.Sprites.GNOLL_EMPIRE_PROPS );
			frames = new TextureFilm( texture, 16, 16 );
		}

		protected int f( int row, int col ){
			return row * 8 + col;
		}
	}

	public static class Drum extends Prop {

		private Animation beatAnim;

		public Drum(){
			super();
			idle = new Animation( 1, true );
			idle.frames( frames, f(0,0) );
			run = idle.clone();
			attack = idle.clone();
			beatAnim = new Animation( 14, false );
			beatAnim.frames( frames, f(0,1), f(0,2), f(0,1), f(0,0) );
			die = new Animation( 10, false );
			die.frames( frames, f(0,3), f(0,4), f(0,5) );
			play( idle );
		}

		public void beat(){
			play( beatAnim, true );
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == beatAnim){
				play( idle );
				return;
			}
			super.onComplete( anim );
		}
	}

	public static class Cage extends Prop {
		public Cage(){
			super();
			idle = new Animation( 1, true );
			idle.frames( frames, f(1,0), f(1,0), f(1,0), f(1,1) );
			run = idle.clone();
			attack = idle.clone();
			die = new Animation( 10, false );
			die.frames( frames, f(1,2), f(1,3), f(1,4) );
			play( idle );
		}
	}

	public static class Barricade extends Prop {
		public Barricade(){
			super();
			idle = new Animation( 1, true );
			idle.frames( frames, f(2,0) );
			run = idle.clone();
			attack = idle.clone();
			die = new Animation( 10, false );
			die.frames( frames, f(2,2), f(2,3), f(2,4) );
			play( idle );
		}

		@Override
		public void flash() {
			super.flash();
			//한 번 맞으면 금 간 모습
			if (ch != null && ch.HP < ch.HT && ch.isAlive()){
				idle = new Animation( 1, true );
				idle.frames( frames, f(2,1) );
				play( idle );
			}
		}
	}

	public static class Banner extends Prop {

		private Animation waving;

		public Banner(){
			super();
			idle = new Animation( 1, true );
			idle.frames( frames, f(3,0) );
			waving = new Animation( 6, true );
			waving.frames( frames, f(3,1), f(3,2), f(3,3), f(3,2) );
			run = idle.clone();
			attack = idle.clone();
			die = new Animation( 10, false );
			die.frames( frames, f(3,4), f(3,5), f(3,6) );
			play( idle );
		}

		public void activate(){
			idle = waving;
			play( idle );
		}
	}
}
