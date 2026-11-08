/*
 * Shattered Pixel Dungeon 모드: 딸기우유
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

//다른 차원에서 흘러들어온 딸기우유
public class StrawberryMilk extends Food {

	//배고픔 50 해소
	public static final float FOOD_VALUE = 50f;
	//최대 체력의 20% 회복
	public static final float HEAL_FACTOR = 0.2f;
	//충전 3턴
	public static final float RECHARGE_TURNS = 3f;

	{
		image = ItemSpriteSheet.STRAWBERRY_MILK;
		energy = FOOD_VALUE;

		bones = false;
	}

	@Override
	protected void satisfy( Hero hero ) {
		super.satisfy( hero );

		int heal = Math.max(1, Math.round(hero.HT * HEAL_FACTOR));
		heal = Math.min(heal, hero.HT - hero.HP);
		if (heal > 0) {
			hero.HP += heal;
			hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING );
		}

		Buff.affect( hero, Recharging.class, RECHARGE_TURNS );
	}

	//음식 대신 물약 마시는 소리
	@Override
	protected void eatSFX() {
		Sample.INSTANCE.play( Assets.Sounds.DRINK );
	}

	@Override
	public int value() {
		return 15 * quantity;
	}
}
