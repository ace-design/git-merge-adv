import importlib
import logging
import sys
import unittest
from os.path import sep, abspath
from queue import Queue
from tempfile import mkdtemp
from threading import Thread

import pytest

from errbot.rendering import text
from errbot.backends.base import Message, MUCRoom, Identifier, MUCIdentifier, ONLINE
from errbot.core_plugins.wsview import reset_app
from errbot.errBot import ErrBot
from errbot.main import setup_bot

# Can't use __name__ because of Yapsy
log = logging.getLogger('errbot.backends.test')

QUIT_MESSAGE = '$STOP$'

STZ_MSG = 1
STZ_PRE = 2
STZ_IQ = 3


class TestIdentifier(Identifier):
    """
    This is an identifier just represented as a string.
    DO NOT USE THIS DIRECTLY AS IT IS NOT COMPATIBLE WITH MOST BACKENDS,
    use self.build_identifier(identifier_as_string) instead.

    Note to back-end implementors: You should provide a custom
    <yourbackend>Identifier object that adheres to this interface.

    You should not directly inherit from SimpleIdentifier, inherit
    from object instead and make sure it includes all properties and
    methods exposed by this class.
    """

    def __init__(self, person, client=None, nick=None, fullname=None):
        self._person = person
        self._client = client
        self._nick = nick
        self._fullname = fullname

    @property
    def person(self):
        """This needs to return the part of the identifier pointing to a person."""
        return self._person

    @property
    def client(self):
        """This needs to return the part of the identifier pointing to a client from which a person is sending a message from.
        Returns None is unspecified"""
        return self._client

    @property
    def nick(self):
        """This needs to return a short display name for this identifier e.g. gbin.
        Returns None is unspecified"""
        return self._nick

    @property
    def fullname(self):
        """This needs to return a long display name for this identifier e.g. Guillaume Binet.
        Returns None is unspecified"""
        return self._fullname

    aclattr = person

    def __unicode__(self):
        if self.client:
            return self._person + "/" + self._client
        return self._person

    __str__ = __unicode__

    def __eq__(self, other):
        return self.person == other.person


# noinspection PyAbstractClass
class TestMUCOccupant(TestIdentifier, MUCIdentifier):
    """ This is a MUC occupant represented as a string.
        DO NOT USE THIS DIRECTLY AS IT IS NOT COMPATIBLE WITH MOST BACKENDS,
    """

    def __init__(self, person, room):
        super().__init__(person)
        self._room = room

    @property
    def room(self):
        return self._room

    def __unicode__(self):
        return self._person + '@' + self._room

    __str__ = __unicode__

    def __eq__(self, other):
        return self.person == other.person and self.room == other.room


class TestMUCRoom(MUCRoom):
    def invite(self, *args):
        pass

    def __init__(self, name, occupants=None, topic=None, bot=None):
        """
        :param name: Name of the room
        :param occupants: Occupants of the room
        :param topic: The MUC's topic
        """
        if occupants is None:
            occupants = []
        self._occupants = occupants
        self._topic = topic
        self._bot = bot
        self._name = name
        self._bot_mucid = TestMUCOccupant(self._bot.bot_config.BOT_IDENTITY['username'], self._name)

    @property
    def occupants(self):
        return self._occupants

    def find_croom(self):
        """ find back the canonical room from a this room"""
        for croom in self._bot._rooms:
            if croom == self:
                return croom
        return None

    @property
    def joined(self):
        room = self.find_croom()
        if room:
            return self._bot_mucid in room.occupants
        return False

    def join(self, username=None, password=None):
        if self.joined:
            logging.warning("Attempted to join room '{!s}', but already in this room".format(self))
            return

        if not self.exists:
            log.debug("Room {!s} doesn't exist yet, creating it".format(self))
            self.create()

        room = self.find_croom()
        room._occupants.append(self._bot_mucid)
        log.info("Joined room {!s}".format(self))
        self._bot.callback_room_joined(room)

    def leave(self, reason=None):
        if not self.joined:
            logging.warning("Attempted to leave room '{!s}', but not in this room".format(self))
            return

        room = self.find_croom()
        room._occupants.remove(self._bot_mucid)
        log.info("Left room {!s}".format(self))
        self._bot.callback_room_left(room)

    @property
    def exists(self):
        return self.find_croom() is not None

    def create(self):
        if self.exists:
            logging.warning("Room {!s} already created".format(self))
            return

        self._bot._rooms.append(self)
        log.info("Created room {!s}".format(self))

    def destroy(self):
        if not self.exists:
            logging.warning("Cannot destroy room {!s}, it doesn't exist".format(self))
            return

        self._bot._rooms.remove(self)
        log.info("Destroyed room {!s}".format(self))

    @property
    def topic(self):
        return self._topic

    @topic.setter
    def topic(self, topic):
        self._topic = topic
        room = self.find_croom()
        room._topic = self._topic
        log.info("Topic for room {!s} set to '{}'".format(self, topic))
        self._bot.callback_room_topic(self)

    def __unicode__(self):
        return self._name

    def __str__(self):
        return self._name

    def __eq__(self, other):
        return self._name == other._name


