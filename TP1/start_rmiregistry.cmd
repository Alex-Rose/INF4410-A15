pushd .
set basepath=%CD%
rmiregistry -J-Djava.rmi.server.codebase=file:%basepath%\bin\
popd.