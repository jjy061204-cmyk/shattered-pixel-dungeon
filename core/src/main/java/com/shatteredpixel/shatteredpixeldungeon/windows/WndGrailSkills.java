/*
 * Shattered Pixel Dungeon 모드: 핏빛 성배 기술 창
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CrimsonGrail;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RightClickMenu;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PointF;

//성직자의 성서 주문 창(WndClericSpells)과 같은 방식:
// 아이콘을 눌러 기술을 쓰고, 길게 눌러 빠른 사용에 등록하고, 정보 버튼으로 설명 보기 모드를 오감
public class WndGrailSkills extends Window {

	protected static final int WIDTH = 120;
	public static int BTN_SIZE = 20;

	public WndGrailSkills( final CrimsonGrail grail, final Hero hero, final boolean info ){

		IconTitle title;
		if (!info){
			title = new IconTitle( new ItemSprite( grail ), Messages.titleCase( Messages.get( this, "use_title" ) ) );
		} else {
			title = new IconTitle( Icons.INFO.get(), Messages.titleCase( Messages.get( this, "info_title" ) ) );
		}
		title.setRect( 0, 0, WIDTH, 0 );
		title.color( 0xFF6680 );
		add( title );

		//창 테두리를 검붉게 물들임
		chrome.hardlight( 0xE8A8B4 );

		IconButton btnInfo = new IconButton( info ? new ItemSprite( grail ) : Icons.INFO.get() ){
			@Override
			protected void onClick() {
				GameScene.show( new WndGrailSkills( grail, hero, !info ) );
				hide();
			}
		};
		btnInfo.setRect( WIDTH - 16, 0, 16, 16 );
		add( btnInfo );

		String text;
		if (info)                           text = Messages.get( this, "info_desc" );
		else if (DeviceCompat.isDesktop())  text = Messages.get( this, "use_desc_desktop" );
		else                                text = Messages.get( this, "use_desc_mobile" );
		RenderedTextBlock msg = PixelScene.renderTextBlock( text, 6 );
		msg.maxWidth( WIDTH );
		msg.setPos( 0, title.bottom() + 4 );
		add( msg );

		//성배에 담긴 생명력: 핏빛 게이지
		float gy = msg.bottom() + 4;
		float ratio = grail.lifeCap() <= 0 ? 0 : Math.min( 1f, grail.vitality() / (float) grail.lifeCap() );
		addBlock( 0, gy, WIDTH, 11, 0xFF14040A );                       //테두리
		addBlock( 1, gy + 1, WIDTH - 2, 9, 0xFF3A0A16 );                //빈 칸
		int fillW = Math.round( (WIDTH - 2) * ratio );
		if (fillW > 0){
			addBlock( 1, gy + 1, fillW, 9, 0xFFA8142E );                 //피
			addBlock( 1, gy + 1, fillW, 2, 0xFFE0485C );                 //윗면 반사
			addBlock( 1, gy + 8, fillW, 2, 0xFF6E0A1E );                 //아랫면 그늘
		}
		RenderedTextBlock life = PixelScene.renderTextBlock( Messages.get( this, "life", grail.vitality(), grail.lifeCap() ), 6 );
		life.hardlight( 0xFFE8EC );
		life.setPos( (WIDTH - life.width()) / 2f, gy + (11 - life.height()) / 2f + 0.5f );
		PixelScene.align( life );
		add( life );

		int top = (int) (gy + 11) + 4;

		//줄 0: 처음부터 있는 기술, 줄 1~3: 특성 단계별로 얻은 기술과 전직 기술 (성서처럼 줄 사이에 구분선)
		boolean first = true;
		//줄 4: 진조의 왕관을 쓰는 동안만 쓰는 왕관 기술 (왕관이 없으면 흐리게)
		for (int row = 0; row <= 4; row++){
			java.util.ArrayList<CrimsonGrail.Skill> skills = CrimsonGrail.skillList( hero, row );
			if (skills.isEmpty()) continue;

			if (!first) top += BTN_SIZE + 9;
			first = false;

			//줄 제목: ── 기본 기술 ── 처럼 양옆에 붉은 선
			RenderedTextBlock head = PixelScene.renderTextBlock( Messages.get( WndGrailSkills.class, "section_" + row ), 6 );
			head.hardlight( row == 4 ? 0xFFD870 : 0xD86A7C );
			head.setPos( (WIDTH - head.width()) / 2f, top );
			PixelScene.align( head );
			add( head );
			float lineY = top + head.height() / 2f;
			float side = (WIDTH - head.width()) / 2f - 4;
			if (side > 2){
				addBlock( 0, lineY, side, 1, 0xFF7A1A2C );
				addBlock( WIDTH - side, lineY, side, 1, 0xFF7A1A2C );
				addBlock( side - 2, lineY - 1, 2, 3, 0xFFB0283E );          //선 끝의 마름모 장식
				addBlock( WIDTH - side, lineY - 1, 2, 3, 0xFFB0283E );
			}
			top += (int) head.height() + 3;

			int left = 2 + (WIDTH - skills.size() * (BTN_SIZE + 4)) / 2;
			for (CrimsonGrail.Skill skill : skills){
				SkillButton btn = new SkillButton( skill, grail, info );
				btn.setRect( left, top, BTN_SIZE, BTN_SIZE );
				add( btn );
				left += BTN_SIZE + 4;
			}
		}

		resize( WIDTH, top + BTN_SIZE + 8 );

		//모바일에서는 창을 도구 모음 바로 위로 내림
		if (SPDSettings.interfaceSize() != 2){
			offset( 0, (int) (GameScene.uiCamera.height / 2 - 30 - height / 2) );
		}
	}

	private com.watabou.noosa.ColorBlock addBlock( float x, float y, float w, float h, int color ){
		com.watabou.noosa.ColorBlock b = new com.watabou.noosa.ColorBlock( w, h, color );
		b.x = x; b.y = y;
		add( b );
		return b;
	}

	public class SkillButton extends IconButton {

		CrimsonGrail.Skill skill;
		CrimsonGrail grail;
		boolean info;

		//검붉은 칸: 바깥 테(빠른 사용 기술은 금빛), 안쪽 바탕, 윗면 반사
		com.watabou.noosa.ColorBlock frame, inner, shine;
		BitmapText cost;

		public SkillButton( CrimsonGrail.Skill skill, CrimsonGrail grail, boolean info ){
			super( new HeroIcon( skill ) );

			this.skill = skill;
			this.grail = grail;
			this.info = info;

			dim();

			boolean quick = grail.quickSkill() == skill;
			frame = new com.watabou.noosa.ColorBlock( 1, 1, quick ? 0xFFE0B040 : 0xFF7A1A2C );
			inner = new com.watabou.noosa.ColorBlock( 1, 1, 0xFF240810 );
			shine = new com.watabou.noosa.ColorBlock( 1, 1, quick ? 0xFF8A6A30 : 0xFF4A1420 );
			addToBack( shine );
			addToBack( inner );
			addToBack( frame );

			//드는 생명력 (빠른 사용에 등록된 기술은 노랗게)
			cost = new BitmapText( PixelScene.pixelFont );
			cost.text( Integer.toString( skill.cost( Dungeon.hero ) ) );
			cost.measure();
			cost.hardlight( grail.quickSkill() == skill ? 0xFFFF44 : 0xFF8899 );
			add( cost );
		}

		private void dim(){
			icon.alpha( grail.canUse( Dungeon.hero, skill ) ? 1f : 0.3f );
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			dim();
		}

		@Override
		protected void layout() {
			super.layout();
			if (frame != null){
				frame.size( width + 2, height + 2 ); frame.x = x - 1; frame.y = y - 1;
				inner.size( width, height );         inner.x = x;     inner.y = y;
				shine.size( width, 1 );              shine.x = x;     shine.y = y;
			}
			if (cost != null){
				cost.x = x + (width - cost.width()) / 2f;
				cost.y = y + height + 1;
				PixelScene.align( cost );
			}
		}

		private void useNow(){
			hide();
			//쓸 수 없으면 성배가 까닭을 알려 줌 (왕관 기술은 '왕관을 써야' 등)
			grail.use( skill );
		}

		private void showInfo(){
			GameScene.show( new WndTitledMessage( new HeroIcon( skill ), Messages.titleCase( skill.title() ),
					skill.desc() + "\n\n" + Messages.get( WndGrailSkills.class, "cost", skill.cost( Dungeon.hero ) ) ) );
		}

		@Override
		protected void onClick() {
			if (info) showInfo();
			else      useNow();
		}

		@Override
		protected boolean onLongClick() {
			hide();
			grail.setQuickSkill( skill );
			return true;
		}

		@Override
		protected void onRightClick() {
			super.onRightClick();
			RightClickMenu r = new RightClickMenu( new Image( icon ),
					Messages.titleCase( skill.title() ),
					Messages.get( WndGrailSkills.class, "use" ),
					Messages.get( WndGrailSkills.class, "info" ),
					Messages.get( WndGrailSkills.class, "quick_use" ) ){
				@Override
				public void onSelect( int index ) {
					switch (index){
						default:
							break;
						case 0:
							useNow();
							break;
						case 1:
							showInfo();
							break;
						case 2:
							hide();
							grail.setQuickSkill( skill );
							break;
					}
				}
			};
			parent.addToFront( r );
			r.camera = camera();
			PointF mousePos = PointerEvent.currentHoverPos();
			mousePos = camera.screenToCamera( (int) mousePos.x, (int) mousePos.y );
			r.setPos( mousePos.x - 3, mousePos.y - 3 );
		}

		@Override
		protected String hoverText() {
			return "_" + Messages.titleCase( skill.title() ) + "_\n" + skill.desc();
		}
	}
}
