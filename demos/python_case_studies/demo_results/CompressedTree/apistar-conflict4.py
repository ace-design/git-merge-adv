import click


def create_sqlalchemy_tables() -> None:
    """
    Create SQLAlchemy tables.
    """
    from apistar.main import get_current_app
    from apistar.backends.sqlalchemy import SQLAlchemy
    app = get_current_app()
    db_backend = SQLAlchemy.build(settings=app.settings)
    db_backend.create_tables()
    click.echo("Tables created")

<<<<<<< left_content.py

def django_makemigrations():
=======
def django_makemigrations() -> None:
>>>>>>> right_content.py
    """
    Makemigrations DjangoBackend.
    """
    from apistar.main import get_current_app
    from apistar.backends import DjangoBackend
    app = get_current_app()
    db_backend = DjangoBackend.build(settings=app.settings)
    db_backend.makemigrations()
    click.echo("makemigrations")

<<<<<<< left_content.py

def django_migrate():
=======
def django_migrate() -> None:
>>>>>>> right_content.py
    """
    Migrate DjangoBackend.
    """
    from apistar.main import get_current_app
    from apistar.backends import DjangoBackend
    app = get_current_app()
    db_backend = DjangoBackend.build(settings=app.settings)
    db_backend.migrate()
    click.echo("migrate")

