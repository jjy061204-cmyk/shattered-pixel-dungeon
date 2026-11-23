/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 환검 기사 곁을 맴도는 환검: 지금 빚은 에테르 점토와 같은 모양의 작은 반투명 칼날이
 * 칼끝을 위로 세운 채 영웅 둘레를 천천히 돎. 공격하면 적에게 날아가 베고 제자리로, 막으면 부서짐
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhantomBlades;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherClay;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;

import java.util.ArrayList;

public class PhantomOrbit extends Group {

	private static final float SPEED = 1.6f;    //초당 회전(라디안)
	private static final float RX = 10f, RY = 4f;
	private static final float SCALE = 0.55f;

	private final PhantomBlades buff;
	private final CharSprite host;
	private final ArrayList<ItemSprite> blades = new ArrayList<>();
	private float phase = 0;
	private int image = -1;

	public PhantomOrbit( PhantomBlades buff, CharSprite host ){
		super();
		this.buff = buff;
		this.host = host;
	}

	private static int clayImage(){
		EtherClay clay = Dungeon.hero != null ? Dungeon.hero.belongings.getItem( EtherClay.class ) : null;
		return clay != null ? clay.image() : ItemSpriteSheet.ETHER_CLAY;
	}

	private ItemSprite makeBlade(){
		ItemSprite b = new ItemSprite( image, null );
		b.origin.set( b.width / 2f, b.height / 2f );
		b.scale.set( SCALE );
		b.angle = -45f;
		b.alpha( 0.8f );
		b.tint( 0.85f, 0.8f, 1f, 0.35f );
		return b;
	}

	private PointF slot( int i, int n ){
		PointF c = host.center();
		float a = phase + (float) (Math.PI * 2 * i / Math.max( 1, n ));
		return new PointF( c.x + (float) Math.cos( a ) * RX, c.y - 3f + (float) Math.sin( a ) * RY );
	}

	@Override
	public void update() {
		super.update();
		if (host == null || host.parent == null || !host.alive){
			killAndErase();
			return;
		}
		int img = clayImage();
		if (img != image){
			image = img;
			for (ItemSprite b : blades) b.view( image, null );
		}
		int want = buff.count();
		while (blades.size() < want){
			ItemSprite b = makeBlade();
			blades.add( b );
			add( b );
		}
		while (blades.size() > want){
			ItemSprite b = blades.remove( blades.size() - 1 );
			b.killAndErase();
		}
		phase += SPEED * Game.elapsed;
		boolean vis = host.visible && buff.target != null && buff.target.invisible == 0;
		for (int i = 0; i < blades.size(); i++){
			ItemSprite b = blades.get( i );
			PointF p = slot( i, blades.size() );
			b.x = p.x - b.width / 2f;
			b.y = p.y - b.height / 2f + (float) Math.sin( phase * 2f + i ) * 0.8f;
			b.visible = vis;
		}
	}

	public void spawn(){
	}

	//i번째 환검이 적에게 날아가 벰 (그림만, 환검은 제자리에 남음)
	public void strike( int i, CharSprite target ){
		if (!host.visible || image < 0) return;
		PointF from = blades.isEmpty() ? host.center() : slot( Math.min( i, blades.size() - 1 ), blades.size() );
		GameScene.effect( new Flyer( image, from, target, i * 0.05f ) );
	}

	//환검 하나가 부서짐
	public void shatter(){
		if (!host.visible) return;
		PointF p = blades.isEmpty() ? host.center() : slot( blades.size() - 1, blades.size() );
		Emitter e = new Emitter();
		e.pos( p.x - 2, p.y - 2, 4, 4 );
		GameScene.effectGizmo( e );
		e.burst( StarlightParticle.BURST, 8 );
		e.autoKill = true;
	}

	private static class Flyer extends ItemSprite {
		private final PointF from;
		private final CharSprite target;
		private float time;
		private boolean slashed = false;
		private static final float GO = 0.10f, BACK = 0.12f;

		Flyer( int image, PointF from, CharSprite target, float delay ){
			super( image, null );
			this.from = from;
			this.target = target;
			this.time = -delay;
			origin.set( width / 2f, height / 2f );
			scale.set( SCALE );
			tint( 0.85f, 0.8f, 1f, 0.35f );
			alpha( 0 );
			x = from.x - width / 2f;
			y = from.y - height / 2f;
		}

		@Override
		public void update() {
			super.update();
			time += Game.elapsed;
			if (time < 0) return;
			PointF to = target.center();
			float dir = (float) Math.toDegrees( Math.atan2( to.y - from.y, to.x - from.x ) );
			angle = dir + 45f;   //칼끝(그림의 오른쪽 위)이 날아가는 쪽을 향함
			if (time < GO){
				float p = time / GO;
				alpha( 0.9f );
				x = from.x + (to.x - from.x) * p - width / 2f;
				y = from.y + (to.y - from.y) * p - height / 2f;
			} else if (time < GO + BACK){
				if (!slashed){
					slashed = true;
					if (target.parent != null) RiftSlash.at( target, 0.55f );
				}
				float p = (time - GO) / BACK;
				alpha( 0.9f * (1f - p) );
				x = to.x - width / 2f;
				y = to.y - height / 2f;
			} else {
				killAndErase();
			}
		}
	}
}
