/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 몹: 성의 호위 기사 (DM-200 자리) — 지금 노리는 적의 공격 30%를 방패로 막고, 걸음이 느림
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NightBlood;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class CastleKnight extends Mob {

	{
		spriteClass = VampireCastleSprites.CastleKnight.class;

		HP = HT = 80;
		defenseSkill = 12;

		EXP = 9;
		maxLvl = 17;

		baseSpeed = 0.8f;

		loot = Random.oneOf( Generator.Category.WEAPON, Generator.Category.ARMOR );
		lootChance = 0.2f;

		properties.add( Property.UNDEAD );
	}

	@Override
	protected void onAdd() {
		super.onAdd();
		NightBlood.grant( this );
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 10, 25 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 20;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 8 );
	}

	@Override
	public int defenseProc( Char attacker, int damage ) {
		//방패는 정면(지금 노리는 적)만 막음
		if (attacker != null && attacker == enemy && state == HUNTING && buff( Paralysis.class ) == null
				&& damage > 0 && Random.Float() < 0.3f){
			if (sprite instanceof VampireCastleSprites.CastleKnight){
				((VampireCastleSprites.CastleKnight) sprite).block();
			}
			if (sprite != null && sprite.visible){
				sprite.showStatus( CharSprite.NEUTRAL, Messages.get( CastleKnight.class, "block" ) );
				Sample.INSTANCE.play( Assets.Sounds.HIT_PARRY );
			}
			return 0;
		}
		return super.defenseProc( attacker, damage );
	}

	@Override
	public float lootChance(){
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
}
