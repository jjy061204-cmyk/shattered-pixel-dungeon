/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 속성 피해: 녹픽던의 물리 피해·마법 피해와는 다른 세 번째 갈래.
 *
 *  ElementalDamage                 ← 모든 속성 피해 (카테고리 맨 위)
 *   ├ SpiritElement                ← 정령 속성 (정령사 · 정령 · 정령 문양이 입히는 피해)
 *   │   ├ 원래 있던 속성: Water 물 · Fire 불 · Ice 냉기 · Shock 번개
 *   │   ├ 정령사의 속성: Wind 바람 · Nature 자연 · Earth 땅 · Rock 바위 · Gold 황금 · Dark 어둠 · Rune 룬 · Chaos 혼돈
 *   │   └ 지역 속성: Life 생명(흡혈귀의 성) · Crystal 수정(수정 광맥)
 *   └ Ether 에테르                  ← 마법 기사 전용 속성
 *
 * 카테고리를 클래스 상속으로 묶어 두었으므로 몹·버프의 저항/면역을 한 줄로 줄 수 있음
 *  (녹픽던의 Char.resist / isImmune 은 isAssignableFrom 으로 비교함)
 *   resistances.add( ElementalDamage.class )        → 모든 속성 피해 반감
 *   resistances.add( ElementalDamage.SpiritElement.class ) → 정령 속성만 반감
 *   resistances.add( ElementalDamage.Fire.class )   → 불 속성만 반감
 *   immunities 도 같은 방식
 * 물·불·냉기·번개는 녹픽던에 원래 있던 면역·저항(불길, 빙결, 전기)도 함께 따름 (legacyResist)
 *
 * 속성 피해는 항상 ElementalDamage.hit(...) 으로 입힘 (면역·저항을 한 번만, 빠짐없이 적용)
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.elemental;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class ElementalDamage {

	// ───── 카테고리 ─────
	public enum Category {
		SPIRIT,     //정령 속성
		ETHER;      //마법 기사 전용 속성

		public String title(){
			return Messages.get( ElementalDamage.class, "cat_" + name().toLowerCase() );
		}
	}

	//피해 숫자 옆에 뜨는 아이콘
	public abstract int icon();

	//이 속성이 속한 카테고리
	public abstract Category category();

	//속성 이름 (물, 불, 에테르 …)
	public String title(){
		return Messages.get( this, "name" );
	}

	//녹픽던에 원래 있던 같은 속성의 면역·저항 (없으면 null)
	public Class<?> legacyResist(){ return null; }

	// ───── 정령 속성 ─────
	public static abstract class SpiritElement extends ElementalDamage {
		@Override public Category category(){ return Category.SPIRIT; }
	}

	//녹픽던에 원래 있던 속성
	public static class Water extends SpiritElement {
		@Override public int icon(){ return FloatingText.WATER; }
	}
	public static class Fire extends SpiritElement {
		@Override public int icon(){ return FloatingText.BURNING; }
		@Override public Class<?> legacyResist(){ return Burning.class; }
	}
	public static class Ice extends SpiritElement {
		@Override public int icon(){ return FloatingText.FROST; }
		@Override public Class<?> legacyResist(){ return Frost.class; }
	}
	public static class Shock extends SpiritElement {
		@Override public int icon(){ return FloatingText.SHOCKING; }
		@Override public Class<?> legacyResist(){ return Electricity.class; }
	}

	//정령사가 새로 다루는 속성
	public static class Wind extends SpiritElement {
		@Override public int icon(){ return FloatingText.WIND_DMG; }
	}
	public static class Nature extends SpiritElement {
		@Override public int icon(){ return FloatingText.NATURE_DMG; }
	}
	public static class Earth extends SpiritElement {
		@Override public int icon(){ return FloatingText.EARTH_DMG; }
	}
	public static class Rock extends SpiritElement {
		@Override public int icon(){ return FloatingText.ROCK_DMG; }
	}
	public static class Gold extends SpiritElement {
		@Override public int icon(){ return FloatingText.GOLD_DMG; }
	}
	public static class Dark extends SpiritElement {
		@Override public int icon(){ return FloatingText.DARK_DMG; }
	}
	public static class Rune extends SpiritElement {
		@Override public int icon(){ return FloatingText.RUNE_DMG; }
	}
	public static class Chaos extends SpiritElement {
		@Override public int icon(){ return FloatingText.CHAOS_DMG; }
	}

	//지역 속성: 그 지역에서만 깨어나는 정령의 힘
	public static class Life extends SpiritElement {      //흡혈귀의 성, 고인 피에서 깨어난 불완전한 생명의 정령
		@Override public int icon(){ return FloatingText.LIFE_DMG; }
	}
	public static class Crystal extends SpiritElement {   //수정 광맥의 수정 정령 (푸른·초록·붉은 수정 모두)
		@Override public int icon(){ return FloatingText.CRYSTAL_DMG; }
	}

	// ───── 마법 기사 전용 속성 ─────
	//에테르: 마법 기사의 에테르 점토와 갑옷 능력이 입히는 피해
	public static class Ether extends ElementalDamage {
		@Override public int icon(){ return FloatingText.ETHER_DMG; }
		@Override public Category category(){ return Category.ETHER; }
	}

	// ───── 목록 ─────
	public static final List<Class<? extends SpiritElement>> SPIRIT_ELEMENTS = Collections.unmodifiableList( Arrays.asList(
			Water.class, Fire.class, Ice.class, Shock.class,
			Wind.class, Nature.class, Earth.class, Rock.class, Gold.class, Dark.class, Rune.class, Chaos.class,
			Life.class, Crystal.class ) );

	public static final List<Class<? extends ElementalDamage>> ALL;
	static {
		ArrayList<Class<? extends ElementalDamage>> all = new ArrayList<>( SPIRIT_ELEMENTS );
		all.add( Ether.class );
		ALL = Collections.unmodifiableList( all );
	}

	//한 카테고리에 속한 속성들
	public static List<Class<? extends ElementalDamage>> inCategory( Category cat ){
		ArrayList<Class<? extends ElementalDamage>> out = new ArrayList<>();
		for (Class<? extends ElementalDamage> c : ALL){
			ElementalDamage e = Reflection.newInstance( c );
			if (e != null && e.category() == cat) out.add( c );
		}
		return out;
	}

	// ───── 판별 ─────
	//피해 출처가 속성 피해인지 (Char.damage 의 src 등)
	public static boolean isElemental( Object src ){
		return src instanceof ElementalDamage;
	}
	//피해 출처가 정령 속성인지
	public static boolean isSpirit( Object src ){
		return src instanceof SpiritElement;
	}
	//피해 출처가 에테르인지
	public static boolean isEther( Object src ){
		return src instanceof Ether;
	}

	// ───── 공통 피해 처리 ─────
	//속성 피해를 입히고 실제로 깎인 양(체력 + 보호막)을 돌려줌
	//  면역: 그 속성(또는 카테고리)과 녹픽던 원래 면역 중 하나라도 있으면 0
	//  저항: 그 속성·카테고리 저항은 Char.damage 가 적용, 녹픽던 원래 저항은 여기서 적용
	public static int hit( Char target, float amount, ElementalDamage type ){
		if (target == null || type == null || !target.isAlive() || amount <= 0) return 0;
		Class<?> rc = type.legacyResist();
		if (target.isImmune( type.getClass() )) return 0;
		if (rc != null){
			if (target.isImmune( rc )) return 0;
			amount *= target.resist( rc );
		}
		int d = Math.max( 1, Math.round( amount ) );
		int before = target.HP + target.shielding();
		target.damage( d, type );
		return Math.max( 0, before - Math.max( 0, target.HP ) - target.shielding() );
	}

	//에테르 피해
	public static int etherHit( Char target, float amount ){
		return hit( target, amount, new Ether() );
	}

	//정령 → 그 정령이 입히는 속성 피해 (상급 정령은 바탕 원소를 따름)
	public static SpiritElement of( ForestWand.Spirit s ){
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
			case LIFE:                              return new Life();
			case CRYSTAL_BLUE: case CRYSTAL_GREEN: case CRYSTAL_RED: return new Crystal();
			case CHAOS: default:                    return new Chaos();
		}
	}
}
