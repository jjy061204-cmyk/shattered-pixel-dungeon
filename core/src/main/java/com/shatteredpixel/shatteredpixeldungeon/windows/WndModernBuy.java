/*
 * Shattered Pixel Dungeon 모드: 현대 세계 가게 결제 창
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.ModernClerk;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.modern.ModernCoin;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.audio.Sample;

//상품 하나를 눌렀을 때: 설명, 개수 고르기(- / + / 최대), 합계, 결제
public class WndModernBuy extends Window {

	private static final int WIDTH = 120;

	private final ModernClerk clerk;
	private final int index;
	private final WndModernShop owner;

	private int amount = 1;

	private RenderedTextBlock amountText;
	private RenderedTextBlock totalText;
	private RedButton minus, plus, max, pay;

	public WndModernBuy( ModernClerk clerk, int index, WndModernShop owner ){
		super();
		this.clerk = clerk;
		this.index = index;
		this.owner = owner;

		Item item = clerk.item( index );

		IconTitle title = new IconTitle( new ItemSprite( item ), Messages.titleCase( item.title() ) );
		title.setRect( 0, 0, WIDTH, 0 );
		add( title );
		float pos = title.bottom() + 3;

		RenderedTextBlock info = PixelScene.renderTextBlock( item.info(), 6 );
		info.maxWidth( WIDTH );
		info.setPos( 0, pos );
		add( info );
		pos = info.bottom() + 4;

		String deal = clerk.dealInfo( index );
		if (!deal.isEmpty()){
			RenderedTextBlock d = PixelScene.renderTextBlock( deal, 6 );
			d.hardlight( 0xFF6060 );
			d.maxWidth( WIDTH );
			d.setPos( 0, pos );
			add( d );
			pos = d.bottom() + 4;
		}

		ColorBlock line = new ColorBlock( WIDTH, 1, 0xFF8A7A6A );
		line.y = pos;
		add( line );
		pos += 4;

		//개수 고르기
		minus = new RedButton( "-" ){
			@Override
			protected void onClick() { setAmount( amount - 1 ); }
		};
		minus.setRect( 0, pos, 20, 16 );
		add( minus );

		amountText = PixelScene.renderTextBlock( 8 );
		add( amountText );

		plus = new RedButton( "+" ){
			@Override
			protected void onClick() { setAmount( amount + 1 ); }
		};
		plus.setRect( 66, pos, 20, 16 );
		add( plus );

		max = new RedButton( clerk.txt( "max" ) ){
			@Override
			protected void onClick() { setAmount( WndModernBuy.this.clerk.maxBuy( WndModernBuy.this.index ) ); }
		};
		max.setRect( 88, pos, WIDTH - 88, 16 );
		add( max );
		pos += 18;

		totalText = PixelScene.renderTextBlock( 6 );
		totalText.setPos( 0, pos );
		add( totalText );
		pos += 18;

		pay = new RedButton( "" ){
			@Override
			protected void onClick() {
				if (WndModernBuy.this.clerk.buy( WndModernBuy.this.index, amount )){
					hide();
					WndModernBuy.this.owner.refresh();
				}
			}
		};
		pay.icon( new ItemSprite( com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.MODERN_COIN ) );
		pay.setRect( 0, pos, WIDTH * 2 / 3 - 1, 18 );
		add( pay );

		RedButton cancel = new RedButton( clerk.txt( "cancel" ) ){
			@Override
			protected void onClick() { hide(); }
		};
		cancel.setRect( WIDTH * 2 / 3 + 1, pos, WIDTH - WIDTH * 2 / 3 - 1, 18 );
		add( cancel );
		pos += 18;

		resize( WIDTH, (int)Math.ceil( pos ) );
		this.amountPos = minus.top();
		setAmount( 1 );
	}

	private float amountPos;

	private void setAmount( int n ){
		int stock = clerk.count( index );
		amount = Math.max( 1, Math.min( n, Math.max( 1, stock ) ) );
		boolean oneplus = clerk.deal( index ) == ModernClerk.DEAL_ONE_PLUS_ONE;

		amountText.text( clerk.txt( oneplus ? "amount_1p1" : "amount", amount, amount * 2 ) );
		amountText.setPos( 21 + (44 - amountText.width()) / 2f, amountPos + (16 - amountText.height()) / 2f );
		PixelScene.align( amountText );

		int total = clerk.price( index ) * amount;
		int have = ModernCoin.count();
		totalText.text( clerk.txt( "total_n", total, have, stock ) );
		totalText.hardlight( total > have ? 0xFF6060 : 0xFFFFFF );
		totalText.maxWidth( WIDTH );

		pay.text( clerk.txt( "pay", total ) );
		pay.enable( total <= have && stock > 0 );
		minus.enable( amount > 1 );
		plus.enable( amount < stock );
		max.enable( clerk.maxBuy( index ) > amount );
		Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.5f );
	}
}
