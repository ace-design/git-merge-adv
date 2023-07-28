import click


<<<<<<< left_content.py
def create_sqlalchemy_tables():
=======
def create_tables() -> None:
>>>>>>> right_content.py
    """
    Create SQLAlchemy tables.
    """
    from apistar.main import get_current_app
    from apistar.backends.sqlalchemy import SQLAlchemy
    app = get_current_app()
    db_backend = SQLAlchemy.build(settings=app.settings)
    db_backend.create_tables()
    click.echo("Tables created")

def django_makemigrations():
    """
    Makemigrations DjangoBackend.
    """
    from apistar.main import get_current_app
    from apistar.backends import DjangoBackend
    app = get_current_app()
    db_backend = DjangoBackend.build(settings=app.settings)
    db_backend.makemigrations()
    click.echo("makemigrations")

def django_migrate():
    """
    Migrate DjangoBackend.
    """
    from apistar.main import get_current_app
    from apistar.backends import DjangoBackend
    app = get_current_app()
    db_backend = DjangoBackend.build(settings=app.settings)
    db_backend.migrate()
    click.echo("migrate")

