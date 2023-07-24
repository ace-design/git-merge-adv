try:
    from builtins import object
except ImportError:
    # python2
    pass
import inspect
import itertools
import logging

from collections import OrderedDict
from collections import defaultdict
from collections import deque
from functools import partial
from six import string_types

import warnings
# make deprecation warnings of transition visible for module users
warnings.filterwarnings(action='default', message=r"Starting from transitions version 0\.6\.0 .*")

logger = logging.getLogger(__name__)
logger.addHandler(logging.NullHandler())


def listify(obj):
    if obj is None:
        return []
    else:
        return obj if isinstance(obj, (list, tuple, type(None))) else [obj]


def get_trigger(model, trigger_name, *args, **kwargs):
    func = getattr(model, trigger_name, None)
    if func:
        return func(*args, **kwargs)
    raise AttributeError("Model has no trigger named '%s'" % trigger_name)


def prep_ordered_arg(desired_length, arg_name):
    """Ensure arguments to add_ordered_transitions are the proper length and
    replicate the given argument if only one given (apply same condition, callback
    to all transitions)
    """
    arg_name = listify(arg_name) if arg_name else [None]
    if len(arg_name) != desired_length and len(arg_name) != 1:
        raise ValueError("Argument length must be either 1 or the same length as "
                         "the number of transitions.")
    if len(arg_name) == 1:
        return arg_name * desired_length
    return arg_name


class State(object):

    dynamic_methods = ['on_enter', 'on_exit']

    def __init__(self, name, on_enter=None, on_exit=None,
                 ignore_invalid_triggers=False):
        """
        Args:
            name (string): The name of the state
            on_enter (string, list): Optional callable(s) to trigger when a
                state is entered. Can be either a string providing the name of
                a callable, or a list of strings.
            on_exit (string, list): Optional callable(s) to trigger when a
                state is exited. Can be either a string providing the name of a
                callable, or a list of strings.
            ignore_invalid_triggers (Boolean): Optional flag to indicate if
                unhandled/invalid triggers should raise an exception

        """
        self.name = name
        self.ignore_invalid_triggers = ignore_invalid_triggers
        self.on_enter = listify(on_enter) if on_enter else []
        self.on_exit = listify(on_exit) if on_exit else []

    def enter(self, event_data):
        """ Triggered when a state is entered. """
        logger.debug("%sEntering state %s. Processing callbacks...", event_data.machine.name, self.name)
        for oe in self.on_enter:
            event_data.machine._callback(oe, event_data)
        logger.info("%sEntered state %s", event_data.machine.name, self.name)

    def exit(self, event_data):
        """ Triggered when a state is exited. """
        logger.debug("%sExiting state %s. Processing callbacks...", event_data.machine.name, self.name)
        for oe in self.on_exit:
            event_data.machine._callback(oe, event_data)
        logger.info("%sExited state %s", event_data.machine.name, self.name)

    def add_callback(self, trigger, func):
        """ Add a new enter or exit callback.
        Args:
            trigger (string): The type of triggering event. Must be one of
                'enter' or 'exit'.
            func (string): The name of the callback function.
        """
        callback_list = getattr(self, 'on_' + trigger)
        callback_list.append(func)

    def __repr__(self):
        return "<%s('%s')@%s>" % (type(self).__name__, self.name, id(self))


class Condition(object):

    def __init__(self, func, target=True):
        """
        Args:
            func (string): Name of the condition-checking callable
            target (bool): Indicates the target state--i.e., when True,
                the condition-checking callback should return True to pass,
                and when False, the callback should return False to pass.
        Notes:
            This class should not be initialized or called from outside a
            Transition instance, and exists at module level (rather than
            nesting under the transition class) only because of a bug in
            dill that prevents serialization under Python 2.7.
        """
        self.func = func
        self.target = target

    def check(self, event_data):
        """ Check whether the condition passes.
        Args:
            event_data (EventData): An EventData instance to pass to the
            condition (if event sending is enabled) or to extract arguments
            from (if event sending is disabled). Also contains the data
            model attached to the current machine which is used to invoke
            the condition.
        """
        predicate = getattr(event_data.model, self.func) if isinstance(self.func, string_types) else self.func

        if event_data.machine.send_event:
            return predicate(event_data) == self.target
        else:
            return predicate(*event_data.args, **event_data.kwargs) == self.target

    def __repr__(self):
        return "<%s(%s)@%s>" % (type(self).__name__, self.func, id(self))


