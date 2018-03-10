import pandas as pd
import seaborn as sns
import math
import numpy as np
from datetime import datetime
from scipy.stats import norm
import statsmodels.formula.api as sm


def load_amihud_markets_price():
    a=pd.read_csv('./AmihudMarket.csv').set_index('Market')
    return load_price()[a.index]

def clean_up_columns(data):
    df=pd.DataFrame()
    for old_name in data.columns:
        new_name = old_name.split('TRc1')[0][:-1]
        df[new_name]=data[old_name]
    return df

def load_maps():
    return pd.read_csv('./mkts.csv',index_col='Market')

def load_daily_volume():
    data=pd.read_csv('./Volume.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='mean')
    v2 = clean_up_columns(data)
    v=pd.read_csv('./volume_data.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='sum')
    volume=pd.DataFrame()
    volume=pd.rolling_mean(v,250,min_periods=100).resample(rule='d',how='mean')[:'2016']
    for x in v2.columns:
        name = x.split(' TRc1')[0]
        volume[name]=pd.rolling_mean(v2[x],250,min_periods=50).resample(rule='d',how='mean')
    return volume.dropna(how='all')

def load_price():
    data=pd.read_csv('./Price.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='last')
    price = clean_up_columns(data)
    qd=pd.read_csv('./liquid_contracts.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='last')
    df=pd.DataFrame()
    df=price.copy()
    for x in qd.columns:
        df[x]=qd[x]
    return df

def load_amihud_markets_price():
    a=pd.read_csv('./AmihudMarket.csv').set_index('Market')
    return load_price()[a.index]

def calculate_FHT(cleansed):
    data=pd.DataFrame()
    for c in cleansed.columns:
        x=cleansed[c].dropna().pct_change()
        nonzero=x[x==0].resample(rule='m',how='count')
        Z=(nonzero/x.resample(rule='m',how='count')).dropna()
        data[c]=pd.Series(norm.cdf((1+Z)/2.),index=Z.index)*2*pd.rolling_std(x,12).resample(rule='m',how='last')
    return data

def sector_map(amihud):
    mp=load_maps()
    d_map={}
    for sect in mp.Sector.unique():
        ind=mp[mp.Sector==sect].index
        new_ind=[]
        for i in ind:
            if i in amihud.columns:
                new_ind.append(i)
        d_map[sect]=new_ind
    return d_map

def calc_zscore_expanding_window(df,min_per=3):
    return (df-pd.rolling_mean(df,100000000,min_periods=min_per))/pd.rolling_std(df,100000000,min_periods=min_per)

# Expontially weighted with a default of two years (24 months)
def calc_zscore_ew(df,lookback=24):
    return (df-pd.ewma(df,lookback,min_periods=12))/pd.ewmstd(df,lookback,min_periods=12)

def load_fx():
    fx=pd.read_csv('./currency.csv',index_col=0,parse_dates=['DATE'])
    fx['USD']=1
    return fx  

def calculate_amihud_liquidity(cleansed):
    volume=load_daily_volume()
    contract_size=load_maps()
    fx=load_fx()
    fx_map=contract_size.to_dict()['Currency']
    tick_map=contract_size.to_dict()['Tick Value']
    sector_map=contract_size.to_dict()['Sector']
    fx=fx.resample(rule='d',how='last')
    px=cleansed.resample(rule='d',how='last')
    total_vol=pd.DataFrame()
    for m in cleansed.columns:
        try:
            curr= str(fx_map[m])
            total_vol[m] = (px[m]/fx[curr]*volume[m]*tick_map[m]).ffill()[:'2016'] 
        except:
            print(m)
    x= (cleansed.pct_change().abs()/ total_vol).resample(rule='m',how='mean')
    return x.replace([np.inf, -np.inf,0], np.nan)
    