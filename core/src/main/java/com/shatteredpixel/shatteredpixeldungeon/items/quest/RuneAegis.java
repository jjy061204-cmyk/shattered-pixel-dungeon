/*
 * Shattered Pixel Dungeon 모드: 룬 성갑의 문장 (유물)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RuneAegisForm;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.AegisAssemble;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

//장착하는 유물. 그림자의 망토처럼 충전을 써서 켜고 끔.
// - 켜면 갑옷 조각이 날아와 맞춰지는 연출과 함께 마도공학 슈트 '룬 성갑' 형태가 되고, 5턴마다 충전 1을 씀
// - 꺼져 있는 동안 충전이 서서히 참. 충전을 쓸수록 경험이 쌓여 유물이 강화되고(+10까지) 최대 충전이 늘어남
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

	public int charges(){
		return charge;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && !cursed && hero.buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune.class ) == null
				&& (charge > 0 || hero.buff( RuneAegisForm.class ) != null)){
			actions.add( AC_USE );
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

		hero.busy();
		hero.sprite.idle();
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		new Flare( 8, 40 ).color( 0x40E8D8, true ).show( hero.sprite, 1.6f );
		hero.sprite.emitter().burst( RuneMote.FACTORY, 20 );

		hero.sprite.parent.add( new AegisAssemble( hero.sprite, new Callback() {
			@Override
			public void call() {
				Buff.affect( hero, RuneAegisForm.class );
				Buff.affect( hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RuneAegisShield.class )
						.setShield( RuneAegisForm.shield( Math.max( 0, level() ) ) );

				if (hero.sprite instanceof HeroSprite){
					((HeroSprite) hero.sprite).updateArmor();
					hero.sprite.place( hero.pos );
				}
				GameScene.flash( 0x6040E8D8 );
				PixelScene.shake( 2, 0.4f );
				new Flare( 12, 56 ).color( 0x80F0FF, true ).show( hero.sprite, 1.2f );
				hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 16 );
				hero.sprite.emitter().burst( RuneMote.FACTORY, 30 );
				Sample.INSTANCE.play( Assets.Sounds.EVOKE );

				updateQuickslot();
				hero.spendAndNext( 1f );
			}
		} ) );
	}

	//성갑이 켜져 있는 동안 5턴마다 불림. 충전이 남아 있지 않으면 false
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

	//꺼져 있는 동안 충전이 참 (강화될수록 빨라짐)
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
		if (isEquipped( com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero )){
			d += "\n\n";
			if (cursed){
				d += Messages.get( this, "desc_cursed" );
			} else {
				int l = Math.max( 0, level() );
				d += Messages.get( this, "desc_stats", RuneAegisForm.drMin( l ), RuneAegisForm.drMax( l ),
						Math.round( RuneAegisForm.accuracyBonus( l ) * 100 ), RuneAegisForm.magicMin( l ), RuneAegisForm.magicMax( l ),
						RuneAegisForm.shield( l ) );
			}
		}
		return d;
	}
}
