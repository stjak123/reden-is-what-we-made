package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage;
import com.github.zly2006.reden.network.GlobalStatus;
import com.github.zly2006.reden.transformers.RedenMixinExtension;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinServer implements ServerData.ServerDataAccess {
    @Unique ServerData serverData = new ServerData(Reden.MOD_VERSION, (MinecraftServer) (Object) this);

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"
            )
    )
    private void tickStageTree(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        serverData.realTicks++;
        // initialize the stage tree.
        assert serverData.getTickStage() != null;
        serverData.getTickStage().setShouldKeepTicking(shouldKeepTicking);
        if (RedenMixinExtension.APPLY_DEBUGGER_MIXINS) {
            serverData.getTickStage().tick();
            // tick the stage tree.
            while (serverData.getTickStageTree().hasNext()) {
                var stage = serverData.getTickStageTree().next();
                if (stage instanceof AbstractBlockUpdateStage<?>) {
                    if (RedenCarpetSettings.Options.redenDebug) {
                        throw new RuntimeException("AbstractBlockUpdateStage should not be in the stage tree.");
                    }
                }
                stage.tick();
            }
        }
    }

    @Inject(
            method = "stop",
            at = @At("HEAD")
    )
    private void stopping(CallbackInfo ci) {
        serverData.removeStatus(GlobalStatus.FROZEN);
        serverData.removeStatus(GlobalStatus.STARTED);
    }

    @NotNull
    @Override
    public ServerData getServerData$reden() {
        return serverData;
    }
}
