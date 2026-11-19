/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 경보 북
 *  - 놀 척후병은 영웅을 보면 가장 가까운 북으로 달려가 2턴 동안 침
 *  - 북이 울리면 10칸 안의 놀이 모두 북 쪽으로 모여듦
 *  - 영웅도 바로 옆에서 칠 수 있고(놀을 끌어들임), 멀리서 맞혀도 울림
 *  - 공격 세 번이면 부서지고, 바로 옆에서 가죽을 찢으면 소리 없이 망가뜨릴 수 있음
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpire;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class WarDrum extends GnollProp {

	{
		spriteClass = GnollEmpireSprites.Drum.class;
		HP = HT = 3;
	}

	@Override
	public boolean interact( Char c ) {
		if (c instanceof Hero){
			Hero hero = (Hero) c;
			showOptions( hero, description() + "\n\n" + Messages.get( this, "prompt" ),
					Messages.get( this, "beat" ), Messages.get( this, "tear" ) );
		}
		return false;
	}

	@Override
	protected void choose( Hero hero, int index ) {
		hero.sprite.operate( pos );
		if (index == 0){
			//북을 쳐서 놀을 이리로 부름
			boom();
			hero.spendAndNext( 1f );
		} else {
			//가죽을 찢어 조용히 망가뜨림
			Sample.INSTANCE.play( Assets.Sounds.TRAMPLE );
			GLog.i( Messages.get( this, "torn" ) );
			HP = 0;
			die( hero );
			hero.spendAndNext( 2f );
		}
	}

	//멀리서 맞혀도 북이 울림 (부서질 만큼 맞으면 울리지 않고 부서짐)
	@Override
	protected void onHit( Object src ) {
		if (HP > 0 && !(src instanceof Burning)){
			boom();
		}
	}

	@Override
	protected void onBroken( Object cause ) {
		if (Dungeon.level.heroFOV[pos]){
			CellEmitter.get( pos ).burst( Speck.factory( Speck.WOOL ), 8 );
			GLog.i( Messages.get( this, "broken" ) );
		}
	}

	//둥둥: 북 둘레 10칸 안의 놀이 모두 북 쪽으로 모여듦
	public void boom(){
		if (sprite instanceof GnollEmpireSprites.Drum) ((GnollEmpireSprites.Drum) sprite).beat();
		Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 1f, 0.6f );
		Sample.INSTANCE.playDelayed( Assets.Sounds.HIT_CRUSH, 0.25f, 1f, 0.55f );
		CellEmitter.center( pos ).burst( Speck.factory( Speck.NOTE ), 4 );

		boolean heard = Dungeon.level.distance( Dungeon.hero.pos, pos ) <= GnollEmpire.DRUM_RANGE + 4;
		if (Dungeon.level.heroFOV[pos] || heard){
			PixelScene.shake( 1, 0.3f );
			GLog.w( Messages.get( this, Dungeon.level.heroFOV[pos] ? "boom" : "boom_far" ) );
			if (sprite != null) sprite.showStatus( CharSprite.WARNING, Messages.get( this, "boom_short" ) );
			Dungeon.hero.interrupt();
		}

		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m != this && m.isAlive() && m.alignment == Alignment.ENEMY && GnollEmpire.isGnoll( m )
					&& Dungeon.level.distance( pos, m.pos ) <= GnollEmpire.DRUM_RANGE){
				m.beckon( pos );
			}
		}
	}

	//척후병이 치기 시작할 때 (아직 울리지 않음)
	public void thump(){
		if (sprite instanceof GnollEmpireSprites.Drum) ((GnollEmpireSprites.Drum) sprite).beat();
		if (Dungeon.level.heroFOV[pos]){
			Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 0.6f, 0.7f );
		}
	}
}
