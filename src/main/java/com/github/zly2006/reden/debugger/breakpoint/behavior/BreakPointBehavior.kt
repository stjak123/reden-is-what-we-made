package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint

/**
 * 挂起
 * log
 * 统计信息
 */
abstract class BreakPointBehavior {
    var priority = 50; protected set
    abstract fun onBreakPoint(breakPoint: BreakPoint, event: Any)
}
