/*
 * Shattered Pixel Dungeon 모드: 엑스칼리버 (6티어)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.GoldSparkle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.particles.Emitter;

//6티어 전설의 검. 봉인된 엑스칼리버(SealedExcalibur)를 풀어야 얻음.
//강화 없이도 5티어 +8 무기 수준 (13-78), 강화 성장은 특대도끼와 같음 (+1/+6), 힘 20
public class Excalibur extends Greatsword {

	{
		image = ItemSpriteSheet.EXCALIBUR;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.85f;

		tier = 6;
		unique = true;
		bones = false;
	}

	@Override
	public int min( int lvl ) {
		return 13 + lvl;
	}

	@Override
	public int max( int lvl ) {
		return 78 + 6 * lvl;
	}

	//힘 요구치는 특대도끼와 같은 20
	@Override
	public int STRReq( int lvl ) {
		return STRReq( 6, lvl );
	}

	@Override
	protected void duelistAbility( Hero hero, Integer target ) {
		int dmgBoost = augment.damageFactor( abilityBoost( buffedLvl() ) );
		Sword.cleaveAbility( hero, target, 1, dmgBoost, this );
	}

	private static int abilityBoost( int lvl ){
		return 15 + 2 * lvl;
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = levelKnown ? abilityBoost( buffedLvl() ) : abilityBoost( 0 );
		if (levelKnown){
			return Messages.get( this, "ability_desc", augment.damageFactor( min() + dmgBoost ), augment.damageFactor( max() + dmgBoost ) );
		} else {
			return Messages.get( this, "typical_ability_desc", min( 0 ) + dmgBoost, max( 0 ) + dmgBoost );
		}
	}

	@Override
	public String upgradeAbilityStat( int level ){
		int dmgBoost = abilityBoost( level );
		return augment.damageFactor( min( level ) + dmgBoost ) + "-" + augment.damageFactor( max( level ) + dmgBoost );
	}

	//은은한 금빛 맥동
	private static final ItemSprite.Glowing GOLD = new ItemSprite.Glowing( 0xFFD860, 2f );

	@Override
	public ItemSprite.Glowing glowing() {
		ItemSprite.Glowing g = super.glowing();
		return g != null ? g : GOLD;
	}

	//바닥에 놓여 있으면 금빛 점이 천천히 떠오름
	@Override
	public Emitter emitter() {
		Emitter e = new Emitter();
		e.fillTarget = true;
		e.pour( GoldSparkle.SHIMMER, 0.18f );
		return e;
	}

	@Override
	public int value() {
		return 1000 * quantity;
	}
}
