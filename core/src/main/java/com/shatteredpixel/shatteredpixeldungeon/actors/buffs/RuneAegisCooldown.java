/*
 * Shattered Pixel Dungeon 모드: (옛 저장 파일 호환용) 룬 성갑의 문장 재충전
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

//유물로 바뀌기 전 버전의 대기 버프. 옛 저장을 불러오면 바로 사라짐
public class RuneAegisCooldown extends Buff {
	public int left = 0;

	@Override
	public boolean act() {
		detach();
		return true;
	}
}
