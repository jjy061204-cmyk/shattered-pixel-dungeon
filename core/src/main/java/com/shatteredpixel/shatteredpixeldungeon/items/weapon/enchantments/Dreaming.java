/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 꿈의 마법: 에테르 점토 전용. 마나 응집도가 너무 높은 점토에 마법을 부여하면 이 마법이나 '몽환의 마법'으로 바뀜
 *  - 확률(+0 25% · +1 40% · +2 50% …)로 대상을 얕은 꿈에 빠뜨려 3턴 동안 느리게 하고,
 *    꿈에서 흘러나온 마력으로 이번 피해의 30%만큼 보호막을 얻음
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Dreaming extends Weapon.Enchantment {

	private static final ItemSprite.Glowing DREAM = new ItemSprite.Glowing( 0x8FB8FF );

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.buffedLvl() );
		float procChance = (level + 1f) / (level + 4f) * procChanceMultiplier( attacker );
		if (Random.Float() < procChance) {
			float power = Math.max( 1f, procChance );
			Buff.prolong( defender, Slow.class, 3f );
			int shield = Math.round( damage * 0.3f * power );
			if (shield > 0) {
				Barrier b = Buff.affect( attacker, Barrier.class );
				b.setShield( Math.max( b.shielding(), shield ) );
			}
			if (defender.sprite != null) defender.sprite.centerEmitter().burst( Speck.factory( Speck.NOTE ), 2 );
			if (attacker.sprite != null) attacker.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 3 + level );
		}
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return DREAM;
	}
}
