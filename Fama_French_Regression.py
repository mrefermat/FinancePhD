# OLS basic regression on stocks
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
y = get_historical_closes(['SPY','IJR','MDY','JCP','AAPL'],'2000-01-01','2016-02-29')
x=pd.read_excel('Factors.xlsx',index_col=0).resample(rule='m',how='last')/100
y = y.resample(rule='m',how='last').pct_change() - x.RF
pandas.ols(x=x.drop('RF',1),y=y.SPY)

