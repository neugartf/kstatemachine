package ru.nsk.kstatemachine

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.then
import org.junit.jupiter.api.Test

class ConditionalTransitionTest {
    @Test
    fun conditionalTransitionStay() {
        val callbacks = mock<Callbacks>()

        val first = object : State("first") {}

        val stateMachine = createStateMachine {
            addState(first) {
                transitionConditionally<SwitchEvent> {
                    direction = { stay() }
                    onTriggered { callbacks.onTriggeringEvent(it.event) }
                }
                onEntry { callbacks.onEntryState(this) }
                onExit { callbacks.onExitState(this) }
            }
            setInitialState(first)
        }

        stateMachine.processEvent(SwitchEvent)
        then(callbacks).should().onEntryState(first)
        then(callbacks).should().onTriggeringEvent(SwitchEvent)
        then(callbacks).shouldHaveNoMoreInteractions()
    }

    @Test
    fun conditionalTransitionNoTransition() {
        val callbacks = mock<Callbacks>()

        val first = object : State("first") {}

        val stateMachine = createStateMachine {
            addState(first) {
                transitionConditionally<SwitchEvent> {
                    direction = { noTransition() }
                    onTriggered { callbacks.onTriggeringEvent(it.event) }
                }
                onEntry { callbacks.onEntryState(this) }
                onExit { callbacks.onExitState(this) }
            }
            onTransition { _, _, event, _ ->
                callbacks.onTriggeringEvent(event)
            }
            setInitialState(first)
        }

        then(callbacks).should().onEntryState(first)

        stateMachine.processEvent(SwitchEvent)
        then(callbacks).shouldHaveNoMoreInteractions()
    }

    @Test
    fun conditionalTransitionTargetState() {
        val callbacks = mock<Callbacks>()

        val first = object : State("first") {}
        val second = object : State("second") {}

        val stateMachine = createStateMachine {
            addState(first) {
                transitionConditionally<SwitchEvent> {
                    direction = { targetState(second) }
                    onTriggered {
                        callbacks.onTriggeringEvent(it.event)
                    }
                }
                onEntry { callbacks.onEntryState(this) }
                onExit { callbacks.onExitState(this) }
            }
            addState(second) {
                onEntry { callbacks.onEntryState(this) }
                onExit { callbacks.onExitState(this) }
            }

            setInitialState(first)
        }

        then(callbacks).should().onEntryState(first)

        stateMachine.processEvent(SwitchEvent)
        then(callbacks).should().onTriggeringEvent(SwitchEvent)
        then(callbacks).should().onExitState(first)
        then(callbacks).should().onEntryState(second)
        then(callbacks).shouldHaveNoMoreInteractions()
    }

    @Test
    fun conditionalTransition() {
        val callbacks = mock<Callbacks>()

        val first = object : State("first") {}
        val second = object : State("second") {}

        val stateMachine = createStateMachine {
            addState(first) {
                transitionConditionally<SwitchEvent> {
                    direction = { targetState(second) }
                    onTriggered { callbacks.onTriggeringEvent(it.event) }
                }
                onEntry { callbacks.onEntryState(this) }
                onExit { callbacks.onExitState(this) }
            }
            addState(second) {
                onEntry { callbacks.onEntryState(this) }
                onExit { callbacks.onExitState(this) }
            }

            setInitialState(first)
        }

        then(callbacks).should().onEntryState(first)

        stateMachine.processEvent(SwitchEvent)
        then(callbacks).should().onTriggeringEvent(SwitchEvent)
        then(callbacks).should().onExitState(first)
        then(callbacks).should().onEntryState(second)
        then(callbacks).shouldHaveNoMoreInteractions()
    }
}