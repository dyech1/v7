package com.colorwheel.mixin;

import com.colorwheel.SkyTint;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tiñe de rojo la niebla del horizonte, para que todo el cielo se vea rojo. */
@Mixin(Biome.class)
public class BiomeMixin {
    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void colorwheel$tintFog(CallbackInfoReturnable<Integer> cir) {
        float k = SkyTint.factor();
        if (k <= 0f) return;
        int c = cir.getReturnValue();
        float r = ((c >> 16) & 255) / 255f, g = ((c >> 8) & 255) / 255f, b = (c & 255) / 255f;
        int nr = (int) ((r * (1f - k) + 0.85f * k) * 255f);
        int ng = (int) ((g * (1f - k) + 0.05f * k) * 255f);
        int nb = (int) ((b * (1f - k) + 0.05f * k) * 255f);
        cir.setReturnValue((nr << 16) | (ng << 8) | nb);
    }
}
