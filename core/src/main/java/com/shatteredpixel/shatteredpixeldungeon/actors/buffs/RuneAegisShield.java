/*
 * Shattered Pixel Dungeon 모드: 룬 성갑의 보호막
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

//성갑을 두르는 순간 얻는 보호막. 시간이 지나도 줄지 않고, 맞아서 닳거나 성갑이 풀릴 때만 사라짐
public class RuneAegisShield extends ShieldBuff {
	{
		type = buffType.POSITIVE;
	}
}
