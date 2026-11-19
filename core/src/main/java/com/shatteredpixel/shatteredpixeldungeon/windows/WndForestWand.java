/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 숲의 완드에 깃든 정령 3칸 관리 (정령 버튼을 길게 누르거나 완드의 '정령 관리')
 *  - 빈칸·깃들이기: 촉매에서 정령을 불러 완드에 담음 / 되돌리기: 장비의 정령을 완드로
 *  - 고른 정령: 부여(장비로 옮김) · 발현(가운데로) · 풀어 주기
 *  - 정령 부여사: 한 장비에 정령을 둘까지 깃들임 (둘 다 차 있으면 어느 쪽과 바꿀지 고름)
 *  - 정령 계약자: 상급 정령을 실체화시키고, 실체화한 정령을 다시 완드로 거둠
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
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
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class WndForestWand extends Window {

	//한 화면 구성: [머리줄] → [정령 3칸] → [고른 정령 이름·한 줄 능력] → [고른 정령에게 할 일] → [완드 전체의 일]
	private static final int WIDTH = 120;
	private static final int GAP = 2;
	private static final int BTN_H = 18;
	private static final int CARD_C = 38;     //가운데(발현) 칸
	private static final int CARD_S = 28;     //양옆(저장) 칸
	private static final int CARD_GAP = 7;

	private static final int IMBUE_TINT  = 0x8FE068;   //깃들이기(촉매 → 완드): 풀빛
	private static final int BESTOW_TINT = 0xFFD060;   //부여(완드 → 장비): 금빛
	private static final int ACTIVE_GOLD = 0xFFE9A0;   //발현 칸
	private static final int TEXT_DIM    = 0xB4B4B4;

	private final ForestWand wand;
	private final int sel;     //고른 칸

	//발현 칸 테두리가 천천히 숨 쉬듯 빛남
	private ColorBlock activeGlow;
	private float glowTime;

	public WndForestWand( ForestWand wand ){
		this( wand, defaultSel( wand ) );
	}

	//처음엔 발현 중인(가운데) 정령, 없으면 깃든 첫 정령을 고름
	private static int defaultSel( ForestWand wand ){
		if (wand.slots[ForestWand.CENTER] != null) return ForestWand.CENTER;
		for (int i = 0; i < ForestWand.SLOTS; i++) if (wand.slots[i] != null) return i;
		return ForestWand.CENTER;
	}

	public WndForestWand( ForestWand wand, int selected ){
		super();
		this.wand = wand;
		this.sel = selected;

		// ── 머리줄: 완드 그림 + 제목, 오른쪽 끝에 도움말 ──
		ItemSprite wandIcon = new ItemSprite( wand );
		wandIcon.x = 0;
		wandIcon.y = 0;
		add( wandIcon );
		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( 19, (16 - title.height()) / 2f );
		add( title );
		IconButton help = new IconButton( Icons.get( Icons.INFO ) ){
			@Override
			protected void onClick() {
				showHelp();
			}
		};
		help.setRect( WIDTH - 16, 0, 16, 16 );
		add( help );
		float y = 16 + 7;

		// ── 정령 3칸: 가운데(발현)는 크게, 양옆(저장)은 작게. 빈칸을 누르면 곧바로 깃들이기 ──
		float total = CARD_S*2 + CARD_C + CARD_GAP*2;
		float x0 = (WIDTH - total) / 2f;
		float[] cardX = { x0, x0 + CARD_S + CARD_GAP, x0 + CARD_S + CARD_GAP*2 + CARD_C };
		for (int i = 0; i < ForestWand.SLOTS; i++){
			boolean center = i == ForestWand.CENTER;
			int size = center ? CARD_C : CARD_S;
			SlotCard card = new SlotCard( i, wand.slots[i], center, i == sel && wand.slots[i] != null );
			card.setRect( cardX[i], y + (CARD_C - size) / 2f, size, size );
			add( card );
			if (center) activeGlow = card.glow;
		}
		y += CARD_C + 2;
		RenderedTextBlock tag = PixelScene.renderTextBlock( Messages.get( this, "active_tag" ), 6 );
		tag.hardlight( wand.slots[ForestWand.CENTER] != null ? ACTIVE_GOLD : 0x777777 );
		tag.setPos( cardX[ForestWand.CENTER] + (CARD_C - tag.width()) / 2f, y );
		add( tag );
		y = tag.bottom() + 7;

		// ── 고른 정령: 이름과 한 줄 능력 (누르면 자세한 설명) ──
		final ForestWand.Spirit s = wand.slots[sel];
		float infoTop = y;
		if (s == null){
			RenderedTextBlock empty = PixelScene.renderTextBlock( Messages.get( this, "empty" ), 7 );
			empty.hardlight( 0xCCCCCC );
			empty.setPos( (WIDTH - empty.width()) / 2f, y );
			add( empty );
			RenderedTextBlock hint = PixelScene.renderTextBlock( Messages.get( this, "empty_hint" ), 6 );
			hint.hardlight( TEXT_DIM );
			hint.maxWidth( WIDTH );
			hint.align( RenderedTextBlock.CENTER_ALIGN );
			hint.setPos( (WIDTH - hint.width()) / 2f, empty.bottom() + 3 );
			add( hint );
			y = hint.bottom() + 8;
		} else {
			RenderedTextBlock name = PixelScene.renderTextBlock( s.title(), 8 );
			name.hardlight( s.color );
			name.setPos( (WIDTH - name.width()) / 2f, y );
			add( name );
			RenderedTextBlock brief = PixelScene.renderTextBlock( Messages.get( this, "brief_" + s.name().toLowerCase() ), 6 );
			brief.hardlight( TEXT_DIM );
			brief.maxWidth( WIDTH );
			brief.align( RenderedTextBlock.CENTER_ALIGN );
			brief.setPos( (WIDTH - brief.width()) / 2f, name.bottom() + 3 );
			add( brief );
			y = brief.bottom() + 7;
		}
		Button info = new Button(){
			@Override
			protected void onClick() {
				showHelp();
			}
		};
		info.setRect( 0, infoTop - 2, WIDTH, y - infoTop - 3 );
		add( info );

		// ── 고른 정령에게 할 일: 부여 · 발현 · (실체화) · 풀어 주기 ──
		if (s != null){
			ArrayList<String> keys = new ArrayList<>();
			keys.add( "bestow" );
			keys.add( "to_center" );
			if (s.greater && contractor()) keys.add( "materialize" );
			keys.add( "release" );
			int perRow = keys.size() == 4 ? 2 : 3;
			int rows = (keys.size() + perRow - 1) / perRow;
			float bw = (WIDTH - GAP*(perRow - 1)) / (float) perRow;
			for (int k = 0; k < keys.size(); k++){
				final String key = keys.get( k );
				String label = Messages.get( this, "act_" + key );
				StyledButton b;
				if (key.equals( "bestow" )){
					b = new TintButton( BESTOW_TINT, label ){
						@Override
						protected void onClick() {
							doSlotAction( sel, key );
						}
					};
				} else {
					b = new StyledButton( Chrome.Type.GREY_BUTTON_TR, label, 7 ){
						@Override
						protected void onClick() {
							doSlotAction( sel, key );
						}
					};
					if (key.equals( "release" )) b.textColor( 0xFF9A8A );
					if (key.equals( "materialize" )) b.textColor( s.color );
					//이미 가운데 칸이면 '발현'은 꺼 둠 (자리는 그대로 두어 버튼이 움직이지 않게)
					if (key.equals( "to_center" ) && sel == ForestWand.CENTER) b.enable( false );
				}
				b.setRect( (k % perRow) * (bw + GAP), y + (k / perRow) * (BTN_H + GAP), bw, BTN_H );
				add( b );
			}
			y += rows * (BTN_H + GAP) - GAP + 6;
		}

		// ── 가는 줄 아래로 완드 전체의 일: 깃들이기 · 되돌리기 ──
		ColorBlock line = new ColorBlock( WIDTH, 1, 0xFF5A5444 );
		line.y = y;
		add( line );
		y += 6;

		boolean full = wand.freeSlot() < 0;
		float hw = (WIDTH - GAP) / 2f;
		TintButton imbueBtn = new TintButton( IMBUE_TINT, Messages.get( this, full ? "btn_imbue_full" : "btn_imbue" ) ){
			@Override
			protected void onClick() {
				imbue();
			}
		};
		imbueBtn.enable( !full );
		imbueBtn.setRect( 0, y, hw, BTN_H );
		add( imbueBtn );

		StyledButton recallBtn = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "btn_recall" ), 8 ){
			@Override
			protected void onClick() {
				recall();
			}
		};
		recallBtn.enable( hasBestowed() );
		recallBtn.setRect( hw + GAP, y, hw, BTN_H );
		add( recallBtn );
		y += BTN_H;

		//정령 계약자: 실체화한 상급 정령을 완드로 거둠
		if (!ForestWand.materializedSpirits().isEmpty()){
			StyledButton unmat = new StyledButton( Chrome.Type.GREY_BUTTON_TR, Messages.get( this, "unmaterialize" ), 7 ){
				@Override
				protected void onClick() {
					hide();
					WndForestWand.this.wand.recallMaterialized( Dungeon.hero );
				}
			};
			unmat.setRect( 0, y + GAP, WIDTH, BTN_H - 2 );
			add( unmat );
			y = unmat.bottom();
		}

		resize( WIDTH, (int) Math.ceil( y ) );
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

			//바깥 빛 (고른 칸은 정령 빛깔, 발현 칸은 금빛으로 은은하게)
			int glowColor = selected ? sp.color : (center && sp != null ? ACTIVE_GOLD : 0x000000);
			glow = new ColorBlock( 1, 1, 0xFF000000 | glowColor );
			glow.alpha( selected ? 0.45f : (center && sp != null ? 0.3f : 0f) );
			add( glow );

			int frameColor;
			if (sp == null)        frameColor = 0xFF3E5A30;
			else if (selected)     frameColor = 0xFF000000 | brighten( sp.color );
			else if (center)       frameColor = 0xFFB8A060;
			else                   frameColor = 0xFF55524A;
			frame = new ColorBlock( 1, 1, frameColor );
			add( frame );

			bgColor = sp == null ? 0xFF141A12 : (0xFF000000 | dim( sp.color, selected ));
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
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			bg.color( bgColor & 0xFFFFFF );
		}

		@Override
		protected void onClick() {
			if (sp == null) imbue();
			else if (slot != sel) reopen( slot );
			else showHelp();
		}
	}

	//풀빛·금빛 버튼 (꺼지면 회색)
	private static class TintButton extends StyledButton {
		private final float tr, tg, tb;
		TintButton( int tint, String label ){
			super( Chrome.Type.GREY_BUTTON, label, 8 );
			tr = ((tint >> 16) & 0xFF) / 255f; tg = ((tint >> 8) & 0xFF) / 255f; tb = (tint & 0xFF) / 255f;
			bg.hardlight( tr, tg, tb );
		}
		@Override
		public void enable( boolean value ) {
			super.enable( value );
			if (value) bg.hardlight( tr, tg, tb );
			else       bg.hardlight( 0.45f, 0.45f, 0.45f );
		}
		@Override
		protected void onPointerDown() {
			super.onPointerDown();
			if (active) bg.hardlight( Math.min( 1f, tr * 1.25f ), Math.min( 1f, tg * 1.25f ), Math.min( 1f, tb * 1.25f ) );
		}
		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			if (active) bg.hardlight( tr, tg, tb );
		}
	}

	//도움말: 고른 정령의 자세한 능력 + 각 버튼이 하는 일
	private void showHelp(){
		ForestWand.Spirit s = wand.slots[sel];
		String body = Messages.get( this, "help" );
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
	private static boolean hasBestowed(){
		if (Dungeon.hero == null) return false;
		for (Item i : Dungeon.hero.belongings){
			if (spiritsOn( i ) > 0) return true;
		}
		return false;
	}

	//칸 바탕: 정령 빛깔을 어둡게 (고른 칸은 조금 밝게)
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

	//깃들이기: 창을 닫고 촉매를 고르게 함
	private void imbue(){
		if (wand.freeSlot() < 0){
			GLog.w( Messages.get( ForestWand.class, "full" ) );
			return;
		}
		hide();
		if (Dungeon.hero != null && Dungeon.hero.ready){
			wand.execute( Dungeon.hero, ForestWand.AC_IMBUE );
		}
	}

	private void reopen(){
		reopen( sel );
	}

	private void reopen( int slot ){
		hide();
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
				//장비에서 떼어 내면 정령은 알아서 숲의 완드로 역소환됨
				if (item instanceof Weapon) ((Weapon) item).enchant( null );
				else                        ((Armor) item).inscribe( null );
				Hero hero = Dungeon.hero;
				Sample.INSTANCE.play( Assets.Sounds.MELD );
				hero.sprite.operate( hero.pos );
				hero.spendAndNext( 1f );
			}
		} );
	}
}
