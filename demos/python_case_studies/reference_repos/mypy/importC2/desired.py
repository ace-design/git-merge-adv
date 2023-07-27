"""Tests for the mypy parser

Test case descriptions are in files test/data/parse[-errors].test."""

import os.path

import typing

from mypy import defaults
from mypy.myunit import Suite, AssertionFailure
from mypy.test.helpers import assert_string_arrays_equal
from mypy.test.data import parse_test_cases
from mypy.test import config
from mypy.parse import parse
from mypy.errors import CompileError


class ParserSuite(Suite):
    parse_files = ['parse.test',
                   'parse-python2.test']

    def cases(self):
        # The test case descriptions are stored in data files.
        c = []
        for f in self.parse_files:
            c += parse_test_cases(
                os.path.join(config.test_data_prefix, f), test_parser)
        return c


def test_parser(testcase):
    """Perform a single parser test case.

    The argument contains the description of the test case.
    """

    if testcase.file.endswith('python2.test'):
        pyversion = defaults.PYTHON2_VERSION
    else:
        pyversion = defaults.PYTHON3_VERSION

    try:
        n = parse(bytes('\n'.join(testcase.input), 'ascii'), pyversion=pyversion, fnam='main')
        a = str(n).split('\n')
    except CompileError as e:
        a = e.messages
    assert_string_arrays_equal(testcase.output, a,
                               'Invalid parser output ({}, line {})'.format(
                                   testcase.file, testcase.line))


# The file name shown in test case output. This is displayed in error
# messages, and must match the file name in the test case descriptions.
INPUT_FILE_NAME = 'file'


class ParseErrorSuite(Suite):
    def cases(self):
        # Test case descriptions are in an external file.
        return parse_test_cases(os.path.join(config.test_data_prefix,
                                             'parse-errors.test'),
                                test_parse_error)


def test_parse_error(testcase):
    try:
        # Compile temporary file.
        parse(bytes('\n'.join(testcase.input), 'ascii'), INPUT_FILE_NAME)
        raise AssertionFailure('No errors reported')
    except CompileError as e:
        # Verify that there was a compile error and that the error messages
        # are equivalent.
        assert_string_arrays_equal(
            testcase.output, e.messages,
            'Invalid compiler output ({}, line {})'.format(testcase.file,
                                                           testcase.line))
