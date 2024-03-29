from utils import InheritedStuff
from utils import Stuff
from transitions import Machine
from transitions import MachineError
from transitions import State
from transitions.core import listify
from unittest import TestCase
import warnings
import sys
from transitions import EventData
from transitions.core import prep_ordered_arg
from unittest import skipIf
try:
    from builtins import object
except ImportError:
    pass

warnings.filterwarnings('error', category=PendingDeprecationWarning, message=r".*0\.5\.0.*")

try:
    from unittest.mock import MagicMock
except ImportError:
    from mock import MagicMock


class TestTransitions(TestCase):

    def setUp(self):
        self.stuff = Stuff()

    def tearDown(self):
        pass

    def test_init_machine_with_hella_arguments(self):
        states = [
            State('State1'),
            'State2',
            {
                'name': 'State3',
                'on_enter': 'hello_world'
            }
        ]
        transitions = [
            {'trigger': 'advance',
                'source': 'State2',
                'dest': 'State3'
             }
        ]
        s = Stuff()
        m = Machine(model=s, states=states, transitions=transitions, initial='State2')
        s.advance()
        self.assertEqual(s.message, 'Hello World!')

    def test_listify(self):
        self.assertEqual(listify(4), [4])
        self.assertEqual(listify(None), [])
        self.assertEqual(listify((4, 5)), (4, 5))
        self.assertEqual(listify([1, 3]), [1, 3])

    def test_property_initial(self):
        states = ['A', 'B', 'C', 'D']
        # Define with list of dictionaries
        transitions = [
            {'trigger': 'walk', 'source': 'A', 'dest': 'B'},
            {'trigger': 'run', 'source': 'B', 'dest': 'C'},
            {'trigger': 'sprint', 'source': 'C', 'dest': 'D'}
        ]
        m = Machine(states=states, transitions=transitions, initial='A')
        self.assertEqual(m.initial, 'A')
        m = Machine(states=states, transitions=transitions, initial='C')
        self.assertEqual(m.initial, 'C')
        m = Machine(states=states, transitions=transitions)
        self.assertEqual(m.initial, 'initial')

    def test_transition_definitions(self):
        states = ['A', 'B', 'C', 'D']
        # Define with list of dictionaries
        transitions = [
            {'trigger': 'walk', 'source': 'A', 'dest': 'B'},
            {'trigger': 'run', 'source': 'B', 'dest': 'C'},
            {'trigger': 'sprint', 'source': 'C', 'dest': 'D'}
        ]
        m = Machine(states=states, transitions=transitions, initial='A')
        m.walk()
        self.assertEqual(m.state, 'B')
        # Define with list of lists
        transitions = [
            ['walk', 'A', 'B'],
            ['run', 'B', 'C'],
            ['sprint', 'C', 'D']
        ]
        m = Machine(states=states, transitions=transitions, initial='A')
        m.to_C()
        m.sprint()
        self.assertEqual(m.state, 'D')

    def test_transitioning(self):
        s = self.stuff
        s.machine.add_transition('advance', 'A', 'B')
        s.machine.add_transition('advance', 'B', 'C')
        s.machine.add_transition('advance', 'C', 'D')
        s.advance()
        self.assertEqual(s.state, 'B')
        self.assertFalse(s.is_A())
        self.assertTrue(s.is_B())
        s.advance()
        self.assertEqual(s.state, 'C')

    def test_pass_state_instances_instead_of_names(self):
        state_A = State('A')
        state_B = State('B')
        states = [state_A, state_B]
        m = Machine(states=states, initial=state_A)
        assert m.state == 'A'
        m.add_transition('advance', state_A, state_B)
        m.advance()
        assert m.state == 'B'
        state_B2 = State('B', on_enter='this_passes')
        with self.assertRaises(ValueError):
            m.add_transition('advance2', state_A, state_B2)

    def test_conditions(self):
        s = self.stuff
        s.machine.add_transition('advance', 'A', 'B', conditions='this_passes')
        s.machine.add_transition('advance', 'B', 'C', unless=['this_fails'])
        s.machine.add_transition('advance', 'C', 'D', unless=['this_fails',
                                                              'this_passes'])
        s.advance()
        self.assertEqual(s.state, 'B')
        s.advance()
        self.assertEqual(s.state, 'C')
        s.advance()
        self.assertEqual(s.state, 'C')

    def test_multiple_add_transitions_from_state(self):
        s = self.stuff
        s.machine.add_transition(
            'advance', 'A', 'B', conditions=['this_fails'])
        s.machine.add_transition('advance', 'A', 'C')
        s.advance()
        self.assertEqual(s.state, 'C')

    def test_use_machine_as_model(self):
        states = ['A', 'B', 'C', 'D']
        m = Machine(states=states, initial='A')
        m.add_transition('move', 'A', 'B')
        m.add_transition('move_to_C', 'B', 'C')
        m.move()
        self.assertEqual(m.state, 'B')

    def test_state_change_listeners(self):
        s = self.stuff
        s.machine.add_transition('advance', 'A', 'B')
        s.machine.add_transition('reverse', 'B', 'A')
        s.machine.on_enter_B('hello_world')
        s.machine.on_exit_B('goodbye')
        s.advance()
        self.assertEqual(s.state, 'B')
        self.assertEqual(s.message, 'Hello World!')
        s.reverse()
        self.assertEqual(s.state, 'A')
        self.assertTrue(s.message.startswith('So long'))

    def test_before_after_callback_addition(self):
        m = Machine(Stuff(), states=['A', 'B', 'C'], initial='A')
        m.add_transition('move', 'A', 'B')
        trans = m.events['move'].transitions['A'][0]
        trans.add_callback('after', 'increase_level')
        m.model.move()
        self.assertEqual(m.model.level, 2)

    def test_before_after_transition_listeners(self):
        m = Machine(Stuff(), states=['A', 'B', 'C'], initial='A')
        m.add_transition('move', 'A', 'B')
        m.add_transition('move', 'B', 'C')

        m.before_move('increase_level')
        m.model.move()
        self.assertEqual(m.model.level, 2)
        m.model.move()
        self.assertEqual(m.model.level, 3)

    def test_prepare(self):
        m = Machine(Stuff(), states=['A', 'B', 'C'], initial='A')
        m.add_transition('move', 'A', 'B', prepare='increase_level')
        m.add_transition('move', 'B', 'C', prepare='increase_level')
        m.add_transition('move', 'C', 'A', prepare='increase_level', conditions='this_fails')
        m.add_transition('dont_move', 'A', 'C', prepare='increase_level')

        m.prepare_move('increase_level')

        m.model.move()
        self.assertEqual(m.model.state, 'B')
        self.assertEqual(m.model.level, 3)

        m.model.move()
        self.assertEqual(m.model.state, 'C')
        self.assertEqual(m.model.level, 5)

        # State does not advance, but increase_level still runs
        m.model.move()
        self.assertEqual(m.model.state, 'C')
        self.assertEqual(m.model.level, 7)

        # An invalid transition shouldn't execute the callback
        try:
            m.model.dont_move()
        except MachineError as e:
            self.assertTrue("Can't trigger event" in str(e))

        self.assertEqual(m.model.state, 'C')
        self.assertEqual(m.model.level, 7)

    def test_state_model_change_listeners(self):
        s = self.stuff
        s.machine.add_transition('go_e', 'A', 'E')
        s.machine.add_transition('go_f', 'E', 'F')
        s.machine.on_enter_F('hello_F')
        s.go_e()
        self.assertEqual(s.state, 'E')
        self.assertEqual(s.message, 'I am E!')
        s.go_f()
        self.assertEqual(s.state, 'F')
        self.assertEqual(s.exit_message, 'E go home...')
        assert 'I am F!' in s.message
        assert 'Hello F!' in s.message

    def test_inheritance(self):
        states = ['A', 'B', 'C', 'D', 'E']
        s = InheritedStuff(states=states, initial='A')
        s.add_transition('advance', 'A', 'B', conditions='this_passes')
        s.add_transition('advance', 'B', 'C')
        s.add_transition('advance', 'C', 'D')
        s.advance()
        self.assertEqual(s.state, 'B')
        self.assertFalse(s.is_A())
        self.assertTrue(s.is_B())
        s.advance()
        self.assertEqual(s.state, 'C')

        class NewMachine(Machine):
            def __init__(self, *args, **kwargs):
                super(NewMachine, self).__init__(*args, **kwargs)

        n = NewMachine(states=states, transitions=[['advance', 'A', 'B']], initial='A')
        self.assertTrue(n.is_A())
        n.advance()
        self.assertTrue(n.is_B())
        with self.assertRaises(ValueError):
            m = NewMachine(state=['A', 'B'])

    def test_send_event_data_callbacks(self):
        states = ['A', 'B', 'C', 'D', 'E']
        s = Stuff()
        # First pass positional and keyword args directly to the callback
        m = Machine(model=s, states=states, initial='A', send_event=False,
                    auto_transitions=True)
        m.add_transition(
            trigger='advance', source='A', dest='B', before='set_message')
        s.advance(message='Hallo. My name is Inigo Montoya.')
        self.assertTrue(s.message.startswith('Hallo.'))
        s.to_A()
        s.advance('Test as positional argument')
        self.assertTrue(s.message.startswith('Test as'))
        # Now wrap arguments in an EventData instance
        m.send_event = True
        m.add_transition(
            trigger='advance', source='B', dest='C', before='extract_message')
        s.advance(message='You killed my father. Prepare to die.')
        self.assertTrue(s.message.startswith('You'))

    def test_send_event_data_conditions(self):
        states = ['A', 'B', 'C', 'D']
        s = Stuff()
        # First pass positional and keyword args directly to the condition
        m = Machine(model=s, states=states, initial='A', send_event=False)
        m.add_transition(
            trigger='advance', source='A', dest='B',
            conditions='this_fails_by_default')
        s.advance(boolean=True)
        self.assertEqual(s.state, 'B')
        # Now wrap arguments in an EventData instance
        m.send_event = True
        m.add_transition(
            trigger='advance', source='B', dest='C',
            conditions='extract_boolean')
        s.advance(boolean=False)
        self.assertEqual(s.state, 'B')

    def test_auto_transitions(self):
        states = ['A', {'name': 'B'}, State(name='C')]
        m = Machine('self', states, initial='A', auto_transitions=True)
        m.to_B()
        self.assertEqual(m.state, 'B')
        m.to_C()
        self.assertEqual(m.state, 'C')
        m.to_A()
        self.assertEqual(m.state, 'A')
        # Should fail if auto transitions is off...
        m = Machine('self', states, initial='A', auto_transitions=False)
        with self.assertRaises(AttributeError):
            m.to_C()

    def test_ordered_transitions(self):
        states = ['beginning', 'middle', 'end']
        m = Machine('self', states)
        m.add_ordered_transitions()
        self.assertEqual(m.state, 'initial')
        m.next_state()
        self.assertEqual(m.state, 'beginning')
        m.next_state()
        m.next_state()
        self.assertEqual(m.state, 'end')
        m.next_state()
        self.assertEqual(m.state, 'initial')

        # Include initial state in loop
        m = Machine('self', states)
        m.add_ordered_transitions(loop_includes_initial=False)
        m.to_end()
        m.next_state()
        self.assertEqual(m.state, 'beginning')

        # Test user-determined sequence and trigger name
        m = Machine('self', states, initial='beginning')
        m.add_ordered_transitions(['end', 'beginning'], trigger='advance')
        m.advance()
        self.assertEqual(m.state, 'end')
        m.advance()
        self.assertEqual(m.state, 'beginning')

        # Via init argument
        m = Machine('self', states, initial='beginning', ordered_transitions=True)
        m.next_state()
        self.assertEqual(m.state, 'middle')

    def test_ordered_transition_error(self):
        m = Machine(states=['A'], initial='A')
        with self.assertRaises(ValueError):
            m.add_ordered_transitions()
        m.add_state('B')
        m.add_ordered_transitions()
        m.add_state('C')
        with self.assertRaises(ValueError):
            m.add_ordered_transitions(['C'])

    def test_ignore_invalid_triggers(self):
        a_state = State('A')
        transitions = [['a_to_b', 'A', 'B']]
        # Exception is triggered by default
        b_state = State('B')
        m1 = Machine('self', states=[a_state, b_state], transitions=transitions,
                     initial='B')
        with self.assertRaises(MachineError):
            m1.a_to_b()
        # Exception is suppressed, so this passes
        b_state = State('B', ignore_invalid_triggers=True)
        m2 = Machine('self', states=[a_state, b_state], transitions=transitions,
                     initial='B')
        m2.a_to_b()
        # Set for some states but not others
        new_states = ['C', 'D']
        m1.add_states(new_states, ignore_invalid_triggers=True)
        m1.to_D()
        m1.a_to_b()  # passes because exception suppressed for D
        m1.to_B()
        with self.assertRaises(MachineError):
            m1.a_to_b()
        # Set at machine level
        m3 = Machine('self', states=[a_state, b_state], transitions=transitions,
                     initial='B', ignore_invalid_triggers=True)
        m3.a_to_b()

    def test_string_callbacks(self):

        m = Machine(states=['A', 'B'],
                    before_state_change='before_state_change',
                    after_state_change='after_state_change', send_event=True,
                    initial='A', auto_transitions=True)

        m.before_state_change = MagicMock()
        m.after_state_change = MagicMock()

        m.to_B()

        self.assertTrue(m.before_state_change[0].called)
        self.assertTrue(m.after_state_change[0].called)

        # after_state_change should have been called with EventData
        event_data = m.after_state_change[0].call_args[0][0]
        self.assertIsInstance(event_data, EventData)
        self.assertTrue(event_data.result)

    def test_function_callbacks(self):
        before_state_change = MagicMock()
        after_state_change = MagicMock()

        m = Machine('self', states=['A', 'B'],
                    before_state_change=before_state_change,
                    after_state_change=after_state_change, send_event=True,
                    initial='A', auto_transitions=True)

        m.to_B()
        self.assertTrue(m.before_state_change[0].called)
        self.assertTrue(m.after_state_change[0].called)

    def test_pickle(self):
        import sys
        if sys.version_info < (3, 4):
            import dill as pickle
        else:
            import pickle

        states = ['A', 'B', 'C', 'D']
        # Define with list of dictionaries
        transitions = [
            {'trigger': 'walk', 'source': 'A', 'dest': 'B'},
            {'trigger': 'run', 'source': 'B', 'dest': 'C'},
            {'trigger': 'sprint', 'source': 'C', 'dest': 'D'}
        ]
        m = Machine(states=states, transitions=transitions, initial='A')
        m.walk()
        dump = pickle.dumps(m)
        self.assertIsNotNone(dump)
        m2 = pickle.loads(dump)
        self.assertEqual(m.state, m2.state)
        m2.run()

    def test_queued(self):
        states = ['A', 'B', 'C', 'D']
        # Define with list of dictionaries

        def change_state(machine):
            self.assertEqual(machine.state, 'A')
            if machine.has_queue:
                machine.run(machine=machine)
                self.assertEqual(machine.state, 'A')
            else:
                with self.assertRaises(MachineError):
                    machine.run(machine=machine)

        transitions = [
            {'trigger': 'walk', 'source': 'A', 'dest': 'B', 'before': change_state},
            {'trigger': 'run', 'source': 'B', 'dest': 'C'},
            {'trigger': 'sprint', 'source': 'C', 'dest': 'D'}
        ]

        m = Machine(states=states, transitions=transitions, initial='A')
        m.walk(machine=m)
        self.assertEqual(m.state, 'B')
        m = Machine(states=states, transitions=transitions, initial='A', queued=True)
        m.walk(machine=m)
        self.assertEqual(m.state, 'C')

    def test_queued_errors(self):
        def before_change(machine):
            if machine.has_queue:
                machine.to_A(machine)
            machine._queued = False

        def after_change(machine):
            machine.to_C(machine)

        def failed_transition(machine):
            raise ValueError('Something was wrong')

        states = ['A', 'B', 'C']
        transitions = [{'trigger': 'do', 'source': '*', 'dest': 'C', 'before': failed_transition}]
        m = Machine(states=states, transitions=transitions, queued=True,
                    before_state_change=before_change, after_state_change=after_change)
        with self.assertRaises(MachineError):
            m.to_B(machine=m)

        with self.assertRaises(ValueError):
            m.do(machine=m)

    def test___getattr___and_identify_callback(self):
        m = Machine(Stuff(), states=['A', 'B', 'C'], initial='A')
        m.add_transition('move', 'A', 'B')
        m.add_transition('move', 'B', 'C')

        callback = m.__getattr__('before_move')
        self.assertTrue(callable(callback))

        with self.assertRaises(AttributeError):
            m.__getattr__('before_no_such_transition')

        with self.assertRaises(AttributeError):
            m.__getattr__('before_no_such_transition')

        with self.assertRaises(AttributeError):
            m.__getattr__('__no_such_method__')

        with self.assertRaises(AttributeError):
            m.__getattr__('')

        type, target = m._identify_callback('on_exit_foobar')
        self.assertEqual(type, 'on_exit')
        self.assertEqual(target, 'foobar')

        type, target = m._identify_callback('on_exitfoobar')
        self.assertEqual(type, None)
        self.assertEqual(target, None)

        type, target = m._identify_callback('notacallback_foobar')
        self.assertEqual(type, None)
        self.assertEqual(target, None)

        type, target = m._identify_callback('totallyinvalid')
        self.assertEqual(type, None)
        self.assertEqual(target, None)

        type, target = m._identify_callback('before__foobar')
        self.assertEqual(type, 'before')
        self.assertEqual(target, '_foobar')

        type, target = m._identify_callback('before__this__user__likes__underscores___')
        self.assertEqual(type, 'before')
        self.assertEqual(target, '_this__user__likes__underscores___')

        type, target = m._identify_callback('before_stuff')
        self.assertEqual(type, 'before')
        self.assertEqual(target, 'stuff')

        type, target = m._identify_callback('before_trailing_underscore_')
        self.assertEqual(type, 'before')
        self.assertEqual(target, 'trailing_underscore_')

        type, target = m._identify_callback('before_')
        self.assertIs(type, None)
        self.assertIs(target, None)

        type, target = m._identify_callback('__')
        self.assertIs(type, None)
        self.assertIs(target, None)

        type, target = m._identify_callback('')
        self.assertIs(type, None)
        self.assertIs(target, None)

    def test_state_and_transition_with_underscore(self):
        m = Machine(Stuff(), states=['_A_', '_B_', '_C_'], initial='_A_')
        m.add_transition('_move_', '_A_', '_B_', prepare='increase_level')
        m.add_transition('_after_', '_B_', '_C_', prepare='increase_level')
        m.add_transition('_on_exit_', '_C_', '_A_', prepare='increase_level', conditions='this_fails')

        m.model._move_()
        self.assertEqual(m.model.state, '_B_')
        self.assertEqual(m.model.level, 2)

        m.model._after_()
        self.assertEqual(m.model.state, '_C_')
        self.assertEqual(m.model.level, 3)

        # State does not advance, but increase_level still runs
        m.model._on_exit_()
        self.assertEqual(m.model.state, '_C_')
        self.assertEqual(m.model.level, 4)

    def test_callback_identification(self):
        m = Machine(Stuff(), states=['A', 'B', 'C', 'D', 'E', 'F'], initial='A')
        m.add_transition('transition', 'A', 'B', before='increase_level')
        m.add_transition('after', 'B', 'C', before='increase_level')
        m.add_transition('on_exit_A', 'C', 'D', before='increase_level', conditions='this_fails')
        m.add_transition('check', 'C', 'E', before='increase_level')
        m.add_transition('prepare', 'E', 'F', before='increase_level')
        m.add_transition('before', 'F', 'A', before='increase_level')

        m.before_transition('increase_level')
        m.before_after('increase_level')
        m.before_on_exit_A('increase_level')
        m.after_check('increase_level')
        m.before_prepare('increase_level')
        m.before_before('increase_level')

        m.model.transition()
        self.assertEqual(m.model.state, 'B')
        self.assertEqual(m.model.level, 3)

        m.model.after()
        self.assertEqual(m.model.state, 'C')
        self.assertEqual(m.model.level, 5)

        m.model.on_exit_A()
        self.assertEqual(m.model.state, 'C')
        self.assertEqual(m.model.level, 5)

        m.model.check()
        self.assertEqual(m.model.state, 'E')
        self.assertEqual(m.model.level, 7)

        m.model.prepare()
        self.assertEqual(m.model.state, 'F')
        self.assertEqual(m.model.level, 9)

        m.model.before()
        self.assertEqual(m.model.state, 'A')
        self.assertEqual(m.model.level, 11)

        # An invalid transition shouldn't execute the callback
        with self.assertRaises(MachineError):
                m.model.on_exit_A()

    def test_process_trigger(self):
        m = Machine(states=['raw', 'processed'], initial='raw')
        m.add_transition('process', 'raw', 'processed')

        m.process()
        self.assertEqual(m.state, 'processed')

    def test_multiple_models(self):
        s1, s2 = Stuff(), Stuff()
        states = ['A', 'B', 'C']

        m = Machine(model=[s1, s2], states=states,
                    initial=states[0])
        self.assertEqual(len(m.models), 2)
        self.assertEqual(len(m.model), 2)
        m.add_transition('advance', 'A', 'B')
        s1.advance()
        self.assertEqual(s1.state, 'B')
        self.assertEqual(s2.state, 'A')
        m = Machine(model=s1, states=states,
                    initial=states[0])
        # for backwards compatibility model should return a model instance
        # rather than a list
        self.assertNotIsInstance(m.model, list)

    def test_string_trigger(self):
        def return_value(value):
            return value

        class Model:
            def trigger(self, value):
                return value

        self.stuff.machine.add_transition('do', '*', 'C')
        self.stuff.trigger('do')
        self.assertTrue(self.stuff.is_C())
        self.stuff.machine.add_transition('maybe', 'C', 'A', conditions=return_value)
        self.assertFalse(self.stuff.trigger('maybe', value=False))
        self.assertTrue(self.stuff.trigger('maybe', value=True))
        self.assertTrue(self.stuff.is_A())
        with self.assertRaises(AttributeError):
            self.stuff.trigger('not_available')

        model = Model()
        m = Machine(model=model)
        self.assertEqual(model.trigger(5), 5)

    def test_get_triggers(self):
        states = ['A', 'B', 'C']
        transitions = [['a2b', 'A', 'B'],
                       ['a2c', 'A', 'C'],
                       ['c2b', 'C', 'B']]
        machine = Machine(states=states, transitions=transitions, initial='A', auto_transitions=False)
        self.assertEqual(len(machine.get_triggers('A')), 2)
        self.assertEqual(len(machine.get_triggers('B')), 0)
        self.assertEqual(len(machine.get_triggers('C')), 1)
        # self stuff machine should have to-transitions to every state
        self.assertEqual(len(self.stuff.machine.get_triggers('B')), len(self.stuff.machine.states))

    @skipIf(sys.version_info < (3, ),
            "String-checking disabled on PY-2 because is different")
    def test_repr(self):
        def a_condition(event_data):
            self.assertRegex(
                str(event_data.transition.conditions),
                r"\[<Condition\(<function TestTransitions.test_repr.<locals>"
                r".a_condition at [^>]+>\)@\d+>\]")

            return True

        def check_repr(event_data):
            self.assertRegex(
                str(event_data),
                r"<EventData\('<State\('A'\)@\d+>', "
                r"<Transition\('A', 'B'\)@\d+>\)@\d+>")

            m.checked = True

        m = Machine(states=['A', 'B'],
                    before_state_change=check_repr, send_event=True,
                    initial='A')
        m.add_transition('do_strcheck', 'A', 'B', conditions=a_condition)

        self.assertTrue(m.do_strcheck())
        self.assertIn('checked', vars(m))

    def test_warning(self):
        import sys
        # does not work with python 3.3. However, the warning is shown when Machine is initialized manually.
        if (3, 3) <= sys.version_info < (3, 4):
            return
        with self.assertRaises(PendingDeprecationWarning):
            m = Machine(None)

        with self.assertRaises(PendingDeprecationWarning):
            m = Machine(initial=None)

        with self.assertRaises(PendingDeprecationWarning):
            m = Machine(None, add_self=False)

