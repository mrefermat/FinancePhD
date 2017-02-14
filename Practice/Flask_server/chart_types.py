
import pandas as pd
import seaborn as sns
from pymongo import MongoClient
from datetime import datetime
from arctic import Arctic
from pandas_highcharts.core import serialize

def equity_markets_charts(div,library):
    key_markets=['FTSE 100','S&P 500','Russell 2000','EuroStoxx 50']
    df=pd.DataFrame()
    for mkt in key_markets:
        try:
            df[mkt]=library.read(mkt).data.Price
        except:
            print mkt        
    data=df.ffill()['2016':]/df.ffill()['2016':].ix[0]
    return serialize(data,render_to=div,title='Equities YTD',output_type='json')

def fixed_income_markets_charts(div,library):
    df = pd.DataFrame()
    map=pd.read_csv('markets.csv',index_col='Market').to_dict()['Sector']
    for mkt in library.list_symbols():
        if map[mkt]=='Fixed Income':
            try:
                df[mkt]=library.read(mkt).data.Price.replace(to_replace=0, method='ffill')
            except:
                print mkt 
    data=df.ffill()['2016':]/df.ffill()['2016':].ix[0]
    return serialize(data,render_to=div,title='Fixed Income YTD',output_type='json')

def currency_markets_charts(div,library):
    df = pd.DataFrame()
    map=pd.read_csv('markets.csv',index_col='Market').to_dict()['Sector']
    for mkt in library.list_symbols():
        if map[mkt]=='Currency':
            try:
                df[mkt]=library.read(mkt).data.Price.replace(to_replace=0, method='ffill')
            except:
                print mkt 
    data=df.ffill()['2016':]/df.ffill()['2016':].ix[0]
    return serialize(data,render_to=div,title='Currency YTD',output_type='json')

def commodity_markets_charts(div,library):
    df = pd.DataFrame()
    map=pd.read_csv('markets.csv',index_col='Market').to_dict()['Sector']
    for mkt in library.list_symbols():
        if map[mkt]=='Commodities':
            try:
                df[mkt]=library.read(mkt).data.Price.replace(to_replace=0, method='ffill')
            except:
                print mkt 
    data=df.ffill()['2016':]/df.ffill()['2016':].ix[0]
    return serialize(data,render_to=div,title='Commodities YTD',output_type='json')

def zscore_ranked(div,library):
    lookback=5
    markets=3
    data=pd.DataFrame()
    for mkt in library.list_symbols():
        try:
            data[mkt]=library.read(mkt).data.Price
        except:
            print mkt
    zscores=(data-pd.ewma(data,20))/pd.ewmstd(data,20)
    latest=zscores.tail(lookback)
    zscore_ranked=latest.T.sort_values(by=latest.T.columns[0]).dropna()[:markets]
    zscore_ranked=zscore_ranked.append(latest.T.sort_values(by=latest.T.columns[0]).dropna()[-markets:])
    final_data=pd.DataFrame()
    i=1
    for d in zscore_ranked.columns:
        final_data['T+'+str(i)]=zscore_ranked[d]
        i=i+1
    return serialize(final_data,render_to=div, kind="bar", title="Noteable market moves",output_type='json')

def portfolio_comparison(div,library):
    portfolio={'Treasuries ETF':.4,
              'S&P 500 ETF':.6
               }
    final=pd.DataFrame()
    for m in portfolio.keys():
        final[m]=library.read(m).data.Close.resample(rule='m',how='last')
    weights=pd.Series(portfolio)
    data=final.resample(rule='m',how='last').pct_change()*weights
    final['60/40 Portfolio']=(1+data.sum(axis=1)).cumprod()
    return serialize(final.pct_change().cumsum(),render_to=div,title='Portfolios',output_type='json')

def sector_ytd(div,library):
    data=pd.DataFrame()
    for m in library.list_symbols():
        if library.read_metadata(m).metadata['type']=='sector':
            data[m]=library.read(m).data.Close
    d=data.tail(1).T.columns[0]
    final=pd.DataFrame()
    final['MTD']=(((data['2017-2']/data['2017-2'].ix[0]).tail(1))-1).T[d]
    final['YTD']=(((data['2017']/data['2017'].ix[0]).tail(1))-1).T[d]
    return serialize(final,kind='bar',render_to=div,title='US Sector YTD',output_type='json')

