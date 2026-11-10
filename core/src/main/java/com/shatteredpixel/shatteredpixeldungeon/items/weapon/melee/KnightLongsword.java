/*
 * Shattered Pixel Dungeon 모드: 기사의 장검
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.SwordSwing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

//5티어 기사의 장검. 장검을 5티어로 키운 기본 성능 + 같은 적을 연달아 베면 추가 피해 + 도처럼 약간의 방어
public class KnightLongsword extends Longsword {

	//연속 공격: 같은 적을 쉬지 않고 연달아 맞히면 1타마다 추가 피해가 쌓임 (최대 3단계)
	public static final int MAX_COMBO = 3;

	{
		image = ItemSpriteSheet.KNIGHT_LONGSWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.9f;

		tier = 5;
	}

	//공격력: 4티어 장검보다 1씩만 높음 (최소 5, 최대 26, 강화당 +1/+5) — 부가 능력의 대가
	@Override
	public int min( int lvl ) {
		return 5 + lvl;
	}

	@Override
	public int max( int lvl ) {
		return 26 + lvl * 5;
	}

	private int comboTarget = -1;
	private int combo = 0;
	private float comboTime = -100;

	//연속 1단계당 추가 피해: 3 (+강화 2번마다 1)
	public int comboBonus( int lvl ){
		return 3 + Math.max( 0, lvl ) / 2;
	}

	@Override
	public int defenseFactor( Char owner ) {
		return 3;   //도와 같은 0-3 방어
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		//같은 적을 직전 공격 뒤 2턴 안에 다시 맞히면 연속 단계가 오름
		if (defender.id() == comboTarget && Actor.now() - comboTime <= 2.01f){
			combo = Math.min( MAX_COMBO, combo + 1 );
		} else {
			combo = 0;
		}
		comboTarget = defender.id();
		comboTime = Actor.now();

		if (combo > 0){
			damage += combo * comboBonus( buffedLvl() );
			if (defender.sprite != null && defender.sprite.visible){
				defender.sprite.showStatus( CharSprite.WARNING, Messages.get( this, "combo", combo + 1 ) );
			}
		}

		//실험 기능: 검을 휘두르는 모습
		if (attacker.sprite != null && defender.sprite != null){
			SwordSwing.play( attacker.sprite, defender.sprite, image, 2.5f, 12.5f, -45f );
		}

		return super.proc( attacker, defender, damage );
	}

	@Override
	protected void duelistAbility( Hero hero, Integer target ) {
		//가르기: +(7+강화) 피해
		int dmgBoost = augment.damageFactor( 7 + buffedLvl() );
		Sword.cleaveAbility( hero, target, 1, dmgBoost, this );
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = levelKnown ? 7 + buffedLvl() : 7;
		if (levelKnown){
			return Messages.get( this, "ability_desc", augment.damageFactor( min() + dmgBoost ), augment.damageFactor( max() + dmgBoost ) );
		} else {
			return Messages.get( this, "typical_ability_desc", min( 0 ) + dmgBoost, max( 0 ) + dmgBoost );
		}
	}

	@Override
	public String upgradeAbilityStat( int level ){
		int dmgBoost = 7 + level;
		return augment.damageFactor( min( level ) + dmgBoost ) + "-" + augment.damageFactor( max( level ) + dmgBoost );
	}

	@Override
	public String statsInfo() {
		int lvl = levelKnown ? buffedLvl() : 0;
		return Messages.get( this, levelKnown ? "stats_desc" : "typical_stats_desc", comboBonus( lvl ), MAX_COMBO * comboBonus( lvl ) );
	}
}
