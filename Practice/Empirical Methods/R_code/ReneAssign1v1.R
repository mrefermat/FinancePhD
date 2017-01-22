# rm(list=ls())
library(openxlsx)
library(moments)
library(zoo)
library(dynlm)
library(MASS)
library(MSwM)
options(stringsAsFactors=FALSE)

#set the directory to read from
workdir="/Users/mrefermat/git/FinancePhD/Empirical Methods/"

measurestailrisk=read.xlsx(paste(c(workdir,"measurestailrisk.xlsx"),collapse=""),sheet=1,startRow=1,detectDates=T)
measurestailrisk=measurestailrisk[,-(6:7)]
measurestailrisk$Date=as.yearmon(apply(cbind(measurestailrisk$Year,measurestailrisk$Month),1,paste,collapse="-"))
tr=ts(data=measurestailrisk[,3:5],start=as.numeric(measurestailrisk[1,c(2,1)]),frequency=12)
time(tr)

predictors=read.xlsx(paste(c(workdir,"Copy\ of\ PredictorData2014.xlsx"),collapse=""),sheet=1,startRow=1,detectDates=T)
numcols=2:18; predictors[,numcols]=apply(predictors[,numcols],2,as.numeric)
predictors$dy=log(predictors$D12/lag(predictors$Index,12))
predictors$termspread=predictors$lty-predictors$tbl
predictors$defaultspread=predictors$BAA-predictors$AAA
predictors$Year=substr(predictors$yyyymm,1,4); predictors$Month=as.numeric(substr(predictors$yyyymm,5,6))
predictors$Date=as.yearmon(apply(cbind(predictors$Year,predictors$Month),1,paste,collapse="-"))
pred=ts(data=predictors[,2:21],start=as.numeric(predictors[1,22]),frequency=12)
#goyal and welch 
#use log div yield?
#DY=log of 12month moving sum of div minus log lagged index price
#TBL=3M Tbill rate
#LTY=long-term yield on gov bonds
#LTR=long-term return on gov bonds
#DFY=default yield spread=difference between BAA and AAA corp bond yields
#DFR=default return spread=long-term corp bond return minus long-term gov bond return
#Inflation=inflation
#https://research.stlouisfed.org/wp/2010/2010-008.pdf
  
voldata=read.table(paste(c(workdir,"VRPtable.txt"),collapse=""),header=T)
voldata$Date=as.yearmon(apply(cbind(voldata$Year,voldata$Month),1,paste,collapse="-"))
vol=ts(data=voldata[,3:7],start=as.numeric(voldata[1,1]),frequency=12)

#calculate lags and combine the data altogether
l1=lag(vol,1);l2=lag(vol,2);l3=lag(vol,3);l4=lag(vol,4);l5=lag(vol,5);l6=lag(vol,6)
l7=lag(vol,7);l8=lag(vol,8);l9=lag(vol,9);l10=lag(vol,10);l11=lag(vol,11);l12=lag(vol,12)
tsalldata=ts.union(vol,pred,tr,l1,l2,l3,l4,l5,l6,l7,l8,l9,l10,l11,l12)
View(tsalldata)
colnames(tsalldata)
time(tsalldata)

###################################################
###################################################
#Q2.1
#Basic Statistics
###################################################
###################################################

apply(measurestailrisk[,3:5],2,mean)
apply(measurestailrisk[,3:5],2,var)
apply(measurestailrisk[,3:5],2,skewness)
apply(measurestailrisk[,3:5],2,kurtosis)

apply(log(measurestailrisk[,3:5]),2,mean,na.rm=T)
apply(log(measurestailrisk[,3:5]),2,var,na.rm=T)
apply(log(measurestailrisk[,3:5]),2,skewness,na.rm=T)
apply(log(measurestailrisk[,3:5]),2,kurtosis,na.rm=T)

par(mfrow=c(3,1))
plot(measurestailrisk$Date,measurestailrisk$Hellinger.Tail.Risk.1,typ='l')
plot(measurestailrisk$Date,measurestailrisk$Hellinger.Tail.Risk.2,typ='l')
plot(measurestailrisk$Date,measurestailrisk$Kelly.and.Jiang,typ='l')

cor(measurestailrisk[,3:5])
acf(measurestailrisk$Hellinger.Tail.Risk.1)
acf(measurestailrisk$Hellinger.Tail.Risk.2)
acf(measurestailrisk$Kelly.and.Jiang)
acf(diff(measurestailrisk$Hellinger.Tail.Risk.1))
acf(diff(measurestailrisk$Hellinger.Tail.Risk.2))
acf(diff(measurestailrisk$Kelly.and.Jiang))

