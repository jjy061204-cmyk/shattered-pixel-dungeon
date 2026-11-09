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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.modern.ModernCoin;
import com.shatteredpixel.shatteredpixeldungeon.levels.ModernLevel;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

public class CurrencyIndicator extends Component {

	private static final float TIME	= 2f;
	
	private int lastGold = 0;
	private int lastEnergy = 0;
	private int lastCoins = 0;
	
	private BitmapText gold;
	private BitmapText energy;
	//모드: 현대 세계 코인
	private BitmapText coins;
	
	private float goldTime;
	private float energyTime;
	private float coinTime;

	public static boolean showGold = false;
	
	@Override
	protected void createChildren() {
		gold = new BitmapText( PixelScene.pixelFont);
		gold.text( Integer.toString(lastGold) );
		gold.measure();
		gold.hardlight( 0xFFFF00 );
		add( gold );

		energy = new BitmapText( PixelScene.pixelFont);
		energy.text( Integer.toString(lastEnergy) );
		energy.measure();
		energy.hardlight( 0x44CCFF );
		add( energy );
		
		coins = new BitmapText( PixelScene.pixelFont);
		coins.text( lastCoins + " COIN" );
		coins.measure();
		coins.hardlight( 0x66FFCC );
		add( coins );
		
		gold.visible = energy.visible = coins.visible = false;
	}
	
	@Override
	protected void layout() {
		gold.x = x+1;
		gold.y = top() + 1;

		energy.x = x+1;
		if (gold.visible) {
			energy.y = top() + energy.height() - 1;
		} else {
			energy.y = top() + 1;
		}

		coins.x = x+1;
		int lines = (gold.visible ? 1 : 0) + (energy.visible ? 1 : 0);
		coins.y = top() + 1 + lines * (coins.height() - 2);
	}
	
	@Override
	public void update() {
		super.update();
		
		if (gold.visible) {
			
			goldTime -= Game.elapsed;
			if (goldTime > 0) {
				gold.alpha( goldTime > TIME / 2 ? 1f : goldTime * 2 / TIME );
			} else {
				gold.visible = false;
			}
			
		}

		if (energy.visible) {

			energyTime -= Game.elapsed;
			if (energyTime > 0) {
				energy.alpha( energyTime > TIME / 2 ? 1f : energyTime * 2 / TIME );
			} else {
				energy.visible = false;
			}

		}

		if (Dungeon.gold != lastGold) {
			
			lastGold = Dungeon.gold;
			
			gold.text( Integer.toString(lastGold) );
			gold.measure();
			
			gold.visible = true;
			goldTime = TIME;
			
			layout();
		}

		if (Dungeon.energy != lastEnergy) {
			lastEnergy = Dungeon.energy;

			energy.text( Integer.toString(lastEnergy) );
			energy.measure();

			energy.visible = true;
			energyTime = TIME;

			layout();
		}

		if (coins.visible) {

			coinTime -= Game.elapsed;
			if (coinTime > 0) {
				coins.alpha( coinTime > TIME / 2 ? 1f : coinTime * 2 / TIME );
			} else {
				coins.visible = false;
			}

		}

		int c = ModernCoin.count();
		if (c != lastCoins) {
			lastCoins = c;

			coins.text( lastCoins + " COIN" );
			coins.measure();

			coins.visible = true;
			coinTime = TIME;

			layout();
		}

		if (showGold){
			if (!gold.visible){
				gold.visible = true;
				layout();
			}
			goldTime = TIME/2;
			//현대 세계 가게에서는 코인도 함께 보여 줌
			if (Dungeon.level instanceof ModernLevel){
				if (!coins.visible){
					coins.visible = true;
					layout();
				}
				coinTime = TIME/2;
			}
		}

	}
}
