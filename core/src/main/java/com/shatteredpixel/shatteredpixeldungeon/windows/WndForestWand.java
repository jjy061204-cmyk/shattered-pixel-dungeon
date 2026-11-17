/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 숲의 완드에 깃든 정령 3칸 관리: 가운데에 놓기 / 정령 부여(다른 장비로 옮김) / 소환 해제, 장비에서 정령 거두기
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
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
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class WndForestWand extends Window {

	private static final int WIDTH = 120;
	private static final int BTN_H = 18;
	private static final int GAP = 2;

	private final ForestWand wand;

	public WndForestWand( ForestWand wand ){
		super();
		this.wand = wand;

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width())/2f, 2 );
		add( title );

		RenderedTextBlock info = PixelScene.renderTextBlock( Messages.get( this, "info" ), 6 );
		info.maxWidth( WIDTH );
		info.setPos( 0, title.bottom() + 4 );
		add( info );

		float y = info.bottom() + 5;
		for (int i = 0; i < ForestWand.SLOTS; i++){
			final int slot = i;
			ForestWand.Spirit s = wand.slots[i];
			String label = Messages.get( ForestWand.class, "slot_" + i ) + ": "
					+ (s == null ? Messages.get( ForestWand.class, "slot_empty" ) : s.title());
			RedButton b = new RedButton( label, 8 ){
				@Override
				protected void onClick() {
					slotMenu( slot );
				}
			};
			if (s != null) b.textColor( s.color );
			b.enable( s != null );
			b.setRect( i == ForestWand.CENTER ? 0 : 6, y, i == ForestWand.CENTER ? WIDTH : WIDTH - 12, BTN_H );
			add( b );
			y += BTN_H + GAP;
		}

		RedButton recall = new RedButton( Messages.get( this, "recall" ), 8 ){
			@Override
			protected void onClick() {
				recall();
			}
		};
		recall.setRect( 0, y + 3, WIDTH, BTN_H );
		add( recall );

		resize( WIDTH, (int)(recall.bottom()) + 1 );
	}

	private void reopen(){
		hide();
		GameScene.show( new WndForestWand( wand ) );
	}

	private void slotMenu( final int slot ){
		final ForestWand.Spirit s = wand.slots[slot];
		if (s == null) return;
		final boolean center = slot == ForestWand.CENTER;
		String[] opts = center
				? new String[]{ Messages.get( this, "bestow" ), Messages.get( this, "release" ) }
				: new String[]{ Messages.get( this, "to_center" ), Messages.get( this, "bestow" ), Messages.get( this, "release" ) };
		GameScene.show( new WndOptions( s.title(), Messages.get( ForestWand.class, "shot_" + s.name().toLowerCase() ), opts ){
			@Override
			protected void onSelect( int index ) {
				if (!center) index -= 1;
				if (index == -1){
					wand.moveToCenter( slot );
					GLog.i( Messages.get( WndForestWand.class, "centered", s.title() ) );
					reopen();
				} else if (index == 0){
					WndForestWand.this.hide();
					bestow( slot );
				} else {
					wand.slots[slot] = null;
					Item.updateQuickslot();
					Hero hero = Dungeon.hero;
					hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
					GLog.i( Messages.get( WndForestWand.class, "released", s.title() ) );
					reopen();
				}
			}
		} );
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
				ForestWand.Spirit s = wand.slots[slot];
				if (item == null || s == null) return;
				Hero hero = Dungeon.hero;
				if (item instanceof Weapon){
					Weapon w = (Weapon) item;
					if (w.hasCurseEnchant()){ GLog.w( Messages.get( WndForestWand.class, "cursed" ) ); return; }
					if (w.enchantment != null && !(w.enchantment instanceof SpiritEnchant)) GLog.w( Messages.get( WndForestWand.class, "replaced", w.enchantment.name() ) );
					//완드에서 먼저 꺼내야, 장비에 있던 정령이 비워진 칸으로 역소환될 수 있음
					wand.slots[slot] = null;
					w.enchant( new SpiritEnchant( s ) );
				} else {
					Armor a = (Armor) item;
					if (a.hasCurseGlyph()){ GLog.w( Messages.get( WndForestWand.class, "cursed" ) ); return; }
					if (a.glyph != null && !(a.glyph instanceof SpiritGlyph)) GLog.w( Messages.get( WndForestWand.class, "replaced", a.glyph.name() ) );
					wand.slots[slot] = null;
					a.inscribe( new SpiritGlyph( s ) );
				}
				Item.updateQuickslot();
				Enchanting.show( hero, item );
				ForestWand.imbueFx( hero.pos, s );
				Sample.INSTANCE.play( Assets.Sounds.MELD );
				GLog.p( Messages.get( WndForestWand.class, "bestowed", s.title(), item.name() ) );
				hero.sprite.operate( hero.pos );
				hero.spendAndNext( 1f );
			}
		} );
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
				return (item instanceof Weapon && ((Weapon) item).enchantment instanceof SpiritEnchant)
						|| (item instanceof Armor && ((Armor) item).glyph instanceof SpiritGlyph);
			}
			@Override
			public void onSelect( Item item ) {
				if (item == null) return;
				int slot = wand.freeSlot();
				if (slot < 0){ GLog.w( Messages.get( ForestWand.class, "full" ) ); return; }
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
