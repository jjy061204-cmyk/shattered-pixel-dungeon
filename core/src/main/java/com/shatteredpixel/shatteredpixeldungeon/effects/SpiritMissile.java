/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령 투사의 정령 한 개체: 땅에서 솟아올라(빛나며 커짐) 살짝 떠오른 뒤, 곡선을 그리며 대상에게 날아가 꽂힘.
 * 지나간 자리에 그 정령다운 입자를 흘림
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class SpiritMissile extends ItemSprite {

	private static final float RISE = 0.22f;   //땅에서 솟아오르는 시간
	private static final float SPEED = 170f;

	private final ForestWand.Spirit spirit;
	private final PointF start, hover;
	private PointF dest;
	private final CharSprite target;
	private final Callback onHit;
	private final float flyTime;
	private float t;
	private final Emitter trail;
	private final PointF ctrl;
	private boolean hit = false;

	private SpiritMissile( ForestWand.Spirit s, int fromCell, CharSprite target, float delay, Callback onHit ){
		super( ItemSpriteSheet.spiritBolt( s.ordinal() ), null );
		this.spirit = s;
		this.target = target;
		this.onHit = onHit;
		start = DungeonTilemap.tileCenterToWorld( fromCell );
		hover = new PointF( start.x + Random.Float( -3, 3 ), start.y - 9 - Random.Float( 4 ) );
		dest = target.center();
		float dist = PointF.distance( hover, dest );
		flyTime = Math.max( 0.12f, dist / SPEED );
		//포물선처럼 위로 휘었다 내리꽂힘
		ctrl = new PointF( (hover.x + dest.x) / 2f + Random.Float( -10, 10 ), Math.min( hover.y, dest.y ) - 10 - dist * 0.15f );
		origin.set( width / 2f, height / 2f );
		t = -delay;
		visible = false;
		x = start.x - width / 2f;
		y = start.y - height / 2f;

		trail = new StraightTrail();
		trail.pos( start.x - 1, start.y - 1, 2, 2 );
		GameScene.effectGizmo( trail );
	}

	@Override
	public void update() {
		super.update();
		t += Game.elapsed;
		if (t < 0) return;
		if (!visible){
			visible = true;
			//땅에서 정령이 솟아남
			Emitter e = new Emitter();
			e.pos( start.x - 4, start.y - 2, 8, 4 );
			GameScene.effectGizmo( e );
			e.burst( spirit.particle(), 6 );
			e.autoKill = true;
			trail.pour( spirit.particle(), spirit.strong ? 0.02f : 0.035f );
		}
		float px, py;
		if (t < RISE){
			float p = t / RISE;
			px = start.x + (hover.x - start.x) * p;
			py = start.y + (hover.y - start.y) * p;
			scale.set( 0.4f + 0.6f * p );
			alpha( p );
		} else {
			if (target.parent != null && target.visible) dest = target.center();
			float p = Math.min( 1f, (t - RISE) / flyTime );
			float q = 1 - p;
			px = q*q*hover.x + 2*q*p*ctrl.x + p*p*dest.x;
			py = q*q*hover.y + 2*q*p*ctrl.y + p*p*dest.y;
			scale.set( 1f );
			alpha( 1f );
			//날아가는 쪽을 향함 (탄 그림은 오른쪽 위를 향해 그려져 있음)
			float tx = 2*q*(ctrl.x - hover.x) + 2*p*(dest.x - ctrl.x);
			float ty = 2*q*(ctrl.y - hover.y) + 2*p*(dest.y - ctrl.y);
			angle = (float) Math.toDegrees( Math.atan2( ty, tx ) ) + 45f;
			if (p >= 1f && !hit){
				hit = true;
				trail.on = false;
				trail.autoKill = true;
				killAndErase();
				if (onHit != null) onHit.call();
				return;
			}
		}
		x = px - width / 2f;
		y = py - height / 2f;
		trail.pos( px - 1, py - 1, 2, 2 );
	}

	public static void launch( ForestWand.Spirit s, int fromCell, CharSprite target, float delay, Callback onHit ){
		GameScene.effect( new SpiritMissile( s, fromCell, target, delay, onHit ) );
	}
}
