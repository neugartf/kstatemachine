package ru.nsk.kstatemachine

import kotlinx.coroutines.CoroutineScope

class CoStateMachine(val scope: CoroutineScope, name: String?, childMode: ChildMode) :
    InternalStateMachine(name, childMode) {

    private val _coMachineListeners = mutableSetOf<CoListener>()
    val coMachineListeners: Collection<CoListener> get() = _coMachineListeners

    /**
     * Same as [StateMachine.Listener] but with suspend functions
     */
    interface CoListener {
        suspend fun onStarted() = Unit
        suspend fun onTransition(transitionParams: TransitionParams<*>) = Unit
        suspend fun onStateChanged(newState: IState) = Unit
        suspend fun onStopped() = Unit
    }
}

internal suspend fun CoStateMachine.coMachineNotify(block: suspend CoStateMachine.CoListener.() -> Unit) =
    coMachineListeners.forEach { it.block() }