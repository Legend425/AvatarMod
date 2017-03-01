/* 
  This file is part of AvatarMod.
    
  AvatarMod is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  AvatarMod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with AvatarMod. If not, see <http://www.gnu.org/licenses/>.
*/

package com.crowsofwar.avatar.client.gui;

import java.util.HashMap;
import java.util.Map;

import com.crowsofwar.avatar.common.bending.BendingAbility;

import net.minecraft.util.ResourceLocation;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class AvatarUiTextures {
	
	static final ResourceLocation radialMenu = new ResourceLocation("avatarmod",
			"textures/radial/circle_segment.png");
	static final ResourceLocation icons = new ResourceLocation("avatarmod", "textures/gui/ability_icons.png");
	static final ResourceLocation blurredIcons = new ResourceLocation("avatarmod",
			"textures/gui/blurred_icons.png");
	static final ResourceLocation skillsGui = new ResourceLocation("avatarmod", "textures/gui/skillmenu.png");
	
	static final ResourceLocation bgWater = new ResourceLocation("avatarmod",
			"textures/gui/bg_water_screen.png");
	static final ResourceLocation bgFire = new ResourceLocation("avatarmod",
			"textures/gui/bg_fire_screen.png");
	static final ResourceLocation bgAir = new ResourceLocation("avatarmod", "textures/gui/bg_air_screen.png");
	static final ResourceLocation bgEarth = new ResourceLocation("avatarmod",
			"textures/gui/bg_earth_screen.png");
	public static final ResourceLocation STATUS_CONTROL_ICONS = new ResourceLocation("avatarmod",
			"textures/gui/status_controls.png");
	
	public static final ResourceLocation WHITE = new ResourceLocation("avatarmod", "textures/gui/white.png");
	
	private static final Map<BendingAbility, ResourceLocation> abilityTextures = new HashMap<>();
	
	public static ResourceLocation getAbilityTexture(BendingAbility ability) {
		
		if (!abilityTextures.containsKey(ability)) {
			
			ResourceLocation location = new ResourceLocation("avatarmod",
					"textures/radial/icon_" + ability.getName() + ".png");
			abilityTextures.put(ability, location);
			return location;
			
		}
		return abilityTextures.get(ability);
		
	}
	
}
