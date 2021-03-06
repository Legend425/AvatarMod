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

package com.crowsofwar.avatar.common.entity;

import java.util.function.Consumer;

import com.crowsofwar.avatar.common.data.ctx.BenderInfo;
import com.crowsofwar.avatar.common.entity.data.OwnerAttribute;
import com.crowsofwar.avatar.common.util.AvatarDataSerializers;
import com.crowsofwar.gorecore.util.Vector;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityArc extends AvatarEntity {
	
	private static final DataParameter<Integer> SYNC_ID = EntityDataManager.createKey(EntityArc.class,
			DataSerializers.VARINT);
	private static final DataParameter<BenderInfo> SYNC_OWNER = EntityDataManager.createKey(EntityArc.class,
			AvatarDataSerializers.SERIALIZER_BENDER);
	
	private static int nextId = 1;
	private ControlPoint[] points;
	
	private final OwnerAttribute ownerAttrib;
	
	public EntityArc(World world) {
		super(world);
		float size = .2f;
		setSize(size, size);
		
		this.points = new ControlPoint[getAmountOfControlPoints()];
		for (int i = 0; i < points.length; i++) {
			points[i] = createControlPoint(size);
		}
		
		if (!world.isRemote) {
			setId(nextId++);
		}
		
		ownerAttrib = new OwnerAttribute(this, SYNC_OWNER, getNewOwnerCallback());
		
	}
	
	/**
	 * Called from the EntityArc constructor to create a new control point
	 * entity.
	 * 
	 * @param size
	 * @return
	 */
	protected ControlPoint createControlPoint(float size) {
		return new ControlPoint(this, size, 0, 0, 0);
	}
	
	/**
	 * Get a callback which is called when the owner is changed.
	 */
	protected Consumer<EntityLivingBase> getNewOwnerCallback() {
		return newOwner -> {
		};
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_ID, 0);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (this.ticksExisted == 1) {
			for (int i = 0; i < points.length; i++) {
				points[i].position().set(position());
			}
		}
		
		ignoreFrustumCheck = true;
		
		updateControlPoints();
		
	}
	
	private void updateControlPoints() {
		
		// Set leader at the arc pos
		
		getLeader().position().set(posX, posY, posZ);
		getLeader().velocity().set(velocity());
		
		// Move control points to follow leader
		
		for (int i = 1; i < points.length; i++) {
			
			ControlPoint leader = points[i - 1];
			ControlPoint p = points[i];
			Vector leadPos = leader.position();
			double sqrDist = p.position().sqrDist(leadPos);
			
			if (sqrDist > getControlPointTeleportDistanceSq()) {
				
				Vector toFollowerDir = p.position().minus(leader.position()).normalize();
				
				double idealDist = Math.sqrt(getControlPointTeleportDistanceSq());
				if (idealDist > 1) idealDist -= 1; // Make sure there is some
													// room
				
				Vector revisedOffset = leader.position().add(toFollowerDir.times(idealDist));
				p.position().set(revisedOffset);
				p.velocity().set(Vector.ZERO);
				
			} else if (sqrDist > getControlPointMaxDistanceSq()) {
				
				Vector diff = leader.position().minus(p.position());
				diff.normalize();
				diff.mul(3);
				p.velocity().add(diff);
				
			}
			
		}
		
		// Update velocity
		for (ControlPoint cp : points) {
			cp.onUpdate();
		}
		
	}
	
	protected abstract Vector getGravityVector();
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		tryDestroy();
		setDead();
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		super.setPosition(x, y, z);
		// Set position - called from entity constructor, so points might be
		// null
		if (points != null) {
			points[0].position().set(x, y, z);
		}
	}
	
	public ControlPoint[] getControlPoints() {
		return points;
	}
	
	public ControlPoint getControlPoint(int index) {
		return points[index];
	}
	
	/**
	 * Get the first control point in this arc.
	 */
	public ControlPoint getLeader() {
		return points[0];// EntityDragon
	}
	
	/**
	 * Get the leader of the specified control point.
	 */
	public ControlPoint getLeader(int index) {
		return points[index == 0 ? index : index - 1];
	}
	
	/**
	 * Get the id of this arc<br />
	 * NOT TO BE CONFUSED WITH {@link #getAvId()}
	 */
	public int getId() {
		return dataManager.get(SYNC_ID);
	}
	
	/**
	 * Set the id of this arc<br />
	 * NOT TO BE CONFUSED WITH {@link #setAvId()}
	 */
	public void setId(int id) {
		dataManager.set(SYNC_ID, id);
	}
	
	/**
	 * Uses id from {@link #getId()} not {@link #getAvId()}
	 */
	public static EntityArc findFromId(World world, int id) {
		for (Object obj : world.loadedEntityList) {
			if (obj instanceof EntityArc && ((EntityArc) obj).getId() == id) return (EntityArc) obj;
		}
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double d) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender() {
		return 15728880;
	}
	
	@Override
	public EntityLivingBase getOwner() {
		return ownerAttrib.getOwner();
	}
	
	public void setOwner(EntityLivingBase owner) {
		this.ownerAttrib.setOwner(owner);
		for (ControlPoint cp : points)
			cp.setOwner(owner);
	}
	
	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}
	
	/**
	 * Returns the amount of control points which will be created.
	 */
	public int getAmountOfControlPoints() {
		return 5;
	}
	
	/**
	 * Returns the maximum distance between control points, squared. Any control
	 * points beyond this distance will follow their leader to get closer.
	 */
	protected double getControlPointMaxDistanceSq() {
		return 1;
	}
	
	/**
	 * Returns the distance between control points to be teleported to their
	 * leader, squared. If any control point is more than this distance from its
	 * leader, then it is teleported to the leader.
	 */
	protected double getControlPointTeleportDistanceSq() {
		return 16;
	}
	
}
