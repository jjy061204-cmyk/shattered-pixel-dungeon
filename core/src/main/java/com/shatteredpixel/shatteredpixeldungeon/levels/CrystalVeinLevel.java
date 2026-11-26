/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 갈림길 테마: 수정 광맥 (11~14층) — 드워프들이 캐다 깨운 수정 생명체가 뒤덮은 광맥
 *  - 타일은 원작 광산(16px) 바탕에, 원작 수정 채굴 퀘스트의 푸른·초록·붉은 수정이 구석구석 침식한 모습
 *    통째로 수정: 수정맥 벽(벽면·테두리), 결정 바닥(특수 바닥), 수정 석상, 수정 기둥(칸마다 색이 다름)
 *    스며든 수정: 바위 결·벽 밑동·문 옆·책장·자갈 틈·사다리·우물·받침대·방책, 긴 풀 대신 결정 덤불, 풀 대신 수정 가루
 *  - 방·몹·아이템 수는 원작 광산과 같은 예산. 트롤 대장장이 퀘스트는 그대로
 *  - 아직 타일·지형만: 몹(수정 위습·결정화된 놀·프리즘 술사…), 마법 탄 반사, 수정 DM-300은 차례로 채울 예정
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.HashMap;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.CrystalVeinPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;

public class CrystalVeinLevel extends CavesLevel {

	{
		color1 = 0x2a3048;
		color2 = 0x66b3ff;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CRYSTAL_VEIN;
	}

	@Override
	protected Painter painter() {
		return new CrystalVeinPainter()
				.setWater(feeling == Feeling.WATER ? 0.85f : 0.30f, 6)
				.setGrass(feeling == Feeling.GRASS ? 0.40f : 0.05f, 3)
				.setTraps(nTraps(), trapClasses(), trapChances());
	}

	// ───── 수정 기둥: 엄폐물이면서 두 번 내리치거나 폭발에 부서짐 ─────
	//  부서지면 둘레 8칸에 파편 피해와 1턴 눈부심, 그 소리에 가까운 몹들이 몰려옴(공명)

	public static boolean isPillar( int t ){
		return t == Terrain.REGION_DECO || t == Terrain.REGION_DECO_ALT;
	}

	private HashMap<Integer, Integer> pillarHits = new HashMap<>();

	public void hitPillar( int cell ){
		int h = (pillarHits.containsKey(cell) ? pillarHits.get(cell) : 0) + 1;
		if (h >= 2){
			pillarHits.remove(cell);
			shatterPillar(cell);
		} else {
			pillarHits.put(cell, h);
			Splash.at(cell, 0x66B3FF, 4);
			Sample.INSTANCE.play(Assets.Sounds.SHATTER, 0.5f, 1.4f);
			GLog.i(Messages.get(CrystalVeinLevel.class, "pillar_crack"));
		}
	}

	public static class PillarShard {}

	public static void shatterPillar( int cell ){
		Level level = Dungeon.level;
		if (level instanceof CrystalVeinLevel) ((CrystalVeinLevel) level).pillarHits.remove(cell);

		Level.set(cell, Terrain.EMPTY_DECO);
		for (int i : PathFinder.NEIGHBOURS9){
			if (cell + i >= 0 && cell + i < level.length()) GameScene.updateMap(cell + i);
		}

		if (level.heroFOV[cell]){
			Splash.at(cell, 0x66B3FF, 14);
			Splash.at(cell, 0xFFFFFF, 6);
			CellEmitter.center(cell).burst(Speck.factory(Speck.STAR), 6);
			PixelScene.shake(1, 0.4f);
		}
		Sample.INSTANCE.play(Assets.Sounds.SHATTER);

		//둘레 8칸 파편 피해 (같은 층 함정 수준) + 1턴 눈부심
		int depth = Dungeon.scalingDepth();
		for (int i : PathFinder.NEIGHBOURS8){
			Char ch = Actor.findChar(cell + i);
			if (ch == null || !ch.isAlive()) continue;
			int dmg = Random.NormalIntRange(2 + depth/2, 6 + depth) - ch.drRoll();
			if (dmg > 0) ch.damage(dmg, new PillarShard());
			if (ch.isAlive()){
				Buff.prolong(ch, Blindness.class, 1f);
			} else if (ch == Dungeon.hero){
				Dungeon.fail(new PillarShard());
				GLog.n(Messages.get(CrystalVeinLevel.class, "pillar_kill"));
			}
		}

		//공명: 깨지는 소리에 가까운 적들이 그쪽으로
		for (Mob m : level.mobs.toArray(new Mob[0])){
			if (m.alignment == Char.Alignment.ENEMY && level.distance(m.pos, cell) <= 8){
				m.beckon(cell);
			}
		}
		if (level.heroFOV[cell]) GLog.w(Messages.get(CrystalVeinLevel.class, "pillar_shatter"));
		Dungeon.observe();
	}

