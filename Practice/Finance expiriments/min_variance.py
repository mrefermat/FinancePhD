import numpy as np
import pandas as pd
import pandas.io.data as web

def create_portfolio(tickers, weights=None):
    if (weights is None):
        shares = np.ones(len(tickers))/len(tickers)
    portfolio = pd.DataFrame({'Tickers':tickers, 'Weights':weights},
                               index=tickers)
    return portfolio

def calculate_weighted_portfolio_value(portfolio,returns,name='Value'):
    total_weights=portfolio.Weights.sum()
    weighted_returns = returns *(portfolio.Weights / total_weights)
    return pd.DataFrame({name: weighted_returns.sum(axis=1)})

def plot_portfolio_returns(returns,title=None):
    returns.plot(figsize=(12,8))
    plt.xlabel('Year')
    plt.ylabel('Returns')
    if (title is not None): plt.title(title)
    plt.show()

def get_historical_closes(ticker,start_date,end_date):
    p = web.DataReader(ticker,"yahoo",start_date,end_date)
    d = p.to_frame()['Adj Close'].reset_index()
    d.rename(columns={'minor': 'Ticker', 'Adj Close': 'Close'}, inplace=True)
    pivoted=d.pivot(index='Date',columns='Ticker')
    pivoted.columns = pivoted.columns.droplevel(0)
    return pivoted

def calc_daily_returns(closes):
    return np.log(closes/closes.shift(1))

def minvar(prices):
    cov=np.cov((prices[1:]/prices[:-1]-1).transpose())
    vu=np.array(cov.shape[1]*[1], float)
    num=np.dot(np.linalg.inv(cov),vu)
    den=np.dot(vu,num)
    return num/den

########### Enter in whatever stock you want
stocks=['SPY','TLT','EEM','GLD','MDY','VNQ','LQD']
closes = get_historical_closes(stocks,'2005-01-01','2015-09-30')
daily_returns = calc_daily_returns(closes)
weights = minvar(closes.values)
portfolio = create_portfolio(stocks,weights)
wr = calculate_weighted_portfolio_value(portfolio, daily_returns,name='Optimizor')
with_value = pd.concat([daily_returns, wr],axis=1)
with_value.cumsum().plot()
weights
df=with_value
df['6040']=with_value.SPY*.6+with_value.TLT*.4
df.cumsum().plot()
sns.corrplot(df)


########### Now min var after 3 years of data
weights = minvar(closes[:dt(2009,1,1)].values)
portfolio = create_portfolio(stocks,weights)
wr = calculate_weighted_portfolio_value(portfolio, daily_returns)
with_value = pd.concat([daily_returns, wr],axis=1)
with_value.cumsum().plot()
weights
df=with_value
df['6040']=with_value.SPY*.6+with_value.TLT*.4


with_value.corr()
