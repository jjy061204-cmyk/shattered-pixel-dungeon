/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 형상 기사: 에테르를 점토 안에 집중시켜, 빚은 무기의 종류마다 다른 자잘한 타격 효과가 일어남
 *  - 검: 메아리 참격 (30%, 피해 40%의 추가 참격)
 *  - 단검: 독날 (35%, 중독)
 *  - 도끼·낫: 깊은 상처 (30%, 출혈)
 *  - 둔기: 충격 (20%, 1턴 기절)
 *  - 창·봉: 밀쳐 내기 (35%, 1칸 밀침)
 *  - 채찍: 휘감기 (35%, 3턴 불구)
 *  - 방패: 에테르 방벽 (맞힐 때마다 보호막 +1, 티어×3까지)
 *  - 장갑·건틀릿: 에테르 충격 (맞힐 때마다 방어를 무시하는 1~티어 추가 피해)
 *  - 석궁: 묶기 (30%, 2턴 속박)
 *  - 빚지 않은 점토: 에테르 파편 (맞힐 때마다 1~2 추가 피해)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.RiftSlash;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.StarlightParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.HashMap;

public class EtherForms {

	public enum Type {
		BLADE   (0.30f),
		DAGGER  (0.35f),
		AXE     (0.30f),
		BLUNT   (0.20f),
		POLE    (0.35f),
		WHIP    (0.35f),
		SHIELD  (1f),
		FIST    (1f),
		CROSSBOW(0.30f),
		RAW     (1f);

		public final float chance;
		Type( float chance ){ this.chance = chance; }

		public boolean always(){
			return chance >= 1f;
		}

		public String title(){
			return Messages.get( EtherForms.class, name().toLowerCase() + "_name" );
		}
	}

	private static final HashMap<Class<? extends MeleeWeapon>, Type> TYPES = new HashMap<>();
	static {
		for (Class<? extends MeleeWeapon> c : new Class[]{ WornShortsword.class, Rapier.class, Shortsword.class, Sword.class,
				Scimitar.class, Longsword.class, RunicBlade.class, Katana.class, Greatsword.class, KnightLongsword.class }) TYPES.put( c, Type.BLADE );
		for (Class<? extends MeleeWeapon> c : new Class[]{ Dagger.class, Dirk.class, Sai.class, AssassinsBlade.class, RiftDagger.class }) TYPES.put( c, Type.DAGGER );
		for (Class<? extends MeleeWeapon> c : new Class[]{ HandAxe.class, Sickle.class, BattleAxe.class, Greataxe.class, WarScythe.class }) TYPES.put( c, Type.AXE );
		for (Class<? extends MeleeWeapon> c : new Class[]{ Cudgel.class, Mace.class, Flail.class, WarHammer.class }) TYPES.put( c, Type.BLUNT );
		for (Class<? extends MeleeWeapon> c : new Class[]{ Spear.class, Quarterstaff.class, Glaive.class }) TYPES.put( c, Type.POLE );
		TYPES.put( Whip.class, Type.WHIP );
		TYPES.put( RoundShield.class, Type.SHIELD );
		TYPES.put( Greatshield.class, Type.SHIELD );
		TYPES.put( Gloves.class, Type.FIST );
		TYPES.put( Gauntlet.class, Type.FIST );
		TYPES.put( Crossbow.class, Type.CROSSBOW );
	}

	public static Type typeOf( MeleeWeapon form ){
		if (form == null) return Type.RAW;
		Type t = TYPES.get( form.getClass() );
		return t != null ? t : Type.BLADE;
	}

	//형상 공명: 발동 확률·효과 +25 / 50 / 75%
	public static float boost( Hero hero ){
		return 1f + 0.25f * hero.pointsInTalent( Talent.FORM_RESONANCE );
	}

	public static float chance( Hero hero, Type t ){
		return Math.min( 1f, t.chance * boost( hero ) );
	}

