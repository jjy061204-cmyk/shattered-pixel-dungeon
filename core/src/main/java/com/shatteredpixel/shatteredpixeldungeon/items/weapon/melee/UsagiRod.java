/*
 * Shattered Pixel Dungeon 모드: 토벌봉
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

//2티어 무기. 소검과 같은 공격력이고, 3번 때릴 때마다 주변 3x3칸에 폭발을 일으킴.
//폭발은 영웅과 아군에게 피해를 주지 않고, 바닥의 아이템도 터뜨리지 않으며, 풀만 부숨.
public class UsagiRod extends Shortsword {

	public static final int   HITS_PER_BLAST = 3;
	public static final float BLAST_MULT     = 1.5f;

	//지금까지 몇 번 때렸는지 (3이 되면 폭발 후 0으로)
	private int hits = 0;

	{
		image = ItemSpriteSheet.USAGI_ROD;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1.2f;

		tier = 2;
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		damage = super.proc( attacker, defender, damage );

		hits++;
		if (hits >= HITS_PER_BLAST) {
			hits = 0;
			//맞은 적이 받는 폭발 피해는 이번 공격 피해에 더해 줌
			damage += blast( attacker, defender );
		}
		updateQuickslot();
		return damage;
	}

	//defender 위치를 중심으로 폭발. defender가 받을 폭발 피해를 돌려줌
	private int blast( Char attacker, Char defender ){
		int center = defender.pos;
		int blastDamage = Math.round( damageRoll( attacker ) * BLAST_MULT );
		int defenderDamage = 0;
		boolean terrainChanged = false;

		Sample.INSTANCE.play( Assets.Sounds.BLAST );
		if (Dungeon.level.heroFOV[center]) {
			CellEmitter.center( center ).burst( BlastParticle.FACTORY, 30 );
		}

		for (int offset : PathFinder.NEIGHBOURS9) {
			int cell = center + offset;
			if (cell < 0 || cell >= Dungeon.level.length()) continue;

			if (Dungeon.level.heroFOV[cell]) {
				CellEmitter.get( cell ).burst( SmokeParticle.FACTORY, 4 );
			}

			//짧은 풀, 긴 풀, 밟힌 풀은 빈 바닥으로
			int terrain = Dungeon.level.map[cell];
			if (terrain == Terrain.GRASS || terrain == Terrain.HIGH_GRASS || terrain == Terrain.FURROWED_GRASS) {
				Level.set( cell, Terrain.EMPTY );
				GameScene.updateMap( cell );
				terrainChanged = true;
			}

			//영웅과 아군은 피해 없음
			Char ch = Actor.findChar( cell );
			if (ch == null || ch == attacker || !ch.isAlive() || ch.alignment == Char.Alignment.ALLY) {
				continue;
			}

			if (ch == defender) {
				defenderDamage = blastDamage;
			} else {
				ch.damage( blastDamage, this );
			}
		}

		if (terrainChanged) {
			Dungeon.observe();
		}
		return defenderDamage;
	}

	@Override
	public String info() {
		String info = super.info();
		info += "\n\n" + Messages.get( this, "blast_counter", HITS_PER_BLAST - hits );
		return info;
	}

	private static final String HITS = "usagi_rod_hits";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( HITS, hits );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		hits = bundle.getInt( HITS );
	}
}
