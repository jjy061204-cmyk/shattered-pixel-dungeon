/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 허수아비 (테스터 소환 기기 전용, 공격 시험용)
 *  - 움직이지도 공격하지도 않고, 아무리 맞아도 쓰러지지 않음 (턴마다 체력이 다시 가득 참)
 *  - 회피 0, 방어 0: 들어간 피해가 그대로 숫자로 보임. 상태 이상은 그대로 받음 (아군으로 만들 수는 없음)
 *  - 경험치·전리품·처치 기록 없음. 한 층에 하나, 소환 기기로 치울 수 있음
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TrainingDummySprite;

public class TrainingDummy extends Mob {

	{
		spriteClass = TrainingDummySprite.class;

		HP = HT = 1000;
		defenseSkill = 0;
		EXP = 0;
		maxLvl = -2;
		lootChance = 0;

		state = PASSIVE;

		properties.add( Property.IMMOVABLE );

		immunities.add( Corruption.class );
		immunities.add( Charm.class );
		immunities.add( Amok.class );
	}

	private boolean removing = false;

	@Override
	protected boolean act() {
		//언제나 가만히, 체력은 다시 가득
		alerted = false;
		state = PASSIVE;
		if (HP < HT) HP = HT;
		return super.act();
	}

	@Override
	public int attackSkill( Char target ) {
		return 0;
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return false;
	}

	@Override
	public int drRoll() {
		return 0;
	}

	@Override
	public void beckon( int cell ) {
		//do nothing
	}

	@Override
	public void damage( int dmg, Object src ) {
		super.damage( dmg, src );
		alerted = false;
		if (state != PASSIVE) state = PASSIVE;
	}

	@Override
	public void die( Object cause ) {
		//쓰러지지 않음
		if (!removing){
			HP = HT;
			return;
		}
		super.die( cause );
	}

	//소환 기기로 치움
	public void dismiss(){
		removing = true;
		if (sprite != null){
			CellEmitter.get( pos ).burst( Speck.factory( Speck.WOOL ), 8 );
			sprite.die();
		}
		destroy();
	}

	@Override
	public boolean reset() {
		return true;
	}
}
