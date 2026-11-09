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
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.ModernLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

//귀환 카드: 현대 세계 층 안에서만 쓸 수 있고, 차원 이동 카드를 썼던 자리로 돌아감.
//현대 세계 층에서 나가는 방법은 이 카드뿐임.
public class DimensionReturnTicket extends Item {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.DIMENSION_RETURN_TICKET;
		defaultAction = AC_USE;
		//카드마다 돌아갈 위치가 다르므로 겹치지 않음
		stackable = false;
		unique = true;
	}

	private int returnDepth = -1;
	private int returnBranch = 0;
	private int returnPos = -1;

	public void setReturn( int depth, int branch, int pos ){
		returnDepth = depth;
		returnBranch = branch;
		returnPos = pos;
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

			if (!(Dungeon.level instanceof ModernLevel)) {
				GLog.w( Messages.get(this, "only_modern") );
				return;
			}

			detach( hero.belongings.backpack );
			Sample.INSTANCE.play( Assets.Sounds.TELEPORT );

			Level.beforeTransition();
			Invisibility.dispel();
			InterlevelScene.mode = InterlevelScene.Mode.RETURN;
			if (returnDepth > 0) {
				InterlevelScene.returnDepth = returnDepth;
				InterlevelScene.returnBranch = returnBranch;
				InterlevelScene.returnPos = returnPos;
			} else {
				//기록이 없으면 지금 층 번호의 본 던전 입구로
				InterlevelScene.returnDepth = Dungeon.depth;
				InterlevelScene.returnBranch = 0;
				InterlevelScene.returnPos = -1;
			}
			Game.switchScene( InterlevelScene.class );
		}
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (returnDepth > 0) {
			desc += "\n\n" + Messages.get(this, "dest", returnDepth);
		}
		return desc;
	}

	private static final ItemSprite.Glowing ORANGE = new ItemSprite.Glowing( 0xFFAA44 );

	@Override
	public ItemSprite.Glowing glowing() {
		return ORANGE;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	//팔 수 없음 (팔면 갇히게 되므로)
	@Override
	public int value() {
		return 0;
	}

	private static final String DEPTH  = "return_depth";
	private static final String BRANCH = "return_branch";
	private static final String POS    = "return_pos";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DEPTH, returnDepth );
		bundle.put( BRANCH, returnBranch );
		bundle.put( POS, returnPos );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		returnDepth = bundle.getInt( DEPTH );
		returnBranch = bundle.getInt( BRANCH );
		returnPos = bundle.getInt( POS );
	}
}
