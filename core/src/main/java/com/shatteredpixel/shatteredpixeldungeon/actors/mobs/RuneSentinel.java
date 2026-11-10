/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 - 룬 감시석
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SanctuaryGolemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

//허공에 뜬 마름모꼴 석판. 눈에서 룬 광탄을 쏨 (드워프 도시 흑마법사 정도)
public class RuneSentinel extends Mob {

	private static final float TIME_TO_ZAP = 1f;

	{
		spriteClass = SanctuaryGolemSprite.Sentinel.class;

		HP = HT = 60;
		defenseSkill = 24;
		flying = true;

		EXP = 11;
		maxLvl = 26;

		loot = Generator.Category.SCROLL;
		lootChance = 0.2f;

		properties.add( Property.INORGANIC );
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 8, 14 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 30;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 6 );
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return super.canAttack( enemy )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos == enemy.pos;
	}

	@Override
	protected boolean doAttack( Char enemy ) {
		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos != enemy.pos) {
			return super.doAttack( enemy );
		} else {
			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				zap();
				return true;
			}
		}
	}

	//저항 계산에서 근접 공격과 구분하기 위한 표시
	public static class RuneBolt{}

	protected void zap() {
		spend( TIME_TO_ZAP );
		Invisibility.dispel( this );
		Char enemy = this.enemy;
		if (enemy == null) return;
		if (hit( this, enemy, true )) {
			enemy.damage( Random.NormalIntRange( 14, 20 ), new RuneBolt() );
			if (enemy == Dungeon.hero && !enemy.isAlive()) {
				Dungeon.fail( this );
				GLog.n( Messages.get( this, "bolt_kill" ) );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL, enemy.defenseVerb() );
		}
	}

	public void onZapComplete() {
		zap();
		next();
	}
}
