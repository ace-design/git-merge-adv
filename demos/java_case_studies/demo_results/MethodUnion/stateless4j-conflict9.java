package com.github.oxo42.stateless4j.triggers;
import com.github.oxo42.stateless4j.delegates.FuncBoolean;
import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.delegates.Func;

public class IgnoredTriggerBehaviour <TState, TTrigger> extends TriggerBehaviour<TState, TTrigger>{


    public IgnoredTriggerBehaviour(TTrigger trigger, FuncBoolean guard) {
        super(trigger, guard);
    }

    public IgnoredTriggerBehaviour(TTrigger trigger, Func<Boolean> guard) {
        super(trigger, guard);
    }

    @Override
    public TState resultsInTransitionFrom(TState source, Object... args) {
        throw new IllegalStateException();
    }

    @Override
    public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
        return false;
    }

}