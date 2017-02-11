package com.therandomlabs.nyancow;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = NyanCow.MODID, version = NyanCow.VERSION,
		acceptedMinecraftVersions = NyanCow.ACCEPTED_MINECRAFT_VERSIONS)
public final class NyanCow {
	public static final String MODID = "nyancow";
	public static final String VERSION = "1.11.2-1.0.0.0";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.10,1.12)";

	@SidedProxy(clientSide = "com.therandomlabs.nyancow.ClientProxy",
			serverSide = "com.therandomlabs.nyancow.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Hah, take that, Forge
		if(!(proxy instanceof ClientProxy)) {
			MinecraftForge.EVENT_BUS.register(this);
		}
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		if(event.phase == Phase.END) {
			//Get server by creating a fake player because for some reason there's no
			//ServerTickEvent.getServer()
			final WorldServer world = DimensionManager.getWorld(0);
			final GameProfile profile = new GameProfile(UUID.randomUUID(), "FakePlayer");
			final FakePlayer fakePlayer = new FakePlayer(world, profile);

			for(Entity player : fakePlayer.getServer().getPlayerList().getPlayers()) {
				updateNyanCow(fakePlayer.getServer(), player);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLogOut(PlayerLoggedOutEvent event) {
		removeNyanCow(event.player.getServer(), event.player);
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if(event.getEntity() instanceof EntityPlayer) {
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
		cow.setPositionAndRotation(entityPosition.getX(), entityPosition.getY() + 3.5,
				entityPosition.getZ(), entity.getRotatedYaw(Rotation.NONE), entity.rotationPitch);
		cow.setRotationYawHead(entity.getRotationYawHead());
		nbt.setUniqueId("NyanCowUUID", cow.getUniqueID());

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
