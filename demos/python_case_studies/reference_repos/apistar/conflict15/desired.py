<<<<<<< HEAD
import os
=======
import inspect
>>>>>>> master
from collections import OrderedDict

import click

from apistar import commands, pipelines, routing, schema


class App(object):
    built_in_commands = (
        commands.new,
        commands.run,
        commands.test,
    )

    def __init__(self, routes=None, commands=None):
        routes = [] if (routes is None) else routes
        commands = [] if (commands is None) else commands

        self.routes = routes
        self.commands = list(self.built_in_commands) + commands

        self.router = routing.Router(self.routes)
        self.wsgi = get_wsgi_server(app=self)
        self.click = get_click_client(app=self)


class AppRoot(str):
    @classmethod
    def build(cls):
        return os.getcwd()


def get_wsgi_server(app, lookup_cache_size=10000):
    lookup = app.router.lookup
    lookup_cache = OrderedDict()  # FIFO Cache for URL lookups.

    # Pre-fill the lookup cache for URLs without path arguments.
    for path, method, view in app.router.routes:
        if '{' not in path:
            key = method.upper() + ' ' + path
            lookup_cache[key] = lookup(path, method)

    def func(environ, start_response):
        method = environ['REQUEST_METHOD']
        path = environ['PATH_INFO']
        lookup_key = method + ' ' + path
        state = {
            'wsgi_environ': environ,
            'app': app,
            'method': method,
            'path': path,
            'exception': None,
            'view': None,
            'url_path_args': {}
        }

        try:
            try:
                (state['view'], pipeline, state['url_path_args']) = lookup_cache[lookup_key]
            except KeyError:
                (state['view'], pipeline, state['url_path_args']) = lookup_cache[lookup_key] = lookup(path, method)
                if len(lookup_cache) > lookup_cache_size:
                    lookup_cache.pop(next(iter(lookup_cache)))

            for function, inputs, output, extra_kwargs in pipeline:
                # Determine the keyword arguments for each step in the pipeline.
                kwargs = {}
                for arg_name, state_key in inputs:
                    kwargs[arg_name] = state[state_key]
                if extra_kwargs is not None:
                    kwargs.update(extra_kwargs)

                # Call the function for each step in the pipeline.
                state[output] = function(**kwargs)
        except Exception as exc:
            state['exception'] = exc
            pipelines.run_pipeline(app.router.exception_pipeline, state)

        wsgi_response = state['wsgi_response']
        start_response(wsgi_response.status, wsgi_response.headers)
        return wsgi_response.iterator

    return func


def get_click_client(app):
    @click.group(invoke_without_command=True, help='API Star')
    @click.option('--version', is_flag=True, help='Display the `apistar` version number.')
    @click.pass_context
    def client(ctx, version):
        if ctx.invoked_subcommand is not None:
            return

        if version:
            from apistar import __version__
            click.echo(__version__)
        else:
            click.echo(ctx.get_help())

    for command in app.commands:

        command_signature = inspect.signature(command)
        for param in reversed(command_signature.parameters.values()):
            name = param.name.replace('_', '-')
            annotation = param.annotation
            kwargs = {}
            if hasattr(annotation, 'default'):
                kwargs['default'] = annotation.default
            if hasattr(annotation, 'description'):
                kwargs['help'] = annotation.description

            if issubclass(annotation, (bool, schema.Boolean)):
                kwargs['is_flag'] = True
                kwargs['default'] = False
            elif hasattr(annotation, 'choices'):
                kwargs['type'] = click.Choice(annotation.choices)
            elif hasattr(annotation, 'native_type'):
                kwargs['type'] = annotation.native_type
            elif annotation is inspect.Signature.empty:
                kwargs['type'] = str
            else:
                kwargs['type'] = annotation

            if 'default' in kwargs:
                name = '--%s' % param.name.replace('_', '-')
                option = click.option(name, **kwargs)
                command = option(command)
            else:
                kwargs.pop('help', None)
                argument = click.argument(param.name, **kwargs)
                command = argument(command)

        cmd_wrapper = click.command(help=command.__doc__)
        command = cmd_wrapper(command)

        client.add_command(command)

    return client
