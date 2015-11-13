@echo off
pushd .
set serverbasepath=%CD%
java -cp %serverbasepath%\bin -Djava.security.policy=%serverbasepath%\policy ca.polymtl.inf4410.tp2.worker.Main %*
popd