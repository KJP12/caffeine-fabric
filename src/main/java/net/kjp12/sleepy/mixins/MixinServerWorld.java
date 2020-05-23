package net.kjp12.sleepy.mixins;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {
    @Shadow private boolean allPlayersSleeping;

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Shadow public abstract void method_29199(long l);

    @Shadow protected abstract void wakeSleepingPlayers();

    @Shadow protected abstract void resetWeather();

    protected MixinServerWorld(MutableWorldProperties mutableWorldProperties, DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, dimensionType, supplier, bl, bl2, l);
    }

    /**
     * @author KJP12
     */
    @Overwrite
    public void updateSleepingPlayers() {
        allPlayersSleeping = false;
        if(!players.isEmpty()) {
            int c1 = 0, c2 = 0;
            for(var p : players) {
                if(p.isSpectator() || p.isCreative()) c1++;
                else if(p.isSleeping()) c2++;
            }
            allPlayersSleeping = c2 > 0 && c2 >= (players.size() - c1) * 0.45F;
        }
    }

    @Inject(method="tick(Ljava/util/function/BooleanSupplier;)V", at = @At(value = "HEAD"))
    public void skipTheNight(CallbackInfo ci) {
        if(allPlayersSleeping) {
            for(var p : players) {
                if(p.isSpectator() || p.isCreative()) continue;
                if(p.isSleeping() && !p.isSleepingLongEnough()) return;
            }
            allPlayersSleeping = false;
            if (getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                long l = this.properties.getTimeOfDay() + 24000L;
                method_29199(l - l % 24000L);
            }

            wakeSleepingPlayers();
            if (getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
                resetWeather();
            }
        }
    }
}
