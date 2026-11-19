/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국 14층 요새의 문지기 놀
 *  - 성문 앞을 지키며, 긴 창으로 2칸 거리까지 찌름. 영웅을 놓치면 자기 자리로 돌아감
 *  - 쓰러지면(또는 더는 놀 편이 아니게 되면) 지키던 성문이 열림
 *  - 처음 쓰러진 문지기가 전리품 창고의 열쇠를 떨어뜨림
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpireLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GnollGatekeeper extends Mob {

	{
		spriteClass = GnollEmpireSprites.Gatekeeper.class;

		HP = HT = 60;
		defenseSkill = 15;

		EXP = 10;
		maxLvl = 18;

		loot = Gold.class;
		lootChance = 0.5f;

		WANDERING = new Guarding();
		state = WANDERING;
	}

	public int post = -1;   //서 있는 자리
	public int gate = -1;   //지키는 성문

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 10, 20 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 22;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 2, 8 );
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		//긴 창: 트인 곳이면 2칸 거리까지
		return Dungeon.level.distance( enemy.pos, pos ) <= 2
				&& new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE ).collisionPos == enemy.pos
				&& new Ballistica( enemy.pos, pos, Ballistica.PROJECTILE ).collisionPos == pos;
	}

	@Override
	protected boolean act() {
		//타락·매혹 등으로 놀 편이 아니게 되면 성문을 열어 줌
		if (alignment != Alignment.ENEMY && gate != -1){
			openGate();
		}
		return super.act();
	}

	@Override
	public void die( Object cause ) {
		openGate();
		if (Dungeon.level instanceof GnollEmpireLevel){
			((GnollEmpireLevel) Dungeon.level).dropStoreKey( pos );
		}
		super.die( cause );
	}

	private void openGate(){
		if (gate != -1 && Dungeon.level instanceof GnollEmpireLevel){
			((GnollEmpireLevel) Dungeon.level).openGate( gate );
		}
		gate = -1;
	}

	@Override
	public String description() {
		return super.description() + (gate != -1 ? "\n\n" + Messages.get( this, "desc_gate" ) : "");
	}

	//자리를 지킴: 영웅을 놓치면 자기 자리로 돌아감
	private class Guarding extends Mob.Wandering {
		@Override
		protected int randomDestination() {
			return post != -1 ? post : super.randomDestination();
		}

		@Override
		protected boolean continueWandering() {
			if (post != -1 && pos == post){
				enemySeen = false;
				spend( TICK );
				return true;
			}
			return super.continueWandering();
		}
	}

	private static final String POST = "post";
	private static final String GATE = "gate";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( POST, post );
		bundle.put( GATE, gate );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		post = bundle.getInt( POST );
		gate = bundle.getInt( GATE );
	}
}
