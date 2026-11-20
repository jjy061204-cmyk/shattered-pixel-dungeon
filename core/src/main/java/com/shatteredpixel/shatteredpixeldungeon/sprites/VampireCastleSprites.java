/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 몹·장치 그림
 *  - 광산 생물이 감염된 몹(구울 놀·피의 사제·혈사 거미): 원작 그림의 틀에 색만 바꾼 그림
 *  - 성 전용 몹(호위 기사·신부·피의 하수인·흡혈 군주): 새로 찍은 그림과 동작 (vc_*.png)
 *  - 장치: vc_props.png (16x16, 한 줄에 16칸)
 *     0 촛대(꺼짐) 1~3 촛대(켜짐) · 4 화로(꺼짐) 5~7 화로(켜짐) · 8 관 9 열린 관 10 부서진 관
 *     11~12 샹들리에 13 떨어진 샹들리에 · 14 성배 제단 15 빈 제단 · 16~17 군주의 관 · 18~19 달빛 창 · 20 옥좌
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CastleLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Chandelier;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GrailAltar;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Halo;
import com.watabou.noosa.Image;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class VampireCastleSprites {

	// ── 몹 (색만 바꾼 원작 그림) ──

	public static class GhoulGnoll extends GnollSprite {

		private Animation crumple;

		public GhoulGnoll(){
			super();
			texture( Assets.Sprites.VC_GHOUL_GNOLL );
			TextureFilm frames = new TextureFilm( texture, 12, 15 );
			crumple = new Animation( 12, false );
			crumple.frames( frames, 8, 9, 10 );
		}

		//쓰러졌지만 아직 죽지 않음 (사라지지 않고 바닥에 남음)
		public void crumple(){
			hideEmo();
			play( crumple );
		}

		@Override
		public void die() {
			if (curAnim == crumple){
				die.frames[0] = die.frames[1] = die.frames[2];
			}
			super.die();
		}
	}

	public static class BloodPriest extends ShamanSprite.Red {
		public BloodPriest(){
			super();
			texture( Assets.Sprites.VC_BLOOD_PRIEST );
		}
	}

	public static class BloodSpider extends SpinnerSprite {
		public BloodSpider(){
			super();
			texture( Assets.Sprites.VC_BLOOD_SPIDER );
		}
	}

	// ── 성 전용 몹 (새로 찍은 그림과 동작) ──

	//성의 호위 기사 16x16: 숨 고르기 2 · 걷기 4 · 내리치기 3 · 방패 막기 3 · 쓰러짐 5
	public static class CastleKnight extends MobSprite {

		private Animation guard;

		public CastleKnight(){
			super();
			texture( Assets.Sprites.VC_CASTLE_KNIGHT );
			TextureFilm frames = new TextureFilm( texture, 16, 16 );

			idle = new Animation( 2, true );
			idle.frames( frames, 0, 0, 0, 1, 0, 0 );
			run = new Animation( 9, true );
			run.frames( frames, 2, 3, 4, 5 );
			attack = new Animation( 12, false );
			attack.frames( frames, 6, 7, 8, 0 );
			guard = new Animation( 14, false );
			guard.frames( frames, 9, 10, 11 );
			die = new Animation( 10, false );
			die.frames( frames, 12, 13, 14, 15, 16 );

			play( idle );
		}

		//방패로 막음
		public void block(){
			if (curAnim != die) play( guard, true );
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == guard){
				idle();
				return;
			}
			super.onComplete( anim );
		}
	}

	//흡혈귀 신부 12x16: 떠 있기 4 · 미끄러지기 4 · 덮치기 3 · 입맞춤(매혹) 4 · 박쥐로 흩어짐 5
	public static class VampireBride extends MobSprite {

		private Animation kiss;

		public VampireBride(){
			super();
			texture( Assets.Sprites.VC_VAMPIRE_BRIDE );
			TextureFilm frames = new TextureFilm( texture, 12, 16 );

			idle = new Animation( 5, true );
			idle.frames( frames, 0, 1, 2, 3 );
			run = new Animation( 10, true );
			run.frames( frames, 4, 5, 6, 7 );
			attack = new Animation( 12, false );
			attack.frames( frames, 8, 9, 10 );
			kiss = new Animation( 10, false );
			kiss.frames( frames, 11, 12, 13, 14 );
			die = new Animation( 10, false );
			die.frames( frames, 15, 16, 17, 18, 19 );

			play( idle );
		}

		//매혹을 걸 때 입맞춤을 날림
		public void charm(){
			if (curAnim == die) return;
			play( kiss, true );
			emitter().burst( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(
					com.shatteredpixel.shatteredpixeldungeon.effects.Speck.HEART ), 3 );
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == kiss){
				idle();
				return;
			}
			super.onComplete( anim );
		}

		@Override
		public void die() {
			super.die();
			emitter().burst( com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle.UP, 8 );
		}
	}

	//피의 하수인 12x14: 숨 고르기 3 · 비틀걸음 4 · 휘두르기 3 · 촛불 끄기 3 · 먼지로 무너짐 4
	public static class BloodThrall extends MobSprite {

		public BloodThrall(){
			super();
			texture( Assets.Sprites.VC_BLOOD_THRALL );
			TextureFilm frames = new TextureFilm( texture, 12, 14 );

			idle = new Animation( 3, true );
			idle.frames( frames, 0, 0, 1, 0, 2 );
			run = new Animation( 9, true );
			run.frames( frames, 3, 4, 5, 6 );
			attack = new Animation( 12, false );
			attack.frames( frames, 7, 8, 9 );
			operate = new Animation( 8, false );
			operate.frames( frames, 10, 11, 12 );
			die = new Animation( 10, false );
			die.frames( frames, 13, 14, 15, 16 );

			play( idle );
		}
	}

	//흡혈 군주 16x18: 망토 자락 4 · 미끄러지기 4 · 할퀴기 4 · 피의 화살 3 · 안개로 흩어짐 4 · 안개 4 · 재가 됨 6
	public static class VampireLord extends MobSprite {

		private Animation stand, mistIn, mistOut, mistIdle;
		private boolean misted = false;

		public VampireLord(){
			super();
			texture( Assets.Sprites.VC_VAMPIRE_LORD );
			TextureFilm frames = new TextureFilm( texture, 16, 18 );

			stand = new Animation( 3, true );
			stand.frames( frames, 0, 1, 0, 3, 0, 2 );
			idle = stand;
			run = new Animation( 9, true );
			run.frames( frames, 4, 5, 6, 7 );
			attack = new Animation( 14, false );
			attack.frames( frames, 8, 9, 10, 11 );
			zap = new Animation( 12, false );
			zap.frames( frames, 12, 13, 14 );
			mistIn = new Animation( 10, false );
			mistIn.frames( frames, 15, 16, 17, 18 );
			mistOut = new Animation( 10, false );
			mistOut.frames( frames, 18, 17, 16, 15 );
			mistIdle = new Animation( 6, true );
			mistIdle.frames( frames, 19, 20, 21, 22 );
			die = new Animation( 8, false );
			die.frames( frames, 23, 24, 25, 26, 27, 28 );

			play( idle );
		}

		//피의 화살 (세 갈래 중 하나)
		public void bolt( int cell, com.watabou.utils.Callback onHit ){
			MagicMissile.boltFromChar( parent, MagicMissile.SHAMAN_RED, this, cell, onHit );
		}

		public void cast( int cell ){
			turnTo( ch.pos, cell );
			play( zap, true );
		}

		//안개가 됨 / 안개에서 다시 모습을 드러냄
		public void mist( boolean on ){
			if (on == misted) return;
			misted = on;
			if (on){
				idle = mistIdle;
				play( mistIn, true );
			} else {
				idle = stand;
				play( mistOut, true );
			}
		}

		//불러올 때 (애니메이션 없이 바로)
		public void setMisted( boolean on ){
			misted = on;
			idle = on ? mistIdle : stand;
			play( idle, true );
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == mistIn || anim == mistOut || anim == zap){
				idle();
				return;
			}
			super.onComplete( anim );
		}
	}

	// ── 장치 ──

	private static abstract class Prop extends MobSprite {

		protected TextureFilm frames;

		public Prop(){
			super();
			texture( Assets.Sprites.VC_PROPS );
			frames = new TextureFilm( texture, 16, 16 );
		}

		protected Animation still( int f ){
			Animation a = new Animation( 1, true );
			a.frames( frames, f );
			return a;
		}
	}

	//촛대·화로: 켜져 있으면 불꽃이 일렁이고 둘레가 은은하게 밝음
	public static class Light extends Prop {

		private Animation off, on;
		private Halo glow;
		protected boolean shownLit = false;

		public Light(){
			super();
			setKind( false );
		}

		private void setKind( boolean brazier ){
			int b = brazier ? 4 : 0;
			off = still( b );
			on = new Animation( 6, true );
			on.frames( frames, b+1, b+2, b+3, b+2 );
			idle = off;
			run = idle.clone();
			attack = idle.clone();
			die = new Animation( 10, false );
			die.frames( frames, b );
			play( idle );
		}

		@Override
		public void link( Char ch ) {
			if (ch instanceof CastleLight && !(ch instanceof Chandelier)){
				setKind( ((CastleLight) ch).brazier );
			}
			super.link( ch );
			refresh();
		}

		public void refresh(){
			boolean lit = ch instanceof CastleLight && ((CastleLight) ch).lit;
			shownLit = lit;
			idle = lit ? on : off;
			play( idle, true );
		}

		@Override
		public void update() {
			super.update();
			if (ch instanceof CastleLight && ((CastleLight) ch).lit != shownLit){
				refresh();
			}
			if (parent != null && ch instanceof CastleLight){
				if (glow == null){
					int r = ((CastleLight) ch).radius();
					glow = new Halo( (r + 0.5f) * DungeonTilemap.SIZE, 0xFFB060, 0.16f );
					parent.addToBack( glow );
				}
				glow.point( x + width / 2f, y + height / 2f );
				glow.visible = visible && shownLit && ch.isAlive();
			}
		}

		@Override
		public void kill() {
			super.kill();
			if (glow != null) glow.killAndErase();
		}
	}

	public static class Coffin extends Prop {

		private Animation open;

		public Coffin(){
			super();
			idle = still( 8 );
			run = idle.clone();
			attack = idle.clone();
			open = new Animation( 3, false );
			open.frames( frames, 9, 9, 9, 8 );
			die = new Animation( 10, false );
			die.frames( frames, 10 );
			play( idle );
		}

		//하수인이 기어 나옴
		public void creak(){
			play( open, true );
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == open){
				play( idle );
				return;
			}
			super.onComplete( anim );
		}
	}

	public static class ChandelierSprite extends Light {
		public ChandelierSprite(){
			super();
		}

		@Override
		public void refresh() {
			shownLit = ch instanceof CastleLight && ((CastleLight) ch).lit;
			idle = new Animation( 4, true );
			idle.frames( frames, 11, 12 );
			die = new Animation( 10, false );
			die.frames( frames, 13 );
			play( idle, true );
		}
	}

	public static class Grail extends Prop {
		public Grail(){
			super();
			idle = still( 14 );
			run = idle.clone();
			attack = idle.clone();
			die = new Animation( 10, false );
			die.frames( frames, 15 );
			play( idle );
		}

		@Override
		public void link( Char ch ) {
			super.link( ch );
			if (ch instanceof GrailAltar && ((GrailAltar) ch).used){
				idle = still( 15 );
				play( idle, true );
			}
		}

		public void drained(){
			idle = still( 15 );
			play( idle, true );
		}
	}

	//레벨 장식용 그림 한 칸 (군주의 관·달빛 창·옥좌)
	public static Image tile( int frame ){
		Image img = new Image( Assets.Sprites.VC_PROPS );
		TextureFilm f = new TextureFilm( img.texture, 16, 16 );
		img.frame( f.get( frame ) );
		return img;
	}
}
