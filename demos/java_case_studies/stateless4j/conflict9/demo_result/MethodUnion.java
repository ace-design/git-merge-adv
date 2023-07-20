package com.github.oxo42.stateless4j.triggers;
import com.github.oxo42.stateless4j.delegates.FuncBoolean;
import com.github.oxo42.stateless4j.OutVar;

public class IgnoredTriggerBehaviour <TState, TTrigger> extends TriggerBehaviour<TState, TTrigger>{


    public IgnoredTriggerBehaviour(TTrigger trigger, FuncBoolean guard) {
        super(trigger, guard);
    }


    

    <<<<<<< left_content.java
=======
@Override
    public TState resultsInTransitionFrom(TState source, Object... args) {
        throw new IllegalStateException();
    }
>>>>>>> right_content.java


    @Override
    public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
        return false;
    }


}