class TestBackend(ErrBot):
    def change_presence(self, status: str = ONLINE, message: str = '') -> None:
        pass

    def __init__(self, config):
        config.BOT_LOG_LEVEL = logging.DEBUG
        config.CHATROOM_PRESENCE = ('testroom',)  # we are testing with simple identfiers
        config.BOT_IDENTITY = {'username': 'err'}  # we are testing with simple identfiers
        self.bot_identifier = self.build_identifier('Err')  # whatever

        super().__init__(config)
        self.incoming_stanza_queue = Queue()
        self.outgoing_message_queue = Queue()
        self.sender = self.build_identifier(config.BOT_ADMINS[0])  # By default, assume this is the admin talking
        self.reset_rooms()
        self.md = text()

    def send_message(self, mess):
        super().send_message(mess)
        self.outgoing_message_queue.put(self.md.convert(mess.body))

    def serve_forever(self):
        self.connect_callback()  # notify that the connection occured
        try:
            while True:
                print('waiting on queue')
                stanza_type, entry = self.incoming_stanza_queue.get()
                print('message received')
                if entry == QUIT_MESSAGE:
                    log.info("Stop magic message received, quitting...")
                    break
                if stanza_type is STZ_MSG:
                    msg = Message(entry)
                    msg.frm = self.sender
                    msg.to = self.bot_identifier  # To me only

                    self.callback_message(msg)

                    # implements the mentions.
                    mentioned = [self.build_identifier(word[1:]) for word in entry.split() if word.startswith('@')]
                    if mentioned:
                        self.callback_mention(msg, mentioned)

                elif stanza_type is STZ_PRE:
                    log.info("Presence stanza received.")
                    self.callback_presence(entry)
                elif stanza_type is STZ_IQ:
                    log.info("IQ stanza received.")
                else:
                    log.error("Unknown stanza type.")

        except EOFError:
            pass
        except KeyboardInterrupt:
            pass
        finally:
            log.debug("Trigger disconnect callback")
            self.disconnect_callback()
            log.debug("Trigger shutdown")
            self.shutdown()

    def connect(self):
        return

    def build_identifier(self, text_representation):
        return TestIdentifier(text_representation)

    def build_reply(self, mess, text=None, private=False):
        msg = self.build_message(text)
        msg.frm = self.bot_identifier
        msg.to = mess.frm
        return msg

    @property
    def mode(self):
        return 'test'

    def rooms(self):
        return [r for r in self._rooms if r.joined]

    def query_room(self, room):
        try:
            return [r for r in self._rooms if str(r) == str(room)][0]
        except IndexError:
            r = TestMUCRoom(room, bot=self)
            return r

    def prefix_groupchat_reply(self, message, identifier):
        message.body = '@{0} {1}'.format(identifier.nick, message.body)

    def pop_message(self, timeout=5, block=True):
        return self.outgoing_message_queue.get(timeout=timeout, block=block)

    def push_message(self, msg):
        self.incoming_stanza_queue.put((STZ_MSG, msg), timeout=5)

    def push_presence(self, presence):
        """ presence must at least duck type base.Presence
        """
        self.incoming_stanza_queue.put((STZ_PRE, presence), timeout=5)

    def zap_queues(self):
        while not self.incoming_stanza_queue.empty():
            msg = self.incoming_stanza_queue.get(block=False)
            log.error('Message left in the incoming queue during a test : %s' % msg)

        while not self.outgoing_message_queue.empty():
            msg = self.outgoing_message_queue.get(block=False)
            log.error('Message left in the outgoing queue during a test : %s' % msg)

    def reset_rooms(self):
        """Reset/clear all rooms"""
        self._rooms = []


