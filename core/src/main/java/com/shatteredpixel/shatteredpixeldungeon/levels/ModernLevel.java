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
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Hoodie;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherJacket;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PaddedJacket;
import com.shatteredpixel.shatteredpixeldungeon.items.modern.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crowbar;
import com.shatteredpixel.shatteredpixeldungeon.items.food.BananaMilk;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Bonster;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Bungeoppang;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Butter;
import com.shatteredpixel.shatteredpixeldungeon.items.food.CupCoffee;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pudding;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StrawberryCake;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StrawberryMilk;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;

//현대 세계 층.
//- 차원 이동 카드로만 들어오고, 귀환 카드로만 나갈 수 있음 (계단 없음)
//- 귀환의 신호기, 워프 신호기 등 다른 층간 순간이동은 막혀 있음 (Dungeon.interfloorTeleportAllowed 참고)
//- 큰길 위쪽에 편의점·카페·금은방·약국·서점, 아래쪽에 철물점·분수 광장·옷가게
//- 도착 지점 바로 위, 횡단보도 건너편이 금은방 (금화를 코인으로 바꾸는 곳)
//- 물건은 바닥에 늘어놓지 않고, 계산대 점원에게 말을 걸어 메뉴판에서 삼 (ModernClerk 참고)
//- 가게마다 던전 물건 대신 현대 물품을 팖 (items/modern 참고)
public class ModernLevel extends Level {

	//현대 세계 층이 쓰는 분기 번호 (0: 본 던전, 1: 광산·금고)
	public static final int BRANCH = 9;

	{
		color1 = 0x8899aa;
		color2 = 0xf0f0e8;
	}

	//==== 바깥 지도 ====
	// # 벽   + 유리문   s 간판(가게 이름판)   . 가게 바닥   , 도로   _ 보도
	// L 가로등   V 자판기   T 쓰레기통   b 벤치   " 화단   ~ 분수   @ 도착 지점
	// 가게 안쪽(10x7)은 아래 SHOP_LAYOUTS 로 따로 채움
	private static final String[] MAP = {
		"########################################################",
		"#..........#..........#..........#..........#..........#",
		"#..........#..........#..........#..........#..........#",
		"#..........#..........#..........#..........#..........#",
		"#..........#..........#..........#..........#..........#",
		"#..........#..........#..........#..........#..........#",
		"#..........#..........#..........#..........#..........#",
		"#..........#..........#..........#..........#..........#",
		"####s+#########s+#########s+#########s+#########s+######",
		"#_________V____________________V_____________________V_#",
		"#_L______L____T_____L_______________L___T______L_____L_#",
		"#,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,#",
		"#,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,#",
		"#_L______L____L__________________________L____L______L_#",
		"#_______T__V________________________________V__T_______#",
		"####s+######________________________________####s+######",
		"#..........#\"______________________________\"#..........#",
		"#..........#__\"___L__________________L___\"__#..........#",
		"#..........#______________~~~~______________#..........#",
		"#..........#___b__________~~~~__________b___#..........#",
		"#..........#________________________________#..........#",
		"#..........#__\"___b________@________b____\"__#..........#",
		"#..........#\"______T________________T______\"#..........#",
		"########################################################",
	};

	//가게 안쪽 왼쪽 위 좌표 (위쪽 5곳, 아래쪽 2곳)
	private static final int[][] SHOP_POS = {
			{1, 1}, {12, 1}, {23, 1}, {34, 1}, {45, 1},     //편의점, 카페, 금은방, 약국, 서점
			{1, 16}, {45, 16}                              //철물점, 옷가게
	};
	private static final ModernClerk.Shop[] SHOP_TYPES = {
			ModernClerk.Shop.CONVENIENCE, ModernClerk.Shop.CAFE, ModernClerk.Shop.JEWELRY,
			ModernClerk.Shop.PHARMACY, ModernClerk.Shop.BOOKSTORE,
			ModernClerk.Shop.HARDWARE, ModernClerk.Shop.CLOTHING
	};

