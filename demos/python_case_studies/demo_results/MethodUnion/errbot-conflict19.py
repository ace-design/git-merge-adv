import logging
BOT_DATA_DIR = '/var/lib/err'

BOT_EXTRA_PLUGIN_DIR = None

BOT_LOG_FILE = '/var/log/err/err.log'

BOT_LOG_LEVEL = logging.INFO

BOT_LOG_SENTRY = False

SENTRY_DSN = ''

SENTRY_LOGLEVEL = BOT_LOG_LEVEL

BOT_ASYNC = True

BOT_IDENTITY = {'username': 'err@localhost', 'password': 'changeme'}

BOT_ADMINS = 'gbin@localhost',

CHATROOM_PRESENCE = 'err@conference.server.tld',

CHATROOM_FN = 'Err'

BOT_PREFIX = '!'

DIVERT_TO_PRIVATE = ()

CHATROOM_RELAY = {}

REVERSE_CHATROOM_RELAY = {}

