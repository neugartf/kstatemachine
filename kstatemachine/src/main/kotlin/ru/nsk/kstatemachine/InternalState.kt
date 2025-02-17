package ru.nsk.kstatemachine

import ru.nsk.kstatemachine.TransitionDirectionProducerPolicy.DefaultPolicy

/**
 * Defines state API for internal library usage. All states must implement this class.
 * Unfortunately cannot use interface for this purpose.
 */
abstract class InternalState : IState {
    override val parent: IState? get() = internalParent
    internal abstract val internalParent: InternalState?
    internal abstract fun setParent(parent: InternalState)

    internal abstract fun getCurrentStates(): List<InternalState>

    internal abstract fun doEnter(transitionParams: TransitionParams<*>)
    internal abstract fun doExit(transitionParams: TransitionParams<*>)
    internal abstract fun afterChildFinished(finishedChild: InternalState, transitionParams: TransitionParams<*>)
    internal open fun onParentCurrentStateChanged(currentState: InternalState) = Unit

    internal abstract fun <E : Event> recursiveFindUniqueResolvedTransition(
        eventAndArgument: EventAndArgument<E>
    ): ResolvedTransition<E>?

    internal abstract fun recursiveEnterInitialStates(transitionParams: TransitionParams<*>)
    internal abstract fun recursiveEnterStatePath(
        path: MutableList<InternalState>,
        transitionParams: TransitionParams<*>
    )

    internal abstract fun recursiveExit(transitionParams: TransitionParams<*>)
    internal abstract fun recursiveStop()

    /**
     * Called after each (including initial) transition completion.
     */
    internal abstract fun recursiveAfterTransitionComplete(transitionParams: TransitionParams<*>)
    internal abstract fun cleanup()
}

internal fun InternalState.requireParent() = requireNotNull(internalParent) { "$this parent is not set" }

internal fun InternalState.stateNotify(block: IState.Listener.() -> Unit) {
    val machine = machine as InternalStateMachine
    listeners.toList().forEach { machine.runDelayingException { it.block() } }
}

internal fun <E : Event> InternalState.findTransitionsByEvent(event: E): List<InternalTransition<E>> {
    val triggeringTransitions = transitions.filter { it.isMatchingEvent(event) }
    @Suppress("UNCHECKED_CAST")
    return triggeringTransitions as List<InternalTransition<E>>
}

internal fun <E : Event> InternalState.findUniqueResolvedTransition(eventAndArgument: EventAndArgument<E>): ResolvedTransition<E>? {
    val policy = DefaultPolicy(eventAndArgument)
    val transitions = findTransitionsByEvent(eventAndArgument.event)
        .map { it to it.produceTargetStateDirection(policy) }
        .filter { it.second !is NoTransition }
    check(transitions.size <= 1) { "Multiple transitions match ${eventAndArgument.event}, $transitions in $this" }
    return transitions.singleOrNull()
}