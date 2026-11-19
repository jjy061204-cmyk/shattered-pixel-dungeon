/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 포로 우리: 놀이 잡아 둔 짐승(박쥐·동굴 거미·게)이 갇혀 있음
 *  - 우리를 열면(또는 부서지면) 짐승이 10턴 동안 가장 가까운 놀을 공격한 뒤 떠남
 *  - 근처에 놀이 없으면 짐승은 영웅을 노림
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpire;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GnollCage extends GnollProp {

	{
		spriteClass = GnollEmpireSprites.Cage.class;
		HP = HT = 3;
	}

	//갇힌 짐승: 0 박쥐, 1 동굴 거미, 2 게
	public int beast = -1;

	public static GnollCage random(){
		GnollCage c = new GnollCage();
		c.beast = Random.chances( new float[]{ 2, 2, 1 } );
		return c;
	}

	@Override
	public boolean interact( Char c ) {
		if (c instanceof Hero){
			showOptions( (Hero) c, description() + "\n\n" + Messages.get( this, "prompt" ),
					Messages.get( this, "open" ), Messages.get( this, "leave" ) );
		}
		return false;
	}

	@Override
	protected void choose( Hero hero, int index ) {
		if (index == 0){
			hero.sprite.operate( pos );
			Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
			HP = 0;
			die( hero );
			hero.spendAndNext( 1f );
		}
	}

	@Override
	protected void onBroken( Object cause ) {
		if (Dungeon.level.heroFOV[pos]){
			CellEmitter.get( pos ).burst( Speck.factory( Speck.WOOL ), 6 );
		}
	}

	@Override
	public void die( Object cause ) {
		final int cell = pos;
		super.die( cause );
		release( cell );
	}

	private void release( int cell ){
		Mob m;
		switch (beast){
			case 0: default: m = new Bat(); break;
			case 1:          m = new Spinner(); break;
			case 2:          m = new Crab(); break;
		}
		m.pos = cell;

		//근처(8칸 안)에 놀이 있으면 그 놀부터 공격
		boolean gnollNear = false;
		for (Mob other : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (other.isAlive() && other.alignment == Alignment.ENEMY && GnollEmpire.isGnoll( other )
					&& Dungeon.level.distance( cell, other.pos ) <= 8){
				gnollNear = true;
				break;
			}
		}

		if (Actor.findChar( cell ) != null) return;
		GameScene.add( m );
		Dungeon.level.occupyCell( m );

		if (gnollNear){
			m.alignment = Alignment.ALLY;
			m.state = m.WANDERING;
			Buff.affect( m, Freed.class, Freed.DURATION );
			if (Dungeon.level.heroFOV[cell]) GLog.p( Messages.get( this, "freed", m.name() ) );
		} else {
			m.state = m.HUNTING;
			m.beckon( Dungeon.hero.pos );
			if (Dungeon.level.heroFOV[cell]) GLog.w( Messages.get( this, "freed_hostile", m.name() ) );
		}
		if (m.sprite != null) m.sprite.showAlert();
	}

	@Override
	public String description() {
		String key = beast == 1 ? "spinner" : (beast == 2 ? "crab" : "bat");
		return Messages.get( this, "desc", Messages.get( this, key ) );
	}

	private static final String BEAST = "beast";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( BEAST, beast );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		beast = bundle.getInt( BEAST );
	}

	//우리에서 풀려난 짐승: 정해진 턴 동안 놀과 싸운 뒤 동굴 속으로 달아남
	public static class Freed extends FlavourBuff {

		public static final float DURATION = 10f;

		{
			type = buffType.POSITIVE;
			announced = true;
		}

		@Override
		public int icon() {
			return BuffIndicator.MARK;
		}

		@Override
		public float iconFadePercent() {
			return Math.max( 0, (DURATION - visualcooldown()) / DURATION );
		}

		@Override
		public void detach() {
			super.detach();
			Char t = target;
			if (t != null && t.isAlive() && t instanceof Mob){
				if (Dungeon.level.heroFOV[t.pos]){
					CellEmitter.get( t.pos ).burst( Speck.factory( Speck.WOOL ), 6 );
					GLog.i( Messages.get( GnollCage.class, "fled", t.name() ) );
				}
				((Mob) t).destroy();
				if (t.sprite != null) t.sprite.killAndErase();
			}
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc", dispTurns() );
		}
	}
}