class ShallowConfig(object):
    pass


class TestBot(object):
    """
    A minimal bot utilizing the TestBackend, for use with unit testing.

    Only one instance of this class should globally be active at any one
    time.

    End-users should not use this class directly. Use
    :func:`~errbot.backends.test.testbot` or
    :class:`~errbot.backends.test.FullStackTest` instead, which use this
    class under the hood.
    """
    bot_thread = None

    def __init__(self, extra_plugin_dir=None, loglevel=logging.DEBUG, extra_config=None):
        self.setup(extra_plugin_dir=extra_plugin_dir, loglevel=loglevel, extra_config=extra_config)

    def setup(self, extra_plugin_dir=None, loglevel=logging.DEBUG, extra_config=None):
        """
        :param extra_config: Piece of extra configuration you want to inject to the config.
        :param extra_plugin_dir: Path to a directory from which additional
            plugins should be loaded.
        :param loglevel: Logging verbosity. Expects one of the constants
            defined by the logging module.
        """
        tempdir = mkdtemp()

        # This is for test isolation.
        config = ShallowConfig()
        config.__dict__.update(importlib.import_module('errbot.config-template').__dict__)
        config.BOT_DATA_DIR = tempdir
        config.BOT_LOG_FILE = tempdir + sep + 'log.txt'
        config.STORAGE = 'Memory'

        if extra_config is not None:
            log.debug('Merging %s to the bot config.' % repr(extra_config))
            for k, v in extra_config.items():
                setattr(config, k, v)

        # reset logging to console
        logging.basicConfig(format='%(levelname)s:%(message)s')
        file = logging.FileHandler(config.BOT_LOG_FILE, encoding='utf-8')
        self.logger = logging.getLogger('')
        self.logger.setLevel(loglevel)
        self.logger.addHandler(file)

        config.BOT_EXTRA_PLUGIN_DIR = extra_plugin_dir
        config.BOT_LOG_LEVEL = loglevel
        self.bot_config = config

    def start(self):
        """
        Start the bot

        Calling this method when the bot has already started will result
        in an Exception being raised.
        """
        if self.bot_thread is not None:
            raise Exception("Bot has already been started")
        self.bot = setup_bot('Test', self.logger, self.bot_config)
        self.bot_thread = Thread(target=self.bot.serve_forever, name='TestBot main thread')
        self.bot_thread.setDaemon(True)
        self.bot_thread.start()

        self.bot.push_message("!echo ready")

        # Ensure bot is fully started and plugins are loaded before returning
        assert self.bot.pop_message(timeout=60) == "ready"

    def stop(self):
        """
        Stop the bot

        Calling this method before the bot has started will result in an
        Exception being raised.
        """
        if self.bot_thread is None:
            raise Exception("Bot has not yet been started")
        self.bot.push_message(QUIT_MESSAGE)
        self.bot_thread.join()
        reset_app()  # empty the bottle ... hips!
        log.info("Main bot thread quits")
        self.bot.zap_queues()
        self.bot.reset_rooms()
        self.bot_thread = None

    def pop_message(self, timeout=5, block=True):
        return self.bot.pop_message(timeout, block)

    def push_message(self, msg):
        return self.bot.push_message(msg)

    def push_presence(self, presence):
        """ presence must at least duck type base.Presence
        """
        return self.bot.push_presence(presence)

    def zap_queues(self):
        return self.bot.zap_queues()

    def assertCommand(self, command, response, timeout=5):
        """Assert the given command returns the given response"""
        self.bot.push_message(command)
        assert response in self.bot.pop_message(timeout)

    def assertCommandFound(self, command, timeout=5):
        """Assert the given command does not exist"""
        self.bot.push_message(command)
        assert 'not found' not in self.bot.pop_message(timeout)


