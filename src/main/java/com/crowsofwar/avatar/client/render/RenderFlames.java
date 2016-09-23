package com.crowsofwar.avatar.client.render;

import java.util.Random;

import com.crowsofwar.avatar.common.entity.EntityFlames;
import com.crowsofwar.avatar.common.particle.AvatarParticleType;
import com.crowsofwar.avatar.common.particle.ParticleSpawner;
import com.crowsofwar.gorecore.util.Vector;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class RenderFlames extends Render<EntityFlames> {
	
	private final Random random;
	private final ParticleSpawner particleSpawner;
	
	/**
	 * @param renderManager
	 */
	public RenderFlames(RenderManager renderManager, ParticleSpawner particle) {
		super(renderManager);
		this.random = new Random();
		this.particleSpawner = particle;
	}
	
	@Override
	public void doRender(EntityFlames entity, double x, double y, double z, float entityYaw,
			float partialTicks) {
		
		particleSpawner.spawnParticles(entity.worldObj, AvatarParticleType.FLAMES, 1, 1,
				Vector.getEntityPos(entity), new Vector(0.02, 0.01, 0.02));
		// entity.worldObj.spawnParticle(AvatarParticles.getParticleFlames(), entity.posX,
		// entity.posY,
		// entity.posZ,
		// // entity.worldObj.spawnParticle(EnumParticleTypes.FLAME, entity.posX, entity.posY,
		// // entity.posZ,
		// (random.nextGaussian() - 0.5) * 0.02, random.nextGaussian() * 0.01,
		// (random.nextGaussian() - 0.5) * 0.02);
		
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityFlames entity) {
		return null;
	}
	
}