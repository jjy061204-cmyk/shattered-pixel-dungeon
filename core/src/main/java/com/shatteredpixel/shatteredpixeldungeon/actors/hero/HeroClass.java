/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.AscendedForm;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.Trinity;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpectralBlades;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.DevChest;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Rapier;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpike;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Reflection;

public enum HeroClass {

	WARRIOR( HeroSubClass.BERSERKER, HeroSubClass.GLADIATOR ),
	MAGE( HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK ),
	ROGUE( HeroSubClass.ASSASSIN, HeroSubClass.FREERUNNER ),
	HUNTRESS( HeroSubClass.SNIPER, HeroSubClass.WARDEN ),
	DUELIST( HeroSubClass.CHAMPION, HeroSubClass.MONK ),
	CLERIC( HeroSubClass.PRIEST, HeroSubClass.PALADIN ),
	//모드: 유물수집가 - 유물마다 고유한 추가 효과를 얻는 영웅
	COLLECTOR( HeroSubClass.CHANNELER, HeroSubClass.CURATOR ),
	//모드: 뱀파이어 - 적의 생명력을 빼앗아 핏빛 성배에 담고, 그것으로 기술을 씀 (갑옷 기술만 전사의 것을 빌림)
	VAMPIRE( HeroSubClass.TRUE_ANCESTOR, HeroSubClass.BLOOD_LORD ),
	//모드: 정령사 - 숲의 완드에 지형지물의 정령을 깃들여 정령 마법을 씀 (전직·특성·갑옷 기술은 아직 전사의 것을 빌림)
	ELEMENTALIST( HeroSubClass.SPIRIT_ENCHANTER, HeroSubClass.SPIRIT_CONTRACTOR ),
	//모드: 마법 기사 - 레벨에 따라 자라는 에테르 점토를 같은 티어의 무기 모양으로 빚어 싸움 (전직·특성·갑옷 기술은 아직 전사의 것을 빌림)
	MAGIC_KNIGHT( HeroSubClass.BERSERKER, HeroSubClass.GLADIATOR );

	private HeroSubClass[] subClasses;

	HeroClass( HeroSubClass...subClasses ) {
		this.subClasses = subClasses;
	}

	public void initHero( Hero hero ) {

		hero.heroClass = this;
		Talent.initClassTalents(hero);

		Item i = new ClothArmor().identify();
		if (!Challenges.isItemBlocked(i)) hero.belongings.armor = (ClothArmor)i;

		//모드: 모든 영웅이 음식 바구니를 들고 시작 (음식보다 먼저 챙겨야 음식이 바구니로 들어감)
		new com.shatteredpixel.shatteredpixeldungeon.items.bags.FoodBag().collect();

		i = new Food();
		if (!Challenges.isItemBlocked(i)) i.collect();

		new VelvetPouch().collect();
		Dungeon.LimitedDrops.VELVET_POUCH.drop();

		Waterskin waterskin = new Waterskin();
		waterskin.collect();


		new ScrollOfIdentify().identify();

		switch (this) {
			case WARRIOR:
				initWarrior( hero );
				break;

			case MAGE:
				initMage( hero );
				break;

			case ROGUE:
				initRogue( hero );
				break;

			case HUNTRESS:
				initHuntress( hero );
				break;

			case DUELIST:
				initDuelist( hero );
				break;

			case CLERIC:
				initCleric( hero );
				break;

			case COLLECTOR:
				initCollector( hero );
				break;

			case VAMPIRE:
				initVampire( hero );
				break;

			case ELEMENTALIST:
				initElementalist( hero );
				break;

			case MAGIC_KNIGHT:
				initMagicKnight( hero );
				break;

		}

		//모드: 실험 모드 - 테스터가 쓰던 아이템을 들고 시작
		if (Dungeon.experiment){
			giveExperimentKit( hero );
		}

		if (SPDSettings.quickslotWaterskin()) {
			for (int s = 0; s < QuickSlot.SIZE; s++) {
				if (Dungeon.quickslot.getItem(s) == null) {
					Dungeon.quickslot.setSlot(s, waterskin);
					break;
				}
			}
		}

	}

