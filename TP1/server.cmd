pushd .
set serverbasepath=%CD%
java -cp %serverbasepath%\bin -Djava.rmi.server.codebase=file:%serverbasepath%\shared.jar -Djava.security.policy=%serverbasepath%\policy ca.polymtl.inf4402.tp1.server.Server
popd