/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeroDisguise;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;

public class HeroSprite extends CharSprite {
	
	private static final int FRAME_WIDTH	= 12;
	private static final int FRAME_HEIGHT	= 15;
	
	private static final int RUN_FRAMERATE	= 20;

	//모드: 64x64칸의 세밀한 도트(꼬마 비율). 다른 영웅과 키가 비슷하도록 줄여 그림
	public static final int HD_WIDTH = 64, HD_HEIGHT = 64;
	public static final float HD_SCALE = 0.32f;

	//화면에서 도트 한 칸이 화면 한 픽셀 이상이면 또렷하게, 그보다 작아지면 뭉개지지 않게 부드럽게 줄임
	public static void smooth( com.watabou.gltextures.SmartTexture tx ){
		float r = com.watabou.noosa.Camera.main != null ? com.watabou.noosa.Camera.main.zoom * HD_SCALE : 1f;
		int mode = r >= 0.95f ? com.watabou.glwrap.Texture.NEAREST : com.watabou.glwrap.Texture.LINEAR;
		tx.filter( mode, mode );
	}
	private HeroClass look;
	
	private static TextureFilm tiers;
	
	private Animation fly;
	private Animation read;

	//모드: 세밀한 도트 영웅의 고유 대기 동작 (가만히 있으면 가끔 재생)
	private Animation[] specials;
	private Animation stop;
	private float idleTimer = 0, nextSpecial = 2.5f;

	//모드: 세밀한 도트 영웅의 동작 표 ({초당 프레임, 프레임...})
	//  0 대기, 1 걷기, 2 쓰러짐, 3 공격, 4 조작, 5 날기, 6 읽기, 7 멈춤(걷기 → 대기 사이), 8~ 고유 대기 동작
	public static int[][] hdAnims( HeroClass cls ){
		switch (cls){
			case COLLECTOR: return new int[][]{
					{1, 0, 0, 0, 1, 0, 0, 1, 1},
					{20, 2, 3, 4, 5, 6, 7},
					{20, 8, 9, 10, 11, 12, 11},
					{15, 13, 14, 15, 0},
					{8, 16, 17, 16, 17},
					{1, 18},
					{20, 19, 20, 20, 20, 20, 20, 20, 20, 20, 19},
					{10, 33, 34, 35},                                       //멈추면 바로 서고, 외투 자락만 관성으로 쏠렸다 가라앉음
					{6, 21, 22, 23, 24, 25, 24, 23, 24, 25, 26, 26, 22},   //유물을 닦음
					{6, 27, 28, 29, 30, 31, 30, 31, 30, 31, 32, 32}};      //마력을 띄워 확인
			case VAMPIRE: return new int[][]{
					//숨쉬기·머리카락과 드레스가 산들거림. 세 바퀴(4초)에 한 번, 약 0.25초 동안 눈을 깜빡임 (58 반쯤 감음, 59 감음, 60 반쯤 뜸)
					{12, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7,
						0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 58, 59, 60, 6, 7, 7,
						0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7},
					{20, 8, 9, 10, 11, 12, 13, 14, 15},                     //진행 방향을 바라보며, 머리카락이 뒤로 날리는 걸음
					{14, 16, 17, 18, 19, 20, 21},
					{18, 22, 23, 24, 25, 26, 27},                           //웅크림 → 치켜듦 → 내려벰(잔상) → 마무리
					{12, 28, 29, 30, 29, 30, 31},                           //원작 영웅처럼 살짝 숙이고, 가운데 모은 작은 두 손만 꼼지락거림
					{4, 32, 33},
					{18, 34, 35, 36, 36, 36, 35, 36, 36, 35, 34},
					{10, 54, 55, 56, 57},                                   //멈추면 바로 서고, 머리카락·드레스만 관성으로 쏠렸다 가라앉음
					{6, 37, 38, 39, 40, 41, 41, 41, 42, 42, 43, 44, 43, 44, 39, 38, 37},   //와인을 마시며 음미
					{6, 45, 46, 47, 48, 49, 50, 49, 50, 51, 51, 52, 53, 53}};             //꾸벅꾸벅 졸다가 깸
			case ELEMENTALIST: return new int[][]{
					//숨쉬기·드릴 머리와 드레스가 산들거림, 가끔 눈을 깜빡임 (59 반쯤 감음, 60 감음, 61 반쯤 뜸)
					{12, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7,
						0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 59, 60, 61, 6, 7, 7,
						0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7},
					{20, 8, 9, 10, 11, 12, 13, 14, 15},                     //진행 방향을 바라보며, 드릴 머리가 뒤로 날리는 걸음
					{14, 16, 17, 18, 19, 20, 21},
					{18, 22, 23, 24, 25, 26, 27},                           //두 손으로 완드를 들어 올렸다가 겨눠 정령빛을 터뜨림
					{12, 28, 29, 30, 29, 30, 31},
					{4, 32, 33},
					{18, 34, 35, 36, 36, 36, 35, 36, 36, 35, 34},
					{10, 55, 56, 57, 58},                                   //멈추면 바로 서고, 드릴 머리·드레스만 관성으로 쏠렸다 가라앉음
					{6, 37, 38, 39, 40, 41, 42, 43, 44, 45, 45, 46, 46},   //작은 숲의 정령빛을 눈으로 좇다가 두 손에 받아 들고 방긋
					{6, 47, 48, 49, 50, 51, 51, 52, 53, 53, 54}};           //치맛자락을 집고 무릎을 굽혀 인사
			default: return null;
		}
	}

