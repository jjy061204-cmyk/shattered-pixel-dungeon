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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ImpSprite;

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

	//==== 모드: 이차원의 성소 퀘스트 (20층) ====

	private static String txt( String key, Object... args ){
		return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( ImpShopkeeper.class, key, args );
	}

	@Override
	public boolean interact( com.shatteredpixel.shatteredpixeldungeon.actors.Char c ) {
		if (c != Dungeon.hero
				|| Dungeon.depth != com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.DEPTH
				|| Dungeon.branch != 0){
			return super.interact( c );
		}
		final ImpShopkeeper imp = this;
		com.watabou.noosa.Game.runOnRenderThread( new com.watabou.utils.Callback() {
			@Override
			public void call() {
				final boolean hasHeart = Dungeon.hero.belongings.getItem( com.shatteredpixel.shatteredpixeldungeon.items.quest.GolemHeart.class ) != null;
				String quest = hasHeart && !com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.rewarded
						? txt( "sanct_give_heart" ) : txt( "sanct_option" );
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
						new com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions( sprite(),
								com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase( name() ),
								txt( "sanct_menu" ), txt( "sanct_shop" ), quest ){
					@Override
					protected void onSelect( int index ) {
						if (index == 0){
							ImpShopkeeper.super.interact( Dungeon.hero );
						} else if (hasHeart && !com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.rewarded){
							giveHeart();
						} else {
							offerSanctuary();
						}
					}
				});
			}
		});
		return true;
	}

	//퀘스트 설명 + 보내 달라고 하기
	private void offerSanctuary(){
		String body;
		if (!com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.given){
			body = txt( "sanct_intro" );
		} else if (!com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.bossDefeated){
			body = txt( "sanct_again" );
		} else {
			body = txt( "sanct_after" );
		}
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				new com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions( sprite(),
						txt( "sanct_title" ), body, txt( "sanct_go" ), txt( "sanct_later" ) ){
			@Override
			protected void onSelect( int index ) {
				if (index == 0){
					com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.given = true;
					com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.travel(
							com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryLevel.BRANCH );
				}
			}
		});
	}

	//골렘의 심장을 건네고 보상 받기
	private void giveHeart(){
		com.shatteredpixel.shatteredpixeldungeon.items.Item heart = Dungeon.hero.belongings.getItem( com.shatteredpixel.shatteredpixeldungeon.items.quest.GolemHeart.class );
		if (heart == null) return;
		heart.detachAll( Dungeon.hero.belongings.backpack );
		com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.rewarded = true;
		int gold = com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryQuest.REWARD_GOLD;
		Dungeon.gold += gold;
		com.shatteredpixel.shatteredpixeldungeon.Statistics.goldCollected += gold;
		com.watabou.noosa.audio.Sample.INSTANCE.play( com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.GOLD );
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				new com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage( sprite(),
						com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase( name() ),
						txt( "sanct_thanks", gold ) ) );
	}
}
