/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

//소화기: 던지면 떨어진 곳 주변 2칸의 불을 끄고, 불붙은 것을 진화하고, 적을 4턴 동안 얼림
public class FireExtinguisher extends Item {

	public static final int RADIUS = 2;

	{
		image = ItemSpriteSheet.FIRE_EXTINGUISHER;
		defaultAction = AC_THROW;
		stackable = true;
	}

	@Override
	protected void onThrow( int cell ) {
		//땅에 떨어뜨리지 않고 터뜨림
		Sample.INSTANCE.play( Assets.Sounds.GAS );
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), RADIUS );
		Blob fire = Dungeon.level.blobs.get( Fire.class );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] == Integer.MAX_VALUE) continue;
			if (fire != null) fire.clear( i );
			if (Dungeon.level.heroFOV[i]) {
				CellEmitter.get( i ).burst( Speck.factory( Speck.STEAM ), 4 );
			}
			Char ch = Actor.findChar( i );
			if (ch != null) {
				Buff.detach( ch, Burning.class );
				if (ch.alignment != Char.Alignment.ALLY) {
					Buff.affect( ch, Chill.class, 4f );
				}
			}
		}
		Catalog.countUse( getClass() );
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 25 * quantity;
	}
}