	private static final String PILLAR_CELLS = "pillar_cells";
	private static final String PILLAR_HITS  = "pillar_hits";

	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle(bundle);
		int[] c = new int[pillarHits.size()], h = new int[pillarHits.size()];
		int k = 0;
		for (Integer key : pillarHits.keySet()){ c[k] = key; h[k] = pillarHits.get(key); k++; }
		bundle.put(PILLAR_CELLS, c);
		bundle.put(PILLAR_HITS, h);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle(bundle);
		pillarHits.clear();
		if (bundle.contains(PILLAR_CELLS)){
			int[] c = bundle.getIntArray(PILLAR_CELLS), h = bundle.getIntArray(PILLAR_HITS);
			for (int k = 0; k < c.length; k++) pillarHits.put(c[k], h[k]);
		}
	}

	public static boolean isVein(){
		return Dungeon.level instanceof CrystalVeinLevel;
	}

	//수정 기둥 색: 원작 수정 채굴 퀘스트처럼 칸마다 푸른(절반)·초록·붉은 수정 중 하나, 모양도 둘 중 하나
	private static int pillarVariance( int pos ){
		byte[] v = DungeonTileSheet.tileVariance;
		return (v == null || pos < 0 || pos >= v.length) ? 99 : v[pos];
	}
	private static int pillarColor( int pos ){
		int v = pillarVariance( pos );
		return v < 25 ? 1 : v < 50 ? 2 : 0;
	}
	public static int pillarRaised( int pos ){
		return DungeonTileSheet.RAISED_MINE_CRYSTAL_BLUE_1 + 2*pillarColor( pos ) + pillarVariance( pos ) % 2;
	}
	public static int pillarOverhang( int pos ){
		return DungeonTileSheet.MINE_CRYSTAL_OVERHANG_BLUE + pillarColor( pos );
	}
	public static int pillarFlat( int pos ){
		return DungeonTileSheet.FLAT_MINE_CRYSTAL + pillarColor( pos );
	}

	//수정이 번져 나오는 칸: 수정맥 벽·수정 기둥
	private static boolean crystalSource( int t ){
		return t == Terrain.WALL_DECO || t == Terrain.REGION_DECO || t == Terrain.REGION_DECO_ALT;
	}
	private static boolean plainWall( int t ){
		return t == Terrain.WALL || t == Terrain.SECRET_DOOR;
	}
	//가로로 가장 가까운 수정맥 벽까지 거리 (같은 줄, 4칸까지)
	public static int veinDistance( int pos, int[] map, int w ){
		int x = pos % w;
		for (int d = 1; d <= 4; d++){
			if (x - d >= 0 && map[pos - d] == Terrain.WALL_DECO) return d;
			if (x + d < w && map[pos + d] == Terrain.WALL_DECO) return d;
		}
		return 99;
	}
	//수정이 번져 나오는 칸이 둘레 2칸 안에 있는지
	public static boolean nearCrystal( int pos, int[] map, int w ){
		int x = pos % w, y = pos / w, h = map.length / w;
		for (int dy = -2; dy <= 2; dy++){
			for (int dx = -2; dx <= 2; dx++){
				int xx = x + dx, yy = y + dy;
				if (xx < 0 || yy < 0 || xx >= w || yy >= h) continue;
				if (crystalSource(map[xx + yy * w])) return true;
			}
		}
		return false;
	}

	//위 1 · 오른쪽 2 · 아래 4 · 왼쪽 8 → 시트의 빈 칸 15개
	private static final int[] FRINGE = {-1, 138, 139, 140, 141, 142, 143, 247, 248, 249, 250, 251, 252, 253, 254, 255};
	public static int floorFringe( int pos, int[] map, int w ){
		int size = map.length;
		int x = pos % w;
		int b = 0;
		int up = pos - w;
		if (up >= 0){
			int t = map[up];
			//위가 수정맥이거나, 수정맥 옆 이음 벽(바위가 수정에 먹혀 드는 벽)
			if (crystalSource(t)
					|| (plainWall(t) && ((x > 0 && map[up - 1] == Terrain.WALL_DECO) || (x < w - 1 && map[up + 1] == Terrain.WALL_DECO)))){
				b |= 1;
			}
		}
		if (x < w - 1 && crystalSource(map[pos + 1])) b |= 2;
		if (pos + w < size && crystalSource(map[pos + w])) b |= 4;
		if (x > 0 && crystalSource(map[pos - 1])) b |= 8;
		if (b != 0) return FRINGE[b];
		//수정에서 먼 바닥은 원작 광산 바닥 그대로(드문 수정 변형을 쓰지 않음), 가까우면 드문 변형이 더 자주
		int v = DungeonTileSheet.tileVariance == null ? 99 : DungeonTileSheet.tileVariance[pos];
		if (nearCrystal(pos, map, w)) {
			return v < 25 ? DungeonTileSheet.FLOOR_ALT_2 : (v < 62 ? DungeonTileSheet.FLOOR_ALT_1 : DungeonTileSheet.FLOOR);
		}
		return v < 50 ? DungeonTileSheet.FLOOR_ALT_1 : DungeonTileSheet.FLOOR;
	}

	@Override
	public String entryText() {
		return Dungeon.depth == 11 ? Messages.get( this, "entry" ) : null;
	}

	@Override
	public String tileName( int tile ) {
		String key = tileKey( tile );
		return key != null ? Messages.get( CrystalVeinLevel.class, key + "_name" ) : super.tileName( tile );
	}

	@Override
	public String tileDesc( int tile ) {
		String key = tileKey( tile );
		return key != null ? Messages.get( CrystalVeinLevel.class, key + "_desc" ) : super.tileDesc( tile );
	}

	private static String tileKey( int tile ){
		switch (tile){
			case Terrain.WALL_DECO:       return "wall_deco";
			case Terrain.EMPTY_DECO:      return "floor_deco";
			case Terrain.GRASS:           return "grass";
			case Terrain.HIGH_GRASS:      return "high_grass";
			case Terrain.FURROWED_GRASS:  return "furrowed_grass";
			case Terrain.EMPTY_SP:        return "floor_sp";
			case Terrain.STATUE:          return "statue";
			case Terrain.STATUE_SP:       return "statue_sp";
			case Terrain.REGION_DECO:     return "region_deco";
			case Terrain.REGION_DECO_ALT: return "region_deco_alt";
			case Terrain.WATER:           return "water";
			case Terrain.ENTRANCE:
			case Terrain.ENTRANCE_SP:     return "entrance";
			case Terrain.EXIT:            return "exit";
			default:                      return null;
		}
	}
}
