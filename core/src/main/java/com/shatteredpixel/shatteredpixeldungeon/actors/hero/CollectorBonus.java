/*
 * Shattered Pixel Dungeon 모드: 유물수집가의 유물별 고유 효과
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.UnstableSpellbook;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.RuneAegis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

//유물수집가는 장착한 유물마다, 그 유물이 원래 쓰이는 방식을 조금 더 끌어낸 추가 효과를 얻음.
//어느 하나가 유독 강하지 않도록 모두 소소한 수준으로 맞춤.
// - 대도의 팔찌: 적의 아이템 드롭률 1.25배
// - 그림자의 망토: 충전 하나로 은신을 5턴 유지 (원래 4턴)
// - 풍요의 뿔: 뿔의 음식을 먹으면 최대 체력의 8% 회복
// - 피의 성배: 성배의 재생이 15% 더 빠르고, 적이 쓰러질 때마다 체력 조금 회복
// - 말린 장미: 유령의 최대 체력 +20%, 회피 +15%
// - 에테르 사슬: 이동·끌어오기의 충전 소모 1 감소(최소 1), 끌려온 적은 3턴 불구
// - 자연의 신발: 신발의 힘을 쓰면 주변에 키 큰 풀이 자람
// - 예지의 부적: 점을 친 뒤 10턴 안에 받는 첫 공격을 피함
// - 시간지기의 모래시계: 시간 정지가 충전당 2.5턴(원래 2턴), 정체가 충전당 6턴(원래 5턴)
// - 불안정한 마법서: 책을 읽으면 마법 막대와 유물이 2턴씩 충전
// - 연금술사의 도구: 60턴마다 연금술 에너지 1
// - 해골 열쇠: 수정 자물쇠를 여는 충전 4(원래 5), 잠긴 공기의 벽이 40% 더 오래 감
// - 수호의 반지: 갑옷의 보호막 최대치 +15%
public class CollectorBonus {

	//유물수집가가 저주받지 않은 그 유물을 장착하고 있는지
	public static boolean active( Hero hero, Class<? extends Artifact> cls ){
		if (hero == null || hero.heroClass != HeroClass.COLLECTOR) return false;
		Artifact a = hero.belongings.getItem( cls );
		return a != null && a.isEquipped( hero ) && !a.cursed;
	}

	private static int level( Hero hero, Class<? extends Artifact> cls ){
		Artifact a = hero.belongings.getItem( cls );
		return a == null ? 0 : Math.max( 0, a.level() );
	}

	private static void heal( Hero hero, int amount ){
		if (amount <= 0 || hero.HP >= hero.HT) return;
		amount = Math.min( amount, hero.HT - hero.HP );
		hero.HP += amount;
		if (hero.sprite != null){
			hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( amount ), FloatingText.HEALING );
		}
	}

	public static class ChainStrikeTracker extends FlavourBuff {}
	public static class ForesightDodge extends FlavourBuff {}

	//유물을 사용한 순간 (Talent.onArtifactUsed와 같은 자리에서 불림)
	public static void onUse( Hero hero, Class<? extends Artifact> cls ){
		if (!active( hero, cls )) return;
		int lvl = level( hero, cls );

		if (cls == HornOfPlenty.class){
			heal( hero, Math.max( 1, Math.round( hero.HT * 0.08f ) ) );
		} else if (cls == SandalsOfNature.class){
			growGrass( hero, 3 + lvl );
		} else if (cls == TalismanOfForesight.class){
			Buff.affect( hero, ForesightDodge.class, 10f );
		} else if (cls == UnstableSpellbook.class){
			Buff.affect( hero, Recharging.class, 2f );
			Buff.affect( hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge.class ).extend( 2f );
		}
	}

	//해골 열쇠: 주변의 숨겨진 문과 함정을 드러냄
	private static void revealSecrets( Hero hero, int radius ){
		boolean found = false;
		for (int c = 0; c < Dungeon.level.length(); c++){
			if (Dungeon.level.secret[c] && Dungeon.level.distance( hero.pos, c ) <= radius){
				int old = Dungeon.level.map[c];
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.discoverTile( c, old );
				Dungeon.level.discover( c );
				com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping.discover( c );
				found = true;
			}
		}
		if (found){
			com.watabou.noosa.audio.Sample.INSTANCE.play( com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.SECRET );
			Dungeon.observe();
		}
	}

	//예지의 부적: 내다본 공격 한 번을 피함. 피했으면 true
	public static boolean foreseenDodge( Hero hero ){
		ForesightDodge d = hero.buff( ForesightDodge.class );
		if (d == null) return false;
		d.detach();
		return active( hero, TalismanOfForesight.class );
	}

	//피의 성배: 재생 속도
	public static float chaliceRegenMultiplier( Hero hero ){
		return active( hero, ChaliceOfBlood.class ) ? 1.15f : 1f;
	}

	//에테르 사슬: 충전 소모
	public static int chainCost( Hero hero, int distance ){
		return active( hero, EtherealChains.class ) ? Math.max( 1, distance - 1 ) : distance;
	}

	//에테르 사슬: 끌려온 적을 옭아맴
	public static void onChainPull( Hero hero, Char enemy ){
		if (active( hero, EtherealChains.class ) && enemy != null && enemy.isAlive()){
			Buff.prolong( enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class, 3f );
		}
	}

	//시간지기의 모래시계
	public static float hourglassTurnsPerCharge( Hero hero ){
		return active( hero, TimekeepersHourglass.class ) ? 2.5f : 2f;
	}
	public static int hourglassStasisTurns( Hero hero ){
		return active( hero, TimekeepersHourglass.class ) ? 6 : 5;
	}

	//그림자의 망토
	public static int cloakTurnsPerCharge( Hero hero ){
		return active( hero, CloakOfShadows.class ) ? 5 : 4;
	}

	//해골 열쇠
	public static int crystalLockCost( Hero hero ){
		return active( hero, SkeletonKey.class ) ? 4 : 5;
	}
	public static int keyWallDuration( Hero hero ){
		return active( hero, SkeletonKey.class ) ? 14 : 10;
	}

	//연금술사의 도구: 60턴마다 연금술 에너지 1
	public static class ToolkitDrip extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff {
		{ revivePersists = true; }
	}
	public static void toolkitTick( Hero hero ){
		if (!active( hero, AlchemistsToolkit.class )) return;
		ToolkitDrip drip = Buff.affect( hero, ToolkitDrip.class );
		drip.countUp( 1 );
		if (drip.count() >= 60){
			drip.countDown( 60 );
			Dungeon.energy++;
			if (hero.sprite != null){
				hero.sprite.showStatusWithIcon( 0x44CCFF, "1", FloatingText.ENERGY );
			}
		}
	}

	//자연의 신발: 주변 빈 땅에 키 큰 풀이 자람
	private static void growGrass( Hero hero, int amount ){
		java.util.ArrayList<Integer> cells = new java.util.ArrayList<>();
		for (int n : com.watabou.utils.PathFinder.NEIGHBOURS8){
			int c = hero.pos + n;
			int t = Dungeon.level.map[c];
			if ((t == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY
					|| t == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY_DECO
					|| t == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMBERS
					|| t == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.GRASS
					|| t == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.FURROWED_GRASS)
					&& Dungeon.level.plants.get( c ) == null && Dungeon.level.traps.get( c ) == null){
				cells.add( c );
			}
		}
		Random.shuffle( cells );
		for (int i = 0; i < amount && i < cells.size(); i++){
			int c = cells.get( i );
			com.shatteredpixel.shatteredpixeldungeon.levels.Level.set( c, com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.HIGH_GRASS );
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap( c );
			com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( c ).burst(
					com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle.LEVEL_SPECIFIC, 4 );
		}
		if (!cells.isEmpty()) Dungeon.observe();
	}

	//근접 공격: 에테르 사슬
	public static int onAttack( Hero hero, Char enemy, int dmg ){
		ChainStrikeTracker t = hero.buff( ChainStrikeTracker.class );
		if (t != null){
			t.detach();

		}
		return dmg;
	}

	//적이 쓰러짐: 피의 성배
	public static void onEnemySlain( Hero hero ){
		if (active( hero, ChaliceOfBlood.class )){
			heal( hero, 1 + level( hero, ChaliceOfBlood.class ) / 3 );
		}
	}

	public static void onPotionUsed( Hero hero ){}

	//이동 속도: 그림자의 망토
	public static float speedMultiplier( Hero hero ){
		return 1f;
	}

	//방어력: 말린 장미 (유령이 층에 있을 때)
	public static int drBonus( Hero hero ){
		if (true) return 0;   //장미의 효과는 유령 능력치 강화로 바뀜
		for (Mob m : Dungeon.level.mobs){
			if (m instanceof DriedRose.GhostHero && m.isAlive()){
				return Random.NormalIntRange( 1, 3 );
			}
		}
		return 0;
	}

	//드롭률: 대도의 팔찌
	public static float lootMultiplier( Hero hero ){
		return active( hero, MasterThievesArmband.class ) ? 1.25f : 1f;
	}

	//수호의 반지: 보호막 최대치
	public static float aegisShieldMultiplier( Hero hero ){
		return active( hero, RuneAegis.class ) ? 1.15f : 1f;
	}

	//유물 설명 아래에 붙는 고유 효과 문구 (유물수집가일 때만)
	public static String info( Hero hero, Item artifact ){
		if (hero == null || hero.heroClass != HeroClass.COLLECTOR) return null;
		String key = artifact.getClass().getSimpleName().toLowerCase();
		String txt = Messages.get( CollectorBonus.class, key );
		if (txt == null || txt.equals( Messages.NO_TEXT_FOUND )) return null;
		return txt;
	}
}
