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

	//1단계: 분류 고르기
	private static void showCategories( final int page ){
		final ArrayList<Catalog> cats = new ArrayList<>();
		for (Catalog cat : Catalog.values()){
			if (!itemClasses(cat).isEmpty()) cats.add(cat);
		}

		final int start = page * PER_PAGE;
		final int end = Math.min(start + PER_PAGE, cats.size());
		final boolean hasPrev = page > 0;
		final boolean hasNext = end < cats.size();

		ArrayList<String> options = new ArrayList<>();
		for (int i = start; i < end; i++){
			options.add(Messages.titleCase(cats.get(i).title()));
		}
		final int listSize = options.size();
		if (hasPrev) options.add(txt("prev"));
		if (hasNext) options.add(txt("next"));

		GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.CRYSTAL_CHEST),
				Messages.titleCase(txt("name")),
				txt("choose_category", page + 1, (cats.size() + PER_PAGE - 1) / PER_PAGE),
				options.toArray(new String[0])){
			@Override
			protected void onSelect(int index) {
				if (index < listSize){
					showItems(cats.get(start + index), 0);
				} else if (hasPrev && index == listSize){
					showCategories(page - 1);
				} else {
					showCategories(page + 1);
				}
			}
		});
	}

	//2단계: 분류 안의 아이템 고르기
	private static void showItems( final Catalog cat, final int page ){
		final ArrayList<Class<?>> classes = itemClasses(cat);

		final int start = page * PER_PAGE;
		final int end = Math.min(start + PER_PAGE, classes.size());
		final boolean hasPrev = page > 0;
		final boolean hasNext = end < classes.size();

		final ArrayList<Item> samples = new ArrayList<>();
		ArrayList<String> options = new ArrayList<>();
		for (int i = start; i < end; i++){
			Item sample = (Item) Reflection.newInstance(classes.get(i));
			samples.add(sample);
			options.add(sample == null ? classes.get(i).getSimpleName() : Messages.titleCase(sample.trueName()));
		}
		final int listSize = options.size();
		if (hasPrev) options.add(txt("prev"));
		if (hasNext) options.add(txt("next"));
		options.add(txt("back"));

		GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.CRYSTAL_CHEST),
				Messages.titleCase(cat.title()),
				txt("choose_item", page + 1, (classes.size() + PER_PAGE - 1) / PER_PAGE),
				options.toArray(new String[0])){

			@Override
			protected boolean hasIcon(int index) {
				return index < listSize && samples.get(index) != null;
			}

			@Override
			protected Image getIcon(int index) {
				return new ItemSprite(samples.get(index));
			}

			@Override
			protected void onSelect(int index) {
				if (index < listSize){
					give(classes.get(start + index));
					//여러 개를 연달아 고를 수 있도록 같은 화면을 다시 엶
					showItems(cat, page);
				} else if (hasPrev && index == listSize){
					showItems(cat, page - 1);
				} else if (hasNext && index == listSize + (hasPrev ? 1 : 0)){
					showItems(cat, page + 1);
				} else {
					showCategories(0);
				}
			}
		});
	}

	//선택한 아이템을 감정된 상태로 가방에 넣음 (가방이 가득 차면 발밑에 떨어뜨림)
	private static void give( Class<?> cls ){
		Item item = (Item) Reflection.newInstance(cls);
		if (item == null) return;

		item.identify();
		//현대 세계 코인은 넉넉하게 1000개
		if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.modern.ModernCoin) item.quantity(1000);
		String name = Messages.titleCase(item.name());
		if (item.collect()){
			GLog.p(txt("got", name));
		} else {
			Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop();
			GLog.i(txt("dropped", name));
		}
	}
}
