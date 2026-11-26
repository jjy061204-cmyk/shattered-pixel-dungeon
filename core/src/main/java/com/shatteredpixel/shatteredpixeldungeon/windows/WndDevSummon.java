/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 테스터 소환 기기 창: 분류를 고르고, 아이콘이 달린 몬스터 목록에서 눌러 바로 옆에 불러냄
 *  - 미믹(★)은 안에 넣을 아이템을 가방에서 골라 넣을 수 있음
 *  - 아래 버튼: 허수아비 소환 / 치우기
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.DevChest;
import com.shatteredpixel.shatteredpixeldungeon.items.DevRemote;
import com.shatteredpixel.shatteredpixeldungeon.items.DevSummoner;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingListPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.noosa.Image;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class WndDevSummon extends Window {

	private static int lastGroup = 0;

	private final int WIDTH;
	private final int HEIGHT;

	private final RedButton category;
	private final ScrollingListPane list;

	public WndDevSummon( DevSummoner summoner ){
		super();

		WIDTH = PixelScene.landscape() ? 200 : 135;
		HEIGHT = Math.max( 140, Math.min( 230, (int)(PixelScene.uiCamera.height * 0.85f) ) );

		IconTitle title = new IconTitle( new ItemSprite( summoner ), Messages.titleCase( summoner.name() ) );
		title.setRect( 0, 0, WIDTH, 0 );
		add( title );
		float pos = title.bottom() + 3;

		category = new RedButton( "" ){
			@Override
			protected void onClick() {
				chooseGroup();
			}
		};
		category.setRect( 0, pos, WIDTH, 18 );
		add( category );
		pos = category.bottom() + 2;

		final float btnH = 18;
		list = new ScrollingListPane();
		add( list );
		list.setRect( 0, pos, WIDTH, HEIGHT - pos - btnH - 3 );

		RedButton dummy = new RedButton( Messages.get( DevSummoner.class, "btn_dummy" ) ){
			@Override
			protected void onClick() {
				hide();
				DevSummoner.summonDummy();
			}
		};
		dummy.setRect( 0, HEIGHT - btnH, WIDTH/2f - 1, btnH );
		add( dummy );

		RedButton undummy = new RedButton( Messages.get( DevSummoner.class, "btn_dummy_remove" ) ){
			@Override
			protected void onClick() {
				hide();
				DevSummoner.removeDummy();
			}
		};
		undummy.setRect( WIDTH/2f + 1, HEIGHT - btnH, WIDTH/2f - 1, btnH );
		undummy.enable( DevSummoner.dummyHere() != null );
		add( undummy );

		resize( WIDTH, HEIGHT );

		fill( Math.min( lastGroup, DevSummoner.groupCount() - 1 ) );
	}

	private void chooseGroup(){
		String[] names = new String[DevSummoner.groupCount()];
		for (int i = 0; i < names.length; i++) names[i] = DevSummoner.groupName( i );
		GameScene.show( new WndOptions( Icons.get( Icons.ENTER ), Messages.get( DevSummoner.class, "category_title" ),
				Messages.get( DevSummoner.class, "category_msg" ), names ){
			@Override
			protected void onSelect( int index ) {
				fill( index );
			}
		} );
	}

	private void fill( int group ){
		lastGroup = group;
		category.text( Messages.get( DevSummoner.class, "category", DevSummoner.groupName( group ) ) );
		list.clear();
		if (group < DevSummoner.MOBS.length){
			for (final Class<?> cls : DevSummoner.MOBS[group]){
				Mob m = (Mob) Reflection.newInstance( cls );
				if (m == null) continue;
				boolean mimic = m instanceof Mimic;
				String label = Messages.titleCase( m.name() ) + (mimic ? "  " + Messages.get( DevSummoner.class, "mimic_mark" ) : "");
				list.addItem( new Row( icon( m ), label ){
					@Override
					protected void onPick() {
						hide();
						if (Mimic.class.isAssignableFrom( cls )) mimicMenu( cls, new ArrayList<>() );
						else DevSummoner.summon( cls );
					}
				} );
			}
		} else {
			for (int i = 0; i < DevSummoner.BOSSES.length; i++){
				final int idx = i;
				Mob m = (Mob) Reflection.newInstance( DevSummoner.BOSSES[i] );
				if (m == null) continue;
				list.addItem( new Row( icon( m ), DevSummoner.bossLabel( i ) ){
					@Override
					protected void onPick() {
						hide();
						DevSummoner.boss( idx );
					}
				} );
			}
		}
		list.scrollTo( 0, 0 );
	}

	//몹 그림을 16칸 안에 맞춘 아이콘
	private static Image icon( Mob m ){
		Image icon;
		try {
			if (m instanceof Mimic) m.alignment = com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY;
			CharSprite sprite = m.sprite();
			sprite.idle();
			icon = new Image( sprite );
		} catch (Exception e){
			ShatteredPixelDungeon.reportException( e );
			return Icons.get( Icons.ENTER );
		}
		float big = Math.max( icon.width(), icon.height() );
		if (big > 17){
			icon.scale.set( 16f / big );
		}
		return icon;
	}

	private static abstract class Row extends ScrollingListPane.ListItem {
		public Row( Image icon, String text ){
			super( icon, text );
		}
		protected abstract void onPick();
		@Override
		public boolean onClick( float x, float y ) {
			if (inside( x, y )){
				onPick();
				return true;
			}
			return false;
		}
	}

	// ── 미믹 꾸미기: 안에 넣을 아이템을 가방에서 고름 ──
	public static void mimicMenu( final Class<?> cls, final ArrayList<Item> inside ){
		Mob m = (Mob) Reflection.newInstance( cls );
		String title = m != null ? Messages.titleCase( m.name() ) : "";
		StringBuilder names = new StringBuilder();
		for (Item i : inside){
			if (names.length() > 0) names.append( ", " );
			names.append( Messages.titleCase( i.title() ) );
		}
		String body = Messages.get( DevSummoner.class, "mimic_msg",
				inside.isEmpty() ? Messages.get( DevSummoner.class, "mimic_none" ) : names.toString() );
		GameScene.show( new WndOptions( new ItemSprite( ItemSpriteSheet.CHEST ), title, body,
				Messages.get( DevSummoner.class, "mimic_add" ),
				Messages.get( DevSummoner.class, inside.isEmpty() ? "mimic_default" : "mimic_go" ),
				Messages.get( DevSummoner.class, "mimic_clear" ),
				Messages.get( DevSummoner.class, "mimic_cancel" ) ){
			@Override
			protected void onSelect( int index ) {
				switch (index){
					case 0:
						pickItem( cls, inside );
						break;
					case 1:
						DevSummoner.summonMimic( cls, inside );
						break;
					case 2:
						giveBack( inside );
						mimicMenu( cls, inside );
						break;
					default:
						giveBack( inside );
						break;
				}
			}
			@Override
			protected boolean enabled( int index ) {
				return index != 2 || !inside.isEmpty();
			}
			@Override
			public void onBackPressed() {
				giveBack( inside );
				super.onBackPressed();
			}
		} );
	}

	private static void pickItem( final Class<?> cls, final ArrayList<Item> inside ){
		final Hero hero = Dungeon.hero;
		GameScene.selectItem( new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get( DevSummoner.class, "mimic_prompt" );
			}
			@Override
			public boolean itemSelectable( Item item ) {
				if (item instanceof DevSummoner || item instanceof DevRemote || item instanceof DevChest || item instanceof Bag) return false;
				return !(item instanceof EquipableItem && ((EquipableItem) item).isEquipped( hero ));
			}
			@Override
			public void onSelect( Item item ) {
				if (item != null){
					Item taken = item.detachAll( hero.belongings.backpack );
					if (taken != null) inside.add( taken );
				}
				mimicMenu( cls, inside );
			}
		} );
	}

	//넣었다가 그만둔 아이템은 가방으로 돌려줌
	private static void giveBack( ArrayList<Item> inside ){
		for (Item i : inside){
			if (!i.collect()) Dungeon.level.drop( i, Dungeon.hero.pos );
		}
		inside.clear();
	}
}
