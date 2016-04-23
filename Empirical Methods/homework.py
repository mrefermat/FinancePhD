import pandas as pd

dir = '/Users/mrefermat/git/FinancePhD/Empirical Methods/'

file1 = 'Copy of PredictorData2014.xlsx'
file2 = 'measurestailrisk.xlsx'
file3 = 'VRPtable.txt'




monthly = pd.read_excel(dir+file1,sheetname='Monthly')
quarterly = pd.read_excel(dir+file1,sheetname='Quarterly')
annual= pd.read_excel(dir+file1,sheetname='Annual')

pd.read_excel(dir+file2)



pd.read_csv(dir+file3,delim_whitespace=True)