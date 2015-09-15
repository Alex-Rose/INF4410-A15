pushd .
set serverbasepath=%CD%
java -cp %serverbasepath%\bin -Djava.security.policy=%serverbasepath%\policy ca.polymtl.inf4402.tp1.client.Client
popd