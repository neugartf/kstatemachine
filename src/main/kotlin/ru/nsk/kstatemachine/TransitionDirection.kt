package ru.nsk.kstatemachine

sealed class TransitionDirection

/**
 * Transition is triggered, but state is not changed
 */
internal object Stay : TransitionDirection()

fun stay(): TransitionDirection = Stay

/**
 * Transition should not be triggered
 */
internal object NoTransition : TransitionDirection()

fun noTransition(): TransitionDirection = NoTransition

/**
 * Transition is triggered with a [targetState]
 */
internal class TargetState(val targetState: State) : TransitionDirection()

fun targetState(targetState: State): TransitionDirection = TargetState(targetState)
