package com.github.oxo42.stateless4j;
import com.github.oxo42.stateless4j.delegates.Action1;
import com.github.oxo42.stateless4j.delegates.Action2;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.triggers.TriggerBehaviour;
import com.github.oxo42.stateless4j.validation.Enforce;
import java.util.*;

public class StateRepresentation <TState, TTrigger>{

    private final TState state;,
    private final Map<TTrigger, List<TriggerBehaviour<TState, TTrigger>>> triggerBehaviours = new HashMap<>();,
    private final List<Action2<Transition<TState, TTrigger>, Object[]>> entryActions = new ArrayList<>();,
    private final List<Action1<Transition<TState, TTrigger>>> exitActions = new ArrayList<>();,
    private final List<StateRepresentation<TState, TTrigger>> substates = new ArrayList<>();,

    // null
    private StateRepresentation<TState, TTrigger> superstate;,

    public StateRepresentation(TState state) {
        this.state = state;
    }

    protected Map<TTrigger, List<TriggerBehaviour<TState, TTrigger>>> getTriggerBehaviours() {
        return triggerBehaviours;
    }

    <<<<<<< left_content.java
public Boolean canHandle(TTrigger trigger) {
        return tryFindHandler(trigger) != null;
=======
public boolean canHandle(TTrigger trigger) {
        try {
            tryFindHandler(trigger);
            return true;
        } catch (Exception e) {
            return false;
        }
>>>>>>> right_content.java
    }


    public TriggerBehaviour<TState, TTrigger> tryFindHandler(TTrigger trigger) {
        TriggerBehaviour result = tryFindLocalHandler(trigger);
        if (result == null && superstate != null) {
            result = superstate.tryFindHandler(trigger);
        }
        return result;
    }

    TriggerBehaviour<TState, TTrigger> tryFindLocalHandler(TTrigger trigger/*, out TriggerBehaviour handler*/) {
        List<TriggerBehaviour<TState, TTrigger>> possible = triggerBehaviours.get(trigger);
        if (possible == null) {
            return null;
        }

        List<TriggerBehaviour<TState, TTrigger>> actual = new ArrayList<>();
        for (TriggerBehaviour<TState, TTrigger> triggerBehaviour : possible) {
            if (triggerBehaviour.isGuardConditionMet()) {
                actual.add(triggerBehaviour);
            }
        }

        if (actual.size() > 1) {
            throw new IllegalStateException("Multiple permitted exit transitions are configured from state '" + trigger + "' for trigger '" + state + "'. Guard clauses must be mutually exclusive.");
        }

        return actual.get(0);
    }

    public void addEntryAction(final TTrigger trigger, final Action2<Transition<TState, TTrigger>, Object[]> action) {
        Enforce.argumentNotNull(action, "action");


        entryActions.add(new Action2<Transition<TState, TTrigger>, Object[]>() {
            @Override
            public void doIt(Transition<TState, TTrigger> t, Object[] args) {
                if (t.getTrigger().equals(trigger)) {
                    action.doIt(t, args);
                }
            }
        });
    }

    @Override
            public void doIt(Transition<TState, TTrigger> t, Object[] args) {
                if (t.getTrigger().equals(trigger)) {
                    action.doIt(t, args);
                }
            }

    public void addEntryAction(Action2<Transition<TState, TTrigger>, Object[]> action) {
        entryActions.add(Enforce.argumentNotNull(action, "action"));
    }

    public void insertEntryAction(Action2<Transition<TState, TTrigger>, Object[]> action) {
        entryActions.add(0, Enforce.argumentNotNull(action, "action"));
    }

    public void addExitAction(Action1<Transition<TState, TTrigger>> action) {
        exitActions.add(Enforce.argumentNotNull(action, "action"));
    }

    public void enter(Transition<TState, TTrigger> transition, Object... entryArgs) {
        Enforce.argumentNotNull(transition, "transtion");

        if (transition.isReentry()) {
            executeEntryActions(transition, entryArgs);
        } else if (!includes(transition.getSource())) {
            if (superstate != null) {
                superstate.enter(transition, entryArgs);
            }

            executeEntryActions(transition, entryArgs);
        }
    }

    public void exit(Transition<TState, TTrigger> transition) {
        Enforce.argumentNotNull(transition, "transtion");

        if (transition.isReentry()) {
            executeExitActions(transition);
        } else if (!includes(transition.getDestination())) {
            executeExitActions(transition);
            if (superstate != null) {
                superstate.exit(transition);
            }
        }
    }

    void executeEntryActions(Transition<TState, TTrigger> transition, Object[] entryArgs) {
        Enforce.argumentNotNull(transition, "transtion");
        Enforce.argumentNotNull(entryArgs, "entryArgs");
        for (Action2<Transition<TState, TTrigger>, Object[]> action : entryActions) {
            action.doIt(transition, entryArgs);
        }
    }

    void executeExitActions(Transition<TState, TTrigger> transition) {
        Enforce.argumentNotNull(transition, "transtion");
        for (Action1<Transition<TState, TTrigger>> action : exitActions) {
            action.doIt(transition);
        }
    }

    public void addTriggerBehaviour(TriggerBehaviour<TState, TTrigger> triggerBehaviour) {
        List<TriggerBehaviour<TState, TTrigger>> allowed;
        if (!triggerBehaviours.containsKey(triggerBehaviour.getTrigger())) {
            allowed = new ArrayList<>();
            triggerBehaviours.put(triggerBehaviour.getTrigger(), allowed);
        }
        allowed = triggerBehaviours.get(triggerBehaviour.getTrigger());
        allowed.add(triggerBehaviour);
    }

    public StateRepresentation<TState, TTrigger> getSuperstate() {
        return superstate;
    }

    public void setSuperstate(StateRepresentation<TState, TTrigger> value) {
        superstate = value;
    }

    public TState getUnderlyingState() {
        return state;
    }

    public void addSubstate(StateRepresentation<TState, TTrigger> substate) {
        Enforce.argumentNotNull(substate, "substate");
        substates.add(substate);
    }

    public boolean includes(TState stateToCheck) {
        for (StateRepresentation<TState, TTrigger> s : substates) {
            if (s.includes(stateToCheck)) {
                return true;
            }
        }
        return this.state.equals(stateToCheck);
    }

    public boolean isIncludedIn(TState stateToCheck) {
        return this.state.equals(stateToCheck) || (superstate != null && superstate.isIncludedIn(stateToCheck));
    }

    @SuppressWarnings("unchecked")
    public List<TTrigger> getPermittedTriggers() {
        Set<TTrigger> result = new HashSet<>();

        for (TTrigger t : triggerBehaviours.keySet()) {
            for (TriggerBehaviour<TState, TTrigger> v : triggerBehaviours.get(t)) {
                if (v.isGuardConditionMet()) {
                    result.add(t);
                    break;
                }
            }
        }

        if (getSuperstate() != null) {
            result.addAll(getSuperstate().getPermittedTriggers());
        }

        return new ArrayList<>(result);
    }

}