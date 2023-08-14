import datetime
from types import MethodType
from datashape import dshape
from datashape.util.testing import assert_dshape_equal
from odo import append
from odo.backends.csv import CSV
from blaze import discover,transform
from blaze.compatibility import pickle
from blaze.expr import symbol
from blaze.interactive import Data,compute,concrete_head,expr_repr,to_html,data,iscorescalar,iscoresequence,iscoretype,coerce_core,into
from blaze.utils import tmpfile,example
tdata = ('Alice', 100), ('Bob', 200)

data = ('Alice', 100), ('Bob', 200)

L = [[1, 'Alice', 100], [2, 'Bob', -200], [3, 'Charlie', 300], [4, 'Denis',
    400], [5, 'Edith', -500]]

t = data(tdata, fields=['name', 'amount'])

t = Data(data, fields=['name', 'amount'])

x = np.ones((2, 2))

def test_discover_on_data():
    assert discover(t) == dshape('2 * {name: string, amount: int64}')

def test_table_raises_on_inconsistent_inputs():
    with pytest.raises(ValueError) as excinfo:
        t = data(tdata, schema='{name: string, amount: float32}', dshape=
            dshape('{name: string, amount: float32}'))
    assert 'specify one of schema= or dshape= keyword' in str(excinfo.value)

def test_resources():
    assert t._resources() == {t: t.data}

def test_resources_fail():
    t = symbol('t', 'var * {x: int, y: int}')
    d = t[t['x'] > 100]
    with pytest.raises(ValueError):
        compute(d)

def test_compute_on_Data_gives_back_data():
    assert compute(data([1, 2, 3])) == [1, 2, 3]

def test_len():
    assert len(t) == 2
    assert len(t.name) == 2

def test_compute():
    assert list(compute(t['amount'] + 1)) == [101, 201]

def test_create_with_schema():
    t = data(tdata, schema='{name: string, amount: float32}')
    assert t.schema == dshape('{name: string, amount: float32}')

def test_create_with_raw_data():
    t = data(tdata, fields=['name', 'amount'])
    assert t.schema == dshape('{name: string, amount: int64}')
    assert t.name
    assert t.data == tdata

def test_repr():
    result = expr_repr(t['name'])
    print(result)
    assert isinstance(result, str)
    assert 'Alice' in result
    assert 'Bob' in result
    assert '...' not in result
    result = expr_repr(t['amount'] + 1)
    print(result)
    assert '101' in result
    t2 = data(tuple((i, i ** 2) for i in range(100)), fields=['x', 'y'])
    assert t2.dshape == dshape('100 * {x: int64, y: int64}')
    result = expr_repr(t2)
    print(result)
    assert len(result.split('\n')) < 20
    assert '...' in result

def test_str_does_not_repr():
    d = data([('aa', 1), ('b', 2)], name='ZZZ', dshape=
        '2 * {a: string, b: int64}')
    expr = transform(d, c=d.a.strlen() + d.b)
    assert str(expr
        ) == "Merge(_child=ZZZ, children=(ZZZ, label(strlen(_child=ZZZ.a) + ZZZ.b, 'c')))"

def test_repr_of_scalar():
    assert expr_repr(t.amount.sum()) == '300'

def test_mutable_backed_repr():
<<<<<<< left_content.py
    mutable_backed_table = data([[0]], fields=['col1'])
    repr(mutable_backed_table)
=======
    mutable_backed_table = Data([[0]], fields=['col1'])
    expr_repr(mutable_backed_table)
>>>>>>> right_content.py


def test_dataframe_backed_repr():
    df = pd.DataFrame(data=[0], columns=['col1'])
<<<<<<< left_content.py
    dataframe_backed_table = data(df)
    repr(dataframe_backed_table)
=======
    dataframe_backed_table = Data(df)
    expr_repr(dataframe_backed_table)
>>>>>>> right_content.py


def test_dataframe_backed_repr_complex():
    df = pd.DataFrame([(1, 'Alice', 100), (2, 'Bob', -200), (3, 'Charlie', 
        300), (4, 'Denis', 400), (5, 'Edith', -500)], columns=['id', 'name',
        'balance'])
<<<<<<< left_content.py
    t = data(df)
    repr(t[t['balance'] < 0])
=======
    t = Data(df)
    expr_repr(t[t['balance'] < 0])
>>>>>>> right_content.py


def test_repr_html_on_no_resources_symbol():
    t = symbol('t', '5 * {id: int, name: string, balance: int}')
    assert to_html(t) == 't'

def test_expr_repr_empty():
    s = expr_repr(t[t.amount > 1000000000.0])
    assert isinstance(s, str)
    assert 'amount' in s

