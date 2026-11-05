package com.colorwheel.mixin;

import com.colorwheel.SkyTint;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tiñe de rojo el color del cielo. */
@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void colorwheel$tintSky(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        float k = SkyTint.factor();
        if (k <= 0f) return;
        Vec3d c = cir.getReturnValue();
        // De noche el cielo rojo es más oscuro; de día, más brillante
        double day = Math.min(1.0, Math.max(0.35, Math.max(c.x, Math.max(c.y, c.z)) * 1.4));
        Vec3d red = new Vec3d(0.85 * day, 0.05 * day, 0.05 * day);
        cir.setReturnValue(c.multiply(1.0 - k).add(red.multiply(k)));
    }
}
