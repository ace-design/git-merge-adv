package com.github.oxo42.stateless4j.triggers;

<<<<<<< HEAD
import com.github.oxo42.stateless4j.delegates.FuncBoolean;

public class IgnoredTriggerBehaviour<TState, TTrigger> extends TriggerBehaviour<TState, TTrigger> {
    public IgnoredTriggerBehaviour(TTrigger trigger, FuncBoolean guard) {
=======
import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.delegates.Func;

public class IgnoredTriggerBehaviour<TState, TTrigger> extends TriggerBehaviour<TState, TTrigger> {

    public IgnoredTriggerBehaviour(TTrigger trigger, Func<Boolean> guard) {
>>>>>>> d4c4092d
        super(trigger, guard);
    }

    @Override
<<<<<<< HEAD
    public TState resultsInTransitionFrom(TState source, Object... args) {
        throw new IllegalStateException();
=======
    public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
        return false;
>>>>>>> d4c4092d
    }
}
