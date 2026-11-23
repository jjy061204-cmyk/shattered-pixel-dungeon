/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사 갑옷 능력 1: 정령 투사
 *  영웅 둘레 8칸 안의 땅(풀·물·불·맨땅 같은 촉매)에서 정령 8개체를 불러내, 고른 적 하나에게 모두 쏘아 보냄.
 *  부를 수 있는 칸이 8칸보다 적으면 있는 칸에서 여러 번 불러내 8개체를 채움.
 *  정령마다 그 속성의 피해 (속성 저항·면역이 적용됨)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.elementalist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpiritMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;

public class SpiritVolley extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	public static final int RANGE = 8;
	public static final int BASE_COUNT = 8;
	//정령 한 개체의 속성 피해 배율 (속성 피해 기준값 × 이 값)
	public static final float MUL = 0.45f;

	//정령 군단: 불러내는 정령 +1/2/3/4
	public static int count( Hero hero ){
		return BASE_COUNT + hero.pointsInTalent( Talent.SPIRIT_LEGION );
	}

	@Override
	public String targetingPrompt() {
		return Messages.get( this, "prompt" );
	}

	private int pending;
	private Char mainTarget;

	@Override
	protected void activate( ClassArmor armor, final Hero hero, Integer target ) {
		if (target == null) return;
		final Char enemy = Actor.findChar( target );
		if (enemy == null || enemy == hero || enemy.alignment == Char.Alignment.ALLY
				|| !Dungeon.level.heroFOV[target] || enemy.sprite == null){
			GLog.w( Messages.get( this, "no_target" ) );
			return;
		}

		//8칸 안에서 정령을 부를 수 있는 칸
		ArrayList<Integer> cells = new ArrayList<>();
		ArrayList<ForestWand.Spirit> kinds = new ArrayList<>();
		for (int c = 0; c < Dungeon.level.length(); c++){
			if (c == hero.pos || c == enemy.pos) continue;
			if (Dungeon.level.distance( hero.pos, c ) > RANGE) continue;
			ForestWand.Spirit s = ForestWand.catalystAt( c );
			if (s == null || s.greater) continue;
			cells.add( c );
			kinds.add( s );
		}
		if (cells.isEmpty()){
			GLog.w( Messages.get( this, "no_spirits" ) );
			return;
		}

		//가까운 칸부터 고르되 같은 거리끼리는 섞음 → 부족하면 다시 처음부터 돌며 채움
		ArrayList<Integer> order = new ArrayList<>();
		for (int i = 0; i < cells.size(); i++) order.add( i );
		Collections.shuffle( order );
		final ArrayList<Integer> fCells = cells;
		Collections.sort( order, ( a, b ) -> Dungeon.level.distance( hero.pos, fCells.get( a ) ) - Dungeon.level.distance( hero.pos, fCells.get( b ) ) );

		int n = count( hero );
		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();
		Invisibility.dispel();

		hero.busy();
		hero.sprite.zap( enemy.pos );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 1f, 1.1f );
		new Flare( 6, 20 ).color( 0x9CFFC8, true ).show( hero.sprite, 0.6f );

		pending = n;
		mainTarget = enemy;
		for (int i = 0; i < n; i++){
			int idx = order.get( i % order.size() );
			final int from = cells.get( idx );
			final ForestWand.Spirit s = kinds.get( idx );
			SpiritMissile.launch( s, from, enemy.sprite, i * 0.07f, new Callback() {
				@Override
				public void call() {
					strike( hero, s, enemy, true );
				}
			} );
		}
	}

	//정령 하나가 닿음. 대상이 이미 쓰러졌으면 연쇄 투사로 가까운 다른 적에게
	private void strike( final Hero hero, final ForestWand.Spirit s, final Char enemy, boolean first ){
		if (enemy.isAlive()){
			impact( s, enemy );
			ForestWand.elemHit( s, enemy, MUL );
			done( hero );
			return;
		}
		int chain = hero.pointsInTalent( Talent.SPIRIT_CHAIN );
		Char next = null;
		if (chain > 0){
			int best = Integer.MAX_VALUE;
			for (Char ch : Actor.chars()){
				if (ch == hero || ch.alignment == Char.Alignment.ALLY || !ch.isAlive() || ch.sprite == null) continue;
				if (!Dungeon.level.heroFOV[ch.pos]) continue;
				int d = Dungeon.level.distance( enemy.pos, ch.pos );
				if (d <= 2 * chain && d < best){ best = d; next = ch; }
			}
		}
		if (next != null){
			final Char nx = next;
			SpiritMissile.launch( s, enemy.pos, nx.sprite, 0f, new Callback() {
				@Override
				public void call() {
					if (nx.isAlive()){
						impact( s, nx );
						ForestWand.elemHit( s, nx, MUL );
					}
					done( hero );
				}
			} );
		} else {
			CellEmitter.center( enemy.pos ).burst( s.particle(), 4 );
			done( hero );
		}
	}

	private static void impact( ForestWand.Spirit s, Char ch ){
		if (ch.sprite == null) return;
		ch.sprite.centerEmitter().burst( s.particle(), s.strong ? 7 : 5 );
		Splash.at( ch.sprite.center(), 0xCC000000 | s.color, 4 );
		Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 0.5f, Random.Float( 0.9f, 1.3f ) );
	}

	private void done( Hero hero ){
		Char main = mainTarget;
		pending--;
		if (pending > 0) return;
		//정령의 속박: 남은 대상을 1/2/3/4턴 동안 묶고 느리게
		int bind = hero.pointsInTalent( Talent.SPIRIT_BIND );
		if (bind > 0 && main != null && main.isAlive()){
			Buff.prolong( main, Roots.class, bind );
			Buff.prolong( main, Slow.class, bind );
		}
		hero.spendAndNext( Actor.TICK );
	}

	@Override
	public int icon() {
		return HeroIcon.SPIRIT_VOLLEY;
	}

	@Override
	public String desc() {
		String d = Messages.get( this, "desc" );
		if (Dungeon.hero != null){
			int b = Dungeon.scalingDepth();
			d += "\n\n" + Messages.get( this, "dmg", Math.round( (5 + b) * MUL ), Math.round( (10 + 2*b) * MUL ) );
		}
		return d + "\n\n" + Messages.get( this, "cost", (int) baseChargeUse );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.SPIRIT_LEGION, Talent.SPIRIT_CHAIN, Talent.SPIRIT_BIND, Talent.HEROIC_ENERGY };
	}
}
