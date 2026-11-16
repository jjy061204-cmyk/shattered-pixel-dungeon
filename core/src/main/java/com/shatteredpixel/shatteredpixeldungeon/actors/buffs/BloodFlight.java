/*
 * Shattered Pixel Dungeon 모드: 뱀파이어의 '핏빛 강하'
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

//진조 흡혈귀의 기술: 6턴 동안 떠올랐다가, 끝나면 내려앉으며 주변 8칸의 적에게 큰 피해를 주고 3턴 동안 불구로 만듦
public class BloodFlight extends BloodWings {

	public static final float TURNS = 6f;

	@Override
	public void detach() {
		Char ch = target;
		super.detach();
		if (ch == null || !ch.isAlive() || !(ShatteredPixelDungeon.scene() instanceof GameScene)) return;

		int lvl = ch instanceof Hero ? ((Hero) ch).lvl : 1;
		boolean hit = false;
		for (int n : PathFinder.NEIGHBOURS8){
			Char e = Actor.findChar( ch.pos + n );
			if (e != null && e.alignment != ch.alignment && e.alignment != Char.Alignment.NEUTRAL){
				e.damage( Random.NormalIntRange( 6 + lvl / 2, 12 + lvl ), this );
				if (e.isAlive()) Buff.prolong( e, Cripple.class, 3f );
				if (e.sprite != null) e.sprite.emitter().burst( BloodParticle.BURST, 6 );
				hit = true;
			}
		}
		com.shatteredpixel.shatteredpixeldungeon.effects.VampireFX.at( ch.pos, com.shatteredpixel.shatteredpixeldungeon.effects.VampireFX.SLAM, 1.0f, 0.5f ).grow( 1.2f );
		Sample.INSTANCE.play( Assets.Sounds.BLAST, hit ? 1f : 0.6f, 1.2f );
		if (ch == Dungeon.hero) PixelScene.shake( 1.5f, 0.3f );
	}

}
