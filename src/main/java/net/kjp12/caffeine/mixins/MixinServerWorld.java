package net.kjp12.caffeine.mixins;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {
    @Shadow
    private boolean allPlayersSleeping;

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;
    private int sleeping;

    /**
     * @reason Completely overriding the method since there is no reason to keep the vanilla code here.
     * @author KJP12
     */
    @Overwrite
    public void updateSleepingPlayers() {
        allPlayersSleeping = false;
        if (!players.isEmpty()) {
            sleeping = 0;
            int c1 = 0;
            for (var p : players) {
                if (p.isSpectator() || p.isCreative()) c1++;
                else if (p.isSleeping()) sleeping++;
            }
            allPlayersSleeping = sleeping > 0 && sleeping >= (players.size() - c1) * 0.33333333F;
        }
    }

    @Redirect(method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z"))
    public boolean caffeine$tick$proxyStream$canSkipTheNight(Stream<ServerPlayerEntity> self, Predicate<ServerPlayerEntity> predicate) {
        return self.filter(ServerPlayerEntity::isSleepingLongEnough).count() >= sleeping;
    }
}