	//==== 가게 안쪽 배치 (가로 10 x 세로 7) ====
	// . 바닥   @ 점원(계산대 자리)
	// C 계산대  R 계산대+포스기  E 계산대+커피머신  F 음료 냉장고  N 과자 진열대  U 컵라면 진열대
	// M 약 진열대  Q 약 진열대(2)  K 책장  W 공구 진열판  H 옷걸이  Y 마네킹  T 카페 탁자
	// I 키오스크  P 화분  Z 잡지 진열대  S 케이크 진열장  A 갑옷 진열대  O 커피 바
	// G 보석 유리 계산대  J 보석 진열장  X 금고  B 계산대+저울  D 금 시세 전광판
	private static final String[][] SHOP_LAYOUTS = {
		{ //편의점: 뒤쪽 냉장고, 진열대 두 줄, 문 옆 계산대
			"FFFF..FFFF",
			"..........",
			".NN..UU.Z.",
			"..........",
			".NN..UU...",
			"..........",
			"CR@C.....P",
		},
		{ //카페: 커피 바, 계산대와 케이크 진열장, 탁자, 키오스크
			"OO.P..P.OO",
			"CE@CS.....",
			"..........",
			".T..T..T..",
			"..........",
			".T.....T.I",
			"P........P",
		},
		{ //금은방: 금고와 보석 진열장, 가운데 유리 계산대와 저울, 금 시세 전광판
			"XJJJ..JJJD",
			"..........",
			"..GB@GG...",
			"..........",
			".G......G.",
			"..........",
			"P........P",
		},
		{ //약국: 약 진열대 벽, 가운데 상담 계산대
			"MQMQ..QMQM",
			"..........",
			"CCC@CC....",
			"..........",
			".M......M.",
			"..........",
			"P........P",
		},
		{ //서점: 책장 벽과 서가, 문 옆 계산대
			"KKKK..KKKK",
			"..........",
			".KK....KK.",
			"..........",
			".KK....KK.",
			"..........",
			"C@C......Z",
		},
		{ //철물점 (문이 위쪽): 공구 진열판, 안쪽 계산대
			"..........",
			".WW....WW.",
			"..........",
			".WW....WW.",
			"..........",
			"..........",
			"AC@C....WA",
		},
		{ //옷가게 (문이 위쪽): 마네킹, 옷걸이, 안쪽 계산대
			"..........",
			"Y.HH..HH.Y",
			"..........",
			"..HH..HH..",
			"..........",
			"..........",
			"YC@C....AY",
		},
	};

	//집기 그림 번호 (modern_props.png 안의 칸 번호)
	private static final HashMap<Character, Integer> PROP_IMG = new HashMap<>();
	static {
		PROP_IMG.put('C', 0);  PROP_IMG.put('R', 1);  PROP_IMG.put('E', 2);  PROP_IMG.put('F', 3);
		PROP_IMG.put('N', 4);  PROP_IMG.put('U', 5);  PROP_IMG.put('M', 6);  PROP_IMG.put('K', 7);
		PROP_IMG.put('W', 8);  PROP_IMG.put('H', 9);  PROP_IMG.put('Y', 10); PROP_IMG.put('T', 11);
		PROP_IMG.put('I', 12); PROP_IMG.put('P', 13); PROP_IMG.put('Z', 14); PROP_IMG.put('S', 15);
		PROP_IMG.put('A', 16); PROP_IMG.put('O', 17); PROP_IMG.put('Q', 18);
		PROP_IMG.put('G', 19); PROP_IMG.put('J', 20); PROP_IMG.put('X', 21); PROP_IMG.put('B', 22);
		PROP_IMG.put('D', 23);
	}
	//간판 그림 번호 (SIGN_IMG + 가게 종류 번호)
	private static final int SIGN_IMG = 32;

