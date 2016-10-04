package com.crowsofwar.avatar.common.bending.air;

import com.crowsofwar.avatar.common.bending.AbilityContext;
import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.bending.BendingController;
import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.util.Raytrace.Info;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class AbilityAirJump extends BendingAbility<AirbendingState> {
	
	/**
	 * @param controller
	 */
	public AbilityAirJump(BendingController<AirbendingState> controller) {
		super(controller);
	}
	
	@Override
	public boolean requiresUpdateTick() {
		return false;
	}
	
	@Override
	public void execute(AbilityContext ctx) {
		
		ctx.addStatusControl(StatusControl.AIR_JUMP);
		
	}
	
	@Override
	public int getIconIndex() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public Info getRaytrace() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
