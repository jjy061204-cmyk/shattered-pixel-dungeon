/*
 * Shattered Pixel Dungeon 모드: 또렷한 숫자 표시
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

//현대 세계 화면에서 쓰는 숫자
//- PRICE: 굵은 4x6 숫자 (가격표, 남은 개수, 행사 딱지)
//- LED: 7세그먼트 5x9 숫자 (자판기 금액 표시창). 꺼진 칸에는 흐린 '8'이 비쳐 보임
public class ModernDigits extends Component {

	public static final int PRICE = 0;
	public static final int LED   = 1;

	private static final String PRICE_CHARS = "0123456789x+-%/SOLD";
	private static final String NARROW = "1x+-L";
	private static final String LED_CHARS = "0123456789SOLdFU-";

	private final int style;
	private String text = "";
	private int color = 0xFFFFFF;
	private int ghostColor = -1;
	private int ledCells = 0;

	private ArrayList<Image> glyphs = new ArrayList<>();
	private ArrayList<Float> offsets = new ArrayList<>();

	public ModernDigits( int style ){
		super();
		this.style = style;
	}

	//LED에서 자릿수를 고정하고 꺼진 칸을 흐리게 보여줌 (오른쪽 정렬)
	public void ledCells( int cells, int ghost ){
		ledCells = cells;
		ghostColor = ghost;
	}

	public void text( String t, int color ){
		this.text = t == null ? "" : t;
		this.color = color;
		rebuild();
	}

	public static float glyphWidth( int style, char c ){
		if (style == LED) return 5;
		return NARROW.indexOf( c ) >= 0 ? 3 : 4;
	}

	public static float measure( int style, String t ){
		if (t == null || t.isEmpty()) return 0;
		float w = 0;
		for (int i = 0; i < t.length(); i++) w += glyphWidth( style, t.charAt( i ) ) + 1;
		return w - 1;
	}

	private Image glyph( char c ){
		if (style == LED){
			int i = LED_CHARS.indexOf( c );
			if (i < 0) return null;
			return new Image( Assets.Sprites.MODERN_UI, 136 + i * 6, 8, 5, 9 );
		} else {
			int i = PRICE_CHARS.indexOf( c );
			if (i < 0) return null;
			return new Image( Assets.Sprites.MODERN_UI, 136 + i * 5, 0, (int)glyphWidth( style, c ), 6 );
		}
	}

	private void rebuild(){
		for (Image g : glyphs) g.killAndErase();
		glyphs.clear();
		offsets.clear();

		float pos = 0;
		if (style == LED && ledCells > 0){
			int pad = Math.max( 0, ledCells - text.length() );
			for (int i = 0; i < ledCells; i++){
				Image ghost = glyph( '8' );
				ghost.hardlight( ghostColor );
				add( ghost ); glyphs.add( ghost ); offsets.add( i * 6f );
			}
			pos = pad * 6f;
		}
		for (int i = 0; i < text.length(); i++){
			char c = text.charAt( i );
			Image g = glyph( c );
			float w = glyphWidth( style, c );
			if (g != null){
				g.hardlight( color );
				add( g ); glyphs.add( g ); offsets.add( pos );
			}
			pos += (style == LED ? 6 : w + 1);
		}
		width = style == LED && ledCells > 0 ? ledCells * 6 - 1 : Math.max( 0, pos - 1 );
		height = style == LED ? 9 : 6;
		layout();
	}

	public void alpha( float a ){
		for (Image g : glyphs) g.alpha( a );
	}

	@Override
	protected void layout() {
		for (int i = 0; i < glyphs.size(); i++){
			glyphs.get( i ).x = x + offsets.get( i );
			glyphs.get( i ).y = y;
		}
	}
}