#correlation of monthly changes
cor(measurestailrisk[2:1053,3:5]-measurestailrisk[1:1052,3:5])
#correlation of annual changes
cor(measurestailrisk[13:1053,3:5]-measurestailrisk[1:1041,3:5])

which(!is.numeric(measurestailrisk[,3:5]),arr.ind=T)

acf(measurestailrisk[,3:5])
acf(measurestailrisk[2:1053,3:5]-measurestailrisk[1:1052,3:5])
acf(measurestailrisk[13:1053,3:5]-measurestailrisk[1:1041,3:5])

###################################################
###################################################
#Regressions
###################################################
###################################################

#Note: consider using log of volatility like Paye?
#number of lags, Paye use 2 based on AIC/BIC evidence, also try 4, Paye data is quarterly

yearStart=1990
yearEnd=2014.25
time(tr)
fitdata=window(tsalldata,start=yearStart,end=yearEnd)
test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=na.omit(fitdata))
colnames(tsalldata)

###################################################
#VRP
###################################################
# paste(colnames(fitdata)[1], "~", paste(colnames(fitdata)[24+5*(1:6)],sep="", collapse = "+"),sep = "")
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

basemodel=lm(vol.VRP~l1.VRP+l2.VRP,data=fitdata)
summary(basemodel)

pred.dymodel=lm(vol.VRP~l1.VRP+l2.VRP+pred.dy,data=fitdata)
summary(pred.dymodel)

pred.termspreadmodel=lm(vol.VRP~l1.VRP+l2.VRP+pred.termspread,data=fitdata)
summary(pred.termspreadmodel)

pred.defaultspreadmodel=lm(vol.VRP~l1.VRP+l2.VRP+pred.defaultspread,data=fitdata)
summary(pred.defaultspreadmodel)

pred.Rfreemodel=lm(vol.VRP~l1.VRP+l2.VRP+pred.Rfree,data=fitdata)
summary(pred.Rfreemodel)

ksmodel=lm(vol.VRP~l1.VRP+l2.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree,data=fitdata)
summary(ksmodel)

###################################################
#RV
###################################################
test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

basemodel=lm(vol.RV~l1.RV,data=fitdata)
summary(basemodel)

pred.dymodel=lm(vol.RV~l1.RV+pred.dy,data=fitdata)
summary(pred.dymodel)

pred.termspreadmodel=lm(vol.RV~l1.RV+pred.termspread,data=fitdata)
summary(pred.termspreadmodel)

pred.defaultspreadmodel=lm(vol.RV~l1.RV+pred.defaultspread,data=fitdata)
summary(pred.defaultspreadmodel) #seems to be significant

pred.Rfreemodel=lm(vol.RV~l1.RV+pred.Rfree,data=fitdata)
summary(pred.Rfreemodel) #seems to be somewhat significant

ksmodel=lm(vol.RV~l1.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree,data=fitdata)
summary(ksmodel) #default spread comes out as most significant of pred vars

###################################################
#IV
###################################################
test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

basemodel=lm(vol.IV~l1.IV+l2.IV+l3.IV,data=fitdata)
summary(basemodel)

pred.dymodel=lm(vol.IV~l1.IV+l2.IV+l3.IV+pred.dy,data=fitdata)
summary(pred.dymodel)

pred.termspreadmodel=lm(vol.IV~l1.IV+l2.IV+l3.IV+pred.termspread,data=fitdata)
summary(pred.termspreadmodel)

pred.defaultspreadmodel=lm(vol.IV~l1.IV+l2.IV+l3.IV+pred.defaultspread,data=fitdata)
summary(pred.defaultspreadmodel) #seems to be significant

pred.Rfreemodel=lm(vol.IV~l1.IV+l2.IV+l3.IV+pred.Rfree,data=fitdata)
summary(pred.Rfreemodel) 

ksmodel=lm(vol.IV~l1.IV+l2.IV+l3.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree,data=fitdata)
summary(ksmodel) #default spread comes out as most significant of pred vars

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

###################################################
#"tr.Hellinger.Tail.Risk.1" 
###################################################
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

###################################################
#"tr.Hellinger.Tail.Risk.2"
###################################################
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

###################################################
#"tr.Kelly.and.Jiang"
###################################################
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

###################################################
#super KS
#include all the tailrisk vars in the sink
###################################################
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang+tr.Hellinger.Tail.Risk.1+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang+tr.Hellinger.Tail.Risk.1+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang+tr.Hellinger.Tail.Risk.1+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

mod.mswm=msmFit(bic.lm,k=2,p=0,sw=c(T,T,T,T,T,T,T,T),control=list(parallel=F))
summary(mod.mswm)