	public static Animation hdAnim( TextureFilm film, int[] def, boolean loop ){
		Animation a = new Animation( def[0], loop );
		Object[] f = new Object[def.length - 1];
		for (int j = 1; j < def.length; j++) f[j - 1] = def[j];
		a.frames( film, f );
		return a;
	}

	private boolean isSpecial( Animation a ){
		if (specials == null || a == null) return false;
		for (Animation s : specials) if (s == a) return true;
		return false;
	}

	public HeroSprite() {
		super();
		
		look = Dungeon.hero.heroClass;
		texture( sheetOf( look ) );
		updateArmor();
		
		link( Dungeon.hero );

		if (ch.isAlive())
			idle();
		else
			die();
	}

	private static String sheetOf( HeroClass cls ){
		return cls.hdSheet() != null ? cls.hdSheet() : cls.spritesheet();
	}

	public void disguise(HeroClass cls){
		look = cls;
		texture( sheetOf( cls ) );
		updateArmor();
	}
	
	//모드: 수호의 반지 갑옷은 영웅 위에 따로 씌우는 반투명 껍데기(AegisShell)로 그림. 영웅 모습은 그대로
	public void updateArmor() {

		TextureFilm film;
		if (look != null && look.hdSheet() != null){
			film = new TextureFilm( texture, HD_WIDTH, HD_HEIGHT );
			smooth( texture );
			scale.set( HD_SCALE );
		} else {
			film = new TextureFilm( tiers(), Dungeon.hero.tier(), FRAME_WIDTH, FRAME_HEIGHT );
			scale.set( 1f );
		}
		
		int[][] hd = (look != null && look.hdSheet() != null) ? hdAnims( look ) : null;
		specials = null;
		stop = null;
		if (hd != null){
			idle = hdAnim( film, hd[0], true );
			run = hdAnim( film, hd[1], true );
			die = hdAnim( film, hd[2], false );
			attack = hdAnim( film, hd[3], false );
			zap = attack.clone();
			operate = hdAnim( film, hd[4], false );
			fly = hdAnim( film, hd[5], true );
			read = hdAnim( film, hd[6], false );
			stop = hdAnim( film, hd[7], false );
			specials = new Animation[hd.length - 8];
			for (int i = 8; i < hd.length; i++) specials[i - 8] = hdAnim( film, hd[i], false );
		} else {
			idle = new Animation( 1, true );
			idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );
			
			run = new Animation( RUN_FRAMERATE, true );
			run.frames( film, 2, 3, 4, 5, 6, 7 );
			
			die = new Animation( 20, false );
			die.frames( film, 8, 9, 10, 11, 12, 11 );
			
			attack = new Animation( 15, false );
			attack.frames( film, 13, 14, 15, 0 );
			
			zap = attack.clone();
			
			operate = new Animation( 8, false );
			operate.frames( film, 16, 17, 16, 17 );
			
			fly = new Animation( 1, true );
			fly.frames( film, 18 );

			read = new Animation( 20, false );
			read.frames( film, 19, 20, 20, 20, 20, 20, 20, 20, 20, 19 );
		}
		
