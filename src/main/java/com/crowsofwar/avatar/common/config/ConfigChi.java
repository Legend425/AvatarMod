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
package com.crowsofwar.avatar.common.config;

import com.crowsofwar.gorecore.config.ConfigLoader;
import com.crowsofwar.gorecore.config.Load;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class ConfigChi {
	
	public static final ConfigChi CHI_CONFIG = new ConfigChi();
	
	private ConfigChi() {}
	
	@Load
	public float regenPerSecond = .05f, availablePerSecond = .4f, availableThreshold = .4f, regenInBed = 1f;
	
	@Load
	public float bonusLearnedBending = 16, bonusAbility = 6, bonusAbilityLevel = 3;
	
	@Load
	public float maxChiCap = 76;
	
	@Load
	public boolean infiniteInCreative = true;
	
	public static void load() {
		ConfigLoader.load(CHI_CONFIG, "avatar/chi.yml");
	}
	
}
