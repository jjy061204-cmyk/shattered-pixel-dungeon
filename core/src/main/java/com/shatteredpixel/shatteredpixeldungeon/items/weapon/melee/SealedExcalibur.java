/*
 * Shattered Pixel Dungeon 모드: 봉인된 엑스칼리버
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.GoldSparkle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//사슬과 부적으로 봉인된 엑스칼리버. 장착할 수 없고,
//확인된 강화 주문서 8장 + 저주 해제 주문서 1장을 바쳐야 엑스칼리버로 깨어남
public class SealedExcalibur extends Item {

	public static final String AC_UNSEAL = "UNSEAL";

	public static final int NEED_UPGRADE = 8;
	public static final int NEED_REMOVE_CURSE = 1;

	{
		image = ItemSpriteSheet.SEALED_EXCALIBUR;
		defaultAction = AC_UNSEAL;
		unique = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_UNSEAL );
		return actions;
	}

	//확인(감정)된 주문서만 셈
	private static int count( Hero hero, Class<? extends Item> cls ){
		int n = 0;
		for (Item i : hero.belongings.getAllItems( cls )){
			if (i.isIdentified()) n += i.quantity();
		}
		return n;
	}

	private static void consume( Hero hero, Class<? extends Item> cls, int need ){
		for (Item i : hero.belongings.getAllItems( cls )){
			while (need > 0 && i.isIdentified() && i.quantity() > 0){
				i.detach( hero.belongings.backpack );
				need--;
			}
			if (need <= 0) return;
		}
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_UNSEAL )){
			int up = count( hero, ScrollOfUpgrade.class );
			int rc = count( hero, ScrollOfRemoveCurse.class );
			if (up < NEED_UPGRADE || rc < NEED_REMOVE_CURSE){
				GLog.w( Messages.get( this, "not_enough", up, NEED_UPGRADE, rc, NEED_REMOVE_CURSE ) );
				return;
			}

			consume( hero, ScrollOfUpgrade.class, NEED_UPGRADE );
			consume( hero, ScrollOfRemoveCurse.class, NEED_REMOVE_CURSE );

			detach( hero.belongings.backpack );

			Excalibur sword = new Excalibur();
			sword.identify();
			sword.cursed = false;
			if (!sword.collect()){
				Dungeon.level.drop( sword, hero.pos ).sprite.drop();
			}

			//사슬이 끊어지고 금빛이 터져 나옴
			Sample.INSTANCE.play( Assets.Sounds.CHAINS );
			Sample.INSTANCE.play( Assets.Sounds.LEVELUP, 1f, 0.8f );
			if (hero.sprite != null){
				new Flare( 8, 40 ).color( 0xFFD860, true ).show( hero.sprite, 2.5f );
				hero.sprite.emitter().burst( GoldSparkle.BURST, 30 );
			}
			GameScene.flash( 0x80FFE6A0 );
			GLog.p( Messages.get( this, "unsealed" ) );

			hero.spendAndNext( 1f );
		}
	}

	@Override
	public String info() {
		String info = super.info();
		if (Dungeon.hero != null){
			info += "\n\n" + Messages.get( this, "progress",
					count( Dungeon.hero, ScrollOfUpgrade.class ), NEED_UPGRADE,
					count( Dungeon.hero, ScrollOfRemoveCurse.class ), NEED_REMOVE_CURSE );
		}
		return info;
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
		return 300;
	}
}
