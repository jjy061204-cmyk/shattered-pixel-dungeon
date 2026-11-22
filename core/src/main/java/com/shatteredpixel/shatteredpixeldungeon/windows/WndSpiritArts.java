/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사의 '정령술' 창: 정령사가 할 수 있는 일을 모두 아이콘 버튼으로 한 화면에
 *  - 누르면 바로 씀, 길게 누르면 행동 버튼(퀵슬롯 옆)에 등록 → 행동 버튼을 누르면 그 정령술을 바로 씀
 *  - 정보 버튼으로 설명 보기 모드
 *  - 행동 버튼을 길게 누르면 언제든 이 창이 열림
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SpiritSwitch;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.levels.ModernLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;

public class WndSpiritArts extends Window {

	private static final int WIDTH = 130;
	private static final int BTN = 20;       //버튼 크기
	private static final int STEP = 26;     //버튼 간격 (아래 글씨가 겹치지 않게)

	private static final int GREEN = 0x9FD86A;
	private static final int GOLD = 0xFFE0B040;

	//정령술 이름표 (행동 버튼에 등록할 수 있는 것들)
	public static final String SWITCH = "switch", SHOOT = "shoot", IMBUE = "imbue", RECALL = "recall",
			CONTRACT = "contract", UNMATERIALIZE = "unmaterialize", BESTOW = "bestow", RELEASE = "release",
			MANAGE = "manage";

	private final ForestWand wand;
	private final boolean info;

	public WndSpiritArts( ForestWand wand ){
		this( wand, false );
	}

	public WndSpiritArts( final ForestWand wand, final boolean info ){
		super();
		this.wand = wand;
		this.info = info;

		IconTitle title = new IconTitle( info ? Icons.INFO.get() : new ItemSprite( wand ),
				Messages.titleCase( Messages.get( this, info ? "info_title" : "title" ) ) );
		title.setRect( 0, 0, WIDTH, 0 );
		title.color( GREEN );
		add( title );
		chrome.hardlight( 0xC8E8B0 );

		IconButton btnInfo = new IconButton( info ? new ItemSprite( wand ) : Icons.INFO.get() ){
			@Override
			protected void onClick() {
				hide();
				GameScene.show( new WndSpiritArts( wand, !info ) );
			}
		};
		btnInfo.setRect( WIDTH - 16, 0, 16, 16 );
		add( btnInfo );

		String text;
		if (info)                           text = Messages.get( this, "info_desc" );
		else if (DeviceCompat.isDesktop())  text = Messages.get( this, "use_desc_desktop" );
		else                                text = Messages.get( this, "use_desc_mobile" );
		RenderedTextBlock msg = PixelScene.renderTextBlock( text, 6 );
		msg.maxWidth( WIDTH );
		msg.setPos( 0, title.bottom() + 4 );
		add( msg );

		float top = msg.bottom() + 6;

		// ── 줄 1: 깃든 정령 3칸 (왼쪽·가운데(발현)·오른쪽). 누르면 그 정령을 바로 발현, 빈칸은 깃들이기 ──
		top = header( Messages.get( this, "section_spirits" ), top );
		int[] order = { 0, ForestWand.CENTER, 2 };
		final int SLOT_STEP = 42;   //정령 이름이 길어 칸 사이를 넓게
		float x = (WIDTH - (order.length - 1) * SLOT_STEP - BTN) / 2f;
		for (int i : order){
			SlotButton b = new SlotButton( i );
			b.setRect( x, top, BTN, BTN );
			add( b );
			x += SLOT_STEP;
		}
		top += BTN + 12;

		// ── 줄 2: 정령술 ──
		ArrayList<String> arts = new ArrayList<>();
		arts.add( SWITCH ); arts.add( SHOOT ); arts.add( IMBUE ); arts.add( BESTOW ); arts.add( RELEASE );
		top = row( Messages.get( this, "section_arts" ), arts, top );

		// ── 줄 3: 장비·계약·관리 ──
		ArrayList<String> more = new ArrayList<>();
		more.add( RECALL );
		if (contractor()) more.add( CONTRACT );
		if (contractor() || !ForestWand.materializedSpirits().isEmpty()) more.add( UNMATERIALIZE );
		more.add( MANAGE );
		top = row( Messages.get( this, "section_more" ), more, top );

		resize( WIDTH, (int) top );

		//모바일에서는 창을 도구 모음 바로 위로 내림 (핏빛 성배 기술 창과 같은 자리)
		if (SPDSettings.interfaceSize() != 2){
			offset( 0, (int) (GameScene.uiCamera.height / 2 - 30 - height / 2) );
		}
	}

