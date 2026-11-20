/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 몹: 피의 사제 (놀 주술사 자리) — 피의 화살로 출혈을 걸고, 어둠 속 가까운 흡혈 몹을 치유함
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NightBlood;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.watabou.utils.Bundle;

public class BloodPriest extends Shaman {

	{
		spriteClass = VampireCastleSprites.BloodPriest.class;
	}

	private int healCooldown = 2;

	@Override
	protected void onAdd() {
		super.onAdd();
		NightBlood.grant( this );
	}

	@Override
	protected void debuff( Char enemy ) {
		Buff.affect( enemy, Bleeding.class ).set( 5 );
	}

	@Override
	protected boolean act() {
		if (healCooldown > 0) healCooldown--;
		if (state == HUNTING && healCooldown <= 0 && !VampireCastle.isLit( pos )){
			Mob best = null;
			for (Mob m : Dungeon.level.mobs){
				if (m == this || !m.isAlive() || m.alignment != alignment || m.buff( NightBlood.class ) == null) continue;
				if (m.HP > m.HT * 0.7f || VampireCastle.isLit( m.pos )) continue;
				if (Dungeon.level.distance( pos, m.pos ) > 4 || !fieldOfView[m.pos]) continue;
				if (best == null || m.HP * best.HT < best.HP * m.HT) best = m;
			}
			if (best != null){
				int heal = Math.min( 10, best.HT - best.HP );
				best.HP += heal;
				if (best.sprite != null && Dungeon.level.heroFOV[best.pos]){
					best.sprite.emitter().burst( BloodParticle.BURST, 8 );
					best.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
				}
				//사제와 치유받는 몹 사이에 붉은 빛줄기
				if (sprite != null && best.sprite != null && sprite.parent != null
						&& (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[best.pos])){
					com.shatteredpixel.shatteredpixeldungeon.effects.Beam beam =
							new com.shatteredpixel.shatteredpixeldungeon.effects.Beam.LightRay( sprite.center(), best.sprite.center() );
					beam.hardlight( 0.85f, 0.1f, 0.15f );
					sprite.parent.add( beam );
				}
				healCooldown = 6;
				spend( TICK );
				return true;
			}
		}
		return super.act();
	}

	private static final String HEAL_CD = "heal_cd";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( HEAL_CD, healCooldown );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		healCooldown = bundle.getInt( HEAL_CD );
	}
}
