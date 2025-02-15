package com.github.zly2006.reden.pearl

class MyEndPearlEntity {
    companion object {
        const val height = 0.25
        const val width = 0.25
    }

    val eyeY get() = height * 0.85F
    val motion = Mutable3d()
    val pos = Mutable3d()

    fun tick() {
        pos.add(motion)
        motion.multiply(0.99F.toDouble())
        motion.add(0.0, -0.03F.toDouble(), 0.0)
    }
}