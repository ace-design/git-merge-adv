# This is the version of this source code.

<<<<<<< left_content.py
verstr = "1.4"
=======
verstr = "1.3.128"
>>>>>>> right_content.py
try:
    from pyutil.version_class import Version as pyutil_Version
    __version__ = pyutil_Version(verstr)
except (ImportError, ValueError):
    # Maybe there is no pyutil installed.
    from distutils.version import LooseVersion as distutils_Version
    __version__ = distutils_Version(verstr)