class FullStackTest(unittest.TestCase, TestBot):
    """
    Test class for use with Python's unittest module to write tests
    against a fully functioning bot.

    For example, if you wanted to test the builtin `!about` command,
    you could write a test file with the following::

        from errbot.backends.test import FullStackTest

        class TestCommands(FullStackTest):
            def test_about(self):
                self.push_message('!about')
                self.assertIn('Err version', self.pop_message())
    """

    def setUp(self, extra_plugin_dir=None, extra_test_file=None, loglevel=logging.DEBUG, extra_config=None):
        """
        :param extra_plugin_dir: Path to a directory from which additional
            plugins should be loaded.
        :param extra_test_file: [Deprecated but kept for backward-compatibility,
            use extra_plugin_dir instead]
            Path to an additional plugin which should be loaded.
        :param loglevel: Logging verbosity. Expects one of the constants
            defined by the logging module.
        :param extra_config: Piece of extra bot config in a dict.
        """
        if extra_plugin_dir is None and extra_test_file is not None:
            extra_plugin_dir = sep.join(abspath(extra_test_file).split(sep)[:-2])

        self.setup(extra_plugin_dir=extra_plugin_dir, loglevel=loglevel, extra_config=extra_config)
        self.start()

    def tearDown(self):
        self.stop()


@pytest.fixture
def testbot(request):
    """
    Pytest fixture to write tests against a fully functioning bot.

    For example, if you wanted to test the builtin `!about` command,
    you could write a test file with the following::

        from errbot.backends.test import testbot

        def test_about(testbot):
            testbot.push_message('!about')
            assert "Err version" in testbot.pop_message()

    It's possible to provide additional configuration to this fixture,
    by setting variables at module level or as class attributes (the
    latter taking precedence over the former). For example::

        from errbot.backends.test import testbot

        extra_plugin_dir = '/foo/bar'

        def test_about(testbot):
            testbot.pushMessage('!about')
            assert "Err version" in testbot.pop_message()

    ..or::

        from errbot.backends.test import testbot

        extra_plugin_dir = '/foo/bar'

        class Tests(object):
            # Wins over `extra_plugin_dir = '/foo/bar'` above
            extra_plugin_dir = '/foo/baz'

            def test_about(self, testbot):
                testbot.push_message('!about')
                assert "Err version" in testbot.pop_message()

    ..to load additional plugins from the directory `/foo/bar` or
    `/foo/baz` respectively. This works for the following items, which are
    passed to the constructor of :class:`~errbot.backends.test.TestBot`:

    * `extra_plugin_dir`
    * `loglevel`
    """

    def on_finish():
        bot.stop()

    #  setup the logging to something digestable.
    logger = logging.getLogger('')
    logging.getLogger('yapsy').setLevel(logging.ERROR)  # this one is way too verbose in debug
    logging.getLogger('MARKDOWN').setLevel(logging.ERROR)  # this one is way too verbose in debug
    logging.getLogger('Rocket.Errors').setLevel(logging.ERROR)  # this one is way too verbose in debug
    logger.setLevel(logging.DEBUG)
    console_hdlr = logging.StreamHandler(sys.stdout)
    console_hdlr.setFormatter(logging.Formatter("%(levelname)-8s %(name)-25s %(message)s"))
    logger.handlers = []
    logger.addHandler(console_hdlr)

    kwargs = {}

    for attr, default in (('extra_plugin_dir', None), ('loglevel', logging.DEBUG),):
        if hasattr(request, 'instance'):
            kwargs[attr] = getattr(request.instance, attr, None)
        if kwargs[attr] is None:
            kwargs[attr] = getattr(request.module, attr, default)

    bot = TestBot(**kwargs)
    bot.start()

    request.addfinalizer(on_finish)
    return bot
