/*
 * Shattered Pixel Dungeon 모드: 현대 물품
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.modern;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//현대 세계에서 파는 소모품의 공통 틀: '사용'하면 1턴이 걸리고 하나가 줄어듦
public abstract class ModernConsumable extends Item {

	public static final String AC_USE = "USE";
	protected static final float TIME_TO_USE = 1f;

	{
		defaultAction = AC_USE;
		stackable = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_USE );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_USE )) {
			if (!canUse( hero )) return;
			detach( hero.belongings.backpack );
			Sample.INSTANCE.play( useSound() );
			String msg = Messages.get( this, "use_msg" );
			if (!msg.equals( Messages.NO_TEXT_FOUND )) GLog.i( msg );
			apply( hero );
			Catalog.countUse( getClass() );
			hero.spendAndNext( TIME_TO_USE );
		}
	}

	protected boolean canUse( Hero hero ){
		return true;
	}

	protected String useSound(){
		return Assets.Sounds.ITEM;
	}

	protected abstract void apply( Hero hero );

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
