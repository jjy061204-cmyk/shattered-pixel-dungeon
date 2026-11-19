/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령의 속성 피해: 녹픽던의 물·불·냉기·번개 피해처럼 '마법 피해'와는 다른 갈래.
 * 속성마다 따로 된 피해 출처 클래스를 두어, 피해 숫자 옆 아이콘으로 바로 구분되고
 * 나중에 몹마다 속성별 면역·저항(resistances.add(ElementalDamage.Gold.class) 등)을 줄 수도 있음.
 *  물·불·냉기·번개는 녹픽던에 원래 있던 면역·저항(불길, 빙결, 전기)을 그대로 따름
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.elemental;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;

public abstract class ElementalDamage {

	//피해 숫자 옆에 뜨는 아이콘
	public abstract int icon();

	//녹픽던에 원래 있던 같은 속성의 면역·저항 (없으면 null)
	public Class<?> legacyResist(){ return null; }

	// ── 녹픽던에 원래 있던 속성 ──
	public static class Water extends ElementalDamage {
		@Override public int icon(){ return FloatingText.WATER; }
	}
	public static class Fire extends ElementalDamage {
		@Override public int icon(){ return FloatingText.BURNING; }
		@Override public Class<?> legacyResist(){ return Burning.class; }
	}
	public static class Ice extends ElementalDamage {
		@Override public int icon(){ return FloatingText.FROST; }
		@Override public Class<?> legacyResist(){ return Frost.class; }
	}
	public static class Shock extends ElementalDamage {
		@Override public int icon(){ return FloatingText.SHOCKING; }
		@Override public Class<?> legacyResist(){ return Electricity.class; }
	}

	// ── 정령사가 새로 다루는 속성 ──
	public static class Wind extends ElementalDamage {
		@Override public int icon(){ return FloatingText.WIND_DMG; }
	}
	public static class Nature extends ElementalDamage {
		@Override public int icon(){ return FloatingText.NATURE_DMG; }
	}
	public static class Earth extends ElementalDamage {
		@Override public int icon(){ return FloatingText.EARTH_DMG; }
	}
	public static class Rock extends ElementalDamage {
		@Override public int icon(){ return FloatingText.ROCK_DMG; }
	}
	public static class Gold extends ElementalDamage {
		@Override public int icon(){ return FloatingText.GOLD_DMG; }
	}
	public static class Dark extends ElementalDamage {
		@Override public int icon(){ return FloatingText.DARK_DMG; }
	}
	public static class Rune extends ElementalDamage {
		@Override public int icon(){ return FloatingText.RUNE_DMG; }
	}
	public static class Chaos extends ElementalDamage {
		@Override public int icon(){ return FloatingText.CHAOS_DMG; }
	}

	//정령 → 그 정령이 입히는 속성 피해 (상급 정령은 바탕 원소를 따름)
	public static ElementalDamage of( ForestWand.Spirit s ){
		switch (s){
			case WATER: case UNDINE:                return new Water();
			case FIRE: case FLAME: case SALAMANDER: return new Fire();
			case FROST: case ICE:                   return new Ice();
			case SHOCK:                             return new Shock();
			case WIND: case SYLPH:                  return new Wind();
			case NATURE:                            return new Nature();
			case EARTH: case GNOME:                 return new Earth();
			case ROCK:                              return new Rock();
			case GOLD:                              return new Gold();
			case DARK:                              return new Dark();
			case RUNE:                              return new Rune();
			case CHAOS: default:                    return new Chaos();
		}
	}
}
