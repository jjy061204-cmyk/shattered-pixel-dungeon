/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 몽환의 마법: 에테르 점토 전용. 마나 응집도가 너무 높은 점토에 마법을 부여하면 이 마법이나 '꿈의 마법'으로 바뀜
 *  - 확률(+0 25% · +1 40% · +2 50% …)로 에테르 환영이 한 번 더 베어 이번 피해의 절반을 더 주고,
 *    대상을 3턴 동안 어지럽게 함
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Reverie extends Weapon.Enchantment {

	private static final ItemSprite.Glowing LILAC = new ItemSprite.Glowing( 0xC89BFF );

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.buffedLvl() );
		float procChance = (level + 1f) / (level + 4f) * procChanceMultiplier( attacker );
		if (Random.Float() < procChance) {
			float power = Math.max( 1f, procChance );
			int echo = Math.round( damage * 0.5f * power );
			Buff.prolong( defender, Vertigo.class, 3f );
			if (defender.sprite != null) {
				defender.sprite.emitter().burst( Speck.factory( Speck.STAR ), 3 + level );
				defender.sprite.centerEmitter().burst( Speck.factory( Speck.CONFUSION ), 2 );
			}
			return damage + echo;
		}
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return LILAC;
	}
}
