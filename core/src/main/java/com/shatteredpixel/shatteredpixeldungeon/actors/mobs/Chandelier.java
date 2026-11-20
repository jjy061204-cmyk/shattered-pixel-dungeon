/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성: 거미줄 낀 샹들리에 (14층 연회장)
 *  - 늘 켜져 있어 둘레 2칸을 밝힘. 하수인의 손이 닿지 않음
 *  - 던진 무기·마법 탄으로 맞히면 떨어져 3x3에 피해를 주고, 그 자리가 잠시 불타며 밝아짐
 *  - 피해는 영웅과 몹에게 똑같이 (같은 층 함정 수준)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Chandelier extends CastleLight {

	{
		spriteClass = VampireCastleSprites.ChandelierSprite.class;
		lit = true;
		permanent = true;
	}

	private boolean falling = false;

	@Override
	public int radius() {
		return 2;
	}

	@Override
	public boolean isInvulnerable( Class effect ) {
		return false;
	}

	@Override
	public void damage( int dmg, Object src ) {
		if (!isAlive() || falling) return;
		fall();
	}

	@Override
	public boolean interact( Char c ) {
		if (c instanceof Hero){
			GLog.i( Messages.get( Chandelier.class, "too_high" ) );
		}
		return false;
	}

	private void fall(){
		falling = true;
		final int cell = pos;
		boolean seen = Dungeon.level.heroFOV[cell];
		if (seen){
			Sample.INSTANCE.play( Assets.Sounds.ROCKS );
			Sample.INSTANCE.play( Assets.Sounds.BURNING );
			PixelScene.shake( 2, 0.5f );
			GLog.w( Messages.get( Chandelier.class, "falls" ) );
		}

		//먼저 자리를 비우고 3x3에 피해
		HP = 0;
		die( null );

		for (int n : PathFinder.NEIGHBOURS9){
			int c = cell + n;
			if (c < 0 || c >= Dungeon.level.length() || Dungeon.level.solid[c]) continue;
			Char ch = Actor.findChar( c );
			if (ch != null && ch.isAlive() && !(ch instanceof CastleLight)){
				int dmg = Random.NormalIntRange( 8, 16 ) + Dungeon.depth / 2;
				dmg -= ch.drRoll();
				if (dmg > 0) ch.damage( dmg, this );
				if (ch == Dungeon.hero && !ch.isAlive()){
					Dungeon.fail( this );
					GLog.n( Messages.get( Chandelier.class, "killed" ) );
				}
			}
			//떨어진 자리와 사방 칸이 3턴 남짓 불탐 (핏물 칸은 제외)
			boolean ortho = n == 0 || n == -1 || n == 1 || n == Dungeon.level.width() || n == -Dungeon.level.width();
			if (ortho && Dungeon.level.map[c] != Terrain.WATER){
				GameScene.add( Blob.seed( c, n == 0 ? 5 : 3, Fire.class ) );
			}
			if (seen) CellEmitter.get( c ).burst( FlameParticle.FACTORY, 4 );
		}
		if (seen) CellEmitter.center( cell ).burst( Speck.factory( Speck.ROCK ), 6 );
		Dungeon.level.drop( new com.shatteredpixel.shatteredpixeldungeon.items.Gold( Random.IntRange( 10, 30 ) ), cell );
	}

	@Override
	public String name() {
		return Messages.get( Chandelier.class, "name" );
	}

	@Override
	public String description() {
		return Messages.get( Chandelier.class, "desc" );
	}
}
