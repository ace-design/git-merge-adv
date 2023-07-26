package com.github.oxo42.stateless4j.triggers;
import com.github.oxo42.stateless4j.delegates.FuncBoolean;
import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.delegates.Func;


public class IgnoredTriggerBehaviour<TState, TTrigger> extends TriggerBehaviour<TState, TTrigger> {
<<<<<<< left_content.java
    public IgnoredTriggerBehaviour(TTrigger trigger, FuncBoolean guard) {
=======

    public IgnoredTriggerBehaviour(TTrigger trigger, Func<Boolean> guard) {
>>>>>>> right_content.java
        super(trigger, guard);
    }

    @Override
<<<<<<< left_content.java
    public TState resultsInTransitionFrom(TState source, Object... args) {
        throw new IllegalStateException();
=======
    public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
        return false;
>>>>>>> right_content.java
    }
}