	public Badges.Badge masteryBadge() {
		switch (this) {
			case WARRIOR:
				return Badges.Badge.MASTERY_WARRIOR;
			case MAGE:
				return Badges.Badge.MASTERY_MAGE;
			case ROGUE:
				return Badges.Badge.MASTERY_ROGUE;
			case HUNTRESS:
				return Badges.Badge.MASTERY_HUNTRESS;
			case DUELIST:
				return Badges.Badge.MASTERY_DUELIST;
			case CLERIC:
				return Badges.Badge.MASTERY_CLERIC;
			case COLLECTOR: case VAMPIRE: case ELEMENTALIST: case MAGIC_KNIGHT:
				return null;
		}
		return null;
	}

	private static void initWarrior( Hero hero ) {
		(hero.belongings.weapon = new WornShortsword()).identify();
		ThrowingStone stones = new ThrowingStone();
		stones.identify().collect();

		Dungeon.quickslot.setSlot(0, stones);

		if (hero.belongings.armor != null){
			hero.belongings.armor.affixSeal(new BrokenSeal());
			Catalog.setSeen(BrokenSeal.class); //as it's not added to the inventory
		}

		new PotionOfHealing().identify();
		new ScrollOfRage().identify();
	}

	private static void initMage( Hero hero ) {
		MagesStaff staff;

		staff = new MagesStaff(new WandOfMagicMissile());

		(hero.belongings.weapon = staff).identify();
		hero.belongings.weapon.activate(hero);

		Dungeon.quickslot.setSlot(0, staff);

		new ScrollOfUpgrade().identify();
		new PotionOfLiquidFlame().identify();
	}

	private static void initRogue( Hero hero ) {
		(hero.belongings.weapon = new Dagger()).identify();

		CloakOfShadows cloak = new CloakOfShadows();
		(hero.belongings.artifact = cloak).identify();
		hero.belongings.artifact.activate( hero );

		ThrowingKnife knives = new ThrowingKnife();
		knives.identify().collect();

		Dungeon.quickslot.setSlot(0, cloak);
		Dungeon.quickslot.setSlot(1, knives);

		new ScrollOfMagicMapping().identify();
		new PotionOfInvisibility().identify();
	}

	//유물수집가: 단검과 투척 단검, 그리고 대도의 팔찌를 차고 시작
	private static void initCollector( Hero hero ) {
		(hero.belongings.weapon = new Dagger()).identify();

		com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband armband =
				new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband();
		(hero.belongings.artifact = armband).identify();
		hero.belongings.artifact.activate( hero );

		ThrowingKnife knives = new ThrowingKnife();
		knives.identify().collect();

		Dungeon.quickslot.setSlot(0, armband);
		Dungeon.quickslot.setSlot(1, knives);

		new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse().identify();
		new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision().identify();
	}

	private static void initVampire( Hero hero ) {
		(hero.belongings.weapon = new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.OrnamentalSword()).identify();
		hero.belongings.weapon.activate( hero );

		com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CrimsonGrail grail =
				new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CrimsonGrail();
		(hero.belongings.artifact = grail).identify();
		hero.belongings.artifact.activate( hero );

		Dungeon.quickslot.setSlot(0, grail);

		new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing().identify();
		new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror().identify();
	}

	//정령사: 숲의 완드(영혼의 활처럼 고유하고 버리거나 팔 수 없음)와 장갑을 들고 시작
	private static void initElementalist( Hero hero ) {
		(hero.belongings.weapon = new Gloves()).identify();

		com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand wand = new com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand();
		wand.identify().collect();
		Dungeon.quickslot.setSlot(0, wand);
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SpiritSwitch.attach( hero );

		new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame().identify();
		new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging().identify();
	}

	//마법 기사: 에테르 점토(1티어, 레벨 5마다 1티어씩 자람)를 들고 시작
	private static void initMagicKnight( Hero hero ) {
		com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherClay clay = new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EtherClay();
		(hero.belongings.weapon = clay).identify();
		hero.belongings.weapon.activate( hero );
		//처음부터 퀵슬롯 첫 칸에 넣어 둠 (누르면 바로 빚기)
		Dungeon.quickslot.setSlot( 0, clay );

		new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength().identify();
		new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade().identify();
	}

