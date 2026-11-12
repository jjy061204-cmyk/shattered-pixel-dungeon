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

//나노 슈트식 장착 연출: 가슴의 동력핵에서부터 슈트의 픽셀이 하나씩 번져 나가며 몸을 덮음.
//새로 돋는 픽셀은 잠깐 청록빛으로 빛난 뒤 제 색으로 가라앉음. 다 덮이면 실제 스프라이트로 바뀜
public class AegisAssemble extends Group {

	private static final int W = 12, H = 15;
	private static final float SPREAD = 0.085f;   //핵에서 한 픽셀 멀어질 때마다 늦어지는 시간
	private static final float GLOW = 0.16f, HOLD = 0.3f;

	private final ColorBlock[] px;
	private final float[] delay;
	private final Callback done;
	private float t = 0, total = 0, tick = 0;
	private boolean finished = false;

	public AegisAssemble( CharSprite target, Callback done ){
		this.done = done;
		SmartTexture tx = TextureCache.get( Assets.Sprites.RUNE_AEGIS_HERO );
		ColorBlock[] blocks = new ColorBlock[W * H];
		float[] delays = new float[W * H];
		int n = 0;
		for (int y = 0; y < H; y++){
			for (int x = 0; x < W; x++){
				int c = tx.getPixel( x, y );
				if ((c >>> 24) == 0) continue;
				ColorBlock b = new ColorBlock( 1, 1, c );
				b.x = target.x + (target.flipHorizontal ? (W - 1 - x) : x);
				b.y = target.y + y;
				b.visible = false;
				float d = (float)Math.sqrt( (x - 5.5f) * (x - 5.5f) + (y - 9f) * (y - 9f) );
				delays[n] = d * SPREAD + Random.Float( 0.14f );
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
		if (tick > 0.09f && t < total - HOLD){
			tick = 0;
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.4f, 1.6f + t );
		}

		for (int i = 0; i < px.length; i++){
			float p = t - delay[i];
			if (p < 0) continue;
			ColorBlock b = px[i];
			b.visible = true;
			if (p < GLOW){
				float k = p / GLOW;
				//흰 청록 → 제 색
				b.rm = b.gm = b.bm = k;
				b.ra = (1 - k) * 0.5f; b.ga = (1 - k); b.ba = (1 - k);
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
