/*
 * Shattered Pixel Dungeon 모드: 수호의 반지로 두른 마법 갑옷
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.CollectorBonus;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.AegisAssemble;
import com.shatteredpixel.shatteredpixeldungeon.effects.AegisShell;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.RuneAegis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

//반지를 작동시킨 동안 붙는 버프. 5턴마다 반지의 충전을 하나 쓰고, 매 턴 보호막을 조금씩 되살림.
//충전이 떨어지거나, 반지를 벗거나, 다시 눌러 끄면 풀림
public class RuneAegisForm extends Buff {

	public static final int TURNS_PER_CHARGE = 5;

	//보호막 최대치: 10 (+0) ~ 40 (+10)
	public static int shieldMax( int lvl ){
		int m = 10 + 3 * lvl;
		//유물수집가의 고유 효과
		return Math.round( m * CollectorBonus.aegisShieldMultiplier( Dungeon.hero ) );
	}
	//턴마다 재생: 1 (+0) ~ 3 (+10)
	public static int shieldRegen( int lvl ){
		return 1 + lvl / 4;
	}

	private int turns = TURNS_PER_CHARGE;

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
		ensureShell();
		int lvl = Math.max( 0, a.level() );

		RuneAegisShield sh = Buff.affect( target, RuneAegisShield.class );
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
		return Math.max( 0, (TURNS_PER_CHARGE - turns) / (float) TURNS_PER_CHARGE );
	}

	//영웅 위에 씌우는 반투명 갑옷 껍데기
	private AegisShell shell;

	public void ensureShell(){
		if (target == null || target.sprite == null || target.sprite.parent == null) return;
		if (shell != null && shell.parent != null && shell.target() == target.sprite) return;
		if (shell != null) shell.killAndErase();
		shell = new AegisShell( target.sprite, RuneAegis.SHELL );
		target.sprite.parent.add( shell );
	}

	@Override
	public void fx( boolean on ) {
		if (on){
			if (target.sprite != null && target.sprite.parent != null){
				ensureShell();
			} else {
				//불러오는 중에는 영웅 그림이 아직 화면에 붙지 않았으니 다음 프레임에 씌움
				ShatteredPixelDungeon.runOnRenderThread( new Callback() {
					@Override
					public void call() {
						if (target != null && target.buff( RuneAegisForm.class ) == RuneAegisForm.this) ensureShell();
					}
				} );
			}
		} else if (shell != null){
			shell.killAndErase();
			shell = null;
		}
	}

	@Override
	public void detach() {
		Char ch = target;
		super.detach();
		if (ch != null) Buff.detach( ch, RuneAegisShield.class );
		if (ch instanceof Hero && ch.sprite instanceof HeroSprite && ch.isAlive()){
			//갑옷이 입자로 흩어져 사라지는 모습
			if (ch.sprite.parent != null){
				ch.sprite.parent.add( new AegisAssemble( ch.sprite, RuneAegis.SHELL, true, null ) );
				Sample.INSTANCE.play( Assets.Sounds.PUFF, 0.6f, 1.3f );
			}
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
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		turns = bundle.contains( "turns" ) ? bundle.getInt( "turns" ) : TURNS_PER_CHARGE;
	}
}