	private static void initHuntress( Hero hero ) {

		(hero.belongings.weapon = new Gloves()).identify();
		SpiritBow bow = new SpiritBow();
		bow.identify().collect();

		Dungeon.quickslot.setSlot(0, bow);

		new PotionOfMindVision().identify();
		new ScrollOfLullaby().identify();
	}

	private static void initDuelist( Hero hero ) {

		(hero.belongings.weapon = new Rapier()).identify();
		hero.belongings.weapon.activate(hero);

		ThrowingSpike spikes = new ThrowingSpike();
		spikes.quantity(2).identify().collect(); //set quantity is 3, but Duelist starts with 2

		Dungeon.quickslot.setSlot(0, hero.belongings.weapon);
		Dungeon.quickslot.setSlot(1, spikes);

		new PotionOfStrength().identify();
		new ScrollOfMirrorImage().identify();
	}

	private static void initCleric( Hero hero ) {

		(hero.belongings.weapon = new Cudgel()).identify();
		hero.belongings.weapon.activate(hero);

		HolyTome tome = new HolyTome();
		(hero.belongings.artifact = tome).identify();
		hero.belongings.artifact.activate( hero );

		Dungeon.quickslot.setSlot(0, tome);

		new PotionOfPurity().identify();
		new ScrollOfRemoveCurse().identify();
	}

	//실험 모드: 직업의 시작 아이템에 더해, 예전 테스터가 쓰던 개발용 아이템을 받음
	private static void giveExperimentKit( Hero hero ) {
		//넉넉한 돈과 감정 주문서
		Dungeon.gold += 10000;
		new ScrollOfIdentify().quantity(20).identify().collect();

		//물약, 주문서, 반지 종류를 전부 감정된 상태로 시작
		for (Class<?> cls : Generator.Category.POTION.classes){
			((Potion)Reflection.newInstance(cls)).setKnown();
		}
		for (Class<?> cls : Generator.Category.SCROLL.classes){
			((Scroll)Reflection.newInstance(cls)).setKnown();
		}
		for (Class<?> cls : Generator.Category.RING.classes){
			((Ring)Reflection.newInstance(cls)).setKnown();
		}

		//원하는 아이템을 바로 얻을 수 있는 개발자 상자, 층 이동과 몬스터·보스 소환을 하는 리모컨
		DevChest chest = new DevChest();
		chest.collect();
		com.shatteredpixel.shatteredpixeldungeon.items.DevRemote remote = new com.shatteredpixel.shatteredpixeldungeon.items.DevRemote();
		remote.collect();

		//빈 빠른 칸에 차례로 넣음 (직업의 시작 아이템은 그대로 둠)
		for (Item it : new Item[]{ chest, remote }){
			for (int s = 0; s < QuickSlot.SIZE; s++){
				if (Dungeon.quickslot.getItem(s) == null){
					Dungeon.quickslot.setSlot(s, it);
					break;
				}
			}
		}
	}

	public String title() {
		return Messages.get(HeroClass.class, name());
	}

	public String desc(){
		return Messages.get(HeroClass.class, name()+"_desc");
	}

	public String shortDesc(){
		return Messages.get(HeroClass.class, name()+"_desc_short");
	}

	public HeroSubClass[] subClasses() {
		return subClasses;
	}

	public ArmorAbility[] armorAbilities(){
		switch (this) {
			case WARRIOR: default:
				return new ArmorAbility[]{new HeroicLeap(), new Shockwave(), new Endure()};
			case MAGE:
				return new ArmorAbility[]{new ElementalBlast(), new WildMagic(), new WarpBeacon()};
			case ROGUE: case COLLECTOR:   //유물수집가 전용 갑옷 기술은 아직 없어 도적의 것을 빌림
				return new ArmorAbility[]{new SmokeBomb(), new DeathMark(), new ShadowClone()};
			case VAMPIRE:
				return new ArmorAbility[]{new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire.HeartPierce(),
						new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire.CrimsonCrown(),
						new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire.UndyingBlood()};
			case HUNTRESS:
				return new ArmorAbility[]{new SpectralBlades(), new NaturesPower(), new SpiritHawk()};
			case DUELIST:
				return new ArmorAbility[]{new Challenge(), new ElementalStrike(), new Feint()};
			case CLERIC:
				return new ArmorAbility[]{new AscendedForm(), new Trinity(), new PowerOfMany()};
		}
	}

