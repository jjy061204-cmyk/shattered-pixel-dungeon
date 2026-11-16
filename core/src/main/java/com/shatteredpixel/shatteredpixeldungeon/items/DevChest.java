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

	public static class WndDevChest extends com.shatteredpixel.shatteredpixeldungeon.ui.Window {

		private static final int[] QTY = { 1, 5, 20, 100 };
		private static final int[] LVL = { 0, 1, 3, 6, 10 };
		private static int qtyIdx = 0, lvlIdx = 0;
		private static float lastScroll = 0;

		private final com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane grid;

		public WndDevChest(){
			super();
			boolean wide = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.landscape();
			int width = wide ? 230 : 136;
			int height = Math.min( (int) com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.height - 24, wide ? 200 : 240 );
			//목록(스크롤 창)은 배치되는 순간의 창 위치를 기준으로 그려지므로, 창 크기부터 정함
			resize( width, height );

			com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock title =
					com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock( Messages.titleCase( txt( "name" ) ), 9 );
			title.hardlight( TITLE_COLOR );
			title.setPos( (width - title.width()) / 2f, 2 );
			add( title );

			com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock hint =
					com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock( txt( "hint" ), 6 );
			hint.maxWidth( width );
			hint.setPos( (width - hint.width()) / 2f, title.bottom() + 2 );
			add( hint );

			final com.shatteredpixel.shatteredpixeldungeon.ui.RedButton qtyBtn = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( qtyLabel(), 7 ){
				@Override
				protected void onClick() {
					qtyIdx = (qtyIdx + 1) % QTY.length;
					text( qtyLabel() );
				}
			};
			final com.shatteredpixel.shatteredpixeldungeon.ui.RedButton lvlBtn = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton( lvlLabel(), 7 ){
				@Override
				protected void onClick() {
					lvlIdx = (lvlIdx + 1) % LVL.length;
					text( lvlLabel() );
				}
			};
			float btnY = height - 16;
			//방금 받은 아이템 표시
			got = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock( "", 6 );
			got.hardlight( 0x88FF88 );
			add( got );
			gotY = btnY - 9;
			qtyBtn.setRect( 0, btnY, width / 2f - 1, 16 );
			lvlBtn.setRect( width / 2f + 1, btnY, width / 2f - 1, 16 );
			add( qtyBtn );
			add( lvlBtn );

			grid = new com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane();
			add( grid );
			for (Catalog cat : Catalog.values()){
				ArrayList<Class<?>> classes = itemClasses( cat );
				if (classes.isEmpty()) continue;
				grid.addHeader( "_" + Messages.titleCase( cat.title() ) + "_" );
				for (final Class<?> cls : classes){
					Item sample = (Item) Reflection.newInstance( cls );
					if (sample == null) continue;
					com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane.GridItem cell =
							new com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane.GridItem( new ItemSprite( sample ) ){
						@Override
						public boolean onClick( float x, float y ) {
							if (inside( x, y )){
								String name = give( cls, QTY[qtyIdx], LVL[lvlIdx] );
								if (name != null) showGot( name );
								return true;
							}
							return false;
						}
					};
					grid.addItem( cell );
				}
			}
			float top = hint.bottom() + 3;
			grid.setRect( 0, top, width, gotY - 2 - top );
			grid.scrollTo( 0, lastScroll );
		}

		private com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock got;
		private float gotY;

		private void showGot( String name ){
			got.text( txt( "got", name ) );
			got.setPos( (width - got.width()) / 2f, gotY );
		}

		private static String qtyLabel(){ return txt( "qty", QTY[qtyIdx] ); }
		private static String lvlLabel(){ return txt( "lvl", LVL[lvlIdx] ); }

		@Override
		public void hide() {
			lastScroll = grid.content().camera.scroll.y;
			super.hide();
		}
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
