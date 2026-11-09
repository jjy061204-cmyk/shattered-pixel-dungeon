/*
 * Shattered Pixel Dungeon 모드
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

//현대 세계 점원. clerks.png 한 줄에 한 가게 점원 (0번 프레임: 눈 깜빡임, 1번 프레임: 기본)
public class ClerkSprite extends ShopkeeperSprite {

	//가게 순서: 0 편의점, 1 카페, 2 약국, 3 서점, 4 철물점, 5 옷가게, 6 금은방
	protected int row(){
		return 0;
	}

	public ClerkSprite() {
		super();

		texture( Assets.Sprites.CLERKS );
		TextureFilm film = new TextureFilm( texture, 14, 14 );

		int f0 = row() * 2, f1 = row() * 2 + 1;

		idle = new Animation( 10, true );
		idle.frames( film, f1, f1, f1, f1, f1, f1, f1, f0, f1, f1 );

		die = new Animation( 20, false );
		die.frames( film, f0 );

		run = idle.clone();

		attack = idle.clone();

		idle();
	}

	public static class Convenience extends ClerkSprite { @Override protected int row(){ return 0; } }
	public static class Cafe        extends ClerkSprite { @Override protected int row(){ return 1; } }
	public static class Pharmacy    extends ClerkSprite { @Override protected int row(){ return 2; } }
	public static class Bookstore   extends ClerkSprite { @Override protected int row(){ return 3; } }
	public static class Hardware    extends ClerkSprite { @Override protected int row(){ return 4; } }
	public static class Clothing    extends ClerkSprite { @Override protected int row(){ return 5; } }
	public static class Jewelry     extends ClerkSprite { @Override protected int row(){ return 6; } }
	//노점 주인
	public static class Tteokbokki  extends ClerkSprite { @Override protected int row(){ return 7; } }
	public static class Fishbread   extends ClerkSprite { @Override protected int row(){ return 8; } }
	public static class Skewer      extends ClerkSprite { @Override protected int row(){ return 9; } }
	public static class Potato      extends ClerkSprite { @Override protected int row(){ return 10; } }
}
