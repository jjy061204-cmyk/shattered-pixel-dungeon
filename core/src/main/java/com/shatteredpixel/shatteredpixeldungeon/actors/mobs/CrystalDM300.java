/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 수정 DM-300: 수정 광맥 15층 보스. 수정에 잠식되어 다시 움직이는 DM-300
 *  - 원작 DM-300의 틀(추격 · 낙석 · 66%/33% 전력 기둥 과충전)은 그대로
 *  - 독가스 대신 수정 첨탑(대장장이 채굴 퀘스트)의 결정 분출: 영웅 쪽 직선 또는 영웅 둘레 마름모로,
 *    빛나는 칸으로 미리 알려 준 뒤 솟아올라 피해와 밀쳐내기. 체력이 줄수록 분출이 여러 겹으로 번짐
 *  - 과충전 중에도 몇 턴마다 영웅 둘레에 마름모 분출
 *  - 분출한 결정은 바로 부서져 사라짐 (지형으로 남지 않음: 곡괭이가 없으니)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalVeinSprites;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CrystalDM300 extends DM300 {

	{
		spriteClass = CrystalVeinSprites.DM300.class;
	}

	//과충전 중 마름모 분출 간격 (행동 횟수, 과충전 중엔 한 턴에 두 번 행동하므로 약 4턴)
	private int chargedEruptCD = 8;

	@Override
	protected boolean act() {
		if (supercharged && paralysed <= 0 && Dungeon.hero.isAlive() && Dungeon.hero.invisible <= 0
				&& fieldOfView != null && fieldOfView.length == Dungeon.level.length() && fieldOfView[Dungeon.hero.pos]){
			if (--chargedEruptCD <= 0){
				chargedEruptCD = 8;
				launch(diamondWaves(Dungeon.hero.pos, 1));
			}
		}
		return super.act();
	}

	//원작의 독가스 자리에 결정 분출
	@Override
	public void ventGas( Char target ) {
		Dungeon.hero.interrupt();
		int extra = HP < HT/3 ? 2 : HP < 2*HT/3 ? 1 : 0;
		if (Random.Int(2) == 0){
			launch(lineWaves(target, extra));
		} else {
			launch(diamondWaves(target.pos, extra));
		}
	}

	private ArrayList<ArrayList<Integer>> lineWaves( Char target, int extra ){
		ArrayList<ArrayList<Integer>> waves = new ArrayList<>();
		ArrayList<Integer> cells = new ArrayList<>();
		Ballistica aim = new Ballistica(pos, target.pos, Ballistica.WONT_STOP);
		for (int i : aim.subPath(1, 8)){
			if (Dungeon.level.solid[i]) break;
			cells.add(i);
		}
		waves.add(new ArrayList<>(cells));
		for (int k = 0; k < extra; k++){
			cells.addAll(spread(cells));
			waves.add(new ArrayList<>(cells));
		}
		return waves;
	}

	private ArrayList<ArrayList<Integer>> diamondWaves( int center, int extra ){
		ArrayList<ArrayList<Integer>> waves = new ArrayList<>();
		ArrayList<Integer> cells = new ArrayList<>();
		cells.add(center);
		cells.addAll(spread(cells));
		waves.add(new ArrayList<>(cells));
		for (int k = 0; k < extra; k++){
			cells.addAll(spread(cells));
			waves.add(new ArrayList<>(cells));
		}
		return waves;
	}

	private ArrayList<Integer> spread( ArrayList<Integer> current ){
		ArrayList<Integer> out = new ArrayList<>();
		for (int i : current){
			for (int j : PathFinder.NEIGHBOURS4){
				int c = i + j;
				if (c >= 0 && c < Dungeon.level.length() && !Dungeon.level.solid[c]
						&& !out.contains(c) && !current.contains(c)){
					out.add(c);
				}
			}
		}
		return out;
	}

	private void launch( ArrayList<ArrayList<Integer>> waves ){
		if (waves.isEmpty() || waves.get(0).isEmpty()) return;
		float delay = GameMath.gate(TICK, (int)Math.ceil(Dungeon.hero.cooldown()), 3*TICK);
		for (int i : waves.get(0)){
			GameScene.targetedCell(i, delay);
		}
		Actor.addDelayed(new Eruption(this, waves), delay);
	}

	public static class CrystalSpike {}

	//예고한 칸에 결정이 솟아 피해를 주고 곧 부서짐 (지형은 그대로)
	public static class Eruption extends Actor {

		{ actPriority = VFX_PRIO; }

		private final CrystalDM300 boss;
		private final ArrayList<ArrayList<Integer>> waves;

		Eruption( CrystalDM300 boss, ArrayList<ArrayList<Integer>> waves ){
			this.boss = boss;
			this.waves = waves;
		}

		@Override
		protected boolean act() {
			if (waves.isEmpty() || boss == null || !boss.isAlive()){
				Actor.remove(this);
				return true;
			}
			ArrayList<Integer> cells = waves.remove(0);
			boolean stronger = Dungeon.isChallenged(Challenges.STRONGER_BOSSES);

			for (int i : cells){
				if (Dungeon.level.heroFOV[i]){
					Splash.at(i, 0x66B3FF, 5);
					CellEmitter.get(i).burst(Speck.factory(Speck.STAR), 1);
				}
			}
			for (int i : cells){
				Char ch = Actor.findChar(i);
				if (ch == null || ch instanceof DM300 || ch instanceof Pylon) continue;

				int dmg = stronger ? Random.NormalIntRange(8, 18) : Random.NormalIntRange(6, 15);
				ch.damage(dmg, new CrystalSpike());

				//보스에게서 먼 쪽으로 밀쳐냄 (수정 첨탑처럼)
				int movePos = i;
				if (!Char.hasProp(ch, Char.Property.IMMOVABLE)){
					for (int j : PathFinder.NEIGHBOURS8){
						int c = i + j;
						if (!Dungeon.level.solid[c] && Actor.findChar(c) == null
								&& Dungeon.level.trueDistance(c, boss.pos) > Dungeon.level.trueDistance(movePos, boss.pos)){
							movePos = c;
						}
					}
				}
				if (ch.isAlive()){
					if (movePos != i){
						Actor.add(new Pushing(ch, i, movePos));
						ch.pos = movePos;
						Dungeon.level.occupyCell(ch);
					}
				} else if (ch == Dungeon.hero){
					GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", boss.name())) );
					Dungeon.fail(boss);
				}
			}

			PixelScene.shake(1, 0.6f);
			Sample.INSTANCE.play(Assets.Sounds.SHATTER);

			if (!waves.isEmpty()){
				for (int i : waves.get(0)){
					GameScene.targetedCell(i, TICK);
				}
				spend(TICK);
			} else {
				Actor.remove(this);
			}
			return true;
		}
	}
}
