package com.github.oxo42.stateless4j.triggers;
import com.github.oxo42.stateless4j.delegates.FuncBoolean;
import com.github.oxo42.stateless4j.OutVar;

public class IgnoredTriggerBehaviour<TState extends java.lang.Object, TTrigger extends java.lang.Object> extends TriggerBehaviour<TState, TTrigger> {
  public IgnoredTriggerBehaviour(TTrigger trigger, FuncBoolean guard) {
    super(trigger, guard);
  }

  @Override public boolean resultsInTransitionFrom(TState source, Object[] args, OutVar<TState> dest) {
    return false;
  }
}