class Transition(object):

    dynamic_methods = ['before', 'after', 'prepare']

    def __init__(self, source, dest, conditions=None, unless=None, before=None,
                 after=None, prepare=None):
        """
        Args:
            source (string): The name of the source State.
            dest (string): The name of the destination State.
            conditions (string, list): Condition(s) that must pass in order for
                the transition to take place. Either a string providing the
                name of a callable, or a list of callables. For the transition
                to occur, ALL callables must return True.
            unless (string, list): Condition(s) that must return False in order
                for the transition to occur. Behaves just like conditions arg
                otherwise.
            before (string or list): callbacks to trigger before the
                transition.
            after (string or list): callbacks to trigger after the transition.
            prepare (string or list): callbacks to trigger before conditions are checked
        """
        self.source = source
        self.dest = dest
        self.prepare = [] if prepare is None else listify(prepare)
        self.before = [] if before is None else listify(before)
        self.after = [] if after is None else listify(after)

        self.conditions = []
        if conditions is not None:
            for c in listify(conditions):
                self.conditions.append(Condition(c))
        if unless is not None:
            for u in listify(unless):
                self.conditions.append(Condition(u, target=False))

    def execute(self, event_data):
        """ Execute the transition.
        Args:
            event: An instance of class EventData.
        Returns: boolean indicating whether or not the transition was
            successfully executed (True if successful, False if not).
        """
        logger.debug("%sInitiating transition from state %s to state %s...",
                     event_data.machine.name, self.source, self.dest)
        machine = event_data.machine

        for func in self.prepare:
            machine._callback(func, event_data)
            logger.debug("Executed callback '%s' before conditions." % func)

        for c in self.conditions:
            if not c.check(event_data):
                logger.debug("%sTransition condition failed: %s() does not " +
                             "return %s. Transition halted.", event_data.machine.name, c.func, c.target)
                return False
        for func in itertools.chain(machine.before_state_change, self.before):
            machine._callback(func, event_data)
            logger.debug("%sExecuted callback '%s' before transition.", event_data.machine.name, func)

        self._change_state(event_data)

        for func in itertools.chain(self.after, machine.after_state_change):
            machine._callback(func, event_data)
            logger.debug("%sExecuted callback '%s' after transition.", event_data.machine.name, func)
        return True

    def _change_state(self, event_data):
        event_data.machine.get_state(self.source).exit(event_data)
        event_data.machine.set_state(self.dest, event_data.model)
        event_data.update(event_data.model)
        event_data.machine.get_state(self.dest).enter(event_data)

    def add_callback(self, trigger, func):
        """ Add a new before, after, or prepare callback.
        Args:
            trigger (string): The type of triggering event. Must be one of
                'before', 'after' or 'prepare'.
            func (string): The name of the callback function.
        """
        callback_list = getattr(self, trigger)
        callback_list.append(func)

    def __repr__(self):
        return "<%s('%s', '%s')@%s>" % (type(self).__name__,
                                        self.source, self.dest, id(self))


class EventData(object):

    def __init__(self, state, event, machine, model, args, kwargs):
        """
        Args:
            state (State): The State from which the Event was triggered.
            event (Event): The triggering Event.
            machine (Machine): The current Machine instance.
            model (object): The model/object the machine is bound to.
            args (list): Optional positional arguments from trigger method
                to store internally for possible later use.
            kwargs (dict): Optional keyword arguments from trigger method
                to store internally for possible later use.
        """
        self.state = state
        self.event = event
        self.machine = machine
        self.model = model
        self.args = args
        self.kwargs = kwargs
        self.transition = None
        self.error = None
        self.result = False

    def update(self, model):
        """ Updates the current State to accurately reflect the Machine. """
        self.state = self.machine.get_state(model.state)

    def __repr__(self):
        return "<%s('%s', %s)@%s>" % (type(self).__name__, self.state,
                                      getattr(self, 'transition'), id(self))


