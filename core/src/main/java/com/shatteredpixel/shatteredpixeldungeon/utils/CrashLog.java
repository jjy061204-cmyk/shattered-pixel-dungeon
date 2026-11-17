/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 게임이 튕기면 원인(오류 내용)을 파일로 남겨 두었다가, 다음에 켤 때 첫 화면에서 보여 줌.
 * 화면을 찍거나 '복사'로 붙여 넣어 주면 어디서 튕겼는지 바로 찾을 수 있음
 */

package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.watabou.noosa.Game;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashLog {

	private static final String FILE = "mod_crash_log.txt";
	private static boolean installed = false;

	public static void install(){
		if (installed) return;
		installed = true;
		final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException( Thread t, Throwable e ) {
				try { save( t, e ); } catch (Throwable ignored) {}
				if (prev != null) prev.uncaughtException( t, e );
			}
		} );
	}

	private static void save( Thread t, Throwable e ){
		StringWriter sw = new StringWriter();
		e.printStackTrace( new PrintWriter( sw ) );
		String s = "v" + Game.version + " / " + t.getName() + "\n" + sw;
		Gdx.files.local( FILE ).writeString( s, false, "UTF-8" );
	}

	//남아 있는 기록을 꺼내고 지움 (없으면 null)
	public static String take(){
		try {
			FileHandle f = Gdx.files.local( FILE );
			if (!f.exists()) return null;
			String s = f.readString( "UTF-8" );
			f.delete();
			return s;
		} catch (Throwable e){
			return null;
		}
	}

	//창에 보여 줄 짧은 요약: 오류 줄과 앞쪽 호출 위치 몇 줄 (패키지 이름은 줄임)
	public static String summary( String full ){
		String[] lines = full.split( "\n" );
		StringBuilder b = new StringBuilder();
		int frames = 0;
		for (String l : lines){
			String t = l.trim().replace( "com.shatteredpixel.shatteredpixeldungeon.", "" ).replace( "com.watabou.", "" );
			if (t.startsWith( "at " )){
				if (frames >= 10) continue;
				frames++;
			} else if (t.startsWith( "..." )){
				continue;
			} else if (t.startsWith( "Caused by" )){
				frames = 4; //원인 쪽도 몇 줄 보이게
			}
			if (b.length() > 0) b.append( '\n' );
			b.append( t );
			if (b.length() > 1400) break;
		}
		return b.toString();
	}
}
