/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 몹: 흡혈귀 신부 (14층 희귀 몹, 구울 자리) — 매혹과 흡혈
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NightBlood;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class VampireBride extends Mob {

	{
		spriteClass = VampireCastleSprites.VampireBride.class;

		HP = HT = 45;
		defenseSkill = 20;

		EXP = 8;
		maxLvl = 20;

		loot = Gold.class;
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
		return Random.NormalIntRange( 15, 21 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 24;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 4 );
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );

		//어둠 속에서는 준 피해의 절반을 빨아들임
		if (!VampireCastle.isLit( pos )){
			int reg = Math.min( damage / 2, HT - HP );
			if (reg > 0){
				HP += reg;
				if (sprite != null) sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( reg ), FloatingText.HEALING );
			}
		}

		//네 번에 한 번 매혹
		if (enemy.isAlive() && Random.Int( 4 ) == 0 && enemy.buff( Charm.class ) == null){
			Charm c = Buff.affect( enemy, Charm.class, Charm.DURATION / 2f );
			c.object = id();
			c.ignoreNextHit = true;
			if (enemy.sprite != null) enemy.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f, 4 );
			if (sprite instanceof VampireCastleSprites.VampireBride) ((VampireCastleSprites.VampireBride) sprite).charm();
			if (enemy == Dungeon.hero) Sample.INSTANCE.play( Assets.Sounds.CHARMS );
		}
		return damage;
	}
}