class Event(object):

    def __init__(self, name, machine):
        """
        Args:
            name (string): The name of the event, which is also the name of the
                triggering callable (e.g., 'advance' implies an advance()
                method).
            machine (Machine): The current Machine instance.
        """
        self.name = name
        self.machine = machine
        self.transitions = defaultdict(list)

    def add_transition(self, transition):
        """ Add a transition to the list of potential transitions.
        Args:
            transition (Transition): The Transition instance to add to the
                list.
        """
        self.transitions[transition.source].append(transition)

    def trigger(self, model, *args, **kwargs):
        f = partial(self._trigger, model, *args, **kwargs)
        return self.machine._process(f)

    def _trigger(self, model, *args, **kwargs):
        """ Serially execute all transitions that match the current state,
        halting as soon as one successfully completes.
        Args:
            args and kwargs: Optional positional or named arguments that will
                be passed onto the EventData object, enabling arbitrary state
                information to be passed on to downstream triggered functions.
        Returns: boolean indicating whether or not a transition was
            successfully executed (True if successful, False if not).
        """
        state = self.machine.get_state(model.state)
        if state.name not in self.transitions:
            msg = "%sCan't trigger event %s from state %s!" % (self.machine.name, self.name,
                                                               state.name)
            if state.ignore_invalid_triggers:
                logger.warning(msg)
                return False
            else:
                raise MachineError(msg)
        event_data = EventData(state, self, self.machine, model, args=args, kwargs=kwargs)

        for func in self.machine.prepare_event:
            self.machine._callback(func, event_data)
            logger.debug("Executed machine preparation callback '%s' before conditions." % func)

        try:
            for t in self.transitions[state.name]:
                event_data.transition = t
                if t.execute(event_data):
                    event_data.result = True
                    break
        except Exception as e:
            event_data.error = e
            raise
        finally:
            for func in self.machine.finalize_event:
                self.machine._callback(func, event_data)
                logger.debug("Executed machine finalize callback '%s'." % func)
        return event_data.result

    def __repr__(self):
        return "<%s('%s')@%s>" % (type(self).__name__, self.name, id(self))

    def add_callback(self, trigger, func):
        """ Add a new before or after callback to all available transitions.
        Args:
            trigger (string): The type of triggering event. Must be one of
                'before', 'after' or 'prepare'.
            func (string): The name of the callback function.
        """
        for t in itertools.chain(*self.transitions.values()):
            t.add_callback(trigger, func)


