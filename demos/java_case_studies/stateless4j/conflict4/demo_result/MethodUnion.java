package com.github.oxo42.stateless4j.triggers;
import com.github.oxo42.stateless4j.delegates.Func;
import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.delegates.Func2;
import com.github.oxo42.stateless4j.validation.Enforce;

public class DynamicTriggerBehaviour <TState, TTrigger> extends TriggerBehaviour<TState, TTrigger>{

    private final Func2<Object[], TState> destination;,

    public DynamicTriggerBehaviour(TTrigger trigger, Func2<Object[], TState> destination, Func<Boolean> guard) {
        super(trigger, guard);
        this.destination = Enforce.argumentNotNull(destination, "destination");
    }

    public DynamicTriggerBehaviour(final TTrigger trigger, final Func2<Object[], TState> destination, final Func<Boolean> guard) {
        super(trigger, guard);
        assert destination != null : "destination is null";
        this.destination = destination;
    }

    public TState resultsInTransitionFrom(TState source, Object... args) {
        return destination.call(args);
    }

    @Override
    public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
        dest.set(destination.call(args));
        return true;
    }

}