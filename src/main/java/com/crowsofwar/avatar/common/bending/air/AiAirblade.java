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
package com.crowsofwar.avatar.common.bending.air;

import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.bending.BendingAi;
import com.crowsofwar.avatar.common.data.ctx.Bender;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class AiAirblade extends BendingAi {
	
	/**
	 * @param ability
	 * @param entity
	 * @param bender
	 */
	protected AiAirblade(BendingAbility ability, EntityLiving entity, Bender bender) {
		super(ability, entity, bender);
	}
	
	@Override
	protected void startExec() {
		EntityLivingBase target = entity.getAttackTarget();
		entity.getLookHelper().setLookPositionWithEntity(target, 20, 20);
		
	}
	
	@Override
	public boolean continueExecuting() {
		if (timeExecuting >= 15) {
			execAbility();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean shouldExecute() {
		
		EntityLivingBase target = entity.getAttackTarget();
		
		if (target != null) {
			double dist = entity.getDistanceSqToEntity(target);
			return dist >= 4 * 4;
		}
		
		return false;
		
	}
	
}
