/*
 * Shattered Pixel Dungeon 모드: 수호의 반지 (유물)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RuneAegisForm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RuneAegisShield;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.AegisAssemble;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;

//수호의 반지 (클래스 이름은 옛 저장 호환을 위해 그대로 둠).
// - 작동: 마법 갑옷을 둘러, 줄지 않는 보호막을 얻고 매 턴 재생. 5턴마다 충전 1 소모 (그림자의 망토처럼 켜고 끔)
public class RuneAegis extends Artifact {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.RUNE_AEGIS;

		exp = 0;
		levelCap = 10;

		charge = 5;
		partialCharge = 0;
		chargeCap = 5;

		defaultAction = AC_USE;
		bones = false;
	}

	//영웅 위에 씌우는 반투명 갑옷 껍데기 (16x19 칸)
	public static final String SHELL = "sprites/rune_aegis_shell.png";

	public int charges(){
		return charge;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && !cursed && hero.buff( MagicImmune.class ) == null){
			if (charge > 0 || hero.buff( RuneAegisForm.class ) != null) actions.add( AC_USE );
		}
		return actions;
	}

	@Override
	public String actionName( String action, Hero hero ) {
		if (action.equals( AC_USE ) && hero.buff( RuneAegisForm.class ) != null){
			return Messages.get( this, "ac_off" );
		}
		return super.actionName( action, hero );
	}

	@Override
	public void execute( final Hero hero, String action ) {
		super.execute( hero, action );

		if (!action.equals( AC_USE )) return;

		RuneAegisForm form = hero.buff( RuneAegisForm.class );
		if (form != null){
			form.detach();
			hero.spendAndNext( 1f );
			return;
		}
		if (!isEquipped( hero )){
			GLog.i( Messages.get( Artifact.class, "need_to_equip" ) );
			return;
		}
		if (cursed){
			GLog.w( Messages.get( this, "cursed" ) );
			return;
		}
		if (charge <= 0){
			GLog.w( Messages.get( this, "no_charge" ) );
			return;
		}

		//갑옷이 몸을 덮는 동작만 보여 주고, 번쩍임·흔들림·글자는 없음
		hero.busy();
		hero.sprite.idle();
		Sample.INSTANCE.play( Assets.Sounds.MELD );
		hero.sprite.parent.add( new AegisAssemble( hero.sprite, SHELL, false, new Callback() {
			@Override
			public void call() {
				//그림자의 망토처럼 켜는 순간 충전 하나를 씀
				charge = Math.max( 0, charge - 1 );
				Buff.affect( hero, RuneAegisForm.class );
				Buff.affect( hero, RuneAegisShield.class ).setShield( RuneAegisForm.shieldMax( Math.max( 0, level() ) ) );
				com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.onArtifactUsed( hero );
				com.shatteredpixel.shatteredpixeldungeon.actors.hero.CollectorBonus.onUse( hero, RuneAegis.class );
				updateQuickslot();
				hero.spendAndNext( 1f );
			}
		} ) );
	}

	//갑옷이 켜져 있는 동안 몇 턴마다 불림. 충전이 남아 있지 않으면 false
	public boolean drain(){
		if (charge <= 0) return false;
		charge--;
		exp += 1;
		if (level() < levelCap && exp >= (level() + 1) * 4){
			exp -= (level() + 1) * 4;
			upgrade();
			GLog.p( Messages.get( this, "levelup" ) );
		}
		updateQuickslot();
		return charge > 0;
	}

	@Override
	public Artifact upgrade() {
		chargeCap = Math.min( chargeCap + 1, 15 );
		return (Artifact) super.upgrade();
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )){
			RuneAegisForm form = hero.buff( RuneAegisForm.class );
			if (form != null) form.detach();
			return true;
		}
		return false;
	}

	@Override
	public void charge( Hero target, float amount ) {
		if (cursed || target.buff( RuneAegisForm.class ) != null) return;
		if (charge < chargeCap){
			partialCharge += 0.25f * amount;
			while (partialCharge >= 1f && charge < chargeCap){
				partialCharge--;
				charge++;
			}
			updateQuickslot();
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new Recharge();
	}

	//꺼져 있는 동안 충전이 참 (강화될수록 더 빨리)
	public class Recharge extends ArtifactBuff {
		@Override
		public boolean act() {
			if (charge < chargeCap && !cursed && target.buff( RuneAegisForm.class ) == null){
				float turnsPerCharge = 45f - level() * 2f;
				partialCharge += 1f / turnsPerCharge;
				if (partialCharge >= 1f){
					partialCharge--;
					charge++;
					updateQuickslot();
				}
			} else if (charge >= chargeCap){
				partialCharge = 0;
			}
			spend( TICK );
			return true;
		}
	}

	@Override
	public String desc() {
		String d = Messages.get( this, "desc" );
		if (isEquipped( Dungeon.hero )){
			d += "\n\n";
			if (cursed){
				d += Messages.get( this, "desc_cursed" );
			} else {
				int l = Math.max( 0, level() );
				d += Messages.get( this, "desc_stats", RuneAegisForm.shieldMax( l ), RuneAegisForm.shieldRegen( l ),
						RuneAegisForm.TURNS_PER_CHARGE );
			}
		}
		return d;
	}
}