def test_to_html():
    s = to_html(t)
    assert s
    assert 'Alice' in s
    assert '<table' in s
    assert to_html(1) == '1'
    assert to_html(t.count()) == '2'

def test_to_html_on_arrays():
    s = to_html(data(np.ones((2, 2))))
    assert '1' in s
    assert 'br>' in s

def test_repr_html():
    assert '<table' in t._repr_html_()
    assert '<table' in t.name._repr_html_()

def test_into():
    assert into(list, t) == into(list, tdata)

def test_serialization():
    import pickle
    t2 = pickle.loads(pickle.dumps(t))
    assert t.schema == t2.schema
    assert t._name == t2._name

def test_table_resource():
    with tmpfile('csv') as filename:
        ds = dshape('var * {a: int, b: int}')
        csv = CSV(filename)
        append(csv, [[1, 2], [10, 20]], dshape=ds)
        t = data(filename)
        assert isinstance(t.data, CSV)
        assert into(list, compute(t)) == into(list, csv)

def test_concretehead_failure():
    t = symbol('t', 'var * {x:int, y:int}')
    d = t[t['x'] > 100]
    with pytest.raises(ValueError):
        concrete_head(d)

def test_into_np_ndarray_column():
    t = data(L, fields=['id', 'name', 'balance'])
    expr = t[t.balance < 0].name
    colarray = into(np.ndarray, expr)
    assert len(list(compute(expr))) == len(colarray)

def test_into_nd_array_selection():
    t = data(L, fields=['id', 'name', 'balance'])
    expr = t[t['balance'] < 0]
    selarray = into(np.ndarray, expr)
    assert len(list(compute(expr))) == len(selarray)

def test_into_nd_array_column_failure():
    tble = data(L, fields=['id', 'name', 'balance'])
    expr = tble[tble['balance'] < 0]
    colarray = into(np.ndarray, expr)
    assert len(list(compute(expr))) == len(colarray)

def test_Data_attribute_repr():
    t = data(CSV(example('accounts-datetimes.csv')))
    result = t.when.day
    expected = pd.DataFrame({'when_day': [1, 2, 3, 4, 5]})
    assert expr_repr(result) == repr(expected)


def test_can_trivially_create_csv_data():
    data(example('iris.csv'))
    with data(example('iris.csv')) as d:
        assert d is not None

def test_can_trivially_create_csv_Data():
    Data(example('iris.csv'))
    with Data(example('iris.csv')) as d:
        assert d is not None

def test_can_trivially_create_csv_Data_with_unicode():
    if sys.version[0] == '2':
        assert isinstance(data(example(u'iris.csv')).data, CSV)

def test_can_trivially_create_sqlite_table():
    pytest.importorskip('sqlalchemy')
    data('sqlite:///' + example('iris.db') + '::iris')
    with data('sqlite:///' + example('iris.db') + '::iris') as d:
        assert d is not None

@pytest.mark.xfail(sys.platform != 'darwin', reason='h5py/pytables mismatch')
@pytest.mark.skipif(sys.version_info[:2] == (3, 4) and sys.platform ==
    'win32', reason='PyTables + Windows + Python 3.4 crashes')
def test_can_trivially_create_pytables():
    pytest.importorskip('tables')
    with data(example('accounts.h5') + '::/accounts') as d:
        assert d is not None

def test_data_passes_kwargs_to_resource():
    assert data(example('iris.csv'), encoding='ascii').data.encoding == 'ascii'

def test_data_on_iterator_refies_data():
    tdata = [1, 2, 3]
    d = data(iter(tdata))
    assert into(list, d) == tdata
    assert into(list, d) == tdata
    with data(iter(tdata)) as d:
        assert d is not None

def test_Data_on_json_is_concrete():
    d = data(example('accounts-streaming.json'))
    assert compute(d.amount.sum()) == 100 - 200 + 300 + 400 - 500
    assert compute(d.amount.sum()) == 100 - 200 + 300 + 400 - 500

def test_repr_on_nd_array_doesnt_err():
<<<<<<< left_content.py
    d = data(np.ones((2, 2, 2)))
    repr(d + 1)
=======
    d = Data(np.ones((2, 2, 2)))
    expr_repr(d + 1)
>>>>>>> right_content.py


def test_generator_reprs_concretely():
    x = [1, 2, 3, 4, 5, 6]
    d = data(x)
    expr = d[d > 2] + 1
    assert '4' in expr_repr(expr)


def test_incompatible_types():
    d = data(pd.DataFrame(L, columns=['id', 'name', 'amount']))
    with pytest.raises(ValueError):
        d.id == 'foo'
    result = compute(d.id == 3)
    expected = pd.Series([False, False, True, False, False], name='id')
    tm.assert_series_equal(result, expected)

