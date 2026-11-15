/*
 * Shattered Pixel Dungeon 모드: 뱀파이어의 핏빛 성배
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGrailSkills;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

//뱀파이어 전용 성배. 충전 = 적에게서 빼앗은 생명력.
// - 저절로 차지 않고, 적을 쓰러뜨릴 때만 참. 생명체가 아닌 적(언데드·무기물)에게서는 1/3만 얻음
// - 성직자의 성서처럼 기술 창(WndGrailSkills)에서 기술을 골라 씀. 길게 누르면 빠른 사용에 등록
// - 쓴 생명력이 쌓이면 성배가 강해짐
public class CrimsonGrail extends Artifact {

	public static final String AC_SKILL = "SKILL";

	{
		image = ItemSpriteSheet.CRIMSON_GRAIL;

		exp = 0;
		levelCap = 10;

		charge = 8;
		partialCharge = 0;
		chargeCap = 30;

		defaultAction = AC_SKILL;

		unique = true;
		bones = false;
	}

	//기술: 이름, 드는 생명력
	public enum Skill {
		DRAIN( 4, 112 ), MIST( 6, 113 ), MEND( 8, 114 );

		public final int cost;
		public final int icon;      //hero_icons 칸 번호 (+ICON_ACTION_OFFSET: 배경 없는 행동 표시용)
		Skill( int cost, int icon ){ this.cost = cost; this.icon = icon; }

		public String title(){ return Messages.get( CrimsonGrail.class, "skill_" + name().toLowerCase() ); }
		public String desc(){ return Messages.get( CrimsonGrail.class, "skill_" + name().toLowerCase() + "_desc" ); }
	}

	public static final int ICON_ACTION_OFFSET = 8;

	public int vitality(){
		return charge;
	}

	public int lifeCap(){
		return chargeCap;
	}

	public boolean canUse( Hero hero, Skill skill ){
		return isEquipped( hero ) && !cursed && hero.buff( MagicImmune.class ) == null && charge >= skill.cost;
	}

	//빠른 사용: 기술 창에서 길게 눌러 등록하면 화면의 행동 버튼으로 바로 씀 (같은 기술을 다시 등록하면 해제)
	private Skill quickSkill = null;

	public Skill quickSkill(){ return quickSkill; }

	public void setQuickSkill( Skill skill ){
		if (quickSkill == skill){
			quickSkill = null;
			if (passiveBuff != null) ActionIndicator.clearAction( (ActionIndicator.Action) passiveBuff );
		} else {
			quickSkill = skill;
			if (passiveBuff != null) ActionIndicator.setAction( (ActionIndicator.Action) passiveBuff );
		}
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && !cursed && hero.buff( MagicImmune.class ) == null){
			actions.add( AC_SKILL );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (!action.equals( AC_SKILL ) || hero.buff( MagicImmune.class ) != null) return;

		if (!isEquipped( hero )){
			GLog.i( Messages.get( Artifact.class, "need_to_equip" ) );
			return;
		}
		if (cursed){
			GLog.w( Messages.get( this, "cursed" ) );
			return;
		}

		GameScene.show( new WndGrailSkills( this, hero, false ) );
	}

	public void use( final Skill skill ){
		final Hero hero = Dungeon.hero;
		switch (skill){
			case DRAIN:
				if (Dungeon.quickslot.contains( this )){
					QuickSlotButton.useTargeting( Dungeon.quickslot.getSlot( this ) );
				}
				GameScene.selectCell( new CellSelector.Listener() {
					@Override
					public void onSelect( Integer cell ) {
						if (cell == null) return;
						Char ch = Actor.findChar( cell );
						if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY
								|| !Dungeon.level.adjacent( hero.pos, cell )){
							GLog.w( Messages.get( CrimsonGrail.class, "drain_invalid" ) );
							return;
						}
						spend( skill.cost );
						int dmg = Random.NormalIntRange( 2 + hero.lvl / 2, 5 + hero.lvl );
						hero.sprite.attack( cell );
						ch.damage( dmg, CrimsonGrail.this );
						if (ch.sprite != null) ch.sprite.emitter().burst( BloodParticle.BURST, 8 );
						int heal = Math.max( 1, Math.round( dmg * (isLiving( ch ) ? 0.5f : 0.2f) ) );
						heal( hero, heal );
						Sample.INSTANCE.play( Assets.Sounds.DRINK, 0.6f, 0.8f );
						hero.spendAndNext( 1f );
					}

					@Override
					public String prompt() {
						return Messages.get( CrimsonGrail.class, "drain_prompt" );
					}
				} );
				break;

			case MIST:
				GameScene.selectCell( new CellSelector.Listener() {
					@Override
					public void onSelect( Integer cell ) {
						if (cell == null) return;
						if (!Dungeon.level.heroFOV[cell] || Dungeon.level.solid[cell] || Actor.findChar( cell ) != null
								|| Dungeon.level.distance( hero.pos, cell ) > 4
								|| new Ballistica( hero.pos, cell, Ballistica.PROJECTILE ).collisionPos != (int) cell){
							GLog.w( Messages.get( CrimsonGrail.class, "mist_invalid" ) );
							return;
						}
						spend( skill.cost );
						hero.sprite.emitter().burst( Speck.factory( Speck.SMOKE ), 6 );
						ScrollOfTeleportation.appear( hero, cell );
						Dungeon.level.occupyCell( hero );
						Dungeon.observe();
						GameScene.updateFog();
						hero.sprite.emitter().burst( BloodParticle.BURST, 6 );
						hero.spendAndNext( 1f );
					}

					@Override
					public String prompt() {
						return Messages.get( CrimsonGrail.class, "mist_prompt" );
					}
				} );
				break;

			case MEND:
				spend( skill.cost );
				heal( hero, Math.max( 1, Math.round( hero.HT * 0.25f ) ) );
				hero.sprite.emitter().burst( BloodParticle.BURST, 10 );
				Sample.INSTANCE.play( Assets.Sounds.DRINK );
				hero.sprite.operate( hero.pos );
				hero.spendAndNext( 1f );
				break;
		}
	}

	private static void heal( Hero hero, int amount ){
		amount = Math.min( amount, hero.HT - hero.HP );
		if (amount <= 0) return;
		hero.HP += amount;
		hero.sprite.showStatusWithIcon( com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE,
				Integer.toString( amount ), com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText.HEALING );
	}

	//생명력을 쓰고, 쓴 만큼 성배가 자람 (30을 쓸 때마다 한 단계, 최대치 +3)
	private void spend( int cost ){
		charge = Math.max( 0, charge - cost );
		exp += cost;
		while (level() < levelCap && exp >= 30){
			exp -= 30;
			upgrade();
			GLog.p( Messages.get( this, "levelup" ) );
		}
		updateQuickslot();
	}

	@Override
	public Artifact upgrade() {
		chargeCap = Math.min( chargeCap + 3, 60 );
		return (Artifact) super.upgrade();
	}

	//살아 있는 생명체인지: 언데드와 무기물은 생명력이 거의 없음
	public static boolean isLiving( Char ch ){
		return !Char.hasProp( ch, Char.Property.UNDEAD ) && !Char.hasProp( ch, Char.Property.INORGANIC );
	}

	//적이 쓰러질 때 (Mob.destroy에서 불림)
	public static void onKill( Mob mob ){
		Hero hero = Dungeon.hero;
		if (hero == null || hero.heroClass != HeroClass.VAMPIRE || !hero.isAlive()) return;
		CrimsonGrail g = hero.belongings.getItem( CrimsonGrail.class );
		if (g == null || !g.isEquipped( hero ) || g.cursed) return;

		//최대 체력의 30% (2~16). 생명체가 아니면 그 1/3, 소수점은 조금씩 모아 둠
		float gain = Math.max( 2f, Math.min( 16f, mob.HT * 0.3f ) );
		if (!isLiving( mob )) gain /= 3f;
		g.partialCharge += gain;
		int whole = (int) g.partialCharge;
		g.partialCharge -= whole;
		if (whole > 0 && g.charge < g.chargeCap){
			g.charge = Math.min( g.chargeCap, g.charge + whole );
			if (mob.sprite != null && isLiving( mob )) mob.sprite.emitter().burst( BloodParticle.BURST, 6 );
			updateQuickslot();
		}
	}

	@Override
	public void charge( Hero target, float amount ) {
		//다른 충전 효과로는 차지 않음 (생명력은 빼앗아야만 얻음)
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new Vessel();
	}

	public class Vessel extends ArtifactBuff implements ActionIndicator.Action {

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )){
				if (quickSkill != null) ActionIndicator.setAction( this );
				return true;
			}
			return false;
		}

		@Override
		public void detach() {
			super.detach();
			ActionIndicator.clearAction( this );
		}

		@Override
		public boolean act() {
			spend( TICK );
			return true;
		}

		@Override
		public String actionName() {
			return quickSkill.title();
		}

		@Override
		public int actionIcon() {
			return quickSkill.icon + ICON_ACTION_OFFSET;
		}

		@Override
		public int indicatorColor() {
			return 0x5A0A1E;
		}

		@Override
		public void doAction() {
			if (quickSkill == null) return;
			if (cursed){
				GLog.w( Messages.get( CrimsonGrail.this, "cursed" ) );
				return;
			}
			if (!canUse( Dungeon.hero, quickSkill )){
				GLog.w( Messages.get( CrimsonGrail.this, "no_skill" ) );
				return;
			}
			use( quickSkill );
		}
	}

	@Override
	public String desc() {
		String d = Messages.get( this, "desc" );
		if (isEquipped( Dungeon.hero )){
			d += "\n\n" + (cursed ? Messages.get( this, "desc_cursed" ) : Messages.get( this, "desc_stats", charge, chargeCap ));
		}
		return d;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if (quickSkill != null) bundle.put( "quick_skill", quickSkill.name() );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		quickSkill = null;
		if (bundle.contains( "quick_skill" )){
			try { quickSkill = Skill.valueOf( bundle.getString( "quick_skill" ) ); } catch (Exception e) { quickSkill = null; }
		}
	}
}
