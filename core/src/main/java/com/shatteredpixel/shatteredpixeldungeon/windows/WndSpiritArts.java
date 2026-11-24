/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령술의 이름·그림·쓸 수 있는지·쓰기 (정령술 창 WndForestWand 와 정령 버튼이 함께 씀)
 *  - 정령 버튼을 누르면 등록한 정령술을 바로 씀, 길게 누르면 정령술 창
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SpiritSwitch;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.levels.ModernLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class WndSpiritArts {

	//정령술 이름표 (정령 버튼에 등록할 수 있는 것들)
	public static final String SWITCH = "switch", SHOOT = "shoot", IMBUE = "imbue", RECALL = "recall",
			CONTRACT = "contract", UNMATERIALIZE = "unmaterialize", BESTOW = "bestow", RELEASE = "release",
			MANAGE = "manage";

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
}
