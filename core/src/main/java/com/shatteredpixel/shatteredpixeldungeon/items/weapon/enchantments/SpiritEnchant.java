/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사가 '정령 부여'로 무기에 옮겨 담은 정령: 정령마다 다른 전용 인챈트.
 * 장비를 강화하거나 다른 정령·마법을 부여하면 깃든 정령은 알아서 숲의 완드로 역소환됨
 * 정령 부여사는 한 장비에 정령을 둘까지 깃들일 수 있고(spirit2), 부여의 효과가 강해짐
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class SpiritEnchant extends Weapon.Enchantment {

	public ForestWand.Spirit spirit = ForestWand.Spirit.EARTH;
	public ForestWand.Spirit spirit2 = null;

	public SpiritEnchant(){}
	public SpiritEnchant( ForestWand.Spirit s ){ spirit = s; }

	//정령 부여사: 확률 ×1.2, 속성 피해 ×1.3
	public static boolean enchanter( Char ch ){
		return ch instanceof Hero && ((Hero) ch).subClass == HeroSubClass.SPIRIT_ENCHANTER;
	}

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.buffedLvl() );
		boolean boost = enchanter( attacker );
		damage = roll( spirit, level, boost, attacker, defender, damage );
		if (spirit2 != null && defender.isAlive()){
			damage = roll( spirit2, level, boost, attacker, defender, damage );
		}
		return damage;
	}

	private int roll( ForestWand.Spirit s, int level, boolean boost, Char attacker, Char defender, int damage ){
		// +0 33%, +2 40%, +5 50% … (강한 정령은 조금 더 자주)
		float chance = (level + 2f) / (level + 6f) * procChanceMultiplier( attacker );
		if (s.strong) chance *= 1.15f;
		if (boost) chance *= 1.2f;
		if (Random.Float() < chance){
			damage = ForestWand.spiritHit( s, attacker, defender, damage, boost ? 1.3f : 1f );
		}
		return damage;
	}

	@Override
	public String name( String weaponName ) {
		if (spirit2 != null) return Messages.get( SpiritEnchant.class, "name2", spirit.title(), spirit2.title(), weaponName );
		return Messages.get( SpiritEnchant.class, "name", spirit.title(), weaponName );
	}

	@Override
	public String desc() {
		if (spirit2 != null){
			return "_" + spirit.title() + "_: " + Messages.get( SpiritEnchant.class, "desc_" + spirit.name().toLowerCase() )
					+ "\n\n_" + spirit2.title() + "_: " + Messages.get( SpiritEnchant.class, "desc_" + spirit2.name().toLowerCase() );
		}
		return Messages.get( SpiritEnchant.class, "desc_" + spirit.name().toLowerCase() );
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return spirit.glowing();
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( "spirit", spirit.name() );
		if (spirit2 != null) bundle.put( "spirit2", spirit2.name() );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		try { spirit = ForestWand.Spirit.valueOf( bundle.getString( "spirit" ) ); } catch (Exception e) { spirit = ForestWand.Spirit.EARTH; }
		spirit2 = null;
		if (bundle.contains( "spirit2" )){
			try { spirit2 = ForestWand.Spirit.valueOf( bundle.getString( "spirit2" ) ); } catch (Exception e) { spirit2 = null; }
		}
	}
}
