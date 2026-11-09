/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;

//백과사전: 가방 속 미감정 아이템 하나를 찾아보고 감정함
public class Encyclopedia extends ModernConsumable {

	{
		image = ItemSpriteSheet.ENCYCLOPEDIA;
	}

	@Override
	public void execute( final Hero hero, String action ) {
		if (action.equals( AC_USE )) {
			GameScene.selectItem( new WndBag.ItemSelector() {
				@Override
				public String textPrompt() {
					return Messages.get( Encyclopedia.class, "prompt" );
				}

				@Override
				public boolean itemSelectable( Item item ) {
					return !item.isIdentified();
				}

				@Override
				public void onSelect( Item item ) {
					if (item == null) return;
					Encyclopedia.this.detach( hero.belongings.backpack );
					item.identify();
					Sample.INSTANCE.play( Assets.Sounds.READ );
					GLog.i( Messages.get( Encyclopedia.class, "found", Messages.titleCase( item.title() ) ) );
					Catalog.countUse( Encyclopedia.class );
					hero.spendAndNext( TIME_TO_USE );
				}
			} );
			return;
		}
		super.execute( hero, action );
	}

	@Override
	protected void apply( Hero hero ) {
		//선택창에서 처리
	}

	@Override
	public int value() {
		return 30 * quantity;
	}
}
