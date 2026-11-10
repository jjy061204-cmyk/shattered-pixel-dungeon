/*
 * Shattered Pixel Dungeon 모드: 엑스칼리버 (6티어)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.GoldSparkle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

//6티어 전설의 검. 처음에는 봉인된 상태로 존재하고,
//감정된 강화 주문서 8장 + 저주 해제 주문서 1장을 바치면 같은 아이템이 그대로 엑스칼리버로 깨어남.
//강화 없이도 5티어 +8 무기 수준 (13-78), 강화 성장은 특대도끼와 같음 (+1/+6), 힘 20
public class Excalibur extends Greatsword {

	public static final String AC_UNSEAL = "UNSEAL";
	public static final int NEED_UPGRADE = 8;
	public static final int NEED_REMOVE_CURSE = 1;

	//봉인 상태 (처음엔 봉인됨)
	public boolean sealed = true;

	{
		image = ItemSpriteSheet.EXCALIBUR;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.85f;

		tier = 6;
		unique = true;
		bones = false;
	}

	@Override
	public int min( int lvl ) {
		return 13 + lvl;
	}

	@Override
	public int max( int lvl ) {
		return 78 + 6 * lvl;
	}

	//힘 요구치는 특대도끼와 같은 20
	@Override
	public int STRReq( int lvl ) {
		return STRReq( 6, lvl );
	}

	@Override
	protected void duelistAbility( Hero hero, Integer target ) {
		int dmgBoost = augment.damageFactor( abilityBoost( buffedLvl() ) );
		Sword.cleaveAbility( hero, target, 1, dmgBoost, this );
	}

	private static int abilityBoost( int lvl ){
		return 15 + 2 * lvl;
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = levelKnown ? abilityBoost( buffedLvl() ) : abilityBoost( 0 );
		if (levelKnown){
			return Messages.get( this, "ability_desc", augment.damageFactor( min() + dmgBoost ), augment.damageFactor( max() + dmgBoost ) );
		} else {
			return Messages.get( this, "typical_ability_desc", min( 0 ) + dmgBoost, max( 0 ) + dmgBoost );
		}
	}

	@Override
	public String upgradeAbilityStat( int level ){
		int dmgBoost = abilityBoost( level );
		return augment.damageFactor( min( level ) + dmgBoost ) + "-" + augment.damageFactor( max( level ) + dmgBoost );
	}

	//은은한 금빛 맥동
	private static final ItemSprite.Glowing GOLD = new ItemSprite.Glowing( 0xFFD860, 2f );

	@Override
	public ItemSprite.Glowing glowing() {
		if (sealed) return null;
		ItemSprite.Glowing g = super.glowing();
		return g != null ? g : GOLD;
	}

	//바닥에 놓여 있으면 금빛 점이 천천히 떠오름
	@Override
	public Emitter emitter() {
		if (sealed) return null;
		Emitter e = new Emitter();
		e.fillTarget = true;
		e.pour( GoldSparkle.SHIMMER, 0.18f );
		return e;
	}

	//==== 봉인 ====

	@Override
	public int image() {
		return sealed ? ItemSpriteSheet.SEALED_EXCALIBUR : ItemSpriteSheet.EXCALIBUR;
	}

	@Override
	public String name() {
		return sealed ? Messages.get( this, "sealed_name" ) : super.name();
	}

	@Override
	public String desc() {
		return sealed ? Messages.get( this, "sealed_desc" ) : super.desc();
	}

	@Override
	public String info() {
		if (!sealed) return super.info();
		String info = desc();
		if (Dungeon.hero != null){
			info += "\n\n" + Messages.get( this, "progress",
					count( Dungeon.hero, ScrollOfUpgrade.class ), NEED_UPGRADE,
					count( Dungeon.hero, ScrollOfRemoveCurse.class ), NEED_REMOVE_CURSE );
		}
		return info;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (sealed){
			actions.remove( AC_EQUIP );
			actions.remove( AC_ABILITY );
			actions.add( AC_UNSEAL );
		}
		return actions;
	}

	@Override
	public String defaultAction() {
		return sealed ? AC_UNSEAL : super.defaultAction();
	}

	@Override
	public boolean doEquip( Hero hero ) {
		if (sealed){
			GLog.w( Messages.get( this, "cant_equip" ) );
			return false;
		}
		return super.doEquip( hero );
	}

	@Override
	public boolean equipSecondary( Hero hero ) {
		if (sealed){
			GLog.w( Messages.get( this, "cant_equip" ) );
			return false;
		}
		return super.equipSecondary( hero );
	}

	@Override
	public boolean isUpgradable() {
		return !sealed && super.isUpgradable();
	}

	@Override
	public com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon enchant( Enchantment ench ) {
		if (sealed) return this;   //봉인된 채로는 마법이 스며들지 않음
		return super.enchant( ench );
	}

	//감정된 주문서만 셈
	private static int count( Hero hero, Class<? extends Item> cls ){
		int n = 0;
		for (Item i : hero.belongings.getAllItems( cls )){
			if (i.isIdentified()) n += i.quantity();
		}
		return n;
	}

	private static void consume( Hero hero, Class<? extends Item> cls, int need ){
		for (Item i : hero.belongings.getAllItems( cls )){
			while (need > 0 && i.isIdentified() && i.quantity() > 0){
				i.detach( hero.belongings.backpack );
				need--;
			}
			if (need <= 0) return;
		}
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_UNSEAL ) && sealed){
			int up = count( hero, ScrollOfUpgrade.class );
			int rc = count( hero, ScrollOfRemoveCurse.class );
			if (up < NEED_UPGRADE || rc < NEED_REMOVE_CURSE){
				GLog.w( Messages.get( this, "not_enough", up, NEED_UPGRADE, rc, NEED_REMOVE_CURSE ) );
				return;
			}

			consume( hero, ScrollOfUpgrade.class, NEED_UPGRADE );
			consume( hero, ScrollOfRemoveCurse.class, NEED_REMOVE_CURSE );

			//같은 아이템이 그대로 깨어남
			sealed = false;
			cursed = false;
			identify();
			updateQuickslot();

			Sample.INSTANCE.play( Assets.Sounds.CHAINS );
			Sample.INSTANCE.play( Assets.Sounds.LEVELUP, 1f, 0.8f );
			if (hero.sprite != null){
				new Flare( 8, 40 ).color( 0xFFD860, true ).show( hero.sprite, 2.5f );
				hero.sprite.emitter().burst( GoldSparkle.BURST, 30 );
			}
			GameScene.flash( 0x80FFE6A0 );
			GLog.p( Messages.get( this, "unsealed" ) );

			hero.spendAndNext( 1f );
		}
	}

	private static final String SEALED = "sealed";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SEALED, sealed );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		sealed = bundle.getBoolean( SEALED );
	}

	@Override
	public int value() {
		return sealed ? 300 : 1000 * quantity;
	}
}