<<<<<<< left_content.py
    def test_remove_transition(self):
        self.stuff.machine.add_transition('go', ['A', 'B', 'C'], 'D')
        self.stuff.machine.add_transition('walk', 'A', 'B')
        self.stuff.go()
        self.assertEqual(self.stuff.state, 'D')
        self.stuff.to_A()
        self.stuff.machine.remove_transition('go', source='A')
        with self.assertRaises(MachineError):
            self.stuff.go()
        self.stuff.walk()
        self.stuff.go()
        self.assertEqual(self.stuff.state, 'D')
        self.stuff.to_C()
        self.stuff.machine.remove_transition('go', dest='D')
        self.assertFalse(hasattr(self.stuff, 'go'))

=======
    def test_machine_prepare(self):

        global_mock = MagicMock()
        local_mock = MagicMock()

        def global_callback():
            global_mock()

        def local_callback():
            local_mock()

        def always_fails():
            return False

        transitions = [
            {'trigger': 'go', 'source': 'A', 'dest': 'B', 'conditions': always_fails, 'prepare': local_callback},
            {'trigger': 'go', 'source': 'A', 'dest': 'B', 'conditions': always_fails, 'prepare': local_callback},
            {'trigger': 'go', 'source': 'A', 'dest': 'B', 'conditions': always_fails, 'prepare': local_callback},
            {'trigger': 'go', 'source': 'A', 'dest': 'B', 'conditions': always_fails, 'prepare': local_callback},
            {'trigger': 'go', 'source': 'A', 'dest': 'B', 'prepare': local_callback},

        ]
        m = Machine(states=['A', 'B'], transitions=transitions,
                    prepare_event=global_callback, initial='A')

        m.go()
        self.assertEqual(global_mock.call_count, 1)
        self.assertEqual(local_mock.call_count, len(transitions))

    def test_machine_finalize(self):

        finalize_mock = MagicMock()

        def always_fails(event_data):
            return False

        def always_raises(event_data):
            raise Exception()

        transitions = [
            {'trigger': 'go', 'source': 'A', 'dest': 'B'},
            {'trigger': 'planA', 'source': 'B', 'dest': 'C', 'conditions': always_fails},
            {'trigger': 'planB', 'source': 'B', 'dest': 'C', 'conditions': always_raises}
        ]
        m = Machine(states=['A', 'B'], transitions=transitions,
                    finalize_event=finalize_mock, initial='A', send_event=True)

        m.go()
        self.assertEqual(finalize_mock.call_count, 1)
        m.planA()

        event_data = finalize_mock.call_args[0][0]
        self.assertIsInstance(event_data, EventData)
        self.assertEqual(finalize_mock.call_count, 2)
        self.assertFalse(event_data.result)
        with self.assertRaises(Exception):
            m.planB()
        self.assertEqual(finalize_mock.call_count, 3)

    def test_machine_finalize_exception(self):

        exception = ZeroDivisionError()

        def always_raises(event):
            raise exception

        def finalize_callback(event):
            self.assertEqual(event.error, exception)

        m = Machine(states=['A', 'B'], send_event=True, initial='A',
                    before_state_change=always_raises,
                    finalize_event=finalize_callback)

        with self.assertRaises(ZeroDivisionError):
            m.to_B()

    def test_prep_ordered_arg(self):
        self.assertTrue(len(prep_ordered_arg(3, None)) == 3)
        self.assertTrue(all(a == None for a in prep_ordered_arg(3, None)))
        with self.assertRaises(ValueError):
            prep_ordered_arg(3, [None, None])

    def test_ordered_transition_callback(self):
        class Model:
            def __init__(self):
                self.flag = False
            def make_true(self):
                self.flag = True

        model = Model()
        states = ['beginning', 'middle', 'end']
        transits = [None, None, 'make_true']
        m = Machine(model, states, initial='beginning')
        m.add_ordered_transitions(before=transits)
        model.next_state()
        self.assertFalse(model.flag)
        model.next_state()
        model.next_state()
        self.assertTrue(model.flag)

    def test_ordered_transition_condition(self):
        class Model:
            def __init__(self):
                self.blocker = False
            def check_blocker(self):
                return self.blocker

        model = Model()
        states = ['beginning', 'middle', 'end']
        m = Machine(model, states, initial='beginning')
        m.add_ordered_transitions(conditions=[None, None, 'check_blocker'])
        model.to_end()
        self.assertFalse(model.next_state())
        model.blocker = True
        self.assertTrue(model.next_state())
>>>>>>> right_content.py
