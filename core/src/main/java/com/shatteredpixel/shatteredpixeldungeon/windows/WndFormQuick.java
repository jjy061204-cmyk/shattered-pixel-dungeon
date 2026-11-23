/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 형상 기사의 형상 단축 창: 등록한 무기 모양 4칸.
 *  칸을 누르면 그 모양으로 빚고, 빈 칸이나 톱니를 누르면 다른 모양을 등록(또는 비우기)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FormQuick;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherClay;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherForms;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Reflection;

public class WndFormQuick extends Window {

	private static final int WIDTH = 124;
	private static final int ROW = 20;
	private static final int GAP = 2;

	public WndFormQuick( final EtherClay clay ){
		super();
		final Hero hero = Dungeon.hero;

		float pos = GAP;
		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.titleCase( Messages.get( this, "title" ) ), 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width())/2f, pos );
		add( title );
		pos = title.bottom() + GAP;

		RenderedTextBlock hint = PixelScene.renderTextBlock( Messages.get( this, "hint" ), 6 );
		hint.maxWidth( WIDTH );
		hint.setPos( 0, pos );
		add( hint );
		pos = hint.bottom() + 2*GAP;

		for (int i = 0; i < EtherClay.QUICK_SLOTS; i++){
			final int slot = i;
			final Class<? extends MeleeWeapon> cls = clay.quickForm( i );
			MeleeWeapon w = cls != null ? Reflection.newInstance( cls ) : null;
			final int tier = cls != null ? EtherClay.tierOf( cls ) : 0;
			boolean usable = cls != null && tier <= EtherClay.maxTier( hero );

			String label;
			if (w == null){
				label = Messages.get( this, "empty" );
			} else {
				label = Messages.titleCase( w.trueName() ) + " (" + Messages.get( this, "tier", tier ) + ")";
				if (cls == clay.formClass()) label += " " + Messages.get( EtherClay.class, "current" );
			}

			RedButton btn = new RedButton( label, 7 ){
				@Override
				protected void onClick() {
					super.onClick();
					if (cls == null){
						assign( clay, slot );
					} else {
						hide();
						FormQuick.shapeSlot( hero, clay, slot );
					}
				}
			};
			btn.leftJustify = true;
			btn.setRect( ROW + GAP, pos, WIDTH - 2*(ROW + GAP), ROW );
			btn.enable( hero.ready && (cls == null || (usable && cls != clay.formClass())) );
			add( btn );

			ItemSprite icon = new ItemSprite( w != null ? EtherClay.formImage( w ) : ItemSpriteSheet.WEAPON_HOLDER, null );
			icon.x = (ROW - icon.width())/2f;
			icon.y = pos + (ROW - icon.height())/2f;
			if (cls != null && !usable) icon.alpha( 0.4f );
			add( icon );

			IconButton edit = new IconButton( Icons.get( Icons.PREFS ) ){
				@Override
				protected void onClick() {
					if (cls == null) assign( clay, slot );
					else editSlot( clay, slot );
				}
			};
			edit.setRect( WIDTH - ROW, pos, ROW, ROW );
			add( edit );

			pos += ROW + GAP;
		}

		//지금 모양의 효과 한 줄
		if (hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.FORM_KNIGHT){
			RenderedTextBlock eff = PixelScene.renderTextBlock( EtherForms.describe( hero, clay.currentForm() ), 6 );
			eff.maxWidth( WIDTH );
			eff.setPos( 0, pos + GAP );
			add( eff );
			pos = eff.bottom() + GAP;
		}

		resize( WIDTH, (int) pos );
	}

	private void assign( final EtherClay clay, final int slot ){
		hide();
		clay.pickForm( Dungeon.hero, Messages.get( WndFormQuick.class, "assign_title", slot + 1 ), new EtherClay.FormPicked() {
			@Override
			public void picked( Class<? extends MeleeWeapon> cls ) {
				clay.setQuickForm( slot, cls );
				GameScene.show( new WndFormQuick( clay ) );
			}
		} );
	}

	private void editSlot( final EtherClay clay, final int slot ){
		GameScene.show( new WndOptions( Icons.get( Icons.PREFS ),
				Messages.get( WndFormQuick.class, "edit_title", slot + 1 ),
				Messages.get( WndFormQuick.class, "edit_desc" ),
				Messages.get( WndFormQuick.class, "change" ),
				Messages.get( WndFormQuick.class, "clear" ) ){
			@Override
			protected void onSelect( int index ) {
				if (index == 0){
					WndFormQuick.this.assign( clay, slot );
				} else if (index == 1){
					clay.setQuickForm( slot, null );
					WndFormQuick.this.hide();
					GameScene.show( new WndFormQuick( clay ) );
				}
			}
		} );
	}
}