def test___array__():
    x = np.ones(4)
    d = data(x)
    assert (np.array(d + 1) == x + 1).all()
    d = data(x[:2])
    x[2:] = d + 1
    assert x.tolist() == [1, 1, 2, 2]

def test_python_scalar_protocols():
    d = data(1)
    assert int(d + 1) == 2
    assert float(d + 1.0) == 2.0
    assert bool(d > 0) is True
    assert complex(d + 1.0j) == 1 + 1.0j

def test_iter():
    x = np.ones(4)
    d = data(x)
    assert list(d + 1) == [2, 2, 2, 2]

@pytest.mark.xfail(reason="DataFrame constructor doesn't yet support __array__"
    )
def test_DataFrame():
    x = np.array([(1, 2), (1.0, 2.0)], dtype=[('a', 'i4'), ('b', 'f4')])
    d = data(x)
    assert isinstance(pd.DataFrame(d), pd.DataFrame)

def test_head_compute():
    tdata = tm.makeMixedDataFrame()
    t = symbol('t', discover(tdata))
    db = into('sqlite:///:memory:::t', tdata, dshape=t.dshape)
    n = 2
    d = data(db)
    expr = d.head(n)
    s = expr_repr(expr)
    assert '...' not in s
    result = s.split('\n')[1:]
    assert len(result) == n


def test_scalar_sql_compute():
    t = into('sqlite:///:memory:::t', tdata, dshape=dshape(
        'var * {name: string, amount: int}'))
<<<<<<< left_content.py
    d = data(t)
    assert repr(d.amount.sum()) == '300'
=======
    d = Data(t)
    assert expr_repr(d.amount.sum()) == '300'
>>>>>>> right_content.py


def test_no_name_for_simple_data():
<<<<<<< left_content.py
    d = data([1, 2, 3])
    assert repr(d) == '    \n0  1\n1  2\n2  3'
=======
    d = Data([1, 2, 3])
    assert expr_repr(d) == '    \n0  1\n1  2\n2  3'
>>>>>>> right_content.py
    assert not d._name
    d = data(1)
    assert not d._name
    assert expr_repr(d) == '1'


def test_coerce_date_and_datetime():
    x = datetime.datetime.now().date()
<<<<<<< left_content.py
    d = data(x)
    assert repr(d) == repr(x)
    x = pd.Timestamp.now()
    d = data(x)
    assert repr(d) == repr(x)
    x = np.nan
    d = data(x, dshape='datetime')
    assert repr(d) == repr(pd.NaT)
    x = float('nan')
    d = data(x, dshape='datetime')
    assert repr(d) == repr(pd.NaT)
=======
    d = Data(x)
    assert expr_repr(d) == repr(x)
    x = pd.Timestamp.now()
    d = Data(x)
    assert expr_repr(d) == repr(x)
    x = np.nan
    d = Data(x, dshape='datetime')
    assert expr_repr(d) == repr(pd.NaT)
    x = float('nan')
    d = Data(x, dshape='datetime')
    assert expr_repr(d) == repr(pd.NaT)
>>>>>>> right_content.py


def test_coerce_timedelta():
    x = datetime.timedelta(days=1, hours=2, minutes=3)
<<<<<<< left_content.py
    d = data(x)
    assert repr(d) == repr(x)
=======
    d = Data(x)
    assert expr_repr(d) == repr(x)
>>>>>>> right_content.py


def test_highly_nested_repr():
<<<<<<< left_content.py
    tdata = [[0, [[1, 2], [3]], 'abc']]
    d = data(tdata)
    assert 'abc' in repr(d.head())
=======
    data = [[0, [[1, 2], [3]], 'abc']]
    d = Data(data)
    assert 'abc' in expr_repr(d.head())
>>>>>>> right_content.py


def test_asarray_fails_on_different_column_names():
    vs = {'first': [2.0, 5.0, 3.0], 'second': [4.0, 1.0, 4.0], 'third': [
        6.0, 4.0, 3.0]}
    df = pd.DataFrame(vs)
    with pytest.raises(ValueError) as excinfo:
        data(df, fields=list('abc'))
    inmsg = (
        "data(data_source).relabel(first='a', second='b', third='c') to rename"
        )
    assert inmsg in str(excinfo.value)

def test_functions_as_bound_methods():
    """
    Test that all functions on a _Data object are instance methods
    of that object.
    """
    callable_attrs = filter(callable, (getattr(t, a, None) for a in dir(t) if
        not a.startswith('__')))
    for attr in callable_attrs:
        assert isinstance(attr, MethodType)
        assert attr.__self__ is t

