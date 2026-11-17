/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사가 '정령 부여'로 갑옷에 옮겨 담은 정령: 정령마다 다른 전용 각인(막아 주는 쪽의 능력).
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WindParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.ForestWand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class SpiritGlyph extends Armor.Glyph {

	public ForestWand.Spirit spirit = ForestWand.Spirit.EARTH;

	public SpiritGlyph(){}
	public SpiritGlyph( ForestWand.Spirit s ){ spirit = s; }

	@Override
	public int proc( Armor armor, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, armor.buffedLvl() );
		// +0 29%, +2 36%, +5 45% …
		float chance = (level + 2f) / (level + 7f) * procChanceMultiplier( defender );
		if (spirit.strong) chance *= 1.15f;
		if (Random.Float() >= chance) return damage;
		return defend( spirit, attacker, defender, damage, level );
	}

	private static int defend( ForestWand.Spirit s, Char attacker, Char defender, int damage, int level ){
		boolean adjacent = attacker != null && attacker.isAlive() && Dungeon.level.adjacent( attacker.pos, defender.pos );
		switch (s){
			case WATER: {   //물: 받은 피해의 1/3을 회복하고 몸의 불을 끔. 붙어 있는 적에겐 물줄기를 되쏨
				int heal = Math.min( Math.max( 1, damage/3 ), defender.HT - defender.HP );
				Buff.detach( defender, Burning.class );
				if (heal > 0){
					defender.HP += heal;
					if (defender.sprite != null){
						defender.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 3 );
						defender.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
					}
				}
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( FlowParticle.FACTORY, 10 );
				ForestWand.fx( defender, s, "g_water" );
				if (adjacent){
					if (attacker.sprite != null) attacker.sprite.centerEmitter().burst( FlowParticle.FACTORY, 8 );
					ForestWand.elemHit( s, attacker, 0.4f );
				}
				return damage;
			}
			case WIND:      //바람: 붙어 있는 적을 밀쳐 내고, 바람을 타 잠시 빨라짐
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( WindParticle.FACTORY, 10 );
				ForestWand.fx( defender, s, "g_wind" );
				Buff.prolong( defender, Haste.class, 3f );
				if (adjacent){
					if (attacker.sprite != null) attacker.sprite.centerEmitter().burst( WindParticle.FACTORY, 10 );
					ForestWand.knockAway( defender, attacker, 2, true );
				}
				return damage;
			case NATURE:    //자연: 나무껍질이 돋고, 붙어 있는 적은 뿌리에 묶임
				Buff.affect( defender, Barkskin.class ).set( 2 + level, 3 );
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( LeafParticle.GENERAL, 10 );
				ForestWand.fx( defender, s, "g_nature" );
				if (adjacent && !attacker.flying && !attacker.properties().contains( Char.Property.IMMOVABLE )){
					Buff.prolong( attacker, Roots.class, 2f );
					CellEmitter.bottom( attacker.pos ).burst( LeafParticle.GENERAL, 10 );
				}
				return damage;
			case FIRE: case FLAME:   //불·화염: 때린 적을 불꽃으로 되받아치고 불태움
				if (adjacent){
					boolean strong = s == ForestWand.Spirit.FLAME;
					if (attacker.sprite != null) attacker.sprite.centerEmitter().burst( FlameParticle.FACTORY, strong ? 14 : 10 );
					ForestWand.fx( defender, s, "g_fire" );
					ForestWand.elemHit( s, attacker, strong ? 0.75f : 0.4f );
					if (attacker.isAlive()) Buff.affect( attacker, Burning.class ).reignite( attacker, strong ? 8f : 4f );
				}
				return damage;
			case EARTH:     //땅: 피해를 1/3 줄이고, 붙어 있는 적의 발을 흙으로 묶음
				CellEmitter.bottom( defender.pos ).burst( EarthParticle.FACTORY, 8 );
				ForestWand.fx( defender, s, "g_earth" );
				if (adjacent) Buff.prolong( attacker, Cripple.class, 3f );
				return damage - damage/3;
			case ROCK: {    //바위: 피해를 절반으로 막고, 막아 낸 힘의 절반을 붙어 있는 적에게 튕겨 냄
				CellEmitter.bottom( defender.pos ).burst( EarthParticle.FACTORY, 12 );
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( EarthParticle.SMALL, 8 );
				ForestWand.fx( defender, s, "g_rock" );
				int blocked = damage - damage/2;
				if (adjacent && blocked >= 2){
					attacker.damage( blocked/2, new ForestWand.ElemSource( s ) );
					if (attacker.sprite != null && attacker.sprite.visible) PixelScene.shake( 1, 0.1f );
				}
				return damage/2;
			}
			case FROST:     //서리: 때린 적을 차갑게 하고, 이미 차가운 적은 얼려 버림
				if (adjacent){
					if (attacker.sprite != null) attacker.sprite.centerEmitter().burst( SnowParticle.FACTORY, 10 );
					ForestWand.fx( defender, s, "g_frost" );
					boolean chilled = attacker.buff( Chill.class ) != null;
					ForestWand.elemHit( s, attacker, 0.4f );
					if (attacker.isAlive()){
						if (chilled && !attacker.isImmune( Frost.class )) Buff.affect( attacker, ForestWand.FreezeNext.class );
						else Buff.prolong( attacker, Chill.class, 4f );
					}
				}
				return damage;
			case SHOCK:     //전기: 때린 적에게 번개를 되돌려 줌 (받은 피해의 절반 + 번개 피해)
				if (attacker != null && attacker.isAlive() && attacker.sprite != null && defender.sprite != null){
					defender.sprite.parent.addToFront( new Lightning( defender.sprite.center(), attacker.sprite.center(), null ) );
					attacker.sprite.centerEmitter().burst( SparkParticle.FACTORY, 6 );
					Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
					ForestWand.fx( defender, s, "g_shock" );
					attacker.damage( Math.max( 1, damage/2 ), new ForestWand.ElemSource( s ) );
					ForestWand.elemHit( s, attacker, 0.3f );
				}
				return damage;
			case CHAOS: default:
				ForestWand.Spirit[] pool = { ForestWand.Spirit.WATER, ForestWand.Spirit.WIND, ForestWand.Spirit.NATURE,
						ForestWand.Spirit.FIRE, ForestWand.Spirit.EARTH, ForestWand.Spirit.ROCK,
						ForestWand.Spirit.FLAME, ForestWand.Spirit.FROST, ForestWand.Spirit.SHOCK };
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( RainbowParticle.BURST, 10 );
				ForestWand.fx( defender, s, "chaos" );
				return defend( Random.element( pool ), attacker, defender, damage, level );
		}
	}

	@Override
	public String name( String armorName ) {
		return Messages.get( SpiritGlyph.class, "name", spirit.title(), armorName );
	}

	@Override
	public String desc() {
		return Messages.get( SpiritGlyph.class, "desc_" + spirit.name().toLowerCase() );
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return spirit.glowing();
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( "spirit", spirit.name() );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		try { spirit = ForestWand.Spirit.valueOf( bundle.getString( "spirit" ) ); } catch (Exception e) { spirit = ForestWand.Spirit.EARTH; }
	}
}
