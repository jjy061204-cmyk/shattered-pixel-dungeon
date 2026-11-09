/*
 * Shattered Pixel Dungeon 모드: 현대 세계 가게 화면
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
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.CurrencyIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.ModernDigits;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//현대 세계 가게 전용 화면
//- 위: 가게 색 머리띠와 줄무늬 차양, 가진 코인
//- 분류 탭 (전체 / 음료 / 식품 ...)
//- 상품 카드: 그림, 행사 딱지, 가격표, 남은 개수, 품절 표시
//- 카드를 누르면 개수를 골라 결제하는 창(WndModernBuy)
public class WndModernShop extends Window {

	public static final int WIDTH  = 128;
	private static final int COLS  = 4;
	private static final int CARD_W = 29;
	private static final int CARD_H = 40;
	private static final int GAP   = 4;

	private final ModernClerk clerk;
	private final String cat;
	private final int page;

	public WndModernShop( final ModernClerk clerk, String cat, int page ){
		super();
		this.clerk = clerk;
		this.cat = cat;

		int theme = clerk.themeColor();

		//==== 머리띠 ====
		ColorBlock bar = new ColorBlock( WIDTH, 16, 0xFF000000 | theme );
		add( bar );

		RenderedTextBlock title = PixelScene.renderTextBlock( clerk.shopName(), 9 );
		title.hardlight( 0xFFFFFF );
		title.setPos( 4, (16 - title.height()) / 2f );
		add( title );

		//가진 코인
		Image coinIcon = new Image( Assets.Sprites.MODERN_UI, 32, 16, 7, 7 );
		ModernDigits coins = new ModernDigits( ModernDigits.PRICE );
		coins.text( Integer.toString( ModernCoin.count() ), 0xFFFFFF );
		coins.setPos( WIDTH - 3 - coins.width(), 5 );
		coinIcon.x = coins.left() - 9;
		coinIcon.y = (16 - 7) / 2f;
		add( coinIcon );
		add( coins );

		//줄무늬 차양 (가게 색으로 물들임)
		Image awning = new Image( Assets.Sprites.MODERN_UI, 0, 0, 128, 12 );
		awning.hardlight( mixWhite( theme, 0.15f ) );
		awning.y = 16;
		add( awning );

		float pos = 30;

		//==== 분류 탭 ====
		ArrayList<String> tabs = new ArrayList<>();
		tabs.add( null );
		tabs.addAll( clerk.categories() );
		float tx = 0;
		for (final String t : tabs){
			boolean sel = (t == null && cat == null) || (t != null && t.equals( cat ));
			Tab tab = new Tab( clerk.catName( t ), sel, theme ){
				@Override
				protected void onClick() {
					Sample.INSTANCE.play( Assets.Sounds.CLICK );
					reopen( t, 0 );
				}
			};
			if (tx + tab.width() > WIDTH){
				tx = 0;
				pos += tab.height() + 2;
			}
			tab.setPos( tx, pos );
			add( tab );
			tx += tab.width() + 2;
		}
		pos += 14;

		//==== 상품 카드 ====
		ArrayList<Integer> shown = new ArrayList<>();
		for (int i = 0; i < clerk.size(); i++){
			if (cat == null || cat.equals( clerk.cat( i ) )) shown.add( i );
		}
		int rows = (int)Math.max( 2, Math.min( 3, (PixelScene.uiCamera.height - 110) / (CARD_H + GAP) ) );
		int perPage = rows * COLS;
		int pages = Math.max( 1, (shown.size() + perPage - 1) / perPage );
		this.page = Math.max( 0, Math.min( page, pages - 1 ) );

		int start = this.page * perPage;
		int usedRows = 0;
		for (int k = start; k < Math.min( shown.size(), start + perPage ); k++){
			int n = k - start;
			int c = n % COLS, r = n / COLS;
			usedRows = r + 1;
			Card card = new Card( shown.get( k ) );
			card.setRect( c * (CARD_W + GAP), pos + r * (CARD_H + GAP), CARD_W, CARD_H );
			add( card );
		}
		if (shown.isEmpty()){
			RenderedTextBlock empty = PixelScene.renderTextBlock( clerk.txt( "sold_out" ), 6 );
			empty.hardlight( 0x999999 );
			empty.setPos( (WIDTH - empty.width()) / 2f, pos + 8 );
			add( empty );
			usedRows = 1;
		}
		pos += Math.max( usedRows, 1 ) * (CARD_H + GAP);

		//==== 행사 안내 ====
		String promo = clerk.promo();
		if (!promo.isEmpty()){
			RenderedTextBlock pt = PixelScene.renderTextBlock( promo, 6 );
			pt.maxWidth( WIDTH );
			pt.setPos( 0, pos );
			add( pt );
			pos = pt.bottom() + 3;
		}

		//==== 아래쪽 단추 ====
		final int fp = this.page;
		if (pages > 1){
			RedButton prev = new RedButton( "<" ){
				@Override
				protected void onClick() { reopen( WndModernShop.this.cat, fp - 1 ); }
			};
			prev.enable( fp > 0 );
			prev.setRect( 0, pos, 20, 16 );
			add( prev );

			ModernDigits pg = new ModernDigits( ModernDigits.PRICE );
			pg.text( (fp + 1) + "/" + pages, 0xFFFFFF );
			pg.setPos( 22 + (16 - pg.width()) / 2f, pos + 5 );
			add( pg );

			RedButton next = new RedButton( ">" ){
				@Override
				protected void onClick() { reopen( WndModernShop.this.cat, fp + 1 ); }
			};
			next.enable( fp < pages - 1 );
			next.setRect( 40, pos, 20, 16 );
			add( next );
		}
		RedButton close = new RedButton( clerk.txt( "close" ) ){
			@Override
			protected void onClick() { hide(); }
		};
		close.setRect( WIDTH - 44, pos, 44, 16 );
		add( close );
		pos += 16;

		resize( WIDTH, (int)Math.ceil( pos ) );
		CurrencyIndicator.showGold = true;
	}

	private static int mixWhite( int color, float t ){
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
		r += (int)((255 - r) * t); g += (int)((255 - g) * t); b += (int)((255 - b) * t);
		return (r << 16) | (g << 8) | b;
	}

	private void reopen( String cat, int page ){
		hide();
		GameScene.show( new WndModernShop( clerk, cat, page ) );
	}

	//결제 뒤 다시 그리기
	public void refresh(){
		reopen( cat, page );
	}

	@Override
	public void hide() {
		super.hide();
		CurrencyIndicator.showGold = false;
	}

	//==== 분류 탭 ====
	private static class Tab extends Button {
		private ColorBlock bg;
		private RenderedTextBlock label;

		Tab( String text, boolean selected, int theme ){
			super();
			bg = new ColorBlock( 1, 1, selected ? (0xFF000000 | theme) : 0xFFDDD6CA );
			add( bg );
			label = PixelScene.renderTextBlock( text, 6 );
			label.hardlight( selected ? 0xFFFFFF : 0x5A4A3A );
			add( label );
			setSize( label.width() + 8, 11 );
		}

		@Override
		protected void layout() {
			super.layout();
			bg.x = x; bg.y = y;
			bg.size( width, height );
			label.setPos( x + 4, y + (height - label.height()) / 2f );
			PixelScene.align( label );
		}
	}

	//==== 상품 카드 ====
	private class Card extends Button {
		private final int index;
		private Image bg;
		private ItemSprite sprite;
		private ModernDigits price;
		private ModernDigits left;
		private ColorBlock tagBg;
		private ModernDigits tag;
		private RenderedTextBlock soldOut;
		private Image coin;

		Card( int index ){
			super();
			this.index = index;
			Item item = clerk.item( index );
			int count = clerk.count( index );
			int p = clerk.price( index );

			bg = new Image( Assets.Sprites.MODERN_UI, 0, 16, CARD_W, CARD_H );
			add( bg );

			sprite = new ItemSprite( item );
			add( sprite );

			price = new ModernDigits( ModernDigits.PRICE );
			price.text( Integer.toString( p ), p > ModernCoin.count() ? 0xC03020 : 0x4A2E08 );
			add( price );
			coin = new Image( Assets.Sprites.MODERN_UI, 32, 16, 7, 7 );
			add( coin );

			left = new ModernDigits( ModernDigits.PRICE );
			left.text( "x" + count, count > 0 ? 0x7A6A5A : 0xC03020 );
			add( left );

			String t = clerk.dealTag( index );
			if (!t.isEmpty()){
				tag = new ModernDigits( ModernDigits.PRICE );
				tag.text( t, 0xFFFFFF );
				tagBg = new ColorBlock( tag.width() + 4, 8, 0xFFD03030 );
				add( tagBg );
				add( tag );
			}

			if (count <= 0){
				sprite.alpha( 0.35f );
				price.alpha( 0.4f );
				coin.alpha( 0.4f );
				soldOut = PixelScene.renderTextBlock( clerk.txt( "sold_out_tag" ), 6 );
				soldOut.hardlight( 0xD03030 );
				add( soldOut );
			}
		}

		@Override
		protected void layout() {
			super.layout();
			bg.x = x; bg.y = y;
			sprite.x = x + (CARD_W - sprite.width()) / 2f;
			sprite.y = y + 2 + (22 - sprite.height()) / 2f;
			PixelScene.align( sprite );

			float pw = price.width() + 9;
			coin.x = (int)(x + (CARD_W - pw) / 2f);
			coin.y = y + 25;
			price.setPos( coin.x + 9, y + 26 );

			left.setPos( (int)(x + CARD_W - 3 - left.width()), y + 33 );

			if (tag != null){
				tagBg.x = x + 1; tagBg.y = y + 1;
				tag.setPos( x + 3, y + 2 );
			}
			if (soldOut != null){
				soldOut.setPos( x + (CARD_W - soldOut.width()) / 2f, y + 8 );
				PixelScene.align( soldOut );
			}
		}

		@Override
		protected void onClick() {
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
			if (clerk.count( index ) <= 0){
				clerk.say( clerk.txt( "sold_out_line" ) );
				return;
			}
			GameScene.show( new WndModernBuy( clerk, index, WndModernShop.this ) );
		}
	}
}