def test_all_string_infer_header():
    sdata = """x,tl,z
Be careful driving.,hy,en
Be careful.,hy,en
Can you translate this for me?,hy,en
Chicago is very different from Boston.,hy,en
Don't worry.,hy,en"""
    with tmpfile('.csv') as fn:
        with open(fn, 'w') as f:
            f.write(sdata)
        tdata = data(fn, has_header=True)
        assert tdata.data.has_header
        assert tdata.fields == ['x', 'tl', 'z']

def test_csv_with_trailing_commas():
    with tmpfile('.csv') as fn:
        with open(fn, 'wt') as f:
            f.write('a,b,c, \n1, 2, 3, ')
        csv = CSV(fn)
<<<<<<< left_content.py
        assert repr(data(fn))
=======
        assert expr_repr(Data(fn))
>>>>>>> right_content.py
        assert discover(csv).measure.names == ['a', 'b', 'c', '']
    with tmpfile('.csv') as fn:
        with open(fn, 'wt') as f:
            f.write('a,b,c,\n1, 2, 3, ')
        csv = CSV(fn)
<<<<<<< left_content.py
        assert repr(data(fn))
=======
        assert expr_repr(Data(fn))
>>>>>>> right_content.py
        assert discover(csv).measure.names == ['a', 'b', 'c', 'Unnamed: 3']


def test_pickle_roundtrip():
    ds = data(1)
    assert ds.isidentical(pickle.loads(pickle.dumps(ds)))
    assert (ds + 1).isidentical(pickle.loads(pickle.dumps(ds + 1)))
    es = data(np.array([1, 2, 3]))
    rs = pickle.loads(pickle.dumps(es))
    assert (es.data == rs.data).all()
    assert_dshape_equal(es.dshape, rs.dshape)

def test_nameless_data():
<<<<<<< left_content.py
    tdata = [('a', 1)]
    assert repr(tdata) in repr(data(tdata))
=======
    data = [('a', 1)]
    assert repr(data) in expr_repr(Data(data))
>>>>>>> right_content.py


def test_partially_bound_expr():
    df = pd.DataFrame([(1, 'Alice', 100), (2, 'Bob', -200), (3, 'Charlie', 
        300), (4, 'Denis', 400), (5, 'Edith', -500)], columns=['id', 'name',
        'balance'])
    tdata = data(df, name='data')
    a = symbol('a', 'int')
<<<<<<< left_content.py
    expr = tdata.name[tdata.balance > a]
    assert repr(expr) == 'data[data.balance > a].name'
=======
    expr = data.name[data.balance > a]
    assert expr_repr(expr) == 'data[data.balance > a].name'
>>>>>>> right_content.py


def test_isidentical_regr():
    tdata = np.array([(np.nan,), (np.nan,)], dtype=[('a', 'float64')])
    ds = data(tdata)
    assert ds.a.isidentical(ds.a)

@pytest.mark.parametrize('data,dshape,exp_type', [(1, symbol('x', 'int').
    dshape, int), (into(da.core.Array, [1, 2], chunks=(10,)), dshape(
    '2 * int'), pd.Series), (into(da.core.Array, [{'a': 1, 'b': 2}, {'a': 3,
    'b': 4}], chunks=(10, 10)), dshape('2 * {a: int, b: int}'), pd.
    DataFrame), (into(da.core.Array, [[1, 2], [3, 4]], chunks=(10, 10)),
    dshape('2 *  2 * int'), np.ndarray)])
def test_coerce_core(data, dshape, exp_type):
    assert isinstance(coerce_core(data, dshape), exp_type)

@pytest.mark.parametrize('data,res', [(1, True), (1.1, True), ('foo', True),
    ([1, 2], False), ((1, 2), False), (pd.Series([1, 2]), False)])
def test_iscorescalar(data, res):
    assert iscorescalar(data) == res

@pytest.mark.parametrize('data,res', [(1, False), ('foo', False), ([1, 2], 
    True), ((1, 2), True), (pd.Series([1, 2]), True), (pd.DataFrame([[1, 2],
    [3, 4]]), True), (np.ndarray([1, 2]), True), (into(da.core.Array, [1, 2
    ], chunks=(10,)), False)])
def test_iscoresequence(data, res):
    assert iscoresequence(data) == res

@pytest.mark.parametrize('data,res', [(1, True), ('foo', True), ([1, 2], 
    True), ((1, 2), True), (pd.Series([1, 2]), True), (pd.DataFrame([[1, 2],
    [3, 4]]), True), (np.ndarray([1, 2]), True), (into(da.core.Array, [1, 2
    ], chunks=(10,)), False)])
def test_iscoretype(data, res):
    assert iscoretype(data) == res

