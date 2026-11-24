/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 수정 광맥 몹: 드워프들이 깨운 살아 있는 수정에 잠식된 동굴 생물들
 *  - 능력과 수치는 원작 동굴 몹 그대로 (루트끼리 난이도를 맞춤), 모습과 이름·설명만 다름
 *  - 원작 희귀 몹 자리도 그대로: 수정 브루트 → 수정 갑주 브루트, 수정 DM-200 → 수정 DM-201
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.levels.Routes;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalVeinSprites;
import com.watabou.utils.Random;

public class CrystalVeinMobs {

	//지금 고른 광산 루트가 수정 광맥인지
	public static boolean active(){
		return Routes.caves() == Routes.Caves.CRYSTAL_VEIN;
	}

	public static class CrystalBat extends Bat {
		{ spriteClass = CrystalVeinSprites.Bat.class; }
	}

	public static class CrystalBrute extends Brute {
		{ spriteClass = CrystalVeinSprites.Brute.class; }
	}

	public static class CrystalArmoredBrute extends ArmoredBrute {
		{ spriteClass = CrystalVeinSprites.ArmoredBrute.class; }
	}

	public static class CrystalRedShaman extends Shaman.RedShaman {
		{ spriteClass = CrystalVeinSprites.RedShaman.class; }
	}

	public static class CrystalBlueShaman extends Shaman.BlueShaman {
		{ spriteClass = CrystalVeinSprites.BlueShaman.class; }
	}

	public static class CrystalPurpleShaman extends Shaman.PurpleShaman {
		{ spriteClass = CrystalVeinSprites.PurpleShaman.class; }
	}

	public static class CrystalSpinner extends Spinner {
		{ spriteClass = CrystalVeinSprites.Spinner.class; }
	}

	public static class CrystalDM200 extends DM200 {
		{ spriteClass = CrystalVeinSprites.DM200.class; }
	}

	public static class CrystalDM201 extends DM201 {
		{ spriteClass = CrystalVeinSprites.DM201.class; }
	}

	//원작 주술사 비율 그대로 (붉은 40% · 푸른 40% · 보라 20%)
	public static Class<? extends Shaman> randomShaman(){
		float roll = Random.Float();
		if (roll < 0.4f){
			return CrystalRedShaman.class;
		} else if (roll < 0.8f){
			return CrystalBlueShaman.class;
		} else {
			return CrystalPurpleShaman.class;
		}
	}
}
