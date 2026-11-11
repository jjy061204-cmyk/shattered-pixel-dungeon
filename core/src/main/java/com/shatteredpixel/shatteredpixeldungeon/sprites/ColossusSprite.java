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

//sanctuary_colossus.png: 64x64 칸(몸은 가운데 48x48, 둘레는 후광·비석 여백), 59프레임
// 0-7 대기 / 8-15 걷기 / 16-22 내려찍기 / 23-26 바위 뽑기 / 27-28 바위 들고 버팀 / 29-32 던지기
// 33-36 룬 광선 충전 / 37-40 발사 / 41-42 과부하로 휘청 / 43-46 기절 / 47-58 쓰러짐
public class ColossusSprite extends MobSprite {

	private Animation charging;
	private Animation blast;
	private Animation lift;
	private Animation hold;
	private Animation hurl;
	private Animation stagger;
	private Animation stunned;

	private Emitter coreGlow;
	private Emitter chargeMotes;
	private int[] blastCells;
	private float stillTime = 0;
	private Animation runA, runB;
	private boolean leftNext = true;

	public ColossusSprite() {
		super();
		texture( Assets.Sprites.SANCTUARY_COLOSSUS );
		TextureFilm frames = new TextureFilm( texture, 64, 64 );

		idle = new Animation( 7, true );
		idle.frames( frames, 0, 1, 2, 3, 4, 5, 6, 7 );

		//한 걸음(한 칸)마다 왼발·오른발을 번갈아 내딛도록 시작 발이 다른 두 벌
		runA = new Animation( 10, true );
		runA.frames( frames, 9, 10, 11, 12, 13, 14, 15, 8 );
		runB = new Animation( 10, true );
		runB.frames( frames, 13, 14, 15, 8, 9, 10, 11, 12 );
		run = runA;

		attack = new Animation( 14, false );
		attack.frames( frames, 16, 17, 17, 18, 19, 20, 20, 21, 22 );

		zap = attack.clone();

		lift = new Animation( 9, false );
		lift.frames( frames, 23, 24, 24, 25, 26 );

		hold = new Animation( 5, true );
		hold.frames( frames, 27, 28 );

		hurl = new Animation( 12, false );
		hurl.frames( frames, 29, 29, 30, 31, 31, 32 );

		charging = new Animation( 10, true );
		charging.frames( frames, 33, 34, 35, 36 );

		blast = new Animation( 12, false );
		blast.frames( frames, 37, 38, 39, 40 );

		stagger = new Animation( 7, false );
		stagger.frames( frames, 41, 41, 42 );

		stunned = new Animation( 6, true );
		stunned.frames( frames, 43, 44, 45, 46 );

		die = new Animation( 9, false );
		die.frames( frames, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58 );

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

		if (ch instanceof SanctuaryColossus){
			SanctuaryColossus c = (SanctuaryColossus) ch;
			if (c.isOverloaded()) play( stunned );
			else if (c.isCharging()) play( charging );
			else if (c.isHoldingRock()) play( hold );
		}
	}

	@Override
	public void update() {
		super.update();
		//걸음이 멈추면 대기 자세로 돌아감 (기본 스프라이트는 다음 행동 전까지 걷는 동작을 계속 되풀이함)
		if (curAnim == run && !isMoving){
			stillTime += com.watabou.noosa.Game.elapsed;
			if (stillTime > 0.3f) idle();
		} else {
			stillTime = 0;
		}
		if (coreGlow != null){
			coreGlow.pos( x + 26, y + 29, 12, 11 );
			coreGlow.visible = visible;
		}
		if (chargeMotes != null){
			chargeMotes.pos( x + 14, y + 20, 36, 30 );
			chargeMotes.visible = visible;
		}
	}

	@Override
	public void play( Animation anim ) {
		if (anim == run && curAnim != runA && curAnim != runB){
			run = leftNext ? runA : runB;
			leftNext = !leftNext;
			anim = run;
		}
		if (chargeMotes != null) chargeMotes.on = anim == charging || anim == stunned;
		super.play( anim );
	}

	//다른 동작 중에는 대기 자세로 돌아가지 않게
	@Override
	public void idle() {
		if (ch instanceof SanctuaryColossus && ch.isAlive()){
			SanctuaryColossus c = (SanctuaryColossus) ch;
			if (c.isOverloaded()) { play( stunned ); return; }
			if (c.isCharging()) { play( charging ); return; }
			if (c.isHoldingRock()) { play( hold ); return; }
		}
		super.idle();
	}

	public void forceIdle(){
		play( idle );
	}

	public PointF corePoint(){
		return new PointF( x + width / 2f, y + 35.5f );
	}

	//룬 광선 모으기
	public void charge( int target ){
		turnTo( ch.pos, target );
		play( charging );
		if (visible) Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
	}

	//룬 광선 발사 (애니메이션이 끝나면 광선이 나감)
	public void blast( int[] cells ){
		blastCells = cells;
		play( blast );
	}

	//바닥의 바위를 뽑아 머리 위로
	public void liftRock( int target ){
		turnTo( ch.pos, target );
		play( lift );
		if (visible){
			Sample.INSTANCE.play( Assets.Sounds.ROCKS, 0.7f, 0.7f );
			bottomEmitter().burst( Speck.factory( Speck.ROCK ), 8 );
		}
	}

	public void hurlRock( int target ){
		turnTo( ch.pos, target );
		play( hurl );
	}

	//과부하: 휘청이다 무릎을 꿇음
	public void overload(){
		play( stagger );
		if (visible){
			emitter().burst( RuneMote.FACTORY, 30 );
			PixelScene.shake( 3, 0.7f );
		}
	}

	//되돌아온 광선
	public void reflect( int mirror ){
		if (parent == null) return;
		PointF from = DungeonTilemap.raisedTileCenterToWorld( mirror );
		Beam b = new Beam.DeathRay( from, corePoint() );
		b.hardlight( 0xFFFFFF );
		parent.add( b );
		Beam b2 = new Beam.LightRay( from, corePoint() );
		b2.hardlight( 0x40E8D8 );
		parent.add( b2 );
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == lift){
			play( hold );
			return;
		}
		if (anim == hurl){
			super.idle();
			return;
		}
		if (anim == stagger){
			play( stunned );
			return;
		}
		if (anim == attack && visible){
			//내려찍는 순간
			PixelScene.shake( 2, 0.3f );
			Sample.INSTANCE.play( Assets.Sounds.ROCKS, 0.6f, 1.2f );
		}
		if (anim == blast){
			super.idle();
			if (blastCells != null && parent != null){
				for (int c : blastCells){
					Char at = Actor.findChar( c );
					PointF end = at != null && at.sprite != null ? at.sprite.center() : DungeonTilemap.raisedTileCenterToWorld( c );
					Beam beam = new Beam.LightRay( corePoint(), end );
					beam.hardlight( 0x40E8D8 );
					parent.add( beam );
					Beam core = new Beam.DeathRay( corePoint(), end );
					core.hardlight( 0xC8FFF4 );
					parent.add( core );
				}
				Sample.INSTANCE.play( Assets.Sounds.RAY );
				PixelScene.shake( 2, 0.4f );
			}
			blastCells = null;
			if (ch instanceof SanctuaryColossus){
				((SanctuaryColossus) ch).resolveBeam();
				ch.next();
			}
			return;
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
