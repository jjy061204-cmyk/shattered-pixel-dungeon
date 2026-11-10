/*
 * Shattered Pixel Dungeon 모드: 엑스칼리버 봉인 해제 창
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.GoldSparkle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Excalibur;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

//봉인된 엑스칼리버를 크게 띄우고, 주문서를 넣어 자물쇠 4개와 보옥을 차례로 부수는 창
public class WndExcaliburSeal extends Window {

	private static final int WIDTH = 132;
	private static final float SCALE = 5f;            //칼 그림 확대 배율 (16 → 80)
	private static final float GRID = 20f;             //칼 그림 원본 칸 수 (20x20)
	private static final String TX = Assets.Interfaces.EXCALIBUR_SEAL;

	//칼 그림(20x20) 위 자물쇠 4개와 보옥 자리
	private static final float[][] LOCK_POS = { {8.75f, 7.25f}, {12.75f, 11.25f}, {12.75f, 3.25f}, {16.75f, 7.25f} };
	private static final float[] ORB_POS = { 6.5f, 13f };

	private final Excalibur sword;

	private ItemSprite swordImg;
	private float swordX, swordY;
	private final Image[] lockImg = new Image[Excalibur.LOCKS];
	private Image orbImg;
	private RenderedTextBlock status;
	private RedButton btnLock, btnOrb;

	//연출 상태
	private static final int NONE = 0, LOCK = 1, ORB = 2;
	private int anim = NONE;
	private float animTime;
	private int animLock;
	private Image halfL, halfR;
	private boolean orbBurst, unsealed;
	private ColorBlock flash;

	public WndExcaliburSeal( Excalibur sword ){
		super();
		this.sword = sword;

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.titleCase( sword.name() ), 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2f, 2 );
		add( title );

		//칼 그림
		swordImg = new ItemSprite( sword );
		swordImg.scale.set( SCALE );
		swordX = (WIDTH - 16 * SCALE) / 2f;
		swordY = title.bottom() + 4;
		swordImg.x = swordX;
		swordImg.y = swordY;
		add( swordImg );

		//보옥 (가드 가운데)
		orbImg = new Image( TX, 16, 0, 11, 11 );
		orbImg.origin.set( 5.5f, 5.5f );
		place( orbImg, ORB_POS, 11, 11 );
		add( orbImg );

		//자물쇠 (사슬 끝)
		for (int i = 0; i < lockImg.length; i++){
			lockImg[i] = new Image( TX, 0, 0, 11, 13 );
			place( lockImg[i], LOCK_POS[i], 11, 13 );
			lockImg[i].visible = i >= sword.locks;
			add( lockImg[i] );
		}

		float y = swordY + 16 * SCALE + 4;

		status = PixelScene.renderTextBlock( 6 );
		status.maxWidth( WIDTH );
		add( status );
		status.setPos( 0, y );

		btnLock = new RedButton( "" ){
			@Override
			protected void onClick() {
				insertUpgrade();
			}
		};
		add( btnLock );

		btnOrb = new RedButton( "" ){
			@Override
			protected void onClick() {
				insertRemoveCurse();
			}
		};
		add( btnOrb );

		refresh();
	}

	//칼 그림 칸 좌표 → 창 좌표 (그림 가운데를 그 자리에)
	private void place( Image img, float[] p, float w, float h ){
		float k = 16f / GRID * SCALE;
		img.x = swordX + p[0] * k - w / 2f;
		img.y = swordY + p[1] * k - h / 2f;
	}

	private void refresh(){
		int up = Dungeon.hero == null ? 0 : sword.upgradeCount( Dungeon.hero );
		int rc = Dungeon.hero == null ? 0 : sword.removeCurseCount( Dungeon.hero );

		status.text( Messages.get( this, "status", sword.locks, Excalibur.LOCKS, up, rc ) );
		status.setPos( (WIDTH - status.width()) / 2f, status.top() );

		float y = status.bottom() + 4;
		btnLock.text( Messages.get( this, "insert_upgrade", Excalibur.UPGRADE_PER_LOCK ) );
		btnLock.enable( anim == NONE && sword.sealed && sword.locks < Excalibur.LOCKS && up >= Excalibur.UPGRADE_PER_LOCK );
		btnLock.setRect( 0, y, WIDTH, 18 );
		y = btnLock.bottom() + 2;

		btnOrb.text( Messages.get( this, "insert_remove_curse" ) );
		btnOrb.enable( anim == NONE && sword.sealed && sword.locks >= Excalibur.LOCKS && rc >= Excalibur.NEED_REMOVE_CURSE );
		btnOrb.setRect( 0, y, WIDTH, 18 );

		resize( WIDTH, (int) btnOrb.bottom() );
	}

	private void insertUpgrade(){
		if (anim != NONE) return;
		int idx = sword.locks;
		if (!sword.breakLock( Dungeon.hero )) return;
		anim = LOCK;
		animTime = 0;
		animLock = idx;
		Sample.INSTANCE.play( Assets.Sounds.READ );
		refresh();
	}

	private void insertRemoveCurse(){
		if (anim != NONE) return;
		if (!sword.breakOrb( Dungeon.hero )) return;
		anim = ORB;
		animTime = 0;
		orbBurst = false;
		Sample.INSTANCE.play( Assets.Sounds.READ );
		refresh();
	}

	private Emitter burst( Image at, Emitter.Factory f, int n ){
		Emitter e = new Emitter();
		e.pos( at.x + at.width() / 2f - 2, at.y + at.height() / 2f - 2, 4, 4 );
		add( e );
		e.burst( f, n );
		return e;
	}

	@Override
	public void update() {
		super.update();
		if (anim == NONE) return;
		animTime += Game.elapsed;

		if (anim == LOCK){
			Image lock = lockImg[animLock];
			float bx = lock.x;
			if (animTime < 0.3f){
				//덜컹덜컹 흔들림
				place( lock, LOCK_POS[animLock], 11, 13 );
				lock.x += (float) Math.sin( animTime * 70f ) * 1.5f;
				lock.tint( 1f, 1f, 1f, animTime / 0.3f * 0.6f );
			} else if (halfL == null){
				//두 동강 나며 튀어 나감
				place( lock, LOCK_POS[animLock], 11, 13 );
				lock.visible = false;
				halfL = new Image( TX, 0, 0, 6, 13 );
				halfR = new Image( TX, 6, 0, 5, 13 );
				halfL.x = lock.x; halfL.y = lock.y;
				halfR.x = lock.x + 6; halfR.y = lock.y;
				halfL.origin.set( 3, 6 ); halfR.origin.set( 2, 6 );
				add( halfL ); add( halfR );
				burst( lock, SparkParticle.FACTORY, 12 );
				Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
				Sample.INSTANCE.play( Assets.Sounds.SHATTER, 0.6f, 1.4f );
			} else if (animTime < 0.8f){
				float p = (animTime - 0.3f) / 0.5f;
				halfL.x -= 30f * Game.elapsed; halfL.y += (p * 60f) * Game.elapsed;
				halfR.x += 30f * Game.elapsed; halfR.y += (p * 60f) * Game.elapsed;
				halfL.angle -= 300f * Game.elapsed; halfR.angle += 300f * Game.elapsed;
				halfL.alpha( 1f - p ); halfR.alpha( 1f - p );
			} else {
				halfL.killAndErase(); halfR.killAndErase();
				halfL = halfR = null;
				anim = NONE;
				refresh();
			}

		} else if (anim == ORB){
			if (animTime < 0.5f){
				//보옥이 떨며 하얗게 달아오름
				place( orbImg, ORB_POS, 11, 11 );
				orbImg.x += (float) Math.sin( animTime * 90f ) * (animTime * 3f);
				orbImg.tint( 1f, 1f, 1f, animTime / 0.5f * 0.8f );
			} else if (!orbBurst){
				orbBurst = true;
				place( orbImg, ORB_POS, 11, 11 );
				burst( orbImg, GoldSparkle.BURST, 40 );
				burst( orbImg, SparkParticle.FACTORY, 16 );
				Sample.INSTANCE.play( Assets.Sounds.SHATTER );
				Sample.INSTANCE.play( Assets.Sounds.CHAINS );
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP, 1f, 0.8f );
				//칼 위로 금빛 번쩍임
				flash = new ColorBlock( 16 * SCALE, 16 * SCALE, 0xFFFFF0B0 );
				flash.x = swordX; flash.y = swordY;
				add( flash );
				GameScene.flash( 0x80FFE6A0 );
			} else if (animTime < 1.0f){
				float p = (animTime - 0.5f) / 0.5f;
				orbImg.scale.set( 1f + p * 1.5f );
				orbImg.alpha( 1f - p );
				if (flash != null) flash.alpha( 1f - p );
				if (!unsealed && p > 0.3f){
					//사슬이 사라진 금빛 칼로 바뀜
					unsealed = true;
					sword.unseal( Dungeon.hero );
					swordImg.view( sword );
					title();
				}
			} else if (animTime < 2.0f){
				orbImg.visible = false;
				if (flash != null){ flash.killAndErase(); flash = null; }
			} else {
				anim = NONE;
				hide();
			}
		}
	}

	private void title(){
		status.text( Messages.get( this, "awakened" ) );
		status.setPos( (WIDTH - status.width()) / 2f, status.top() );
		btnLock.enable( false );
		btnOrb.enable( false );
	}

	@Override
	public void onBackPressed() {
		//연출 중에는 닫히지 않음
		if (anim == NONE) super.onBackPressed();
	}
}
