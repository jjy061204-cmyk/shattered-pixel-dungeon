/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 원소 정령이 쓰러진 자리에 잠시 남는 정령의 기운: 정령사가 숲의 완드로 강한 정령을 깃들게 할 수 있음
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public abstract class SpiritRemnant extends Blob {

	@SuppressWarnings("unchecked")
	public static final Class<? extends SpiritRemnant>[] TYPES = new Class[]{
			Flame.class, Frost.class, Shock.class, Chaos.class };

	//남아 있는 턴 수
	public static final int DURATION = 30;

	public abstract ForestWand.Spirit spirit();

	@Override
	protected void evolve() {
		int cell;
		for (int i = area.left; i < area.right; i++) {
			for (int j = area.top; j < area.bottom; j++) {
				cell = i + j * Dungeon.level.width();
				off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;
				volume += off[cell];
			}
		}
	}

	@Override
	public String tileDesc() {
		return Messages.get( SpiritRemnant.class, "desc", spirit().title() );
	}

	public static class Flame extends SpiritRemnant {
		@Override public ForestWand.Spirit spirit() { return ForestWand.Spirit.FLAME; }
		@Override public void use( BlobEmitter emitter ) { super.use( emitter ); emitter.pour( FlameParticle.FACTORY, 0.1f ); }
	}

	public static class Frost extends SpiritRemnant {
		@Override public ForestWand.Spirit spirit() { return ForestWand.Spirit.FROST; }
		@Override public void use( BlobEmitter emitter ) { super.use( emitter ); emitter.pour( SnowParticle.FACTORY, 0.1f ); }
	}

	public static class Shock extends SpiritRemnant {
		@Override public ForestWand.Spirit spirit() { return ForestWand.Spirit.SHOCK; }
		@Override public void use( BlobEmitter emitter ) { super.use( emitter ); emitter.pour( SparkParticle.STATIC, 0.12f ); }
	}

	public static class Chaos extends SpiritRemnant {
		@Override public ForestWand.Spirit spirit() { return ForestWand.Spirit.CHAOS; }
		@Override public void use( BlobEmitter emitter ) { super.use( emitter ); emitter.pour( RainbowParticle.BURST, 0.15f ); }
	}
}