###################################################
#MS KJ
###################################################
test.lm=lm(tr.Kelly.and.Jiang~1,data=fitdata)
mod.mswm2m=msmFit(test.lm,k=2,p=0,sw=c(T,F),control=list(parallel=F))
mod.mswm2m
summary(mod.mswm2m)
intervals(mod.mswm2m)

mod.mswm2m2v=msmFit(test.lm,k=2,p=0,sw=c(T,T),control=list(parallel=F))
mod.mswm2m2v
summary(mod.mswm2m2v)
intervals(mod.mswm2m2v)
plotProb(mod.mswm2m2v,which=1)
plotProb(mod.mswm2m2v,which=2)
plotProb(mod.mswm2m2v,which=3)

#2m2v AIC/BIC significantly better so use it

regime=1+1*(mod.mswm2m2v@Fit@smoProb[1:nrow(fitdata)+1,2]>0.5)
fitdataR1=ts(fitdata[which(regime==1),])
fitdataR2=ts(fitdata[which(regime==2),])

test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

###################################################
#MS HTR1
###################################################
test.lm=lm(tr.Hellinger.Tail.Risk.1~1,data=fitdata)
mod.mswm2m=msmFit(test.lm,k=2,p=0,sw=c(T,F),control=list(parallel=F))
mod.mswm2m
summary(mod.mswm2m)
intervals(mod.mswm2m)

mod.mswm2m2v=msmFit(test.lm,k=2,p=0,sw=c(T,T),control=list(parallel=F))
mod.mswm2m2v
summary(mod.mswm2m2v)
intervals(mod.mswm2m2v)
plotProb(mod.mswm2m2v,which=1)
plotProb(mod.mswm2m2v,which=2)
plotProb(mod.mswm2m2v,which=3)

#2m2v AIC/BIC significantly better so use it

regime=1+1*(mod.mswm2m2v@Fit@smoProb[1:nrow(fitdata)+1,2]>0.5)
fitdataR1=ts(fitdata[which(regime==1),])
fitdataR2=ts(fitdata[which(regime==2),])

test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.1,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

###################################################
#MS HTR2
###################################################
test.lm=lm(tr.Hellinger.Tail.Risk.2~1,data=fitdata)
mod.mswm2m=msmFit(test.lm,k=2,p=0,sw=c(T,F),control=list(parallel=F))
mod.mswm2m
summary(mod.mswm2m)
intervals(mod.mswm2m)

mod.mswm2m2v=msmFit(test.lm,k=2,p=0,sw=c(T,T),control=list(parallel=F))
mod.mswm2m2v
summary(mod.mswm2m2v)
intervals(mod.mswm2m2v)
plotProb(mod.mswm2m2v,which=1)
plotProb(mod.mswm2m2v,which=2)
plotProb(mod.mswm2m2v,which=3)

#2m2v AIC/BIC significantly better so use it

regime=1+1*(mod.mswm2m2v@Fit@smoProb[1:nrow(fitdata)+1,2]>0.5)
fitdataR1=ts(fitdata[which(regime==1),])
fitdataR2=ts(fitdata[which(regime==2),])

test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdataR1)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Hellinger.Tail.Risk.2,data=fitdataR2)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)

###################################################
#super KS MSM
#do MSM directly on the KS regression
###################################################
test.lm=lm(vol.VRP~l1.VRP+l2.VRP+l3.VRP+l4.VRP+l5.VRP+l6.VRP+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang+tr.Hellinger.Tail.Risk.1+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
mod.mswm=msmFit(bic.lm,k=2,p=0,sw=c(T,T,T,T,T,T,T),control=list(parallel=F))
summary(mod.mswm)

test.lm=lm(vol.RV~l1.RV+l2.RV+l3.RV+l4.RV+l5.RV+l6.RV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang+tr.Hellinger.Tail.Risk.1+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
mod.mswm=msmFit(bic.lm,k=2,p=0,sw=c(T,T,T,T,T,T),control=list(parallel=F))
summary(mod.mswm)

test.lm=lm(vol.IV~l1.IV+l2.IV+l3.IV+l4.IV+l5.IV+l6.IV+pred.dy+pred.termspread+pred.defaultspread+pred.Rfree+tr.Kelly.and.Jiang+tr.Hellinger.Tail.Risk.1+tr.Hellinger.Tail.Risk.2,data=fitdata)
aic.lm=stepAIC(test.lm,direction="both",trace=F,k=2)
bic.lm=stepAIC(test.lm,direction="both",trace=F,k=log(nrow(fitdata)))
summary(aic.lm)
summary(bic.lm)
mod.mswm=msmFit(bic.lm,k=2,p=0,sw=c(T,T,T,T,T,T,T,T),control=list(parallel=F))
summary(mod.mswm)

