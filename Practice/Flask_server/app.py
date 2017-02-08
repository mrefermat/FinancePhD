from flask import Flask, render_template,jsonify, request
import pandas as pd
from pandas.compat import StringIO
from pandas.io.common import urlopen
from pandas_highcharts.core import serialize
from arctic import Arctic
from pymongo import MongoClient
from flask_bootstrap import Bootstrap
from chart_types import *

app = Flask(__name__)
store = Arctic('localhost')
store.initialize_library('FUTURES')
library = store['FUTURES']

@app.route('/')
@app.route('/index')
def index(chartID = 'chart_ID', chart_type = 'barh', chart_height = 500):
    chart = {"renderTo": chartID, "type": chart_type, "height": chart_height,}
    series = [{"name": 'Label1', "data": [1,2,3]}, {"name": 'Label2', "data": [4, 5, 6]}]
    title = {"text": 'My Title'}
    xAxis = {"categories": ['xAxis Data1', 'xAxis Data2', 'xAxis Data3']}
    yAxis = {"title": {"text": 'yAxis Label'}}
    return render_template('index.html', chartID=chartID, chart=chart, series=series, title=title, xAxis=xAxis, yAxis=yAxis)

@app.route('/macro')
def macro(chartID = 'chart_ID', chart_type = 'bar', chart_height = 500):
	chart_data=zscore_ranked('chart1',library)
	data2=currency_markets_charts('chart2',library)
	data3=fixed_income_markets_charts('chart3',library)
	data4=commodity_markets_charts('chart4',library)
	data5=equity_markets_charts('chart5',library)
	data6=equity_markets_charts('chart6',library)
	return render_template('graph.html', 
		chart1=chart_data, 
		chart2=data2,
		chart3=data3,
		chart4=data4,
		chart5=data5,
		chart6=data6)

@app.route('/about')
def about(chartID = 'chart_ID', chart_type = 'bar', chart_height = 500):
	return render_template('about.html')

if __name__ == "__main__":
    app.run(debug = True, host='0.0.0.0', port=5000, passthrough_errors=True,threaded=True)