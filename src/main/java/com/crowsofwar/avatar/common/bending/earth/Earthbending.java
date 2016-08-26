package com.crowsofwar.avatar.common.bending.earth;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.bending.BendingController;
import com.crowsofwar.avatar.common.bending.BendingManager;
import com.crowsofwar.avatar.common.bending.IBendingState;
import com.crowsofwar.avatar.common.controls.AvatarControl;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.data.PlayerState;
import com.crowsofwar.avatar.common.gui.AvatarGuiIds;
import com.crowsofwar.avatar.common.gui.BendingMenuInfo;
import com.crowsofwar.avatar.common.gui.MenuTheme;
import com.crowsofwar.avatar.common.gui.MenuTheme.ThemeColor;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class Earthbending extends BendingController {
	
	private final BendingMenuInfo menu;
	private final List<Block> bendableBlocks;
	
	private final BendingAbility abilityPickUpBlock, abilityThrowBlock, abilityPutBlock;
	
	public Earthbending() {
		
		bendableBlocks = new ArrayList<Block>();
		bendableBlocks.add(Blocks.STONE);
		bendableBlocks.add(Blocks.SAND);
		bendableBlocks.add(Blocks.SANDSTONE);
		bendableBlocks.add(Blocks.COBBLESTONE);
		bendableBlocks.add(Blocks.DIRT);
		bendableBlocks.add(Blocks.GRAVEL);
		bendableBlocks.add(Blocks.BRICK_BLOCK);
		bendableBlocks.add(Blocks.MOSSY_COBBLESTONE);
		bendableBlocks.add(Blocks.NETHER_BRICK);
		bendableBlocks.add(Blocks.STONEBRICK);
		
		addAbility(this.abilityPickUpBlock = new AbilityPickUpBlock(this,
				state -> bendableBlocks.contains(state.getBlock())));
		addAbility(this.abilityThrowBlock = new AbilityThrowBlock(this));
		addAbility(this.abilityPutBlock = new AbilityPutBlock(this));
		
		Color light = new Color(225, 225, 225);
		Color brown = new Color(79, 57, 45);
		Color gray = new Color(90, 90, 90);
		Color lightBrown = new Color(255, 235, 224);
		ThemeColor background = new ThemeColor(lightBrown, brown);
		ThemeColor edge = new ThemeColor(brown, brown);
		ThemeColor icon = new ThemeColor(gray, light);
		menu = new BendingMenuInfo(new MenuTheme(background, edge, icon), AvatarControl.KEY_EARTHBENDING,
				AvatarGuiIds.GUI_RADIAL_MENU_EARTH, abilityPickUpBlock);
		
	}
	
	@Override
	public int getID() {
		return BendingManager.BENDINGID_EARTHBENDING;
	}
	
	@Override
	public IBendingState createState(AvatarPlayerData data) {
		return new EarthbendingState(data);
	}
	
	@Override
	public BendingAbility getAbility(AvatarPlayerData data, AvatarControl input) {
		PlayerState state = data.getState();
		EarthbendingState ebs = (EarthbendingState) data.getBendingState(this);
		EntityPlayer player = data.getPlayerEntity();
		// TODO [1.10] How does dual wielding even work?? (lol i didn't really play 1.9 that much)
		ItemStack holding = player.getActiveItemStack();
		
		if (ebs.getPickupBlock() != null) {
			if (input == AvatarControl.CONTROL_LEFT_CLICK_DOWN) return abilityThrowBlock;
			if (input == AvatarControl.CONTROL_RIGHT_CLICK_DOWN && holding == null
					|| (holding != null && !(holding.getItem() instanceof ItemBlock)))
				return abilityPutBlock;
		}
		
		return null;
	}
	
	@Override
	public BendingMenuInfo getRadialMenu() {
		return menu;
	}
	
	@Override
	public String getControllerName() {
		return "earthbending";
	}
	
}