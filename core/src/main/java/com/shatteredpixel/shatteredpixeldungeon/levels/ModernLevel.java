/*
 * Shattered Pixel Dungeon 모드: 현대 세계 층
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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.ModernClerk;
import com.shatteredpixel.shatteredpixeldungeon.items.DimensionTicket;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Torch;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.BananaMilk;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Bonster;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Bungeoppang;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Butter;
import com.shatteredpixel.shatteredpixeldungeon.items.food.CupCoffee;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pudding;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StrawberryCake;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StrawberryMilk;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.audio.Music;

import java.util.ArrayList;

//현대 세계 층.
//- 차원 이동 카드로만 들어오고, 귀환 카드로만 나갈 수 있음 (계단 없음)
//- 귀환의 신호기, 워프 신호기 등 다른 층간 순간이동은 막혀 있음 (Dungeon.interfloorTeleportAllowed 참고)
//- 큰길을 따라 편의점, 카페, 약국, 서점, 철물점, 옷가게가 있고, 가운데 아래에 분수 광장이 있음
public class ModernLevel extends Level {

	//현대 세계 층이 쓰는 분기 번호 (0: 본 던전, 1: 광산·금고)
	public static final int BRANCH = 9;

	{
		color1 = 0x8899aa;
		color2 = 0xf0f0e8;
	}

	//지도 기호
	// # 벽   + 문   = 간판(장식 벽)   . 가게 바닥   , 도로   _ 보도
	// L 가로등   V 자판기   T 쓰레기통   b 벤치   " 화단   ~ 분수   B 책장   @ 도착 지점
	private static final String[] MAP = {
		"#################################",
		"#.......#.......#.......#.......#",
		"#.......#.......#.......#.......#",
		"#.......#.......#.......#.......#",
		"#.......#.......#.......#.......#",
		"##=#+#=###=#+#=###=#+#=###=#+#=##",
		"#_L___V_L_____T_L_______L_T___L_#",
		"#,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,#",
		"#,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,#",
		"#_L___T_L_______________L_V___L_#",
		"##=#+#=##_______________#=##+##=#",
		"#.......#\"_____________\"#.......#",
		"#.......#__\"___~~~___\"__#.......#",
		"#.......#__\"___~~~___\"__#.......#",
		"#.......#___b___@___b___#.......#",
		"#.......#\"_____________\"#.......#",
		"#################################",
	};

	//가게 안쪽 왼쪽 위 좌표 (x, y), 안쪽 크기는 가로 7 x 세로 4~5
	private static final int[][] TOP_SHOPS = { {1, 1}, {9, 1}, {17, 1}, {25, 1} };
	private static final int[][] BOTTOM_SHOPS = { {1, 11}, {25, 11} };

	private int arrival = -1;

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_MODERN;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_MODERN;
	}

	@Override
	public void playLevelMusic() {
		Music.INSTANCE.play( Assets.Music.THEME_1, true );
	}

	@Override
	protected boolean build() {

		int w = MAP[0].length();
		//지도 줄 길이가 어긋나 있으면 부족한 칸을 벽으로 채움
		for (String row : MAP) w = Math.max(w, row.length());
		setSize( w, MAP.length );

		for (int y = 0; y < height(); y++) {
			String row = MAP[y];
			for (int x = 0; x < width(); x++) {
				char c = x < row.length() ? row.charAt(x) : '#';
				int cell = x + y * width();
				switch (c) {
					case '#': default: map[cell] = Terrain.WALL; break;
					case '=': map[cell] = Terrain.WALL_DECO; break;
					case '+': map[cell] = Terrain.DOOR; break;
					case '.': map[cell] = Terrain.EMPTY_SP; break;
					case ',': map[cell] = Terrain.EMPTY; break;
					case '_': map[cell] = Terrain.EMPTY_DECO; break;
					case 'L': map[cell] = Terrain.STATUE; break;
					case '"': map[cell] = Terrain.GRASS; break;
					case '~': map[cell] = Terrain.WATER; break;
					case 'B': map[cell] = Terrain.BOOKSHELF; break;
					case 'V': map[cell] = Terrain.STATUE_SP; break;
					case 'T': map[cell] = Terrain.REGION_DECO; break;
					case 'b': map[cell] = Terrain.REGION_DECO_ALT; break;
					case '@':
						map[cell] = Terrain.EMPTY_DECO;
						arrival = cell;
						break;
				}
			}
		}

		//서점(위쪽 네 번째 가게) 뒤쪽 벽을 따라 책장
		int bx = TOP_SHOPS[3][0], by = TOP_SHOPS[3][1];
		for (int x = bx; x < bx + 7; x++) {
			if (x != bx + 3) map[x + by * width()] = Terrain.BOOKSHELF;
		}

		//도로 위 중앙선과 횡단보도
		ModernRoad road = new ModernRoad();
		road.setRect( 1, 7, ModernRoad.ROAD_W, 2 );
		customTiles.add( road );

		//가장자리는 항상 벽
		for (int x = 0; x < width(); x++) {
			map[x] = Terrain.WALL;
			map[x + (height()-1) * width()] = Terrain.WALL;
		}
		for (int y = 0; y < height(); y++) {
			map[y * width()] = Terrain.WALL;
			map[width() - 1 + y * width()] = Terrain.WALL;
		}

		return arrival != -1;
	}

	//계단이 없으므로, 게임이 "입구"를 찾을 때는 저장되지 않는 가짜 입구(도착 지점)를 돌려줌.
	//transitions 목록은 비어 있으므로 밟아도 아무 일도 일어나지 않음.
	@Override
	public LevelTransition getTransition( LevelTransition.Type type ) {
		LevelTransition t = super.getTransition( type );
		if (t != null) return t;
		return new LevelTransition( this, arrivalCell(), LevelTransition.Type.BRANCH_ENTRANCE,
				Dungeon.depth, 0, LevelTransition.Type.BRANCH_EXIT );
	}

	private int arrivalCell(){
		if (arrival != -1) return arrival;
		//불러온 뒤에는 지도에서 다시 계산
		int x = 16, y = 14;
		return x + y * width();
	}

	//==== 점원 ====

	@Override
	protected void createMobs() {
		ModernClerk.Shop[] top = { ModernClerk.Shop.CONVENIENCE, ModernClerk.Shop.CAFE,
				ModernClerk.Shop.PHARMACY, ModernClerk.Shop.BOOKSTORE };
		for (int i = 0; i < TOP_SHOPS.length; i++) {
			//가게 안쪽 맨 뒤 줄 가운데
			addClerk( top[i], TOP_SHOPS[i][0] + 3, TOP_SHOPS[i][1] );
		}
		//아래쪽 가게는 안쪽 맨 아래 줄 가운데
		addClerk( ModernClerk.Shop.HARDWARE, BOTTOM_SHOPS[0][0] + 3, BOTTOM_SHOPS[0][1] + 4 );
		addClerk( ModernClerk.Shop.CLOTHING, BOTTOM_SHOPS[1][0] + 3, BOTTOM_SHOPS[1][1] + 4 );
	}

	private void addClerk( ModernClerk.Shop shop, int x, int y ){
		ModernClerk clerk = new ModernClerk( shop );
		clerk.pos = x + y * width();
		mobs.add( clerk );
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	//==== 상품 ====

	@Override
	protected void createItems() {
		int floorSet = Math.min( 4, Dungeon.depth / 5 );

		//편의점
		ArrayList<Item> conv = new ArrayList<>();
		conv.add( new BananaMilk() );
		conv.add( new StrawberryMilk() );
		conv.add( new CupCoffee() );
		conv.add( new Bonster() );
		conv.add( new Pudding() );
		conv.add( new Bungeoppang().quantity(2) );
		conv.add( new Butter() );
		conv.add( new Food() );
		conv.add( new Torch() );
		conv.add( new DimensionTicket() );
		stockTopShop( 0, conv );

		//카페
		ArrayList<Item> cafe = new ArrayList<>();
		cafe.add( new CupCoffee().quantity(2) );
		cafe.add( new StrawberryCake() );
		cafe.add( new Pudding() );
		cafe.add( new StrawberryMilk() );
		cafe.add( new BananaMilk() );
		stockTopShop( 1, cafe );

		//약국: 치유 물약은 이름이 적혀 있음(감정됨)
		ArrayList<Item> pharm = new ArrayList<>();
		pharm.add( new PotionOfHealing().identify() );
		pharm.add( new PotionOfHealing().identify() );
		for (int i = 0; i < 3; i++) {
			pharm.add( Generator.randomUsingDefaults( Generator.Category.POTION ) );
		}
		stockTopShop( 2, pharm );

		//서점
		ArrayList<Item> books = new ArrayList<>();
		books.add( new ScrollOfIdentify() );
		books.add( new ScrollOfIdentify() );
		books.add( new ScrollOfRemoveCurse() );
		for (int i = 0; i < 2; i++) {
			books.add( Generator.randomUsingDefaults( Generator.Category.SCROLL ) );
		}
		books.add( Generator.randomUsingDefaults( Generator.Category.STONE ) );
		stockTopShop( 3, books );

		//철물점: 폭탄, 투척 무기, 근접 무기
		ArrayList<Item> hard = new ArrayList<>();
		hard.add( new Bomb() );
		hard.add( new Bomb() );
		MissileWeapon m = Generator.randomMissile( floorSet, true );
		m.enchant( null ); m.cursed = false; m.level( 0 ); m.identify( false );
		hard.add( m );
		MeleeWeapon w = Generator.randomWeapon( floorSet, true );
		w.enchant( null ); w.cursed = false; w.level( 0 ); w.identify( false );
		hard.add( w );
		hard.add( new Torch() );
		stockBottomShop( 0, hard );

		//옷가게: 방어구
		ArrayList<Item> clothes = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			Armor a = Generator.randomArmor( floorSet );
			a.inscribe( null ); a.cursed = false; a.level( 0 ); a.identify( false );
			clothes.add( a );
		}
		stockBottomShop( 1, clothes );
	}

	//위쪽 가게: 점원(맨 뒤 줄 가운데)과 문 앞 통로(가운데 세로줄, 맨 앞 줄)를 비우고 진열
	private void stockTopShop( int shop, ArrayList<Item> items ){
		int left = TOP_SHOPS[shop][0], top = TOP_SHOPS[shop][1];
		ArrayList<Integer> spots = new ArrayList<>();
		for (int y = top; y < top + 3; y++) {
			for (int x = left; x < left + 7; x++) {
				if (x == left + 3) continue;
				int cell = x + y * width();
				if (map[cell] == Terrain.EMPTY_SP) spots.add( cell );
			}
		}
		place( items, spots );
	}

	//아래쪽 가게: 점원이 맨 아래 줄에 있으므로 위쪽 세 줄에 진열 (문 바로 아래 가운데 줄은 비움)
	private void stockBottomShop( int shop, ArrayList<Item> items ){
		int left = BOTTOM_SHOPS[shop][0], top = BOTTOM_SHOPS[shop][1];
		ArrayList<Integer> spots = new ArrayList<>();
		for (int y = top + 1; y < top + 4; y++) {
			for (int x = left; x < left + 7; x++) {
				if (x == left + 3) continue;
				int cell = x + y * width();
				if (map[cell] == Terrain.EMPTY_SP) spots.add( cell );
			}
		}
		place( items, spots );
	}

	private void place( ArrayList<Item> items, ArrayList<Integer> spots ){
		for (int i = 0; i < items.size() && i < spots.size(); i++) {
			Item item = items.get(i);
			if (item == null) continue;
			drop( item, spots.get(i) ).type = Heap.Type.FOR_SALE;
		}
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		return arrivalCell();
	}

	//==== 타일 이름과 설명 ====

	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.EMPTY:       return Messages.get(ModernLevel.class, "road_name");
			case Terrain.EMPTY_DECO:  return Messages.get(ModernLevel.class, "sidewalk_name");
			case Terrain.EMPTY_SP:    return Messages.get(ModernLevel.class, "shop_floor_name");
			case Terrain.STATUE:      return Messages.get(ModernLevel.class, "lamp_name");
			case Terrain.STATUE_SP:   return Messages.get(ModernLevel.class, "vending_name");
			case Terrain.REGION_DECO: return Messages.get(ModernLevel.class, "trash_name");
			case Terrain.REGION_DECO_ALT: return Messages.get(ModernLevel.class, "bench_name");
			case Terrain.WATER:       return Messages.get(ModernLevel.class, "fountain_name");
			case Terrain.GRASS:       return Messages.get(ModernLevel.class, "flowerbed_name");
			case Terrain.WALL:        return Messages.get(ModernLevel.class, "wall_name");
			case Terrain.WALL_DECO:   return Messages.get(ModernLevel.class, "sign_name");
			case Terrain.DOOR:
			case Terrain.OPEN_DOOR:   return Messages.get(ModernLevel.class, "door_name");
			default:                  return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
			case Terrain.EMPTY:       return Messages.get(ModernLevel.class, "road_desc");
			case Terrain.EMPTY_DECO:  return Messages.get(ModernLevel.class, "sidewalk_desc");
			case Terrain.EMPTY_SP:    return Messages.get(ModernLevel.class, "shop_floor_desc");
			case Terrain.STATUE:      return Messages.get(ModernLevel.class, "lamp_desc");
			case Terrain.STATUE_SP:   return Messages.get(ModernLevel.class, "vending_desc");
			case Terrain.REGION_DECO: return Messages.get(ModernLevel.class, "trash_desc");
			case Terrain.REGION_DECO_ALT: return Messages.get(ModernLevel.class, "bench_desc");
			case Terrain.WATER:       return Messages.get(ModernLevel.class, "fountain_desc");
			case Terrain.GRASS:       return Messages.get(ModernLevel.class, "flowerbed_desc");
			case Terrain.WALL:        return Messages.get(ModernLevel.class, "wall_desc");
			case Terrain.WALL_DECO:   return Messages.get(ModernLevel.class, "sign_desc");
			case Terrain.DOOR:
			case Terrain.OPEN_DOOR:   return Messages.get(ModernLevel.class, "door_desc");
			default:                  return super.tileDesc( tile );
		}
	}

	//도로 위에 덧그리는 중앙선과 횡단보도 (31칸 x 2칸 그림)
	public static class ModernRoad extends CustomTilemap {

		public static final int ROAD_W = 31;

		{
			texture = Assets.Environment.MODERN_ROAD;
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( mapSimpleImage( 0, 0, 512 ), tileW );
			return v;
		}
	}
}
