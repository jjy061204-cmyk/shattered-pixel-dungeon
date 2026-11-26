/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 정령사가 '정령 부여'로 갑옷에 옮겨 담은 정령: 정령마다 다른 전용 각인(막아 주는 쪽의 능력).
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
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
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.SpiritEnchant;
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

	public ForestWand.Spirit spirit2 = null;

	@Override
	public int proc( Armor armor, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, armor.buffedLvl() );
		boolean boost = SpiritEnchant.enchanter( defender );
		float pow = boost ? 1.3f : 1f;
		float chanceMul = SpiritEnchant.chanceBonus( defender, level );
		boolean p1 = Random.Float() < chance( spirit, level, boost, defender, chanceMul );
		boolean p2 = spirit2 != null && Random.Float() < chance( spirit2, level, boost, defender, chanceMul );
		//쌍정령 조화
		if (spirit2 != null && (p1 ^ p2) && defender instanceof Hero
				&& Random.Float() < 0.2f * ((Hero) defender).pointsInTalent( Talent.TWIN_HARMONY )){
			p1 = p2 = true;
		}
		if (p1) damage = defend( spirit, attacker, defender, damage, level, pow );
		if (p2) damage = defend( spirit2, attacker, defender, damage, level, pow );
		return damage;
	}

	// +0 29%, +2 36%, +5 45% … (정령 부여사는 ×1.2)
	private float chance( ForestWand.Spirit s, int level, boolean boost, Char defender, float chanceMul ){
		float chance = (level + 2f) / (level + 7f) * procChanceMultiplier( defender ) * chanceMul;
		if (s.strong) chance *= 1.15f;
		if (boost) chance *= 1.2f;
		return chance;
	}

	public static int defend( ForestWand.Spirit s, Char attacker, Char defender, int damage, int level, float pow ){
		boolean adjacent = attacker != null && attacker.isAlive() && Dungeon.level.adjacent( attacker.pos, defender.pos );
		switch (s){
			case WATER: {   //물: 받은 피해의 1/3을 회복하고 몸의 불을 끔. 붙어 있는 적에겐 물줄기를 되쏨
				int heal = Math.min( Math.max( 1, Math.round( damage/3f*pow ) ), defender.HT - defender.HP );
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
					ForestWand.elemHit( s, attacker, (0.4f)*pow );
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
					ForestWand.elemHit( s, attacker, (strong ? 0.75f : 0.4f)*pow );
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
					com.shatteredpixel.shatteredpixeldungeon.actors.elemental.ElementalDamage.hit( attacker, blocked/2f*pow, com.shatteredpixel.shatteredpixeldungeon.actors.elemental.ElementalDamage.of( s ) );
					if (attacker.sprite != null && attacker.sprite.visible) PixelScene.shake( 1, 0.1f );
				}
				return damage/2;
			}
			case ICE:       //얼음: 얼음 껍질이 피해를 1/5 막고, 붙어 있는 적을 차갑게 함
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( SnowParticle.FACTORY, 6 );
				ForestWand.fx( defender, s, "g_ice" );
				if (adjacent && attacker.isAlive()){
					ForestWand.elemHit( s, attacker, 0.25f*pow );
					if (attacker.isAlive()) Buff.prolong( attacker, Chill.class, 3f );
				}
				return damage - damage/5;
			case FROST:     //서리: 때린 적을 차갑게 하고, 이미 차가운 적은 얼려 버림
				if (adjacent){
					if (attacker.sprite != null) attacker.sprite.centerEmitter().burst( SnowParticle.FACTORY, 10 );
					ForestWand.fx( defender, s, "g_frost" );
					boolean chilled = attacker.buff( Chill.class ) != null;
					ForestWand.elemHit( s, attacker, (0.4f)*pow );
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
					com.shatteredpixel.shatteredpixeldungeon.actors.elemental.ElementalDamage.hit( attacker, damage/2f*pow, com.shatteredpixel.shatteredpixeldungeon.actors.elemental.ElementalDamage.of( s ) );
					ForestWand.elemHit( s, attacker, (0.3f)*pow );
				}
				return damage;
			case GOLD:      //황금: 금빛 장막이 피해를 1/4 막고, 붙어 있는 적은 금빛에 눈이 부심
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( Speck.factory( Speck.YELLOW_LIGHT ), 8 );
				ForestWand.fx( defender, s, "g_gold" );
				if (adjacent) Buff.prolong( attacker, Blindness.class, 2f*pow );
				return damage - damage/4;
			case DARK:      //어둠: 붙어 있는 적의 눈을 가리고, 그림자에 몸을 숨김
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( ShadowParticle.UP, 10 );
				ForestWand.fx( defender, s, "g_dark" );
				if (adjacent) Buff.prolong( attacker, Blindness.class, 2f*pow );
				Buff.affect( defender, Invisibility.class, 2f );
				return damage;
			case RUNE:      //룬 마법: 룬 결계가 피해를 1/5 막고, 붙어 있는 적에게 룬을 새김 (셋이면 폭발)
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( MagicMissile.WardParticle.FACTORY, 8 );
				ForestWand.fx( defender, s, "g_rune" );
				if (adjacent) ForestWand.runeMark( s, defender, attacker, 0.5f*pow );
				return damage - damage/5;
			case LIFE:      //불완전한 생명: 때린 적에게 생명의 빛줄기를 되쏨 (멀리서 때려도)
				if (attacker != null && attacker.isAlive() && attacker != defender){
					ForestWand.lifeRay( defender, attacker );
					if (attacker.sprite != null) attacker.sprite.centerEmitter().burst( s.particle(), 8 );
					ForestWand.fx( defender, s, "g_life" );
					ForestWand.elemHit( s, attacker, 0.6f*pow );
					ForestWand.lifeUsed();
				}
				return damage;
			case CRYSTAL_BLUE: case CRYSTAL_GREEN: case CRYSTAL_RED:   //수정: 수정 껍질이 피해를 1/4 막고, 붙어 있는 적에게 파편이 박힘
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( s.particle(), 10 );
				ForestWand.fx( defender, s, "g_crystal" );
				if (adjacent){
					if (attacker.sprite != null) attacker.sprite.centerEmitter().burst( s.particle(), 6 );
					ForestWand.elemHit( s, attacker, 0.3f*pow );
				}
				return damage - damage/4;
			//상급 정령: 바탕 원소의 보호 능력을 훨씬 강하게
			case UNDINE:
				ForestWand.fx( defender, s, "g_undine" );
				return defend( ForestWand.Spirit.WATER, attacker, defender, damage, level, pow*1.6f );
			case SALAMANDER:
				ForestWand.fx( defender, s, "g_salamander" );
				return defend( ForestWand.Spirit.FLAME, attacker, defender, damage, level, pow*1.4f );
			case GNOME:
				ForestWand.fx( defender, s, "g_gnome" );
				return defend( ForestWand.Spirit.ROCK, attacker, defender,
						defend( ForestWand.Spirit.EARTH, attacker, defender, damage, level, pow ), level, pow*1.3f );
			case SYLPH:
				ForestWand.fx( defender, s, "g_sylph" );
				Buff.prolong( defender, Haste.class, 5f );
				return defend( ForestWand.Spirit.WIND, attacker, defender, damage, level, pow*1.5f );
			case CHAOS: default:
				ForestWand.Spirit[] pool = { ForestWand.Spirit.WATER, ForestWand.Spirit.WIND, ForestWand.Spirit.NATURE,
						ForestWand.Spirit.FIRE, ForestWand.Spirit.EARTH, ForestWand.Spirit.ROCK,
						ForestWand.Spirit.FLAME, ForestWand.Spirit.FROST, ForestWand.Spirit.SHOCK };
				if (defender.sprite != null) defender.sprite.centerEmitter().burst( RainbowParticle.BURST, 10 );
				ForestWand.fx( defender, s, "chaos" );
				return defend( Random.element( pool ), attacker, defender, damage, level, pow );
		}
	}

	@Override
	public String name( String armorName ) {
		if (spirit2 != null) return Messages.get( SpiritGlyph.class, "name2", spirit.title(), spirit2.title(), armorName );
		return Messages.get( SpiritGlyph.class, "name", spirit.title(), armorName );
	}

	@Override
	public String desc() {
		if (spirit2 != null){
			return "_" + spirit.title() + "_: " + Messages.get( SpiritGlyph.class, "desc_" + spirit.name().toLowerCase() )
					+ "\n\n_" + spirit2.title() + "_: " + Messages.get( SpiritGlyph.class, "desc_" + spirit2.name().toLowerCase() );
		}
		return Messages.get( SpiritGlyph.class, "desc_" + spirit.name().toLowerCase() );
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return spirit.glowing( spirit2 );
	}

	//모드: 인벤토리·바닥에서 장비 그림 위로 피어오르는 정령 입자
	public com.watabou.noosa.particles.Emitter itemEmitter(){
		com.watabou.noosa.particles.Emitter e = new com.watabou.noosa.particles.Emitter();
		boolean strong = spirit.strong || (spirit2 != null && spirit2.strong);
		e.pour( com.shatteredpixel.shatteredpixeldungeon.effects.particles.SpiritItemParticle.factory( spirit, spirit2 ),
				(strong ? 0.11f : 0.16f) * (spirit2 != null ? 0.8f : 1f) );
		return e;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( "spirit", spirit.name() );
		if (spirit2 != null) bundle.put( "spirit2", spirit2.name() );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		try { spirit = ForestWand.Spirit.valueOf( bundle.getString( "spirit" ) ); } catch (Exception e) { spirit = ForestWand.Spirit.EARTH; }
		spirit2 = null;
		if (bundle.contains( "spirit2" )){
			try { spirit2 = ForestWand.Spirit.valueOf( bundle.getString( "spirit2" ) ); } catch (Exception e) { spirit2 = null; }
		}
	}
}
