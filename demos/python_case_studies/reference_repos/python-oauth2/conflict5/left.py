# This is the version of this source code.

manual_verstr = "1.4"



auto_build_num = "134"



verstr = manual_verstr + "." + auto_build_num
try:
    from pyutil.version_class import Version as pyutil_Version
    __version__ = pyutil_Version(verstr)
except (ImportError, ValueError):
    # Maybe there is no pyutil installed.
    from distutils.version import LooseVersion as distutils_Version
    __version__ = distutils_Version(verstr)
