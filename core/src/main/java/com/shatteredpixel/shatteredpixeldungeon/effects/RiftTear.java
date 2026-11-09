/*
 * Shattered Pixel Dungeon 모드: 차원 균열 이펙트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

//10층 차원의 균열과 같은 그림이 잠깐 찢어지듯 벌어졌다가 닫히는 효과
public class RiftTear extends Image {

	private static final float TIME = 0.4f;
	private float time = TIME;
	private final float size;

	public RiftTear( float x, float y, float size ){
		super( Assets.Environment.DIMENSION_RIFT, 0, 0, 16, 32 );
		this.size = size;
		origin.set( width / 2f, height / 2f );
		this.x = x - width / 2f;
		this.y = y - height / 2f;
		angle = Random.Float( -40, 40 );
		scale.set( size * 0.2f, size * 0.4f );
	}

	@Override
	public void update() {
		super.update();
		time -= Game.elapsed;
		if (time <= 0){
			killAndErase();
			return;
		}
		float p = 1f - time / TIME;
		//빠르게 벌어지고 천천히 닫힘
		float open = p < 0.3f ? p / 0.3f : 1f - (p - 0.3f) / 0.7f;
		scale.set( size * (0.2f + 0.6f * open), size * (0.4f + 0.6f * Math.min( 1f, p * 3f )) );
		alpha( Math.min( 1f, open * 1.6f ) );
	}

	//캐릭터 몸 가운데에 균열을 엶
	public static void at( CharSprite sprite, float size ){
		if (sprite == null || sprite.parent == null) return;
		RiftTear t = new RiftTear( sprite.center().x, sprite.center().y, size );
		GameScene.effect( t );
	}

	public static void at( float x, float y, float size ){
		GameScene.effect( new RiftTear( x, y, size ) );
	}
}