	public String spritesheet() {
		switch (this) {
			case WARRIOR: default:
				return Assets.Sprites.WARRIOR;
			case MAGE:
				return Assets.Sprites.MAGE;
			case ROGUE:
				return Assets.Sprites.ROGUE;
			case HUNTRESS:
				return Assets.Sprites.HUNTRESS;
			case DUELIST:
				return Assets.Sprites.DUELIST;
			case CLERIC:
				return Assets.Sprites.CLERIC;
			case COLLECTOR:
				return Assets.Sprites.COLLECTOR;
			case VAMPIRE:
				return Assets.Sprites.VAMPIRE;
			case ELEMENTALIST:
				return Assets.Sprites.ELEMENTALIST;
			case MAGIC_KNIGHT:
				return Assets.Sprites.MAGIC_KNIGHT;
		}
	}

	//모드: 64x64칸의 세밀한 도트(꼬마 비율)를 쓰는 영웅 (게임에서는 한 칸 크기로 줄여 그림). 없으면 null
	public String hdSheet(){
		switch (this){
			case COLLECTOR: return Assets.Sprites.COLLECTOR;
			case VAMPIRE:   return Assets.Sprites.VAMPIRE;
			case ELEMENTALIST: return Assets.Sprites.ELEMENTALIST;
			case MAGIC_KNIGHT: return Assets.Sprites.MAGIC_KNIGHT;
			default:        return null;
		}
	}

	//모드: 세밀한 도트 영웅을 그릴 때 곱하는 크기. 마법 기사는 64칸 안에 그린 몸(49칸)이 뱀파이어(57칸)보다 작아서 키를 맞춰 키움
	public float hdScale(){
		return this == MAGIC_KNIGHT ? 57f / 49f : 1f;
	}

	public String splashArt(){
		switch (this) {
			case WARRIOR: default:
				return Assets.Splashes.WARRIOR;
			case MAGE:
				return Assets.Splashes.MAGE;
			case ROGUE:
				return Assets.Splashes.ROGUE;
			case HUNTRESS:
				return Assets.Splashes.HUNTRESS;
			case DUELIST:
				return Assets.Splashes.DUELIST;
			case CLERIC:
				return Assets.Splashes.CLERIC;
			case COLLECTOR:
				return Assets.Splashes.COLLECTOR;
			case VAMPIRE:
				return Assets.Splashes.VAMPIRE;
			case ELEMENTALIST:
				return Assets.Splashes.ELEMENTALIST;
			case MAGIC_KNIGHT:
				return Assets.Splashes.MAGIC_KNIGHT;
		}
	}
	
	public boolean isUnlocked(){
		//always unlock on debug builds
		if (DeviceCompat.isDebug()) return true;

		switch (this){
			case WARRIOR: default:
				return true;
			case MAGE:
				return Badges.isUnlocked(Badges.Badge.UNLOCK_MAGE);
			case ROGUE:
				return Badges.isUnlocked(Badges.Badge.UNLOCK_ROGUE);
			case HUNTRESS:
				return Badges.isUnlocked(Badges.Badge.UNLOCK_HUNTRESS);
			case DUELIST:
				return Badges.isUnlocked(Badges.Badge.UNLOCK_DUELIST);
			case CLERIC:
				return Badges.isUnlocked(Badges.Badge.UNLOCK_CLERIC);
			case COLLECTOR: case VAMPIRE: case ELEMENTALIST: case MAGIC_KNIGHT:
				return true;
		}
	}
	
	public String unlockMsg() {
		return shortDesc() + "\n\n" + Messages.get(HeroClass.class, name()+"_unlock");
	}

}
