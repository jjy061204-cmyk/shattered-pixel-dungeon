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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

//백과사전: 아주 무겁고 두꺼운 책. 던지면 맞은 적에게 피해를 주고 2턴 동안 멍하게 만듦.
//책은 떨어진 자리에 남아서 다시 주워 쓸 수 있음.
public class Encyclopedia extends Item {

	{
		image = ItemSpriteSheet.ENCYCLOPEDIA;
		defaultAction = AC_THROW;
		stackable = true;
	}

	public int damageRoll(){
		int depth = Dungeon.scalingDepth();
		return Random.NormalIntRange( 2 + depth / 3, 6 + depth / 2 );
	}

	@Override
	protected void onThrow( int cell ) {
		Char ch = Actor.findChar( cell );
		if (ch != null && ch != Dungeon.hero && ch.alignment != Char.Alignment.ALLY) {
			ch.damage( damageRoll(), this );
			if (ch.isAlive()) Buff.affect( ch, Daze.class, 2f );
			Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG );
		}
		//책은 그 자리에 떨어짐
		super.onThrow( cell );
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
