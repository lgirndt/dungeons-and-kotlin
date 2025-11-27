package io.dungeons.board

import io.dungeons.core.Id

interface Token {
    val id: Id<Token>

    val layer: BoardLayer
        get() = BoardLayer.GROUND

    val allowsMovement: Boolean
        get() = true
    val allowsSight: Boolean
        get() = true
}
