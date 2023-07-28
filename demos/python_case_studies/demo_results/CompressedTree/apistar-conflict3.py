import click


def create_tables() -> None:
    """
    Create SQLAlchemy tables.
    """
<<<<<<< left_content.py
    from apistar.cli import get_current_app
    from apistar.backends import SQLAlchemy
=======
    from apistar.main import get_current_app
    from apistar.backends.sqlalchemy import SQLAlchemy
>>>>>>> right_content.py
    app = get_current_app()
    db_backend = SQLAlchemy.build(settings=app.settings)
    db_backend.create_tables()
    click.echo("Tables created")

