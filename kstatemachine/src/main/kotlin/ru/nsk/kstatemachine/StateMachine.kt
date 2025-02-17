package ru.nsk.kstatemachine

import ru.nsk.kstatemachine.visitors.Visitor

@DslMarker
annotation class StateMachineDslMarker

interface StateMachine : State {
    var logger: Logger
    var ignoredEventHandler: IgnoredEventHandler
    var pendingEventHandler: PendingEventHandler

    /**
     * If machine catches exception from client code (listeners callbacks) it stores it until event processing
     * completes, and passes it to this handler. That keeps machine in well-defined predictable state and allows
     * to complete all required notifications.
     * Note that generally speaking listeners should not throw.
     *
     * Default implementation rethrows exception (only first one).
     * With your own handler you can mute or just log them for example.
     */
    var listenerExceptionHandler: ListenerExceptionHandler
    val isRunning: Boolean
    val machineListeners: Collection<Listener>

    /**
     * Allows the library to automatically call destroy() on current state owning machine instance if user tries
     * to reuse its states in another machine. Usually this is a result of using object states in sequentially created
     * similar machines. destroy() will be called on the previous machine instance.
     * If set to false an exception will be thrown on state reuse attempt.
     */
    val autoDestroyOnStatesReuse: Boolean
    val isDestroyed: Boolean

    val isUndoEnabled: Boolean

    fun <L : Listener> addListener(listener: L): L
    fun removeListener(listener: Listener)

    /**
     * Starts state machine
     */
    fun start(argument: Any? = null)

    /**
     * Forces state machine to stop
     */
    fun stop()

    /**
     * Machine must be started to process events
     */
    fun processEvent(event: Event, argument: Any? = null)

    /**
     * Destroys machine structure clearing all listeners, states etc.
     */
    fun destroy(stop: Boolean = true)

    fun log(lazyMessage: () -> String)

    override fun accept(visitor: Visitor) = visitor.visit(this)

    interface Listener {
        /**
         * Notifies that state machine started (entered initial state).
         */
        fun onStarted() = Unit

        /**
         * This method is called when transition is performed.
         * There might be many transitions from one state to another,
         * this method might be used to listen to all transitions in one place
         * instead of listening for each transition separately.
         */
        fun onTransition(transitionParams: TransitionParams<*>) = Unit

        /**
         * Same as [onTransition] but called after transition is complete and provides set of current active states.
         */
        fun onTransitionComplete(transitionParams: TransitionParams<*>, activeStates: Set<IState>) = Unit

        /**
         * Notifies about child state entry (including nested states).
         */
        fun onStateEntry(state: IState) = Unit

        /**
         * Notifies that state machine has stopped.
         */
        fun onStopped() = Unit
    }

    /**
     * State machine uses this interface to support internal logging on different platforms
     */
    fun interface Logger {
        fun log(message: String)
    }

    fun interface IgnoredEventHandler {
        fun onIgnoredEvent(event: Event, argument: Any?)
    }

    fun interface PendingEventHandler {
        fun onPendingEvent(pendingEvent: Event, argument: Any?)
    }

    fun interface ListenerExceptionHandler {
        fun onException(exception: Exception)
    }
}

fun StateMachine.restart(argument: Any? = null) {
    stop()
    start(argument)
}

typealias StateMachineBlock = StateMachine.() -> Unit

fun StateMachine.onStarted(block: StateMachine.() -> Unit) =
    addListener(object : StateMachine.Listener {
        override fun onStarted() = block()
    })

fun StateMachine.onStopped(block: StateMachine.() -> Unit) =
    addListener(object : StateMachine.Listener {
        override fun onStopped() = block()
    })

fun StateMachine.onTransition(block: StateMachine.(TransitionParams<*>) -> Unit) =
    addListener(object : StateMachine.Listener {
        override fun onTransition(transitionParams: TransitionParams<*>) =
            block(transitionParams)
    })

fun StateMachine.onTransitionComplete(block: StateMachine.(TransitionParams<*>, Set<IState>) -> Unit) =
    addListener(object : StateMachine.Listener {
        override fun onTransitionComplete(transitionParams: TransitionParams<*>, activeStates: Set<IState>) =
            block(transitionParams, activeStates)
    })

fun StateMachine.onStateEntry(block: StateMachine.(state: IState) -> Unit) =
    addListener(object : StateMachine.Listener {
        override fun onStateEntry(state: IState) = block(state)
    })

/**
 * Rolls back transition (usually it is navigating machine to previous state).
 * Previous states are stored in a stack, so this method mey be called multiple times if needed.
 * This function has same effect as alternative syntax processEvent(UndoEvent), but throws if undo feature is not enabled.
 */
fun StateMachine.undo(argument: Any? = null) {
    check(isUndoEnabled) {
        "Undo functionality is not enabled, use createStateMachine(isUndoEnabled = true) argument to enable it."
    }
    processEvent(UndoEvent, argument)
}

/**
 * Factory method for creating [StateMachine]
 */
fun createStateMachine(
    name: String? = null,
    childMode: ChildMode = ChildMode.EXCLUSIVE,
    start: Boolean = true,
    autoDestroyOnStatesReuse: Boolean = true,
    enableUndo: Boolean = false,
    init: StateMachineBlock
): StateMachine = StateMachineImpl(name, childMode, autoDestroyOnStatesReuse, enableUndo).apply {
    init()
    if (start) start()
}