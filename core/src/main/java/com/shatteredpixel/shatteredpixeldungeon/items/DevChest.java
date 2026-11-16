/*
 * Shattered Pixel Dungeon 모드: 테스터 직업용 개발자 상자
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.utils.Reflection;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

//원하는 아이템을 골라 즉시 얻을 수 있는 개발자용 아이템
public class DevChest extends Item {

	public static final String AC_OPEN = "OPEN";

	//한 화면에 보여줄 항목 수
	private static final int PER_PAGE = 6;

	{
		image = ItemSpriteSheet.CRYSTAL_CHEST;
		defaultAction = AC_OPEN;
		unique = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_OPEN );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_OPEN )) {
			showCategories( 0 );
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 0;
	}

	//분류 안에서 실제로 만들 수 있는 아이템 클래스만 골라냄
	private static ArrayList<Class<?>> itemClasses( Catalog cat ){
		ArrayList<Class<?>> result = new ArrayList<>();
		for (Class<?> cls : cat.items()){
			if (Item.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())){
				result.add(cls);
			}
		}
		return result;
	}

	private static String txt( String key, Object... args ){
		return Messages.get(DevChest.class, key, args);
	}

	//개발자 상자 창: 모든 아이템을 분류별 제목 아래 아이콘 격자로 한 화면에 늘어놓고 위아래로 넘겨 봄.
	// 아이콘을 누르면 바로 받고, 창은 열린 채로 남아 여러 개를 연달아 고를 수 있음.
	// 아래 단추로 받을 개수(쌓이는 아이템)와 강화 수치(강화할 수 있는 아이템)를 정함. 마지막 위치와 설정은 기억함
	private static void showCategories( final int page ){
		GameScene.show( new WndDevChest() );
	}

	//개발자 상자 창 (쪽 넘김 방식): 위에서 분류를 고르고, 가운데에 아이콘과 이름이 함께 적힌 단추가 쪽마다 늘어섬.
	// 아이템 단추를 누르면 바로 받고 창은 그대로. 아래 단추로 쪽을 넘기고, 받을 개수와 강화 수치를 정함.
	// 마지막으로 보던 분류·쪽과 설정은 기억함
	public static class WndDevChest extends com.shatteredpixel.shatteredpixeldungeon.ui.Window {

		private static final int[] QTY = { 1, 5, 20, 100 };
		private static final int[] LVL = { 0, 1, 3, 6, 10 };
		private static int qtyIdx = 0, lvlIdx = 0;
		private static int catIdx = 0, page = 0;
		private static boolean pickingCategory = false;

		private static final int BTN_H = 18;

		private final ArrayList<Catalog> cats = new ArrayList<>();
		private final ArrayList<com.watabou.noosa.ui.Component> cells = new ArrayList<>();

		private final int cols;
		private final int rows;
		private final float gridTop;

		private final com.shatteredpixel.shatteredpixeldungeon.ui.RedButton catBtn;
		private final com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock pageTxt;
		private final com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock got;
		private final float gotY;

		public WndDevChest(){
			super();
			for (Catalog cat : Catalog.values()){
				if (!itemClasses( cat ).isEmpty()) cats.add( cat );
			}
			if (catIdx >= cats.size()) catIdx = 0;

			boolean wide = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.landscape();
			int w = wide ? 240 : 140;
			int h = Math.min( (int) com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.height - 20, wide ? 210 : 260 );
			resize( w, h );
			cols = wide ? 3 : 2;

			com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock title =
					com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock( Messages.titleCase( txt( "name" ) ), 9 );
			title.hardlight( TITLE_COLOR );
			title.setPos( (w - title.width()) / 2f, 1 );
			add( title );

			//분류: ◀ [이름 - 누르면 분류 목록] ▶
			float cy = title.bottom() + 3;
			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton prevCat = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( "<", 8 ){
				@Override
				protected void onClick() {
					catIdx = (catIdx + cats.size() - 1) % cats.size(); page = 0; pickingCategory = false; refresh();
				}
			};
			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton nextCat = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( ">", 8 ){
				@Override
				protected void onClick() {
					catIdx = (catIdx + 1) % cats.size(); page = 0; pickingCategory = false; refresh();
				}
			};
			catBtn = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( "", 7 ){
				@Override
				protected void onClick() {
					pickingCategory = !pickingCategory; page = 0; refresh();
				}
			};
			prevCat.setRect( 0, cy, 18, 16 );
			nextCat.setRect( w - 18, cy, 18, 16 );
			catBtn.setRect( 20, cy, w - 40, 16 );
			add( prevCat ); add( nextCat ); add( catBtn );
			gridTop = cy + 18;

			//아래: 개수·강화, 그 위에 쪽 넘김, 그 위에 받은 아이템 표시
			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton qtyBtn = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( qtyLabel(), 7 ){
				@Override
				protected void onClick() {
					qtyIdx = (qtyIdx + 1) % QTY.length;
					text( qtyLabel() );
				}
			};
			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton lvlBtn = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( lvlLabel(), 7 ){
				@Override
				protected void onClick() {
					lvlIdx = (lvlIdx + 1) % LVL.length;
					text( lvlLabel() );
				}
			};
			qtyBtn.setRect( 0, h - 16, w / 2f - 1, 16 );
			lvlBtn.setRect( w / 2f + 1, h - 16, w / 2f - 1, 16 );
			add( qtyBtn ); add( lvlBtn );

			float py = h - 34;
			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton prevPage = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( txt( "prev" ), 7 ){
				@Override
				protected void onClick() {
					if (page > 0){ page--; refresh(); }
				}
			};
			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton nextPage = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( txt( "next" ), 7 ){
				@Override
				protected void onClick() {
					if (page < pageCount() - 1){ page++; refresh(); }
				}
			};
			prevPage.setRect( 0, py, 44, 16 );
			nextPage.setRect( w - 44, py, 44, 16 );
			add( prevPage ); add( nextPage );
			pageTxt = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock( "", 7 );
			add( pageTxt );

			got = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock( "", 6 );
			got.hardlight( 0x88FF88 );
			add( got );
			gotY = py - 9;

			rows = Math.max( 1, (int) ((gotY - 1 - gridTop) / (BTN_H + 1)) );
			refresh();
		}

		private int perPage(){ return cols * rows; }

		private int count(){
			return pickingCategory ? cats.size() : itemClasses( cats.get( catIdx ) ).size();
		}

		private int pageCount(){ return Math.max( 1, (count() + perPage() - 1) / perPage() ); }

		private void refresh(){
			for (com.watabou.noosa.ui.Component c : cells){
				c.killAndErase();
				c.destroy();
			}
			cells.clear();
			if (page >= pageCount()) page = pageCount() - 1;

			catBtn.text( pickingCategory ? txt( "pick_category" ) : Messages.titleCase( cats.get( catIdx ).title() ) );

			float cw = (width - (cols - 1)) / (float) cols;
			int start = page * perPage();
			int end = Math.min( start + perPage(), count() );
			ArrayList<Class<?>> classes = pickingCategory ? null : itemClasses( cats.get( catIdx ) );
			for (int i = start; i < end; i++){
				final int idx = i;
				com.shatteredpixel.shatteredpixeldungeon.ui.RedButton b;
				if (pickingCategory){
					b = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( Messages.titleCase( cats.get( i ).title() ), 6 ){
						@Override
						protected void onClick() {
							catIdx = idx; page = 0; pickingCategory = false; refresh();
						}
					};
				} else {
					final Class<?> cls = classes.get( i );
					Item sample = (Item) Reflection.newInstance( cls );
					String label = sample == null ? cls.getSimpleName() : Messages.titleCase( sample.trueName() );
					b = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( label, 6 ){
						@Override
						protected void onClick() {
							String name = give( cls, QTY[qtyIdx], LVL[lvlIdx] );
							if (name != null) showGot( name );
						}
					};
					if (sample != null) b.icon( new ItemSprite( sample ) );
					b.leftJustify = true;
				}
				b.multiline = true;
				int k = i - start;
				b.setRect( (k % cols) * (cw + 1), gridTop + (k / cols) * (BTN_H + 1), cw, BTN_H );
				add( b );
				cells.add( b );
			}

			pageTxt.text( (page + 1) + "/" + pageCount() );
			pageTxt.setPos( (width - pageTxt.width()) / 2f, height - 34 + (16 - pageTxt.height()) / 2f );
		}

		private void showGot( String name ){
			got.text( txt( "got", name ) );
			got.setPos( (width - got.width()) / 2f, gotY );
		}

		private static String qtyLabel(){ return txt( "qty", QTY[qtyIdx] ); }
		private static String lvlLabel(){ return txt( "lvl", LVL[lvlIdx] ); }
	}

	//선택한 아이템을 감정된 상태로 가방에 넣음 (가방이 가득 차면 발밑에 떨어뜨림)
	private static void give( Class<?> cls ){
		give( cls, 1, 0 );
	}

	private static String give( Class<?> cls, int qty, int lvl ){
		Item item = (Item) Reflection.newInstance(cls);
		if (item == null) return null;

		item.identify();
		//쌓이는 아이템은 고른 개수만큼, 강화할 수 있는 아이템은 고른 수치까지 강화
		if (item.stackable && qty > 1) item.quantity( qty );
		if (lvl > 0 && item.isUpgradable()){
			for (int i = 0; i < lvl; i++) item.upgrade();
		}
		//현대 세계 코인은 넉넉하게 1000개
		if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.modern.ModernCoin) item.quantity(1000);
		String name = Messages.titleCase(item.name());
		if (item.collect()){
			GLog.p(txt("got", name));
		} else {
			Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop();
			GLog.i(txt("dropped", name));
		}
		return name;
	}
}
