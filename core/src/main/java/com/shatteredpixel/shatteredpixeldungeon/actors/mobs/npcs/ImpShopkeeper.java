/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ImpSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

public class ImpShopkeeper extends Shopkeeper {

	{
		spriteClass = ImpSprite.class;
	}
	
	private boolean seenBefore = false;
	
	@Override
	protected boolean act() {

		if (!seenBefore && Dungeon.level.heroFOV[pos]) {
			if (Dungeon.hero.buff(AscensionChallenge.class) == null) {
				yell(Messages.get(this, "greetings", Messages.titleCase(Dungeon.hero.name())));
			} else {
				yell(Messages.get(this, "greetings_ascent", Messages.titleCase(Dungeon.hero.name())));
			}
			seenBefore = true;
		}

		return super.act();
	}

	//==== 모드: 이차원의 성소로 가는 게이트 (20층) ====
	//임프가 드워프 금고에서 찾은 이차원 게이트 장치를, 원하면 상점 옆에 설치해 줌

	private static String txt( String key, Object... args ){
		return Messages.get( ImpShopkeeper.class, key, args );
	}

	private static CityBossLevel cityLevel(){
		return Dungeon.level instanceof CityBossLevel ? (CityBossLevel) Dungeon.level : null;
	}

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero || cityLevel() == null || Dungeon.branch != 0){
			return super.interact( c );
		}
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				final boolean installed = cityLevel() != null && cityLevel().hasSanctuaryGate();
				GameScene.show( new WndOptions( sprite(), Messages.titleCase( name() ),
						txt( "sanct_menu" ), txt( "sanct_shop" ), txt( installed ? "sanct_option_after" : "sanct_option" ) ){
					@Override
					protected void onSelect( int index ) {
						if (index == 0){
							ImpShopkeeper.super.interact( Dungeon.hero );
						} else if (installed){
							aboutGate();
						} else {
							offerGate();
						}
					}
				});
			}
		});
		return true;
	}

	//금고에서 찾은 장치 이야기 + 설치해 줄까?
	private void offerGate(){
		GameScene.show( new WndOptions( sprite(), txt( "sanct_title" ), txt( "sanct_intro" ),
				txt( "sanct_install" ), txt( "sanct_later" ) ){
			@Override
			protected void onSelect( int index ) {
				if (index == 0 && cityLevel() != null){
					SanctuaryQuest.given = true;
					cityLevel().installSanctuaryGate();
					yell( txt( "sanct_installed_yell" ) );
					GLog.p( txt( "sanct_installed" ) );
				}
			}
		});
	}

	//이미 설치한 뒤에는 성소 이야기만
	private void aboutGate(){
		String body = SanctuaryQuest.bossDefeated ? txt( "sanct_after" ) : txt( "sanct_again" );
		GameScene.show( new WndTitledMessage( sprite(), txt( "sanct_title" ), body ) );
	}
}
