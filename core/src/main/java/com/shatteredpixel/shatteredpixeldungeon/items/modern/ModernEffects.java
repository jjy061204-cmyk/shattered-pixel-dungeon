/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

//현대 물품이 공통으로 쓰는 효과
public class ModernEffects {

	//최대 체력의 일정 비율만큼 즉시 회복
	public static void heal( Hero hero, float fraction ){
		int amount = Math.max( 1, Math.round( hero.HT * fraction ) );
		amount = Math.min( amount, hero.HT - hero.HP );
		if (amount > 0) {
			hero.HP += amount;
			hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( amount ), FloatingText.HEALING );
		}
	}
}
