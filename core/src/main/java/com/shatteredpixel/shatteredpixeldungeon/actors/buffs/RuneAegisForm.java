/*
 * Shattered Pixel Dungeon 모드: 룬 성갑 (마도공학 슈트 형태)
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
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.RuneAegis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

//룬 성갑의 문장(유물)을 켜고 있는 동안 붙는 버프. 그림자의 망토처럼 일정 턴마다 유물 충전을 하나씩 씀.
//충전이 떨어지거나, 유물을 벗거나, 다시 눌러 끄면 풀림. 실제 능력치 계산은 Hero 쪽에서 이 버프가 있는지로 판단
public class RuneAegisForm extends Buff {

	public static final int TURNS_PER_CHARGE = 5;

	//모든 수치는 유물 강화 수치(0~10)에 따라 자람
	public static class RuneSurge {}

	//성갑이 켜져 있으면 유물 강화 수치, 아니면 -1
	public static int level( Hero hero ){
		if (hero.buff( RuneAegisForm.class ) == null) return -1;
		RuneAegis a = hero.belongings.getItem( RuneAegis.class );
		return a == null ? -1 : Math.max( 0, a.level() );
	}

	//정확성: +5% (+0) ~ +30% (+10)
	public static float accuracyBonus( int lvl ){ return 0.05f + 0.025f * lvl; }
	//(받는 피해 감소는 없앰) 변신 직후 얻는 보호막: 8 (+0) ~ 38 (+10). 턴이 지나도 줄지 않음
	public static float damageReduction( int lvl ){ return 0f; }
	public static int shield( int lvl ){ return 8 + 3 * lvl; }
	//방어력: 2~4 (+0) ~ 5~10 (+10)
	public static int drMin( int lvl ){ return 2 + Math.round( 0.3f * lvl ); }
	public static int drMax( int lvl ){ return 4 + Math.round( 0.6f * lvl ); }
	//타격 시 추가 마법 피해: 1~3 (+0) ~ 6~13 (+10)
	public static int magicMin( int lvl ){ return 1 + lvl / 2; }
	public static int magicMax( int lvl ){ return 3 + lvl; }

	public static float accuracy( Hero hero ){
		int l = level( hero );
		return l < 0 ? 1f : 1f + accuracyBonus( l );
	}
	public static float damageTaken( Hero hero ){
		int l = level( hero );
		return l < 0 ? 1f : 1f - damageReduction( l );
	}
	public static int drRoll( Hero hero ){
		int l = level( hero );
		return l < 0 ? 0 : com.watabou.utils.Random.NormalIntRange( drMin( l ), drMax( l ) );
	}
	public static int magicRoll( Hero hero ){
		int l = level( hero );
		return l < 0 ? 0 : com.watabou.utils.Random.NormalIntRange( magicMin( l ), magicMax( l ) );
	}

	private int turns = TURNS_PER_CHARGE;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private RuneAegis sigil(){
		if (!(target instanceof Hero)) return null;
		RuneAegis a = ((Hero) target).belongings.getItem( RuneAegis.class );
		return a != null && a.isEquipped( (Hero) target ) ? a : null;
	}

	@Override
	public boolean act() {
		RuneAegis a = sigil();
		if (a == null){
			detach();
			return true;
		}
		turns--;
		if (turns <= 0){
			if (!a.drain()){
				detach();
				return true;
			}
			turns = TURNS_PER_CHARGE;
		}
		Item.updateQuickslot();
		spend( TICK );
		return true;
	}

	@Override
	public int icon() {
		return BuffIndicator.ARMOR;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 0.25f, 0.9f, 0.85f );
	}

	@Override
	public float iconFadePercent() {
		return (TURNS_PER_CHARGE - turns) / (float)TURNS_PER_CHARGE;
	}

	@Override
	public void detach() {
		Char ch = target;
		super.detach();
		if (ch instanceof Hero && ch.sprite instanceof HeroSprite && ch.isAlive()){
			((HeroSprite) ch.sprite).updateArmor();
			ch.sprite.place( ch.pos );
			ch.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 10 );
			ch.sprite.emitter().burst( RuneMote.FACTORY, 16 );
			Sample.INSTANCE.play( Assets.Sounds.SHATTER, 0.6f, 1.3f );
			Buff.detach( ch, RuneAegisShield.class );
			Item.updateQuickslot();
		}
	}

	@Override
	public String desc() {
		RuneAegis a = sigil();
		int l = a == null ? 0 : Math.max( 0, a.level() );
		return Messages.get( this, "desc", drMin( l ), drMax( l ),
				Math.round( accuracyBonus( l ) * 100 ), magicMin( l ), magicMax( l ), turns, a == null ? 0 : a.charges() );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "turns", turns );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		turns = bundle.contains( "turns" ) ? bundle.getInt( "turns" ) : TURNS_PER_CHARGE;
	}
}
