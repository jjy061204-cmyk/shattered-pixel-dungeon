/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 마법 기사의 전용 무기 '에테르 점토': 여러 빛깔이 섞인 반투명한 마력 응집체.
 *  - 1티어로 시작해 영웅 레벨 5마다 1티어씩, 최대 5티어까지 자람 (1~4레벨 1티어, 5레벨 2티어 … 20레벨 5티어)
 *  - '빚기'로 지금 다룰 수 있는 티어 이하의 근접 무기 모양을 골라, 그 무기와 같은 성능(피해·명중·공격 속도·사거리·힘·막기)으로 씀
 *  - 아직 빚지 않았으면 같은 티어의 기본 무기 성능
 * 고유한 무기라 뼈 무덤에 남지 않음. 강화·마법 부여는 일반 무기처럼 됨
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class EtherClay extends MeleeWeapon {

	public static final String AC_SHAPE = "SHAPE";
	public static final String AC_ABSORB = "ABSORB";
	public static final String AC_SALUTE = "SALUTE";

	public static final int MAX_TIER = 5;

	{
		image = ItemSpriteSheet.ETHER_CLAY;
		hitSound = Assets.Sounds.HIT_MAGIC;
		hitSoundPitch = 1.1f;

		tier = 1;

		unique = true;
		bones = false;
	}

	//지금 빚은 모양 (없으면 아직 빚지 않은 응집체)
	private Class<? extends MeleeWeapon> formClass = null;
	private MeleeWeapon form = null;

	//영웅 레벨로 다룰 수 있는 최고 티어: 5레벨마다 1티어씩 (에테르 단련: 4.5/4/3.5레벨마다)
	public static int maxTier( Hero hero ){
		int lvl = hero != null ? hero.lvl : 1;
		int pts = hero != null ? hero.pointsInTalent( Talent.ETHER_TEMPERING ) : 0;
		return maxTier( lvl, pts );
	}

	public static int maxTier( int lvl, int tempering ){
		float step = 5f - 0.5f*tempering;
		return Math.min( MAX_TIER, 1 + (int)(lvl / step) );
	}

	//빚을 수 있는 모양: 그 티어에서 던전에 실제로 나오는 근접 무기
	public static ArrayList<Class<? extends MeleeWeapon>> formsOf( int tier ){
		Generator.Category cat;
		switch (tier){
			case 1: default: cat = Generator.Category.WEP_T1; break;
			case 2: cat = Generator.Category.WEP_T2; break;
			case 3: cat = Generator.Category.WEP_T3; break;
			case 4: cat = Generator.Category.WEP_T4; break;
			case 5: cat = Generator.Category.WEP_T5; break;
		}
		ArrayList<Class<? extends MeleeWeapon>> list = new ArrayList<>();
		for (int i = 0; i < cat.classes.length; i++){
			if (cat.defaultProbs[i] <= 0) continue;
			if (!MeleeWeapon.class.isAssignableFrom( cat.classes[i] )) continue;
			if (cat.classes[i] == MagesStaff.class) continue;
			list.add( (Class<? extends MeleeWeapon>) cat.classes[i] );
		}
		return list;
	}

	//빚은 무기 모양마다 점토 빛깔을 입힌 그림 (ItemSpriteSheet.ETHER_FORMS 순서)
	public static final int[] FORM_IMAGES = {
			ItemSpriteSheet.WORN_SHORTSWORD, ItemSpriteSheet.DAGGER, ItemSpriteSheet.GLOVES, ItemSpriteSheet.RAPIER, ItemSpriteSheet.CUDGEL, ItemSpriteSheet.SHORTSWORD, ItemSpriteSheet.HAND_AXE, ItemSpriteSheet.SPEAR, ItemSpriteSheet.QUARTERSTAFF, ItemSpriteSheet.DIRK, ItemSpriteSheet.SICKLE, ItemSpriteSheet.SWORD, ItemSpriteSheet.MACE, ItemSpriteSheet.SCIMITAR, ItemSpriteSheet.ROUND_SHIELD, ItemSpriteSheet.SAI, ItemSpriteSheet.WHIP, ItemSpriteSheet.LONGSWORD, ItemSpriteSheet.BATTLE_AXE, ItemSpriteSheet.FLAIL, ItemSpriteSheet.RUNIC_BLADE, ItemSpriteSheet.ASSASSINS_BLADE, ItemSpriteSheet.CROSSBOW, ItemSpriteSheet.KATANA, ItemSpriteSheet.GREATSWORD, ItemSpriteSheet.WAR_HAMMER, ItemSpriteSheet.GLAIVE, ItemSpriteSheet.GREATAXE, ItemSpriteSheet.GREATSHIELD, ItemSpriteSheet.GAUNTLETS, ItemSpriteSheet.WAR_SCYTHE, ItemSpriteSheet.RIFT_DAGGER, ItemSpriteSheet.KNIGHT_LONGSWORD
	};

	@Override
	public int image() {
		if (formClass != null){
			MeleeWeapon f = form();
			if (f != null){
				for (int i = 0; i < FORM_IMAGES.length; i++){
					if (FORM_IMAGES[i] == f.image) return ItemSpriteSheet.ETHER_FORM_FIRST + i;
				}
			}
		}
		return image;
	}

	//빚은 모양의 성능을 그대로 따름 (강화 수치는 점토의 것)
	private MeleeWeapon form(){
		if (form == null && formClass != null){
			form = Reflection.newInstance( formClass );
		}
		if (form != null){
			form.level( level() );
			form.augment = augment;
		}
		return form;
	}

	private void applyForm(){
		MeleeWeapon f = form();
		if (f != null){
			tier = f.tier;
			ACC = f.ACC;
			DLY = f.DLY;
			RCH = f.RCH;
		} else {
			tier = 1;
			ACC = 1f; DLY = 1f; RCH = 1;
		}
	}

	public void shape( Class<? extends MeleeWeapon> cls ){
		formClass = cls;
		form = null;
		applyForm();
		updateQuickslot();
	}

	@Override
	public int min( int lvl ) {
		MeleeWeapon f = form();
		return f != null ? f.min( lvl ) : super.min( lvl );
	}

	@Override
	public int max( int lvl ) {
		MeleeWeapon f = form();
		return f != null ? f.max( lvl ) : super.max( lvl );
	}

	@Override
	public int STRReq( int lvl ) {
		MeleeWeapon f = form();
		int req = f != null ? f.STRReq( lvl ) : STRReq( tier, lvl );
		if (masteryPotionBonus) req -= 2;
		//가벼운 형상: 1/2/3 낮춤
		if (Dungeon.hero != null) req -= Dungeon.hero.pointsInTalent( Talent.LIGHT_FORM );
		return req;
	}

	//지금 빚은 모양 (강화 수치는 점토와 같게 맞춰 둠)
	public MeleeWeapon currentForm(){
		return formClass != null ? form() : null;
	}

	//기습 공격 피해: 단검 75%, 도검 67%, 암살자의 칼날 50%부터 최대치까지
	private float surpriseShift(){
		MeleeWeapon f = currentForm();
		if (f instanceof AssassinsBlade) return 0.50f;
		if (f instanceof Dirk) return 0.67f;
		if (f instanceof Dagger) return 0.75f;
		return 0f;
	}

	@Override
	public int damageRoll( Char owner ) {
		float shift = surpriseShift();
		if (shift > 0 && owner instanceof Hero){
			Hero hero = (Hero) owner;
			Char enemy = hero.attackTarget();
			if (enemy instanceof Mob && ((Mob) enemy).surprisedBy( hero )){
				int diff = max() - min();
				int damage = augment.damageFactor( Hero.heroDamageIntRange( min() + Math.round( diff*shift ), max() ) );
				int exStr = hero.STR() - STRReq();
				if (exStr > 0) damage += Hero.heroDamageIntRange( 0, exStr );
				return damage;
			}
		}
		return super.damageRoll( owner );
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		MeleeWeapon f = currentForm();
		//형상 기사: 빚은 무기 종류마다 다른 타격 효과
		if (attacker instanceof Hero){
			damage = EtherForms.proc( (Hero) attacker, defender, damage, f, tier );
		}
		if (f instanceof KnightLongsword){
			damage = ((KnightLongsword) f).comboHit( attacker, defender, damage, false );
		} else if (f instanceof RiftDagger && defender.sprite != null && defender.sprite.visible){
			com.shatteredpixel.shatteredpixeldungeon.effects.RiftSlash.at( defender.sprite, 0.8f );
			defender.sprite.emitter().burst( com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle.BURST, 4 );
		}
		return super.proc( attacker, defender, damage );
	}

	//지금 모양의 특징 (정확도·속도·사거리·기습·막기 등)
	private String formTraits(){
		MeleeWeapon f = currentForm();
		if (f == null) return "";
		ArrayList<String> t = new ArrayList<>();
		if (ACC > 1.001f) t.add( Messages.get( EtherClay.class, "tr_acc_up", Math.round( (ACC - 1f)*100 ) ) );
		if (ACC < 0.999f) t.add( Messages.get( EtherClay.class, "tr_acc_down", Math.round( (1f - ACC)*100 ) ) );
		if (DLY < 0.999f) t.add( Messages.get( EtherClay.class, "tr_fast", Messages.decimalFormat( "#.##", 1f/DLY ) ) );
		if (DLY > 1.001f) t.add( Messages.get( EtherClay.class, "tr_slow", Messages.decimalFormat( "#.##", DLY ) ) );
		if (RCH > 1) t.add( Messages.get( EtherClay.class, "tr_reach", RCH ) );
		float sh = surpriseShift();
		if (sh > 0) t.add( Messages.get( EtherClay.class, "tr_surprise", Math.round( sh*100 ) ) );
		int block = defenseFactor( Dungeon.hero );
		if (block > 0) t.add( Messages.get( EtherClay.class, "tr_block", block ) );
		if (f instanceof Crossbow) t.add( Messages.get( EtherClay.class, "tr_darts" ) );
		if (f instanceof RiftDagger) t.add( Messages.get( EtherClay.class, "tr_pierce", Math.round( ((RiftDagger) f).armorPierce()*100 ) ) );
		if (f instanceof KnightLongsword) t.add( Messages.get( EtherClay.class, "tr_combo" ) );
		if (t.isEmpty()) return "";
		StringBuilder sb = new StringBuilder( Messages.get( EtherClay.class, "tr_title" ) );
		for (String l : t) sb.append( "\n- " ).append( l );
		return sb.toString();
	}

	@Override
	public int defenseFactor( Char owner ) {
		MeleeWeapon f = form();
		return f != null ? f.defenseFactor( owner ) : super.defenseFactor( owner );
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_SHAPE );
		if (hero.hasTalent( Talent.WEAPON_ABSORB )) actions.add( AC_ABSORB );
		if (hero.hasTalent( Talent.KNIGHTS_SALUTE )) actions.add( AC_SALUTE );
		return actions;
	}

	@Override
	public String actionName( String action, Hero hero ) {
		if (action.equals( AC_SHAPE )) return Messages.get( this, "ac_shape" );
		if (action.equals( AC_ABSORB )) return Messages.get( this, "ac_absorb" );
		if (action.equals( AC_SALUTE )) return Messages.get( this, "ac_salute" );
		return super.actionName( action, hero );
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_SHAPE )){
			chooseTier( hero );
		} else if (action.equals( AC_ABSORB )){
			absorb( hero );
		} else if (action.equals( AC_SALUTE )){
			salute( hero );
		}
	}

	//퀵슬롯에 넣어 한 번 눌러 바로 빚을 수 있게: 기본 행동 = 빚기
	@Override
	public String defaultAction() {
		return AC_SHAPE;
	}

	//무기 흡수: 가방의 근접 무기를 녹여 그 무기의 마법 부여를 점토로 옮김
	private void absorb( final Hero hero ){
		final EtherClay clay = this;
		GameScene.selectItem( new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get( EtherClay.class, "absorb_prompt" );
			}
			@Override
			public boolean itemSelectable( Item item ) {
				return item instanceof MeleeWeapon && !(item instanceof EtherClay)
						&& !item.unique && !item.isEquipped( hero );
			}
			@Override
			public void onSelect( Item item ) {
				if (!(item instanceof MeleeWeapon) || !itemSelectable( item )) return;
				confirmAbsorb( hero, (MeleeWeapon) item );
			}
		} );
	}

	//흡수로 오르는 강화 수치: 마법사의 지팡이에 마법 막대를 융합할 때와 같은 식, 단 흡수로는 +3까지만
	public static final int ABSORB_CAP = 3;
	public int absorbLevel( MeleeWeapon w ){
		int mine = trueLevel();
		int target = Math.max( mine, w.trueLevel() );
		if (w.trueLevel() >= mine && mine > 0) target++;
		if (target > mine) target = Math.min( target, Math.max( mine, ABSORB_CAP ) );
		return target;
	}

	private void confirmAbsorb( final Hero hero, final MeleeWeapon w ){
		final int newLvl = absorbLevel( w );
		final boolean ench = w.enchantment != null;
		//얻을 게 하나도 없으면 녹이지 않음
		if (!ench && newLvl <= trueLevel()){
			w.cursedKnown = true;
			GLog.w( Messages.get( EtherClay.class, "absorb_nothing", Messages.titleCase( w.trueName() ) ) );
			return;
		}
		String body = Messages.get( EtherClay.class, "absorb_desc" );
		if (w.levelKnown){
			body += "\n\n" + Messages.get( EtherClay.class, "absorb_level", newLvl );
		} else {
			body += "\n\n" + Messages.get( EtherClay.class, "absorb_unknown" );
		}
		if (trueLevel() >= ABSORB_CAP) body += "\n\n" + Messages.get( EtherClay.class, "absorb_cap" );
		GameScene.show( new WndOptions( new ItemSprite( w ),
				Messages.titleCase( w.name() ), body,
				Messages.get( EtherClay.class, "absorb_yes" ), Messages.get( EtherClay.class, "absorb_no" ) ){
			@Override
			protected void onSelect( int index ) {
				if (index == 0) doAbsorb( hero, w );
			}
		} );
	}

	private void doAbsorb( Hero hero, MeleeWeapon w ){
		boolean purify = hero.pointsInTalent( Talent.WEAPON_ABSORB ) >= 2;
		int oldLvl = trueLevel();
		int newLvl = absorbLevel( w );
		w.detach( hero.belongings.backpack );

		if (newLvl > oldLvl){
			level( newLvl );
			GLog.p( Messages.get( EtherClay.class, "absorb_upgraded", newLvl ) );
		}

		if (w.enchantment != null){
			boolean curse = w.enchantment.curse();
			if (curse && purify){
				GLog.i( Messages.get( EtherClay.class, "absorb_purified" ) );
			} else {
				enchant( w.enchantment );
				if (curse){
					cursed = true;
					cursedKnown = true;
					GLog.n( Messages.get( EtherClay.class, "absorb_cursed" ) );
				} else {
					GLog.p( Messages.get( EtherClay.class, "absorb_done", w.enchantment.name() ) );
				}
			}
		}
		if (w.cursed && !purify && !cursed){
			cursed = true;
			cursedKnown = true;
			GLog.n( Messages.get( EtherClay.class, "absorb_cursed" ) );
		}
		levelKnown = true;

		if (hero.sprite != null){
			hero.sprite.operate( hero.pos );
			hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 8 );
		}
		Sample.INSTANCE.play( Assets.Sounds.MELD );
		updateQuickslot();
		hero.spendAndNext( 1f );
	}

	//기사의 경례: 적이 보이지 않을 때만. 다음 첫 공격이 강해짐
	private void salute( Hero hero ){
		if (hero.visibleEnemies() > 0){
			GLog.w( Messages.get( EtherClay.class, "salute_enemies" ) );
			return;
		}
		Buff.affect( hero, Talent.KnightsSaluteBuff.class );
		if (hero.sprite instanceof HeroSprite){
			((HeroSprite) hero.sprite).playSpecial( 1 );
		}
		hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
		GLog.p( Messages.get( EtherClay.class, "saluted" ) );
		hero.spendAndNext( 1f );
	}

	//능숙한 손놀림: 1 / 0.5 / 0턴
	public static float shapeTime( Hero hero ){
		switch (hero.pointsInTalent( Talent.DEFT_HANDS )){
			case 0: default: return 1f;
			case 1: return 0.5f;
			case 2: return 0f;
		}
	}

	private void chooseTier( final Hero hero ){
		final int max = maxTier( hero );
		if (max <= 1){
			chooseForm( hero, 1 );
			return;
		}
		String[] opts = new String[max];
		for (int t = 1; t <= max; t++) opts[max - t] = Messages.get( this, "tier", t );
		GameScene.show( new WndOptions( new ItemSprite( this ),
				Messages.get( this, "shape_title" ),
				Messages.get( this, "shape_tier", max ), opts ){
			@Override
			protected void onSelect( int index ) {
				chooseForm( hero, max - index );
			}
		} );
	}

	private void chooseForm( final Hero hero, final int tier ){
		final ArrayList<Class<? extends MeleeWeapon>> forms = formsOf( tier );
		String[] opts = new String[forms.size()];
		for (int i = 0; i < forms.size(); i++){
			MeleeWeapon w = Reflection.newInstance( forms.get( i ) );
			opts[i] = Messages.titleCase( w.trueName() ) + (forms.get( i ) == formClass ? " " + Messages.get( EtherClay.class, "current" ) : "");
		}
		GameScene.show( new WndOptions( new ItemSprite( this ),
				Messages.get( this, "shape_title" ),
				Messages.get( this, "shape_form", tier ), opts ){
			@Override
			protected void onSelect( int index ) {
				Class<? extends MeleeWeapon> cls = forms.get( index );
				if (cls == formClass) return;
				shape( cls );
				MeleeWeapon w = Reflection.newInstance( cls );
				GLog.p( Messages.get( EtherClay.class, "shaped", Messages.titleCase( w.trueName() ) ) );
				if (hero.sprite != null){
					hero.sprite.operate( hero.pos );
					hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 6 );
				}
				Sample.INSTANCE.play( Assets.Sounds.MELD );
				//형태 적응: 4/6턴 동안 정확도·회피 상승
				if (hero.hasTalent( Talent.FORM_ADAPTATION )){
					Buff.prolong( hero, Talent.FormAdaptation.class, 2 + 2*hero.pointsInTalent( Talent.FORM_ADAPTATION ) );
				}
				EtherForms.onReshape( hero );
				hero.spendAndNext( shapeTime( hero ) );
			}
		} );
	}

	//영웅이 레벨업할 때: 새 티어에 닿으면 알림
	public static void onHeroLevelUp( Hero hero ){
		int pts = hero.pointsInTalent( Talent.ETHER_TEMPERING );
		if (maxTier( hero.lvl, pts ) <= maxTier( hero.lvl - 1, pts )) return;
		announceTier( hero );
	}

	//에테르 단련을 찍어 다룰 수 있는 티어가 오르면 알림
	public static void onTemperingUpgraded( Hero hero ){
		int pts = hero.pointsInTalent( Talent.ETHER_TEMPERING );
		if (maxTier( hero.lvl, pts ) <= maxTier( hero.lvl, pts - 1 )) return;
		announceTier( hero );
	}

	private static void announceTier( Hero hero ){
		EtherClay clay = hero.belongings.getItem( EtherClay.class );
		if (clay == null) return;
		GLog.p( Messages.get( EtherClay.class, "tier_up", maxTier( hero ) ) );
		if (hero.sprite != null) hero.sprite.showStatus( CharSprite.POSITIVE, Messages.get( EtherClay.class, "tier_up_short" ) );
	}

	@Override
	public String name() {
		String n = super.name();
		if (formClass != null){
			MeleeWeapon f = form();
			n = Messages.get( this, "name_form", n, Messages.titleCase( f.trueName() ) );
		}
		return n;
	}

	@Override
	public String info() {
		String info = super.info();
		info += "\n\n" + Messages.get( this, "growth", maxTier( Dungeon.hero ) );
		if (formClass == null) info += " " + Messages.get( this, "unshaped" );
		else info += " " + Messages.get( this, "current_form", Messages.titleCase( form().trueName() ), tier );
		String tr = formTraits();
		if (!tr.isEmpty()) info += "\n\n" + tr;
		if (Dungeon.hero != null && Dungeon.hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.FORM_KNIGHT){
			info += "\n\n" + EtherForms.describe( Dungeon.hero, currentForm() );
		}
		return info;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		ItemSprite.Glowing g = super.glowing();
		return g != null ? g : new ItemSprite.Glowing( 0xB8A4FF, 2f );
	}

	@Override
	public int value() {
		return 0;
	}

	private static final String FORM = "form";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if (formClass != null) bundle.put( FORM, formClass );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		formClass = null; form = null;
		if (bundle.contains( FORM )){
			try {
				Class<?> c = bundle.getClass( FORM );
				if (c != null && MeleeWeapon.class.isAssignableFrom( c )) formClass = (Class<? extends MeleeWeapon>) c;
			} catch (Exception ignored) {}
		}
		applyForm();
	}

	@Override
	public Item identify( boolean byHero ) {
		applyForm();
		return super.identify( byHero );
	}
}
