import pandas as pd
import seaborn as sns
import math
import numpy as np
import quandl
from scipy.stats import norm
import statsmodels.formula.api as sm
token="Us3wFmXGgAj_1cUtHAAR"

def load_maps():
    return pd.read_csv('./mkts_extend.csv',index_col='Market')

# Function to return a dictionary of the refinded by the list you give 
# it (to ensure it doesn't cause errors later)
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

def load_fx():
    fx=pd.read_csv('./currency.csv',index_col=0,parse_dates=['DATE'])
    fx['USD']=1
    return fx  

def load_volume():
    data=pd.read_csv('./Volume.csv',index_col=0,parse_dates=['Date']).resample(rule='m',how='mean')
    v2 = clean_up_columns(data)
    v=pd.read_csv('./volume_data.csv',index_col=0,parse_dates=['Date']).resample(rule='m',how='sum')
    volume=pd.DataFrame()
    volume=pd.rolling_mean(v,250,min_periods=100).resample(rule='m',how='mean')[:'2016']
    for x in v2.columns:
        name = x.split(' TRc1')[0]
        volume[name]=pd.rolling_mean(v2[x],250,min_periods=50).resample(rule='m',how='mean')
    return volume

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
            print(m)    
    return total_vol

def calculate_amihud_liquidity(cleansed):
    volume=load_daily_volume()
    contract_size=load_maps()
    fx=load_fx()
    fx_map=contract_size.to_dict()['Currency']
    tick_map=contract_size.to_dict()['Tick Value']
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
        print(upper_range)

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
            print(ans[ans])
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

def load_amihud_markets_price():
    a=pd.read_csv('./AmihudMarket.csv').set_index('Market')
    return load_price()[a.index]


