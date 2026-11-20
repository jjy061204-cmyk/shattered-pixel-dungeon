/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dobrokhotov
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Visual;
import com.watabou.noosa.particles.Emitter;

//모드: 날아가는 탄의 궤도.
//입자마다 제 움직임(불꽃은 솟고, 눈송이·물방울은 내려앉고, 그림자는 떠오름)이 있어서 꼬리가 휘어 보이므로,
//뿌린 입자를 뿌린 자리에 그대로 멈춰 두어 탄이 지나간 길을 따라 곧게 남김. 모양·빛깔·사라지는 모습은 그대로.
public class StraightTrail extends Emitter {

	private boolean[] existed = new boolean[32];

	@Override
	protected void emit( int index ) {
		int before = length;
		if (existed.length < before) existed = new boolean[before * 2];
		for (int i = 0; i < before; i++){
			Gizmo g = members.get( i );
			existed[i] = g != null && g.exists;
		}

		super.emit( index );

		float px = target != null ? target.x + x : x;
		float py = target != null ? target.y + y : y;
		for (int i = 0; i < length; i++){
			Gizmo g = members.get( i );
			if (g == null || !g.exists || (i < before && existed[i])) continue;
			if (g instanceof Visual){
				Visual v = (Visual) g;
				v.speed.set( 0 );
				v.acc.set( 0 );
				//입자의 한가운데가 탄이 지나간 자리에 오게
				v.x = px - v.origin.x;
				v.y = py - v.origin.y;
			}
		}
	}
}
