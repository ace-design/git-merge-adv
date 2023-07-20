package com.github.oxo42.stateless4j.triggers;
import com.github.oxo42.stateless4j.delegates.Func;
import com.github.oxo42.stateless4j.delegates.Func2;
import com.github.oxo42.stateless4j.OutVar;


public class DynamicTriggerBehaviour<TState, TTrigger> extends TriggerBehaviour<TState, TTrigger> {

<<<<<<< left_content.java
    private Func2<Object[], TState> destination;
=======
    private final Func2<Object[], TState> destination;
>>>>>>> right_content.java

    public DynamicTriggerBehaviour(final TTrigger trigger, final Func2<Object[], TState> destination, final Func<Boolean> guard) {
        super(trigger, guard);
        assert destination != null : "destination is null";
        this.destination = destination;
    }

    @Override
    public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
        dest.set(destination.call(args));
        return true;
    }
}