		if (Dungeon.hero.isAlive())
			idle();
		else
			die();
	}
	
	@Override
	public void place( int p ) {
		super.place( p );
		if (Game.scene() instanceof GameScene) Camera.main.panFollow(this, 5f);
	}

	@Override
	public void move( int from, int to ) {
		super.move( from, to );
		if (ch != null && ch.flying) {
			play( fly );
		}
		Camera.main.panFollow(this, 20f);
	}

	@Override
	public void idle() {
		//모드: 걷다가 멈추면 몸은 곧바로 서되, 머리카락·옷자락이 관성으로 흔들리다 가라앉는 장면을 먼저 보여 줌
		if (stop != null && ch != null && ch.isAlive() && !sleeping && !ch.flying){
			if (curAnim == run){
				play( stop );
				return;
			}
			if (curAnim == stop && !finished) return;
		}
		plainIdle();
	}

	private void plainIdle() {
		super.idle();
		if (ch != null && ch.flying) {
			play( fly );
		}
	}

	@Override
	public void jump( int from, int to, float height, float duration,  Callback callback ) {
		super.jump( from, to, height, duration, callback );
		play( fly );
		Camera.main.panFollow(this, 20f);
	}

	public synchronized void read() {
		animCallback = new Callback() {
			@Override
			public void call() {
				idle();
				ch.onOperateComplete();
			}
		};
		play( read );
	}

	@Override
	public void bloodBurstA(PointF from, int damage) {
		//Does nothing.

		/*
		 * This is both for visual clarity, and also for content ratings regarding violence
		 * towards human characters. The heroes are the only human or human-like characters which
		 * participate in combat, so removing all blood associated with them is a simple way to
		 * reduce the violence rating of the game.
		 */
	}

	@Override
	public void update() {
		sleeping = ch.isAlive() && ((Hero)ch).resting;
		
		super.update();

		//모드: 뱀파이어의 핏빛 날개가 새 그림에도 붙어 있게 함
		if (ch != null && ch.flying) com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BloodWings.ensureFx( this );
		//모드: 진조의 왕관도 새 그림 위에 다시 얹음
		if (ch != null && parent != null) com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire.CrimsonCrown.Crown.ensureFx( this );
		if (ch != null && parent != null) com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire.UndyingBlood.Tracker.ensureFx( this );

		//가만히 서 있으면 몇 초마다 고유 대기 동작을 함
		if (specials != null && ch != null && ch.isAlive() && !sleeping && !isMoving && curAnim == idle && !ch.flying){
			idleTimer += Game.elapsed;
			if (idleTimer >= nextSpecial){
				idleTimer = 0;
				nextSpecial = com.watabou.utils.Random.Float( 2.5f, 5f );
				play( specials[com.watabou.utils.Random.Int( specials.length )] );
			}
		} else if (!isSpecial( curAnim )){
			idleTimer = 0;
		}
	}

	@Override
	public synchronized void onComplete( Animation anim ) {
		if (isSpecial( anim ) || (stop != null && anim == stop)){
			plainIdle();
			return;
		}
		super.onComplete( anim );
		//콜백이 딸린 공격이나 시전(zap)은 원작에서 마지막 프레임이 대기 자세라 따로 되돌리지 않음.
		//세밀한 도트 영웅은 마지막 프레임이 휘두른 자세이므로, 다른 동작이 시작되지 않았다면 직접 대기로 되돌림
		//(콜백이 같은 동작을 다시 시작했거나 새 콜백을 걸어 두었다면 건드리지 않음)
		if (specials != null && (anim == attack || anim == zap) && curAnim == anim && finished
				&& animCallback == null && ch != null && ch.isAlive()){
			plainIdle();
		}
	}

	//모드: 영웅 고유 공격 이펙트 (뱀파이어: 은빛 장검 참격, 유물수집가: 보랏빛 연금 폭발)
	@Override
	public synchronized void attack( int cell, Callback callback ) {
		super.attack( cell, callback );
		if (look != null && look.hdSheet() != null && parent != null && ch != null && cell != ch.pos){
			parent.add( new com.shatteredpixel.shatteredpixeldungeon.effects.HeroAttackFX( this, cell,
					look == HeroClass.VAMPIRE ? com.shatteredpixel.shatteredpixeldungeon.effects.HeroAttackFX.SLASH
							: look == HeroClass.ELEMENTALIST ? com.shatteredpixel.shatteredpixeldungeon.effects.HeroAttackFX.LEAF
							: com.shatteredpixel.shatteredpixeldungeon.effects.HeroAttackFX.ALCHEMY ) );
		}
	}
	
	public void sprint( float speed ) {
		run.delay = 1f / speed / RUN_FRAMERATE;
	}
	
	public static TextureFilm tiers() {
		if (tiers == null) {
			SmartTexture texture = TextureCache.get( Assets.Sprites.ROGUE );
			tiers = new TextureFilm( texture, texture.width, FRAME_HEIGHT );
		}
		
		return tiers;
	}

	public static Image avatar( Hero hero ){
		if (hero.buff(HeroDisguise.class) != null){
			return avatar(hero.buff(HeroDisguise.class).getDisguise(), hero.tier());
		} else {
			return avatar(hero.heroClass, hero.tier());
		}
	}
	
	public static Image avatar( HeroClass cl, int armorTier ) {
		//세밀한 도트 영웅: 첫 프레임에서 몸이 있는 가운데 부분만 잘라 원래 초상화 크기(약 15칸 높이)로 줄임
		if (cl.hdSheet() != null){
			Image hd = new Image( cl.hdSheet(), 8, 0, 48, HD_HEIGHT );
			smooth( hd.texture );
			hd.scale.set( 15f / HD_HEIGHT );
			return hd;
		}
		
		RectF patch = tiers().get( armorTier );
		Image avatar = new Image( cl.spritesheet() );
		RectF frame = avatar.texture.uvRect( 1, 0, FRAME_WIDTH, FRAME_HEIGHT );
		frame.shift( patch.left, patch.top );
		avatar.frame( frame );
		
		return avatar;
	}
}
