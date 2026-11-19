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
		if (anim == throwing || anim == roaring){
			idle();
			return;
		}
		super.onComplete( anim );
	}
}
