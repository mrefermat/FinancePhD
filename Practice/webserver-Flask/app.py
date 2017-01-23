from flask import Flask, render_template
import quandl
import pandas as pd
from pandas.compat import StringIO
from pandas.io.common import urlopen
from pandas_highcharts.core import serialize
from arctic import Arctic
from pymongo import MongoClient

app = Flask(__name__)

@app.route('/')
@app.route('/index')
def index(chartID = 'chart_ID', chart_type = 'barh', chart_height = 500):
    chart = {"renderTo": chartID, "type": chart_type, "height": chart_height,}
    series = [{"name": 'Label1', "data": [1,2,3]}, {"name": 'Label2', "data": [4, 5, 6]}]
    title = {"text": 'My Title'}
    xAxis = {"categories": ['xAxis Data1', 'xAxis Data2', 'xAxis Data3']}
    yAxis = {"title": {"text": 'yAxis Label'}}
    return render_template('index.html', chartID=chartID, chart=chart, series=series, title=title, xAxis=xAxis, yAxis=yAxis)

@app.route('/graph_example')
def graph_example(chartID = 'chart_ID', chart_type = 'stock', chart_height = 500):
	data = """Date,Value\n2019-12-31,1.7\n2018-12-31,1.7\n2017-12-31,1.7\n2016-12-31,1.5\n2015-12-31,1.247\n2014-12-31,0.896\n2013-12-31,1.601\n2012-12-31,2.13\n2011-12-31,2.498\n2010-12-31,1.158\n2009-12-31,0.226\n2008-12-31,2.738\n2007-12-31,2.285\n2006-12-31,1.784\n2005-12-31,1.92\n2004-12-31,1.799\n2003-12-31,1.022\n2002-12-31,1.346\n2001-12-31,1.904\n2000-12-31,1.418\n1999-12-31,0.626\n1998-12-31,0.593\n1997-12-31,1.542\n1996-12-31,1.19\n1995-12-31,1.733\n1994-12-31,2.717\n1993-12-31,4.476\n1992-12-31,5.046\n1991-12-31,3.474\n1990-12-31,2.687\n1989-12-31,2.778\n1988-12-31,1.274\n1987-12-31,0.242\n1986-12-31,-0.125\n1985-12-31,2.084\n1984-12-31,2.396\n1983-12-31,3.284\n1982-12-31,5.256\n1981-12-31,6.324\n1980-12-31,5.447\n"""
	df = pd.read_csv(StringIO(data), index_col=0, parse_dates=True)
	chart_data=serialize(df,render_to='my-chart',output_type='json')
	return render_template('graph.html', chart=chart_data)

@app.route('/graph_data')
def graph_data(chartID = 'chart_ID', chart_type = 'stock', chart_height = 500):
	# Using Arctic database to store
	store = Arctic('localhost')
	# Create the library - defaults to VersionStore
	store.initialize_library('FUTURES')
	# Access the library
	library = store['FUTURES']
	df=library.read('Cotton').data[['Price','High']]
	chart_data=serialize(df,render_to='my-chart',output_type='json')
	return render_template('graph.html', chart=chart_data)

if __name__ == "__main__":
    app.run(debug = True, host='0.0.0.0', port=5000, passthrough_errors=True)