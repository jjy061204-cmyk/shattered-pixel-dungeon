/*
 * Shattered Pixel Dungeon 모드: 골렘의 심장 (이차원의 성소 퀘스트 아이템)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//성소의 거대 골렘이 떨어뜨리는 심장. 20층 임프에게 가져가면 보상을 받음
public class GolemHeart extends Item {

	{
		image = ItemSpriteSheet.GOLEM_HEART;
		unique = true;
		bones = false;
	}

	private static final ItemSprite.Glowing VIOLET = new ItemSprite.Glowing( 0xAA88FF, 1.5f );

	@Override
	public ItemSprite.Glowing glowing() {
		return VIOLET;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
