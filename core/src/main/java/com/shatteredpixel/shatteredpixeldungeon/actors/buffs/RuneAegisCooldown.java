/*
 * Shattered Pixel Dungeon 모드: 룬 성갑의 문장 재충전
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

//문장이 다시 힘을 모으는 동안 붙어 있는 표시용 버프 (아이콘 없음)
public class RuneAegisCooldown extends FlavourBuff {

	public static final float DURATION = 250f;

	{
		revivePersists = true;
	}
}
