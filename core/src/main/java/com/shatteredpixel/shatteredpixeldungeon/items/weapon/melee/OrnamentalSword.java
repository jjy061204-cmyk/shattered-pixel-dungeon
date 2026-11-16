/*
 * Shattered Pixel Dungeon 모드: 영웅 고유 공격 이펙트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CrimsonGrail;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//모드: 뱀파이어의 시작 무기 '장식용 검'
// 레이피어와 같은 1단계 세검(돌진 능력, 방어 +1)이지만, 칼자루의 혈석이 벤 피를 머금어
// 살아 있는 적을 맞힐 때마다 핏빛 성배에 생명력이 조금(0.25) 고임
public class OrnamentalSword extends Rapier {

	{
		image = ItemSpriteSheet.ORNAMENTAL_SWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.2f;

		tier = 1;
		bones = false;
	}

	public static final float LIFE_PER_HIT = 0.25f;

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		if (attacker instanceof Hero && CrimsonGrail.isLiving( defender )){
			CrimsonGrail.gainLife( (Hero) attacker, LIFE_PER_HIT );
		}
		return super.proc( attacker, defender, damage );
	}
}
