/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령왕(불·물·바람·땅) 도트: spirit_kings.png 한 줄에 24칸 (64x64). 로브를 두른 얼굴 없는 형체로, 두건 속엔 속성 구체나 룬만 빛남
 *  그림은 그대로 두고 영웅과 같은 배율로 줄여 그림
 *  대기 0-5(떠 있는 채로 속성 빛이 은은하게 웅웅 맥동), 이동 6-11(로브 자락이 뒤로 끌림), 공격 12-17, 사라짐 18-23
 *  물의 정령왕은 공격 장면으로 물의 창을 던짐
 *  곁에 늘 그 속성의 입자가 은은하게 피어오름
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SpiritKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WindParticle;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public abstract class SpiritKingSprite extends MobSprite {

	public static final int SIZE = 64;
	public static final int PER_ROW = 24;
	public static final float SCALE = HeroSprite.HD_SCALE;

	protected abstract int row();
	protected abstract Emitter.Factory particle();
	protected abstract int color();

	private Emitter aura;

	public SpiritKingSprite() {
		super();

		texture( Assets.Sprites.SPIRIT_KINGS );
		TextureFilm frames = new TextureFilm( texture, SIZE, SIZE );
		HeroSprite.smooth( texture, SCALE );
		scale.set( SCALE );
		int o = row() * PER_ROW;

		//천천히 떠오르내리며 웅웅 빛이 차오르는 대기 동작
		idle = new Animation( 6, true );
		idle.frames( frames, o, o+1, o+2, o+3, o+4, o+5 );

		//로브 자락을 끌며 미끄러지듯 이동
		run = new Animation( 10, true );
		run.frames( frames, o+6, o+7, o+8, o+9, o+10, o+11 );

		attack = new Animation( 15, false );
		attack.frames( frames, o+12, o+13, o+14, o+15, o+16, o+17 );

		zap = attack.clone();

		die = new Animation( 10, false );
		die.frames( frames, o+18, o+19, o+20, o+21, o+22, o+23 );

		play( idle );
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );
		if (aura == null){
			aura = emitter();
			aura.pour( particle(), 0.18f );
		}
	}

	@Override
	public void update() {
		super.update();
		if (aura != null) aura.visible = visible;
	}

	@Override
	public void die() {
		super.die();
		if (aura != null){
			aura.on = false;
			aura = null;
		}
		emitter().burst( particle(), 20 );
		emitter().burst( Speck.factory( Speck.LIGHT ), 6 );
	}

	@Override
	public void kill() {
		super.kill();
		if (aura != null){
			aura.on = false;
			aura = null;
		}
	}

	@Override
	public int blood() {
		return 0xFF000000 | color();
	}

	public static class Fire extends SpiritKingSprite {
		@Override protected int row() { return 0; }
		@Override protected Emitter.Factory particle() { return FlameParticle.FACTORY; }
		@Override protected int color() { return 0xFF6622; }
	}

	public static class Water extends SpiritKingSprite {
		@Override protected int row() { return 1; }
		@Override protected Emitter.Factory particle() { return FlowParticle.FACTORY; }
		@Override protected int color() { return 0x55B8FF; }

		//물의 창을 던짐
		@Override
		public void zap( int cell ) {
			super.zap( cell );
			MagicMissile.boltFromChar( parent, MagicMissile.FROST, this, cell, new Callback() {
				@Override
				public void call() {
					if (ch instanceof SpiritKing.WaterKing) ((SpiritKing.WaterKing) ch).onZapComplete();
				}
			} );
			Sample.INSTANCE.play( Assets.Sounds.ZAP, 1f, 0.8f );
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == zap) idle();
			super.onComplete( anim );
		}
	}

	public static class Wind extends SpiritKingSprite {
		@Override protected int row() { return 2; }
		@Override protected Emitter.Factory particle() { return WindParticle.FACTORY; }
		@Override protected int color() { return 0xC8FFE8; }
	}

	public static class Earth extends SpiritKingSprite {
		@Override protected int row() { return 3; }
		@Override protected Emitter.Factory particle() { return EarthParticle.SMALL; }
		@Override protected int color() { return 0xB08850; }
	}
}
