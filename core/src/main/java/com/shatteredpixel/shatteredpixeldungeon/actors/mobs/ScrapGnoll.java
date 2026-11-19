/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 고철 갑주 놀 (원작 광산의 DM-200 자리)
 *  - DM 기계의 장갑을 두른 놀. 방어가 높고 느림. DM-200과 같은 전리품(무기·갑옷)
 *  - 쓰러지면 그 자리에 고철 부스러기를 남김
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class ScrapGnoll extends Mob {

	{
		spriteClass = GnollEmpireSprites.ScrapGnoll.class;

		HP = HT = 80;
		defenseSkill = 11;
		baseSpeed = 0.8f;

		EXP = 9;
		maxLvl = 17;

		loot = Random.oneOf( Generator.Category.WEAPON, Generator.Category.ARMOR );
		lootChance = 0.2f; //DM-200과 같음, lootChance() 참고
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 10, 24 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 20;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 3, 11 );
	}

	@Override
	public float lootChance() {
		return super.lootChance() * (float)Math.pow( 1/3f, Dungeon.LimitedDrops.DM200_EQUIP.count );
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.DM200_EQUIP.count++;
		if (loot == Generator.Category.WEAPON){
			return Generator.randomWeapon( 4, true );
		} else {
			return Generator.randomArmor( 4 );
		}
	}

	@Override
	public void die( Object cause ) {
		int cell = pos;
		super.die( cause );
		//고철 부스러기 (지나다닐 수 있는 바닥 장식)
		int t = Dungeon.level.map[cell];
		if (t == Terrain.EMPTY || t == Terrain.GRASS || t == Terrain.EMBERS){
			Level.set( cell, Terrain.EMPTY_DECO );
			GameScene.updateMap( cell );
		}
		if (Dungeon.level.heroFOV[cell]){
			CellEmitter.get( cell ).burst( Speck.factory( Speck.ROCK ), 6 );
			Sample.INSTANCE.play( Assets.Sounds.ROCKS, 0.5f, 1.4f );
		}
	}
}
