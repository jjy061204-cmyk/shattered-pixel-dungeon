/*
 * Shattered Pixel Dungeon 모드: 수호의 반지로 두른 마법 갑옷
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.RuneAegis;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

//반지를 작동시킨 동안 붙는 버프. 5턴마다 반지의 충전을 하나 쓰고, 매 턴 보호막을 조금씩 되살림.
//충전이 떨어지거나, 반지를 벗거나, 다시 눌러 끄면 풀림
public class RuneAegisForm extends Buff {

	public static final int TURNS_PER_CHARGE = 5;

	//보호막 최대치: 10 (+0) ~ 40 (+10)
	public static int shieldMax( int lvl ){ return 10 + 3 * lvl; }
	//턴마다 재생: 1 (+0) ~ 3 (+10)
	public static int shieldRegen( int lvl ){ return 1 + lvl / 4; }

	private int turns = TURNS_PER_CHARGE;
	private boolean mirrorUsed = false;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private RuneAegis ring(){
		if (!(target instanceof Hero)) return null;
		RuneAegis a = ((Hero) target).belongings.getItem( RuneAegis.class );
		return a != null && a.isEquipped( (Hero) target ) ? a : null;
	}

	@Override
	public boolean act() {
		RuneAegis a = ring();
		if (a == null){
			detach();
			return true;
		}
		int lvl = Math.max( 0, a.level() );

		RuneAegisShield sh = Buff.affect( target, RuneAegisShield.class );
		if (sh.shielding() <= 0 && !mirrorUsed && a.imbue() == RuneAegis.Imbue.MIRROR){
			//거울상: 보호막이 깨지면 한 번 거울상을 불러냄
			mirrorUsed = true;
			ScrollOfMirrorImage.spawnImages( (Hero) target, 1 );
		}
		if (sh.shielding() < shieldMax( lvl )){
			sh.setShield( Math.min( shieldMax( lvl ), sh.shielding() + shieldRegen( lvl ) ) );
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

	//갑옷을 두른 영웅이 근접 공격을 받았을 때: 화염·서리 속성의 반격
	public static void onDefend( Hero hero, Char enemy ){
		if (enemy == null || hero.buff( RuneAegisForm.class ) == null) return;
		RuneAegis a = hero.belongings.getItem( RuneAegis.class );
		if (a == null || !Dungeon.level.adjacent( hero.pos, enemy.pos )) return;
		if (a.imbue() == RuneAegis.Imbue.FIRE){
			Buff.affect( enemy, Burning.class ).reignite( enemy );
		} else if (a.imbue() == RuneAegis.Imbue.FROST){
			Buff.prolong( enemy, Chill.class, 4f );
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.ARMOR;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 0.7f, 0.85f, 1f );
	}

	@Override
	public float iconFadePercent() {
		return (TURNS_PER_CHARGE - turns) / (float)TURNS_PER_CHARGE;
	}

	@Override
	public void detach() {
		Char ch = target;
		super.detach();
		if (ch != null) Buff.detach( ch, RuneAegisShield.class );
		if (ch instanceof Hero && ch.sprite instanceof HeroSprite && ch.isAlive()){
			((HeroSprite) ch.sprite).updateArmor();
			ch.sprite.place( ch.pos );
			Item.updateQuickslot();
		}
	}

	@Override
	public String desc() {
		RuneAegis a = ring();
		int l = a == null ? 0 : Math.max( 0, a.level() );
		return Messages.get( this, "desc", shieldMax( l ), shieldRegen( l ), turns, a == null ? 0 : a.charges() );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "turns", turns );
		bundle.put( "mirror_used", mirrorUsed );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		turns = bundle.contains( "turns" ) ? bundle.getInt( "turns" ) : TURNS_PER_CHARGE;
		mirrorUsed = bundle.getBoolean( "mirror_used" );
	}
}
