/*
 * Shattered Pixel Dungeon 모드: 차원 균열 참격 이펙트
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

//반투명한 보라색 일자 참격. 한쪽 끝에서 쭉 그어지듯 늘어났다가 가늘어지며 사라짐
public class RiftSlash extends Image {

	private static final float DRAW = 0.08f;   //그어지는 시간
	private static final float FADE = 0.30f;   //사라지는 시간
	private static final float MAX_ALPHA = 0.75f;

	private float time = 0;
	private final float size;

	public RiftSlash( float x, float y, float angle, float size ){
		super( Assets.Effects.RIFT_SLASH );
		this.size = size;
		origin.set( width / 2f, height / 2f );
		this.x = x - width / 2f;
		this.y = y - height / 2f;
		this.angle = angle;
		scale.set( 0, size );
		alpha( MAX_ALPHA );
	}

	@Override
	public void update() {
		super.update();
		time += Game.elapsed;
		if (time < DRAW){
			float p = time / DRAW;
			scale.set( size * (0.2f + 0.8f * p), size );
			alpha( MAX_ALPHA );
		} else if (time < DRAW + FADE){
			float p = (time - DRAW) / FADE;
			scale.set( size * (1f + 0.15f * p), size * (1f - 0.7f * p) );
			alpha( MAX_ALPHA * (1f - p) );
		} else {
			killAndErase();
		}
	}

	//사선 참격 (오른쪽 위 또는 왼쪽 위로 무작위)
	private static float randomAngle(){
		float a = Random.Float( 25, 60 );
		return Random.Int( 2 ) == 0 ? -a : a;
	}

	//캐릭터 몸 가운데를 가르는 참격
	public static void at( CharSprite sprite, float size ){
		if (sprite == null || sprite.parent == null) return;
		GameScene.effect( new RiftSlash( sprite.center().x, sprite.center().y, randomAngle(), size ) );
	}

	public static void at( float x, float y, float size ){
		GameScene.effect( new RiftSlash( x, y, randomAngle(), size ) );
	}

	public static void at( float x, float y, float angle, float size ){
		GameScene.effect( new RiftSlash( x, y, angle, size ) );
	}
}
