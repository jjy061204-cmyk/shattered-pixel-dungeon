/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 갈림길: 광산(11~15층)과 지옥(21~25층)에 들어갈 때 테마를 고름
 *  - 하수도·감옥·드워프 도시는 원작 그대로
 *  - 고른 테마는 저장되며 바꿀 수 없음. 고르지 않은 채 내려가면(예전 저장 등) 원작 테마
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class Routes {

	//광산 (11~15층)
	public enum Caves {
		MINES( 2, true ),            //원작 광산
		GNOLL_EMPIRE( 3, true ),     //놀 군단이 DM-300과 기계들을 몰아내고 세운 놀 제국
		VAMPIRE_CASTLE( 3, true ),   //광산에 숨어든 뱀파이어가 생물들을 감염시킨 흡혈귀의 성
		CRYSTAL_VEIN( 2, true );    //드워프들이 캐다 깨운 수정 생명체가 뒤덮은 수정 광맥

		public final int danger;
		//만들어진 길인지 (아직이면 갈림길에서 고를 수 없음)
		public final boolean ready;
		Caves( int danger, boolean ready ){ this.danger = danger; this.ready = ready; }
		public String title(){ return Messages.get( Routes.class, "caves_" + name().toLowerCase() ); }
		public String desc(){ return Messages.get( Routes.class, "caves_" + name().toLowerCase() + "_desc" ); }
	}

	//지옥 (21~25층)
	public enum Halls {
		DEMON_HALLS( 3, true ),          //원작 지옥 (요그제바)
		DRAGON_RUINS( 4, false ),        //요그제바가 고룡을 조종하는 용의 유적
		SHATTERED_DIMENSION( 4, false ), //아뮬렛의 힘이 시공을 뒤틀어 온갖 테마가 뒤섞인 깨어진 이차원
		FROZEN_ABYSS( 3, false );        //요그제바의 냉기가 지옥 불을 꺼 버린 얼어붙은 심연

		public final int danger;
		public final boolean ready;
		Halls( int danger, boolean ready ){ this.danger = danger; this.ready = ready; }
		public String title(){ return Messages.get( Routes.class, "halls_" + name().toLowerCase() ); }
		public String desc(){ return Messages.get( Routes.class, "halls_" + name().toLowerCase() + "_desc" ); }
	}

	public static Caves caves = null;
	public static Halls halls = null;

	public static void reset(){
		caves = null;
		halls = null;
	}

	//고르지 않았으면 원작 테마
	public static Caves caves(){
		return caves == null ? Caves.MINES : caves;
	}

	public static Halls halls(){
		return halls == null ? Halls.DEMON_HALLS : halls;
	}

	private static final String NODE = "routes";

	public static void storeInBundle( Bundle bundle ){
		Bundle node = new Bundle();
		if (caves != null) node.put( "caves", caves.name() );
		if (halls != null) node.put( "halls", halls.name() );
		bundle.put( NODE, node );
	}

	public static void restoreFromBundle( Bundle bundle ){
		reset();
		Bundle node = bundle.getBundle( NODE );
		if (node.isNull()) return;
		try {
			if (node.contains( "caves" )) caves = Caves.valueOf( node.getString( "caves" ) );
			if (node.contains( "halls" )) halls = Halls.valueOf( node.getString( "halls" ) );
		} catch (Exception e){
			//알 수 없는 이름이면 원작 테마
		}
	}

	private static String stars( int n ){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) sb.append( i < n ? "★" : "☆" );
		return sb.toString();
	}

	//이 계단으로 내려가기 전에 갈림길을 골라야 하는지
	public static boolean needsChoice( LevelTransition transition ){
		if (Dungeon.branch != 0 || transition.type != LevelTransition.Type.REGULAR_EXIT) return false;
		if (Dungeon.depth == 10 && caves == null) return true;
		if (Dungeon.depth == 20 && halls == null) return true;
		return false;
	}

	//낭떠러지·구덩이 함정으로 떨어질 때도 갈림길을 골라야 하는지 (10층→광산, 20층→지옥)
	public static boolean needsFallChoice(){
		if (Dungeon.branch != 0) return false;
		if (Dungeon.depth == 10 && caves == null) return true;
		if (Dungeon.depth == 20 && halls == null) return true;
		return false;
	}

	//갈림길 창: 테마를 누르면 소개를 보고 그 길로 내려감
	public static void choose( final Hero hero, final LevelTransition transition ){
		chooseThen( new Callback() {
			@Override
			public void call() {
				Dungeon.level.activateTransition( hero, transition );
			}
		}, false );
	}

	//테마를 고른 뒤 then 을 부름 (계단·낙하 공용)
	//forced: 떨어지는 중이라 창을 닫을 수 없음 (반드시 골라야 함)
	public static void chooseThen( final Callback then, final boolean forced ){
		final boolean isCaves = Dungeon.depth == 10;
		final Enum<?>[] opts = isCaves ? Caves.values() : Halls.values();
		final String[] labels = new String[opts.length];
		final boolean[] ready = new boolean[opts.length];
		for (int i = 0; i < opts.length; i++){
			if (isCaves) { labels[i] = ((Caves) opts[i]).title(); ready[i] = ((Caves) opts[i]).ready; }
			else         { labels[i] = ((Halls) opts[i]).title(); ready[i] = ((Halls) opts[i]).ready; }
			if (!ready[i]) labels[i] = Messages.get( Routes.class, "not_ready", labels[i] );
		}
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndOptions( Icons.get( Icons.STAIRS ),
						Messages.get( Routes.class, isCaves ? "title_caves" : "title_halls" ),
						Messages.get( Routes.class, isCaves ? "msg_caves" : "msg_halls" ),
						labels ){
					@Override
					protected void onSelect( int index ) {
						confirm( then, isCaves, opts[index], forced );
					}

					@Override
					public void onBackPressed() {
						if (!forced) super.onBackPressed();
					}

					@Override
					protected boolean enabled( int index ) {
						return ready[index];
					}

					@Override
					protected boolean hasInfo( int index ) {
						return true;
					}

					@Override
					protected void onInfo( int index ) {
						Enum<?> o = opts[index];
						String title = isCaves ? ((Caves) o).title() : ((Halls) o).title();
						String desc = isCaves ? ((Caves) o).desc() : ((Halls) o).desc();
						int danger = isCaves ? ((Caves) o).danger : ((Halls) o).danger;
						GameScene.show( new WndTitledMessage( Icons.get( Icons.STAIRS ), title,
								desc + "\n\n" + Messages.get( Routes.class, "danger", stars( danger ) ) ) );
					}
				} );
			}
		} );
	}

	private static void confirm( final Callback then, final boolean isCaves, final Enum<?> o, final boolean forced ){
		String title = isCaves ? ((Caves) o).title() : ((Halls) o).title();
		String desc = isCaves ? ((Caves) o).desc() : ((Halls) o).desc();
		int danger = isCaves ? ((Caves) o).danger : ((Halls) o).danger;
		GameScene.show( new WndOptions( Icons.get( Icons.STAIRS ), title,
				desc + "\n\n" + Messages.get( Routes.class, "danger", stars( danger ) ) + "\n\n" + Messages.get( Routes.class, "confirm_msg" ),
				Messages.get( Routes.class, "confirm_yes" ), Messages.get( Routes.class, "confirm_no" ) ){
			@Override
			protected void onSelect( int index ) {
				if (index == 0){
					if (isCaves) caves = (Caves) o;
					else         halls = (Halls) o;
					then.call();
				} else {
					chooseThen( then, forced );
				}
			}

			@Override
			public void onBackPressed() {
				if (!forced) super.onBackPressed();
				else { hide(); chooseThen( then, true ); }
			}
		} );
	}
}
