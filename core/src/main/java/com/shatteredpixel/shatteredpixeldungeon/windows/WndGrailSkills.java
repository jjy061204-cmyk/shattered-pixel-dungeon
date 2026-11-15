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
		add( title );

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

		//성배에 담긴 생명력
		RenderedTextBlock life = PixelScene.renderTextBlock( Messages.get( this, "life", grail.vitality(), grail.lifeCap() ), 6 );
		life.hardlight( 0xFF6680 );
		life.maxWidth( WIDTH );
		life.setPos( (WIDTH - life.width()) / 2f, msg.bottom() + 4 );
		add( life );

		int top = (int) life.bottom() + 5;

		//줄 0: 처음부터 있는 기술, 줄 1~3: 특성 단계별로 얻은 기술과 전직 기술 (성서처럼 줄 사이에 구분선)
		boolean first = true;
		for (int row = 0; row <= 3; row++){
			java.util.ArrayList<CrimsonGrail.Skill> skills = CrimsonGrail.skillList( hero, row );
			if (skills.isEmpty()) continue;

			if (!first){
				top += BTN_SIZE + 9;
				com.watabou.noosa.ColorBlock sep = new com.watabou.noosa.ColorBlock( WIDTH, 1, 0xFF000000 );
				sep.y = top;
				add( sep );
				top += 3;
			}
			first = false;

			int left = 2 + (WIDTH - skills.size() * (BTN_SIZE + 4)) / 2;
			for (CrimsonGrail.Skill skill : skills){
				SkillButton btn = new SkillButton( skill, grail, info );
				btn.setRect( left, top, BTN_SIZE, BTN_SIZE );
				add( btn );
				left += BTN_SIZE + 4;
			}
		}

		resize( WIDTH, top + BTN_SIZE + 7 );

		//모바일에서는 창을 도구 모음 바로 위로 내림
		if (SPDSettings.interfaceSize() != 2){
			offset( 0, (int) (GameScene.uiCamera.height / 2 - 30 - height / 2) );
		}
	}

	public class SkillButton extends IconButton {

		CrimsonGrail.Skill skill;
		CrimsonGrail grail;
		boolean info;

		NinePatch bg;
		BitmapText cost;

		public SkillButton( CrimsonGrail.Skill skill, CrimsonGrail grail, boolean info ){
			super( new HeroIcon( skill ) );

			this.skill = skill;
			this.grail = grail;
			this.info = info;

			dim();

			bg = Chrome.get( Chrome.Type.TOAST );
			addToBack( bg );

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
			if (bg != null){
				bg.size( width, height );
				bg.x = x;
				bg.y = y;
			}
			if (cost != null){
				cost.x = x + (width - cost.width()) / 2f;
				cost.y = y + height + 1;
				PixelScene.align( cost );
			}
		}

		private void useNow(){
			hide();
			if (!grail.canUse( Dungeon.hero, skill )){
				GLog.w( Messages.get( CrimsonGrail.class, "no_skill" ) );
			} else {
				grail.use( skill );
			}
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
