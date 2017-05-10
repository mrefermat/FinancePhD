from __future__ import print_function, division
from pandas import read_csv
import pandas as pd
from pandas.tools.plotting import scatter_matrix
codes = ['GDPC1','INDPRO','CPILFESL','UNRATE','GS10','GS1',
         'BAA','AAA']
names = ['Real GDP','Industrial Production','Core CPI','Unemployment Rate',
         '10 Year Yield','1 Year Yield','Baa Yield','Aaa Yield']
# r to disable escape
base_url = r'http://research.stlouisfed.org/fred2/data/'
data = []
for code in codes:
    url = base_url + code + '.csv'
    data.append(read_csv(url))

time_series = {}
for code, d in zip(codes,data):
    d.index = d.DATE
    time_series[code] = d.VALUE
merged_data = pd.DataFrame(time_series)
# Unequal length series
print(merged_data)
term_premium = merged_data['GS10'] - merged_data['GS1']
term_premium.name = 'Term'
merged_data = merged_data.join(term_premium,how='outer')
default_premium = merged_data['BAA'] - merged_data['AAA']
default_premium.name = 'Default'
merged_data = merged_data.join(default_premium,how='outer')
merged_data = merged_data.drop(['AAA','BAA','GS10','GS1'],axis=1)
print(merged_data.tail())
quarterly = merged_data.dropna()
print(quarterly.tail())

growth_rates_selector = ['GDPC1','INDPRO','CPILFESL']
growth_rates = quarterly[growth_rates_selector].pct_change()
final = quarterly.drop(growth_rates_selector, axis=1).join(growth_rates)

new_names = {'GDPC1':'GDP_growth','INDPRO':'IP_growth','CPILFESL':'Inflation',
             'UNRATE':'Unemp_rate'}
final = final.rename(columns = new_names ).dropna()
final['Unemp_rate']=final.Unemp_rate/100



final[['GDP_growth','IP_growth','Unemp_rate']].plot(subplots=True)
scatter_matrix(final[['GDP_growth','IP_growth','Unemp_rate','Inflation']],diagonal='kde')