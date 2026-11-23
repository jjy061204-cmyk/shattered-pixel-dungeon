/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 환검 기사의 '환검': 에테르 점토에서 떼어 낸 작은 환영 칼날이 영웅 곁을 맴돎 (최대 3자루)
 *  - 12턴마다 한 자루씩 생겨남 (환검 생성: 10/8/6턴)
 *  - 근접 공격으로 적을 맞히면, 떠 있는 환검이 하나씩 날아가 같은 적을 벰 (한 자루에 기본 공격 피해의 20%, 방어 무시)
 *  - 적에게 맞으면 환검 하나가 부서지며 그 피해를 절반 막아 냄 (파편 반격: 막은 피해의 50/100/150%를 돌려줌)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.PhantomOrbit;
import com.shatteredpixel.shatteredpixeldungeon.effects.RiftSlash;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class PhantomBlades extends Buff {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public static final int MAX = 3;
	public static final float BASE_INTERVAL = 12f;
	public static final float BASE_PCT = 0.20f;

	private int count = 0;
	private float progress = 0;
	private PhantomOrbit orbit;

	public int count(){
		return count;
	}

	//환검 생성: 12 → 10 / 8 / 6턴
	public static float interval( Hero hero ){
		return BASE_INTERVAL - 2f * (hero == null ? 0 : hero.pointsInTalent( Talent.BLADE_GENESIS ));
	}

	//예리한 환검: 20 → 27 / 33 / 40%
	public static float pct( Hero hero ){
		switch (hero == null ? 0 : hero.pointsInTalent( Talent.KEEN_PHANTOMS )){
			case 0: default: return BASE_PCT;
			case 1: return 0.27f;
			case 2: return 0.33f;
			case 3: return 0.40f;
		}
	}

	private Hero hero(){
		return target instanceof Hero ? (Hero) target : null;
	}

	public void addBlade(){
		if (count >= MAX) return;
		count++;
		if (orbit != null) orbit.spawn();
		if (target.sprite != null && target.sprite.visible){
			target.sprite.emitter().burst( StarlightParticle.BURST, 4 );
		}
		BuffIndicator.refreshHero();
	}

	@Override
	public boolean act() {
		Hero hero = hero();
		if (hero != null && hero.isAlive() && count < MAX){
			progress += 1f;
			if (progress >= interval( hero )){
				progress = 0;
				addBlade();
			}
		} else if (count >= MAX){
			progress = 0;
		}
		spend( TICK );
		return true;
	}

	//근접 공격이 맞았을 때: 떠 있는 환검이 모두 같은 적을 따라 벰
	public static void onHeroHit( Hero hero, Char enemy ){
		if (hero.subClass != HeroSubClass.PHANTOM_BLADE || enemy == null) return;
		PhantomBlades pb = hero.buff( PhantomBlades.class );
		if (pb == null || pb.count <= 0) return;
		float p = pct( hero );
		for (int i = 0; i < pb.count && enemy.isAlive(); i++){
			int dmg = Math.max( 1, Math.round( hero.damageRoll() * p ) );
			enemy.damage( dmg, pb );
			if (pb.orbit != null && enemy.sprite != null) pb.orbit.strike( i, enemy.sprite );
			else if (enemy.sprite != null) RiftSlash.at( enemy.sprite, 0.6f );
		}
		Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 0.7f, 1.4f );
	}

	//적에게 맞을 때 (Hero.defenseProc 에서 부름): 환검 하나가 부서지며 피해를 절반 막음
	public static int onHeroDefend( Hero hero, Char enemy, int damage ){
		if (hero.subClass != HeroSubClass.PHANTOM_BLADE || damage <= 1) return damage;
		PhantomBlades pb = hero.buff( PhantomBlades.class );
		if (pb == null || pb.count <= 0) return damage;
		int blocked = damage / 2;
		damage -= blocked;
		pb.count--;
		if (pb.orbit != null) pb.orbit.shatter();
		if (hero.sprite != null){
			hero.sprite.centerEmitter().burst( Speck.factory( Speck.LIGHT ), 4 );
			hero.sprite.showStatus( 0xB8A4FF, Messages.get( PhantomBlades.class, "parried" ) );
		}
		Sample.INSTANCE.play( Assets.Sounds.SHATTER, 0.7f, 1.3f );

		int counter = Math.round( blocked * 0.5f * hero.pointsInTalent( Talent.SHATTER_COUNTER ) );
		if (counter > 0 && enemy != null && enemy.isAlive()){
			enemy.damage( counter, pb );
			if (enemy.sprite != null){
				enemy.sprite.burst( 0xFFB8A4FF, 5 );
				RiftSlash.at( enemy.sprite, 0.7f );
			}
		}
		BuffIndicator.refreshHero();
		return damage;
	}

	@Override
	public void fx( boolean on ) {
		if (on && target.sprite != null){
			if (orbit != null) orbit.killAndErase();
			orbit = new PhantomOrbit( this, target.sprite );
			GameScene.effectGizmo( orbit );
		} else if (!on && orbit != null){
			orbit.killAndErase();
			orbit = null;
		}
	}

	@Override
	public void detach() {
		super.detach();
		if (orbit != null){
			orbit.killAndErase();
			orbit = null;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.THROWN_WEP;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 0xB8A4FF );
	}

	@Override
	public float iconFadePercent() {
		if (count >= MAX) return 0;
		return Math.max( 0, 1f - progress / interval( hero() ) );
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString( count );
	}

	@Override
	public String desc() {
		Hero hero = hero();
		String d = Messages.get( this, "desc", count, MAX, Math.round( pct( hero ) * 100 ), (int) interval( hero ) );
		if (count < MAX) d += "\n\n" + Messages.get( this, "next", (int) Math.ceil( interval( hero ) - progress ) );
		return d;
	}

	private static final String COUNT = "count";
	private static final String PROGRESS = "progress";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( COUNT, count );
		bundle.put( PROGRESS, progress );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		count = bundle.getInt( COUNT );
		progress = bundle.getFloat( PROGRESS );
	}

	//전직했을 때 한 자루를 바로 줌
	public static void onSubclassChosen( Hero hero ){
		PhantomBlades pb = Buff.affect( hero, PhantomBlades.class );
		if (pb.count == 0) pb.addBlade();
	}
}