	//줄 제목: ── 제목 ── (양옆 풀빛 선)
	private float header( String text, float top ){
		RenderedTextBlock head = PixelScene.renderTextBlock( text, 6 );
		head.hardlight( 0xB8E890 );
		head.setPos( (WIDTH - head.width()) / 2f, top );
		PixelScene.align( head );
		add( head );
		float lineY = top + head.height() / 2f;
		float side = (WIDTH - head.width()) / 2f - 4;
		if (side > 2){
			block( 0, lineY, side, 1, 0xFF3E5A2C );
			block( WIDTH - side, lineY, side, 1, 0xFF3E5A2C );
		}
		return top + head.height() + 4;
	}

	private float row( String head, ArrayList<String> keys, float top ){
		top = header( head, top );
		float x = (WIDTH - (keys.size() - 1) * STEP - BTN) / 2f;
		for (String k : keys){
			ArtButton b = new ArtButton( k );
			b.setRect( x, top, BTN, BTN );
			add( b );
			x += STEP;
		}
		return top + BTN + 13;
	}

	private ColorBlock block( float x, float y, float w, float h, int color ){
		ColorBlock b = new ColorBlock( w, h, color );
		b.x = x; b.y = y;
		add( b );
		return b;
	}

	private static boolean contractor(){
		return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.SPIRIT_CONTRACTOR;
	}

	// ───────────────────────── 정령술: 이름·그림·쓸 수 있는지·쓰기 (행동 버튼도 같은 것을 부름) ─────────────────────────

	public static String artName( String key ){
		return Messages.get( WndSpiritArts.class, "art_" + key );
	}

	public static String artDesc( String key ){
		return Messages.get( WndSpiritArts.class, "desc_" + key );
	}

	public static Image spiritIcon( ForestWand.Spirit s ){
		Image img = new Image( Assets.Sprites.ITEM_ICONS );
		img.frame( ItemSpriteSheet.Icons.film.get( s.icon() ) );
		img.scale.set( 2f );
		return img;
	}

	public static Image artIcon( ForestWand wand, String key ){
		switch (key){
			case SWITCH: {
				ForestWand.Spirit n = wand.nextSpirit();
				return n != null ? spiritIcon( n ) : Icons.SHUFFLE.get();
			}
			case SHOOT:
				return new ItemSprite( wand.knockBolt() );
			case IMBUE:
				return new ItemSprite( ItemSpriteSheet.FOREST_WAND );
			case BESTOW:
				return new ItemSprite( ItemSpriteSheet.SWORD );
			case RELEASE:
				return Icons.CLOSE.get();
			case RECALL:
				return new ItemSprite( ItemSpriteSheet.ARMOR_SCALE );
			case CONTRACT:
				return Icons.TALENT.get();
			case UNMATERIALIZE: {
				ArrayList<com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GreaterSpirit> m = ForestWand.materializedSpirits();
				return m.isEmpty() ? Icons.ARROW.get() : spiritIcon( m.get( 0 ).spirit() );
			}
			default:
				return Icons.PREFS.get();
		}
	}

	public static boolean canUse( ForestWand wand, String key ){
		switch (key){
			case SWITCH:        return wand.nextSpirit() != null;
			case IMBUE:         return wand.freeSlot() >= 0 && !(Dungeon.level instanceof ModernLevel);
			case BESTOW:
			case RELEASE:       return wand.active() != null;
			case RECALL:        return wand.freeSlot() >= 0 && WndForestWand.hasBestowed();
			case CONTRACT:      return contractor();
			case UNMATERIALIZE: return wand.freeSlot() >= 0 && !ForestWand.materializedSpirits().isEmpty();
			default:            return true;
		}
	}

