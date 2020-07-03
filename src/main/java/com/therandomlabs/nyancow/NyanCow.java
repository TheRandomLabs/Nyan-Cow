package com.therandomlabs.nyancow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod(NyanCow.MOD_ID)
public final class NyanCow {
	public static final String MOD_ID = "nyancow";

	private static final IParticleData[] particles = {
			new RedstoneParticleData(1.0F, 0.0F, 0.0F, 1.0F),
			new RedstoneParticleData(0.0F, 1.0F, 0.0F, 1.0F),
			new RedstoneParticleData(0.0F, 0.0F, 1.0F, 1.0F),
			new RedstoneParticleData(1.0F, 1.0F, 0.5F, 1.0F),
			new RedstoneParticleData(0.0F, 1.0F, 1.0F, 1.0F),
			new RedstoneParticleData(1.0F, 1.0F, 1.0F, 1.0F),
			new RedstoneParticleData(0.5F, 0.5F, 0.5F, 1.0F),
			new RedstoneParticleData(0.5F, 0.5F, 1.0F, 1.0F),
			new RedstoneParticleData(1.0F, 0.5F, 1.0F, 1.0F)
	};

	public NyanCow() {
		MinecraftForge.EVENT_BUS.addListener(this::onWorldTick);
	}

	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
			final ServerWorld world = (ServerWorld) event.world;
			world.getEntities(null, entity -> !entity.getPersistentData().getBoolean("NyanCow")).
					forEach(entity -> updateNyanCow(world, entity));
		}
	}

	public static void updateNyanCow(ServerWorld world, Entity entity) {
		removeNyanCow(world, entity);

		if (!entity.isAlive()) {
			return;
		}

		final CompoundNBT data = entity.getPersistentData();

		final CowEntity cow = new CowEntity(EntityType.COW, world);
		final Vector3d entityPosition = entity.getPositionVec();

		cow.setPositionAndRotation(
				entityPosition.getX(),
				entityPosition.getY() + entity.getEyeHeight() + 1.88,
				entityPosition.getZ() + 1.0,
				entity.getRotatedYaw(Rotation.NONE),
				entity.rotationPitch
		);
		cow.setRotationYawHead(entity.getRotationYawHead());

		cow.getPersistentData().putBoolean("NyanCow", true);
		cow.setInvulnerable(true);
		data.putUniqueId("NyanCowUUID", cow.getUniqueID());

		world.addEntity(cow);

		final Vector3d cowPosition = cow.getPositionVec();
		final Direction direction = Direction.getDirectionFacing(cow, Rotation.CLOCKWISE_90);

		world.spawnParticle(
				particles[(int) world.getGameTime() % particles.length],
				cowPosition.getX() + direction.getXDirection() * 12,
				cowPosition.getY(),
				cowPosition.getZ() + direction.getZDirection() * 12,
				//Number of particles
				10,
				direction.getXDirection() * 5,
				0.0,
				direction.getZDirection() * 5,
				//Speed
				10.0
		);
	}

	public static void removeNyanCow(ServerWorld world, Entity entity) {
		final CompoundNBT data = entity.getPersistentData();

		if (data.hasUniqueId("NyanCowUUID")) {
			final Entity cow = world.getEntityByUuid(data.getUniqueId("NyanCowUUID"));

			if (cow != null) {
				world.removeEntity(cow);
			}
		}
	}
}
