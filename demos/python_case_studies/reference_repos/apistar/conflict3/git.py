import click


def create_tables() -> None:
    """
    Create SQLAlchemy tables.
    """
<<<<<<< HEAD
    from apistar.cli import get_current_app
    from apistar.backends import SQLAlchemy
=======
    from apistar.main import get_current_app
    from apistar.backends.sqlalchemy import SQLAlchemy
>>>>>>> 20263004
    app = get_current_app()
    db_backend = SQLAlchemy.build(settings=app.settings)
    db_backend.create_tables()
    click.echo("Tables created")
