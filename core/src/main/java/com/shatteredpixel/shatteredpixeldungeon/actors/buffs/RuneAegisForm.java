/*
 * Shattered Pixel Dungeon 모드: 수호의 반지로 두른 마법 갑옷
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.RuneAegis;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

//반지를 작동시킨 동안 붙는 버프. 5턴마다 반지의 충전을 하나 쓰고, 매 턴 보호막을 조금씩 되살림.
//충전이 떨어지거나, 반지를 벗거나, 다시 눌러 끄면 풀림
public class RuneAegisForm extends Buff {

	public static final int TURNS_PER_CHARGE = 5;

	//보호막 최대치: 10 (+0) ~ 40 (+10). 충전 속성은 1.25배
	public static int shieldMax( int lvl, RuneAegis.Imbue i ){
		int m = 10 + 3 * lvl;
		if (i == RuneAegis.Imbue.CHARGE) m = Math.round( m * 1.25f );
		//유물수집가의 고유 효과
		return Math.round( m * com.shatteredpixel.shatteredpixeldungeon.actors.hero.CollectorBonus.aegisShieldMultiplier( Dungeon.hero ) );
	}
	//턴마다 재생: 1 (+0) ~ 3 (+10). 서리 속성은 +1
	public static int shieldRegen( int lvl, RuneAegis.Imbue i ){
		return 1 + lvl / 4 + (i == RuneAegis.Imbue.FROST ? 1 : 0);
	}
	//충전 하나로 버티는 턴: 충전 속성은 7턴
	public static int turnsPerCharge( RuneAegis.Imbue i ){
		return i == RuneAegis.Imbue.CHARGE ? 7 : TURNS_PER_CHARGE;
	}

	private int turns = TURNS_PER_CHARGE;
	private boolean mirrorUsed = false;
	private boolean broken = false;

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private RuneAegis ring(){
		if (!(target instanceof Hero)) return null;
		RuneAegis a = ((Hero) target).belongings.getItem( RuneAegis.class );
		return a != null && a.isEquipped( (Hero) target ) ? a : null;
	}

	private static RuneAegis.Imbue imbueOf( Hero hero ){
		if (hero.buff( RuneAegisForm.class ) == null) return null;
		RuneAegis a = hero.belongings.getItem( RuneAegis.class );
		return a == null ? null : a.imbue();
	}

	//속성에 따른 면역: 화염 갑옷은 불에, 서리 갑옷은 냉기·빙결에 걸리지 않음
	@Override
	public java.util.HashSet<Class> immunities() {
		java.util.HashSet<Class> set = super.immunities();
		RuneAegis a = ring();
		if (a != null){
			if (a.imbue() == RuneAegis.Imbue.FIRE){
				set.add( Burning.class );
				set.add( com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire.class );
			} else if (a.imbue() == RuneAegis.Imbue.FROST){
				set.add( Chill.class );
				set.add( Frost.class );
				set.add( com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing.class );
			}
		}
		return set;
	}

	@Override
	public boolean act() {
		RuneAegis a = ring();
		if (a == null){
			detach();
			return true;
		}
		ensureShell();
		int lvl = Math.max( 0, a.level() );
		RuneAegis.Imbue im = a.imbue();
		Hero hero = (Hero) target;

		RuneAegisShield sh = Buff.affect( target, RuneAegisShield.class );
		if (sh.shielding() <= 0){
			if (!broken){
				broken = true;
				onShieldBreak( hero, im, lvl );
			}
		} else if (sh.shielding() >= shieldMax( lvl, im ) / 2){
			broken = false;
		}
		if (sh.shielding() < shieldMax( lvl, im )){
			sh.setShield( Math.min( shieldMax( lvl, im ), sh.shielding() + shieldRegen( lvl, im ) ) );
		}

		//충전 속성: 갑옷을 두른 동안 마법 막대가 조금씩 충전됨
		if (im == RuneAegis.Imbue.CHARGE){
			hero.belongings.charge( 0.2f );
		}

		turns--;
		if (turns <= 0){
			if (!a.drain()){
				detach();
				return true;
			}
			turns = turnsPerCharge( im );
		}
		Item.updateQuickslot();
		spend( TICK );
		return true;
	}

	//보호막이 깨지는 순간
	private void onShieldBreak( Hero hero, RuneAegis.Imbue im, int lvl ){
		if (im != RuneAegis.Imbue.FIRE && im != RuneAegis.Imbue.FROST) return;
		//화염: 주변의 적을 불태우고 피해 / 서리: 주변의 적을 얼림
		for (int n : com.watabou.utils.PathFinder.NEIGHBOURS8){
			int c = hero.pos + n;
			Char ch = com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar( c );
			if (Dungeon.level.heroFOV[c] && !Dungeon.level.solid[c]){
				com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( c ).burst(
						im == RuneAegis.Imbue.FIRE
								? com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle.FACTORY
								: com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle.FACTORY, 6 );
			}
			if (ch == null || ch.alignment == Char.Alignment.ALLY) continue;
			if (im == RuneAegis.Imbue.FIRE){
				ch.damage( com.watabou.utils.Random.NormalIntRange( 4 + lvl, 8 + 2 * lvl ), this );
				if (ch.isAlive()) Buff.affect( ch, Burning.class ).reignite( ch );
			} else {
				Buff.prolong( ch, Frost.class, 3f + lvl * 0.3f );
			}
		}
		com.watabou.noosa.audio.Sample.INSTANCE.play( im == RuneAegis.Imbue.FIRE
				? com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.BURNING
				: com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.SHATTER );
	}

	//갑옷을 두른 영웅이 공격을 받았을 때
	public static void onDefend( Hero hero, Char enemy, int damage ){
		RuneAegis.Imbue im = imbueOf( hero );
		if (im == null || enemy == null || !Dungeon.level.adjacent( hero.pos, enemy.pos )) return;
		if (im == RuneAegis.Imbue.FIRE){
			Buff.affect( enemy, Burning.class ).reignite( enemy );
		} else if (im == RuneAegis.Imbue.FROST){
			Buff.prolong( enemy, Chill.class, 4f );
		} else if (im == RuneAegis.Imbue.MIRROR && damage > 0){
			//거울 갑옷: 받은 근접 피해의 1/4을 되돌려 줌
			enemy.damage( Math.max( 1, damage / 4 ), new Reflect() );
		}
	}

	public static class Reflect {}

	//갑옷을 두른 영웅이 적을 때렸을 때
	public static void onAttack( Hero hero, Char enemy, int damage ){
		RuneAegis.Imbue im = imbueOf( hero );
		if (im == null || enemy == null || !enemy.isAlive()) return;
		if (im == RuneAegis.Imbue.FIRE){
			if (com.watabou.utils.Random.Int( 3 ) == 0) Buff.affect( enemy, Burning.class ).reignite( enemy );
		} else if (im == RuneAegis.Imbue.FROST){
			//이미 냉기에 걸린 적은 가끔 얼어붙음
			if (enemy.buff( Chill.class ) != null && com.watabou.utils.Random.Int( 4 ) == 0){
				Buff.prolong( enemy, Frost.class, 2f );
			} else {
				Buff.prolong( enemy, Chill.class, 3f );
			}
		} else if (im == RuneAegis.Imbue.MIRROR){
			//거울 갑옷: 근접 타격마다 피해의 30%만큼 거울의 추가 타격
			if (damage > 0 && Dungeon.level.adjacent( hero.pos, enemy.pos )){
				enemy.damage( Math.max( 1, Math.round( damage * 0.3f ) ), new Reflect() );
				if (enemy.sprite != null) enemy.sprite.flash();
			}
		} else if (im == RuneAegis.Imbue.CHARGE){
			//타격마다 반지의 충전이 조금 차오름
			RuneAegis a = hero.belongings.getItem( RuneAegis.class );
			if (a != null) a.spark();
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.ARMOR;
	}

	@Override
	public void tintIcon( Image icon ) {
		icon.hardlight( 0.7f, 0.85f, 1f );
	}

	@Override
	public float iconFadePercent() {
		RuneAegis a = ring();
		float per = turnsPerCharge( a == null ? RuneAegis.Imbue.NONE : a.imbue() );
		return Math.max( 0, (per - turns) / per );
	}

	//영웅 위에 씌우는 반투명 갑옷 껍데기
	private com.shatteredpixel.shatteredpixeldungeon.effects.AegisShell shell;

	private String shellLook(){
		RuneAegis a = ring();
		return RuneAegis.shellTexture( a == null ? RuneAegis.Imbue.NONE : a.imbue() );
	}

	public void ensureShell(){
		if (target == null || target.sprite == null || target.sprite.parent == null) return;
		if (shell != null && shell.parent != null && shell.target() == target.sprite){
			shell.setLook( shellLook() );
			return;
		}
		if (shell != null) shell.killAndErase();
		shell = new com.shatteredpixel.shatteredpixeldungeon.effects.AegisShell( target.sprite, shellLook() );
		target.sprite.parent.add( shell );
	}

	@Override
	public void fx( boolean on ) {
		if (on){
			if (target.sprite != null && target.sprite.parent != null){
				ensureShell();
			} else {
				//불러오는 중에는 영웅 그림이 아직 화면에 붙지 않았으니 다음 프레임에 씌움
				com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.runOnRenderThread( new com.watabou.utils.Callback() {
					@Override
					public void call() {
						if (target != null && target.buff( RuneAegisForm.class ) == RuneAegisForm.this) ensureShell();
					}
				} );
			}
		} else if (shell != null){
			shell.killAndErase();
			shell = null;
		}
	}

	@Override
	public void detach() {
		Char ch = target;
		super.detach();
		if (ch != null) Buff.detach( ch, RuneAegisShield.class );
		if (ch instanceof Hero && ch.sprite instanceof HeroSprite && ch.isAlive()){
			String tex = RuneAegis.shellTexture( (Hero) ch );
			//갑옷이 입자로 흩어져 사라지는 모습
			if (ch.sprite.parent != null){
				ch.sprite.parent.add( new com.shatteredpixel.shatteredpixeldungeon.effects.AegisAssemble( ch.sprite, tex, true, null ) );
				com.watabou.noosa.audio.Sample.INSTANCE.play( com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.PUFF, 0.6f, 1.3f );
			}
			Item.updateQuickslot();
		}
	}

	@Override
	public String desc() {
		RuneAegis a = ring();
		int l = a == null ? 0 : Math.max( 0, a.level() );
		RuneAegis.Imbue im = a == null ? RuneAegis.Imbue.NONE : a.imbue();
		return Messages.get( this, "desc", shieldMax( l, im ), shieldRegen( l, im ), turns, a == null ? 0 : a.charges() );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "turns", turns );
		bundle.put( "mirror_used", mirrorUsed );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		turns = bundle.contains( "turns" ) ? bundle.getInt( "turns" ) : TURNS_PER_CHARGE;
		mirrorUsed = bundle.getBoolean( "mirror_used" );
	}
}
