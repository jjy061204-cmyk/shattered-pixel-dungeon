/*
 * Shattered Pixel Dungeon 모드: 현대 세계 돈
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//현대 세계에서 쓰는 돈. 금은방에서 금화 1개를 코인 1개로 바꿔 줌 (반대로도 가능)
//가방 안에 한 칸을 차지하며 개수만큼 쌓임
public class ModernCoin extends Item {

	{
		image = ItemSpriteSheet.MODERN_COIN;
		stackable = true;
		bones = false;
	}

	//영웅이 가진 코인 수
	public static int count(){
		if (Dungeon.hero == null || Dungeon.hero.belongings == null) return 0;
		ModernCoin c = Dungeon.hero.belongings.getItem( ModernCoin.class );
		return c == null ? 0 : c.quantity();
	}

	//코인을 n개 냄. 모자라면 아무것도 하지 않고 false
	public static boolean spend( int n ){
		if (n <= 0) return true;
		if (Dungeon.hero == null) return false;
		ModernCoin c = Dungeon.hero.belongings.getItem( ModernCoin.class );
		if (c == null || c.quantity() < n) return false;
		if (c.quantity() == n) {
			c.detachAll( Dungeon.hero.belongings.backpack );
		} else {
			c.quantity( c.quantity() - n );
		}
		updateQuickslot();
		return true;
	}

	//코인을 n개 받음. 가방이 가득 차면 발밑에 떨어뜨림
	public static void give( int n ){
		if (n <= 0 || Dungeon.hero == null) return;
		ModernCoin c = new ModernCoin();
		c.quantity( n );
		if (!c.collect()) {
			Dungeon.level.drop( c, Dungeon.hero.pos ).sprite.drop();
		}
		updateQuickslot();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	//던전 상인에게 팔면 1코인당 1골드
	@Override
	public int value() {
		return quantity;
	}
}
