package de.yaya.weathereffects.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import de.yaya.yayconfig.settings.SettingsStorage;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer
{
	@Shadow private static float red;
	@Shadow private static float green;
	@Shadow private static float blue;
	
	@Unique private static Vec3d color;
	
	@Inject(method = "render", at = @At("TAIL"))
	private static void onRender(Camera camera, float tickDelta, ClientWorld world, int i, float f, CallbackInfo ci)
	{
		if(!SettingsStorage.getBoolean("fog.enabled"))
			return;
		float g;
		Vec3d goalColor;
		if((g = world.getRainGradient(tickDelta)) > 0)
		{
			if(color == null)
				color = new Vec3d(0.7f, 0.82f, 1f);
			Vec3d pos = camera.getPos();
			Biome.Category biomeCategory = world.getBiome(new BlockPos(pos)).getCategory();
			switch(biomeCategory)
			{
				case DESERT -> goalColor = new Vec3d(2f, 1.6f, 0.8f);
				case MESA -> goalColor = new Vec3d(1.36f, 0.8f, 0.52f);
				case SWAMP -> goalColor = new Vec3d(1f, 1f, 1f);
				default -> goalColor = new Vec3d(0.7f, 0.82f, 1f);
			}
			float colorSpeed = (float)SettingsStorage.getDouble(biomeCategory.getName().toLowerCase().replace("_", "-") + ".fog.speed");
			color = color.lerp(goalColor.multiply(g).add(new Vec3d(1 - g, 1 - g, 1 - g)),
					tickDelta * 0.01f * colorSpeed);
			red *= color.x;
			green *= color.y;
			blue *= color.z;
			RenderSystem.clearColor(red, green, blue, 0f);
			RenderSystem.setShaderFogColor(red, green, blue, 1f);
		}
	}
}
