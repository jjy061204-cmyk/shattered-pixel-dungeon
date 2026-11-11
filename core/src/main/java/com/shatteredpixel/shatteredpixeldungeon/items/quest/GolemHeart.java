/*
 * Shattered Pixel Dungeon 모드: 골렘의 심장 (성소 고유 아이템)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

//성소의 수호 거상이 남기는 룬 핵. 세 번까지 쓸 수 있고, 쓰면 잠시 거상의 갑주를 두름
//(최대 체력의 40% 보호막 + 서서히 사라지는 돌 피부)
public class GolemHeart extends Item {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.GOLEM_HEART;
		unique = true;
		bones = false;
		defaultAction = AC_USE;
	}

	private int charges = 3;

	private static final ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x40E8D8, 1.5f );

	@Override
	public ItemSprite.Glowing glowing() {
		return charges > 0 ? TEAL : null;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (charges > 0) actions.add( AC_USE );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_USE )){
			if (charges <= 0){
				GLog.w( Messages.get( this, "empty" ) );
				return;
			}
			charges--;
			Buff.affect( hero, Barrier.class ).setShield( Math.round( hero.HT * 0.4f ) );
			Barkskin.conditionallyAppend( hero, 10 + hero.lvl / 2, 2 );
			hero.sprite.emitter().burst( RuneMote.FACTORY, 20 );
			hero.sprite.operate( hero.pos );
			Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
			GLog.p( Messages.get( this, "used" ) );
			hero.spendAndNext( 1f );
			updateQuickslot();
		}
	}

	@Override
	public String status() {
		return charges + "/3";
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc" ) + "\n\n" + Messages.get( this, "charges", charges );
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
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "charges", charges );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		charges = bundle.contains( "charges" ) ? bundle.getInt( "charges" ) : 3;
	}
}
