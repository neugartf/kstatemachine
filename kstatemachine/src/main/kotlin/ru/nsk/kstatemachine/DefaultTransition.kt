package ru.nsk.kstatemachine

import java.util.concurrent.CopyOnWriteArraySet

open class DefaultTransition<E : Event>(
    override val name: String?,
    override val eventMatcher: EventMatcher<E>,
    sourceState: IState
) : InternalTransition<E> {
    private val _listeners = CopyOnWriteArraySet<Transition.Listener>()
    override val listeners: Collection<Transition.Listener> get() = _listeners

    private val _coListeners = CopyOnWriteArraySet<Transition.CoListener>()
    override val coListeners: Collection<Transition.CoListener> get() = _coListeners

    override val sourceState = sourceState as InternalState

    /**
     * Function that is called during event processing,
     * not during state machine configuration. So it is possible to check some outer (business logic) values in it.
     * If [Transition] does not have target state then [StateMachine] keeps current state
     * when such [Transition] is triggered.
     * This function should not have side effects.
     */
    private var targetStateDirectionProducer: TransitionDirectionProducer<E> = { stay() }

    override var argument: Any? = null

    constructor(
        name: String?,
        eventMatcher: EventMatcher<E>,
        sourceState: IState,
        targetState: IState?
    ) : this(name, eventMatcher, sourceState) {
        targetStateDirectionProducer = if (targetState == null) {
            { stay() }
        } else {
            { targetState(targetState) }
        }
    }

    constructor(
        name: String?,
        eventMatcher: EventMatcher<E>,
        sourceState: IState,
        targetStateDirectionProducer: TransitionDirectionProducer<E>
    ) : this(name, eventMatcher, sourceState) {
        this.targetStateDirectionProducer = targetStateDirectionProducer
    }

    override fun <L : Transition.Listener> addListener(listener: L): L {
        require(_listeners.add(listener)) { "$listener is already added" }
        return listener
    }

    override fun removeListener(listener: Transition.Listener) {
        _listeners.remove(listener)
    }

    override fun <L : Transition.CoListener> addCoListener(listener: L): L {
        require(_coListeners.add(listener)) { "$listener is already added" }
        return listener
    }

    override fun removeCoListener(listener: Transition.CoListener) {
        _coListeners.remove(listener)
    }

    override fun isMatchingEvent(event: Event) = eventMatcher.match(event)

    override fun produceTargetStateDirection(policy: TransitionDirectionProducerPolicy<E>) =
        targetStateDirectionProducer(policy)

    override fun toString() = "${this::class.simpleName}${if (name != null) "($name)" else ""}"
}