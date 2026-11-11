/*
 * Shattered Pixel Dungeon 모드: 룬 성갑의 문장 (성소 고유 유물)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RuneAegisCooldown;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RuneAegisForm;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.AegisAssemble;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RuneMote;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

//사용하면 갑옷 조각이 날아와 몸에 맞춰지는 연출과 함께, 40턴 동안 전신 갑옷 '룬 성갑' 형태가 됨.
//성갑이 풀린 뒤 250턴 동안 다시 힘을 모아야 또 쓸 수 있음
public class RuneAegis extends Item {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.RUNE_AEGIS;
		unique = true;
		bones = false;
		defaultAction = AC_USE;
	}

	private static final ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x40E8D8, 2f );

	@Override
	public ItemSprite.Glowing glowing() {
		return TEAL;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_USE );
		return actions;
	}

	@Override
	public void execute( final Hero hero, String action ) {
		super.execute( hero, action );
		if (!action.equals( AC_USE )) return;

		if (hero.buff( RuneAegisForm.class ) != null){
			GLog.w( Messages.get( this, "already" ) );
			return;
		}
		RuneAegisCooldown cd = hero.buff( RuneAegisCooldown.class );
		if (cd != null){
			GLog.w( Messages.get( this, "cooldown", cd.left ) );
			return;
		}

		hero.busy();
		hero.sprite.idle();
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		new Flare( 8, 40 ).color( 0x40E8D8, true ).show( hero.sprite, 1.6f );
		hero.sprite.emitter().burst( RuneMote.FACTORY, 20 );
		GLog.p( Messages.get( this, "calling" ) );

		hero.sprite.parent.add( new AegisAssemble( hero.sprite, new Callback() {
			@Override
			public void call() {
				Buff.prolong( hero, RuneAegisForm.class, RuneAegisForm.DURATION );
				Buff.affect( hero, RuneAegisCooldown.class ).left = (int)(RuneAegisForm.DURATION + RuneAegisCooldown.DURATION);
				Buff.affect( hero, Barrier.class ).setShield( Math.round( hero.HT * 0.3f ) );

				if (hero.sprite instanceof HeroSprite){
					((HeroSprite) hero.sprite).updateArmor();
					hero.sprite.place( hero.pos );
				}
				GameScene.flash( 0x6040E8D8 );
				PixelScene.shake( 2, 0.4f );
				new Flare( 12, 56 ).color( 0xFFE9A8, true ).show( hero.sprite, 1.2f );
				hero.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 16 );
				hero.sprite.emitter().burst( RuneMote.FACTORY, 30 );
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
				Sample.INSTANCE.play( Assets.Sounds.EVOKE );
				GLog.p( Messages.get( RuneAegis.class, "donned" ) );

				updateQuickslot();
				hero.spendAndNext( 1f );
			}
		} ) );
	}

	@Override
	public String status() {
		if (Dungeon.hero == null) return null;
		RuneAegisCooldown cd = Dungeon.hero.buff( RuneAegisCooldown.class );
		return cd == null ? null : Integer.toString( cd.left );
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc" ) + "\n\n" + Messages.get( this, "category" );
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
