import abc
try:
    import pygraphviz as pgv
except:
    pgv = None


class Diagram(object):

    def __init__(self, machine):
        self.machine = machine

    @abc.abstractmethod
    def get_graph(self):
        return


class AGraph(Diagram):
    state_attributes = {
        'shape': 'circle',
        'height': '1.2',
    }

    machine_attributes = {
        'directed': True,
        'strict': False,
        'rankdir': 'LR',
        'ratio': '0.3'
    }

    def _add_nodes(self, states, container, first_state=None):
        # For each state, draw a circle
        for state in states.keys():
            shape = self.state_attributes['shape']

            if first_state is None:
                first_state = list(states.keys())[0]

            # We want the first state to be a double circle (UML style)
            if state == first_state:
                shape = 'doublecircle'
            else:
                shape = self.state_attributes['shape']

            container.add_node(n=state, shape=shape)

    def _add_edges(self, events, container):
        # For each event, add an edge
        for event in events.items():
            event = event[1]
            label = str(event.name)

            for transition in event.transitions.items():
                src = transition[0]
                dst = transition[1][0].dest

                container.add_edge(src, dst, label=label)

    def get_graph(self, title=None, initial_state=None):
        """ Generate a DOT graph with pygraphviz, returns an AGraph object
        Args:
            title (string): Optional title for the graph.
            initial_state (string): Optional name for initial state.
        """
        if not pgv:
            raise Exception('AGraph diagram requires pygraphviz')

        if title is None:
            title = self.__class__.__name__
        elif title is False:
            title = ''

        if initial_state is None:
            first_state = list(self.machine.states.keys())[0]
        else:
            first_state = initial_state

        fsm_graph = pgv.AGraph(title=title, **self.machine_attributes)
        fsm_graph.node_attr.update(self.state_attributes)

        # For each state, draw a circle
        self._add_nodes(self.machine.states, fsm_graph, first_state=first_state)

        self._add_edges(self.machine.events, fsm_graph)

        return fsm_graph