def load_price():
    data=pd.read_csv('./Price.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='last')
    price = clean_up_columns(data)
    qd=pd.read_csv('./liquid_contracts.csv',index_col=0,parse_dates=['Date']).resample(rule='d',how='last')
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
            print(rm_mkt)
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
    x=pnl.dropna(how='all')
    return x.divide(x.count(axis=1),axis=0).sum(axis=1)

def quantile_portfolios_annual_with_rank(rank_data,price_data,number_of_buckets=10):
    deciles={}
    med_value={}
    for i in range(0,number_of_buckets,1):
        deciles[str(i)]=pd.Series()
        med_value[str(i)]=pd.Series()
    for y in range(rank_data.index[0].year,rank_data.index[-1].year,1):
        year=str(y) + '-12-31'
        for i in range(0,number_of_buckets,1):
            mkts=quantile_columns(rank_data.resample(rule='a',how='median'),year,number_of_buckets,i)
            rtns = price_data.resample(rule='m',how='last')[mkts].pct_change()[str(y+1)].mean(axis=1)
            med=rank_data[mkts][str(y+1)].median(axis=1)
            deciles[str(i)]=deciles[str(i)].append(rtns)
            med_value[str(i)]=med_value[str(i)].append(med)
    return pd.DataFrame(deciles),pd.DataFrame(med_value)

def quantile_portfolios_annual(rank_data,price_data,number_of_buckets=10):
    deciles={}
    for i in range(0,number_of_buckets,1):
        deciles[str(i)]=pd.Series()
    for y in range(rank_data.index[0].year,rank_data.index[-1].year,1):
        year=str(y) + '-12-31'
        for i in range(0,number_of_buckets,1):
            mkts=quantile_columns(rank_data.resample(rule='a',how='median'),year,number_of_buckets,i)
            rtns = price_data.resample(rule='m',how='last')[mkts].pct_change()[str(y+1)].mean(axis=1)
            deciles[str(i)]=deciles[str(i)].append(rtns)
    return pd.DataFrame(deciles)

def double_sort_annual(rank_data,second_sort_data,price_data,number_of_buckets=10,second_sort_buckets=2):
    deciles={}
    for i in range(0,number_of_buckets,1):
        for j in range(0,second_sort_buckets,1):
            deciles[str(i)+'-'+str(j)]=pd.Series()
    for y in range(rank_data.index[0].year,rank_data.index[-1].year,1):
        year=str(y) + '-12-31'
        for i in range(0,number_of_buckets,1):
            mkts=quantile_columns(rank_data.resample(rule='a',how='median'),year,number_of_buckets,i)
            # Second sort
            for j in range(0,second_sort_buckets):
                mkts2=quantile_columns(second_sort_data[mkts].resample(rule='a',how='median'),year,second_sort_buckets,j)
                rtns = price_data.resample(rule='m',how='last')[mkts2].pct_change()[str(y+1)].mean(axis=1)
                deciles[str(i)+'-'+str(j)]=deciles[str(i)+'-'+str(j)].append(rtns)
    return pd.DataFrame(deciles)

def double_sort_monthly(rank_data,second_sort_data,price_data,number_of_buckets=10,second_sort_buckets=2):
    deciles={}
    for i in range(0,number_of_buckets,1):
        for j in range(0,second_sort_buckets,1):
            deciles[str(i)+'-'+str(j)]=pd.Series()
    for y in range(rank_data.index[0].year+1,rank_data.index[-1].year,1):
        for m in range(1,13,1):
            mon=str(y)+'-'+str(m)
            for i in range(0,number_of_buckets,1):
                mkts=quantile_columns_monthly(rank_data.resample(rule='m',how='median'),mon,number_of_buckets,i)
                for j in range(0,second_sort_buckets):
                    mkts2=quantile_columns_monthly(second_sort_data[mkts].resample(rule='m',how='median'),mon,second_sort_buckets,j)
                    y_temp= y
                    m_temp=m+1
                    if m==12:
                        y_temp=y+1
                        m_temp=1
                    next_mon = str(y_temp)+'-'+str(m_temp)
                    rtns = price_data.resample(rule='m',how='last')[mkts2].pct_change()[next_mon].mean(axis=1)
                    deciles[str(i)+'-'+str(j)]=deciles[str(i)+'-'+str(j)].append(rtns)
    return pd.DataFrame(deciles)
    
# After sorting the portfolios are inverse vol weighted where their volatility contribution is equal (not dollar contribution)
def quantile_portfolios_annual_inverse_vol(rank_data,price_data,number_of_buckets=10):
    deciles={}
    # Inverse vol numbers using three year look back
    vol=pd.DataFrame.rolling(price_data.resample(rule='m',how='last').pct_change(),36).std().replace(0,1)
    vol=vol.apply(lambda x: np.where(x<0.00289,1,x))
    in_vol=1/(vol.replace(0,1))
    for i in range(0,number_of_buckets,1):
        deciles[str(i)]=pd.Series()
    for y in range(rank_data.index[0].year,rank_data.index[-1].year,1):
        year=str(y) + '-12-31'
        for i in range(0,number_of_buckets,1):
            mkts=quantile_columns(rank_data.resample(rule='a',how='median'),year,number_of_buckets,i)
            # Weighting schema
            a=(in_vol[str(y+1)][mkts]).sum(axis=1)
            w=((in_vol[str(y+1)][mkts]).T.div(a)).T
            rtns = w.multiply(price_data.resample(rule='m',how='last')[mkts].pct_change()[str(y+1)]).sum(axis=1)
            #rtns = price_data.resample(rule='m',how='last')[mkts].pct_change()[str(y+1)].mean(axis=1)
            deciles[str(i)]=deciles[str(i)].append(rtns)
    return pd.DataFrame(deciles)

def quantile_portfolios_annual_month_check(rank_data,price_data,number_of_buckets=10):
    deciles={}
    for i in range(0,number_of_buckets,1):
        deciles[str(i)]=pd.Series()
    for y in range(rank_data.index[0].year,rank_data.index[-1].year,1):
        year=str(y) + '-12-31'
        for i in range(0,number_of_buckets,1):
            #checks to make sure there are 12 months for the contact
            ind=rank_data[str(y)].count()==12
            temp=rank_data.T[ind].T
            mkts=quantile_columns(temp.resample(rule='a',how='median'),year,number_of_buckets,i)
            rtns = price_data.resample(rule='m',how='last')[mkts].pct_change()[str(y+1)].mean(axis=1)
            deciles[str(i)]=deciles[str(i)].append(rtns)
    return pd.DataFrame(deciles)

def quantile_portfolios_monthly_month_check(rank_data,price_data,number_of_buckets=10):
    deciles={}
    for i in range(0,number_of_buckets,1):
        deciles[str(i)]=pd.Series()
    for y in range(rank_data.index[0].year+1,rank_data.index[-1].year,1):
        for m in range(1,13,1):
            mon=str(y)+'-'+str(m)
            for i in range(0,number_of_buckets,1):
                ind=rank_data[str(y)].count()==12
                temp=rank_data.T[ind].T
                mkts=quantile_columns_monthly(temp.resample(rule='m',how='median'),mon,number_of_buckets,i)
                y_temp= y
                m_temp=m+1
                if m==12:
                    y_temp=y+1
                    m_temp=1
                next_mon = str(y_temp)+'-'+str(m_temp)
                rtns = price_data.resample(rule='m',how='last')[mkts].pct_change()[next_mon].mean(axis=1)
                deciles[str(i)]=deciles[str(i)].append(rtns)
    return pd.DataFrame(deciles)
    
def quantile_portfolios_monthly(rank_data,price_data,number_of_buckets=10):
    deciles={}
    for i in range(0,number_of_buckets,1):
        deciles[str(i)]=pd.Series()
    for y in range(rank_data.index[0].year+1,rank_data.index[-1].year,1):
        for m in range(1,13,1):
            mon=str(y)+'-'+str(m)
            for i in range(0,number_of_buckets,1):
                mkts=quantile_columns_monthly(rank_data.resample(rule='m',how='median'),mon,number_of_buckets,i)
                y_temp= y
                m_temp=m+1
                if m==12:
                    y_temp=y+1
                    m_temp=1
                next_mon = str(y_temp)+'-'+str(m_temp)
                rtns = price_data.resample(rule='m',how='last')[mkts].pct_change()[next_mon].mean(axis=1)
                deciles[str(i)]=deciles[str(i)].append(rtns)
    return pd.DataFrame(deciles)

# Inverse vol weighted monthly portfolio sorts
def quantile_portfolios_monthly_inverse_vol(rank_data,price_data,number_of_buckets=10):
    deciles={}
    # Inverse vol numbers using three year look back
    vol=pd.DataFrame.rolling(price_data.resample(rule='m',how='last').pct_change(),36).std().replace(0,1)
    vol=vol.apply(lambda x: np.where(x<0.00289,1,x))
    in_vol=1/(vol.replace(0,1))
    for i in range(0,number_of_buckets,1):
        deciles[str(i)]=pd.Series()
    for y in range(rank_data.index[0].year+1,rank_data.index[-1].year,1):
        for m in range(1,13,1):
            mon=str(y)+'-'+str(m)
            for i in range(0,number_of_buckets,1):
                mkts=quantile_columns_monthly(rank_data.resample(rule='m',how='median'),mon,number_of_buckets,i)
                y_temp= y
                m_temp=m+1
                if m==12:
                    y_temp=y+1
                    m_temp=1
                next_mon = str(y_temp)+'-'+str(m_temp)
                # Weighting schema
                a=(in_vol[mon][mkts]).sum(axis=1)
                w=((in_vol[mon][mkts]).T.div(a)).T
                rtns = (w.min()*price_data.resample(rule='m',how='last')[mkts].pct_change()[next_mon]).sum(axis=1)

                #rtns = price_data.resample(rule='m',how='last')[mkts].pct_change()[next_mon].mean(axis=1)
                deciles[str(i)]=deciles[str(i)].append(rtns)
    return pd.DataFrame(deciles)

def quantile_columns_monthly(df,date,buckets,number):
    s=df.T[date].dropna()
    s=s.sort_values(s.columns[0])
    lower_range = number/float(buckets)
    upper_range = (number+1)/float(buckets)
    try:
        return list(s[(s>s.quantile(lower_range)) & (s<=s.quantile(upper_range))].dropna().index)
    except:
        print(upper_range)

def calc_zscore_expanding_window(df,min_per=3):
    return (df-pd.rolling_mean(df,100000000,min_periods=min_per))/pd.rolling_std(df,100000000,min_periods=min_per)

def calc_zscore_rolling_window(df,per=36):
    return (df-pd.rolling_mean(df,per,min_periods=per))/pd.rolling_std(df,per,min_periods=per)

# Cross sectional standardization
def calc_cross_sectional_standardization(df):
    return ((((df.T-df.mean(axis=1)))/df.std(axis=1)).T)

# Returns a dictionary of all sectors and 'All Markets' using cross sectional
# standardization.  Also you can run it without equity sector
def sector_XS_normalized(raw_scores,with_equity=True):
    sector_zscores={}
    d_map=sector_map(raw_scores)
    first = True
    for sect in d_map.keys():
        sector_zscores[sect]=calc_cross_sectional_standardization(raw_scores[d_map[sect]].dropna(how='all'))
        if first:
            if with_equity or sect!='Equities':
                all_markets=sector_zscores[sect].copy()
                first=False
        else:
            if with_equity or sect!='Equities':
                all_markets=all_markets.join(sector_zscores[sect])
    sector_zscores['All']=all_markets
    return sector_zscores

# Expontially weighted with a default of two years (24 months)
def calc_zscore_ew(df,lookback=24):
    return (df-pd.ewma(df,lookback,min_periods=12))/pd.ewmstd(df,lookback,min_periods=12)

def calculate_FHT(cleansed):
    data=pd.DataFrame()
    for c in cleansed.columns:
        x=cleansed[c].dropna().pct_change()
        nonzero=x[x==0].resample(rule='m',how='count')
        Z=(nonzero/x.resample(rule='m',how='count')).dropna()
        data[c]=pd.Series(norm.cdf((1+Z)/2.),index=Z.index)*2*pd.rolling_std(x,12).resample(rule='m',how='last')
    return data
    
def portfolio_sort_table(un_dec,sector_rtn,d_map,sector):
    un_dec['Factor']=un_dec[un_dec.columns[-1]]-un_dec[un_dec.columns[0]]
    ind=un_dec['2000':'2016'].dropna(how='all').index
    # AR(1) first
    ex=un_dec.dropna(how='all')
    en=ex.shift(-1).dropna()
    en['Intercept']=1
    ex=ex.ix[en.index]
    r2=[]
    coef=[]
    tstat=[]
    for i in un_dec.columns:
        res=sm.OLS(ex[str(i)],en[[str(i)]]).fit(cov_type='HAC',cov_kwds={'maxlags':1})
        coef.append(res.params[str(i)])
        tstat.append(res.tvalues[str(i)])        
    ar1=pd.DataFrame()
    ar1['Coef']=pd.Series(coef,index=un_dec.columns)
    ar1['Tstats']=pd.Series(tstat,index=un_dec.columns)
    # CAPM regression
    capm_factor=pd.DataFrame()
    if sector == 'All':
        capm_factor['Mkt-RF']=sector_rtn.mean(axis=1)
    else:
        capm_factor['Mkt-RF']=sector_rtn[d_map[sector]].mean(axis=1)
    capm_factor['Intercept']=1
    alpha=[]
    beta=[]
    tstat_alpha=[]
    tstat_beta=[]
    for i in un_dec.columns:
        res=sm.OLS(un_dec.dropna()[str(i)].loc[ind],capm_factor[['Intercept','Mkt-RF']].loc[ind]).fit(cov_type='HAC',cov_kwds={'maxlags':1})
        alpha.append(res.params['Intercept'])
        beta.append(res.params['Mkt-RF'])
        tstat_alpha.append(res.tvalues['Intercept'])
        tstat_beta.append(res.tvalues['Mkt-RF']) 
        r2.append(res.rsquared_adj)  
    CAPM=pd.DataFrame()
    CAPM['Alpha']=pd.Series(alpha,index=un_dec.columns)
    CAPM['Alpha Tstat']=pd.Series(tstat_alpha,index=un_dec.columns)
    CAPM['Beta']=pd.Series(beta,index=un_dec.columns)
    CAPM['Beta Tstat']=pd.Series(tstat_beta,index=un_dec.columns)
    CAPM['r2']=pd.Series(r2,index=un_dec.columns)
    # Presentation
    res=pd.DataFrame()
    res['Monthly Return (in %)']=un_dec.mean()*100
    res['Standard Deviation']=un_dec.std()*math.sqrt(12)*100
    res['Information Ratio']=calc_Sharpe(un_dec)
    res['Skewness']=un_dec.skew()
    res['Excess Kurtosis']=un_dec.kurtosis()
    #res['AR(1)']=ar1.Coef
    #res['AR(1) Tstat']=ar1.Tstats
    res['CAPM Alpha Annualized (in %)']=CAPM.Alpha*1200
    res['CAPM Alpha Tstat']=CAPM['Alpha Tstat']
    res['CAPM Beta (in %)']=CAPM.Beta
    res['CAPM Beta Tstat']=CAPM['Beta Tstat']
    res['$R^2$']=CAPM.r2.abs()
    res =res.round(2)
    return res.T

# Function takes a series and returns residuals of AR(2) process with constant
def calc_AR2_resid(data):
    ar=pd.DataFrame()
    data=data.dropna()
    ar['T-2']=data
    ar['T-1']=data.shift()
    ar['T']=data.shift(2)
    ar=ar.dropna()
    ar['const']=1
    rest=sm.OLS(ar['T'],ar[['T-2','T-1']]).fit()
    return rest.resid

# Cacluates residual data frame using the entire history (not expanding window)
# Calculates innovations to liquidity for each market within the dataframe
def calc_resid_df(data):
    resid_df=pd.DataFrame()
    for m in data.columns:
        resid_df[m]=calc_AR2_resid(data[m])
    return resid_df

def read_monthly(amihud=True,sorts=2,xs=False,IV=False):
    data={}
    for s in ['Agriculturals','Currencies','Energies','Equities',
                'Metals','Fixed Income','All']:
        if amihud:
            if sorts==2:
                if xs:
                    data[s]=pd.read_pickle('data/'+s+'_monthly_2_XS.pickle')
                else:
                    data[s]=pd.read_pickle('data/'+s+'_monthly.pickle')
            elif sorts==3:
                if xs:
                    if IV:
                        data[s]=pd.read_pickle('data/'+s+'_monthly_3_XS_IV.pickle')
                    else:
                        data[s]=pd.read_pickle('data/'+s+'_monthly_3_XS.pickle')
                else:
                    data[s]=pd.read_pickle('data/'+s+'_monthly_3.pickle')
            elif sorts==10:
                data[s]=pd.read_pickle('data/'+s+'_monthly_10.pickle')
        else:
            if sorts==2:
                if xs:
                    data[s]=pd.read_pickle('data/'+s+'_monthly_FHT_2_XS.pickle')
                else:
                    data[s]=pd.read_pickle('data/'+s+'_monthly_FHT.pickle')
            elif sorts==3:
                if xs:
                    if IV:
                        data[s]=pd.read_pickle('data/'+s+'_monthly_FHT_3_XS_IV.pickle')  
                    else:
                        data[s]=pd.read_pickle('data/'+s+'_monthly_FHT_3_XS.pickle')
                else:
                    data[s]=pd.read_pickle('data/'+s+'_monthly_FHT_3.pickle')
            elif sorts==10:
                data[s]=pd.read_pickle('data/'+s+'_monthly_FHT_10.pickle')
    return data

# From Asness et al (2013), Value Momentum Everywhere. Source aqr.com
# pass sector = 'GLOBAL' if you want everything   
def get_aqr_factors(sector,mom=True,val=True,tsmom=True,carry=True):
    aqr=pd.read_csv('AQR.csv',parse_dates=['DATE'],index_col=0)
    col=[]
    if sector in ('All', 'GLOBAL'):
        return aqr[['VAL','MOM','TSMOM','CARRY']]
    sec_map={'Equities':'EQ',
             'Commodities':'CO',
             'Currencies':'FX',
             'Fixed Income':'FI',
             'Agriculturals':'CO',
             'Metals':'CO',
             'Energies':'CO'
            }
    if mom:
        col.append('MOM_'+sec_map[sector])
    if val:
        col.append('VAL_'+sec_map[sector])
    if tsmom:
        col.append('TSMOM_'+sec_map[sector])
    if carry:
        col.append('CARRY_'+sec_map[sector])
    return aqr[col]

#Function must be run with internet connection
def liquidity_observables():
    funding_liquidity = pd.DataFrame()
    ted=quandl.get('FRED/TEDRATE',authtoken=token).Value
    tbill=quandl.get('FRED/DTB3',authtoken=token).Value
    libor=quandl.get('FRED/USDONTD156N',token=token).VALUE
    repo=pd.read_csv('repo.csv',index_col=0,parse_dates=True)
    # This needs to get replaced with 
    libor_term_repo=(repo.TreasuryRepo-libor).dropna()
    funding_liquidity['TED Spread']=ted.resample(rule='m',how='last')/100.
    funding_liquidity['LIBOR term repo']=libor_term_repo.resample(rule='m',how='last')/100.
    
    # Market liquidity observables
    market_liquidity = pd.DataFrame()
    PS=pd.read_csv('PS.csv',index_col=0,parse_dates=True).resample(rule='m',how='last')
    us10y_zero=quandl.get('FED/SVENY',authtoken=token).SVENY10.resample(rule='d',how='last').dropna()
    yield10=quandl.get('USTREASURY/YIELD',authtoken=token)['10 YR'].resample(rule='d',how='last').dropna()
    on_off=(us10y_zero-yield10).dropna()
    market_liquidity['PS']=PS['Innovations in aggregate liquidity'].resample(rule='m',how='last')
    market_liquidity['On versus off the run Treasuries']=on_off.resample(rule='m',how='last')
    return funding_liquidity, market_liquidity

def get_all_factors(sorts=2,xs=False):
    if sorts==2:
        return pd.read_csv('all_market_factor.csv',index_col=0,parse_dates=[0])
    if sorts==3:
        if xs:
            return pd.read_csv('all_market_factor_3_XS.csv',index_col=0,parse_dates=[0])
        else:
            return pd.read_csv('all_market_factor_3.csv',index_col=0,parse_dates=[0])
    if sorts==10:
        return pd.read_csv('all_market_factor_10.csv',index_col=0,parse_dates=[0])

def generate_return_table(factor10,cleansed):
    # AR(1) first
    ex=factor10.dropna(how='all')
    en=ex.shift(-1).dropna()
    en['Intercept']=1
    ex=ex.ix[en.index]
    r2=[]
    coef=[]
    tstat=[]
    for i in factor10.columns:
        res=sm.OLS(ex[str(i)],en[[str(i)]]).fit(cov_type='HAC',cov_kwds={'maxlags':1})
        coef.append(res.params[str(i)])
        tstat.append(res.tvalues[str(i)])        
    ar1=pd.DataFrame()
    ar1['Coef']=pd.Series(coef,index=factor10.columns)
    ar1['Tstats']=pd.Series(tstat,index=factor10.columns)
    # CAPM regression
    capm_factor=pd.DataFrame()
    capm_factor['Mkt-RF']=cleansed.resample(rule='m',how='last').pct_change().mean(axis=1)
    capm_factor['Intercept']=1
    alpha=[]
    beta=[]
    tstat_alpha=[]
    tstat_beta=[]
    for i in factor10.columns:
        ind=factor10['2000':'2016'][i].dropna().index
        res=sm.OLS(factor10[str(i)].loc[ind],capm_factor[['Intercept','Mkt-RF']].loc[ind]).fit(cov_type='HAC',cov_kwds={'maxlags':1})
        alpha.append(res.params['Intercept'])
        beta.append(res.params['Mkt-RF'])
        tstat_alpha.append(res.tvalues['Intercept'])
        tstat_beta.append(res.tvalues['Mkt-RF']) 
        r2.append(res.rsquared_adj)  
    CAPM=pd.DataFrame()
    CAPM['Alpha']=pd.Series(alpha,index=factor10.columns)
    CAPM['Alpha Tstat']=pd.Series(tstat_alpha,index=factor10.columns)
    CAPM['Beta']=pd.Series(beta,index=factor10.columns)
    CAPM['Beta Tstat']=pd.Series(tstat_beta,index=factor10.columns)
    CAPM['r2']=pd.Series(r2,index=factor10.columns)
    # Presentation
    res=pd.DataFrame()
    res['Monthly Return (in %)']=factor10.mean()*100
    res['Standard Deviation']=factor10.std()*math.sqrt(12)*100
    res['Information Ratio']=calc_Sharpe(factor10)
    res['Skewness']=factor10.skew()
    res['Excess Kurtosis']=factor10.kurtosis()
    #res['AR(1)']=ar1.Coef
    #res['AR(1) Tstat']=ar1.Tstats
    res['CAPM Alpha Annualized (in %)']=CAPM.Alpha*1200
    res['CAPM Alpha Tstat']=CAPM['Alpha Tstat']
    res['CAPM Beta (in %)']=CAPM.Beta
    res['CAPM Beta Tstat']=CAPM['Beta Tstat']
    res['$R^2$']=CAPM.r2.abs()
    return res.round(2).T

def get_speculator_ratio():
    return pd.read_csv('speculator_ratio.csv',index_col=0,parse_dates=['Date'])