/*
 * Shattered Pixel Dungeon 모드: 룬 성갑 장착 연출
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

//마법 갑옷이 입혀지는 동작: 반지를 낀 손에서부터 반투명 갑옷의 픽셀이 팔을 타고 온몸으로 번져 나감.
//새로 돋는 픽셀은 잠깐 하얗게 빛난 뒤 제 색으로 가라앉음. 다 덮이면 영웅 위에 갑옷 껍데기(AegisShell)가 씌워짐
public class AegisAssemble extends Group {

	private static final int W = AegisShell.W, H = AegisShell.H, M = AegisShell.MARGIN;
	private static final float SPREAD = 0.07f;   //핵에서 한 픽셀 멀어질 때마다 늦어지는 시간
	private static final float GLOW = 0.16f, HOLD = 0.3f;

	private final ColorBlock[] px;
	private final float[] delay;
	private final Callback done;
	private float t = 0, total = 0, tick = 0;
	private boolean finished = false;

	private final boolean dissolve;

	//dissolve가 참이면 반대로, 덮여 있던 갑옷이 입자로 흩어지며 사라짐 (끝나도 아무것도 부르지 않아도 됨)
	public AegisAssemble( CharSprite target, String texture, boolean dissolve, Callback done ){
		this.done = done;
		this.dissolve = dissolve;
		SmartTexture tx = TextureCache.get( texture );
		ColorBlock[] blocks = new ColorBlock[W * H];
		float[] delays = new float[W * H];
		int n = 0;
		for (int y = 0; y < H; y++){
			for (int x = 0; x < W; x++){
				int c = tx.getPixel( x, y );
				if ((c >>> 24) == 0) continue;
				ColorBlock b = new ColorBlock( 1, 1, c );
				b.x = target.x - M + (target.flipHorizontal ? (W - 1 - x) : x);
				b.y = target.y - M + y;
				b.visible = dissolve;
				//반지를 낀 손에서 시작해 팔을 타고 온몸으로 번짐
				float d = Math.abs( x - (9f + M) ) + Math.abs( y - (10f + M) ) * 0.8f;
				delays[n] = d * SPREAD + Random.Float( 0.08f );
				total = Math.max( total, delays[n] );
				blocks[n++] = b;
				add( b );
			}
		}
		px = new ColorBlock[n];
		delay = new float[n];
		System.arraycopy( blocks, 0, px, 0, n );
		System.arraycopy( delays, 0, delay, 0, n );
		total += GLOW + HOLD;
	}

	@Override
	public void update() {
		super.update();
		if (finished) return;
		t += Game.elapsed;
		tick += Game.elapsed;
		if (!dissolve && tick > 0.09f && t < total - HOLD){
			tick = 0;
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.25f, 1.8f );
		}

		for (int i = 0; i < px.length; i++){
			float p = t - delay[i];
			if (p < 0) continue;
			ColorBlock b = px[i];
			if (dissolve){
				//밝게 빛나며 위로 떠올라 사라짐
				if (p >= GLOW * 2){ b.visible = false; continue; }
				float k = p / (GLOW * 2);
				b.y -= Game.elapsed * 14f;
				b.rm = b.gm = b.bm = 1 - k;
				b.ra = b.ga = b.ba = k * 0.9f;
				b.alpha( 1 - k );
				continue;
			}
			b.visible = true;
			if (p < GLOW){
				float k = p / GLOW;
				//흰 청록 → 제 색
				b.rm = b.gm = b.bm = k;
				b.ra = (1 - k) * 0.8f; b.ga = (1 - k) * 0.85f; b.ba = (1 - k);
			} else {
				b.resetColor();
			}
		}

		if (t > total){
			finished = true;
			if (done != null) done.call();
			killAndErase();
		}
	}
}
