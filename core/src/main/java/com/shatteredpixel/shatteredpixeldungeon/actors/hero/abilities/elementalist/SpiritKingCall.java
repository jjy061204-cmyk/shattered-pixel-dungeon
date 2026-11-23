/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사 갑옷 능력 3: 정령왕 소환
 *  숲의 완드에 담긴(또는 실체화한) 상급 정령 하나, 또는 같은 속성의 일반 정령 둘을 바쳐 그 속성의 정령왕을 부름.
 *  정령왕: 불·물·바람·땅. 한 번에 하나만 머물 수 있고, 머무는 동안 다시 쓰면 정령왕에게 명령을 내림 (충전 소모 없음)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.elementalist;

import com.shatteredpixel.shatteredpixeldungeon.effects.SpiritRite;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GreaterSpirit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SpiritKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpiritKingCall extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	@Override
	public float chargeUse( Hero hero ) {
		return SpiritKing.find() != null ? 0 : super.chargeUse( hero );
	}

	@Override
	public String targetingPrompt() {
		return SpiritKing.find() != null ? Messages.get( this, "prompt_direct" ) : null;
	}

	//바칠 정령: 같은 속성 일반 정령 둘(완드 칸) 또는 상급 정령 하나(완드 칸·실체화)
	private static class Offering {
		int[] slots = new int[0];
		GreaterSpirit materialized;
		String text = null;
	}

	private static Offering offeringFor( Hero hero, SpiritKing.Element e ){
		ForestWand w = hero.belongings.getItem( ForestWand.class );
		Offering o = new Offering();
		if (w != null){
			ArrayList<Integer> same = new ArrayList<>();
			for (int i = 0; i < ForestWand.SLOTS; i++) if (e.matches( w.slots[i] )) same.add( i );
			if (same.size() >= 2){
				o.slots = new int[]{ same.get( 0 ), same.get( 1 ) };
				ForestWand.Spirit a = w.slots[same.get( 0 )], b = w.slots[same.get( 1 )];
				o.text = a == b ? Messages.get( SpiritKingCall.class, "offer_pair", a.title() )
						: Messages.get( SpiritKingCall.class, "offer_two", a.title(), b.title() );
				return o;
			}
			for (int i = 0; i < ForestWand.SLOTS; i++){
				if (w.slots[i] == e.greater){
					o.slots = new int[]{ i };
					o.text = Messages.get( SpiritKingCall.class, "offer_greater", e.greater.title() );
					return o;
				}
			}
		}
		for (GreaterSpirit g : ForestWand.materializedSpirits()){
			if (g.spirit() == e.greater){
				o.materialized = g;
				o.text = Messages.get( SpiritKingCall.class, "offer_greater", e.greater.title() );
				return o;
			}
		}
		return null;
	}

	@Override
	protected void activate( final ClassArmor armor, final Hero hero, Integer target ) {
		SpiritKing king = SpiritKing.find();
		if (king != null){
			if (target != null) king.directTocell( target );
			return;
		}

		final SpiritKing.Element[] els = SpiritKing.Element.values();
		final Offering[] offers = new Offering[els.length];
		String[] opts = new String[els.length];
		boolean any = false;
		for (int i = 0; i < els.length; i++){
			offers[i] = offeringFor( hero, els[i] );
			opts[i] = Messages.titleCase( els[i].title() ) + (offers[i] != null ? " - " + offers[i].text : " - " + Messages.get( this, "offer_none" ));
			if (offers[i] != null) any = true;
		}
		if (!any){
			GLog.w( Messages.get( this, "nothing" ) );
			return;
		}

		GameScene.show( new WndOptions( new HeroIcon( this ),
				Messages.titleCase( name() ), Messages.get( this, "choose" ), opts ){
			@Override
			protected boolean enabled( int index ) {
				return offers[index] != null;
			}
			@Override
			protected void onSelect( int index ) {
				if (offers[index] == null || !hero.ready) return;
				summon( armor, hero, els[index], offers[index] );
			}
		} );
	}

	private void summon( ClassArmor armor, Hero hero, SpiritKing.Element e, Offering o ){
		//나타날 자리
		SpiritKing king = e.create();
		ArrayList<Integer> spots = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int p = hero.pos + n;
			if (Actor.findChar( p ) != null) continue;
			if (king.flying ? (Dungeon.level.passable[p] || Dungeon.level.avoid[p])
					: (Dungeon.level.passable[p] && !Dungeon.level.pit[p])) spots.add( p );
		}
		if (spots.isEmpty()){
			GLog.w( Messages.get( this, "no_space" ) );
			return;
		}

		//정령을 바침
		ForestWand w = hero.belongings.getItem( ForestWand.class );
		for (int s : o.slots){
			if (w != null && w.slots[s] != null){
				CellEmitter.center( hero.pos ).burst( w.slots[s].particle(), 10 );
				w.slots[s] = null;
			}
		}
		if (w != null){
			w.preferredSlot = -1;
			Item.updateQuickslot();
		}
		if (o.materialized != null && o.materialized.isAlive()){
			CellEmitter.center( o.materialized.pos ).burst( e.greater.particle(), 14 );
			o.materialized.destroy();
			if (o.materialized.sprite != null) o.materialized.sprite.killAndErase();
		}

		armor.charge -= super.chargeUse( hero );
		armor.updateQuickslot();
		Invisibility.dispel();

		king.adjustStats();
		king.HP = king.HT;
		king.setLifetime( SpiritKing.lifetime( hero ) );
		king.pos = Random.element( spots );
		SpiritRite.kingDescends( king.pos, e.spirit.color );
		GameScene.add( king );
		ScrollOfTeleportation.appear( king, king.pos );
		if (king.sprite != null){
			king.sprite.centerEmitter().burst( e.spirit.particle(), 30 );
			king.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 8 );
			new Flare( 10, 32 ).color( e.spirit.color, true ).show( king.sprite, 1.2f );
		}
		PixelScene.shake( 1.5f, 0.4f );
		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 1f, 0.7f );
		Sample.INSTANCE.play( Assets.Sounds.BLAST, 0.7f, 0.8f );
		GLog.p( Messages.get( this, "summoned", king.name() ) );

		//왕의 가호: 정령왕 최대 체력의 10/20/30/40%만큼 영웅에게 보호막
		int aegis = hero.pointsInTalent( Talent.ROYAL_AEGIS );
		if (aegis > 0){
			Buff.affect( hero, Barrier.class ).setShield( Math.round( king.HT * 0.1f * aegis ) );
		}

		hero.sprite.zap( king.pos );
		Dungeon.observe();
		hero.spendAndNext( Actor.TICK );
	}

	@Override
	public int icon() {
		return HeroIcon.SPIRIT_KING;
	}

	@Override
	public String desc() {
		String d = Messages.get( this, "desc" );
		if (Dungeon.hero != null) d += "\n\n" + Messages.get( this, "dur", (int) SpiritKing.lifetime( Dungeon.hero ) );
		return d + "\n\n" + Messages.get( this, "cost", (int) baseChargeUse );
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.KING_MAJESTY, Talent.KING_REIGN, Talent.ROYAL_AEGIS, Talent.HEROIC_ENERGY };
	}
}
