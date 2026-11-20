/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성: 흡혈 몹이 지닌 '밤의 피'
 *  - 어두운 칸에서 매 턴 체력 1 회복 (핏물 웅덩이 위라면 1 더)
 *  - 밝은 칸에서는 회복하지 않음 (명중 감소는 Char.hit에서)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle;

public class NightBlood extends Buff {

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	//어둠 속 회복량 (흡혈 군주의 핏빛 달 단계에서는 늘어남)
	public int power = 1;

	public static void grant( Char ch ){
		if (ch != null && ch.buff( NightBlood.class ) == null){
			Buff.affect( ch, NightBlood.class );
		}
	}

	@Override
	public boolean act() {
		boolean mist = target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.VampireLord
				&& ((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.VampireLord) target).misted;
		if (!mist && target.isAlive() && target.HP < target.HT && !VampireCastle.isLit( target.pos )){
			int heal = power + (VampireCastle.inBlood( target.pos ) ? 1 : 0);
			target.HP = Math.min( target.HT, target.HP + heal );
		}
		spend( TICK );
		return true;
	}

	private static final String POWER = "power";

	@Override
	public void storeInBundle( com.watabou.utils.Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( POWER, power );
	}

	@Override
	public void restoreFromBundle( com.watabou.utils.Bundle bundle ) {
		super.restoreFromBundle( bundle );
		power = Math.max( 1, bundle.getInt( POWER ) );
	}
}
