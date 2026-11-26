/*
 * Shattered Pixel Dungeon 모드: 영웅 고유 공격 이펙트
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.VampireFX;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CrimsonGrail;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Callback;

//뱀파이어 갑옷 능력 1: 심장 꿰뚫기 (단일 극딜)
// 거대한 핏빛 말뚝을 던져, 처음 닿는 적 하나에게 큰 마법 피해. 체력이 절반 이하인 적에게는 1.5배
//  - 준 피해의 25%만큼 뱀파이어 회복
//  - 처형: 맞고 남은 체력이 최대 체력의 15%(꿰뚫는 말뚝 +5%/점) 이하이면 심장이 터져 즉사 (보스·중간 보스는 대신 피해 +25%)
public class HeartPierce extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get( this, "prompt" );
	}

	public static int minDamage( Hero hero ){ return 12 + 3 * hero.lvl; }
	public static int maxDamage( Hero hero ){ return 24 + 5 * hero.lvl; }

	//처형 기준: 최대 체력의 15/20/25/30/35%
	public static float executeAt( Hero hero ){
		return 0.15f + 0.05f * hero.pointsInTalent( Talent.PIERCING_STAKE );
	}

	@Override
	protected void activate( final ClassArmor armor, final Hero hero, Integer target ) {
		if (target == null) return;

		final Ballistica path = new Ballistica( hero.pos, target, Ballistica.PROJECTILE );
		final Char enemy = Actor.findChar( path.collisionPos );
		if (enemy == null || enemy == hero || !Char.foeTarget( enemy )){
			GLog.w( Messages.get( this, "no_target" ) );
			return;
		}

		final float used = chargeUse( hero );
		armor.charge -= used;
		armor.updateQuickslot();
		Invisibility.dispel();

		hero.busy();
		hero.sprite.zap( enemy.pos );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 1f, 0.7f );
		//손끝에 핏빛 기운이 나선으로 모여 응축됨 → 말뚝이 날아감 → 심장이 갈라지며 터짐
		VampireFX.onBig( hero.sprite, VampireFX.B_CHARGE, 0.42f, 0.4f ).offset( 0, -3f );

		VampireFX.shoot( VampireFX.Projectile.STAKE, hero.sprite.center(),
				enemy.sprite != null ? enemy.sprite.center() : hero.sprite.center(), 0.3f, new Callback() {
			@Override
			public void call() {
				VampireFX.atBig( enemy.pos, VampireFX.B_IMPACT, 0.62f, 0.6f );
				PixelScene.shake( 2f, 0.3f );
				Sample.INSTANCE.play( Assets.Sounds.BLAST, 1f, 0.8f );
				Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG );

				if (enemy.isAlive()){
					int dmg = Random.NormalIntRange( minDamage( hero ), maxDamage( hero ) );
					//꿰뚫는 말뚝: 피해 +15/30/45/60%
					dmg = Math.round( dmg * (1f + 0.15f * hero.pointsInTalent( Talent.PIERCING_STAKE )) );
					//체력이 절반 이하인 적에게는 1.5배
					if (enemy.HP <= enemy.HT / 2) dmg = Math.round( dmg * 1.5f );
					boolean boss = Char.hasProp( enemy, Char.Property.BOSS ) || Char.hasProp( enemy, Char.Property.MINIBOSS );
					if (boss) dmg = Math.round( dmg * 1.25f );
					int before = Math.max( 0, enemy.HP ) + enemy.shielding();
					enemy.damage( dmg, HeartPierce.this );
					int dealt = Math.max( 0, before - Math.max( 0, enemy.HP ) - enemy.shielding() );

					//처형: 심장이 꿰뚫린 채 버틸 수 없을 만큼 약해지면 즉사
					if (enemy.isAlive() && !boss && enemy.HP <= Math.round( enemy.HT * executeAt( hero ) )){
						dealt += enemy.HP;
						if (enemy.sprite != null) enemy.sprite.showStatus( CharSprite.NEGATIVE, Messages.get( HeartPierce.class, "execute" ) );
						VampireFX.atBig( enemy.pos, VampireFX.B_HEART, 0.5f, 0.6f );
						enemy.die( HeartPierce.this );
					}

					//빼앗은 피: 준 피해의 25%만큼 회복
					int heal = Math.min( Math.round( dealt * 0.25f ), hero.HT - hero.HP );
					if (heal > 0){
						hero.HP += heal;
						hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
					}

					if (!enemy.isAlive()){
						//피의 수확: 쓰러뜨리면 쓴 충전량의 15/30/45/60%를 돌려받고 성배에 생명력
						int pts = hero.pointsInTalent( Talent.BLOOD_HARVEST );
						if (pts > 0){
							armor.charge = Math.min( 100f, armor.charge + used * 0.15f * pts );
							armor.updateQuickslot();
							CrimsonGrail.gainLife( hero, 2f * pts );
							//쓰러진 적의 피로 최대 체력의 5/10/15/20% 더 회복
							int more = Math.min( Math.round( hero.HT * 0.05f * pts ), hero.HT - hero.HP );
							if (more > 0){
								hero.HP += more;
								hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( more ), FloatingText.HEALING );
							}
						}
					} else {
						//잔혹한 상처: 2/4/6/8턴 동안 취약(받는 피해 증가), 출혈
						int pts = hero.pointsInTalent( Talent.CRUEL_WOUND );
						if (pts > 0){
							Buff.prolong( enemy, Vulnerable.class, 2f * pts );
							Buff.prolong( enemy, Cripple.class, 2f * pts );
							Buff.affect( enemy, Bleeding.class ).set( Math.max( 1f, dmg * 0.05f * pts ) );
						}
					}
				}
				hero.spendAndNext( Actor.TICK );
			}
		} );
	}

	@Override
	public int icon() {
		return HeroIcon.HEART_PIERCE;
	}

	@Override
	public String desc() {
		String d = Messages.get( this, "desc" );
		if (Dungeon.hero != null){
			d += "\n\n" + Messages.get( this, "dmg", minDamage( Dungeon.hero ), maxDamage( Dungeon.hero ) );
			d += "\n" + Messages.get( this, "execute_at", Math.round( executeAt( Dungeon.hero ) * 100 ) );
		}
		return d + "\n\n" + Messages.get( this, "cost", (int) baseChargeUse );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.PIERCING_STAKE, Talent.BLOOD_HARVEST, Talent.CRUEL_WOUND, Talent.HEROIC_ENERGY };
	}
}
