/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 핏빛 권속: 진조의 왕관을 쓴 뱀파이어가 '권속 소환'으로 부르는 핏빛 박쥐
 *  - 빠르게 날며 적을 물고, 빤 피의 일부는 자신과 왕(뱀파이어)에게 돌아감
 *  - 15턴(+권속 특성 점수마다 5턴) 뒤 핏물로 흩어짐
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrownBatSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class CrownBat extends DirectableAlly {

	{
		spriteClass = CrownBatSprite.class;
		HP = HT = 12;
		baseSpeed = 2f;
		flying = true;
		immunities.add( AllyBuff.class );
	}

	private int pts = 1;
	private float timeLeft = 20f;

	private static int lvl(){
		return Dungeon.hero != null ? Dungeon.hero.lvl : 1;
	}

	public void setup( int pts ){
		this.pts = Math.max( 1, pts );
		HP = HT = 8 + 2 * lvl() + 3 * this.pts;
		timeLeft = 15f + 5f * this.pts;
	}

	@Override
	public int attackSkill( Char target ) {
		return 12 + lvl() + lvl() / 2;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 8 + lvl();
	}

	@Override
	public int damageRoll() {
		int l = lvl();
		return Math.round( Random.NormalIntRange( 1 + l / 4, 4 + l / 2 ) * (0.9f + 0.1f * pts) );
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		//빤 피: 자신을 피해의 절반, 왕을 1/5만큼 치유
		int self = Math.min( Math.max( 1, damage / 2 ), HT - HP );
		if (self > 0){
			HP += self;
			if (sprite != null) sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( self ), FloatingText.HEALING );
		}
		Hero hero = Dungeon.hero;
		if (hero != null && hero.isAlive()){
			int give = Math.min( Math.max( 1, damage / 5 ), hero.HT - hero.HP );
			if (give > 0){
				hero.HP += give;
				if (hero.sprite != null) hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( give ), FloatingText.HEALING );
			}
		}
		return damage;
	}

	@Override
	protected boolean act() {
		if (timeLeft <= 0){
			die( null );
			return true;
		}
		return super.act();
	}

	@Override
	protected void spend( float time ) {
		super.spend( time );
		timeLeft -= time;
	}

	@Override
	public void die( Object cause ) {
		flying = false;
		if (sprite != null) sprite.emitter().burst( BloodParticle.BURST, 10 );
		super.die( cause );
	}

	@Override
	public String description() {
		return Messages.get( this, "desc" ) + "\n\n" + Messages.get( this, "desc_time", (int) Math.ceil( Math.max( 0, timeLeft ) ) );
	}

	private static final String PTS = "pts", TIME = "time_left";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( PTS, pts );
		bundle.put( TIME, timeLeft );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		pts = bundle.getInt( PTS );
		timeLeft = bundle.getFloat( TIME );
	}
}
