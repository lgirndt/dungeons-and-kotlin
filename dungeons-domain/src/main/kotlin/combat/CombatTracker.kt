package io.dungeons.combat

interface CombatTrackerListener {
    fun afterRolledInitiative(combatants: List<Combatant>) {}
}

private val NOOP_COMBAT_TRACKER_LISTENER = object : CombatTrackerListener {}

class CombatTracker(
    combatants: Iterable<Combatant>,
    val listener: CombatTrackerListener = NOOP_COMBAT_TRACKER_LISTENER
) {
    private var round = 1
    private var turnIndex = 0

    val combatants: List<Combatant> = combatants
        .sortedByDescending { it.initiative }
        .also { listener.afterRolledInitiative(it) }
}