@echo off

FOR /L %%A IN (1,1,10) DO start cmd . /C "client test test.txt" &