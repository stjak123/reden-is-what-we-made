package com.github.zly2006.reden.mixin.debugger.updateQueue;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.TickStageOwnerAccess;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = {
        ChainRestrictedNeighborUpdater.SixWayEntry.class,
        ChainRestrictedNeighborUpdater.SimpleEntry.class,
        ChainRestrictedNeighborUpdater.StatefulEntry.class,
        ChainRestrictedNeighborUpdater.StateReplacementEntry.class,
}, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class Mixin119UpdaterEntries implements TickStageOwnerAccess {
    @Unique boolean ticked = false;
    @Unique AbstractBlockUpdateStage<?> stage;

    @Override
    public boolean getTicked$reden() {
        return ticked;
    }

    @Override
    public void setTicked$reden(boolean ticked) {
        this.ticked = ticked;
    }

    @Override
    public @NotNull AbstractBlockUpdateStage<?> getTickStage$reden() {
        return stage;
    }

    @Override
    public void setTickStage$reden(TickStage tickStage) {
        stage = (AbstractBlockUpdateStage<?>) tickStage;
    }

    @Inject(
            method = "update",
            at = @At("HEAD"),
            cancellable = true
    )
    private void redirectUpdate(World world, CallbackInfoReturnable<Boolean> cir) {
        if (world == null) { // if only notify mixins
            if (ticked) {
                cir.setReturnValue(false);
                return;
            }
            if (stage == null) {
                throw new IllegalStateException("stage is null");
            }
            stage.doTick();
            cir.setReturnValue(false);
        }
    }
}
