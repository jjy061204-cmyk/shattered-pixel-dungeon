/*
 * Shattered Pixel Dungeon 모드: 수호의 반지가 만드는 보호막
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

//시간이 지나도 줄지 않는 보호막. 0이 되어도 갑옷이 유지되는 동안은 남아서 다시 차오름
public class RuneAegisShield extends ShieldBuff {
	{
		type = buffType.POSITIVE;
		detachesAtZero = false;
	}
}
