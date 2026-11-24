/*
 * Shattered Pixel Dungeon 모드: 정령사의 '정령 바꾸기' 버튼
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSpiritArts;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;

//숲의 완드에 정령이 둘 이상 깃들어 있으면, 퀵슬롯 옆 행동 버튼(전사의 연속 공격 버튼 자리)에
//'다음에 발현될 정령'이 떠서 한 번 눌러 바로 바꿀 수 있음. 턴을 쓰지 않으며, 퀵슬롯·다른 아이템과 겹치지 않음
public class SpiritSwitch extends Buff implements ActionIndicator.Action {

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	private ForestWand.Spirit shown = null;

	public static void attach( Hero hero ){
		if (hero.buff( SpiritSwitch.class ) == null) Buff.affect( hero, SpiritSwitch.class );
	}

	private static ForestWand wand(){
		Hero h = Dungeon.hero;
		return h == null ? null : h.belongings.getItem( ForestWand.class );
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;   //버프 칸에는 안 보임
	}

	//게임을 시작하거나 층을 옮겨 화면이 새로 그려질 때 곧바로 버튼을 띄움 (첫 턴을 기다리지 않음)
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

	public void refreshButton(){
		refreshButton( false );
	}

	//버튼을 보이거나 감추고, 발현 중인 정령이 바뀌었으면(또는 force) 그림을 새로 그림
	public void refreshButton( boolean force ){
		ForestWand w = wand();
		ForestWand.Spirit next = shownSpirit( w );
		if (target != Dungeon.hero || w == null){
			ActionIndicator.clearAction( this );
			shown = null;
			return;
		}
		if (ActionIndicator.action == null){
			shown = next;
			ActionIndicator.setAction( this );
		} else if (ActionIndicator.action == this && (force || next != shown || next == null)){
			shown = next;
			ActionIndicator.refresh();
		}
	}

	//버튼에 보일 정령: 지금 발현 중인(가운데 칸) 정령. 가운데가 비었으면 완드 그림
	private static ForestWand.Spirit shownSpirit( ForestWand w ){
		if (w == null) return null;
		return w.active();
	}

	private static String quick( ForestWand w ){
		return w == null || w.quickArt == null ? WndSpiritArts.SWITCH : w.quickArt;
	}

	@Override
	public String actionName() {
		ForestWand w = wand();
		if (!quick( w ).equals( WndSpiritArts.SWITCH )) return WndSpiritArts.artName( quick( w ) );
		ForestWand.Spirit next = w == null ? null : w.nextSpirit();
		if (next == null) return Messages.get( this, "manage" );
		return Messages.get( this, "action", next.title() );
	}

	@Override
	public Visual primaryVisual() {
		ForestWand w0 = wand();
		//정령 바꾸기가 아닌 정령술을 등록했으면 그 정령술 그림
		if (w0 != null && !quick( w0 ).equals( WndSpiritArts.SWITCH )) return WndSpiritArts.artIcon( w0, quick( w0 ) );
		ForestWand.Spirit next = shownSpirit( wand() );
		Image img = new Image( Assets.Sprites.ITEM_ICONS );
		if (next != null){
			img.frame( ItemSpriteSheet.Icons.film.get( next.icon() ) );
			return img;
		}
		//정령이 없으면 완드 그림 (누르면 정령 관리 창)
		return new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite( ItemSpriteSheet.FOREST_WAND );
	}

	@Override
	public int indicatorColor() {
		ForestWand.Spirit next = shownSpirit( wand() );
		return next == null ? 0x6A8A40 : next.color;
	}

	@Override
	public void doAction() {
		Hero hero = Dungeon.hero;
		ForestWand w = wand();
		if (hero == null || w == null || !hero.ready) return;
		String q = quick( w );
		//등록한 정령술을 지금 쓸 수 없으면(바꿀 정령이 없는 등) 정령술 창을 엶
		if (!WndSpiritArts.canUse( w, q )){
			openArts( w );
			return;
		}
		WndSpiritArts.use( w, q );
	}

	//길게 누르면 정령사의 모든 일을 담은 정령술 창
	@Override
	public boolean doLongAction() {
		ForestWand w = wand();
		if (w == null || Dungeon.hero == null || !Dungeon.hero.ready) return false;
		openArts( w );
		return true;
	}

	private void openArts( ForestWand w ){
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show( new com.shatteredpixel.shatteredpixeldungeon.windows.WndForestWand( w ) );
	}

	//다음 정령을 가운데로 (턴을 쓰지 않음)
	public static void switchNext(){
		Hero hero = Dungeon.hero;
		ForestWand w = wand();
		if (hero == null || w == null) return;
		ForestWand.Spirit s = w.rotate();
		announce( hero, s );
	}

	//고른 칸의 정령을 바로 가운데로 (턴을 쓰지 않음)
	public static void bringToCenter( int slot ){
		Hero hero = Dungeon.hero;
		ForestWand w = wand();
		if (hero == null || w == null || w.slots[slot] == null) return;
		w.moveToCenter( slot );
		com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();
		announce( hero, w.active() );
	}

	private static void announce( Hero hero, ForestWand.Spirit s ){
		if (s != null){
			hero.sprite.emitter().burst( s.particle(), 6 );
			Sample.INSTANCE.play( Assets.Sounds.MELD, 0.6f, 1.3f );
			GLog.i( Messages.get( SpiritSwitch.class, "switched", s.title() ) );
		}
		SpiritSwitch sw = hero.buff( SpiritSwitch.class );
		if (sw != null) sw.refreshButton( true );
		ActionIndicator.refresh();
	}
}