class Machine(object):

    separator = '_'  # separates callback type from state/transition name
    wildcard_all = '*'  # will be expanded to ALL states
    wildcard_same = '='  # will be expanded to source state
    state_cls = State
    transition_cls = Transition
    event_cls = Event

    def __init__(self, model='self', states=None, initial='initial', transitions=None,
                 send_event=False, auto_transitions=True,
                 ordered_transitions=False, ignore_invalid_triggers=None,
                 before_state_change=None, after_state_change=None, name=None,
                 queued=False, prepare_event=None, finalize_event=None, **kwargs):
        """
        Args:
            model (object or list): The object(s) whose states we want to manage. If 'self',
                the current Machine instance will be used the model (i.e., all
                triggering events will be attached to the Machine itself). Note that an empty list
                is treated like no model.
            states (list): A list of valid states. Each element can be either a
                string or a State instance. If string, a new generic State
                instance will be created that has the same name as the string.
            initial (string or State): The initial state of the Machine.
            transitions (list): An optional list of transitions. Each element
                is a dictionary of named arguments to be passed onto the
                Transition initializer.
            send_event (boolean): When True, any arguments passed to trigger
                methods will be wrapped in an EventData object, allowing
                indirect and encapsulated access to data. When False, all
                positional and keyword arguments will be passed directly to all
                callback methods.
            auto_transitions (boolean): When True (default), every state will
                automatically have an associated to_{state}() convenience
                trigger in the base model.
            ordered_transitions (boolean): Convenience argument that calls
                add_ordered_transitions() at the end of initialization if set
                to True.
            ignore_invalid_triggers: when True, any calls to trigger methods
                that are not valid for the present state (e.g., calling an
                a_to_b() trigger when the current state is c) will be silently
                ignored rather than raising an invalid transition exception.
            before_state_change: A callable called on every change state before
                the transition happened. It receives the very same args as normal
                callbacks.
            after_state_change: A callable called on every change state after
                the transition happened. It receives the very same args as normal
                callbacks.
            name: If a name is set, it will be used as a prefix for logger output
            queued (boolean): When True, processes transitions sequentially. A trigger
                executed in a state callback function will be queued and executed later.
                Due to the nature of the queued processing, all transitions will
                _always_ return True since conditional checks cannot be conducted at queueing time.
            prepare_event: A callable called on for before possible transitions will be processed.
                It receives the very same args as normal callbacks.
            finalize_event: A callable called on for each triggered event after transitions have been processed.
                This is also called when a transition raises an exception.

            **kwargs additional arguments passed to next class in MRO. This can be ignored in most cases.
        """

        if kwargs.pop('add_self', None) is not None:
            warnings.warn("Starting from transitions version 0.6.0 'add_self' is no longer"
                          "supported. To add the machine as a model use the new default "
                          "value model='self' instead.", DeprecationWarning)

        try:
            super(Machine, self).__init__(**kwargs)
        except TypeError as err:
            raise ValueError('Passing arguments {0} caused an inheritance error: {1}'.format(kwargs.keys(), err))

        # initialize protected attributes first
        self._queued = queued
        self._transition_queue = deque()
        self._before_state_change = []
        self._after_state_change = []
        self._prepare_event = []
        self._finalize_event = []
        self._initial = None

        self.states = OrderedDict()
        self.events = {}
        self.send_event = send_event
        self.auto_transitions = auto_transitions
        self.ignore_invalid_triggers = ignore_invalid_triggers
        self.prepare_event = prepare_event
        self.before_state_change = before_state_change
        self.after_state_change = after_state_change
        self.finalize_event = finalize_event
        self.name = name + ": " if name is not None else ""

        self.models = []

        if states is not None:
            self.add_states(states)

        if initial is not None:
            self.initial = initial

        if transitions is not None:
            self.add_transitions(transitions)

        if ordered_transitions:
            self.add_ordered_transitions()

        if model:
            self.add_model(model)

    def add_model(self, model, initial=None):
        """ Register a model with the state machine, initializing triggers and callbacks. """
        models = listify(model)

        if initial is None:
            if self.initial is None:
                raise ValueError("No initial state configured for machine, must specify when adding model.")
            else:
                initial = self.initial

        for model in models:
            model = self if model == 'self' else model
            if model not in self.models:
                if hasattr(model, 'trigger'):
                    logger.warning("%sModel already contains an attribute 'trigger'. Skip method binding ",
                                   self.name)
                else:
                    model.trigger = partial(get_trigger, model)

                for trigger, _ in self.events.items():
                    self._add_trigger_to_model(trigger, model)

                for _, state in self.states.items():
                    self._add_model_to_state(state, model)

                self.set_state(initial, model=model)
                self.models.append(model)

    def remove_model(self, model):
        """ Remove a model from the state machine. The model will still contain all previously added triggers
        and callbacks, but will not receive updates when states or transitions are added to the Machine. """
        models = listify(model)

        for model in models:
            self.models.remove(model)

    @classmethod
    def _create_transition(cls, *args, **kwargs):
        return cls.transition_cls(*args, **kwargs)

    @classmethod
    def _create_event(cls, *args, **kwargs):
        return cls.event_cls(*args, **kwargs)

    @classmethod
    def _create_state(cls, *args, **kwargs):
        return cls.state_cls(*args, **kwargs)

    @property
    def initial(self):
        """ Return the initial state. """
        return self._initial

    @initial.setter
    def initial(self, value):
        if isinstance(value, State):
            if value.name not in self.states:
                self.add_state(value)
            else:
                assert self._has_state(value)
            self._initial = value.name
        else:
            if value not in self.states:
                self.add_state(value)
            self._initial = value

    @property
    def has_queue(self):
        """ Return boolean indicating if machine has queue or not """
        return self._queued

    @property
    def model(self):
        if len(self.models) == 1:
            return self.models[0]
        else:
            return self.models

    @property
    def before_state_change(self):
        return self._before_state_change

    # this should make sure that _before_state_change is always a list
    @before_state_change.setter
    def before_state_change(self, value):
        self._before_state_change = listify(value)

    @property
    def after_state_change(self):
        return self._after_state_change

    # this should make sure that _after_state_change is always a list
    @after_state_change.setter
    def after_state_change(self, value):
        self._after_state_change = listify(value)

    @property
    def prepare_event(self):
        return self._prepare_event

    # this should make sure that prepare_event is always a list
    @prepare_event.setter
    def prepare_event(self, value):
        self._prepare_event = listify(value)

    @property
    def finalize_event(self):
        return self._finalize_event

    # this should make sure that finalize_event is always a list
    @finalize_event.setter
    def finalize_event(self, value):
        self._finalize_event = listify(value)

    def is_state(self, state, model):
        """ Check whether the current state matches the named state. """
        return model.state == state

    def get_state(self, state):
        """ Return the State instance with the passed name. """
        if state not in self.states:
            raise ValueError("State '%s' is not a registered state." % state)
        return self.states[state]

    def set_state(self, state, model=None):
        """ Set the current state. """
        if isinstance(state, string_types):
            state = self.get_state(state)
        models = self.models if model is None else listify(model)
        for m in models:
            m.state = state.name

    def add_state(self, *args, **kwargs):
        """ Alias for add_states. """
        self.add_states(*args, **kwargs)

    def add_states(self, states, on_enter=None, on_exit=None,
                   ignore_invalid_triggers=None, **kwargs):
        """ Add new state(s).
        Args:
            states (list, string, dict, or State): a list, a State instance, the
                name of a new state, or a dict with keywords to pass on to the
                State initializer. If a list, each element can be of any of the
                latter three types.
            on_enter (string or list): callbacks to trigger when the state is
                entered. Only valid if first argument is string.
            on_exit (string or list): callbacks to trigger when the state is
                exited. Only valid if first argument is string.
            ignore_invalid_triggers: when True, any calls to trigger methods
                that are not valid for the present state (e.g., calling an
                a_to_b() trigger when the current state is c) will be silently
                ignored rather than raising an invalid transition exception.
                Note that this argument takes precedence over the same
                argument defined at the Machine level, and is in turn
                overridden by any ignore_invalid_triggers explicitly
                passed in an individual state's initialization arguments.

            **kwargs additional keyword arguments used by state mixins.
        """

        ignore = ignore_invalid_triggers
        if ignore is None:
            ignore = self.ignore_invalid_triggers

        states = listify(states)
        for state in states:
            if isinstance(state, string_types):
                state = self._create_state(
                    state, on_enter=on_enter, on_exit=on_exit,
                    ignore_invalid_triggers=ignore, **kwargs)
            elif isinstance(state, dict):
                if 'ignore_invalid_triggers' not in state:
                    state['ignore_invalid_triggers'] = ignore
                state = self._create_state(**state)
            self.states[state.name] = state
            for model in self.models:
                self._add_model_to_state(state, model)
        # Add automatic transitions after all states have been created
        if self.auto_transitions:
            for s in self.states.keys():
                self.add_transition('to_%s' % s, self.wildcard_all, s)

    def _add_model_to_state(self, state, model):
        setattr(model, 'is_%s' % state.name,
                partial(self.is_state, state.name, model))

        # Add dynamic method callbacks (enter/exit) if there are existing bound methods in the model
        # except if they are already mentioned in 'on_enter/exit' of the defined state
        for callback in self.state_cls.dynamic_methods:
            method = "{0}_{1}".format(callback, state.name)
            if hasattr(model, method) and inspect.ismethod(getattr(model, method)) and \
                    method not in getattr(state, callback):
                state.add_callback(callback[3:], method)

    def _add_trigger_to_model(self, trigger, model):
        trig_func = partial(self.events[trigger].trigger, model)
        setattr(model, trigger, trig_func)

    def get_triggers(self, *args):
        states = set(args)
        return [t for (t, ev) in self.events.items() if any(state in ev.transitions for state in states)]

    def add_transition(self, trigger, source, dest, conditions=None,
                       unless=None, before=None, after=None, prepare=None, **kwargs):
        """ Create a new Transition instance and add it to the internal list.
        Args:
            trigger (string): The name of the method that will trigger the
                transition. This will be attached to the currently specified
                model (e.g., passing trigger='advance' will create a new
                advance() method in the model that triggers the transition.)
            source(string): The name of the source state--i.e., the state we
                are transitioning away from. This can be a single state, a
                list of states or an asterisk for all states.
            dest (string): The name of the destination State--i.e., the state
                we are transitioning into. This can be a single state or an
                equal sign to specify that the transition should be reflexive
                so that the destination will be the same as the source for
                every given source.
            conditions (string or list): Condition(s) that must pass in order
                for the transition to take place. Either a list providing the
                name of a callable, or a list of callables. For the transition
                to occur, ALL callables must return True.
            unless (string, list): Condition(s) that must return False in order
                for the transition to occur. Behaves just like conditions arg
                otherwise.
            before (string or list): Callables to call before the transition.
            after (string or list): Callables to call after the transition.
            prepare (string or list): Callables to call when the trigger is activated
            **kwargs: Additional arguments which can be passed to the created transition.
                This is useful if you plan to extend Machine.Transition and require more parameters.
        """
        if trigger not in self.events:
            self.events[trigger] = self._create_event(trigger, self)
            for model in self.models:
                self._add_trigger_to_model(trigger, model)

        if isinstance(source, string_types):
            source = list(self.states.keys()) if source == self.wildcard_all else [source]
        else:
            source = [s.name if self._has_state(s) else s for s in listify(source)]

        for s in source:
            d = s if dest == self.wildcard_same else dest
            if self._has_state(d):
                d = d.name
            t = self._create_transition(s, d, conditions, unless, before,
                                        after, prepare, **kwargs)
            self.events[trigger].add_transition(t)

    def add_transitions(self, transitions):
        """ Add several transitions.

        Args:
            transitions (list): A list of transitions.

        """
        for t in listify(transitions):
            if isinstance(t, list):
                self.add_transition(*t)
            else:
                self.add_transition(**t)

    def add_ordered_transitions(self, states=None, trigger='next_state',
                                loop=True, loop_includes_initial=True,
                                conditions=None, unless=None, before=None,
                                after=None, prepare=None, **kwargs):
        """ Add a set of transitions that move linearly from state to state.
        Args:
            states (list): A list of state names defining the order of the
                transitions. E.g., ['A', 'B', 'C'] will generate transitions
                for A --> B, B --> C, and C --> A (if loop is True). If states
                is None, all states in the current instance will be used.
            trigger (string): The name of the trigger method that advances to
                the next state in the sequence.
            loop (boolean): Whether or not to add a transition from the last
                state to the first state.
            loop_includes_initial (boolean): If no initial state was defined in
                the machine, setting this to True will cause the _initial state
                placeholder to be included in the added transitions.
            conditions (string or list): Condition(s) that must pass in order
                for the transition to take place. Either a list providing the
                name of a callable, or a list of callables. For the transition
                to occur, ALL callables must return True.
            unless (string, list): Condition(s) that must return False in order
                for the transition to occur. Behaves just like conditions arg
                otherwise.
            before (string or list): Callables to call before the transition.
            after (string or list): Callables to call after the transition.
            prepare (string or list): Callables to call when the trigger is activated
            **kwargs: Additional arguments which can be passed to the created transition.
                This is useful if you plan to extend Machine.Transition and require more parameters.
        """
        if states is None:
            states = list(self.states.keys())  # need to listify for Python3
        len_transitions = len(states)
        if len_transitions < 2:
            raise ValueError("Can't create ordered transitions on a Machine "
                             "with fewer than 2 states.")
        if not loop:
            len_transitions -= 1
        # ensure all args are the proper length
        conditions = prep_ordered_arg(len_transitions, conditions)
        unless = prep_ordered_arg(len_transitions, unless)
        before = prep_ordered_arg(len_transitions, before)
        after = prep_ordered_arg(len_transitions, after)
        prepare = prep_ordered_arg(len_transitions, prepare)

        states.remove(self._initial)
        self.add_transition(trigger, self._initial, states[0],
                            conditions=conditions[0],
                            unless=unless[0],
                            before=before[0],
                            after=after[0],
                            prepare=prepare[0],
                            **kwargs)

        for i in range(1, len(states)):
            self.add_transition(trigger, states[i - 1], states[i],
                                conditions=conditions[i],
                                unless=unless[i],
                                before=before[i],
                                after=after[i],
                                prepare=prepare[i],
                                **kwargs)
        if loop:
            self.add_transition(trigger, states[-1],
                                self._initial if loop_includes_initial else states[0],
                                conditions=conditions[-1],
                                unless=unless[-1],
                                before=before[-1],
                                after=after[-1],
                                prepare=prepare[-1],
                                **kwargs)

    def remove_transition(self, trigger, source="*", dest="*"):
        """ Removes a transition from the Machine and all models.
        Args:
            trigger (string): Trigger name of the transition
            source (string): Limits removal to transitions from a certain state.
            dest (string): Limits removal to transitions to a certain state.
        """
        source = listify(source) if source != "*" else source
        dest = listify(dest) if dest != "*" else dest
        # outer comprehension, keeps events if inner comprehension returns lists with length > 0
        tmp = {key: value for key, value in
               {k: [t for t in v
                    # keep entries if source should not be filtered; same for dest.
                    if (source is not "*" and t.source not in source) or
                    (dest is not "*" and t.dest not in dest)]
                # }.items() takes the result of the inner comprehension and uses it
                # for the outer comprehension (see first line of comment)
                for k, v in self.events[trigger].transitions.items()}.items()
               if len(value) > 0}
        # convert dict back to defaultdict in case tmp is not empty
        if tmp:
            self.events[trigger].transitions = defaultdict(list, tmp)
        # if no transition is left remove the trigger from the machine and all models
        else:
            for m in self.models:
                delattr(m, trigger)
            del self.events[trigger]

    def _callback(self, func, event_data):
        """ Trigger a callback function, possibly wrapping it in an EventData
        instance.
        Args:
            func (callable): The callback function.
            event_data (EventData): An EventData instance to pass to the
                callback (if event sending is enabled) or to extract arguments
                from (if event sending is disabled).
        """
        if isinstance(func, string_types):
            func = getattr(event_data.model, func)

        if self.send_event:
            func(event_data)
        else:
            func(*event_data.args, **event_data.kwargs)

    def _has_state(self, s):
        if isinstance(s, State):
            if s in self.states.values():
                return True
            else:
                raise ValueError('State %s has not been added to the machine' % s.name)
        else:
            return False

    def _process(self, trigger):

        # default processing
        if not self.has_queue:
            if not self._transition_queue:
                # if trigger raises an Error, it has to be handled by the Machine.process caller
                return trigger()
            else:
                raise MachineError("Attempt to process events synchronously while transition queue is not empty!")

        # process queued events
        self._transition_queue.append(trigger)
        # another entry in the queue implies a running transition; skip immediate execution
        if len(self._transition_queue) > 1:
            return True

        # execute as long as transition queue is not empty
        while self._transition_queue:
            try:
                self._transition_queue[0]()
                self._transition_queue.popleft()
            except Exception:
                # if a transition raises an exception, clear queue and delegate exception handling
                self._transition_queue.clear()
                raise
        return True

    @classmethod
    def _identify_callback(cls, name):
        # Does the prefix match a known callback?
        for cb in itertools.chain(cls.state_cls.dynamic_methods, cls.transition_cls.dynamic_methods):
            if name.startswith(cb):
                callback_type = cb
                break
        else:
            return None, None

        # Extract the target by cutting the string after the type and separator
        target = name[len(callback_type) + len(cls.separator):]

        # Make sure there is actually a target to avoid index error and enforce _ as a separator
        if target == '' or name[len(callback_type)] != cls.separator:
            return None, None

        return callback_type, target

    def __getattr__(self, name):
        # Machine.__dict__ does not contain double underscore variables.
        # Class variables will be mangled.
        if name.startswith('__'):
            raise AttributeError("'{}' does not exist on <Machine@{}>"
                                 .format(name, id(self)))

        # Could be a callback
        callback_type, target = self._identify_callback(name)

        if callback_type is not None:
            if callback_type in self.transition_cls.dynamic_methods:
                if target not in self.events:
                    raise AttributeError("event '{}' is not registered on <Machine@{}>"
                                         .format(target, id(self)))
                return partial(self.events[target].add_callback, callback_type)

            elif callback_type in self.state_cls.dynamic_methods:
                state = self.get_state(target)
                return partial(state.add_callback, callback_type[3:])

        # Nothing matched
        raise AttributeError("'{}' does not exist on <Machine@{}>".format(name, id(self)))


class MachineError(Exception):

    def __init__(self, value):
        self.value = value

    def __str__(self):
        return repr(self.value)