	private int arrival = -1;
	private int[] clerkPos = new int[SHOP_POS.length];
	private ModernProps props;

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

		int w = 0;
		for (String row : MAP) w = Math.max(w, row.length());
		setSize( w, MAP.length );

		props = new ModernProps();
		props.setRect( 0, 0, width(), height() );
		props.data = new int[width() * height()];
		java.util.Arrays.fill( props.data, -1 );

		//바깥 지도
		for (int y = 0; y < height(); y++) {
			String row = MAP[y];
			for (int x = 0; x < width(); x++) {
				char c = x < row.length() ? row.charAt(x) : '#';
				int cell = x + y * width();
				switch (c) {
					case '#': default: map[cell] = Terrain.WALL; break;
					case '+': map[cell] = Terrain.DOOR; break;
					case '.': map[cell] = Terrain.EMPTY_SP; break;
					case ',': map[cell] = Terrain.EMPTY; break;
					case '_': map[cell] = Terrain.EMPTY_DECO; break;
					case 'L': map[cell] = Terrain.STATUE; break;
					case 'V': map[cell] = Terrain.STATUE_SP; break;
					case 'T': map[cell] = Terrain.REGION_DECO; break;
					case 'b': map[cell] = Terrain.REGION_DECO_ALT; break;
					case '"': map[cell] = Terrain.GRASS; break;
					case '~': map[cell] = Terrain.WATER; break;
					case 's':
						//가게 이름판 자리 (그림은 아래에서 가게 종류에 맞춰 덧그림)
						map[cell] = Terrain.WALL;
						break;
					case '@':
						map[cell] = Terrain.EMPTY_DECO;
						arrival = cell;
						break;
				}
			}
		}

		//가게 안쪽
		for (int s = 0; s < SHOP_POS.length; s++) {
			int left = SHOP_POS[s][0], top = SHOP_POS[s][1];
			String[] layout = SHOP_LAYOUTS[s];
			clerkPos[s] = -1;
			//간판: 위쪽 가게는 가게 아래 벽, 아래쪽 가게는 가게 위 벽의 왼쪽에서 네 번째 칸
			int signY = top < height() / 2 ? top + layout.length : top - 1;
			props.data[(left + 3) + signY * width()] = SIGN_IMG + SHOP_TYPES[s].ordinal();
			for (int y = 0; y < layout.length; y++) {
				for (int x = 0; x < layout[y].length(); x++) {
					char c = layout[y].charAt(x);
					int cell = (left + x) + (top + y) * width();
					if (c == '@') {
						clerkPos[s] = cell;
					} else if (PROP_IMG.containsKey(c)) {
						map[cell] = Terrain.CUSTOM_DECO;      //지나갈 수 없는 집기
						props.data[cell] = PROP_IMG.get(c);
					}
				}
			}
		}
		customTerrain.add( props );

