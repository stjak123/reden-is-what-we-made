package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.tickPackets
import com.github.zly2006.reden.network.BreakPointInterrupt
import com.github.zly2006.reden.network.GlobalStatus
import com.github.zly2006.reden.network.StageTreeS2CPacket
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.server
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Util

class FreezeGame: BreakPointBehavior() {
    init {
        priority = 100
    }
    override fun onBreakPoint(breakPoint: BreakPoint, event: Any) {
        val tree = server.data().tickStageTree
        server.sendToAll(BreakPointInterrupt(breakPoint.id, tree, true))

        server.sendToAll(StageTreeS2CPacket(tree))
        server.data().addStatus(GlobalStatus.FROZEN)
            .let {
                GlobalStatus(it, NbtCompound().apply {
                    putString("reason", "game-paused")
                })
            }.let(server::sendToAll)

        while (server.data().hasStatus(GlobalStatus.FROZEN) && server.isRunning) {
            tickPackets(server)
        }


        server.timeReference = Util.getMeasuringTimeMs()
        server.sendToAll(BreakPointInterrupt(breakPoint.id, tree, false))
    }
}