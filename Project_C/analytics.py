import pandas as pd
import numpy as np


def calc_pnl(position,data):
	mul=get_contract_multipliers()
    return position*data*mul

def calc_pnl_wc(position,data,fee=0.0005):
	mul=get_contract_multipliers()
	return position*data*mul-cost_model(position,fee)

def cost_model(pos,fee=0.0005):
	return (pos.diff().abs()*fee)

def calc_Sharpe(pnl,N=12):
    return np.sqrt(N) * pnl.mean() / pnl.std()

def ew_portfolio_pnl(pnl):
	x=pnl.dropna(how='all')
	return x.divide(x.count(axis=1),axis=0).sum(axis=1)