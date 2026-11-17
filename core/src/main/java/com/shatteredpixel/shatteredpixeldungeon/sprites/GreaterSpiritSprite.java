/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 상급 정령(운디네·샐러맨더·노움·실피드) 도트: greater_spirits.png 한 줄에 14칸
 *  대기 0-3, 이동 4-7, 공격 8-10, 사라짐 11-13
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WindParticle;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;

public abstract class GreaterSpiritSprite extends MobSprite {

	private static final int PER_ROW = 14;

	protected abstract int row();
	protected abstract Emitter.Factory particle();
	protected abstract int color();

	public GreaterSpiritSprite() {
		super();

		texture( Assets.Sprites.GREATER_SPIRITS );
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		int o = row() * PER_ROW;

		idle = new Animation( 6, true );
		idle.frames( frames, o, o+1, o+2, o+3 );

		run = new Animation( 10, true );
		run.frames( frames, o+4, o+5, o+6, o+7 );

		attack = new Animation( 14, false );
		attack.frames( frames, o+8, o+9, o+10, o );

		die = new Animation( 10, false );
		die.frames( frames, o+11, o+12, o+13 );

		play( idle );
	}

	@Override
	public void die() {
		super.die();
		emitter().burst( particle(), 12 );
		emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
	}

	@Override
	public int blood() {
		return 0xFF000000 | color();
	}

	public static class Undine extends GreaterSpiritSprite {
		@Override protected int row() { return 0; }
		@Override protected Emitter.Factory particle() { return FlowParticle.FACTORY; }
		@Override protected int color() { return 0x55B8FF; }
	}

	public static class Salamander extends GreaterSpiritSprite {
		@Override protected int row() { return 1; }
		@Override protected Emitter.Factory particle() { return FlameParticle.FACTORY; }
		@Override protected int color() { return 0xFF6622; }
	}

	public static class Gnome extends GreaterSpiritSprite {
		@Override protected int row() { return 2; }
		@Override protected Emitter.Factory particle() { return EarthParticle.FACTORY; }
		@Override protected int color() { return 0xB08850; }
	}

	public static class Sylph extends GreaterSpiritSprite {
		@Override protected int row() { return 3; }
		@Override protected Emitter.Factory particle() { return WindParticle.FACTORY; }
		@Override protected int color() { return 0xC8FFE8; }
	}
}
