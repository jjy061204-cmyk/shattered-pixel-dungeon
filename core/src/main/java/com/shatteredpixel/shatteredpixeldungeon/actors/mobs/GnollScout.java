/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 놀 척후병 (원작 광산의 박쥐 자리)
 *  - 빠르고 회피가 높음. 박쥐와 같은 전리품(치유 물약)을 떨어뜨림
 *  - 영웅을 보면 10칸 안의 가장 가까운 경보 북으로 달려가 2턴 동안 침 (한 번만)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.levels.GnollEmpire;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollEmpireSprites;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class GnollScout extends Mob {

	{
		spriteClass = GnollEmpireSprites.Scout.class;

		HP = HT = 30;
		defenseSkill = 17;
		baseSpeed = 2f;

		EXP = 7;
		maxLvl = 15;

		loot = PotionOfHealing.class;
		lootChance = 0.1667f; //박쥐와 같음, lootChance() 참고

		HUNTING = new ScoutHunting();
	}

	//놀 대족장이 부른 증원병: 경험치·전리품이 없고 북을 찾지 않음
	public boolean minion = false;

	private boolean alarmRaised = false;
	private int beatTurns = 0;

	public void makeMinion(){
		minion = true;
		maxLvl = -2;
		EXP = 0;
		properties.add( Property.BOSS_MINION );
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 5, 17 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 16;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 4 );
	}

	@Override
	public float lootChance() {
		if (minion) return 0;
		return super.lootChance() * ((7f - Dungeon.LimitedDrops.BAT_HP.count) / 7f);
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.BAT_HP.count++;
		return super.createLoot();
	}

	//이미 북을 울렸거나, 겁에 질렸거나, 증원병이면 북을 찾지 않음
	private WarDrum drumToBeat(){
		if (alarmRaised || minion || rooted || buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror.class ) != null) return null;
		return GnollEmpire.nearestDrum( pos, GnollEmpire.DRUM_RANGE );
	}

	private class ScoutHunting extends Mob.Hunting {

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			WarDrum drum = drumToBeat();
			if (drum != null){
				enemySeen = enemyInFOV;

				if (Dungeon.level.adjacent( pos, drum.pos )){
					return beat( drum );
				}

				int spot = GnollEmpire.drumSpot( drum, pos );
				int oldPos = pos;
				if (spot != -1 && getCloser( spot )){
					if (beatTurns == 0 && Dungeon.level.heroFOV[pos]){
						beatTurns = -1; //한 번만 알림
						if (sprite != null) sprite.showStatus( CharSprite.WARNING, Messages.get( GnollScout.class, "running" ) );
						GLog.w( Messages.get( GnollScout.class, "runs_to_drum" ) );
					}
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				} else {
					//북까지 갈 수 없으면 그냥 싸움
					alarmRaised = true;
				}
			}
			return super.act( enemyInFOV, justAlerted );
		}
	}

	private boolean beat( final WarDrum drum ){
		if (beatTurns < 0) beatTurns = 0;
		beatTurns++;
		if (beatTurns >= 2){
			drum.boom();
			alarmRaised = true;
			beatTurns = 0;
		} else {
			drum.thump();
			if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[drum.pos]){
				GLog.w( Messages.get( GnollScout.class, "beating" ) );
				Dungeon.hero.interrupt();
			}
		}
		spend( 1f );
		if (sprite != null && (sprite.visible || drum.sprite.visible)){
			sprite.attack( drum.pos, new Callback() {
				@Override
				public void call() {
					sprite.idle();
					next();
				}
			} );
			return false;
		}
		return true;
	}

	@Override
	public String description() {
		String d = super.description();
		if (minion) d += "\n\n" + Messages.get( this, "desc_minion" );
		return d;
	}

	private static final String ALARM = "alarm_raised";
	private static final String BEAT = "beat_turns";
	private static final String MINION = "minion";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ALARM, alarmRaised );
		bundle.put( BEAT, beatTurns );
		bundle.put( MINION, minion );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		alarmRaised = bundle.getBoolean( ALARM );
		beatTurns = bundle.getInt( BEAT );
		if (bundle.getBoolean( MINION )) makeMinion();
	}
}
