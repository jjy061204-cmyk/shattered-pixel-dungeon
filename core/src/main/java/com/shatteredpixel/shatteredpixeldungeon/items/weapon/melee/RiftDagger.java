/*
 * Shattered Pixel Dungeon 모드: 차원 균열 단검
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.RiftTear;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;

//5티어 단검. 칼날이 공간 파편으로 되어 있어 적의 방어력 일부를 꿰뚫음.
//방어 무시율: 기본 25%, 강화 1번마다 +5%, 최대 75%
public class RiftDagger extends Dagger {

	public static final float PIERCE_BASE = 0.25f;
	public static final float PIERCE_PER_LVL = 0.05f;
	public static final float PIERCE_MAX = 0.75f;

	{
		image = ItemSpriteSheet.RIFT_DAGGER;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 0.8f;

		tier = 5;
	}

	//단검처럼 기본 최대 피해가 약간 낮음 (24 + 강화당 6, 일반 5티어는 30 + 6)
	@Override
	public int max( int lvl ) {
		return 4 * (tier + 1) + lvl * (tier + 1);
	}

	public float armorPierce(){
		return armorPierce( buffedLvl() );
	}

	public static float armorPierce( int lvl ){
		return Math.min( PIERCE_MAX, PIERCE_BASE + PIERCE_PER_LVL * Math.max( 0, lvl ) );
	}

	private static int pct( float f ){
		return Math.round( f * 100 );
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		damage = super.proc( attacker, defender, damage );
		//맞은 자리에 균열이 찢어졌다 닫힘
		if (defender.sprite != null && defender.sprite.visible) {
			RiftTear.at( defender.sprite, 0.9f );
			defender.sprite.emitter().burst( PurpleParticle.BURST, 8 );
		}
		return damage;
	}

	//결투가 능력: 차원 도약 (조용한 발걸음과 같지만 6칸, 균열을 통해 이동)
	@Override
	protected void duelistAbility( Hero hero, Integer target ) {
		int from = hero.pos;
		PointF fromP = hero.sprite != null ? hero.sprite.center() : null;
		sneakAbility( hero, target, 6, 2 + buffedLvl(), this );
		if (hero.pos != from) {
			if (fromP != null) RiftTear.at( fromP.x, fromP.y, 1.2f );
			RiftTear.at( hero.sprite, 1.2f );
			CellEmitter.center( from ).burst( PurpleParticle.BURST, 12 );
			CellEmitter.center( hero.pos ).burst( PurpleParticle.BURST, 12 );
			Sample.INSTANCE.play( Assets.Sounds.TELEPORT, 0.8f, 1.2f );
		}
	}

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get( this, "ability_desc", 2 + buffedLvl() );
		} else {
			return Messages.get( this, "typical_ability_desc", 2 );
		}
	}

	@Override
	public String info() {
		String info = super.info();
		if (levelKnown) {
			info += "\n\n" + Messages.get( this, "pierce", pct( armorPierce() ) );
		} else {
			info += "\n\n" + Messages.get( this, "typical_pierce", pct( armorPierce( 0 ) ) );
		}
		int next = buffedLvl() + 1;
		if (levelKnown && armorPierce( next ) > armorPierce()) {
			info += " " + Messages.get( this, "pierce_next", pct( armorPierce( next ) ) );
		}
		return info;
	}

	//보라빛으로 천천히 맥동
	private static final ItemSprite.Glowing RIFT = new ItemSprite.Glowing( 0x8844FF, 1.5f );

	@Override
	public ItemSprite.Glowing glowing() {
		ItemSprite.Glowing g = super.glowing();
		return g != null ? g : RIFT;
	}

	//바닥에 떨어져 있으면 균열처럼 보라색 입자가 새어 나옴
	@Override
	public Emitter emitter() {
		Emitter e = new Emitter();
		e.fillTarget = true;
		e.pour( PurpleParticle.MISSILE, 0.25f );
		return e;
	}
}
