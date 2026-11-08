package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//검(Sword)을 그대로 물려받고, 최대 공격력만 높인 새 무기
public class MyBlade extends Sword {

	{
		image = ItemSpriteSheet.SWORD; //그림은 일단 검 그림을 재사용
		tier = 3;
	}

	@Override
	public int max(int lvl) {
		return 25 + lvl * 5; //원래 검은 20 + lvl * 4
	}
}
