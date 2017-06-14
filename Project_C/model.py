import pandas as pd
from marketdata import *
import math

def tsmom_daily(data,signal_lookback,vol_lookback=20):
	mul=get_contract_multipliers()[data.columns]
	vol=pd.ewmstd(data,vol_lookback,min_periods=vol_lookback)*math.sqrt(256)
	signal=pd.rolling_mean(data,signal_lookback)
	signal = signal /abs(signal)
	position=(signal / (vol *mul))
	return position.shift(1)

# TODO: Further test this out to ensure the vol targeting hits the appropriate
#  level and to ensure the lookback for z-scoring is correct as well
def ewma_mom_daily(data,short_lookback,long_lookback,vol_lookback=20):
	mul=get_contract_multipliers()
	vol=pd.ewmstd(data,vol_lookback,min_periods=vol_lookback)*math.sqrt(256)
	signal=signal=pd.ewma(data,short_lookback)-pd.ewma(data,long_lookback)
	# Rolling z secore using longer lookback
	zscore= calc_zscore(signal,long_lookback)
	position=(zscore / (vol*mul))
	return position.shift(1)

# TODO: Think about winsorising the tails
def calc_zscore(signal,lookback):
    return (signal/pd.ewmstd(signal,lookback,min_periods=lookback*2))

