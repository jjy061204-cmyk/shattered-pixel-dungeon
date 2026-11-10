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
//(실제 계산은 Hero·Excalibur 쪽에서 이 버프가 있는지로 판단)
public class ExcaliburBlessing extends Buff {

	public static final float ACCURACY = 1.5f;       //정확성 +50%
	public static final float DAMAGE_TAKEN = 0.8f;   //받는 모든 피해 20% 감소
	public static final float HT_MULTI = 1.2f;       //최대 체력 +20%
	public static final float MAGIC_DMG = 0.1f;      //공격 시 공격력의 10%만큼 마법 피해 추가
	//+ 원소의 반지와 같은 종류의 해로운 효과 30% 감소 (Excalibur.resist)

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
			hero.updateHT( true );     //늘어난 최대 체력만큼 체력도 채움
		} else if (!wielding && b != null){
			b.detach();
			hero.updateHT( false );
		}
	}
}
