/*
 * Shattered Pixel Dungeon 모드: 자판기 화면
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.modern.ModernCoin;
import com.shatteredpixel.shatteredpixeldungeon.levels.ModernLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.CurrencyIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//진짜 자판기처럼 생긴 화면
//1) 동전 투입구(+10)나 지폐 투입구(+50)를 눌러 코인을 넣으면 위 금액 표시창 숫자가 올라감
//2) 넣은 돈으로 살 수 있는 음료는 아래 버튼에 초록 불, 품절은 빨간 불
//3) 버튼을 누르면 잠시 뒤 아래 꺼내는 곳으로 음료가 '덜컹' 떨어짐 -> 눌러서 꺼냄
//4) 둥근 반환 버튼을 누르면 남은 돈을 돌려받음 (창을 닫아도 자동으로 돌려받고, 꺼내지 않은 음료도 챙김)
public class WndVending extends Window {

	private static final int W = 128, H = 152;
	private static final int SLOTS = 8;

	private final ModernLevel level;
	private final int cell;

	private int credit = 0;

	private Image[] lights = new Image[SLOTS];
	private BitmapText[] prices = new BitmapText[SLOTS];
	private ItemSprite[] shelf = new ItemSprite[SLOTS];
	private BitmapText led;
	private RenderedTextBlock notice;
	private float noticeTime;
	private String ledFlash;
	private float ledTime;

	//떨어지는 중인 음료와 꺼내는 곳에 놓인 음료
	private ArrayList<ItemSprite> falling = new ArrayList<>();
	private ArrayList<Float> fallSpeed = new ArrayList<>();
	private ArrayList<Float> fallDelay = new ArrayList<>();
	private ArrayList<Item> fallingItems = new ArrayList<>();
	private ArrayList<ItemSprite> outletSprites = new ArrayList<>();
	private ArrayList<Item> outlet = new ArrayList<>();
	private Image flap;
	private Group drops;

	private static final float OUT_X = 12, OUT_Y = 111, OUT_W = 76, OUT_BOTTOM = 137;

	public WndVending( ModernLevel level, int cell ){
		super( W, H, Chrome.get( Chrome.Type.BLANK ) );
		this.level = level;
		this.cell = cell;

		Image body = new Image( Assets.Sprites.VENDING_UI, 0, 0, W, H );
		add( body );
		//꺼내는 곳의 음료는 몸체 바로 위, 반투명 덮개 아래에 그림
		drops = new Group();
		add( drops );

		RenderedTextBlock head = PixelScene.renderTextBlock( Messages.get( this, "title" ), 8 );
		head.hardlight( 0xC8323C );
		head.setPos( (W - head.width()) / 2f, 3 + (14 - head.height()) / 2f );
		PixelScene.align( head );
		add( head );

		int[] stock = level.vendingStock( cell );
		for (int i = 0; i < SLOTS; i++){
			final int slot = i;
			int sx = 6 + (i % 4) * 22, sy = 19 + (i / 4) * 44;

			Item drink = ModernLevel.vendingDrink( i );
			ItemSprite spr = new ItemSprite( drink );
			spr.x = sx + (22 - spr.width()) / 2f;
			spr.y = sy + 21 - spr.height();
			PixelScene.align( spr );
			if (stock[i] <= 0) spr.alpha( 0.3f );
			shelf[i] = spr;
			add( spr );

			BitmapText pr = new BitmapText( PixelScene.pixelFont );
			prices[i] = pr;
			add( pr );

			Image light = new Image( Assets.Sprites.VENDING_UI, 136, 0, 10, 5 );
			light.x = sx + 6; light.y = sy + 32;
			lights[i] = light;
			add( light );

			Button b = new Button(){
				@Override
				protected void onClick() { select( slot ); }
			};
			b.setRect( sx, sy, 22, 40 );
			add( b );
		}

		//유리 반사광
		Image glare = new Image( Assets.Sprites.VENDING_UI, 136, 24, 90, 88 );
		glare.x = 5; glare.y = 19;
		add( glare );

		led = new BitmapText( PixelScene.pixelFont );
		led.hardlight( 0x55FF77 );
		add( led );

		Button coinSlot = new Button(){
			@Override
			protected void onClick() { insert( 10 ); }
		};
		coinSlot.setRect( 100, 35, 23, 19 );
		add( coinSlot );

		Button billSlot = new Button(){
			@Override
			protected void onClick() { insert( 50 ); }
		};
		billSlot.setRect( 98, 54, 26, 15 );
		add( billSlot );

		Button lever = new Button(){
			@Override
			protected void onClick() { returnCredit( true ); }
		};
		lever.setRect( 101, 69, 20, 20 );
		add( lever );

		flap = new Image( Assets.Sprites.VENDING_UI, 136, 120, 80, 14 );
		flap.x = 10; flap.y = OUT_Y;

		Button out = new Button(){
			@Override
			protected void onClick() { takeOut(); }
		};
		out.setRect( 8, 109, 84, 32 );
		add( out );
		add( flap );

		notice = PixelScene.renderTextBlock( 6 );
		notice.hardlight( 0xFFFFFF );
		add( notice );

		CurrencyIndicator.showGold = true;
		refresh();
	}

	private void addLabel( String s, float cx, float y ){
		BitmapText t = new BitmapText( s, PixelScene.pixelFont );
		t.measure();
		t.hardlight( 0x3A3A44 );
		t.x = cx - t.width() / 2f; t.y = y;
		PixelScene.align( t );
		add( t );
	}

	private void refresh(){
		int[] stock = level.vendingStock( cell );
		for (int i = 0; i < SLOTS; i++){
			int sx = 6 + (i % 4) * 22, sy = 19 + (i / 4) * 44;
			int p = ModernLevel.vendingPrice( i );
			BitmapText pr = prices[i];
			if (stock[i] <= 0){
				pr.text( "SOLD" );
				pr.hardlight( 0xD03030 );
				lights[i].frame( 136, 12, 10, 5 );
				shelf[i].alpha( 0.3f );
			} else {
				pr.text( Integer.toString( p ) );
				pr.hardlight( 0x30303A );
				lights[i].frame( 136, credit >= p ? 0 : 6, 10, 5 );
			}
			pr.measure();
			pr.x = sx + 2 + (18 - pr.width()) / 2f;
			pr.y = sy + 23 + (7 - pr.baseLine()) / 2f + 0.5f;
			PixelScene.align( pr );
		}
		String s = ledFlash != null ? ledFlash : Integer.toString( credit );
		led.text( s );
		led.measure();
		led.x = 121 - led.width();
		led.y = 24;
		PixelScene.align( led );
	}

	private void say( String key, Object... args ){
		notice.text( Messages.get( this, key, args ), W - 8 );
		notice.setPos( (W - notice.width()) / 2f, 142 + (9 - notice.height()) / 2f );
		PixelScene.align( notice );
		noticeTime = 2f;
		notice.alpha( 1 );
	}

	private void flash( String s ){
		ledFlash = s;
		ledTime = 0.9f;
		refresh();
	}

	private void insert( int n ){
		int have = ModernCoin.count();
		if (credit >= 990){
			flash( "FULL" );
			return;
		}
		n = Math.min( n, Math.min( have, 990 - credit ) );
		if (n <= 0){
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
			flash( "0" );
			say( "no_coin" );
			return;
		}
		if (!ModernCoin.spend( n )) return;
		credit += n;
		Sample.INSTANCE.play( Assets.Sounds.GOLD, 0.8f, 1.3f );
		ledFlash = null;
		refresh();
	}

	private void select( int i ){
		int[] stock = level.vendingStock( cell );
		int p = ModernLevel.vendingPrice( i );
		if (stock[i] <= 0){
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
			flash( "SOLD" );
			return;
		}
		if (credit < p){
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
			flash( Integer.toString( p ) );
			say( "need_more", p - credit );
			return;
		}
		credit -= p;
		stock[i]--;
		Sample.INSTANCE.play( Assets.Sounds.CLICK );
		//기계가 움직이는 소리 뒤에 덜컹
		Sample.INSTANCE.playDelayed( Assets.Sounds.STURDY, 0.35f, 0.8f );

		Item drink = ModernLevel.vendingDrink( i );
		ItemSprite spr = new ItemSprite( drink );
		int n = outlet.size() + falling.size();
		spr.x = OUT_X + 6 + (n % 5) * 14 + (12 - spr.width()) / 2f;
		spr.y = OUT_Y - spr.height();
		spr.visible = false;
		drops.add( spr );
		falling.add( spr ); fallSpeed.add( 0f ); fallDelay.add( 0.35f ); fallingItems.add( drink );
		ledFlash = null;
		refresh();
	}

	private void takeOut(){
		if (outlet.isEmpty()){
			if (!falling.isEmpty()) return;
			say( "outlet_empty" );
			return;
		}
		for (Item it : outlet) give( it );
		Sample.INSTANCE.play( Assets.Sounds.ITEM );
		say( "took", outlet.size() );
		outlet.clear();
		for (ItemSprite s : outletSprites) s.killAndErase();
		outletSprites.clear();
	}

	private void give( Item item ){
		GLog.p( Messages.get( this, "got", Messages.titleCase( item.title() ) ) );
		if (!item.collect()){
			Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
		}
	}

	private void returnCredit( boolean sound ){
		if (credit <= 0){
			if (sound) Sample.INSTANCE.play( Assets.Sounds.CLICK );
			return;
		}
		ModernCoin.give( credit );
		if (sound) {
			Sample.INSTANCE.play( Assets.Sounds.GOLD, 1f, 0.8f );
			say( "returned", credit );
		}
		credit = 0;
		refresh();
	}

	@Override
	public void update() {
		super.update();
		for (int k = 0; k < falling.size(); k++){
			ItemSprite s = falling.get( k );
			float d = fallDelay.get( k ) - Game.elapsed;
			fallDelay.set( k, d );
			if (d > 0) continue;
			s.visible = true;
			float v = fallSpeed.get( k ) + 400f * Game.elapsed;
			fallSpeed.set( k, v );
			s.y += v * Game.elapsed;
			float floor = OUT_BOTTOM - s.height();
			if (s.y >= floor){
				s.y = floor;
				outlet.add( fallingItems.get( k ) );
				outletSprites.add( s );
				falling.remove( k ); fallSpeed.remove( k ); fallDelay.remove( k ); fallingItems.remove( k );
				k--;
			}
		}
		if (ledTime > 0){
			ledTime -= Game.elapsed;
			if (ledTime <= 0){
				ledFlash = null;
				refresh();
			}
		}
		if (noticeTime > 0){
			noticeTime -= Game.elapsed;
			notice.alpha( Math.min( 1f, noticeTime * 2 ) );
		}
	}

	@Override
	public void hide() {
		//떨어지던 음료와 꺼내지 않은 음료, 남은 돈을 모두 챙김
		outlet.addAll( fallingItems );
		fallingItems.clear();
		for (Item it : outlet) give( it );
		outlet.clear();
		returnCredit( false );
		super.hide();
		CurrencyIndicator.showGold = false;
	}
}
