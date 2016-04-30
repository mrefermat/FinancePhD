# OLS basic regression on stocks on monthly data
import numpy as np
import pandas as pd
import pandas.io.data as web

def get_historical_closes(ticker,start_date,end_date):
    p = web.DataReader(ticker,"yahoo",start_date,end_date)
    d = p.to_frame()['Adj Close'].reset_index()
    d.rename(columns={'minor': 'Ticker', 'Adj Close': 'Close'}, inplace=True)
    pivoted=d.pivot(index='Date',columns='Ticker')
    pivoted.columns = pivoted.columns.droplevel(0)
    return pivoted
y = get_historical_closes(['SPY','GOOG','WMT','WEN','XOM','MCD','IJR','MDY','JCP','AAPL'],'2000-01-01','2016-02-29')
x=pd.read_excel('Factors.xlsx',index_col=0).resample(rule='m',how='last')/100
y = y.resample(rule='m',how='last').pct_change() - x.RF
pd.ols(x=x.drop('RF',1),y=y.SPY)



# OLS basic regression on stocks on daily data
import numpy as np
import pandas as pd
import pandas.io.data as web

y = get_historical_closes(['SPY','GOOG','WMT','WEN','XOM','MCD','IJR','MDY','JCP','AAPL'],'1970-01-01','2016-01-29')
x=pd.read_excel('FF_daily.xlsx',index_col=0).resample(rule='d',how='last')
y = y.resample(rule='d',how='last').pct_change() - x.RF
pd.ols(x=x,y=y.SPY)
