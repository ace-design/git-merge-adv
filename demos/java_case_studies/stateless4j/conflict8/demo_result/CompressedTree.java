package com.github.oxo42.stateless4j.triggers;
import com.github.oxo42.stateless4j.delegates.Func2;
import com.github.oxo42.stateless4j.delegates.FuncBoolean;
import com.github.oxo42.stateless4j.validation.Enforce;
import com.github.oxo42.stateless4j.OutVar;


public class DynamicTriggerBehaviour<TState, TTrigger> extends TriggerBehaviour<TState, TTrigger> {

    private final Func2<Object[], TState> destination;

    public DynamicTriggerBehaviour(TTrigger trigger, Func2<Object[], TState> destination, FuncBoolean guard) {
        super(trigger, guard);
        this.destination = Enforce.argumentNotNull(destination, "destination");
    }

    @Override
<<<<<<< left_content.java
    public TState resultsInTransitionFrom(TState source, Object... args) {
        return destination.call(args);
=======
    public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
        dest.set(destination.call(args));
        return true;
>>>>>>> right_content.java
    }
}

