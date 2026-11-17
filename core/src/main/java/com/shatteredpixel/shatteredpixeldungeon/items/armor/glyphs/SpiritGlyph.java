/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사가 '정령 부여'로 갑옷에 옮겨 담은 정령: 정령마다 다른 전용 각인(막아 주는 쪽의 능력).
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
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
		boolean adjacent = attacker != null && Dungeon.level.adjacent( attacker.pos, defender.pos );
		switch (s){
			case WATER:     //물: 받은 피해 일부만큼 회복
				int heal = Math.max( 1, damage/3 );
				defender.HP = Math.min( defender.HT, defender.HP + heal );
				if (defender.sprite != null){
					defender.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 2 );
					defender.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText.HEALING );
				}
				return damage;
			case WIND:      //바람: 붙어 있는 적을 밀쳐 냄
				if (adjacent) ForestWand.knockAway( defender, attacker, 2, true );
				return damage;
			case NATURE:    //자연: 나무껍질 방어
				Buff.affect( defender, Barkskin.class ).set( 2 + level, 3 );
				return damage;
			case FIRE: case FLAME:   //불·화염: 때린 적을 불태움
				if (adjacent && attacker.isAlive()) Buff.affect( attacker, Burning.class ).reignite( attacker, s == ForestWand.Spirit.FLAME ? 8f : 4f );
				return damage;
			case EARTH:     //땅: 피해를 1/3 줄임
				if (defender.sprite != null) CellEmitter.bottom( defender.pos ).burst( EarthParticle.FACTORY, 4 );
				return damage - damage/3;
			case ROCK:      //바위: 피해를 절반으로
				if (defender.sprite != null) CellEmitter.bottom( defender.pos ).burst( EarthParticle.FACTORY, 6 );
				return damage/2;
			case FROST:     //서리: 때린 적을 얼림
				if (adjacent && attacker.isAlive()){
					Buff.prolong( attacker, Chill.class, 4f );
					if (Random.Int( 3 ) == 0) Buff.prolong( attacker, Frost.class, 2f );
				}
				return damage;
			case SHOCK:     //전기: 때린 적에게 번개를 되돌려 줌
				if (attacker != null && attacker.isAlive() && attacker.sprite != null && defender.sprite != null){
					attacker.damage( Math.max( 1, damage/2 ), SpiritGlyph.class );
					attacker.sprite.centerEmitter().burst( SparkParticle.FACTORY, 3 );
					defender.sprite.parent.addToFront( new Lightning( defender.sprite.center(), attacker.sprite.center(), null ) );
					Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				}
				return damage;
			case CHAOS: default:
				ForestWand.Spirit[] pool = { ForestWand.Spirit.WATER, ForestWand.Spirit.WIND, ForestWand.Spirit.NATURE,
						ForestWand.Spirit.FLAME, ForestWand.Spirit.ROCK, ForestWand.Spirit.FROST, ForestWand.Spirit.SHOCK };
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
