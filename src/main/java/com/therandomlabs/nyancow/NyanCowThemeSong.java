package com.therandomlabs.nyancow;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;

public class NyanCowThemeSong extends MovingSound {
	private final Entity entity;

	public NyanCowThemeSong(Entity entity) {
		super(NyanCow.NYAN_COW_THEME_SONG, SoundCategory.NEUTRAL);
		this.entity = entity;
		repeat = true;
		repeatDelay = 2;
	}

	@Override
	public void update() {
		if(entity.isDead) {
			donePlaying = true;
		} else {
			xPosF = (float) entity.getPositionVector().xCoord;
			yPosF = (float) entity.getPositionVector().yCoord;
			zPosF = (float) entity.getPositionVector().zCoord;
		}
	}
}
