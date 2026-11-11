/*
 * Shattered Pixel Dungeon 모드: 룬 성갑의 문장 재충전
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;

//문장이 다시 힘을 모으는 동안 붙어 있는 버프 (아이콘 없음). 매 턴 남은 턴을 줄이고 퀵슬롯 숫자를 새로 고침
public class RuneAegisCooldown extends Buff {

	public static final float DURATION = 250f;

	public int left = 0;

	{
		revivePersists = true;
	}

	@Override
	public boolean act() {
		left--;
		if (left <= 0){
			detach();
		}
		Item.updateQuickslot();
		spend( TICK );
		return true;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "left", left );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		left = bundle.getInt( "left" );
	}
}
