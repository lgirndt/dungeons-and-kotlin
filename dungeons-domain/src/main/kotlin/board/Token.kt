package io.dungeons.board

import io.dungeons.core.Id

interface Token

{
    val id: Id<Token>

    val allowsMovementToSameSqqare: Boolean
}

data class EmptyToken(
    override val id: Id<Token>
) : Token {
    override val allowsMovementToSameSqqare: Boolean = true
}