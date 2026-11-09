/*
 * Shattered Pixel Dungeon 모드
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.modern.ModernCoin;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ClerkSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CurrencyIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

//현대 세계 층의 점원.
//던전 상점처럼 바닥에 물건을 늘어놓지 않고, 계산대에서 말을 걸면 상품 목록(메뉴판)이 뜸.
//- 가게마다 행사가 다름: 편의점 1+1, 카페 오늘의 메뉴 할인, 옷가게 전 품목 할인
//- 약국은 물약 성분 분석, 서점은 주문서 감정 서비스를 제공
//- 결제하면 영수증 메시지가 남음
//- 돈은 금화가 아니라 '코인'(ModernCoin)으로 냄. 금은방에서 금화 1개 = 코인 1개로 바꿀 수 있음
//- 물건 팔기(금으로 매입)는 금은방에서만 함
public class ModernClerk extends Shopkeeper {

	public enum Shop {
		CONVENIENCE, CAFE, PHARMACY, BOOKSTORE, HARDWARE, CLOTHING, JEWELRY
	}

	//금은방 환전 단위
	private static final int[] EXCHANGE_STEPS = { 100, 500, 1000 };

	//행사 종류
	public static final int DEAL_NONE    = 0;
	public static final int DEAL_ONE_PLUS_ONE = 1;   //하나 사면 하나 더
	public static final int DEAL_20_OFF  = 2;
	public static final int DEAL_30_OFF  = 3;

	//예전 방식(바닥 진열) 가게용: 자기 가게로 보는 범위
	private static final int SHOP_RADIUS = 4;
	//목록 한 쪽에 보여줄 상품 수
	private static final int PER_PAGE = 5;

	public Shop shop = Shop.CONVENIENCE;

	public ArrayList<Item> stock = new ArrayList<>();
	public ArrayList<Integer> deals = new ArrayList<>();

	public ModernClerk(){
		super();
		updateSprite();
	}

	public ModernClerk( Shop shop ){
		super();
		this.shop = shop;
		updateSprite();
	}

	private void updateSprite(){
		switch (shop){
			case CONVENIENCE: default: spriteClass = ClerkSprite.Convenience.class; break;
			case CAFE:      spriteClass = ClerkSprite.Cafe.class; break;
			case PHARMACY:  spriteClass = ClerkSprite.Pharmacy.class; break;
			case BOOKSTORE: spriteClass = ClerkSprite.Bookstore.class; break;
			case HARDWARE:  spriteClass = ClerkSprite.Hardware.class; break;
			case CLOTHING:  spriteClass = ClerkSprite.Clothing.class; break;
			case JEWELRY:   spriteClass = ClerkSprite.Jewelry.class; break;
		}
	}

	public void addStock( Item item, int deal ){
		if (item == null) return;
		stock.add( item );
		deals.add( deal );
	}

	private String key(){
		return shop.name().toLowerCase();
	}

	private String txt( String k, Object... args ){
		return Messages.get( ModernClerk.class, k, args );
	}

	@Override
	public String name() {
		return txt( "name_" + key() );
	}

	@Override
	public String description() {
		return txt( "desc_" + key() );
	}

	//가게마다 대사 3개 중 하나
	private String line( String kind ){
		return txt( kind + "_" + key() + "_" + Random.Int(3) );
	}

	//빨간 경고 대신 평범한 말풍선처럼 보여주기
	private void say( String str ){
		GLog.newLine();
		GLog.i( "%s: \"%s\"", Messages.titleCase( name() ), str );
	}

	//==== 가격 ====

	public int price( int index ){
		Item item = stock.get( index );
		int base = Shopkeeper.sellPrice( item );
		switch (deals.get( index )){
			case DEAL_20_OFF: return Math.max(1, Math.round( base * 0.8f ));
			case DEAL_30_OFF: return Math.max(1, Math.round( base * 0.7f ));
			default: return base;
		}
	}

	private String dealTag( int index ){
		switch (deals.get( index )){
			case DEAL_ONE_PLUS_ONE: return txt( "tag_1p1" );
			case DEAL_20_OFF: return txt( "tag_20" );
			case DEAL_30_OFF: return txt( "tag_30" );
			default: return "";
		}
	}

	private int serviceFee(){
		return 40 * (Dungeon.depth / 5 + 1);
	}

	private boolean hasService(){
		return shop == Shop.PHARMACY || shop == Shop.BOOKSTORE;
	}

	//==== 말 걸기 ====

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero) {
			return true;
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				showMainMenu();
			}
		});
		return true;
	}

	private void showMainMenu(){
		if (shop == Shop.JEWELRY){
			showJewelryMenu();
			return;
		}
		final ArrayList<String> options = new ArrayList<>();
		options.add( txt( "browse", stock.size() ) );
		final boolean service = hasService();
		if (service) options.add( txt( "service_" + key(), serviceFee() ) );
		options.add( txt( "talk" ) );

		String msg = line( "greet" );
		String promo = txt( "promo_" + key() );
		if (!promo.isEmpty()) msg += "\n\n" + promo;
		msg += "\n\n" + txt( "wallet", ModernCoin.count() );

		CurrencyIndicator.showGold = true;
		GameScene.show( new ClerkWnd( sprite(), Messages.titleCase( name() ), msg,
				options.toArray( new String[0] ) ){
			@Override
			protected void onSelect( int index ) {
				super.onSelect( index );
				int i = index;
				if (i == 0) { showStock( 0 ); return; }
				if (service) {
					if (i == 1) { startService(); return; }
					i--;
				}
				if (i == 1) {
					GameScene.show( new WndTitledMessage( sprite(), Messages.titleCase( name() ), line( "chat" ) ) );
				}
			}
		});
	}

	//==== 금은방: 환전과 매입 ====

	private void showJewelryMenu(){
		String msg = line( "greet" ) + "\n\n" + txt( "promo_jewelry" )
				+ "\n\n" + txt( "wallet_both", Dungeon.gold, ModernCoin.count() );
		CurrencyIndicator.showGold = true;
		GameScene.show( new ClerkWnd( sprite(), Messages.titleCase( name() ), msg,
				txt( "to_coin" ), txt( "to_gold" ), txt( "sell_jewelry" ), txt( "talk" ) ){
			@Override
			protected void onSelect( int index ) {
				super.onSelect( index );
				switch (index){
					case 0: showExchange( true ); break;
					case 1: showExchange( false ); break;
					case 2: sell(); break;
					case 3: GameScene.show( new WndTitledMessage( sprite(), Messages.titleCase( name() ), line( "chat" ) ) ); break;
				}
			}
		});
	}

	//toCoin: 금화 -> 코인, 아니면 코인 -> 금화
	private void showExchange( final boolean toCoin ){
		final int have = toCoin ? Dungeon.gold : ModernCoin.count();
		final ArrayList<String> options = new ArrayList<>();
		final ArrayList<Integer> amounts = new ArrayList<>();
		for (int n : EXCHANGE_STEPS){
			options.add( txt( toCoin ? "amount_to_coin" : "amount_to_gold", n ) );
			amounts.add( n );
		}
		options.add( txt( "amount_all", have ) );
		amounts.add( have );
		options.add( txt( "back" ) );

		CurrencyIndicator.showGold = true;
		GameScene.show( new ClerkWnd( sprite(), txt( toCoin ? "to_coin" : "to_gold" ),
				txt( toCoin ? "exchange_body_to_coin" : "exchange_body_to_gold", Dungeon.gold, ModernCoin.count() ),
				options.toArray( new String[0] ) ){
			@Override
			protected boolean enabled( int i ) {
				if (i < amounts.size()) {
					int n = amounts.get( i );
					return n > 0 && n <= have;
				}
				return true;
			}

			@Override
			protected void onSelect( int i ) {
				super.onSelect( i );
				if (i < amounts.size()){
					exchange( toCoin, amounts.get( i ) );
				}
				showJewelryMenu();
			}
		});
	}

	private void exchange( boolean toCoin, int n ){
		if (n <= 0) return;
		if (toCoin){
			if (Dungeon.gold < n) { say( txt( "no_gold" ) ); return; }
			Dungeon.gold -= n;
			Catalog.countUses( Gold.class, n );
			ModernCoin.give( n );
			GLog.p( txt( "exchanged_to_coin", n, n ) );
		} else {
			if (!ModernCoin.spend( n )) { say( txt( "no_money" ) ); return; }
			Dungeon.gold += n;
			GLog.p( txt( "exchanged_to_gold", n, n ) );
		}
		Sample.INSTANCE.play( Assets.Sounds.GOLD );
		say( line( "thanks" ) );
	}

	//==== 상품 목록 (메뉴판) ====

	private void showStock( final int page ){
		if (stock.isEmpty()){
			say( txt( "sold_out" ) );
			return;
		}
		final int pages = (stock.size() + PER_PAGE - 1) / PER_PAGE;
		final int p = Math.min( page, pages - 1 );
		final int start = p * PER_PAGE;
		final int end = Math.min( start + PER_PAGE, stock.size() );

		final ArrayList<String> options = new ArrayList<>();
		for (int i = start; i < end; i++){
			Item item = stock.get(i);
			String name = Messages.titleCase( item.title() );
			if (name.length() > 16) name = name.substring(0, 15) + "…";
			options.add( txt( "price_line", name, price(i), dealTag(i) ) );
		}
		final int listSize = options.size();
		final boolean hasPrev = p > 0, hasNext = p < pages - 1;
		final int coins = ModernCoin.count();
		if (hasPrev) options.add( txt( "prev" ) );
		if (hasNext) options.add( txt( "next" ) );
		options.add( txt( "back" ) );

		CurrencyIndicator.showGold = true;
		GameScene.show( new ClerkWnd( sprite(), txt( "menu_title_" + key() ),
				txt( "menu_body", p + 1, pages, coins ), options.toArray( new String[0] ) ){

			@Override
			protected boolean hasIcon( int index ) {
				return index < listSize;
			}

			@Override
			protected Image getIcon( int index ) {
				return new ItemSprite( stock.get( start + index ) );
			}

			@Override
			protected void onSelect( int index ) {
				super.onSelect( index );
				if (index < listSize){
					confirm( start + index, p );
				} else if (hasPrev && index == listSize){
					showStock( p - 1 );
				} else if (hasNext && index == listSize + (hasPrev ? 1 : 0)){
					showStock( p + 1 );
				} else {
					showMainMenu();
				}
			}
		});
	}

	private void confirm( final int index, final int page ){
		final Item item = stock.get( index );
		final int price = price( index );
		String info = item.info();
		if (deals.get(index) == DEAL_ONE_PLUS_ONE) info += "\n\n" + txt( "deal_1p1_info" );
		info += "\n\n" + txt( "total", price, ModernCoin.count() );

		GameScene.show( new ClerkWnd( new ItemSprite( item ), Messages.titleCase( item.title() ), info,
				txt( "pay", price ), txt( "cancel" ) ){
			@Override
			protected boolean enabled( int i ) {
				if (i == 0) return ModernCoin.count() >= price;
				return true;
			}

			@Override
			protected void onSelect( int i ) {
				super.onSelect( i );
				if (i == 0){
					buy( index );
				}
				showStock( page );
			}
		});
	}

	private void buy( int index ){
		if (index < 0 || index >= stock.size()) return;
		int price = price( index );
		if (ModernCoin.count() < price) return;
		if (!ModernCoin.spend( price )) return;

		Item item = stock.remove( index );
		int deal = deals.remove( index );

		//1+1: 겹칠 수 있는 물건이면 하나 더
		if (deal == DEAL_ONE_PLUS_ONE && item.stackable){
			item.quantity( item.quantity() * 2 );
		}

		Sample.INSTANCE.play( Assets.Sounds.GOLD );
		GLog.p( txt( "receipt", Messages.titleCase( item.title() ), price ) );
		say( line( "thanks" ) );

		if (!item.doPickUp( Dungeon.hero )){
			Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
		}
	}

	//==== 감정 서비스 (약국: 물약, 서점: 주문서) ====

	private void startService(){
		final int fee = serviceFee();
		if (ModernCoin.count() < fee){
			say( txt( "no_money" ) );
			return;
		}
		final boolean potions = shop == Shop.PHARMACY;
		GameScene.selectItem( new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return txt( potions ? "service_prompt_pharmacy" : "service_prompt_bookstore" );
			}

			@Override
			public boolean itemSelectable( Item item ) {
				if (item.isIdentified()) return false;
				return potions ? item instanceof Potion : item instanceof Scroll;
			}

			@Override
			public void onSelect( Item item ) {
				if (item == null) return;
				if (!ModernCoin.spend( fee )) return;
				item.identify();
				Sample.INSTANCE.play( Assets.Sounds.GOLD );
				say( txt( potions ? "service_done_pharmacy" : "service_done_bookstore",
						Messages.titleCase( item.title() ) ) );
			}
		});
	}

	//==== 도망 ====

	@Override
	public void destroy() {
		//가게 물건은 점원과 함께 사라짐
		stock.clear();
		deals.clear();

		//예전 방식(바닥 진열) 가게: 다른 가게 물건은 잠시 일반 더미로 바꿔서 보호
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

	//창을 닫으면 금화·코인 표시도 다시 숨김
	private static class ClerkWnd extends WndOptions {
		public ClerkWnd( Image icon, String title, String message, String... options ){
			super( icon, title, message, options );
		}

		@Override
		public void hide() {
			super.hide();
			CurrencyIndicator.showGold = false;
		}
	}

	//==== 저장 ====

	private static final String SHOP  = "modern_shop";
	private static final String STOCK = "modern_stock";
	private static final String DEALS = "modern_deals";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SHOP, shop.name() );
		bundle.put( STOCK, stock );
		int[] d = new int[deals.size()];
		for (int i = 0; i < d.length; i++) d[i] = deals.get(i);
		bundle.put( DEALS, d );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( SHOP )) {
			shop = Shop.valueOf( bundle.getString( SHOP ) );
		}
		updateSprite();
		stock.clear();
		deals.clear();
		if (bundle.contains( STOCK )) {
			Collection<?> items = bundle.getCollection( STOCK );
			for (Object o : items) stock.add( (Item) o );
			int[] d = bundle.getIntArray( DEALS );
			for (int i = 0; i < stock.size(); i++) deals.add( d != null && i < d.length ? d[i] : DEAL_NONE );
		}
	}
}
