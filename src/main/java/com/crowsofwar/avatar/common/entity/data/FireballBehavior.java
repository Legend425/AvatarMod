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

package com.crowsofwar.avatar.common.entity.data;

import static com.crowsofwar.avatar.common.bending.BendingAbility.ABILITY_FIREBALL;
import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

import java.util.List;

import com.crowsofwar.avatar.common.AvatarDamageSource;
import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.data.AbilityData.AbilityTreePath;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.ctx.Bender;
import com.crowsofwar.avatar.common.entity.EntityFireball;
import com.crowsofwar.gorecore.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.world.World;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public abstract class FireballBehavior extends Behavior<EntityFireball> {
	
	public static final DataSerializer<FireballBehavior> DATA_SERIALIZER = new Behavior.BehaviorSerializer<>();
	
	public static int ID_NOTHING, ID_FALL, ID_PICKUP, ID_PLAYER_CONTROL, ID_THROWN;
	
	public static void register() {
		DataSerializers.registerSerializer(DATA_SERIALIZER);
		ID_NOTHING = registerBehavior(Idle.class);
		ID_PLAYER_CONTROL = registerBehavior(PlayerControlled.class);
		ID_THROWN = registerBehavior(Thrown.class);
	}
	
	public static class Idle extends FireballBehavior {
		
		@Override
		public FireballBehavior onUpdate(EntityFireball entity) {
			return this;
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {}
		
		@Override
		public void toBytes(PacketBuffer buf) {}
		
		@Override
		public void load(NBTTagCompound nbt) {}
		
		@Override
		public void save(NBTTagCompound nbt) {}
		
	}
	
	public static class Thrown extends FireballBehavior {
		
		int time = 0;
		
		@Override
		public FireballBehavior onUpdate(EntityFireball entity) {
			
			time++;
			
			if (entity.isCollided || (!entity.world.isRemote && time > 100)) {
				entity.setDead();
				entity.onCollideWithSolid();
			}
			
			entity.velocity().add(0, -9.81 / 40, 0);
			
			World world = entity.world;
			if (!entity.isDead) {
				List<Entity> collidedList = world.getEntitiesWithinAABBExcludingEntity(entity,
						entity.getExpandedHitbox());
				if (!collidedList.isEmpty()) {
					Entity collided = collidedList.get(0);
					if (collided instanceof EntityLivingBase && collided != entity.getOwner()) {
						collision((EntityLivingBase) collided, entity);
					} else if (collided != entity.getOwner()) {
						Vector motion = new Vector(collided).minus(new Vector(entity));
						motion.mul(0.3);
						motion.setY(0.08);
						collided.addVelocity(motion.x(), motion.y(), motion.z());
					}
					
				}
			}
			
			return this;
			
		}
		
		private void collision(EntityLivingBase collided, EntityFireball entity) {
			double speed = entity.velocity().magnitude();
			collided.attackEntityFrom(AvatarDamageSource.causeFireballDamage(collided, entity.getOwner()),
					entity.getDamage());
			collided.setFire(STATS_CONFIG.fireballSettings.fireTime);
			
			Vector motion = entity.velocity().dividedBy(20);
			motion.mul(STATS_CONFIG.fireballSettings.push);
			motion.setY(0.08);
			collided.addVelocity(motion.x(), motion.y(), motion.z());
			
			BendingData data = Bender.create(entity.getOwner()).getData();
			if (!collided.world.isRemote && data != null) {
				float xp = SKILLS_CONFIG.fireballHit;
				data.getAbilityData(ABILITY_FIREBALL).addXp(xp);
			}
			
			// Remove the fireball & spawn particles
			if (!entity.world.isRemote) entity.setDead();
			entity.onCollideWithSolid();
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {}
		
		@Override
		public void toBytes(PacketBuffer buf) {}
		
		@Override
		public void load(NBTTagCompound nbt) {}
		
		@Override
		public void save(NBTTagCompound nbt) {}
		
	}
	
	public static class PlayerControlled extends FireballBehavior {
		
		public PlayerControlled() {}
		
		@Override
		public FireballBehavior onUpdate(EntityFireball entity) {
			EntityLivingBase owner = entity.getOwner();
			
			if (owner == null) return this;
			
			BendingData data = Bender.create(owner).getData();
			
			double yaw = Math.toRadians(owner.rotationYaw);
			double pitch = Math.toRadians(owner.rotationPitch);
			Vector forward = Vector.toRectangular(yaw, pitch);
			Vector eye = Vector.getEyePos(owner);
			Vector target = forward.times(2).plus(eye);
			Vector motion = target.minus(new Vector(entity));
			motion.mul(5);
			entity.velocity().set(motion);
			
			if (data.getAbilityData(BendingAbility.ABILITY_FIREBALL).isMasterPath(AbilityTreePath.SECOND)) {
				int size = entity.getSize();
				if (size < 60 && entity.ticksExisted % 4 == 0) {
					entity.setSize(size + 1);
				}
			}
			
			return this;
		}
		
		@Override
		public void fromBytes(PacketBuffer buf) {}
		
		@Override
		public void toBytes(PacketBuffer buf) {}
		
		@Override
		public void load(NBTTagCompound nbt) {}
		
		@Override
		public void save(NBTTagCompound nbt) {}
		
	}
	
}
