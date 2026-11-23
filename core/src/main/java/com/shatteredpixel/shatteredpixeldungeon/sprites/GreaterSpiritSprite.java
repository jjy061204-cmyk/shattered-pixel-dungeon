/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 상급 정령(운디네·샐러맨더·노움·실피드) 도트: greater_spirits.png 한 줄에 24칸 (64x64, 영웅 고해상도 도트와 같은 밀도)
 *  대기 0-5, 이동 6-11, 공격 12-17, 사라짐 18-23
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

	public static final int SIZE = 64;
	public static final int PER_ROW = 24;
	public static final float SCALE = HeroSprite.HD_SCALE;

	protected abstract int row();
	protected abstract Emitter.Factory particle();
	protected abstract int color();

	public GreaterSpiritSprite() {
		super();

		texture( Assets.Sprites.GREATER_SPIRITS );
		//64칸 세밀한 도트를 줄여 그림 (영웅의 세밀한 도트와 같은 방식)
		TextureFilm frames = new TextureFilm( texture, SIZE, SIZE );
		HeroSprite.smooth( texture, SCALE );
		scale.set( SCALE );
		int o = row() * PER_ROW;

		idle = new Animation( 8, true );
		idle.frames( frames, o, o+1, o+2, o+3, o+4, o+5 );

		run = new Animation( 12, true );
		run.frames( frames, o+6, o+7, o+8, o+9, o+10, o+11 );

		attack = new Animation( 16, false );
		attack.frames( frames, o+12, o+13, o+14, o+15, o+16, o+17 );

		die = new Animation( 10, false );
		die.frames( frames, o+18, o+19, o+20, o+21, o+22, o+23 );

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
