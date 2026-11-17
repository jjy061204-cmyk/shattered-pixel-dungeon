/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사가 '정령 부여'로 무기에 옮겨 담은 정령: 정령마다 다른 전용 인챈트.
 * 장비를 강화하거나 다른 정령·마법을 부여하면 깃든 정령은 알아서 숲의 완드로 역소환됨
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
			damage = ForestWand.spiritHit( spirit, attacker, defender, damage, 1f );
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
