/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 놀 투척병 (원작 광산의 동굴 거미 자리)
 *  - 3칸 거리에서 창을 던짐. 맞으면 2턴 불구
 *  - 바로 옆에 붙으면 가끔 한 걸음 물러나 거리를 벌림 (계속 도망치지는 않음)
 *  - 망루 칸에 서면 사거리 +1
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpireLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GnollJavelineer extends Mob {

	{
		spriteClass = GnollEmpireSprites.Javelineer.class;

		HP = HT = 50;
		defenseSkill = 17;

		EXP = 9;
		maxLvl = 17;

		loot = MysteryMeat.class;
		lootChance = 0.125f;
	}

	private int retreatCooldown = 0;
	//지금 하는 공격이 투창인지 (맞으면 불구)
	private boolean throwing = false;

	public int range(){
		return 3 + (GnollEmpireLevel.isTower( pos ) ? 1 : 0);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 9, 18 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 22;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 6 );
	}

	@Override
	protected boolean act() {
		if (retreatCooldown > 0) retreatCooldown--;
		return super.act();
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		if (Dungeon.level.adjacent( pos, enemy.pos )) return true;
		return Dungeon.level.distance( pos, enemy.pos ) <= range()
				&& new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE ).collisionPos == enemy.pos;
	}

	@Override
	protected boolean doAttack( Char enemy ) {
		//바로 옆이면 가끔 한 걸음 물러나 던질 거리를 만듦
		if (Dungeon.level.adjacent( pos, enemy.pos ) && retreatCooldown <= 0 && !rooted && Random.Int( 2 ) == 0){
			int oldPos = pos;
			if (getFurther( enemy.pos )){
				retreatCooldown = 5;
				spend( 1 / speed() );
				return moveSprite( oldPos, pos );
			}
		}
		throwing = !Dungeon.level.adjacent( pos, enemy.pos );
		return super.doAttack( enemy );
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (throwing && enemy.isAlive()){
			Buff.prolong( enemy, Cripple.class, 2f );
		}
		return damage;
	}

	@Override
	public void onAttackComplete() {
		super.onAttackComplete();
		throwing = false;
	}

	private static final String RETREAT = "retreat_cooldown";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( RETREAT, retreatCooldown );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		retreatCooldown = bundle.getInt( RETREAT );
	}
}
