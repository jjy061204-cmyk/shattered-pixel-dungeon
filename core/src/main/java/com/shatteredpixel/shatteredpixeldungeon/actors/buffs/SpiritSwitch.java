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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.SpiritGlyph;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.SpiritEnchant;

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

	@Override
	public boolean act() {
		refreshAura( false );
		refreshButton();
		spend( TICK );
		return true;
	}

	//버튼을 보이거나 감추고, 다음 정령이 바뀌었으면 그림을 새로 그림
	public void refreshButton(){
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
		} else if (ActionIndicator.action == this && (next != shown || next == null)){
			shown = next;
			ActionIndicator.refresh();
		}
	}

	//버튼에 보일 정령: 바꿀 정령이 있으면 다음 정령, 하나뿐이면 지금 정령
	private static ForestWand.Spirit shownSpirit( ForestWand w ){
		if (w == null || w.spiritCount() <= 0) return null;
		ForestWand.Spirit next = w.nextSpirit();
		return next != null ? next : w.active();
	}

	@Override
	public String actionName() {
		ForestWand w = wand();
		ForestWand.Spirit next = w == null ? null : w.nextSpirit();
		if (next == null) return Messages.get( this, "manage" );
		return Messages.get( this, "action", next.title() );
	}

	@Override
	public Visual primaryVisual() {
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
		//바꿀 정령이 없으면 바로 정령 관리 창
		if (w.nextSpirit() == null){
			openManager( w );
			return;
		}
		ForestWand.Spirit s = w.rotate();
		if (s != null){
			hero.sprite.emitter().burst( s.particle(), 6 );
			Sample.INSTANCE.play( Assets.Sounds.MELD, 0.6f, 1.3f );
			GLog.i( Messages.get( this, "switched", s.title() ) );
		}
		refreshButton();
		ActionIndicator.refresh();
	}

	//길게 누르면 인벤토리를 열지 않고 정령 관리 창 (풀어 주기·부여·거두기)
	@Override
	public boolean doLongAction() {
		ForestWand w = wand();
		if (w == null || Dungeon.hero == null || !Dungeon.hero.ready) return false;
		openManager( w );
		return true;
	}

	private void openManager( ForestWand w ){
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				new com.shatteredpixel.shatteredpixeldungeon.windows.WndForestWand( w ) );
	}

	//===== 정령이 깃든 장비를 착용하는 동안 영웅 주위에 계속 피어오르는 정령 기운 =====
	//[0],[1] 무기의 정령 (진하게)  [2],[3] 갑옷의 정령 (은은하게)
	private final Emitter[] aura = new Emitter[4];
	private final ForestWand.Spirit[] auraKey = new ForestWand.Spirit[4];

	public static void refreshAuraOf( Char ch ){
		SpiritSwitch b = ch == null ? null : ch.buff( SpiritSwitch.class );
		if (b != null) b.refreshAura( false );
	}

	private ForestWand.Spirit[] equipped(){
		ForestWand.Spirit[] r = new ForestWand.Spirit[4];
		if (!(target instanceof Hero)) return r;
		Hero h = (Hero) target;
		KindOfWeapon kw = h.belongings.weapon();
		if (kw instanceof Weapon && ((Weapon) kw).enchantment instanceof SpiritEnchant){
			SpiritEnchant e = (SpiritEnchant) ((Weapon) kw).enchantment;
			r[0] = e.spirit; r[1] = e.spirit2;
		}
		Armor a = h.belongings.armor();
		if (a != null && a.glyph instanceof SpiritGlyph){
			SpiritGlyph g = (SpiritGlyph) a.glyph;
			r[2] = g.spirit; r[3] = g.spirit2;
		}
		return r;
	}

	private void refreshAura( boolean rebuild ){
		if (target == null || target.sprite == null) return;
		ForestWand.Spirit[] now = equipped();
		for (int i = 0; i < 4; i++){
			boolean stale = aura[i] != null && (!aura[i].alive || aura[i].parent == null);
			if (!rebuild && !stale && now[i] == auraKey[i] && (now[i] == null) == (aura[i] == null)) continue;
			if (aura[i] != null) aura[i].on = false;
			aura[i] = null;
			auraKey[i] = now[i];
			if (now[i] == null || target.sprite.parent == null) continue;
			Emitter e = target.sprite.emitter();
			if (e == null) continue;
			boolean weapon = i < 2;
			boolean dual = now[i ^ 1] != null;
			float interval = weapon ? (now[i].strong ? 0.07f : 0.1f) : (now[i].strong ? 0.16f : 0.22f);
			if (dual) interval *= 1.6f;
			e.pour( now[i].particle(), interval );
			aura[i] = e;
		}
		for (Emitter e : aura){
			if (e != null) e.visible = target.sprite.visible;
		}
	}

	@Override
	public void fx( boolean on ) {
		if (on){
			refreshAura( true );
		} else {
			for (int i = 0; i < 4; i++){
				if (aura[i] != null) aura[i].on = false;
				aura[i] = null;
				auraKey[i] = null;
			}
		}
	}

}
