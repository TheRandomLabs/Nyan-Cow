package com.therandomlabs.nyancow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = NyanCow.MODID, version = NyanCow.VERSION,
		acceptedMinecraftVersions = NyanCow.ACCEPTED_MINECRAFT_VERSIONS,
		acceptableRemoteVersions = NyanCow.ACCEPTABLE_REMOTE_VERSIONS,
		updateJSON = NyanCow.UPDATE_JSON,
		certificateFingerprint = NyanCow.CERTIFICATE_FINGERPRINT)
public final class NyanCow {
	public static final String MODID = "randomtweaks";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.10,1.13)";
	public static final String ACCEPTABLE_REMOTE_VERSIONS = "*";
	public static final String UPDATE_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/Nyan-Cow/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	@SubscribeEvent
	public static void onWorldTick(WorldTickEvent event) {
		if(event.phase == Phase.END && event.side == Side.SERVER) {
			for(Entity entity : event.world.getEntities(Entity.class, entity -> true)) {
				if(!entity.getEntityData().getBoolean("NyanCow")) {
					updateNyanCow(event.world.getMinecraftServer(), entity);
				}
			}
		}
	}

	public static void removeNyanCow(MinecraftServer server, Entity entity) {
		final NBTTagCompound nbt = entity.getEntityData();

		if(nbt.hasUniqueId("NyanCowUUID")) {
			final Entity cow = server.getEntityFromUuid(nbt.getUniqueId("NyanCowUUID"));

			if(cow != null) {
				cow.getEntityWorld().removeEntity(cow);
			}
		}
	}

	public static void updateNyanCow(MinecraftServer server, Entity entity) {
		removeNyanCow(server, entity);
		
		if(entity.isDead) {
			return;
		}

		final NBTTagCompound nbt = entity.getEntityData();
		final WorldServer world = (WorldServer) entity.getEntityWorld();

		final EntityCow cow = new EntityCow(world);
		final BlockPos entityPosition = entity.getPosition();
		
		cow.setPositionAndRotation(
				entityPosition.getX(),
				entityPosition.getY() + entity.getEyeHeight() + 1.88,
				entityPosition.getZ() + 1,
				entity.getRotatedYaw(Rotation.NONE),
				entity.rotationPitch
		);
		cow.setRotationYawHead(entity.getRotationYawHead());

		cow.getEntityData().setBoolean("NyanCow", true);
		cow.setEntityInvulnerable(true);
		nbt.setUniqueId("NyanCowUUID", cow.getUniqueID());

		world.spawnEntity(cow);

		final BlockPos cowPosition = cow.getPosition();
		final Direction direction = Direction.getDirectionFacing(cow, Rotation.CLOCKWISE_90);

		world.spawnParticle(
				//Wool
				EnumParticleTypes.REDSTONE,
				//No long distances
				false,
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
}
