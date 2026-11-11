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
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.GoldSparkle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

//갑옷 조각 여섯(다리 → 흉갑 → 팔 → 어깨 → 망토 → 투구)이 사방에서 차례로 날아와 영웅 몸에 맞춰짐.
//rune_aegis_parts.png: 20x24 칸 여섯, 각 칸은 전신 그림에서 그 조각만 남긴 것이라 같은 자리에 겹치면 전신이 완성됨
public class AegisAssemble extends Group {

	private static final int W = 20, H = 24, N = 6;
	private static final float STAGGER = 0.16f, FLY = 0.42f, HOLD = 0.3f;

	//조각이 날아오는 출발 위치 (도착점 기준)
	private static final float[][] FROM = {
			{ 0, 46 }, { 52, 6 }, { -50, 14 }, { -44, -30 }, { -58, -4 }, { 0, -54 }
	};

	private final Image[] parts = new Image[N];
	private final boolean[] landed = new boolean[N];
	private final CharSprite target;
	private final PointF base;
	private final Callback done;
	private float t = 0;
	private boolean finished = false;

	public AegisAssemble( CharSprite target, Callback done ){
		this.target = target;
		this.done = done;
		base = new PointF( target.center().x - W / 2f, target.y + target.height() - H );
		for (int i = 0; i < N; i++){
			Image im = new Image( Assets.Sprites.RUNE_AEGIS_PARTS );
			im.frame( im.texture.uvRect( i * W, 0, i * W + W, H ) );
			im.flipHorizontal = target.flipHorizontal;
			im.visible = false;
			parts[i] = im;
			add( im );
		}
	}

	@Override
	public void update() {
		super.update();
		if (finished) return;
		t += Game.elapsed;

		for (int i = 0; i < N; i++){
			float p = (t - i * STAGGER) / FLY;
			if (p <= 0) continue;
			Image im = parts[i];
			im.visible = true;
			if (p >= 1){
				im.x = base.x;
				im.y = base.y;
				im.alpha( 1f );
				im.resetColor();
				if (!landed[i]){
					landed[i] = true;
					target.emitter().burst( GoldSparkle.BURST, 6 );
					target.emitter().burst( RuneMote.FACTORY, 5 );
					Sample.INSTANCE.play( Assets.Sounds.HIT_PARRY, 0.7f, 0.8f + i * 0.12f );
				}
			} else {
				float e = 1 - (1 - p) * (1 - p) * (1 - p);
				float dir = target.flipHorizontal ? -1 : 1;
				im.x = base.x + FROM[i][0] * dir * (1 - e);
				im.y = base.y + FROM[i][1] * (1 - e);
				im.alpha( Math.min( 1f, p * 2.5f ) );
			}
		}

		if (t > (N - 1) * STAGGER + FLY + HOLD){
			finished = true;
			if (done != null) done.call();
			killAndErase();
		}
	}
}
