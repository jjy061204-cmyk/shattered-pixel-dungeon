/*
 * Mirror Pixel Dungeon (Shattered Pixel Dungeon 모드)
 * 놀 제국의 장치(경보 북·포로 우리·고철 바리케이드)가 함께 쓰는 틀
 *  - 움직이지 않는 물건. 누르면 상호작용 창이 뜸
 *  - 내구도: 공격·투척·마법 한 번에 1씩 깎이고, 폭발은 한 번에 부숨. 불에 타는 장치는 불이 붙은 동안 매 턴 1씩
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Callback;

public abstract class GnollProp extends NPC {

	{
		HP = HT = 3;
		EXP = 0;
		maxLvl = -2;

		properties.add( Property.IMMOVABLE );
		properties.add( Property.OBJECT );
		properties.add( Property.INORGANIC );
	}

	//불에 타는 장치인지 (나무·가죽)
	protected boolean flammable(){
		return true;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 0;
	}

	@Override
	public boolean add( Buff buff ) {
		//물건이라 상태 이상은 받지 않음. 불만 붙을 수 있음
		if (buff instanceof Burning && flammable()){
			return super.add( buff );
		}
		return false;
	}

	@Override
	public void damage( int dmg, Object src ) {
		if (!isAlive() || dmg < 0) return;
		if (src instanceof Burning && !flammable()) return;

		int loss = (src instanceof Bomb) ? HP : 1;
		HP = Math.max( 0, HP - loss );
		if (sprite != null){
			sprite.flash();
			if (HP > 0) sprite.showStatus( CharSprite.WARNING, Messages.get( GnollProp.class, "cracked", HP ) );
		}
		onHit( src );
		if (HP <= 0){
			die( src );
		}
	}

	//맞았을 때 (부서지기 전)
	protected void onHit( Object src ){
	}

	@Override
	public void die( Object cause ) {
		//물건은 몹처럼 경험치·전리품·처치 기록을 남기지 않음
		onBroken( cause );
		destroy();
		if (sprite != null) sprite.die();
	}

	//부서질 때
	protected void onBroken( Object cause ){
	}

	@Override
	public boolean reset() {
		return true;
	}

	//선택지가 있는 상호작용 창. options[i]를 누르면 choose(i)
	protected void showOptions( final Hero hero, final String text, final String... options ){
		ShatteredPixelDungeon.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndOptions( sprite(), Messages.titleCase( name() ), text, options ){
					@Override
					protected void onSelect( int index ) {
						if (isAlive() && Dungeon.level.adjacent( hero.pos, pos )){
							choose( hero, index );
						}
					}
				});
			}
		});
	}

	protected void choose( Hero hero, int index ){
	}

	@Override
	public String description() {
		return Messages.get( this, "desc" );
	}
}