		//도로 위 중앙선, 횡단보도, 맨홀
		ModernRoad road = new ModernRoad();
		road.setRect( 1, 11, width() - 2, 2 );
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
		//예전 버전(가로 33칸)으로 만들어진 층
		if (width() == 33) return 16 + 14 * width();
		if (width() == 45) return 22 + 21 * width();
		return 27 + 21 * width();
	}

	//==== 점원과 상품 ====

	private ModernClerk[] clerks = new ModernClerk[SHOP_POS.length];

	@Override
	protected void createMobs() {
		for (int s = 0; s < SHOP_POS.length; s++) {
			if (clerkPos[s] < 0) continue;
			ModernClerk clerk = new ModernClerk( SHOP_TYPES[s] );
			clerk.pos = clerkPos[s];
			mobs.add( clerk );
			clerks[s] = clerk;
		}
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	private ModernClerk clerk( ModernClerk.Shop type ){
		for (int s = 0; s < SHOP_TYPES.length; s++){
			if (SHOP_TYPES[s] == type) return clerks[s];
		}
		return null;
	}

	@Override
	protected void createItems() {

		//편의점: 음료 하나는 1+1 행사, 차원 이동 카드도 판매
		ModernClerk conv = clerk( ModernClerk.Shop.CONVENIENCE );
		if (conv != null) {
			ArrayList<Item> drinks = new ArrayList<>();
			drinks.add( new BananaMilk() ); drinks.add( new StrawberryMilk() );
			drinks.add( new CupCoffee() );  drinks.add( new Bonster() );
			drinks.add( new IonDrink() );   drinks.add( new BottledWater().quantity(2) );
			int onePlusOne = Random.Int( drinks.size() );
			for (int i = 0; i < drinks.size(); i++) {
				conv.addStock( drinks.get(i), i == onePlusOne ? ModernClerk.DEAL_ONE_PLUS_ONE : ModernClerk.DEAL_NONE );
			}
			conv.addStock( new TriangleKimbap().quantity(2), ModernClerk.DEAL_NONE );
			conv.addStock( new CupRamen(), ModernClerk.DEAL_NONE );
			conv.addStock( new ChocoBar().quantity(2), ModernClerk.DEAL_NONE );
			conv.addStock( new Pudding(), ModernClerk.DEAL_NONE );
			conv.addStock( new Bungeoppang().quantity(2), ModernClerk.DEAL_NONE );
			conv.addStock( new Butter(), ModernClerk.DEAL_NONE );
			conv.addStock( new DimensionTicket(), ModernClerk.DEAL_NONE );
		}

		//카페: 오늘의 메뉴 하나 30% 할인
		ModernClerk cafe = clerk( ModernClerk.Shop.CAFE );
		if (cafe != null) {
			ArrayList<Item> menu = new ArrayList<>();
			menu.add( new CupCoffee() ); menu.add( new CafeLatte() ); menu.add( new BubbleTea() );
			menu.add( new StrawberryCake() ); menu.add( new Croissant() ); menu.add( new Macaron().quantity(2) );
			menu.add( new Pudding() ); menu.add( new Parfait() );
			int today = Random.Int( menu.size() );
			for (int i = 0; i < menu.size(); i++) {
				cafe.addStock( menu.get(i), i == today ? ModernClerk.DEAL_30_OFF : ModernClerk.DEAL_NONE );
			}
		}

		//약국: 현대 의약품
		ModernClerk pharm = clerk( ModernClerk.Shop.PHARMACY );
		if (pharm != null) {
			pharm.addStock( new Bandage().quantity(3), ModernClerk.DEAL_NONE );
			pharm.addStock( new Painkiller().quantity(2), ModernClerk.DEAL_NONE );
			pharm.addStock( new Antidote(), ModernClerk.DEAL_NONE );
			pharm.addStock( new PainPatch().quantity(2), ModernClerk.DEAL_NONE );
			pharm.addStock( new EyeDrops(), ModernClerk.DEAL_NONE );
		}

		//서점: 백과사전(던지는 두꺼운 책), 문제집, 만화책
		ModernClerk books = clerk( ModernClerk.Shop.BOOKSTORE );
		if (books != null) {
			books.addStock( new Encyclopedia(), ModernClerk.DEAL_NONE );
			books.addStock( new Encyclopedia(), ModernClerk.DEAL_NONE );
			books.addStock( new Workbook(), ModernClerk.DEAL_NONE );
			books.addStock( new ComicBook().quantity(2), ModernClerk.DEAL_NONE );
		}

		//철물점: 손전등, 호루라기, 덕트 테이프, 소화기, 쇠지렛대
		ModernClerk hard = clerk( ModernClerk.Shop.HARDWARE );
		if (hard != null) {
			hard.addStock( new Flashlight(), ModernClerk.DEAL_NONE );
			hard.addStock( new Whistle(), ModernClerk.DEAL_NONE );
			hard.addStock( new DuctTape().quantity(2), ModernClerk.DEAL_NONE );
			hard.addStock( new FireExtinguisher().quantity(2), ModernClerk.DEAL_NONE );
			Crowbar bar = new Crowbar();
			bar.enchant( null ); bar.cursed = false; bar.level( 0 ); bar.identify( false );
			hard.addStock( bar, ModernClerk.DEAL_NONE );
		}

		//옷가게: 시즌 오프, 전 품목 20% 할인
		ModernClerk clothes = clerk( ModernClerk.Shop.CLOTHING );
		if (clothes != null) {
			Armor[] wear = { new Hoodie(), new PaddedJacket(), new LeatherJacket() };
			for (Armor a : wear) {
				a.inscribe( null ); a.cursed = false; a.level( 0 ); a.identify( false );
				clothes.addStock( a, ModernClerk.DEAL_20_OFF );
			}
		}
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		return arrivalCell();
	}

	//==== 저장 ====

	private static final String ARRIVAL = "modern_arrival";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ARRIVAL, arrival );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		arrival = bundle.contains( ARRIVAL ) ? bundle.getInt( ARRIVAL ) : -1;
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

	//==== 덧그림 ====

	//가게 집기와 간판 (지도 전체 크기, -1 칸은 그리지 않음)
	public static class ModernProps extends CustomTilemap {

		{
			texture = Assets.Environment.MODERN_PROPS;
		}

		public int[] data;

		//그림 번호 -> 이름 열쇠
		private static final String[] KEYS = {
				"counter", "register", "espresso", "fridge", "snack_shelf", "noodle_shelf", "med_shelf", "book_shelf",
				"tool_rack", "clothes_rack", "mannequin", "cafe_table", "kiosk", "plant", "magazine", "cake_showcase",
				"armor_stand", "coffee_bar", "med_shelf",
				"jewel_counter", "jewel_cabinet", "safe", "scale_counter", "price_board"
		};
		//가게 종류 순서 (ModernClerk.Shop)
		private static final String[] SIGN_KEYS = {
				"sign_convenience", "sign_cafe", "sign_pharmacy", "sign_bookstore", "sign_hardware", "sign_clothing",
				"sign_jewelry"
		};

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			if (data != null) v.map( data, tileW );
			return v;
		}

		//그림이 없는 칸(-1)은 그림 없음으로 처리 (칸 살펴보기에서 오류가 나지 않도록)
		@Override
		public com.watabou.noosa.Image image( int tileX, int tileY ) {
			if (data == null) return null;
			int i = tileX + tileY * tileW;
			if (i < 0 || i >= data.length || data[i] < 0) return null;
			return super.image( tileX, tileY );
		}

		private String key( int tileX, int tileY ){
			if (data == null) return null;
			int i = tileX + tileY * tileW;
			if (i < 0 || i >= data.length) return null;
			int img = data[i];
			if (img >= 0 && img < KEYS.length) return KEYS[img];
			if (img >= SIGN_IMG && img < SIGN_IMG + SIGN_KEYS.length) return SIGN_KEYS[img - SIGN_IMG];
			return null;
		}

		@Override
		public String name( int tileX, int tileY ) {
			String k = key( tileX, tileY );
			return k == null ? null : Messages.get( ModernLevel.class, "prop_" + k + "_name" );
		}

		@Override
		public String desc( int tileX, int tileY ) {
			String k = key( tileX, tileY );
			return k == null ? null : Messages.get( ModernLevel.class, "prop_" + k + "_desc" );
		}

		private static final String DATA = "prop_data";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			if (data != null) bundle.put( DATA, data );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			if (bundle.contains( DATA )) data = bundle.getIntArray( DATA );
		}
	}

	//도로 위에 덧그리는 중앙선, 횡단보도, 맨홀 (54칸 x 2칸 그림)
	public static class ModernRoad extends CustomTilemap {

		public static final int ROAD_W = 54;

		{
			texture = Assets.Environment.MODERN_ROAD;
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( mapSimpleImage( 0, 0, 1024 ), tileW );
			return v;
		}
	}
}
