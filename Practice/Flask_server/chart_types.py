
import pandas as pd
import seaborn as sns
from pymongo import MongoClient
from datetime import datetime
from arctic import Arctic
from pandas_highcharts.core import serialize



def equity_markets_charts(div):
    store = Arctic('localhost')
    store.initialize_library('FUTURES')
    library = store['FUTURES']
    key_markets=['FTSE 100','S&P 500','Russell 2000','EuroStoxx 50']
    df=pd.DataFrame()
    for mkt in key_markets:
        try:
            df[mkt]=library.read(mkt).data.Price
        except:
            print mkt        
    data=df.ffill()['2016-6-30':]/df.ffill()['2016-6-30':].ix[0]
    return serialize(data,render_to=div,title='Equities YTD',output_type='json')

