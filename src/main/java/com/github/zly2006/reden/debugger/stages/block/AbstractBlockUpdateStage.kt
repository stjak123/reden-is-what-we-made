package com.github.zly2006.reden.debugger.stages.block

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.access.TickStageOwnerAccess
import com.github.zly2006.reden.access.UpdaterData.Companion.updaterData
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.storage.BlocksResetStorage
import com.github.zly2006.reden.debugger.tree.StageTree
import com.github.zly2006.reden.network.TagBlockPos
import com.github.zly2006.reden.render.BlockBorder
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.block.ChainRestrictedNeighborUpdater
import net.minecraft.world.block.NeighborUpdater
import net.minecraft.world.block.ChainRestrictedNeighborUpdater as Updater119

abstract class AbstractBlockUpdateStage<T: Updater119.Entry>(
    name: String,
    parent: TickStage
): TickStage(name, parent), TickStageWithWorld {
    override val world get() = (parent as TickStageWithWorld).world
    abstract val entry: T
    val resetStorage = BlocksResetStorage()

    override fun tick() {
        super.tick()
        if (world == null) {
            error("World is null, are you ticking this stage at a client?")
        }
        world!!.server.data().breakpoints.checkBreakpointsForUpdating(this)
        world!!.neighborUpdater.updaterData().tickEntry(this)
    }

    /**
     * Actual tick logic from vanilla code.
     */
    abstract fun doTick()

    override fun reset() {
        if (world == null) {
            error("World is null, are you ticking this stage at a client?")
        }
        resetStorage.apply(world!!)
    }

    abstract val sourcePos: BlockPos
    abstract val targetPos: BlockPos?
    abstract val sourceBlock: Block
    override val displayName: MutableText
        get() = super.displayName.copy().append(" ").append(sourcePos.toShortString()).append(" -> ").append(targetPos?.toShortString())
            .append(" by ").append(sourceBlock.name)

    companion object {
        @Suppress("UNCHECKED_CAST", "KotlinConstantConditions")
        @JvmStatic
        fun <T : ChainRestrictedNeighborUpdater.Entry> createAndInsert(updater: NeighborUpdater, entry: T): AbstractBlockUpdateStage<T> {
            val stageOwnerAccess = entry as TickStageOwnerAccess
            if (stageOwnerAccess.`tickStage$reden` is AbstractBlockUpdateStage<*>) {
                error("Already has a block update stage")
            }
            val data = updater.updaterData()
            val parent = data.tickingStage
                ?: data.tickStageTree.peekNonBlockStage()
                ?: error("No parent stage found")
            val stage = when (entry) {
                is Updater119.StateReplacementEntry -> StageBlockPPUpdate(parent, entry)
                is Updater119.SixWayEntry -> StageBlockNCUpdateSixWay(parent, entry)
                is Updater119.StatefulEntry -> StageBlockNCUpdateWithSource(parent, entry)
                is Updater119.SimpleEntry -> StageBlockNCUpdate(parent, entry)
                else -> throw IllegalArgumentException("Unknown updater entry type: ${entry.javaClass}")
            } as AbstractBlockUpdateStage<T> // unchecked, but we know it's right
            stageOwnerAccess.`tickStage$reden` = stage

            data.tickStageTree.insert2childAtLast(parent, stage)
            return stage
        }
    }

    override fun focused(mc: MinecraftClient) {
        super.focused(mc)
        BlockBorder[sourcePos] = TagBlockPos.green
        if (targetPos != null) {
            BlockBorder[targetPos!!] = TagBlockPos.red
        }
    }

    override fun unfocused(mc: MinecraftClient) {
        super.unfocused(mc)
        BlockBorder.tags.clear()
    }
}

private fun StageTree.peekNonBlockStage(): TickStage? {
    var stage: TickStage? = this.peekLeaf()
    while (stage is AbstractBlockUpdateStage<*>) {
        stage = stage.parent
    }
    return stage
}
