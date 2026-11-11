/*
 * Shattered Pixel Dungeon 모드: 룬 수정 (성소 고유 아이템)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

//성소에서만 나오는 던지는 수정. 깨지면 룬 빛이 터져 3x3의 적에게 마법 피해를 주고 발을 묶음 (영웅은 다치지 않음)
public class RuneCrystal extends Item {

	{
		image = ItemSpriteSheet.RUNE_CRYSTAL;
		stackable = true;
		defaultAction = AC_THROW;
		bones = true;
	}

	@Override
	protected void onThrow( int cell ) {
		if (Dungeon.level.pit[cell]){
			super.onThrow( cell );
			return;
		}
		Sample.INSTANCE.play( Assets.Sounds.SHATTER );
		for (int n : PathFinder.NEIGHBOURS9){
			int c = cell + n;
			if (c < 0 || c >= Dungeon.level.length() || Dungeon.level.solid[c]) continue;
			if (Dungeon.level.heroFOV[c]) CellEmitter.center( c ).burst( RuneMote.FACTORY, n == 0 ? 12 : 5 );
			Char ch = Actor.findChar( c );
			if (ch != null && ch != Dungeon.hero && ch.alignment != Char.Alignment.ALLY){
				ch.damage( Random.NormalIntRange( 20, 32 ), this );
				if (ch.isAlive()) Buff.prolong( ch, Cripple.class, 4f );
			}
		}
	}

	@Override
	public String desc() {
		return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( this, "desc" ) + "\n\n"
				+ com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( this, "category" );
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
		return 30 * quantity;
	}
}
