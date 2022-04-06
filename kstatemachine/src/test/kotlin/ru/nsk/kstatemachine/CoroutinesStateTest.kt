package ru.nsk.kstatemachine

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay

class CoroutinesStateTest : StringSpec({
    "call suspend function in a state listener" {
        createStateMachine {
            initialState("first") {
                onCoEntry { delay(0) }
                onCoExit { delay(0) }

                transition<SwitchEvent> {
                    onCoTriggered { delay(0) }
                }
            }
        }
    }
})