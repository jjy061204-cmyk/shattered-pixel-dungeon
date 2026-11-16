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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BloodBlade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BloodFlight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BloodWings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.VampireFX;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGrailSkills;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

//뱀파이어 전용 성배. 충전 = 생명력.
// - 적을 쓰러뜨리면 크게 차고(생명체가 아니면 1/3), 흡혈로도 조금씩 차며, 시간이 지나면 아주 느리게 저절로도 참
// - 성직자의 성서처럼 기술 창(WndGrailSkills)에서 기술을 골라 씀. 길게 누르면 빠른 사용에 등록
// - 기술은 처음부터 있는 것, 특성을 찍어야 생기는 것, 전직하면 생기는 것으로 나뉨
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

	//기술: 드는 생명력, 창에서 놓이는 줄(0 기본, 1~3 특성 단계·전직), 필요한 특성·전직
	public enum Skill {
		//처음부터
		DRAIN( 0, 0, null, null ),
		SLASH( 4, 0, null, null ),
		MIST( 6, 0, null, null ),
		MEND( 8, 0, null, null ),
		//1단계 특성
		CHARM( 3, 1, Talent.ENTHRALLING_GAZE, null ),
		SWARM( 3, 1, Talent.BAT_SWARM, null ),
		//2단계 특성
		LANCE( 4, 2, Talent.BLOOD_LANCE, null ),
		VEIL( 5, 2, Talent.CRIMSON_VEIL, null ),
		DREAD( 5, 2, Talent.DREADFUL_EYES, null ),
		//3단계 특성
		NOVA( 10, 3, Talent.BLOOD_NOVA, null ),
		//진조 흡혈귀
		FEAST( 6, 3, null, HeroSubClass.TRUE_ANCESTOR ),
		DIVE( 7, 3, null, HeroSubClass.TRUE_ANCESTOR ),
		MASS_DRAIN( 8, 3, Talent.MASS_DRAIN, null ),
		//피의 지배자
		BLADE( 5, 3, null, HeroSubClass.BLOOD_LORD ),
		INFUSE( 6, 3, null, HeroSubClass.BLOOD_LORD ),
		CHAINS( 6, 3, Talent.BLOOD_CHAINS, null );

		public final int baseCost;
		public final int row;
		public final Talent talent;
		public final HeroSubClass subClass;

		Skill( int cost, int row, Talent talent, HeroSubClass subClass ){
			this.baseCost = cost; this.row = row; this.talent = talent; this.subClass = subClass;
		}

		//hero_icons 칸 번호 (+ICON_ACTION_OFFSET: 배경 없는 행동 표시용)
		public int icon(){ return ICON_BASE + ordinal(); }

		public boolean available( Hero hero ){
			if (talent != null) return hero.hasTalent( talent );
			if (subClass != null) return hero.subClass == subClass;
			return true;
		}

		//피의 지배자의 '절제된 잔': 소모량 10/20/30% 감소 (최소 1)
		public int cost( Hero hero ){
			if (baseCost == 0) return 0;
			int pts = hero.pointsInTalent( Talent.TEMPERED_CUP );
			//진조의 왕관을 쓰는 동안 절반
			float mult = (1f - 0.1f * pts) * com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire.CrimsonCrown.costMultiplier( hero );
			return Math.max( 1, Math.round( baseCost * mult ) );
		}

		public String title(){ return Messages.get( CrimsonGrail.class, "skill_" + name().toLowerCase() ); }
		public String desc(){ return Messages.get( CrimsonGrail.class, "skill_" + name().toLowerCase() + "_desc" ); }
	}

	public static final int ICON_BASE = 128;
	public static final int ICON_ACTION_OFFSET = 32;

	public static ArrayList<Skill> skillList( Hero hero, int row ){
		ArrayList<Skill> list = new ArrayList<>();
		for (Skill s : Skill.values()){
			if (s.row == row && s.available( hero )) list.add( s );
		}
		return list;
	}

	public int vitality(){
		return charge;
	}

	//최대치: 성배 단계 + 피의 지배자의 '넘치는 잔'(+5/10/15)
	public int lifeCap(){
		int cap = chargeCap;
		if (Dungeon.hero != null){
			cap += 5 * Dungeon.hero.pointsInTalent( Talent.OVERFLOWING_CUP );
			//피의 성배를 함께 장착하면 3 + 피의 성배 레벨만큼 더 담음 (+0: 3, +10: 13)
			ChaliceOfBlood c = chalice( Dungeon.hero );
			if (c != null) cap += 3 + c.level();
		}
		return cap;
	}

	//함께 장착한, 저주받지 않은 피의 성배
	public static ChaliceOfBlood chalice( Hero hero ){
		if (hero == null) return null;
		ChaliceOfBlood c = hero.belongings.getItem( ChaliceOfBlood.class );
		return (c != null && c.isEquipped( hero ) && !c.cursed) ? c : null;
	}

	public boolean canUse( Hero hero, Skill skill ){
		return isEquipped( hero ) && !cursed && hero.buff( MagicImmune.class ) == null
				&& skill.available( hero ) && charge >= skill.cost( hero );
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

	//------------------------------------------------------------------ 기술 쓰기

	private interface OnTarget { void hit( Char ch, int cell ); }

	//------------------------------------------------------------------ 조준
	// 성직자의 성서처럼: 대상을 고르는 기술을 쓰면 가까운 적에게 조준점이 뜨고,
	// 빠른 사용 버튼을 한 번 더 누르면 그 적에게 곧바로 씀 (다른 적은 화면에서 눌러 고를 수 있음)

	private Skill aimSkill = null;
	private Char aimTarget = null;
	private VampireFX.Mark aimMark = null;

	private void startAim( Skill skill, Char target ){
		endAim();
		if (target == null) return;
		aimSkill = skill;
		aimTarget = target;
		QuickSlotButton.target( target );
		//성배가 도구 모음에 있으면 원작처럼 그 칸에도 조준 표시가 뜨고, 성배 칸을 한 번 더 눌러도 씀
		if (Dungeon.quickslot.contains( this )){
			QuickSlotButton.useTargeting( Dungeon.quickslot.getSlot( this ) );
		} else {
			aimMark = VampireFX.Mark.on( target.sprite );
		}
	}

	//자동 조준이 노릴 칸: 피의 창만 날아가다 막히고, 나머지 기술은 고른 칸에 그대로 닿음
	@Override
	public int targetingPos( Hero user, int dst ) {
		if (aimSkill == Skill.LANCE) return new Ballistica( user.pos, dst, Ballistica.PROJECTILE ).collisionPos;
		return dst;
	}

	private void endAim(){
		aimSkill = null;
		aimTarget = null;
		if (aimMark != null) aimMark.killAndErase();
		aimMark = null;
	}

	private static boolean validFoe( Hero hero, Char ch ){
		return ch != null && ch != hero && ch.isAlive() && ch.alignment == Char.Alignment.ENEMY
				&& ch.invisible <= 0 && Dungeon.level.heroFOV[ch.pos]
				&& !Char.hasProp( ch, Char.Property.OBJECT );
	}

	//바로 옆의 적 가운데 하나 (마지막으로 노린 적이 옆에 있으면 그 적)
	private static Char nearAdjacent( Hero hero ){
		Char last = QuickSlotButton.lastTarget;
		if (validFoe( hero, last ) && Dungeon.level.adjacent( hero.pos, last.pos )) return last;
		Char best = null;
		for (int n : PathFinder.NEIGHBOURS8){
			Char ch = Actor.findChar( hero.pos + n );
			if (validFoe( hero, ch ) && (best == null || ch.HP < best.HP)) best = ch;
		}
		return best;
	}

	//거리 안의 보이는 적 가운데 가장 가까운 것 (마지막으로 노린 적이 거리 안이면 그 적)
	private static Char nearEnemy( Hero hero, int range ){
		Char last = QuickSlotButton.lastTarget;
		if (validFoe( hero, last ) && Dungeon.level.distance( hero.pos, last.pos ) <= range) return last;
		Char best = null;
		for (Mob mob : Dungeon.level.mobs){
			if (validFoe( hero, mob ) && Dungeon.level.distance( hero.pos, mob.pos ) <= range
					&& (best == null || Dungeon.level.distance( hero.pos, mob.pos ) < Dungeon.level.distance( hero.pos, best.pos ))){
				best = mob;
			}
		}
		return best;
	}

	//바로 옆의 적을 고름
	private void pickAdjacent( final Hero hero, final Skill skill, final OnTarget then ){
		Char suggest = nearAdjacent( hero );
		if (suggest == null){
			GLog.w( Messages.get( CrimsonGrail.class, "adjacent_none" ) );
			return;
		}
		GameScene.selectCell( new CellSelector.Listener() {
			@Override
			public void onSelect( Integer cell ) {
				endAim();
				if (cell == null) return;
				Char ch = Actor.findChar( cell );
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY
						|| !Dungeon.level.adjacent( hero.pos, cell )){
					GLog.w( Messages.get( CrimsonGrail.class, "adjacent_invalid" ) );
					return;
				}
				then.hit( ch, cell );
			}

			@Override
			public String prompt() {
				return Messages.get( CrimsonGrail.class, "adjacent_prompt", skill.title() );
			}
		} );
		startAim( skill, suggest );
	}

	//보이는 적을 고름 (거리 제한)
	private void pickEnemy( final Hero hero, final Skill skill, final int range, final OnTarget then ){
		Char suggest = nearEnemy( hero, range );
		GameScene.selectCell( new CellSelector.Listener() {
			@Override
			public void onSelect( Integer cell ) {
				endAim();
				if (cell == null) return;
				Char ch = Actor.findChar( cell );
				//피의 창은 비스듬히 노린 칸으로도 쏠 수 있음 (날아가다 처음 닿는 적)
				if (ch == null && skill == Skill.LANCE) ch = Actor.findChar( new Ballistica( hero.pos, cell, Ballistica.PROJECTILE ).collisionPos );
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY || !Dungeon.level.heroFOV[ch.pos]
						|| Dungeon.level.distance( hero.pos, ch.pos ) > range){
					GLog.w( Messages.get( CrimsonGrail.class, "enemy_invalid", range ) );
					return;
				}
				then.hit( ch, cell );
			}

			@Override
			public String prompt() {
				return Messages.get( CrimsonGrail.class, "enemy_prompt", skill.title() );
			}
		} );
		startAim( skill, suggest );
	}

	//기본 공격 한 번의 피해 (대상의 방어를 뺀 값)에 배율을 곱함
	private static int strike( Hero hero, Char enemy, float mult ){
		int roll = hero.damageRoll() - enemy.drRoll();
		return Math.max( 1, Math.round( Math.max( 0, roll ) * mult ) );
	}

	//흡혈 한 번의 회복량: 최대 체력의 3% (최소 1) + 날카로운 송곳니, 진조는 두 배. 생명체가 아니면 절반
	private static int drainHeal( Hero hero, Char enemy ){
		float heal = Math.max( 1f, hero.HT * 0.03f ) + hero.pointsInTalent( Talent.SHARP_FANGS );
		if (hero.subClass == HeroSubClass.TRUE_ANCESTOR) heal *= 2f;
		if (!isLiving( enemy )) heal *= 0.5f;
		return Math.max( 1, Math.round( heal ) );
	}

	//진조의 '피의 광란': 피를 빤 뒤 1/2/3턴 동안 아드레날린
	private static void frenzy( Hero hero ){
		int pts = hero.pointsInTalent( Talent.BLOOD_FRENZY );
		if (pts > 0) Buff.prolong( hero, Adrenaline.class, pts );
	}

	private int bite( Hero hero, Char ch, int cell, float mult ){
		int dmg = strike( hero, ch, mult );
		boolean living = isLiving( ch );
		int healing = drainHeal( hero, ch );
		VampireFX.on( ch.sprite, VampireFX.BITE, 0.55f, 0.42f );
		bloodStream( hero, ch );
		ch.damage( dmg, this );
		if (ch.sprite != null) ch.sprite.emitter().burst( BloodParticle.BURST, 6 );
		heal( hero, healing );
		gainLife( hero, living ? 1f : 0.34f );
		return dmg;
	}

	//적에게서 뱀파이어에게로 빨려 오는 핏줄기
	private static void bloodStream( Hero hero, Char from ){
		if (from.sprite == null || hero.sprite == null || hero.sprite.parent == null) return;
		PointF a = from.sprite.center(), b = hero.sprite.center();
		((MagicMissile) hero.sprite.parent.recycle( MagicMissile.class )).reset( MagicMissile.SHAMAN_RED, a, b, null );
	}

	public void use( final Skill skill ){
		final Hero hero = Dungeon.hero;
		if (!canUse( hero, skill )){
			GLog.w( Messages.get( this, "no_skill" ) );
			return;
		}
		final int lvl = hero.lvl;
		switch (skill){

			//--------------------------------------------- 처음부터 있는 기술
			case DRAIN:
				pickAdjacent( hero, skill, new OnTarget() {
					@Override
					public void hit( final Char ch, final int cell ) {
						Invisibility.dispel();
						hero.busy();
						hero.sprite.attack( cell, new Callback() {
							@Override
							public void call() {
								//기본 공격 피해의 80% (진조는 100%), 생명력 소모 없음. 살아 있는 적에게는 약한 출혈
								if (ch.isAlive()){
									int dmg = bite( hero, ch, cell, hero.subClass == HeroSubClass.TRUE_ANCESTOR ? 1f : 0.8f );
									if (ch.isAlive() && isLiving( ch )) Buff.affect( ch, Bleeding.class ).set( Math.max( 1f, dmg * 0.25f ) );
									Sample.INSTANCE.play( Assets.Sounds.DRINK, 0.6f, 0.8f );
									frenzy( hero );
								}
								finish( hero, hero.attackDelay() );
							}
						} );
					}
				} );
				break;

			case SLASH:
				//보이는 적을 향해 핏빛 검기를 날려, 5칸 안 직선 위의 적 모두에게 피해와 약한 출혈
				pickEnemy( hero, skill, 5, new OnTarget() {
					@Override
					public void hit( Char target, final int cell ) {
						spend( hero, skill );
						Invisibility.dispel();
						hero.busy();
						final Ballistica line = new Ballistica( hero.pos, cell, Ballistica.STOP_SOLID );
						final int reach = Math.max( 1, Math.min( line.dist, 5 ) );
						final int endCell = line.path.get( reach );
						hero.sprite.attack( cell, new Callback() {
							@Override
							public void call() {
								Sample.INSTANCE.play( Assets.Sounds.HIT_SLASH, 1f, 0.8f );
								VampireFX.shoot( VampireFX.Projectile.SLASH, hero.sprite.center(),
										com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.raisedTileCenterToWorld( endCell ), 0f, new Callback() {
									@Override
									public void call() {
										for (int c : line.subPath( 1, reach )){
											Char ch = Actor.findChar( c );
											if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) continue;
											int dmg = Random.NormalIntRange( 2 + lvl / 3, 5 + lvl * 2 / 3 );
											ch.damage( dmg, CrimsonGrail.this );
											VampireFX.at( c, VampireFX.SPLASH, 0.45f, 0.35f );
											if (ch.isAlive() && isLiving( ch )) Buff.affect( ch, Bleeding.class ).set( Math.max( 1f, dmg * 0.3f ) );
										}
										finish( hero, 1f );
									}
								} );
							}
						} );
					}
				} );
				break;

			case DIVE:
				//진조 흡혈귀: 6턴 동안 떠오른 뒤, 내려앉으며 주변 8칸에 큰 피해 (BloodFlight)
				spend( hero, skill );
				BloodWings.fly( hero, BloodFlight.class, BloodFlight.TURNS );
				VampireFX.on( hero.sprite, VampireFX.WINGS, 0.8f, 0.5f ).rise( 6f );
				hero.sprite.emitter().burst( BloodParticle.BURST, 8 );
				Sample.INSTANCE.play( Assets.Sounds.MISS, 1f, 0.7f );
				finish( hero, 1f );
				break;

			case MIST:
				if (hero.rooted){
					GLog.w( Messages.get( this, "rooted" ) );
					break;
				}
				final int mistRange = 4 + hero.pointsInTalent( Talent.MIST_BODY );
				GameScene.selectCell( new CellSelector.Listener() {
					@Override
					public void onSelect( Integer cell ) {
						if (cell == null) return;
						if (!Dungeon.level.heroFOV[cell] || !Dungeon.level.passable[cell] || Dungeon.level.pit[cell]
								|| Actor.findChar( cell ) != null
								|| Dungeon.level.distance( hero.pos, cell ) > mistRange
								|| new Ballistica( hero.pos, cell, Ballistica.PROJECTILE ).collisionPos != (int) cell){
							GLog.w( Messages.get( CrimsonGrail.class, "mist_invalid", mistRange ) );
							return;
						}
						spend( hero, skill );
						VampireFX.at( hero.pos, VampireFX.MIST, 0.75f, 0.45f );
						ScrollOfTeleportation.appear( hero, cell );
						Dungeon.level.occupyCell( hero );
						Dungeon.observe();
						GameScene.updateFog();
						VampireFX.on( hero.sprite, VampireFX.MIST, 0.75f, 0.4f ).reversed();
						Sample.INSTANCE.play( Assets.Sounds.PUFF );
						//안개의 몸: 옮겨 간 뒤 2/4턴 동안 투명
						int pts = hero.pointsInTalent( Talent.MIST_BODY );
						if (pts > 0) Buff.affect( hero, Invisibility.class, 2f * pts );
						finish( hero, 1f );
					}

					@Override
					public String prompt() {
						return Messages.get( CrimsonGrail.class, "mist_prompt" );
					}
				} );
				break;

			case MEND:
				spend( hero, skill );
				heal( hero, Math.max( 1, Math.round( hero.HT * 0.25f ) ) );
				VampireFX.on( hero.sprite, VampireFX.MEND, 0.7f, 0.6f );
				Sample.INSTANCE.play( Assets.Sounds.DRINK );
				hero.sprite.operate( hero.pos );
				finish( hero, 1f );
				break;

			//--------------------------------------------- 특성으로 얻는 기술
			case CHARM:
				pickEnemy( hero, skill, 5, new OnTarget() {
					@Override
					public void hit( Char ch, int cell ) {
						spend( hero, skill );
						Invisibility.dispel();
						//3/5턴 동안 매혹
						Buff.affect( ch, Charm.class, 1f + 2f * hero.pointsInTalent( Talent.ENTHRALLING_GAZE ) ).object = hero.id();
						VampireFX.on( ch.sprite, VampireFX.CHARM, 0.6f, 0.65f ).offset( 0, -3f );
						if (ch.sprite != null) ch.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f, 5 );
						Sample.INSTANCE.play( Assets.Sounds.CHARMS );
						hero.sprite.zap( cell );
						finish( hero, 1f );
					}
				} );
				break;

			case SWARM:
				pickEnemy( hero, skill, 5, new OnTarget() {
					@Override
					public void hit( Char ch, int cell ) {
						spend( hero, skill );
						Invisibility.dispel();
						final int pts = hero.pointsInTalent( Talent.BAT_SWARM );
						hero.busy();
						hero.sprite.zap( cell );
						Sample.INSTANCE.play( Assets.Sounds.MISS, 1f, 1.4f );
						final Char target = ch;
						PointF a = hero.sprite.center();
						PointF b = ch.sprite != null ? ch.sprite.center() : a;
						VampireFX.shoot( VampireFX.Projectile.BAT, new PointF( a.x, a.y - 6 ), b, 0f, null );
						VampireFX.shoot( VampireFX.Projectile.BAT, new PointF( a.x, a.y + 5 ), b, 0.07f, null );
						VampireFX.shoot( VampireFX.Projectile.BAT, a, b, 0.14f, new Callback() {
							@Override
							public void call() {
								if (target.isAlive()){
									VampireFX.on( target.sprite, VampireFX.BATS, 0.6f, 0.5f );
									target.damage( Random.NormalIntRange( 1 + pts, 3 + lvl / 4 + pts ), CrimsonGrail.this );
									//2/3턴 동안 실명
									if (target.isAlive()) Buff.prolong( target, Blindness.class, 1f + pts );
									if (target.sprite != null) target.sprite.emitter().burst( ShadowParticle.CURSE, 6 );
								}
								finish( hero, 1f );
							}
						} );
					}
				} );
				break;

			case LANCE:
				pickEnemy( hero, skill, 7, new OnTarget() {
					@Override
					public void hit( Char target, final int cell ) {
						final Ballistica shot = new Ballistica( hero.pos, cell, Ballistica.PROJECTILE );
						spend( hero, skill );
						Invisibility.dispel();
						hero.busy();
						hero.sprite.zap( cell );
						Sample.INSTANCE.play( Assets.Sounds.ZAP );
						Char first = Actor.findChar( shot.collisionPos );
						PointF end = first != null && first.sprite != null ? first.sprite.center()
								: com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap.raisedTileCenterToWorld( shot.collisionPos );
						VampireFX.shoot( VampireFX.Projectile.LANCE, hero.sprite.center(), end, 0.1f, new Callback() {
							@Override
							public void call() {
								VampireFX.at( shot.collisionPos, VampireFX.SPLASH, 0.6f, 0.4f );
								Char ch = Actor.findChar( shot.collisionPos );
								if (ch != null && ch != hero){
									int pts = hero.pointsInTalent( Talent.BLOOD_LANCE );
									int dmg = Math.round( Random.NormalIntRange( 3 + lvl / 2, 6 + lvl ) * (0.8f + 0.2f * pts) );
									boolean living = isLiving( ch );
									ch.damage( dmg, CrimsonGrail.this );
									if (ch.sprite != null) ch.sprite.emitter().burst( BloodParticle.BURST, 8 );
									//살아 있는 적이면 준 피해의 20%만큼 회복
									if (living) heal( hero, Math.max( 1, Math.round( dmg * 0.2f ) ) );
								}
								finish( hero, 1f );
							}
						} );
					}
				} );
				break;

			case VEIL:
				spend( hero, skill );
				//최대 체력의 15/20%만큼 보호막
				Buff.affect( hero, Barrier.class ).setShield(
						Math.max( 3, Math.round( hero.HT * (0.10f + 0.05f * hero.pointsInTalent( Talent.CRIMSON_VEIL )) ) ) );
				VampireFX.on( hero.sprite, VampireFX.VEIL, 0.68f, 0.6f );
				Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 0.8f, 0.8f );
				hero.sprite.operate( hero.pos );
				finish( hero, 1f );
				break;

			case DREAD: {
				spend( hero, skill );
				int pts = hero.pointsInTalent( Talent.DREADFUL_EYES );
				Invisibility.dispel();
				//보이는 적 가운데 4/5칸 안의 적을 6/8턴 동안 공포에 빠뜨림
				for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )){
					if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]
							&& Dungeon.level.distance( hero.pos, mob.pos ) <= 3 + pts){
						Buff.affect( mob, Terror.class, 4f + 2f * pts ).object = hero.id();
						VampireFX.on( mob.sprite, VampireFX.DREAD, 0.36f, 0.5f ).offset( 0, -11f ).delay( 0.2f );
						if (mob.sprite != null) mob.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
					}
				}
				VampireFX.on( hero.sprite, VampireFX.DREAD, 0.85f, 0.7f ).offset( 0, -15f );
				Sample.INSTANCE.play( Assets.Sounds.CURSED, 0.8f, 0.7f );
				hero.sprite.operate( hero.pos );
				finish( hero, 1f );
				break;
			}

			case NOVA: {
				spend( hero, skill );
				int pts = hero.pointsInTalent( Talent.BLOOD_NOVA );
				Invisibility.dispel();
				//2칸 안의 모든 적에게 피해, 맞힌 살아 있는 적 하나당 체력 2/3/4 회복
				PathFinder.buildDistanceMap( hero.pos, BArray.not( Dungeon.level.solid, null ), 2 );
				int healed = 0;
				for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )){
					if (mob.alignment != Char.Alignment.ALLY && PathFinder.distance[mob.pos] < Integer.MAX_VALUE){
						int dmg = Math.round( Random.NormalIntRange( 4 + lvl / 2, 8 + lvl ) * (0.75f + 0.25f * pts) );
						boolean living = isLiving( mob );
						mob.damage( dmg, this );
						if (mob.sprite != null) mob.sprite.emitter().burst( BloodParticle.BURST, 8 );
						if (living) healed += 1 + pts;
					}
				}
				VampireFX.at( hero.pos, VampireFX.NOVA, 1.6f, 0.5f ).grow( 1.75f );
				com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.shake( 1.5f, 0.25f );
				Sample.INSTANCE.play( Assets.Sounds.BLAST );
				if (healed > 0) heal( hero, healed );
				hero.sprite.operate( hero.pos );
				finish( hero, 1f );
				break;
			}

			//--------------------------------------------- 진조 흡혈귀
			case FEAST:
				pickAdjacent( hero, skill, new OnTarget() {
					@Override
					public void hit( final Char ch, final int cell ) {
						spend( hero, skill );
						Invisibility.dispel();
						hero.busy();
						hero.sprite.attack( cell, new Callback() {
							@Override
							public void call() {
								if (ch.isAlive()){
									//기본 공격의 150% 피해, 준 피해의 절반만큼 회복 (생명체가 아니면 20%), 출혈
									int dmg = strike( hero, ch, 1.5f );
									boolean living = isLiving( ch );
									VampireFX.on( ch.sprite, VampireFX.JAWS, 0.75f, 0.45f );
									bloodStream( hero, ch );
									ch.damage( dmg, CrimsonGrail.this );
									if (ch.sprite != null) ch.sprite.emitter().burst( BloodParticle.BURST, 12 );
									if (ch.isAlive() && living) Buff.affect( ch, Bleeding.class ).set( Math.max( 1, dmg / 3f ) );
									heal( hero, Math.max( 1, Math.round( dmg * (living ? 0.5f : 0.2f) ) ) );
									Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG, 1f, 0.8f );
									frenzy( hero );
								}
								finish( hero, hero.attackDelay() );
							}
						} );
					}
				} );
				break;

			case MASS_DRAIN: {
				spend( hero, skill );
				//주변 8칸의 모든 적을 한꺼번에 물어 기본 공격의 50/75/100% 피해
				float mult = 0.25f + 0.25f * hero.pointsInTalent( Talent.MASS_DRAIN );
				boolean any = false;
				for (int n : PathFinder.NEIGHBOURS8){
					Char ch = Actor.findChar( hero.pos + n );
					if (ch != null && ch.alignment != Char.Alignment.ALLY){
						bite( hero, ch, hero.pos + n, mult );
						any = true;
					}
				}
				Invisibility.dispel();
				VampireFX.on( hero.sprite, VampireFX.SIPHON, 0.95f, 0.55f );
				if (any){
					Sample.INSTANCE.play( Assets.Sounds.DRINK, 0.8f, 0.7f );
					frenzy( hero );
				}
				hero.sprite.operate( hero.pos );
				finish( hero, hero.attackDelay() );
				break;
			}

			//--------------------------------------------- 피의 지배자
			case BLADE:
				spend( hero, skill );
				//12턴 동안 근접 공격에 추가 피해가 붙고, 맞힐 때마다 생명력을 빼앗음
				Buff.affect( hero, BloodBlade.class, BloodBlade.TURNS );
				VampireFX.on( hero.sprite, VampireFX.BLADE, 0.62f, 0.6f ).offset( 0, -3f );
				Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 0.8f, 1.2f );
				hero.sprite.operate( hero.pos );
				finish( hero, 1f );
				break;

			case INFUSE:
				spend( hero, skill );
				//다른 유물을 10턴 분량만큼 곧바로 충전하고, 마법 막대를 5턴 동안 충전
				ArtifactRecharge.chargeArtifacts( hero, 10f );
				Buff.affect( hero, Recharging.class, 5f );
				VampireFX.on( hero.sprite, VampireFX.INFUSE, 0.72f, 0.6f ).offset( 0, -5f );
				hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
				Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
				hero.sprite.operate( hero.pos );
				finish( hero, 1f );
				break;

			case CHAINS: {
				spend( hero, skill );
				int pts = hero.pointsInTalent( Talent.BLOOD_CHAINS );
				Invisibility.dispel();
				//보이는 적 가운데 3칸 안의 적에게 피해를 주고 2/3/4턴 동안 묶음
				for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )){
					if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]
							&& Dungeon.level.distance( hero.pos, mob.pos ) <= 3){
						if (mob.sprite != null && hero.sprite.parent != null){
							hero.sprite.parent.add( new Chains( hero.sprite.center(), mob.sprite.center(), Effects.Type.CHAIN, null ).hardlight( 0xE8304C ) );
							VampireFX.on( mob.sprite, VampireFX.SHACKLE, 0.5f, 0.6f ).delay( 0.15f );
						}
						mob.damage( Random.NormalIntRange( 2 + lvl / 4, 4 + lvl / 2 ), this );
						if (mob.isAlive()) Buff.prolong( mob, Roots.class, 1f + pts );
					}
				}
				Sample.INSTANCE.play( Assets.Sounds.CHAINS );
				hero.sprite.operate( hero.pos );
				finish( hero, 1f );
				break;
			}
		}
	}

	private void finish( Hero hero, float time ){
		Talent.onArtifactUsed( hero );
		updateQuickslot();
		hero.spendAndNext( time );
	}

	private static void heal( Hero hero, int amount ){
		amount = Math.min( amount, hero.HT - hero.HP );
		if (amount <= 0) return;
		hero.HP += amount;
		hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( amount ), FloatingText.HEALING );
	}

	//생명력을 쓰고, 쓴 만큼 성배가 자람 (24를 쓸 때마다 한 단계, 최대치 +3)
	private void spend( Hero hero, Skill skill ){
		int cost = skill.cost( hero );
		if (cost <= 0) return;
		consume( hero, cost );
	}

	//기술 말고 다른 곳(피의 성배 강화)에 생명력을 씀. 기술에 쓴 것처럼 성배의 성장에도 들어감
	public boolean payLife( Hero hero, int cost ){
		if (cursed || charge < cost) return false;
		consume( hero, cost );
		return true;
	}

	private void consume( Hero hero, int cost ){
		charge = Math.max( 0, charge - cost );
		exp += cost;
		while (level() < levelCap && exp >= 24){
			exp -= 24;
			upgrade();
			GLog.p( Messages.get( this, "levelup" ) );
		}
		//피의 지배자: 쓴 생명력이 다른 유물로 흘러 들어감 (생명력 1당 0.5턴 분량)
		if (hero.subClass == HeroSubClass.BLOOD_LORD){
			ArtifactRecharge.chargeArtifacts( hero, cost * 0.5f );
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

	public static CrimsonGrail of( Hero hero ){
		if (hero == null || hero.heroClass != HeroClass.VAMPIRE) return null;
		CrimsonGrail g = hero.belongings.getItem( CrimsonGrail.class );
		return (g != null && g.isEquipped( hero ) && !g.cursed) ? g : null;
	}

	//생명력을 얻음. 소수점은 조금씩 모아 둠
	public static void gainLife( Hero hero, float amount ){
		CrimsonGrail g = of( hero );
		if (g == null) return;
		int cap = g.lifeCap();
		if (g.charge >= cap) return;
		g.partialCharge += amount;
		int whole = (int) g.partialCharge;
		if (whole > 0){
			g.partialCharge -= whole;
			g.charge = Math.min( cap, g.charge + whole );
			if (g.charge >= cap) g.partialCharge = 0;
			updateQuickslot();
		}
	}

	//적이 쓰러질 때 (Mob.destroy에서 불림)
	public static void onKill( Mob mob ){
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive() || of( hero ) == null) return;
		boolean living = isLiving( mob );

		//최대 체력의 30% (2~16). 생명체가 아니면 그 1/3 (진조는 2/3)
		float gain = Math.max( 2f, Math.min( 16f, mob.HT * 0.3f ) );
		if (!living) gain *= hero.subClass == HeroSubClass.TRUE_ANCESTOR ? 0.67f : 0.34f;
		//깊은 잔: +10/20/30%
		gain *= 1f + 0.1f * hero.pointsInTalent( Talent.DEEP_CUP );
		gainLife( hero, gain );
		if (mob.sprite != null && living) mob.sprite.emitter().burst( BloodParticle.BURST, 6 );

		//핏빛 갈증: 살아 있는 적을 쓰러뜨리면 체력 2/3 회복
		if (living && hero.hasTalent( Talent.BLOOD_THIRST )){
			heal( hero, 1 + hero.pointsInTalent( Talent.BLOOD_THIRST ) );
		}
	}

	//핏빛 검이 걸린 동안의 근접 공격 (Hero.attackProc에서 불림)
	public static int onHeroAttack( Hero hero, Char enemy, int damage ){
		if (hero.buff( BloodBlade.class ) == null || of( hero ) == null) return damage;
		if (hero.belongings.attackingWeapon() instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon) return damage;
		gainLife( hero, isLiving( enemy ) ? 1f : 0.34f );
		if (enemy.sprite != null) enemy.sprite.emitter().burst( BloodParticle.BURST, 4 );
		return damage + 2 + hero.lvl / 5;
	}

	@Override
	public void charge( Hero target, float amount ) {
		//다른 충전 효과로는 차지 않음 (생명력은 빼앗거나, 저절로 고이기를 기다려야 함)
	}

	@Override
	public String status() {
		if (!isIdentified() || cursed) return null;
		return Messages.format( "%d/%d", charge, lifeCap() );
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
			//시간이 지나면 아주 느리게 저절로 고임: 60턴에 1 (피의 지배자는 두 배, 깊은 잔 +25/50/75%)
			if (target instanceof Hero && !cursed && charge < lifeCap()
					&& target.buff( MagicImmune.class ) == null && Regeneration.regenOn()){
				Hero hero = (Hero) target;
				float perTurn = 1f / 60f;
				perTurn *= RingOfEnergy.artifactChargeMultiplier( target );
				if (hero.subClass == HeroSubClass.BLOOD_LORD) perTurn *= 2f;
				perTurn *= 1f + 0.25f * hero.pointsInTalent( Talent.DEEP_CUP );
				//피의 성배를 함께 장착하면 20% + 피의 성배 레벨당 10% 빨라짐 (+10이면 2.2배)
				ChaliceOfBlood c = chalice( hero );
				if (c != null) perTurn *= 1.2f + 0.1f * c.level();
				gainLife( hero, perTurn );
			}
			spend( TICK );
			return true;
		}

		@Override
		public String actionName() {
			return quickSkill != null ? quickSkill.title() : Messages.get( CrimsonGrail.this, "name" );
		}

		@Override
		public int actionIcon() {
			return (quickSkill != null ? quickSkill.icon() : Skill.DRAIN.icon()) + ICON_ACTION_OFFSET;
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
			//어떤 기술이든 조준 중이면, 이 버튼을 눌러 조준점이 있는 적에게 곧바로 씀
			// (빠른 사용 기술이 아니라 기술 창에서 고른 매혹의 시선·박쥐 떼·피의 창·진조의 포식도 같음)
			if (aimSkill != null && aimTarget != null && aimTarget.isAlive()){
				int cell = QuickSlotButton.autoAim( aimTarget, CrimsonGrail.this );
				GameScene.handleCell( cell != -1 ? cell : aimTarget.pos );
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
			d += "\n\n" + (cursed ? Messages.get( this, "desc_cursed" ) : Messages.get( this, "desc_stats", charge, lifeCap() ));
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
		//'넘치는 잔'으로 늘어난 몫이 불러올 때 잘리지 않게, 저장된 생명력을 그대로 되살림
		if (bundle.contains( "charge" )) charge = bundle.getInt( "charge" );
		quickSkill = null;
		if (bundle.contains( "quick_skill" )){
			try { quickSkill = Skill.valueOf( bundle.getString( "quick_skill" ) ); } catch (Exception e) { quickSkill = null; }
		}
	}
}
