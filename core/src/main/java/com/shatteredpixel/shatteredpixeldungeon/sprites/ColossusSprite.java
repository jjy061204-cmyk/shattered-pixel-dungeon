/*
 * Shattered Pixel Dungeon 모드: 성소의 수호 거상 스프라이트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SanctuaryColossus;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;

//sanctuary_colossus.png: 48x48 칸, 48프레임
// 0-5 대기(숨쉬기·핵 맥동·룬 바퀴 회전), 6-13 걷기, 14-19 주먹질, 20-27 대지 강타,
// 28-31 룬 광선 충전, 32-35 발사, 36-47 쓰러짐(금 감·무릎 꿇음·돌무더기로 무너짐)
public class ColossusSprite extends MobSprite {

	private Animation charging;
	private Animation blast;

	private Emitter coreGlow;
	private Emitter chargeMotes;
	private int blastPos = -1;

	public ColossusSprite() {
		super();
		texture( Assets.Sprites.SANCTUARY_COLOSSUS );
		TextureFilm frames = new TextureFilm( texture, 48, 48 );

		idle = new Animation( 6, true );
		idle.frames( frames, 0, 1, 2, 3, 4, 5 );

		run = new Animation( 9, true );
		run.frames( frames, 6, 7, 8, 9, 10, 11, 12, 13 );

		attack = new Animation( 13, false );
		attack.frames( frames, 14, 15, 16, 17, 18, 19 );

		zap = new Animation( 11, false );
		zap.frames( frames, 20, 21, 22, 23, 24, 25, 26, 27 );

		charging = new Animation( 10, true );
		charging.frames( frames, 28, 29, 30, 31 );

		blast = new Animation( 12, false );
		blast.frames( frames, 32, 33, 34, 35 );

		die = new Animation( 9, false );
		die.frames( frames, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47 );

		play( idle );
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );

		coreGlow = GameScene.emitter();
		if (coreGlow != null){
			coreGlow.autoKill = false;
			coreGlow.pour( RuneMote.FACTORY, 0.45f );
		}
		chargeMotes = GameScene.emitter();
		if (chargeMotes != null){
			chargeMotes.autoKill = false;
			chargeMotes.pour( RuneMote.FACTORY, 0.04f );
			chargeMotes.on = false;
		}

		if (ch instanceof SanctuaryColossus && ((SanctuaryColossus) ch).isCharging()){
			play( charging );
		}
	}

	@Override
	public void update() {
		super.update();
		//가슴 핵 둘레에서 빛 알갱이가 피어오름
		if (coreGlow != null){
			coreGlow.pos( x + (flipHorizontal ? 18 : 18), y + 15, 12, 11 );
			coreGlow.visible = visible;
		}
		if (chargeMotes != null){
			chargeMotes.pos( x + 6, y + 6, 36, 30 );
			chargeMotes.visible = visible;
		}
	}

	@Override
	public void play( Animation anim ) {
		if (chargeMotes != null) chargeMotes.on = anim == charging;
		if (coreGlow != null && anim != die) coreGlow.on = true;
		super.play( anim );
	}

	//룬 광선 모으기
	public void charge( int target ){
		turnTo( ch.pos, target );
		play( charging );
		if (visible) Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
	}

	//룬 광선 발사 (애니메이션이 끝나면 광선이 나감)
	public void blast( int target ){
		blastPos = target;
		turnTo( ch.pos, target );
		play( blast );
	}

	//대지 강타: 두 팔을 들어 내려침
	public void slam( int target ){
		turnTo( ch.pos, target );
		play( zap );
	}

	public PointF corePoint(){
		return new PointF( x + width / 2f, y + 21.5f );
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap){
			//내려치는 순간 화면 흔들림 + 먼지
			if (visible){
				PixelScene.shake( 3, 0.6f );
				Sample.INSTANCE.play( Assets.Sounds.ROCKS );
				bottomEmitter().burst( Speck.factory( Speck.ROCK ), 10 );
			}
			idle();
			return;
		}
		if (anim == blast){
			idle();
			if (blastPos >= 0){
				PointF end = Actor.findChar( blastPos ) != null
						? Actor.findChar( blastPos ).sprite.center()
						: DungeonTilemap.raisedTileCenterToWorld( blastPos );
				Beam beam = new Beam.LightRay( corePoint(), end );
				beam.hardlight( 0x40E8D8 );
				parent.add( beam );
				Beam core = new Beam.DeathRay( corePoint(), end );
				core.hardlight( 0xC8FFF4 );
				parent.add( core );
				Sample.INSTANCE.play( Assets.Sounds.RAY );
				PixelScene.shake( 2, 0.4f );
			}
			blastPos = -1;
			if (ch instanceof SanctuaryColossus){
				((SanctuaryColossus) ch).resolveBeam();
			}
			ch.next();
			return;
		}
		if (anim == die){
			if (coreGlow != null) coreGlow.on = false;
		}
		super.onComplete( anim );
	}

	@Override
	public void die() {
		super.die();
		if (coreGlow != null) coreGlow.on = false;
		if (chargeMotes != null) chargeMotes.on = false;
		if (visible){
			emitter().burst( Speck.factory( Speck.ROCK ), 14 );
			emitter().burst( RuneMote.FACTORY, 24 );
		}
	}

	@Override
	public void kill() {
		super.kill();
		if (coreGlow != null) coreGlow.killAndErase();
		if (chargeMotes != null) chargeMotes.killAndErase();
	}

	@Override
	public int blood() {
		return 0xFF8C8E78;
	}
}
