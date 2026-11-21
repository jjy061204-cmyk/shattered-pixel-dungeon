/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 기사의 경례 단축 버튼: 특성을 찍으면 퀵슬롯 옆 행동 버튼(전사의 연속 공격 버튼 자리)에 '경례'가 떠서
 * 에테르 점토를 열지 않고 한 번 눌러 바로 경례함
 *  - 경례할 수 있을 때(적이 보이지 않고, 아직 경례하지 않았을 때)만 보임
 *  - 연속 공격·광폭화 같은 다른 버튼이 떠 있으면 그쪽을 가리지 않음
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherClay;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.watabou.noosa.Visual;

public class SaluteButton extends Buff implements ActionIndicator.Action {

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	public static void attach( Hero hero ){
		if (hero.pointsInTalent( Talent.KNIGHTS_SALUTE ) > 0 && hero.buff( SaluteButton.class ) == null){
			Buff.affect( hero, SaluteButton.class );
		}
	}

	private static EtherClay clay(){
		Hero h = Dungeon.hero;
		return h == null ? null : h.belongings.getItem( EtherClay.class );
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;   //버프 칸에는 안 보임
	}

	//화면이 새로 그려질 때 첫 턴을 기다리지 않고 바로
	@Override
	public void fx( boolean on ) {
		if (on) refreshButton();
	}

	@Override
	public boolean act() {
		refreshButton();
		spend( TICK );
		return true;
	}

	private boolean canSalute(){
		if (!(target instanceof Hero) || target != Dungeon.hero || !target.isAlive()) return false;
		Hero hero = (Hero) target;
		return hero.pointsInTalent( Talent.KNIGHTS_SALUTE ) > 0
				&& clay() != null
				&& hero.buff( Talent.KnightsSaluteBuff.class ) == null
				&& hero.visibleEnemies() == 0;
	}

	public void refreshButton(){
		if (canSalute()){
			if (ActionIndicator.action == null){
				ActionIndicator.setAction( this );
			}
		} else if (ActionIndicator.action == this){
			ActionIndicator.clearAction( this );
		}
	}

	@Override
	public String actionName() {
		return Messages.get( EtherClay.class, "ac_salute" );
	}

	@Override
	public Visual primaryVisual() {
		return new TalentIcon( Talent.KNIGHTS_SALUTE );
	}

	@Override
	public int indicatorColor() {
		return 0xB89CE8;   //에테르 점토의 연보랏빛
	}

	@Override
	public void doAction() {
		Hero hero = Dungeon.hero;
		EtherClay c = clay();
		if (hero == null || c == null || !hero.ready) return;
		c.execute( hero, EtherClay.AC_SALUTE );
		refreshButton();
	}
}
