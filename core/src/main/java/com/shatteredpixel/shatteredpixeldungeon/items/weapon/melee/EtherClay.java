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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
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

	//영웅 레벨로 다룰 수 있는 최고 티어: 5레벨마다 1티어씩
	public static int maxTier( Hero hero ){
		int lvl = hero != null ? hero.lvl : 1;
		return Math.min( MAX_TIER, 1 + lvl/5 );
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
		return req;
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
		return actions;
	}

	@Override
	public String actionName( String action, Hero hero ) {
		if (action.equals( AC_SHAPE )) return Messages.get( this, "ac_shape" );
		return super.actionName( action, hero );
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_SHAPE )){
			chooseTier( hero );
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
				hero.spendAndNext( 1f );
			}
		} );
	}

	//영웅이 레벨업할 때: 새 티어에 닿으면 알림
	public static void onHeroLevelUp( Hero hero ){
		if (hero.lvl % 5 != 0 || hero.lvl/5 + 1 > MAX_TIER) return;
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
