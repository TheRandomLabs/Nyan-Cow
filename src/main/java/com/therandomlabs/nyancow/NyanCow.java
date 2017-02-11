package com.therandomlabs.nyancow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = NyanCow.MODID, version = NyanCow.VERSION,
		acceptedMinecraftVersions = NyanCow.ACCEPTED_MINECRAFT_VERSIONS)
public final class NyanCow {
	public static final String MODID = "nyancow";
	public static final String VERSION = "1.11.2-1.0.0.0";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.10,1.12)";

	@EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if(event.phase == Phase.END && event.side == Side.SERVER) {
			for(Entity entity : event.world.getEntities(Entity.class, entity -> true)) {
				if(!entity.getEntityData().getBoolean("NyanCow")) {
					updateNyanCow(event.world.getMinecraftServer(), entity);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLogOut(PlayerLoggedOutEvent event) {
		removeNyanCow(event.player.getServer(), event.player);
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if(event.getEntity().getEntityData().getBoolean("NyanCow")) {
			event.setCanceled(true);
		} else {
			removeNyanCow(event.getEntity().getServer(), event.getEntity());
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
		cow.setPositionAndRotation(entityPosition.getX() + 1,
				entityPosition.getY() + entity.getEyeHeight() + 1.88,
				entityPosition.getZ() + 1,
				entity.getRotatedYaw(Rotation.NONE),
				entity.rotationPitch);
		cow.setRotationYawHead(entity.getRotationYawHead());
		cow.getEntityData().setBoolean("NyanCow", true);
		cow.setEntityInvulnerable(true);
		nbt.setUniqueId("NyanCowUUID", cow.getUniqueID());
		world.spawnEntity(cow);

		final BlockPos cowPosition = cow.getPosition();
		final Direction direction = Direction.getDirectionFacing(cow, Rotation.CLOCKWISE_90);
		world.spawnParticle(
				EnumParticleTypes.REDSTONE,								//Wool
				false,													//Not long distance
				cowPosition.getX() + direction.getXDirection() * 12,	//Where
				cowPosition.getY(),
				cowPosition.getZ() + direction.getZDirection() * 12,
				10,														//Number of particles
				direction.getXDirection() * 5,							//X offset
				0.0,													//Y offset
				direction.getZDirection() * 5,							//Z offset
				10.0													//Speed
		);
	}
}
