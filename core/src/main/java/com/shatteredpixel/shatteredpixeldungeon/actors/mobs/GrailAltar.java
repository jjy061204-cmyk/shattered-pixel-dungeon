/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성 전용 특수방: 성배 제단
 *  - 붉은 천 아래 아이템 하나가 놓여 있음. 최대 체력의 10%를 성배에 바치면 감정된 채로 받음
 *  - 한 번만 쓸 수 있고, 부서지지 않음
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GrailAltar extends GnollProp {

	{
		spriteClass = VampireCastleSprites.Grail.class;
	}

	public boolean used = false;
	public Item prize;

	public static GrailAltar create(){
		GrailAltar a = new GrailAltar();
		Generator.Category cat = Random.oneOf( Generator.Category.WAND, Generator.Category.RING,
				Generator.Category.ARMOR, Generator.Category.WEAPON );
		a.prize = Generator.random( cat );
		return a;
	}

	public int cost( Hero hero ){
		return Math.max( 1, (int)Math.ceil( hero.HT / 10f ) );
	}

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public boolean isInvulnerable( Class effect ) {
		return true;
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean interact( Char c ) {
		if (c instanceof Hero){
			Hero hero = (Hero) c;
			if (used || prize == null){
				GLog.i( Messages.get( GrailAltar.class, "empty" ) );
				return false;
			}
			int cost = cost( hero );
			if (hero.HP <= cost){
				GLog.w( Messages.get( GrailAltar.class, "too_weak" ) );
				return false;
			}
			showOptions( hero, description() + "\n\n" + Messages.get( GrailAltar.class, "prompt", cost ),
					Messages.get( GrailAltar.class, "offer", cost ), Messages.get( GrailAltar.class, "leave" ) );
		}
		return false;
	}

	@Override
	protected void choose( Hero hero, int index ) {
		if (index != 0 || used || prize == null) return;
		int cost = cost( hero );
		if (hero.HP <= cost) return;

		hero.HP -= cost;
		hero.sprite.showStatus( CharSprite.NEGATIVE, Integer.toString( cost ) );
		hero.sprite.operate( pos );
		CellEmitter.center( pos ).burst( BloodParticle.BURST, 12 );
		Sample.INSTANCE.play( Assets.Sounds.DRINK );

		used = true;
		Item it = prize;
		prize = null;
		it.identify();
		Dungeon.level.drop( it, hero.pos ).sprite.drop( pos );
		if (sprite instanceof VampireCastleSprites.Grail){
			((VampireCastleSprites.Grail) sprite).drained();
		}
		GLog.p( Messages.get( GrailAltar.class, "granted", it.name() ) );
		hero.spendAndNext( 1f );
	}

	@Override
	public String description() {
		return Messages.get( GrailAltar.class, used ? "desc_used" : "desc" );
	}

	private static final String USED = "used";
	private static final String PRIZE = "prize";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USED, used );
		if (prize != null) bundle.put( PRIZE, prize );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		used = bundle.getBoolean( USED );
		prize = bundle.contains( PRIZE ) ? (Item) bundle.get( PRIZE ) : null;
	}
}