	//정령술 쓰기. 턴을 쓰는 일은 영웅이 행동할 수 있을 때만
	public static void use( ForestWand wand, String key ){
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.ready) return;
		if (!canUse( wand, key )){
			GLog.w( Messages.get( WndSpiritArts.class, "cant_" + key ) );
			return;
		}
		switch (key){
			case SWITCH:   SpiritSwitch.switchNext(); break;
			case SHOOT:    wand.execute( hero, ForestWand.AC_SHOOT ); break;
			case IMBUE:    wand.execute( hero, ForestWand.AC_IMBUE ); break;
			case CONTRACT: wand.execute( hero, ForestWand.AC_CONTRACT ); break;
			case MANAGE:   GameScene.show( new WndForestWand( wand ) ); break;
			default:       WndForestWand.runArt( wand, key ); break;
		}
	}

	public static void setQuick( ForestWand wand, String key ){
		wand.quickArt = key;
		GLog.i( Messages.get( WndSpiritArts.class, "quick_set", artName( key ) ) );
		if (Dungeon.hero != null){
			SpiritSwitch sw = Dungeon.hero.buff( SpiritSwitch.class );
			if (sw != null) sw.refreshButton( true );
		}
	}

	// ───────────────────────── 버튼 ─────────────────────────

	//초록 테두리의 네모 칸(행동 버튼에 등록된 것은 금빛 테두리) + 아이콘 + 아래 이름
	private abstract class Cell extends IconButton {

		protected ColorBlock frame, inner, shine;
		protected RenderedTextBlock label;

		Cell( Image icon, String name, boolean gold, boolean usable, int bg ){
			super( icon );
			frame = new ColorBlock( 1, 1, gold ? GOLD : 0xFF4E7038 );
			inner = new ColorBlock( 1, 1, 0xFF000000 | bg );
			shine = new ColorBlock( 1, 1, gold ? 0xFF8A6A30 : 0xFF2E4424 );
			addToBack( shine );
			addToBack( inner );
			addToBack( frame );
			label = PixelScene.renderTextBlock( name, 5 );
			label.hardlight( gold ? 0xFFE080 : (usable ? 0xDCEFD0 : 0x7A8070) );
			add( label );
			icon.alpha( usable ? 1f : 0.35f );
		}

		@Override
		protected void layout() {
			super.layout();
			if (frame != null){
				frame.size( width + 2, height + 2 ); frame.x = x - 1; frame.y = y - 1;
				inner.size( width, height );         inner.x = x;     inner.y = y;
				shine.size( width, 1 );              shine.x = x;     shine.y = y;
			}
			if (label != null){
				label.setPos( x + (width - label.width()) / 2f, y + height + 2 );
				PixelScene.align( label );
			}
		}
	}

	private class ArtButton extends Cell {

		final String key;

		ArtButton( String key ){
			super( artIcon( wand, key ), artName( key ), key.equals( wand.quickArt ), canUse( wand, key ), 0x14200E );
			this.key = key;
		}

		@Override
		protected void onClick() {
			if (info){
				GameScene.show( new WndTitledMessage( artIcon( wand, key ), Messages.titleCase( artName( key ) ), artDesc( key ) ) );
				return;
			}
			hide();
			use( wand, key );
		}

		@Override
		protected boolean onLongClick() {
			hide();
			setQuick( wand, key );
			return true;
		}

		@Override
		protected String hoverText() {
			return "_" + Messages.titleCase( artName( key ) ) + "_\n" + artDesc( key );
		}
	}

	private class SlotButton extends Cell {

		final int slot;

		SlotButton( int slot ){
			super( wand.slots[slot] != null ? spiritIcon( wand.slots[slot] ) : Icons.PLUS.get(),
					wand.slots[slot] != null ? wand.slots[slot].title()
							: Messages.get( WndSpiritArts.class, "empty" ),
					slot == ForestWand.CENTER && wand.slots[slot] != null, true,
					wand.slots[slot] != null ? darkOf( wand.slots[slot].color ) : 0x101A0C );
			this.slot = slot;
		}

		@Override
		protected void onClick() {
			ForestWand.Spirit s = wand.slots[slot];
			if (info){
				if (s != null) GameScene.show( new WndTitledMessage( spiritIcon( s ), s.title(),
						Messages.get( ForestWand.class, "shot_" + s.name().toLowerCase() ) ) );
				return;
			}
			Hero hero = Dungeon.hero;
			if (hero == null || !hero.ready) return;
			if (s == null){
				//빈칸: 이 칸에 깃들이기
				hide();
				if (!canUse( wand, IMBUE )){ GLog.w( Messages.get( WndSpiritArts.class, "cant_imbue" ) ); return; }
				wand.preferredSlot = slot;
				wand.execute( hero, ForestWand.AC_IMBUE );
				return;
			}
			if (slot != ForestWand.CENTER){
				//턴을 쓰지 않고 이 정령을 바로 발현
				SpiritSwitch.bringToCenter( slot );
			}
			hide();
			GameScene.show( new WndSpiritArts( wand, false ) );
		}

		@Override
		protected boolean onLongClick() {
			//길게 누르면 그 칸을 고른 채 정령 관리 창 (부여·풀어 주기·실체화)
			hide();
			GameScene.show( new WndForestWand( wand, slot ) );
			return true;
		}
	}

	private static int darkOf( int c ){
		int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
		return ((r * 30 / 100 + 8) << 16) | ((g * 30 / 100 + 8) << 8) | (b * 30 / 100 + 10);
	}
}
