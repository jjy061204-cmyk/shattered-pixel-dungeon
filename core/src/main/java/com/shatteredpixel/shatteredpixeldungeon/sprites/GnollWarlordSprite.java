/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 대족장 그림 (gnoll_warlord.png, 24x20 칸 한 줄, 몸은 가운데)
 *  0~1 정지, 2~4 근접 공격, 5~6 투창, 7~10 걷기, 11~12 드릴 회전(돌진 준비), 13~14 함성, 15~17 쓰러짐
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Javelin;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class GnollWarlordSprite extends MobSprite {

	private Animation throwing;
	private Animation revving;
	private Animation roaring;
	private Animation aiming;
	private Animation spinning;

	public GnollWarlordSprite() {
		super();

		texture( Assets.Sprites.GNOLL_WARLORD );

		TextureFilm frames = new TextureFilm( texture, 24, 20 );

		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

		run = new Animation( 12, true );
		run.frames( frames, 7, 8, 9, 10 );

		attack = new Animation( 14, false );
		attack.frames( frames, 2, 3, 4, 0 );

		throwing = new Animation( 12, false );
		throwing.frames( frames, 5, 6, 6, 0 );

		revving = new Animation( 16, true );
		revving.frames( frames, 11, 12 );

		roaring = new Animation( 6, false );
		roaring.frames( frames, 13, 14, 14, 14, 13, 0 );

		//투창 세례를 겨눔: 창을 치켜든 채 멈춰 있음
		aiming = new Animation( 1, true );
		aiming.frames( frames, 5 );

		//드릴 회오리: 휘두르는 동작을 빠르게 두 번
		spinning = new Animation( 20, false );
		spinning.frames( frames, 2, 3, 4, 11, 12, 2, 3, 4, 0 );

		die = new Animation( 8, false );
		die.frames( frames, 15, 16, 17 );

		play( idle );
	}

	//투창: 창이 닿으면 then
	public void throwJavelin( int cell, final Callback then ){
		turnTo( ch.pos, cell );
		play( throwing, true );
		((MissileSprite) parent.recycle( MissileSprite.class )).
				reset( this, cell, new Javelin(), then );
	}

	//투창 여러 자루를 한꺼번에 던짐: 한 자루 닿을 때마다 landed
	public interface Landing {
		void landed( int cell );
	}

	public void aim(){
		play( aiming, true );
	}

	public void throwVolley( int[] cells, final Landing landing ){
		if (cells.length > 0) turnTo( ch.pos, cells[0] );
		play( throwing, true );
		for (final int c : cells){
			((MissileSprite) parent.recycle( MissileSprite.class )).
					reset( this, c, new Javelin(), new Callback() {
						@Override
						public void call() {
							landing.landed( c );
						}
					} );
		}
	}

	public void spin(){
		play( spinning, true );
		emitter().burst( SparkParticle.FACTORY, 12 );
	}

	//돌진 준비: 드릴이 돌며 불꽃이 튐
	public void rev(){
		play( revving, true );
		emitter().burst( SparkParticle.FACTORY, 8 );
	}

	public void roar(){
		play( roaring, true );
		emitter().burst( Speck.factory( Speck.SCREAM ), 3 );
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == throwing || anim == roaring || anim == spinning){
			idle();
			return;
		}
		super.onComplete( anim );
	}
}
