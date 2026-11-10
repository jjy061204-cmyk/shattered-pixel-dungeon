/*
 * Shattered Pixel Dungeon 모드: 이차원의 성소 보스 - 성소의 거대 골렘
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.GoldSparkle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GolemHeart;
import com.shatteredpixel.shatteredpixeldungeon.levels.SanctuaryBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ColossusSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//성소를 지키는 거대 골렘.
//- 처음엔 왕좌 위 석상처럼 잠들어 있다가 영웅이 투기장에 들어오면 깨어남
//- 몇 턴마다 바닥을 내려쳐 영웅 주변에 돌이 떨어짐 (2턴 뒤, 표시된 칸)
//- 체력 2/3, 1/3에서 받침대에서 석상들을 불러냄. 1/2 아래로는 더 빠르게 공격
public class SanctuaryColossus extends Mob {

	{
		spriteClass = ColossusSprite.class;

		HP = HT = 450;
		defenseSkill = 18;

		EXP = 60;
		maxLvl = -2;

		state = PASSIVE;

		properties.add( Property.BOSS );
		properties.add( Property.INORGANIC );
	}

	private boolean awake = false;
	private int quakeCD = 4;
	private int summons = 0;

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 25, 38 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 34;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 14 );
	}

	private boolean enraged(){
		return HP < HT / 2;
	}

	@Override
	public float attackDelay() {
		return super.attackDelay() * (enraged() ? 0.75f : 1f);
	}

	public boolean isAwake(){
		return awake;
	}

	//투기장에 들어온 영웅을 보고 깨어남
	public void awaken(){
		if (awake) return;
		awake = true;
		state = HUNTING;
		enemy = Dungeon.hero;
		target = Dungeon.hero.pos;
		BossHealthBar.assignBoss( this );
		yell( Messages.get( this, "notice" ) );
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		PixelScene.shake( 2, 0.8f );
		if (sprite != null) sprite.emitter().burst( GoldSparkle.BURST, 20 );
		if (Dungeon.level instanceof SanctuaryBossLevel){
			((SanctuaryBossLevel) Dungeon.level).onFightStart();
		}
	}

	@Override
	protected boolean act() {
		if (!awake){
			spend( TICK );
			return true;
		}

		if (quakeCD > 0){
			quakeCD--;
		} else if (Dungeon.hero.isAlive() && Dungeon.level.heroFOV[pos]
				&& Dungeon.level.distance( pos, Dungeon.hero.pos ) <= 8){
			quake();
			return true;
		}
		return super.act();
	}

	//바닥 내려치기: 영웅 자리와 그 둘레에 돌이 떨어질 칸 표시
	private void quake(){
		ArrayList<Integer> cells = new ArrayList<>();
		cells.add( Dungeon.hero.pos );
		ArrayList<Integer> around = new ArrayList<>();
		for (int i = 0; i < Dungeon.level.length(); i++){
			if (Dungeon.level.passable[i] && Dungeon.level.distance( i, Dungeon.hero.pos ) <= 2
					&& i != Dungeon.hero.pos && i != pos){
				around.add( i );
			}
		}
		int extra = enraged() ? 7 : 5;
		while (extra-- > 0 && !around.isEmpty()){
			cells.add( around.remove( Random.Int( around.size() ) ) );
		}
		Buff.append( this, QuakeRocks.class, 2f ).setRockPositions( cells );

		if (sprite != null) sprite.zap( Dungeon.hero.pos );
		PixelScene.shake( 2, 0.5f );
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		GLog.w( Messages.get( this, "quake" ) );

		quakeCD = enraged() ? 4 : 6;
		spend( TICK );
	}

	@Override
	public void damage( int dmg, Object src ) {
		if (!awake) awaken();
		super.damage( dmg, src );
		if (!isAlive()) return;
		if (summons == 0 && HP < HT * 2 / 3){
			summons = 1;
			summon( 2, false );
		} else if (summons == 1 && HP < HT / 3){
			summons = 2;
			summon( 2, true );
		}
	}

	//받침대에서 석상을 불러냄
	private void summon( int count, boolean withSentinel ){
		yell( Messages.get( this, "summon" ) );
		ArrayList<Integer> spots = new ArrayList<>();
		if (Dungeon.level instanceof SanctuaryBossLevel){
			for (int c : ((SanctuaryBossLevel) Dungeon.level).summonCells()){
				if (Actor.findChar( c ) == null && Dungeon.level.passable[c]) spots.add( c );
			}
		}
		if (spots.isEmpty()){
			for (int n : PathFinder.NEIGHBOURS8){
				int c = pos + n;
				if (Actor.findChar( c ) == null && Dungeon.level.passable[c]) spots.add( c );
			}
		}
		for (int i = 0; i < count && !spots.isEmpty(); i++){
			int c = spots.remove( Random.Int( spots.size() ) );
			Mob m = (withSentinel && i == 0) ? new RuneSentinel() : new StoneSoldier();
			m.pos = c;
			m.state = m.HUNTING;
			GameScene.add( m, 1f );
			CellEmitter.get( c ).burst( Speck.factory( Speck.ROCK ), 8 );
			if (m.sprite != null) m.sprite.emitter().burst( GoldSparkle.BURST, 8 );
		}
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
	}

	@Override
	public void die( Object cause ) {
		super.die( cause );

		//보상: 골렘의 심장 + 성소의 보물
		ArrayList<Item> drops = new ArrayList<>();
		drops.add( new GolemHeart() );
		drops.add( Generator.random( Random.Int( 2 ) == 0 ? Generator.Category.WEAPON : Generator.Category.ARMOR ) );
		drops.add( Generator.random( Random.Int( 2 ) == 0 ? Generator.Category.RING : Generator.Category.WAND ) );
		drops.add( Generator.random( Generator.Category.ARTIFACT ) );
		for (Item it : drops){
			if (it == null) continue;
			int cell = pos;
			for (int t = 0; t < 10; t++){
				int c = pos + PathFinder.NEIGHBOURS9[Random.Int( 9 )];
				if (Dungeon.level.passable[c]) { cell = c; break; }
			}
			Dungeon.level.drop( it, cell ).sprite.drop( pos );
		}

		//남아 있는 석상들도 함께 무너짐
		for (Mob m : Dungeon.level.mobs.toArray( new Mob[0] )){
			if (m != this && (m instanceof StoneSoldier || m instanceof RuneSentinel || m instanceof StoneWarden)){
				m.die( null );
			}
		}

		GameScene.bossSlain();
		yell( Messages.get( this, "defeated" ) );
		if (Dungeon.level instanceof SanctuaryBossLevel){
			((SanctuaryBossLevel) Dungeon.level).onBossDefeated();
		}
	}

	@Override
	public void notice() {
		//투기장에 들어올 때까지는 반응하지 않음 (레벨이 깨움)
		if (awake) super.notice();
	}

	//2턴 뒤 떨어지는 돌
	public static class QuakeRocks extends DelayedRockFall {
		@Override
		public void affectChar( Char ch ) {
			if (ch instanceof SanctuaryColossus) return;
			ch.damage( Random.NormalIntRange( 12, 20 ), this );
			if (ch.isAlive()){
				Buff.prolong( ch, Cripple.class, 3f );
			} else if (ch == Dungeon.hero){
				Dungeon.fail( target );
				GLog.n( Messages.get( SanctuaryColossus.class, "rock_kill" ) );
			}
		}
	}

	private static final String AWAKE = "awake";
	private static final String QUAKE = "quake_cd";
	private static final String SUMMONS = "summons";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( AWAKE, awake );
		bundle.put( QUAKE, quakeCD );
		bundle.put( SUMMONS, summons );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		awake = bundle.getBoolean( AWAKE );
		quakeCD = bundle.getInt( QUAKE );
		summons = bundle.getInt( SUMMONS );
		if (awake) BossHealthBar.assignBoss( this );
	}
}
