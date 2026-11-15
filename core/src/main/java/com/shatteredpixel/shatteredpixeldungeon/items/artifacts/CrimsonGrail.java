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
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

//뱀파이어 전용 성배. 충전 = 적에게서 빼앗은 생명력.
// - 저절로 차지 않고, 적을 쓰러뜨릴 때만 참. 생명체가 아닌 적(언데드·무기물)에게서는 1/3만 얻음
// - 생명력을 써서 기술을 씀. 쓴 생명력이 쌓이면 성배가 강해짐
public class CrimsonGrail extends Artifact {

	public static final String AC_SKILL = "SKILL";

	{
		image = ItemSpriteSheet.CRIMSON_GRAIL;

		exp = 0;
		levelCap = 10;

		charge = 5;
		partialCharge = 0;
		chargeCap = 20;

		defaultAction = AC_SKILL;

		unique = true;
		bones = false;
	}

	//기술: 이름, 드는 생명력
	public enum Skill {
		DRAIN( 4 ), MIST( 6 ), MEND( 8 );

		public final int cost;
		Skill( int cost ){ this.cost = cost; }

		public String title(){ return Messages.get( CrimsonGrail.class, "skill_" + name().toLowerCase() ); }
		public String desc(){ return Messages.get( CrimsonGrail.class, "skill_" + name().toLowerCase() + "_desc" ); }
	}

	public int vitality(){
		return charge;
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

		final Skill[] skills = Skill.values();
		String[] opts = new String[skills.length];
		for (int i = 0; i < skills.length; i++){
			opts[i] = Messages.get( this, "skill_option", skills[i].title(), skills[i].cost );
		}
		GameScene.show( new WndOptions( new ItemSprite( this ), Messages.titleCase( name() ),
				Messages.get( this, "skill_prompt", charge, chargeCap ), opts ){
			@Override
			protected boolean enabled( int index ) {
				return charge >= skills[index].cost;
			}

			@Override
			protected void onSelect( int index ) {
				use( skills[index] );
			}

			@Override
			protected boolean hasInfo( int index ) {
				return true;
			}

			@Override
			protected void onInfo( int index ) {
				GameScene.show( new com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage(
						new ItemSprite( CrimsonGrail.this ), skills[index].title(), skills[index].desc() ) );
			}
		} );
	}

	private void use( final Skill skill ){
		final Hero hero = Dungeon.hero;
		switch (skill){
			case DRAIN:
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

	//생명력을 쓰고, 쓴 만큼 성배가 자람 (20을 쓸 때마다 한 단계, 최대치 +2)
	private void spend( int cost ){
		charge = Math.max( 0, charge - cost );
		exp += cost;
		while (level() < levelCap && exp >= 20){
			exp -= 20;
			upgrade();
			GLog.p( Messages.get( this, "levelup" ) );
		}
		updateQuickslot();
	}

	@Override
	public Artifact upgrade() {
		chargeCap = Math.min( chargeCap + 2, 40 );
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

		//최대 체력의 15% (1~8). 생명체가 아니면 그 1/3, 소수점은 조금씩 모아 둠
		float gain = Math.max( 1f, Math.min( 8f, mob.HT * 0.15f ) );
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

	public class Vessel extends ArtifactBuff {
		@Override
		public boolean act() {
			spend( TICK );
			return true;
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
	}
}
