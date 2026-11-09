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
import com.shatteredpixel.shatteredpixeldungeon.windows.WndModernShop;
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
		CONVENIENCE, CAFE, PHARMACY, BOOKSTORE, HARDWARE, CLOTHING, JEWELRY,
		//먹자골목 노점
		TTEOKBOKKI, FISHBREAD, SKEWER, POTATO
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

	public Shop shop = Shop.CONVENIENCE;

	//상품 진열: 상품 견본(1개), 남은 재고, 행사, 분류
	public ArrayList<Item> stock = new ArrayList<>();
	public ArrayList<Integer> counts = new ArrayList<>();
	public ArrayList<Integer> deals = new ArrayList<>();
	public ArrayList<String> cats = new ArrayList<>();

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
			case TTEOKBOKKI: spriteClass = ClerkSprite.Tteokbokki.class; break;
			case FISHBREAD: spriteClass = ClerkSprite.Fishbread.class; break;
			case SKEWER:    spriteClass = ClerkSprite.Skewer.class; break;
			case POTATO:    spriteClass = ClerkSprite.Potato.class; break;
		}
	}

	//가게 화면 머리띠 색
	public int themeColor(){
		switch (shop){
			case CONVENIENCE: default: return 0x2E9650;
			case CAFE:      return 0x7A4C2E;
			case PHARMACY:  return 0x28967A;
			case BOOKSTORE: return 0x3C5096;
			case HARDWARE:  return 0xD66E1E;
			case CLOTHING:  return 0x964690;
			case JEWELRY:   return 0x1E2246;
			case TTEOKBOKKI: return 0xC83228;
			case FISHBREAD: return 0x326ED2;
			case SKEWER:    return 0xE09A1E;
			case POTATO:    return 0x8C5A32;
		}
	}

	public void addStock( Item item, int count, int deal, String cat ){
		if (item == null || count <= 0) return;
		item.quantity( 1 );
		stock.add( item );
		counts.add( count );
		deals.add( deal );
		cats.add( cat );
	}

	public int size(){ return stock.size(); }
	public Item item( int i ){ return stock.get( i ); }
	public int count( int i ){ return counts.get( i ); }
	public int deal( int i ){ return deals.get( i ); }
	public String cat( int i ){ return cats.get( i ); }

	//진열된 분류 (넣은 순서대로)
	public ArrayList<String> categories(){
		ArrayList<String> out = new ArrayList<>();
		for (String c : cats) if (!out.contains( c )) out.add( c );
		return out;
	}

	public String catName( String cat ){
		return cat == null ? txt( "cat_all" ) : txt( "cat_" + cat );
	}

	public String shopName(){
		return txt( "shop_" + key() );
	}

	public String promo(){
		return txt( "promo_" + key() );
	}

	private String key(){
		return shop.name().toLowerCase();
	}

	public String txt( String k, Object... args ){
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
	public String line( String kind ){
		return txt( kind + "_" + key() + "_" + Random.Int(3) );
	}

	//빨간 경고 대신 평범한 말풍선처럼 보여주기
	public void say( String str ){
		GLog.newLine();
		GLog.i( "%s: \"%s\"", Messages.titleCase( name() ), str );
	}

	//==== 가격 ====

	//한 개 값
	public int price( int index ){
		int base = Shopkeeper.sellPrice( stock.get( index ) );
		switch (deals.get( index )){
			case DEAL_20_OFF: return Math.max(1, Math.round( base * 0.8f ));
			case DEAL_30_OFF: return Math.max(1, Math.round( base * 0.7f ));
			default: return base;
		}
	}

	//행사 표시 (작은 숫자 글꼴용)
	public String dealTag( int index ){
		switch (deals.get( index )){
			case DEAL_ONE_PLUS_ONE: return "1+1";
			case DEAL_20_OFF: return "-20%";
			case DEAL_30_OFF: return "-30%";
			default: return "";
		}
	}

	public String dealInfo( int index ){
		switch (deals.get( index )){
			case DEAL_ONE_PLUS_ONE: return txt( "deal_1p1_info" );
			case DEAL_20_OFF: return txt( "deal_off_info", 20 );
			case DEAL_30_OFF: return txt( "deal_off_info", 30 );
			default: return "";
		}
	}

	//지금 살 수 있는 최대 개수
	public int maxBuy( int index ){
		int p = price( index );
		int afford = p <= 0 ? counts.get( index ) : ModernCoin.count() / p;
		return Math.min( counts.get( index ), afford );
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
		openMenu();
		return true;
	}

	//계산대에서 말 걸기 (집기를 눌렀을 때도 사용)
	public void openMenu(){
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				showMainMenu();
			}
		});
	}

	//가게 화면 열기 (cat: 처음 보여줄 분류, null이면 전체)
	public void openShop( final String cat ){
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				showShop( cat );
			}
		});
	}

	public void showShop( String cat ){
		if (cat != null && !cats.contains( cat )) cat = null;
		CurrencyIndicator.showGold = true;
		GameScene.show( new WndModernShop( this, cat, 0 ) );
	}

	private void showMainMenu(){
		if (shop == Shop.JEWELRY){
			showJewelryMenu();
			return;
		}
		final ArrayList<String> options = new ArrayList<>();
		options.add( txt( "browse" ) );
		final boolean service = hasService();
		if (service) options.add( txt( "service_" + key(), serviceFee() ) );
		options.add( txt( "talk" ) );

		String msg = line( "greet" );
		String promo = promo();
		if (!promo.isEmpty()) msg += "\n\n" + promo;
		msg += "\n\n" + txt( "wallet", ModernCoin.count() );

		CurrencyIndicator.showGold = true;
		GameScene.show( new ClerkWnd( sprite(), Messages.titleCase( name() ), msg,
				options.toArray( new String[0] ) ){
			@Override
			protected void onSelect( int index ) {
				super.onSelect( index );
				int i = index;
				if (i == 0) { showShop( null ); return; }
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

	//==== 결제 ====

	//index 상품을 n개 삼. 성공하면 true
	public boolean buy( int index, int n ){
		if (index < 0 || index >= stock.size() || n <= 0) return false;
		n = Math.min( n, counts.get( index ) );
		if (n <= 0) return false;
		int total = price( index ) * n;
		if (!ModernCoin.spend( total )) {
			say( txt( "no_money" ) );
			return false;
		}
		counts.set( index, counts.get( index ) - n );

		Item proto = stock.get( index );
		//1+1: 산 만큼 하나씩 더
		int get = deals.get( index ) == DEAL_ONE_PLUS_ONE ? n * 2 : n;

		Sample.INSTANCE.play( Assets.Sounds.GOLD );
		GLog.p( txt( "receipt_n", Messages.titleCase( proto.title() ), get, total ) );
		say( line( "thanks" ) );

		if (proto.stackable){
			give( proto.duplicate().quantity( get ) );
		} else {
			for (int i = 0; i < get; i++) give( proto.duplicate().quantity( 1 ) );
		}
		return true;
	}

	private void give( Item item ){
		if (item == null) return;
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
		counts.clear();
		deals.clear();
		cats.clear();

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
	private static final String COUNTS = "modern_counts";
	private static final String DEALS = "modern_deals";
	private static final String CATS  = "modern_cats";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SHOP, shop.name() );
		bundle.put( STOCK, stock );
		int[] c = new int[counts.size()];
		int[] d = new int[deals.size()];
		for (int i = 0; i < c.length; i++) c[i] = counts.get(i);
		for (int i = 0; i < d.length; i++) d[i] = deals.get(i);
		bundle.put( COUNTS, c );
		bundle.put( DEALS, d );
		bundle.put( CATS, cats.toArray( new String[0] ) );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( SHOP )) {
			shop = Shop.valueOf( bundle.getString( SHOP ) );
		}
		updateSprite();
		stock.clear(); counts.clear(); deals.clear(); cats.clear();
		if (bundle.contains( STOCK )) {
			Collection<?> items = bundle.getCollection( STOCK );
			for (Object o : items) stock.add( (Item) o );
			int[] c = bundle.contains( COUNTS ) ? bundle.getIntArray( COUNTS ) : null;
			int[] d = bundle.getIntArray( DEALS );
			String[] k = bundle.contains( CATS ) ? bundle.getStringArray( CATS ) : null;
			for (int i = 0; i < stock.size(); i++) {
				counts.add( c != null && i < c.length ? c[i] : Math.max( 1, stock.get(i).quantity() ) );
				deals.add( d != null && i < d.length ? d[i] : DEAL_NONE );
				cats.add( k != null && i < k.length ? k[i] : "etc" );
				stock.get(i).quantity( 1 );
			}
		}
	}
}
