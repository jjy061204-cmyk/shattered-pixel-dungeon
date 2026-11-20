/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 흡혈귀의 성: 관 (12·13층 1개, 14층 2개)
 *  - 관이 있는 층은 원작의 자연 리스폰 대신, 어둠 속 관에서 '피의 하수인'이 기어 나옴 (총 몹 수는 같음)
 *  - 관이 빛 안에 있으면 나오지 않음. 모두 부수면 원작 리스폰으로 돌아감
 *  - 바로 옆에서 2턴 들여 부수거나, 던진 무기·마법으로 세 번 맞혀 부숨
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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.VampireCastle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VampireCastleSprites;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Coffin extends GnollProp {

	{
		spriteClass = VampireCastleSprites.Coffin.class;
		HP = HT = 3;
	}

	@Override
	protected boolean flammable() {
		return true;
	}

	@Override
	public boolean interact( Char c ) {
		if (c instanceof Hero){
			showOptions( (Hero) c, description() + "\n\n" + Messages.get( Coffin.class, "prompt" ),
					Messages.get( Coffin.class, "smash" ), Messages.get( Coffin.class, "leave" ) );
		}
		return false;
	}

	@Override
	protected void choose( Hero hero, int index ) {
		if (index == 0){
			hero.sprite.attack( pos );
			Sample.INSTANCE.play( Assets.Sounds.HIT_STRONG );
			HP = 0;
			die( hero );
			hero.spendAndNext( 2f );
		}
	}

	@Override
	protected void onBroken( Object cause ) {
		if (Dungeon.level.heroFOV[pos]){
			CellEmitter.get( pos ).burst( Speck.factory( Speck.WOOL ), 6 );
			CellEmitter.get( pos ).burst( ShadowParticle.UP, 8 );
			GLog.p( Messages.get( Coffin.class, "broken" ) );
		}
	}

	//하수인이 나올 수 있는지 (관이 어둠 속에 있음)
	public boolean dormant(){
		return isAlive() && !VampireCastle.isLit( pos );
	}

	//관 둘레 빈칸에 피의 하수인을 내보냄. 성공하면 true
	public boolean release(){
		ArrayList<Integer> cells = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int c = pos + n;
			if (Dungeon.level.passable[c] && !Dungeon.level.avoid[c] && Actor.findChar( c ) == null){
				cells.add( c );
			}
		}
		if (cells.isEmpty()) return false;

		BloodThrall t = new BloodThrall();
		t.pos = Random.element( cells );
		t.state = t.WANDERING;
		GameScene.add( t );
		Dungeon.level.occupyCell( t );

		if (sprite instanceof VampireCastleSprites.Coffin){
			((VampireCastleSprites.Coffin) sprite).creak();
		}
		if (Dungeon.level.heroFOV[pos]){
			CellEmitter.get( t.pos ).burst( ShadowParticle.CURSE, 6 );
			Sample.INSTANCE.play( Assets.Sounds.BONES );
			GLog.w( Messages.get( Coffin.class, "creak" ) );
		}
		return true;
	}

	@Override
	public String description() {
		return Messages.get( Coffin.class, "desc" )
				+ "\n\n" + Messages.get( Coffin.class, dormant() ? "state_dark" : "state_lit" );
	}
}
