/*
 * Shattered Pixel Dungeon 모드
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ClerkSprite;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

//현대 세계 층의 점원. 가게 종류에 따라 이름과 설명이 바뀜.
//공격받아 도망가면 자기 가게의 물건만 사라짐 (다른 가게는 그대로).
public class ModernClerk extends Shopkeeper {

	public enum Shop {
		CONVENIENCE, CAFE, PHARMACY, BOOKSTORE, HARDWARE, CLOTHING
	}

	//자기 가게로 보는 범위 (가게 안쪽 크기 기준)
	private static final int SHOP_RADIUS = 4;

	{
		spriteClass = ClerkSprite.class;
	}

	public Shop shop = Shop.CONVENIENCE;

	public ModernClerk(){
		super();
	}

	public ModernClerk( Shop shop ){
		super();
		this.shop = shop;
	}

	@Override
	public String name() {
		return Messages.get(this, "name_" + shop.name().toLowerCase());
	}

	@Override
	public String description() {
		return Messages.get(this, "desc_" + shop.name().toLowerCase());
	}

	@Override
	public void destroy() {
		//다른 가게 물건은 잠시 일반 더미로 바꿔서 보호한 뒤, 끝나면 되돌림
		ArrayList<Heap> otherShops = new ArrayList<>();
		if (Dungeon.level != null) {
			for (Heap heap : Dungeon.level.heaps.valueList()) {
				if (heap.type == Heap.Type.FOR_SALE && Dungeon.level.distance(heap.pos, pos) > SHOP_RADIUS) {
					heap.type = Heap.Type.HEAP;
					otherShops.add(heap);
				}
			}
		}
		super.destroy();
		for (Heap heap : otherShops) {
			heap.type = Heap.Type.FOR_SALE;
		}
	}

	private static final String SHOP = "modern_shop";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SHOP, shop.name() );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( SHOP )) {
			shop = Shop.valueOf( bundle.getString( SHOP ) );
		}
	}
}
