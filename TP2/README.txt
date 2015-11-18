Pour compiler ce projet utilisez la commande `ant`

Il est possible de supprimer les .jar générés en utilisant `ant clean`

Pour exécuter un serveur de calcul, vous pouvez partir le registre RMI
en utilisant le script unix_start_rmiregistry dans un environnement linux
ou le script start_rmiregistry.cmd sous Cindows. Ces deux scripts peuvent
prendre le numéro de port en argument. Ensuite, vous pouvez utiliser le script
worker -c configuration.xml (linux) et worker.cmd -c configuration.xml (Windows)

Pour exécuter le dispatcher (répartisseur) vous pouvez utiliser le script
master -c configuration.xml (linux) ou master.cmd -c configuration.xml (linux)

Des exemples de fichier de configuration XML sont fournis avec ce projet.

Si vous souhaitez exécuter ce projet dans une IDE, les fonctions main se trouvent
dans ca.polymtl.inf4410.tp2.master.Main et dans ca.polymtl.inf4410.tp2.worker.Main

Il est possible d'utiliser plusieurs workers en localhost, il faut simplement que les
ports et noms définis dans les configurations soient différents.