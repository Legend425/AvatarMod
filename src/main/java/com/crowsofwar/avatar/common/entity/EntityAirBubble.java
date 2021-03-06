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

import com.crowsofwar.avatar.AvatarMod;
import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.ctx.Bender;
import com.crowsofwar.avatar.common.data.ctx.BenderInfo;
import com.crowsofwar.avatar.common.entity.data.OwnerAttribute;
import com.crowsofwar.avatar.common.network.packets.PacketCErrorMessage;
import com.crowsofwar.avatar.common.util.AvatarDataSerializers;
import com.crowsofwar.avatar.common.util.AvatarUtils;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

import static com.crowsofwar.avatar.common.bending.BendingAbility.ABILITY_AIR_BUBBLE;
import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class EntityAirBubble extends AvatarEntity {
	
	public static final DataParameter<BenderInfo> SYNC_OWNER = EntityDataManager
			.createKey(EntityAirBubble.class, AvatarDataSerializers.SERIALIZER_BENDER);
	public static final DataParameter<Integer> SYNC_DISSIPATE = EntityDataManager
			.createKey(EntityAirBubble.class, DataSerializers.VARINT);
	public static final DataParameter<Float> SYNC_HEALTH = EntityDataManager.createKey(EntityAirBubble.class,
			DataSerializers.FLOAT);
	public static final DataParameter<Float> SYNC_MAX_HEALTH = EntityDataManager
			.createKey(EntityAirBubble.class, DataSerializers.FLOAT);
	public static final DataParameter<Boolean> SYNC_HOVERING = EntityDataManager
			.createKey(EntityAirBubble.class, DataSerializers.BOOLEAN);
	public static final DataParameter<Float> SYNC_SIZE = EntityDataManager.createKey(EntityAirBubble.class,
			DataSerializers.FLOAT);
	
	public static final UUID SLOW_ATTR_ID = UUID.fromString("40354c68-6e88-4415-8a6b-e3ddc56d6f50");
	public static final AttributeModifier SLOW_ATTR = new AttributeModifier(SLOW_ATTR_ID,
			"airbubble_slowness", -.3, 2);
	
	private final OwnerAttribute ownerAttr;
	private int airLeft;
	
	public EntityAirBubble(World world) {
		super(world);
		// setSize(2.5f, 2.5f);
		setSize(0, 0);
		this.ownerAttr = new OwnerAttribute(this, SYNC_OWNER);
		this.airLeft = 600;
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_DISSIPATE, 0);
		dataManager.register(SYNC_HEALTH, 20f);
		dataManager.register(SYNC_MAX_HEALTH, 20f);
		dataManager.register(SYNC_HOVERING, false);
		dataManager.register(SYNC_SIZE, 2.5f);
	}
	
	@Override
	public EntityLivingBase getOwner() {
		return ownerAttr.getOwner();
	}
	
	public void setOwner(EntityLivingBase owner) {
		ownerAttr.setOwner(owner);
	}
	
	@Override
	public EntityLivingBase getController() {
		return !isDissipating() ? getOwner() : null;
	}
	
	public boolean doesAllowHovering() {
		return dataManager.get(SYNC_HOVERING);
	}
	
	public void setAllowHovering(boolean floating) {
		dataManager.set(SYNC_HOVERING, floating);
	}
	
	public float getSize() {
		return dataManager.get(SYNC_SIZE);
	}
	
	public void setSize(float size) {
		dataManager.set(SYNC_SIZE, size);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		EntityLivingBase owner = getOwner();
		if (owner == null) {
			setDead();
			return;
		}
		if (owner.isDead) {
			dissipateSmall();
			return;
		}

		if (!world.isRemote && owner.isInsideOfMaterial(Material.WATER)) {
			owner.setAir(Math.min(airLeft, 300));
			airLeft--;
		}
		if (owner.isBurning()) {
			owner.extinguish();
		}
		
		setPosition(owner.posX, owner.posY, owner.posZ);

		Bender ownerBender = ownerAttr.getOwnerBender();
		if (!world.isRemote
				&& !ownerBender.getData().chi().consumeChi(STATS_CONFIG.chiAirBubbleOneSecond / 20f)) {

			dissipateSmall();

		}
		
		ItemStack chest = owner.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		boolean elytraOk = (STATS_CONFIG.allowAirBubbleElytra || chest.getItem() != Items.ELYTRA);
		if (!elytraOk && !world.isRemote) {
			AvatarMod.network.sendTo(new PacketCErrorMessage("avatar.airBubbleElytra"),
					(EntityPlayerMP) owner);
			dissipateSmall();
		}
		
		if (!isDissipating()) {
			IAttributeInstance attribute = owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
			if (attribute.getModifier(SLOW_ATTR_ID) == null) {
				attribute.applyModifier(SLOW_ATTR);
			}
			
			if (!owner.isInWater() && !ownerBender.isFlying() && chest.getItem() != Items.ELYTRA) {
				
				owner.motionY += .03;
				
				if (doesAllowHovering()) {
					
					if (doesAllowHovering() && !owner.isSneaking()) {
						handleHovering();
					} else {
						owner.motionY += 0.03;
					}
				}
				
			}
			
		}
		
		float size = getSize();
		
		if (isDissipatingLarge()) {
			setDissipateTime(getDissipateTime() + 1);
			float mult = 1 + getDissipateTime() / 10f;
			setSize(size * mult, size * mult);
			if (getDissipateTime() >= 10) {
				setDead();
			}
		} else if (isDissipatingSmall()) {
			setDissipateTime(getDissipateTime() - 1);
			float mult = 1 + getDissipateTime() / 40f;
			setSize(size * mult, size * mult);
			if (getDissipateTime() <= -10) {
				setDead();
			}
		} else {
			setSize(size, size);
		}
		
	}

	/**
	 * Handles hovering logic to make the owner hover. Preconditions (not in water, owner
	 * present, etc) are handled by the caller
	 */
	private void handleHovering() {

		if (getOwner() != null) {
			getOwner().fallDistance = 0;
		}

		// Min/max acceptable hovering distance
		// Hovering is allowed between these two values
		// Hover distance doesn't need to be EXACT
		final double minFloatHeight = 1.8;
		final double maxFloatHeight = 2.2;

		EntityLivingBase owner = getOwner();

		// Find whether there are blocks under the owner
		// Done by making a hitbox around the owner's feet and checking if there are blocks
		// colliding with that hitbox

		double x = owner.posX;
		double y = owner.posY;
		double z = owner.posZ;
		AxisAlignedBB hitbox = new AxisAlignedBB(x, y, z, x, y, z);
		hitbox = hitbox.grow(0.2, 0, 0.2);
		hitbox = hitbox.expand(0, -maxFloatHeight, 0);

		List<AxisAlignedBB> blockCollisions = world.getCollisionBoxes(null, hitbox);

		if (!blockCollisions.isEmpty()) {

			// Calculate the top-of-ground ground y position
			// Performed by finding the maximum ypos of each collided block
			double groundPosition = Double.MIN_VALUE;
			for (AxisAlignedBB blockHitbox : blockCollisions) {
				if (blockHitbox.maxY > groundPosition) {
					groundPosition = blockHitbox.maxY;
				}
			}
			// Now calculate the distance from ground
			// and use that to determine whether owner should float
			double distanceFromGround = owner.posY - groundPosition;

			// Tweak motion based on distance to ground, and target distance
			// Minecraft gravity is 0.08 blocks/tick

			if (distanceFromGround < minFloatHeight) {
				owner.motionY += 0.11;
			}
			if (distanceFromGround >= minFloatHeight && distanceFromGround < maxFloatHeight) {
				owner.motionY *= 0.7;
			}
			if (distanceFromGround >= maxFloatHeight) {
				owner.motionY += 0.07;

				// Avoid falling at over 3 m/s
				if (owner.motionY < -3.0 / 20) {
					owner.motionY = 0;
				}
			}

		}

	}
	
	@Override
	public void setDead() {
		super.setDead();
		EntityLivingBase owner = getOwner();
		if (owner != null) {
			IAttributeInstance attribute = owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
			if (attribute.getModifier(SLOW_ATTR_ID) != null) {
				attribute.removeModifier(SLOW_ATTR);
			}
		}
	}
	
	@Override
	protected void onCollideWithEntity(Entity entity) {
		
		double mult = -2;
		if (isDissipatingLarge()) mult = -4;
		Vector vel = new Vector(this.posX - entity.posX, this.posY - entity.posY, this.posZ - entity.posZ);
		vel.normalize();
		vel.mul(mult);
		vel.add(0, .3f, 0);
		
		double velX = vel.x(), velY = vel.y(), velZ = vel.z();
		
		// Need to use addVelocity() so avatar entities can detect it
		entity.motionX = entity.motionY = entity.motionZ = 0;
		// entity.addVelocity(velX, velY, velZ);
		entity.motionY = velY;
		entity.motionX = velX;
		entity.motionZ = velZ;
		if (entity instanceof AvatarEntity) {
			AvatarEntity avent = (AvatarEntity) entity;
			avent.velocity().set(velX, velY, velZ);
		}
		entity.isAirBorne = true;
		AvatarUtils.afterVelocityAdded(entity);
	}
	
	@Override
	protected boolean canCollideWith(Entity entity) {
		return entity != getOwner() && !(entity instanceof AvatarEntity) && !(entity instanceof EntityArrow);
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		ownerAttr.load(nbt);
		setDissipateTime(nbt.getInteger("Dissipate"));
		setHealth(nbt.getFloat("Health"));
		setAllowHovering(nbt.getBoolean("AllowHovering"));
		airLeft = nbt.getInteger("AirLeft");
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		ownerAttr.save(nbt);
		nbt.setInteger("Dissipate", getDissipateTime());
		nbt.setFloat("Health", getHealth());
		nbt.setBoolean("AllowHovering", doesAllowHovering());
		nbt.setInteger("AirLeft", airLeft);
	}
	
	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		
		EntityLivingBase owner = getOwner();
		if (owner != null) {
			
			if (!world.isRemote) {
				Entity sourceEntity = source.getTrueSource();
				if (sourceEntity != null) {
					if (!owner.isEntityInvulnerable(source)) {
						BendingData data = Bender.getData(owner);
						if (data.chi().consumeChi(STATS_CONFIG.chiAirBubbleTakeDamage * amount)) {
							
							data.getAbilityData(ABILITY_AIR_BUBBLE).addXp(SKILLS_CONFIG.airbubbleProtect);
							setHealth(getHealth() - amount);
							return true;
							
						} else {
							dissipateSmall();
							return true;
						}
					}
				}
			}
			
		} else {
			dissipateSmall();
			return true;
		}
		return false;
		
	}
	
	@Override
	public boolean tryDestroy() {
		return false;
	}
	
	public int getDissipateTime() {
		return dataManager.get(SYNC_DISSIPATE);
	}
	
	public void setDissipateTime(int dissipate) {
		dataManager.set(SYNC_DISSIPATE, dissipate);
	}
	
	public float getHealth() {
		return dataManager.get(SYNC_HEALTH);
	}
	
	public void setHealth(float health) {
		dataManager.set(SYNC_HEALTH, health);
		if (health <= 0) dissipateSmall();
		if (health > getMaxHealth()) health = getMaxHealth();
	}
	
	public float getMaxHealth() {
		return dataManager.get(SYNC_MAX_HEALTH);
	}
	
	public void setMaxHealth(float health) {
		dataManager.set(SYNC_MAX_HEALTH, health);
	}
	
	public void dissipateLarge() {
		if (!isDissipating()) setDissipateTime(1);
		removeStatCtrl();
	}
	
	public void dissipateSmall() {
		if (!isDissipating()) setDissipateTime(-1);
		removeStatCtrl();
	}
	
	public boolean isDissipating() {
		return getDissipateTime() != 0;
	}
	
	public boolean isDissipatingLarge() {
		return getDissipateTime() > 0;
	}
	
	public boolean isDissipatingSmall() {
		return getDissipateTime() < 0;
	}
	
	private void removeStatCtrl() {
		if (getOwner() != null) {
			BendingData data = Bender.create(getOwner()).getData();
			data.removeStatusControl(StatusControl.BUBBLE_EXPAND);
			data.removeStatusControl(StatusControl.BUBBLE_CONTRACT);
			
			IAttributeInstance attribute = getOwner()
					.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
			if (attribute.getModifier(SLOW_ATTR_ID) != null) {
				attribute.removeModifier(SLOW_ATTR);
			}
		}
	}
	
}
