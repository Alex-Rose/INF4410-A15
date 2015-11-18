Pour compiler ce projet utilisez la commande `ant`

Il est possible de supprimer les .jar g�n�r�s en utilisant `ant clean`

Pour ex�cuter un serveur de calcul, vous pouvez partir le registre RMI
en utilisant le script unix_start_rmiregistry dans un environnement linux
ou le script start_rmiregistry.cmd sous Cindows. Ces deux scripts peuvent
prendre le num�ro de port en argument. Ensuite, vous pouvez utiliser le script
worker -c configuration.xml (linux) et worker.cmd -c configuration.xml (Windows)

Pour ex�cuter le dispatcher (r�partisseur) vous pouvez utiliser le script
master -c configuration.xml (linux) ou master.cmd -c configuration.xml (linux)

Des exemples de fichier de configuration XML sont fournis avec ce projet.

Si vous souhaitez ex�cuter ce projet dans une IDE, les fonctions main se trouvent
dans ca.polymtl.inf4410.tp2.master.Main et dans ca.polymtl.inf4410.tp2.worker.Main

Il est possible d'utiliser plusieurs workers en localhost, il faut simplement que les
ports et noms d�finis dans les configurations soient diff�rents.