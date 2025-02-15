package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.utils.isClient

val TAG_BLOCK_POS = Reden.identifier("tag_block_pos")
val TNT_SYNC_PACKET = Reden.identifier("tnt_sync_packet")
val RVC_DATA_SYNC = Reden.identifier("rvc_data_sync")
val RVC_TRACKPOINTS_C2S = Reden.identifier("rvc_trackpoints_c2s")
fun register() {
    Hello.register()
    TagBlockPos.register()
    Undo.register()
    UpdateBreakpointPacket.register()
    BreakPointInterrupt.register()
    StepOver.register()
    StepInto.register()
    Continue.register()
    TntSyncPacket.register()
    GlobalStatus.register()
    RvcTrackpointsC2SRequest.register()
    RvcDataS2CPacket.register()
    HopperCDSync.register()
    StageTreeS2CPacket.register()
    SyncBreakpointsPacket.register()
}
