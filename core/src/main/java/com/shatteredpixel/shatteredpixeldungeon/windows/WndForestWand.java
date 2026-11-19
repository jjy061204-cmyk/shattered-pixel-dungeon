/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 숲의 완드에 깃든 정령 3칸 관리: 가운데에 놓기 / 정령 부여(다른 장비로 옮김) / 소환 해제, 장비에서 정령 거두기
 *  - 정령 부여사: 한 장비에 정령을 둘까지 깃들임 (둘 다 차 있으면 어느 쪽과 바꿀지 고름)
 *  - 정령 계약자: 상급 정령을 실체화시키고, 실체화한 정령을 다시 완드로 거둠
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
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
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class WndForestWand extends Window {

	private static final int WIDTH = 120;
	private static final int BTN_H = 18;
	private static final int GAP = 2;
	private static final int CARD = 36;     //정령 칸 크기
	private static final int CARD_GAP = 6;

	private final ForestWand wand;
	private final int sel;     //고른 칸

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

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width())/2f, 2 );
		add( title );

		// ── 정령 3칸: 왼쪽(저장) · 가운데(발현) · 오른쪽(저장) ──
		float y = title.bottom() + 6;
		float x = (WIDTH - (CARD*3 + CARD_GAP*2)) / 2f;
		for (int i = 0; i < ForestWand.SLOTS; i++){
			final int slot = i;
			final ForestWand.Spirit sp = wand.slots[i];
			float cx = x + i*(CARD + CARD_GAP);
			boolean center = i == ForestWand.CENTER;

			//테두리(고른 칸은 밝게) + 바탕(정령 빛깔을 어둡게)
			int frame = slot == sel ? 0xFFFFE9A0 : (center ? 0xFF8A7A50 : 0xFF4A4A4A);
			ColorBlock border = new ColorBlock( CARD, CARD, frame );
			border.x = cx; border.y = y;
			add( border );
			int bgc = sp == null ? 0xFF1E1E1E : (0xFF000000 | dim( sp.color ));
			ColorBlock bg = new ColorBlock( CARD - 2, CARD - 2, bgc );
			bg.x = cx + 1; bg.y = y + 1;
			add( bg );

			Image icon = new Image( Assets.Sprites.ITEM_ICONS );
			if (sp != null){
				icon.frame( ItemSpriteSheet.Icons.film.get( sp.icon() ) );
				icon.scale.set( 3f );
			} else {
				icon.frame( ItemSpriteSheet.Icons.film.get( ItemSpriteSheet.Icons.spirit( 0 ) ) );
				icon.scale.set( 3f );
				icon.alpha( 0f );
			}
			IconButton btn = new IconButton( icon ){
				@Override
				protected void onClick() {
					if (sp != null && slot != sel) reopen( slot );
				}
			};
			btn.setRect( cx, y, CARD, CARD );
			add( btn );

			if (sp == null){
				RenderedTextBlock empty = PixelScene.renderTextBlock( Messages.get( ForestWand.class, "slot_empty" ), 6 );
				empty.hardlight( 0x777777 );
				empty.setPos( cx + (CARD - empty.width())/2f, y + (CARD - empty.height())/2f );
				add( empty );
			}

			RenderedTextBlock lbl = PixelScene.renderTextBlock( Messages.get( this, center ? "card_active" : "card_stored" ), 6 );
			lbl.hardlight( center ? 0xFFE9A0 : 0xAAAAAA );
			lbl.setPos( cx + (CARD - lbl.width())/2f, y + CARD + 2 );
			add( lbl );
		}
		y += CARD + 12;

		// ── 고른 정령의 설명과 할 수 있는 일 ──
		final ForestWand.Spirit s = wand.slots[sel];
		if (s == null){
			RenderedTextBlock none = PixelScene.renderTextBlock( Messages.get( this, "none" ), 6 );
			none.maxWidth( WIDTH );
			none.setPos( 0, y );
			add( none );
			y = none.bottom() + 6;
		} else {
			RenderedTextBlock name = PixelScene.renderTextBlock( s.title(), 9 );
			name.hardlight( s.color );
			name.setPos( (WIDTH - name.width())/2f, y );
			add( name );
			RenderedTextBlock desc = PixelScene.renderTextBlock( Messages.get( ForestWand.class, "shot_" + s.name().toLowerCase() ), 6 );
			desc.maxWidth( WIDTH );
			desc.setPos( 0, name.bottom() + 3 );
			add( desc );
			y = desc.bottom() + 5;

			final ArrayList<String> keys = actionKeys( sel, s );
			int cols = 2;
			float bw = (WIDTH - GAP) / 2f;
			for (int k = 0; k < keys.size(); k++){
				final String key = keys.get( k );
				RedButton b = new RedButton( Messages.get( this, key ), 7 ){
					@Override
					protected void onClick() {
						doSlotAction( sel, key );
					}
				};
				int c = k % cols, r = k / cols;
				//마지막 줄에 하나만 남으면 가로로 꽉 채움
				boolean full = (k == keys.size() - 1) && (k % cols == 0);
				b.setRect( full ? 0 : c*(bw + GAP), y + r*(BTN_H + GAP), full ? WIDTH : bw, BTN_H );
				add( b );
			}
			y += ((keys.size() + cols - 1) / cols) * (BTN_H + GAP) + 3;
		}

		//깃들이기: 인벤토리를 열지 않고 여기서 바로
		RedButton imbue = new RedButton( Messages.get( this, "imbue" ), 7 ){
			@Override
			protected void onClick() {
				hide();
				if (Dungeon.hero != null && Dungeon.hero.ready){
					WndForestWand.this.wand.execute( Dungeon.hero, ForestWand.AC_IMBUE );
				}
			}
		};
		imbue.setRect( 0, y, WIDTH, BTN_H );
		add( imbue );
		y = imbue.bottom() + GAP;

		RedButton recall = new RedButton( Messages.get( this, "recall" ), 7 ){
			@Override
			protected void onClick() {
				recall();
			}
		};
		recall.setRect( 0, y, WIDTH, BTN_H );
		add( recall );
		float bottom = recall.bottom();

		//정령 계약자: 실체화한 상급 정령을 완드로 거둠
		if (!ForestWand.materializedSpirits().isEmpty()){
			RedButton unmat = new RedButton( Messages.get( this, "unmaterialize" ), 7 ){
				@Override
				protected void onClick() {
					hide();
					WndForestWand.this.wand.recallMaterialized( Dungeon.hero );
				}
			};
			unmat.setRect( 0, bottom + GAP, WIDTH, BTN_H );
			add( unmat );
			bottom = unmat.bottom();
		}

		resize( WIDTH, (int)bottom + 1 );
	}

	//정령 빛깔을 어둡게 (칸 바탕)
	private static int dim( int c ){
		int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
		r = r * 30 / 100 + 10; g = g * 30 / 100 + 10; b = b * 30 / 100 + 14;
		return (r << 16) | (g << 8) | b;
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

	private ArrayList<String> actionKeys( int slot, ForestWand.Spirit s ){
		ArrayList<String> keys = new ArrayList<>();
		if (slot != ForestWand.CENTER) keys.add( "to_center" );
		if (s.greater && contractor()) keys.add( "materialize" );
		keys.add( "bestow" );
		keys.add( "release" );
		return keys;
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
		SpiritSwitch.refreshAuraOf( hero );
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
