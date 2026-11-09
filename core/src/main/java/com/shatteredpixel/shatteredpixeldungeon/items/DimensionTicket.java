/*
 * Shattered Pixel Dungeon 모드
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.ModernLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//차원 이동 카드: 던전에서 사용하면 현대 세계 층으로 이동하고, 돌아갈 위치가 기록된 귀환 카드를 받음.
//현대 세계 층은 이 카드로만 들어갈 수 있음.
public class DimensionTicket extends Item {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.DIMENSION_TICKET;
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

			if (Dungeon.level instanceof ModernLevel) {
				GLog.w( Messages.get(this, "already_here") );
				return;
			}
			//본 던전에서만, 그리고 층간 이동이 막히지 않은 곳(보스전 중, 아뮬렛 소지 등 제외)에서만 사용 가능
			if (Dungeon.branch != 0 || !Dungeon.interfloorTeleportAllowed()) {
				GLog.w( Messages.get(this, "cant_use") );
				return;
			}

			//돌아올 위치를 기록한 귀환 카드 지급
			DimensionReturnTicket ret = new DimensionReturnTicket();
			ret.setReturn( Dungeon.depth, Dungeon.branch, hero.pos );
			if (!ret.collect( hero.belongings.backpack )) {
				GLog.w( Messages.get(this, "bag_full") );
				return;
			}

			detach( hero.belongings.backpack );
			Catalog.countUse( getClass() );
			Sample.INSTANCE.play( Assets.Sounds.TELEPORT );

			Level.beforeTransition();
			Invisibility.dispel();
			InterlevelScene.mode = InterlevelScene.Mode.RETURN;
			InterlevelScene.returnDepth = Dungeon.depth;
			InterlevelScene.returnBranch = ModernLevel.BRANCH;
			InterlevelScene.returnPos = -1;
			Game.switchScene( InterlevelScene.class );
		}
	}

	private static final ItemSprite.Glowing BLUE = new ItemSprite.Glowing( 0x66CCFF );

	@Override
	public ItemSprite.Glowing glowing() {
		return BLUE;
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
		return 40 * quantity;
	}
}