	//빚은 무기의 타격 효과 (EtherClay.proc 에서 부름)
	public static int proc( Hero hero, Char enemy, int damage, MeleeWeapon form, int tier ){
		if (hero.subClass != HeroSubClass.FORM_KNIGHT || enemy == null || enemy == hero) return damage;
		Type t = typeOf( form );

		boolean forced = false;
		ShiftingStrike ss = hero.buff( ShiftingStrike.class );
		if (ss != null){
			ss.detach();
			forced = true;
			damage = Math.round( damage * (1f + 0.15f * hero.pointsInTalent( Talent.SHIFTING_STRIKE )) );
		}

		if (!forced && !t.always() && Random.Float() >= chance( hero, t )) return damage;

		float b = boost( hero );
		CharSprite es = enemy.sprite;
		switch (t){
			case BLADE: {
				int extra = Math.max( 1, Math.round( damage * 0.4f * (forced ? b : 1f) ) );
				damage += extra;
				if (es != null && es.visible) RiftSlash.at( es, 0.8f );
				Sample.INSTANCE.play( Assets.Sounds.HIT_SLASH, 0.8f, 1.2f );
				break;
			}
			case DAGGER:
				Buff.affect( enemy, Poison.class ).set( Math.round( (2 + 2*tier) * b ) );
				if (es != null) es.burst( 0xFF8BC34A, 4 );
				break;
			case AXE:
				Buff.affect( enemy, Bleeding.class ).set( Math.max( 1f, damage * 0.6f * b ) );
				break;
			case BLUNT:
				Buff.prolong( enemy, Paralysis.class, 1f );
				if (es != null) es.centerEmitter().burst( Speck.factory( Speck.STAR ), 3 );
				Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 0.8f, 0.9f );
				break;
			case POLE:
				if (enemy.isAlive() && !enemy.properties().contains( Char.Property.IMMOVABLE )){
					Ballistica traj = new Ballistica( enemy.pos, enemy.pos + (enemy.pos - hero.pos), Ballistica.MAGIC_BOLT );
					WandOfBlastWave.throwChar( enemy, traj, Math.round( b ), true, true, hero );
				}
				break;
			case WHIP:
				Buff.prolong( enemy, Cripple.class, 3f * b );
				break;
			case SHIELD: {
				Barrier br = Buff.affect( hero, Barrier.class );
				int cap = 3 * Math.max( 1, tier );
				int add = Math.max( 1, Math.round( b ) );
				int now = br.shielding();
				if (now < cap) br.incShield( Math.min( add, cap - now ) );
				break;
			}
			case FIST: {
				int extra = Random.NormalIntRange( 1, Math.max( 1, Math.round( tier * b ) ) );
				enemy.damage( extra, hero );
				if (es != null) es.burst( 0xFFB8A4FF, 3 );
				break;
			}
			case CROSSBOW:
				Buff.prolong( enemy, Roots.class, 2f * b );
				break;
			case RAW: default: {
				int extra = Random.NormalIntRange( 1, Math.max( 1, Math.round( 2 * b ) ) );
				enemy.damage( extra, hero );
				break;
			}
		}

		//방패·장갑처럼 늘 일어나는 효과는 별가루를 조금만
		if (es != null && es.visible && !t.always()) es.emitter().burst( StarlightParticle.BURST, 3 );

		//순환하는 에테르: 발동한 효과(늘 일어나는 효과는 4번에 한 번꼴)만큼 체력 1/2/3 회복
		int heal = hero.pointsInTalent( Talent.ETHER_CIRCULATION );
		if (heal > 0 && (!t.always() || Random.Int( 4 ) == 0) && hero.HP < hero.HT){
			heal = Math.min( heal, hero.HT - hero.HP );
			hero.HP += heal;
			if (hero.sprite != null) hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
		}
		return damage;
	}

	//점토를 새로 빚었을 때 (변형 일격)
	public static void onReshape( Hero hero ){
		if (hero.subClass == HeroSubClass.FORM_KNIGHT && hero.hasTalent( Talent.SHIFTING_STRIKE )){
			Buff.prolong( hero, ShiftingStrike.class, ShiftingStrike.DURATION );
		}
	}

	//점토 설명에 붙는 지금 형상의 효과
	public static String describe( Hero hero, MeleeWeapon form ){
		Type t = typeOf( form );
		String desc = Messages.get( EtherForms.class, t.name().toLowerCase() + "_desc", Math.round( chance( hero, t ) * 100 ) );
		return Messages.get( EtherForms.class, "info", t.title(), desc );
	}

	//변형 일격: 점토를 새로 빚은 뒤 5턴 안의 다음 공격은 효과가 반드시 일어나고 피해 +15/30/45%
	public static class ShiftingStrike extends FlavourBuff {
		public static final float DURATION = 5f;
		{ type = buffType.POSITIVE; }

		@Override
		public int icon() {
			return BuffIndicator.UPGRADE;
		}

		@Override
		public void tintIcon( Image icon ) {
			icon.hardlight( 0xB8A4FF );
		}

		@Override
		public float iconFadePercent() {
			return Math.max( 0, (DURATION - visualcooldown()) / DURATION );
		}

		@Override
		public String desc() {
			Hero hero = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
			return Messages.get( this, "desc", 15 * (hero != null ? hero.pointsInTalent( Talent.SHIFTING_STRIKE ) : 1), dispTurns() );
		}
	}
}
