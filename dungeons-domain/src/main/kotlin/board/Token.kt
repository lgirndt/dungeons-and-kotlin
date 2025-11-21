package io.dungeons.board

import io.dungeons.core.Id

interface Token

{
    val id: Id<Token>

    val allowsMovement: Boolean
}

data class EmptyToken(
    override val id: Id<Token> = Id.generate(),
) : Token {
    override val allowsMovement: Boolean = true
}