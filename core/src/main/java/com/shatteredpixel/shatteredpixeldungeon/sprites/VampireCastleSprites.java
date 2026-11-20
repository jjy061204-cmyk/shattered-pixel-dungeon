/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 몹·장치 그림
 *  - 몹: 원작 그림의 틀을 그대로 쓰고 색만 바꾼 그림 (vc_*.png)
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

	public static class CastleKnight extends GuardSprite {
		public CastleKnight(){
			super();
			texture( Assets.Sprites.VC_CASTLE_KNIGHT );
		}
	}

	public static class VampireBride extends SuccubusSprite {
		public VampireBride(){
			super();
			texture( Assets.Sprites.VC_VAMPIRE_BRIDE );
		}
	}

	public static class BloodThrall extends GhoulSprite {
		public BloodThrall(){
			super();
			texture( Assets.Sprites.VC_BLOOD_THRALL );
		}
	}

	public static class VampireLord extends KingSprite {

		public VampireLord(){
			super();
			texture( Assets.Sprites.VC_VAMPIRE_LORD );
		}

		//피의 화살 (세 갈래 중 하나)
		public void bolt( int cell, com.watabou.utils.Callback onHit ){
			MagicMissile.boltFromChar( parent, MagicMissile.SHAMAN_RED, this, cell, onHit );
		}

		//안개가 된 동안은 흐릿하게
		public void mist( boolean on ){
			alpha( on ? 0.35f : 1f );
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
