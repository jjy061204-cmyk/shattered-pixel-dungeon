/*
 * Shattered Pixel Dungeon 모드: 영웅 고유 공격 이펙트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.VampireFX;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

//뱀파이어의 비행: 부유와 효과는 같지만, 구름 대신 등 뒤에 붉은 에너지 날개가 돋음
public class BloodWings extends Levitation {

	//처음 걸린 시간 (아이콘이 줄어드는 정도에 씀)
	protected float total = 8f;

	private VampireFX.Wings wings;

	public static <T extends BloodWings> T fly( Char ch, Class<T> cls, float turns ){
		T b = Buff.affect( ch, cls, turns );
		b.total = Math.max( b.total, turns );
		if (b.cooldown() > b.total) b.total = b.cooldown();
		return b;
	}

	@Override
	protected boolean showsClouds() {
		return false;
	}

	@Override
	public void fx( boolean on ) {
		if (target == null || target.sprite == null) return;
		if (on){
			ensureFx( target.sprite );
		} else {
			if (wings != null) wings.killAndErase();
			wings = null;
			//다른 핏빛 날개가 남아 있으면 그쪽이 날개를 이어받음
			ensureFx( target.sprite );
		}
	}

	//날개 그림이 지금 캐릭터 그림을 따라다니는지 확인하고, 없으면 하나 만듦
	// (층을 옮기거나 불러오면 캐릭터 그림이 새로 만들어지므로 HeroSprite.update에서도 부름)
	public static void ensureFx( CharSprite s ){
		if (s == null || s.ch == null || s.parent == null) return;
		BloodWings first = null;
		for (BloodWings w : s.ch.buffs( BloodWings.class )){
			if (w.wings != null && w.wings.parent != null && w.wings.follows( s )) return;
			if (first == null) first = w;
		}
		if (first != null) first.wings = VampireFX.Wings.on( s );
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 1f, 0.2f, 0.3f );
	}

	@Override
	public float iconFadePercent() {
		return Math.max( 0, (total - visualcooldown()) / total );
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc", dispTurns() );
	}

	private static final String TOTAL = "total";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( TOTAL, total );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( TOTAL )) total = bundle.getFloat( TOTAL );
	}
}
