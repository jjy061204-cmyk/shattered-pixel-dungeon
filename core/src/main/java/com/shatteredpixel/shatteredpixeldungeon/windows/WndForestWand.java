/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령술: 정령사가 숲의 완드로 하는 모든 일을 한 창에 (정령 버튼을 길게 누르거나 완드의 '정령술')
 *
 *  ┌ 정령술 ─────────────────── [?] ┐
 *  │   [왼쪽]  [  가운데(발현)  ]  [오른쪽]   │  ← 칸을 눌러 고름. 고른 빈칸을 한 번 더 누르면 정령 소환
 *  │          고른 정령 이름 · 한 줄 능력        │
 *  │  [부여] [발현] (실체화) [풀어 주기]         │  ← 고른 정령에게 하는 일
 *  │ ─────────────── 숲의 완드 ─────────────── │
 *  │  [정령 소환] [되돌리기] [바꾸기]            │  ← 완드 전체의 일
 *  │  (계약 소환) (거두기)                       │  ← 정령 계약자·실체화한 정령이 있을 때만
 *  │  누르면 사용 · 길게 누르면 정령 버튼에 등록  │
 *  └───────────────────────────────────┘
 *  - 정령 버튼에 등록된 일은 금빛 테두리. 부여·풀어 주기를 등록하면 발현 중인(가운데) 정령에게 씀
 *  - 정령 부여사: 한 장비에 정령을 둘까지 / 정령 계약자: 상급 정령 실체화·거두기
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GreaterSpirit;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.SpiritGlyph;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.SpiritEnchant;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SpiritSwitch;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class WndForestWand extends Window {

	private static final int WIDTH = 128;
	private static final int GAP = 2;
	private static final int BTN_H = 17;
	private static final int CARD_C = 38;     //가운데(발현) 칸
	private static final int CARD_S = 28;     //양옆(저장) 칸
	private static final int CARD_GAP = 8;

	private static final int ACTIVE_GOLD = 0xFFE9A0;   //발현 칸
	private static final int GREEN = 0x9FD86A;
	private static final int QUICK_GOLD = 0xFFD860;    //정령 버튼에 등록된 일

	//버튼 빛깔표: 테두리 · 바탕 · 위 빛줄 · 아래 그림자 · 글씨
	private static final int UI_BASE = 0x1A1815;
	private static final int[] PAL_NEUTRAL = palette( 0x948E80, false );
	private static final int[] PAL_BESTOW  = withText( PAL_NEUTRAL, 0xFFE2A0 );   //부여(완드 → 장비): 금빛 글씨
	private static final int[] PAL_IMBUE   = withText( PAL_NEUTRAL, 0xCCF2B4 );   //정령 소환(촉매 → 완드): 풀빛 글씨
	private static final int[] PAL_RELEASE = withText( PAL_NEUTRAL, 0xFFAFA2 );   //풀어 주기: 붉은 글씨
	private static final int[] PAL_QUICK   = { 0xD8A838, 0x2A2416, 0x3A3220, 0x16120A, 0xFFE89A };
	private static final int[] PAL_OFF     = { 0x4C4942, 0x262420, 0x2E2C28, 0x1E1C19, 0x7E7A72 };
	private static final int EMPTY_GREEN   = 0x8FD86A;   //고른 빈칸
	private static final int TEXT_DIM    = 0xB4B4B4;

	private final ForestWand wand;
	private final int sel;     //고른 칸
	private boolean quickRun = false;
	private boolean hidden = false;

	@Override
	public void hide() {
		if (hidden) return;
		hidden = true;
		super.hide();
	}

	//모드: 정령 버튼에서 창을 열지 않고 바로 씀 (되돌리기·실체 거두기·부여·풀어 주기)
	// 고른 칸은 발현 중인 가운데 정령
	public static void runArt( ForestWand wand, String key ){
		WndForestWand w = new WndForestWand( wand, ForestWand.CENTER );
		w.quickRun = true;
		w.act( key );
		w.hide();
	}

	//발현 칸 테두리가 천천히 숨 쉬듯 빛남
	private ColorBlock activeGlow;
	private float glowTime;

	public WndForestWand( ForestWand wand ){
		this( wand, defaultSel( wand ) );
	}

	//처음엔 발현 중인(가운데) 정령, 없으면 첫 정령, 정령이 없으면 가운데 빈칸
	private static int defaultSel( ForestWand wand ){
		if (wand.slots[ForestWand.CENTER] != null) return ForestWand.CENTER;
		for (int i = 0; i < ForestWand.SLOTS; i++) if (wand.slots[i] != null) return i;
		return ForestWand.CENTER;
	}

	public WndForestWand( ForestWand wand, int selected ){
		super();
		this.wand = wand;
		this.sel = selected;
		wand.preferredSlot = -1;
		chrome.hardlight( 0xC8E8B0 );

		// ── 머리줄: 완드 그림 + 제목, 오른쪽 끝에 도움말 ──
		ItemSprite wandIcon = new ItemSprite( wand );
		wandIcon.x = 0;
		wandIcon.y = 0;
		add( wandIcon );
		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.titleCase( Messages.get( this, "title" ) ), 9 );
		title.hardlight( GREEN );
		title.setPos( 19, (16 - title.height()) / 2f );
		PixelScene.align( title );
		add( title );
		UiButton help = new UiButton( "?", 8, PAL_NEUTRAL ){
			@Override
			protected void onClick() {
				showHelp();
			}
		};
		help.setRect( WIDTH - 14, 1, 14, 14 );
		add( help );
		float y = 16 + 6;

		// ── 정령 3칸: 가운데(발현)는 크게, 양옆(저장)은 작게 ──
		float total = CARD_S*2 + CARD_C + CARD_GAP*2;
		float x0 = (WIDTH - total) / 2f;
		float[] cardX = { x0, x0 + CARD_S + CARD_GAP, x0 + CARD_S + CARD_GAP*2 + CARD_C };
		for (int i = 0; i < ForestWand.SLOTS; i++){
			boolean center = i == ForestWand.CENTER;
			int size = center ? CARD_C : CARD_S;
			SlotCard card = new SlotCard( i, wand.slots[i], center, i == sel );
			card.setRect( cardX[i], y + (CARD_C - size) / 2f, size, size );
			add( card );
			if (center) activeGlow = card.glow;
		}
		y += CARD_C + 2;
		RenderedTextBlock tag = PixelScene.renderTextBlock( Messages.get( this, "active_tag" ), 6 );
		tag.hardlight( wand.slots[ForestWand.CENTER] != null ? ACTIVE_GOLD : 0x777777 );
		tag.setPos( cardX[ForestWand.CENTER] + (CARD_C - tag.width()) / 2f, y );
		add( tag );
		y = tag.bottom() + 6;

		// ── 고른 칸: 이름과 한 줄 능력 (누르면 자세한 설명) ──
		final ForestWand.Spirit s = wand.slots[sel];
		float infoTop = y;
		RenderedTextBlock name = PixelScene.renderTextBlock(
				s != null ? s.title() : Messages.get( this, "empty_slot" ), 8 );
		name.hardlight( s != null ? s.color : 0xB8E8A0 );
		name.setPos( (WIDTH - name.width()) / 2f, y );
		add( name );
		RenderedTextBlock brief = PixelScene.renderTextBlock(
				s != null ? Messages.get( this, "brief_" + s.name().toLowerCase() ) : Messages.get( this, "empty_hint" ), 6 );
		brief.hardlight( TEXT_DIM );
		brief.maxWidth( WIDTH );
		brief.align( RenderedTextBlock.CENTER_ALIGN );
		brief.setPos( (WIDTH - brief.width()) / 2f, name.bottom() + 3 );
		add( brief );
		y = brief.bottom() + 6;
		Button info = new Button(){
			@Override
			protected void onClick() {
				showHelp();
			}
		};
		info.setRect( 0, infoTop - 2, WIDTH, y - infoTop - 3 );
		add( info );

		// ── 고른 정령에게 하는 일 (빈칸이면 줄 없이 위의 안내만) ──
		if (s != null){
			ArrayList<String> keys = new ArrayList<>();
			keys.add( "bestow" );
			keys.add( "to_center" );
			if (s.greater && contractor()) keys.add( "materialize" );
			keys.add( "release" );
			y = buttonRow( keys, y, true );
		}

		// ── 숲의 완드 전체의 일 ──
		y = header( Messages.get( this, "section_wand" ), y + 3 );
		ArrayList<String> wandKeys = new ArrayList<>();
		wandKeys.add( WndSpiritArts.IMBUE );
		wandKeys.add( WndSpiritArts.RECALL );
		wandKeys.add( WndSpiritArts.SWITCH );
		y = buttonRow( wandKeys, y, false );
		ArrayList<String> more = new ArrayList<>();
		if (contractor()) more.add( WndSpiritArts.CONTRACT );
		if (!ForestWand.materializedSpirits().isEmpty()) more.add( WndSpiritArts.UNMATERIALIZE );
		if (!more.isEmpty()) y = buttonRow( more, y + GAP, false );

		// ── 맨 아래 안내: 누르면 사용 · 길게 누르면 정령 버튼에 등록 (지금 등록된 일) ──
		String q = wand.quickArt == null ? WndSpiritArts.SWITCH : wand.quickArt;
		RenderedTextBlock foot = PixelScene.renderTextBlock( Messages.get( this,
				DeviceCompat.isDesktop() ? "foot_desktop" : "foot_mobile", WndSpiritArts.artName( q ) ), 5 );
		foot.hardlight( 0x9A9A90 );
		foot.maxWidth( WIDTH );
		foot.align( RenderedTextBlock.CENTER_ALIGN );
		foot.setPos( (WIDTH - foot.width()) / 2f, y + 5 );
		add( foot );
		y = foot.bottom();

		resize( WIDTH, (int) Math.ceil( y ) );

		//모바일에서는 창을 도구 모음 바로 위로 내림
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
			ColorBlock l = new ColorBlock( side, 1, 0xFF3E5A2C ); l.x = 0; l.y = lineY; add( l );
			ColorBlock r = new ColorBlock( side, 1, 0xFF3E5A2C ); r.x = WIDTH - side; r.y = lineY; add( r );
		}
		return top + head.height() + 4;
	}

	//한 줄에 같은 너비의 버튼을 나란히. spiritRow: 고른 정령에게 하는 일
	private float buttonRow( ArrayList<String> keys, float y, boolean spiritRow ){
		int n = keys.size();
		float bw = (WIDTH - GAP*(n - 1)) / (float) n;
		final ForestWand.Spirit s = wand.slots[sel];
		for (int k = 0; k < n; k++){
			final String key = keys.get( k );
			final String quickKey = quickKeyOf( key );
			boolean isQuick = quickKey != null && quickKey.equals( wand.quickArt == null ? WndSpiritArts.SWITCH : wand.quickArt );
			int[] pal;
			if (isQuick)                    pal = PAL_QUICK;
			else if (key.equals( "bestow" ))  pal = PAL_BESTOW;
			else if (key.equals( WndSpiritArts.IMBUE ) || key.equals( WndSpiritArts.UNMATERIALIZE )) pal = PAL_IMBUE;
			else if (key.equals( "release" )) pal = PAL_RELEASE;
			else                            pal = PAL_NEUTRAL;
			String label = spiritRow ? Messages.get( this, "act_" + key ) : WndSpiritArts.artName( key );
			UiButton b = new UiButton( label, 6, pal ){
				@Override
				protected void onClick() {
					act( key );
				}
				@Override
				protected boolean onLongClick() {
					return register( quickKey );
				}
				@Override
				protected void onRightClick() {
					register( quickKey );
				}
			};
			if (key.equals( "bestow" ) && s != null){
				Image si = new Image( Assets.Sprites.ITEM_ICONS );
				si.frame( ItemSpriteSheet.Icons.film.get( s.icon() ) );
				b.icon( si );
			}
			if (key.equals( WndSpiritArts.UNMATERIALIZE )){
				ArrayList<GreaterSpirit> m = ForestWand.materializedSpirits();
				if (!m.isEmpty()){
					Image si = new Image( Assets.Sprites.ITEM_ICONS );
					si.frame( ItemSpriteSheet.Icons.film.get( m.get( 0 ).spirit().icon() ) );
					b.icon( si );
				}
			}
			//쓸 수 없는 일은 자리는 그대로 두고 꺼 둠
			if (key.equals( "to_center" ) && sel == ForestWand.CENTER) b.enable( false );
			if (!spiritRow && !WndSpiritArts.canUse( wand, key )) b.enable( false );
			b.setRect( k * (bw + GAP), y, bw, BTN_H );
			add( b );
		}
		return y + BTN_H;
	}

	//정령 버튼에 등록할 때의 이름 (발현·실체화는 등록하지 않음)
	private static String quickKeyOf( String key ){
		switch (key){
			case "bestow":  return WndSpiritArts.BESTOW;
			case "release": return WndSpiritArts.RELEASE;
			case "to_center": case "materialize": return null;
			default: return key;
		}
	}

	private boolean register( String quickKey ){
		if (quickKey == null) return false;
		WndSpiritArts.setQuick( wand, quickKey );
		hide();
		if (!quickRun) GameScene.show( new WndForestWand( wand, sel ) );
		return true;
	}

	@Override
	public void update() {
		super.update();
		if (activeGlow != null && wand.slots[ForestWand.CENTER] != null){
			glowTime += Game.elapsed;
			activeGlow.alpha( 0.25f + 0.2f * (float) Math.sin( glowTime * 3f ) );
		}
	}

	//정령 한 칸: 테두리 + 정령 빛깔 바탕 + 아이콘 (빈칸은 '+')
	private class SlotCard extends Button {

		private final int slot;
		private final ForestWand.Spirit sp;
		private final boolean center, selected;
		final ColorBlock glow;
		private final ColorBlock frame, bg;
		private Image icon;
		private RenderedTextBlock plus;
		private final int bgColor;

		SlotCard( int slot, ForestWand.Spirit sp, boolean center, boolean selected ){
			super();
			this.slot = slot;
			this.sp = sp;
			this.center = center;
			this.selected = selected;

			int glowColor = selected ? (sp != null ? sp.color : EMPTY_GREEN) : (center && sp != null ? ACTIVE_GOLD : 0x000000);
			glow = new ColorBlock( 1, 1, 0xFF000000 | glowColor );
			glow.alpha( selected ? 0.45f : (center && sp != null ? 0.3f : 0f) );
			add( glow );

			int frameColor;
			if (sp == null)        frameColor = selected ? 0xFF000000 | EMPTY_GREEN : 0xFF3E5A30;
			else if (selected)     frameColor = 0xFF000000 | brighten( sp.color );
			else if (center)       frameColor = 0xFFB8A060;
			else                   frameColor = 0xFF55524A;
			frame = new ColorBlock( 1, 1, frameColor );
			add( frame );

			bgColor = sp == null ? (selected ? 0xFF1E2C18 : 0xFF141A12) : (0xFF000000 | dim( sp.color, selected ));
			bg = new ColorBlock( 1, 1, bgColor );
			add( bg );

			if (sp != null){
				icon = new Image( Assets.Sprites.ITEM_ICONS );
				icon.frame( ItemSpriteSheet.Icons.film.get( sp.icon() ) );
				icon.scale.set( center ? 3f : 2f );
				if (!selected && !center) icon.alpha( 0.85f );
				add( icon );
			} else {
				plus = PixelScene.renderTextBlock( "+", center ? 14 : 11 );
				plus.hardlight( 0x8FD86A );
				add( plus );
			}
		}

		@Override
		protected void layout() {
			super.layout();
			glow.size( width + 2, height + 2 );
			glow.x = x - 1; glow.y = y - 1;
			frame.size( width, height );
			frame.x = x; frame.y = y;
			int in = selected ? 2 : 1;
			bg.size( width - in*2, height - in*2 );
			bg.x = x + in; bg.y = y + in;
			if (icon != null){
				icon.x = x + (width - icon.width()) / 2f;
				icon.y = y + (height - icon.height()) / 2f;
				PixelScene.align( icon );
			}
			if (plus != null){
				plus.setPos( x + (width - plus.width()) / 2f, y + (height - plus.height()) / 2f - 1 );
				PixelScene.align( plus );
			}
		}

		@Override
		protected void onPointerDown() {
			super.onPointerDown();
			bg.color( brighten( bgColor & 0xFFFFFF ) );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			bg.color( bgColor & 0xFFFFFF );
		}

		@Override
		protected void onClick() {
			if (slot != sel)  reopen( slot );   //다른 칸을 누르면 그 칸을 고름
			else if (sp == null) imbue();       //고른 빈칸을 한 번 더 누르면 바로 정령 소환
			else showHelp();                    //고른 정령을 한 번 더 누르면 자세한 설명
		}
	}

	//납작한 버튼: 모서리를 깎은 1칸 테두리 + 위쪽 빛줄·아래쪽 그림자 + (있으면) 작은 아이콘
	private static class UiButton extends Button {

		private final ColorBlock top, bottom, left, right, fill, hi, lo;
		private final RenderedTextBlock label;
		private Image icon;
		private final int[] pal;
		private boolean enabled = true;

		UiButton( String text, int size, int[] pal ){
			super();
			this.pal = pal;
			fill = block();
			hi = block();
			lo = block();
			top = block();
			bottom = block();
			left = block();
			right = block();
			label = PixelScene.renderTextBlock( text, size );
			add( label );
			paint( pal );
		}

		private ColorBlock block(){
			ColorBlock b = new ColorBlock( 1, 1, 0xFFFFFFFF );
			add( b );
			return b;
		}

		private void paint( int[] p ){
			top.color( p[0] ); bottom.color( p[0] ); left.color( p[0] ); right.color( p[0] );
			fill.color( p[1] );
			hi.color( p[2] );
			lo.color( p[3] );
			label.hardlight( p[4] );
		}

		float labelWidth(){
			return label.width();
		}

		void icon( Image img ){
			icon = img;
			add( icon );
		}

		void enable( boolean value ){
			enabled = value;
			active = value;
			paint( value ? pal : PAL_OFF );
			if (icon != null) icon.alpha( value ? 1f : 0.35f );
		}

		@Override
		protected void onPointerDown() {
			fill.color( mix( pal[1], 0xFFFFFF, 0.12f ) );
			hi.color( mix( pal[2], 0xFFFFFF, 0.18f ) );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			if (enabled) paint( pal );
		}

		@Override
		protected void layout() {
			super.layout();
			top.size( width - 2, 1 );     top.x = x + 1;          top.y = y;
			bottom.size( width - 2, 1 );  bottom.x = x + 1;       bottom.y = y + height - 1;
			left.size( 1, height - 2 );   left.x = x;             left.y = y + 1;
			right.size( 1, height - 2 );  right.x = x + width - 1; right.y = y + 1;
			fill.size( width - 2, height - 2 ); fill.x = x + 1;   fill.y = y + 1;
			hi.size( width - 2, 1 );      hi.x = x + 1;           hi.y = y + 1;
			lo.size( width - 2, 1 );      lo.x = x + 1;           lo.y = y + height - 2;

			float cw = label.width() + (icon != null ? icon.width() + 2 : 0);
			float cx = x + (width - cw) / 2f;
			if (icon != null){
				icon.x = cx;
				icon.y = y + (height - icon.height()) / 2f;
				PixelScene.align( icon );
				cx += icon.width() + 2;
			}
			label.setPos( cx, y + (height - label.height()) / 2f );
			PixelScene.align( label );
		}
	}

	private static int mix( int a, int b, float t ){
		int ra = (a >> 16) & 0xFF, ga = (a >> 8) & 0xFF, ba = a & 0xFF;
		int rb = (b >> 16) & 0xFF, gb = (b >> 8) & 0xFF, bb = b & 0xFF;
		return ((int)(ra + (rb - ra)*t) << 16) | ((int)(ga + (gb - ga)*t) << 8) | (int)(ba + (bb - ba)*t);
	}

	private static int[] withText( int[] pal, int text ){
		int[] p = pal.clone();
		p[4] = text;
		return p;
	}

	private static int[] palette( int accent, boolean filled ){
		return new int[]{
				mix( accent, 0x000000, 0.12f ),
				mix( accent, UI_BASE, filled ? 0.56f : 0.86f ),
				mix( accent, UI_BASE, filled ? 0.42f : 0.74f ),
				mix( accent, 0x000000, filled ? 0.78f : 0.86f ),
				mix( accent, 0xFFFFFF, filled ? 0.42f : 0.62f ) };
	}

	//도움말: 고른 정령의 자세한 능력 + 각 버튼이 하는 일
	private void showHelp(){
		ForestWand.Spirit s = wand.slots[sel];
		String body = Messages.get( this, "help" );
		if (contractor()) body += "\n" + Messages.get( this, "help_contractor" );
		Image icon;
		String title;
		if (s != null){
			body = Messages.get( ForestWand.class, "shot_" + s.name().toLowerCase() ) + "\n\n" + body;
			icon = new Image( Assets.Sprites.ITEM_ICONS );
			icon.frame( ItemSpriteSheet.Icons.film.get( s.icon() ) );
			icon.scale.set( 2f );
			title = s.title();
		} else {
			icon = new ItemSprite( ItemSpriteSheet.FOREST_WAND );
			title = Messages.get( this, "title" );
		}
		GameScene.show( new WndTitledMessage( icon, title, body ) );
	}

	//장비 가운데 정령이 깃든 것이 있는지 (되돌리기 버튼)
	public static boolean hasBestowed(){
		if (Dungeon.hero == null) return false;
		for (Item i : Dungeon.hero.belongings){
			if (spiritsOn( i ) > 0) return true;
		}
		return false;
	}

	private static int dim( int c, boolean lit ){
		int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
		int k = lit ? 38 : 26;
		r = r * k / 100 + 10; g = g * k / 100 + 10; b = b * k / 100 + 14;
		return (r << 16) | (g << 8) | b;
	}

	private static int brighten( int c ){
		int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
		r = Math.min( 255, r + (255 - r) / 3 + 12 );
		g = Math.min( 255, g + (255 - g) / 3 + 12 );
		b = Math.min( 255, b + (255 - b) / 3 + 12 );
		return (r << 16) | (g << 8) | b;
	}

	private void act( String key ){
		switch (key){
			case "imbue":
				imbue();
				break;
			case "recall":
				recall();
				break;
			case "switch":
				//다음 정령을 가운데로 (턴을 쓰지 않음)
				if (wand.nextSpirit() == null) return;
				SpiritSwitch.switchNext();
				reopen( ForestWand.CENTER );
				break;
			case "contract":
				hide();
				if (Dungeon.hero != null && Dungeon.hero.ready) wand.execute( Dungeon.hero, ForestWand.AC_CONTRACT );
				break;
			case "unmaterialize":
				if (Dungeon.hero == null || !Dungeon.hero.ready) return;
				hide();
				//고른 칸이 비어 있으면 그 칸으로, 아니면 아무 빈칸으로
				wand.preferredSlot = wand.slots[sel] == null ? sel : -1;
				wand.recallMaterialized( Dungeon.hero );
				wand.preferredSlot = -1;
				refreshSwitch();
				break;
			default:
				doSlotAction( sel, key );
				break;
		}
	}

	//깃들이기: 창을 닫고 촉매를 고르게 함
	private void imbue(){
		if (wand.freeSlot() < 0){
			GLog.w( Messages.get( ForestWand.class, "full" ) );
			return;
		}
		hide();
		if (Dungeon.hero != null && Dungeon.hero.ready){
			//고른 빈칸에 담기
			wand.preferredSlot = wand.slots[sel] == null ? sel : -1;
			wand.execute( Dungeon.hero, ForestWand.AC_IMBUE );
		}
	}

	private void reopen(){
		reopen( sel );
	}

	private void reopen( int slot ){
		hide();
		if (quickRun) return;    //정령술 창·행동 버튼에서 바로 쓴 경우엔 관리 창을 다시 열지 않음
		if (wand.slots[slot] == null) slot = defaultSel( wand );
		GameScene.show( new WndForestWand( wand, slot ) );
	}

	private static boolean enchanter(){
		return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.SPIRIT_ENCHANTER;
	}

	//정령의 직감 +2: 저주받은 장비에도 정령을 부여하며 저주를 씻어 냄
	private static boolean cleanses(){
		return Dungeon.hero != null && Dungeon.hero.pointsInTalent( Talent.SPIRIT_INTUITION ) >= 2;
	}

	private static boolean contractor(){
		return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.SPIRIT_CONTRACTOR;
	}

	private void doSlotAction( final int slot, String k ){
		final ForestWand.Spirit s = wand.slots[slot];
		if (s == null) return;
		if (k.equals( "to_center" )){
			wand.moveToCenter( slot );
			Item.updateQuickslot();
			refreshSwitch();
			GLog.i( Messages.get( WndForestWand.class, "centered", s.title() ) );
			reopen( ForestWand.CENTER );
		} else if (k.equals( "materialize" )){
			hide();
			wand.materialize( Dungeon.hero, slot );
			refreshSwitch();
		} else if (k.equals( "bestow" )){
			hide();
			bestow( slot );
		} else {
			wand.slots[slot] = null;
			Item.updateQuickslot();
			refreshSwitch();
			Hero hero = Dungeon.hero;
			hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
			GLog.i( Messages.get( WndForestWand.class, "released", s.title() ) );
			if (hero.hasTalent( Talent.SPIRIT_RELEASE )){
				hide();
				releaseBurst( hero, s );
			} else {
				reopen( defaultSel( wand ) );
			}
		}
	}

	//정령 바꾸기 버튼을 새로 그림
	private static void refreshSwitch(){
		if (Dungeon.hero == null) return;
		SpiritSwitch sw = Dungeon.hero.buff( SpiritSwitch.class );
		if (sw != null) sw.refreshButton();
	}

	//정령 해방: 풀려난 정령이 터지며 둘레 1/2칸의 적에게 50/100% 힘으로 능력을 발휘
	private static void releaseBurst( Hero hero, ForestWand.Spirit s ){
		int pts = hero.pointsInTalent( Talent.SPIRIT_RELEASE );
		for (int n : PathFinder.NEIGHBOURS9) CellEmitter.get( hero.pos + n ).burst( s.particle(), 6 );
		Sample.INSTANCE.play( Assets.Sounds.BLAST, 0.7f );
		for (Char ch : Actor.chars().toArray( new Char[0] )){
			if (ch.alignment == Char.Alignment.ENEMY && ch.isAlive()
					&& Dungeon.level.distance( hero.pos, ch.pos ) <= pts && Dungeon.level.heroFOV[ch.pos]){
				ForestWand.spiritHit( s, hero, ch, 0, 0.5f * pts );
			}
		}
		hero.sprite.operate( hero.pos );
		hero.spendAndNext( 1f );
	}

	//정령 부여: 가운데든 양옆이든, 고른 칸의 정령을 무기·갑옷으로 옮김
	private void bestow( final int slot ){
		GameScene.selectItem( new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get( WndForestWand.class, "bestow_prompt" );
			}
			@Override
			public boolean itemSelectable( Item item ) {
				return item instanceof MeleeWeapon || item instanceof Armor;
			}
			@Override
			public void onSelect( Item item ) {
				final ForestWand.Spirit s = wand.slots[slot];
				if (item == null || s == null) return;

				if (item instanceof Weapon){
					final Weapon w = (Weapon) item;
					if (w.hasCurseEnchant()){
						if (!cleanses()){ GLog.w( Messages.get( WndForestWand.class, "cursed" ) ); return; }
						w.enchant( null ); w.cursed = false;
						GLog.p( Messages.get( WndForestWand.class, "cleansed" ) );
					}
					//정령 부여사: 이미 정령이 깃든 무기에 하나 더
					if (enchanter() && w.enchantment instanceof SpiritEnchant){
						final SpiritEnchant e = (SpiritEnchant) w.enchantment;
						if (e.spirit2 == null){
							wand.slots[slot] = null;
							e.spirit2 = s;
							finish( item, s );
						} else {
							chooseReplace( item, slot, s, e.spirit, e.spirit2, new Replacer(){
								@Override public ForestWand.Spirit replace( int which ){
									ForestWand.Spirit old;
									if (which == 0){ old = e.spirit; e.spirit = s; } else { old = e.spirit2; e.spirit2 = s; }
									return old;
								}
							} );
						}
						return;
					}
					if (w.enchantment != null && !(w.enchantment instanceof SpiritEnchant)) GLog.w( Messages.get( WndForestWand.class, "replaced", w.enchantment.name() ) );
					//완드에서 먼저 꺼내야, 장비에 있던 정령이 비워진 칸으로 역소환될 수 있음
					wand.slots[slot] = null;
					w.enchant( new SpiritEnchant( s ) );
				} else {
					final Armor a = (Armor) item;
					if (a.hasCurseGlyph()){
						if (!cleanses()){ GLog.w( Messages.get( WndForestWand.class, "cursed" ) ); return; }
						a.inscribe( null ); a.cursed = false;
						GLog.p( Messages.get( WndForestWand.class, "cleansed" ) );
					}
					if (enchanter() && a.glyph instanceof SpiritGlyph){
						final SpiritGlyph g = (SpiritGlyph) a.glyph;
						if (g.spirit2 == null){
							wand.slots[slot] = null;
							g.spirit2 = s;
							finish( item, s );
						} else {
							chooseReplace( item, slot, s, g.spirit, g.spirit2, new Replacer(){
								@Override public ForestWand.Spirit replace( int which ){
									ForestWand.Spirit old;
									if (which == 0){ old = g.spirit; g.spirit = s; } else { old = g.spirit2; g.spirit2 = s; }
									return old;
								}
							} );
						}
						return;
					}
					if (a.glyph != null && !(a.glyph instanceof SpiritGlyph)) GLog.w( Messages.get( WndForestWand.class, "replaced", a.glyph.name() ) );
					wand.slots[slot] = null;
					a.inscribe( new SpiritGlyph( s ) );
				}
				finish( item, s );
			}
		} );
	}

	private interface Replacer {
		ForestWand.Spirit replace( int which );
	}

	//정령이 둘 다 깃든 장비: 어느 정령과 바꿀지 고름 (밀려난 정령은 완드로 역소환)
	private void chooseReplace( final Item item, final int slot, final ForestWand.Spirit s,
	                            ForestWand.Spirit a, ForestWand.Spirit b, final Replacer r ){
		GameScene.show( new WndOptions( Messages.get( WndForestWand.class, "swap_title" ),
				Messages.get( WndForestWand.class, "swap_body", s.title() ), a.title(), b.title() ){
			@Override
			protected void onSelect( int index ) {
				if (wand.slots[slot] != s) return;
				wand.slots[slot] = null;
				ForestWand.Spirit old = r.replace( index );
				ForestWand.returnToWand( old );
				finish( item, s );
			}
		} );
	}

	private void finish( Item item, ForestWand.Spirit s ){
		Hero hero = Dungeon.hero;
		//정령의 직감: 정령을 부여한 장비는 곧바로 감정됨
		if (hero.hasTalent( Talent.SPIRIT_INTUITION )) item.identify();
		Item.updateQuickslot();
		Enchanting.show( hero, item );
		ForestWand.imbueFx( hero.pos, s );
		Sample.INSTANCE.play( Assets.Sounds.MELD );
		boolean pair = spiritsOn( item ) >= 2;
		GLog.p( Messages.get( WndForestWand.class, pair ? "bestowed_pair" : "bestowed", s.title(), item.trueName() ) );
		hero.sprite.operate( hero.pos );
		hero.spendAndNext( 1f );
	}

	private static int spiritsOn( Item item ){
		if (item instanceof Weapon && ((Weapon) item).enchantment instanceof SpiritEnchant){
			return ((SpiritEnchant) ((Weapon) item).enchantment).spirit2 != null ? 2 : 1;
		}
		if (item instanceof Armor && ((Armor) item).glyph instanceof SpiritGlyph){
			return ((SpiritGlyph) ((Armor) item).glyph).spirit2 != null ? 2 : 1;
		}
		return 0;
	}

	//장비에 부여한 정령을 다시 완드로 거둠
	private void recall(){
		if (wand.freeSlot() < 0){
			GLog.w( Messages.get( ForestWand.class, "full" ) );
			return;
		}
		final int slotForRecall = wand.slots[sel] == null ? sel : -1;
		hide();
		GameScene.selectItem( new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get( WndForestWand.class, "recall_prompt" );
			}
			@Override
			public boolean itemSelectable( Item item ) {
				return spiritsOn( item ) > 0;
			}
			@Override
			public void onSelect( Item item ) {
				if (item == null) return;
				if (ForestWand.SLOTS - wand.spiritCount() < spiritsOn( item )){
					GLog.w( Messages.get( ForestWand.class, "full" ) );
					return;
				}
				//장비에서 떼어 내면 정령은 알아서 숲의 완드로 역소환됨 (고른 빈칸부터)
				wand.preferredSlot = slotForRecall;
				if (item instanceof Weapon) ((Weapon) item).enchant( null );
				else                        ((Armor) item).inscribe( null );
				wand.preferredSlot = -1;
				Hero hero = Dungeon.hero;
				Sample.INSTANCE.play( Assets.Sounds.MELD );
				hero.sprite.operate( hero.pos );
				hero.spendAndNext( 1f );
			}
		} );
	}
}
