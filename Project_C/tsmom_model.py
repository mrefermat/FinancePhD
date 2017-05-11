import pandas as pd
import quandl
import pandas as pd
import seaborn as sns
import math
import numpy as np
from datetime import datetime

def load_maps():
    return pd.read_csv('mkts.csv',index_col='Market')

def load_fx():
    fx=pd.read_csv('currency.csv',index_col=0,parse_dates=['DATE'])
    fx['USD']=1
    return fx  

def load_volume():
    data=pd.read_csv('Volume.csv',index_col=0,parse_dates=['Date']).resample(rule='m',how='mean')
    v2 = clean_up_columns(data)
    v=pd.read_csv('volume_data.csv',index_col=0,parse_dates=['Date']).resample(rule='m',how='sum')
    volume=pd.DataFrame()
    volume=pd.rolling_mean(v,250,min_periods=100).resample(rule='m',how='mean')[:'2016']
    for x in v2.columns:
        name = x.split(' TRc1')[0]
        volume[name]=pd.rolling_mean(v2[x],250,min_periods=50).resample(rule='m',how='mean')
    return volume

def load_daily_volume():
    data=pd.read_csv('Volume.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='mean')
    v2 = clean_up_columns(data)
    v=pd.read_csv('volume_data.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='sum')
    volume=pd.DataFrame()
    volume=pd.rolling_mean(v,250,min_periods=100).resample(rule='d',how='mean')[:'2016']
    for x in v2.columns:
        name = x.split(' TRc1')[0]
        volume[name]=pd.rolling_mean(v2[x],250,min_periods=50).resample(rule='d',how='mean')
    return volume.dropna(how='all')

def calculate_dollar_volume(cleansed):
    volume=load_volume()
    contract_size =load_maps()
    fx=load_fx()
    fx_map=contract_size.to_dict()['Currency']
    tick_map=contract_size.to_dict()['Tick Value']
    sector_map=contract_size.to_dict()['Sector']
    fx=fx.resample(rule='m',how='last')
    px=cleansed.resample(rule='m',how='last')
    total_vol=pd.DataFrame()
    for m in cleansed.columns:
        try:
            curr= str(fx_map[m])
            total_vol[m] = (px[m]/fx[curr]*volume[m]*tick_map[m]).ffill()[:'2016'] 
        except:
            print m    
    return total_vol

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
            print m
    x= (cleansed.pct_change().abs()/ total_vol).resample(rule='m',how='mean')
    return x.replace([np.inf, -np.inf], np.nan)
    
def quantile_pnl_and_means(cleansed,total_volume,pnl,number_of_buckets):
    col=[]
    col.append('Year')
    for i in range(1,number_of_buckets+1,1):
        col.append(str(i))
    bkts=[]
    expected_rtn=[]
    for y in range(2000,2017,1):
        year=str(y) + '-12-31'
        sharpes=[]
        means=[]
        sharpes.append(y)
        means.append(y)
        for i in range(0,number_of_buckets,1):
            mkts=quantile_columns(total_volume,year,number_of_buckets,i)
            sharpes.append(pnl[mkts][str(y)].mean().mean())
            means.append(cleansed.resample(rule='m',how='last')[mkts].pct_change()[str(y)].mean().mean()) 
        bkts.append(sharpes)
        expected_rtn.append(means)
    df=pd.DataFrame(bkts,columns=col).set_index('Year')
    m=pd.DataFrame(expected_rtn,columns=col).set_index('Year')
    return df, m

def quantile_columns(df,date,buckets,number):
    s=df.T[date].dropna().sort_values()
    lower_range = number/float(buckets)
    upper_range = (number+1)/float(buckets)
    try:
        return list(s[(s>s.quantile(lower_range)) & (s<=s.quantile(upper_range))].dropna().index)
    except:
        print upper_range

# Function to give list of correlations above a certain amount
def pair_correlation(df,level):
    corr=df.resample(rule='m',how='last').corr()
    pairs=[]
    for mkt in df.T.T.columns:
        ans= corr[mkt].sort_values().tail().head(4) >level
        if ans[ans].count() ==1:
            if mkt != ans[ans].index[0]:
                pairs.append([mkt,ans[ans].index[0]])
        elif ans[ans].count() ==0:
            continue
        else:
            print ans[ans]
    return pairs

# Function to seperate which has longer data.  takes a list of list
def longer_list(pairs,df):
    more =[]
    less =[]
    for x,y in pairs:
        if x == y:
            continue
        mkt1 =df[x].resample(rule='m',how='last').count()
        mkt2 =df[y].resample(rule='m',how='last').count()
        if mkt1>mkt2:
            more.append(x)
            less.append(y)
        else:
            more.append(y)
            less.append(x)        
    return more, less

def load_price():
    data=pd.read_csv('Price.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='last')
    price = clean_up_columns(data)
    qd=pd.read_csv('liquid_contracts.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='last')
    df=pd.DataFrame()
    df=price.copy()
    for x in qd.columns:
        df[x]=qd[x]
    return df

def cleansed_data():
    price = load_price()
    pairs = pair_correlation(price,.99)
    more , less = longer_list(pairs,price)
    cleansed = price.copy()
    for rm_mkt in set(less):
        try:
            cleansed.drop(rm_mkt, axis=1, inplace=True)
        except:
            print rm_mkt
    data = cleansed.resample(rule='m').last()[:'2016']
    cleansed=cleansed.T[data.count()>48].T
    return cleansed
        
def clean_up_columns(data):
    df=pd.DataFrame()
    for old_name in data.columns:
        new_name = old_name.split('TRc1')[0][:-1]
        df[new_name]=data[old_name]
    return df

def tsmom(data,months):
    vol=pd.rolling_std(data.pct_change(),24)*math.sqrt(12).resample(rule='m',how='last')
    signal=data/data.shift(months)-1
    signal = signal /abs(signal)
    position=signal / vol 
    return position

# TODO: For some reason this works in notebooks but not here
def tsmom_improved(data,months):
    vol=pd.ewmstd(data.pct_change(),500)*math.sqrt(12)
    data = data.resample(rule='m',how='last')
    signal=data/data.shift(months)-1
    signal = signal /abs(signal)
    position=signal / vol 
    return position

def calc_pnl(position,data):
    return position*data.pct_change().shift(1)

def calc_Sharpe(pnl,N=12):
    return np.sqrt(N) * pnl.mean() / pnl.std()

def ew_portfolio_pnl(pnl):
    return pnl.divide(pnl.count(axis=1),axis=0).sum(axis=1)

