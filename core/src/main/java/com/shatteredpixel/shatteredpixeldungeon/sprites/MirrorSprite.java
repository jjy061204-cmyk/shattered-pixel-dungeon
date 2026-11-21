/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;

public class MirrorSprite extends MobSprite {
	
	private static final int FRAME_WIDTH	= 12;
	private static final int FRAME_HEIGHT	= 15;
	
	public MirrorSprite() {
		super();
		
		texture( Dungeon.hero != null ? Dungeon.hero.heroClass.spritesheet() : HeroClass.WARRIOR.spritesheet() );
		updateArmor( 0 );
		idle();
	}
	
	@Override
	public void link( Char ch ) {
		super.link( ch );
		updateArmor();
	}

	@Override
	public void bloodBurstA(PointF from, int damage) {
		//do nothing
	}

	public void updateArmor(){
		updateArmor( ((MirrorImage)ch).armTier );
	}
	
	public void updateArmor( int tier ) {
		TextureFilm film;
		HeroClass cl = Dungeon.hero != null ? Dungeon.hero.heroClass : HeroClass.WARRIOR;
		if (cl.hdSheet() != null){
			film = new TextureFilm( texture, HeroSprite.HD_WIDTH, HeroSprite.HD_HEIGHT );
			float s = HeroSprite.HD_SCALE * cl.hdScale();
			HeroSprite.smooth( texture, s );
			scale.set( s );
		} else {
			film = new TextureFilm( HeroSprite.tiers(), tier, FRAME_WIDTH, FRAME_HEIGHT );
		}
		
		int[][] hd = cl.hdSheet() != null ? HeroSprite.hdAnims( cl ) : null;
		if (hd != null){
			//분신도 본체와 같은 동작 표를 씀
			idle = HeroSprite.hdAnim( film, hd[0], true );
			run = HeroSprite.hdAnim( film, hd[1], true );
			die = new Animation( 20, false );
			die.frames( film, hd[0][1] );
			attack = HeroSprite.hdAnim( film, hd[3], false );
		} else {
			idle = new Animation( 1, true );
			idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );
			
			run = new Animation( 20, true );
			run.frames( film, 2, 3, 4, 5, 6, 7 );
			
			die = new Animation( 20, false );
			die.frames( film, 0 );
			
			attack = new Animation( 15, false );
			attack.frames( film, 13, 14, 15, 0 );
		}
		
		idle();
	}
}
