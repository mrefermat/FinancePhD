from arctic import Arctic
store = Arctic('localhost')
price_table = store['CHINA_PX']
volume_table = store['CHINA_VOL']


def compare(last,this):
    if this[1:]>last[1:] or this[0] > last[0]:
        return this
    else:
        return last


def adjusted_returns(price,volume):
    rtn=price.pct_change()
    ww=volume.apply(lambda s: s.nlargest(2).index.tolist(), axis=1)
    s=ww.copy()
    mon='A00'
    spread=0
    for ind, val in ww.iteritems():
        mon=compare(mon,val[0])
        s.ix[ind]=rtn[val[0]].ix[ind]
    return s


def update_data():
 	return

def load_market_price(market):
	return price_table.read(mkt).data

def load_market_volume(market):
	return volume_table.read(mkt).data

# Currently returns a Series with just the adjusted price.
# We could think about adding the 'adjusted' or total volume
def get_timeseries(market):
	price=load_market_price(market)
	volume=load_market_volume(market)
	return adjusted_returns(price,volume)