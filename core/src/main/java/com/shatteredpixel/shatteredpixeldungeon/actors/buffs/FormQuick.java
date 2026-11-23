/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 형상 기사의 '형상 단축' 행동 버튼: 자주 쓰는 무기 모양 4개를 등록해 두고 바로 빚음.
 *  - 누르면 단축 창 (칸을 누르면 그 모양으로 빚기, 빈 칸·톱니를 누르면 등록)
 *  - 길게 누르면 등록한 다음 모양으로 바로 빚기
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherClay;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndFormQuick;
import com.watabou.noosa.Visual;
import com.watabou.utils.Reflection;

public class FormQuick extends Buff implements ActionIndicator.Action {

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	public static EtherClay clay( Hero hero ){
		return hero == null ? null : hero.belongings.getItem( EtherClay.class );
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public boolean act() {
		refreshButton();
		spend( TICK );
		return true;
	}

	@Override
	public void fx( boolean on ) {
		if (on) refreshButton();
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction( this );
	}

	private boolean wantsButton(){
		Hero hero = target instanceof Hero ? (Hero) target : null;
		return hero != null && hero == Dungeon.hero && hero.isAlive()
				&& hero.subClass == HeroSubClass.FORM_KNIGHT && clay( hero ) != null;
	}

	public void refreshButton(){
		if (wantsButton()){
			if (ActionIndicator.action != this) ActionIndicator.setAction( this );
			else ActionIndicator.refresh();
		} else if (ActionIndicator.action == this){
			ActionIndicator.clearAction( this );
		}
	}

	@Override
	public String actionName() {
		return Messages.get( this, "action_name" );
	}

	@Override
	public Visual primaryVisual() {
		EtherClay c = clay( Dungeon.hero );
		return new ItemSprite( c != null ? c.image() : com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.ETHER_CLAY, null );
	}

	@Override
	public int indicatorColor() {
		return 0x5E5490;
	}

	@Override
	public void doAction() {
		EtherClay c = clay( Dungeon.hero );
		if (c != null) GameScene.show( new WndFormQuick( c ) );
	}

	//길게 누르면: 등록한 모양 가운데 지금 모양 다음 것으로 바로 빚음
	@Override
	public boolean doLongAction() {
		Hero hero = Dungeon.hero;
		EtherClay c = clay( hero );
		if (c == null || !hero.ready) return false;
		int cur = -1;
		for (int i = 0; i < EtherClay.QUICK_SLOTS; i++) if (c.quickForm( i ) != null && c.quickForm( i ) == c.formClass()) cur = i;
		for (int k = 1; k <= EtherClay.QUICK_SLOTS; k++){
			int i = (cur + k + EtherClay.QUICK_SLOTS) % EtherClay.QUICK_SLOTS;
			Class<? extends MeleeWeapon> cls = c.quickForm( i );
			if (cls != null && cls != c.formClass()){
				shapeSlot( hero, c, i );
				return true;
			}
		}
		GLog.w( Messages.get( this, "none" ) );
		return true;
	}

	//등록한 칸의 모양으로 빚기
	public static void shapeSlot( Hero hero, EtherClay c, int i ){
		Class<? extends MeleeWeapon> cls = c.quickForm( i );
		if (cls == null) return;
		int tier = EtherClay.tierOf( cls );
		if (tier > EtherClay.maxTier( hero )){
			GLog.w( Messages.get( FormQuick.class, "too_high", tier ) );
			return;
		}
		if (cls == c.formClass()){
			GLog.i( Messages.get( FormQuick.class, "already", Messages.titleCase( Reflection.newInstance( cls ).trueName() ) ) );
			return;
		}
		c.doShape( hero, cls );
		ActionIndicator.refresh();
	}
}
