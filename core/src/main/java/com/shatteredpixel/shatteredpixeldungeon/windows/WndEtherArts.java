/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 에테르 기사의 에테르 기술 창: 지금 모인 에테르와 네 가지 기술 (모자라면 흐리게)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.StarEther;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndEtherArts extends Window {

	private static final int WIDTH_P = 124;
	private static final int WIDTH_L = 180;
	private static final int MARGIN  = 2;
	private static final int ICON    = 16;

	public WndEtherArts( final StarEther ether ){
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.titleCase( Messages.get( this, "title" ) ), 9 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( width - MARGIN*2 );
		title.setPos( (width - title.width())/2f, pos );
		add( title );
		pos = title.bottom() + MARGIN;

		RenderedTextBlock now = PixelScene.renderTextBlock(
				Messages.get( this, "energy", ether.percent(), (int) StarEther.maxEnergy( Dungeon.hero ) ), 6 );
		now.maxWidth( width - MARGIN*2 );
		now.setPos( (width - now.width())/2f, pos );
		add( now );
		pos = now.bottom() + 3*MARGIN;

		for (final StarEther.Art art : StarEther.Art.values()){
			String text = "_" + Messages.titleCase( art.title() ) + " (" + art.cost + "%):_ " + art.desc();
			RedButton btn = new RedButton( text, 6 ){
				@Override
				protected void onClick() {
					super.onClick();
					hide();
					ether.use( art );
				}
			};
			btn.leftJustify = true;
			btn.multiline = true;
			btn.setSize( width - ICON - MARGIN, btn.reqHeight() );
			float h = Math.max( ICON, btn.reqHeight() );
			btn.setRect( ICON + MARGIN, pos, width - ICON - MARGIN, h );
			boolean ok = ether.canUse( art ) && Dungeon.hero.ready;
			btn.enable( ok );
			add( btn );

			HeroIcon ic = new HeroIcon( art.icon );
			ic.x = 0;
			ic.y = pos + (h - ICON) / 2f;
			if (!ok) ic.alpha( 0.4f );
			add( ic );

			pos = btn.bottom() + MARGIN;
		}

		resize( width, (int) pos );
	}
}
