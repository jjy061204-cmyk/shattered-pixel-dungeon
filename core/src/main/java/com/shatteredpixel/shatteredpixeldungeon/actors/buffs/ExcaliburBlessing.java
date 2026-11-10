/*
 * Shattered Pixel Dungeon 모드: 엑스칼리버를 든 자의 가호
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Excalibur;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

//봉인이 풀린 엑스칼리버를 주무기로 들고 있는 동안 계속 붙어 있는 버프.
//정확성 +50%, 방어 0~10 추가, 힘 +2 (실제 계산은 Hero 쪽에서 이 버프가 있는지로 판단)
public class ExcaliburBlessing extends Buff {

	public static final float ACCURACY = 1.5f;
	public static final int ARMOR = 10;
	public static final int STR = 2;

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.EXCALIBUR;
	}

	//영웅이 행동할 때마다 엑스칼리버를 들고 있는지 확인해서 붙이거나 뗌
	public static void update( Hero hero ){
		boolean wielding = hero.belongings.weapon() instanceof Excalibur
				&& !((Excalibur) hero.belongings.weapon()).sealed;
		ExcaliburBlessing b = hero.buff( ExcaliburBlessing.class );
		if (wielding && b == null){
			Buff.affect( hero, ExcaliburBlessing.class );
		} else if (!wielding && b != null){
			b.detach();
		}
	}
}
