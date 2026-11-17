/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사가 '정령 부여'로 무기에 옮겨 담은 정령: 정령마다 다른 전용 인챈트.
 * 깃든 정령은 강화해도 떨어져 나가지 않으며, 다른 장비로 옮기거나 완드로 거두기 전까지 남음
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class SpiritEnchant extends Weapon.Enchantment {

	public ForestWand.Spirit spirit = ForestWand.Spirit.EARTH;

	public SpiritEnchant(){}
	public SpiritEnchant( ForestWand.Spirit s ){ spirit = s; }

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.buffedLvl() );
		// +0 33%, +2 40%, +5 50% … (강한 정령은 조금 더 자주)
		float chance = (level + 2f) / (level + 6f) * procChanceMultiplier( attacker );
		if (spirit.strong) chance *= 1.15f;
		if (Random.Float() < chance){
			damage = ForestWand.spiritHit( spirit, attacker, defender, damage, level );
		}
		return damage;
	}

	@Override
	public String name( String weaponName ) {
		return Messages.get( SpiritEnchant.class, "name", spirit.title(), weaponName );
	}

	@Override
	public String desc() {
		return Messages.get( SpiritEnchant.class, "desc_" + spirit.name().toLowerCase() );
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return spirit.glowing();
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( "spirit", spirit.name() );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		try { spirit = ForestWand.Spirit.valueOf( bundle.getString( "spirit" ) ); } catch (Exception e) { spirit = ForestWand.Spirit.EARTH; }
	}
}
