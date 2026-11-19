/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 고철 공방의 고철 바리케이드: 기계 잔해를 쌓아 만든 벽. 공격 두 번이면 무너짐 (불에 타지 않음)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.watabou.noosa.audio.Sample;

public class ScrapBarricade extends GnollProp {

	{
		spriteClass = GnollEmpireSprites.Barricade.class;
		HP = HT = 2;
	}

	@Override
	protected boolean flammable() {
		return false;
	}

	@Override
	public boolean interact( Char c ) {
		if (c instanceof Hero){
			showOptions( (Hero) c, description() + "\n\n" + Messages.get( this, "prompt", HP ),
					Messages.get( this, "smash" ), Messages.get( this, "leave" ) );
		}
		return false;
	}

	@Override
	protected void choose( Hero hero, int index ) {
		if (index == 0){
			hero.sprite.operate( pos );
			hero.spendAndNext( hero.attackDelay() );
			damage( 1, hero );
		}
	}

	@Override
	protected void onHit( Object src ) {
		Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 0.8f, 1.2f );
		if (Dungeon.level.heroFOV[pos]) CellEmitter.center( pos ).burst( SparkParticle.FACTORY, 6 );
	}

	@Override
	protected void onBroken( Object cause ) {
		if (Dungeon.level.heroFOV[pos]){
			CellEmitter.get( pos ).burst( Speck.factory( Speck.ROCK ), 8 );
			Sample.INSTANCE.play( Assets.Sounds.ROCKS, 0.7f );
		}
	}
}
