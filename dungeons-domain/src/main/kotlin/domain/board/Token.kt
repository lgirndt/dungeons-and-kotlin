package io.dungeons.domain.board

import io.dungeons.port.TokenId

interface Token {
    val id: TokenId

    val layer: BoardLayer
        get() = BoardLayer.GROUND

    val allowsMovement: Boolean
        get() = true
    val allowsSight: Boolean
        get() = true
}
