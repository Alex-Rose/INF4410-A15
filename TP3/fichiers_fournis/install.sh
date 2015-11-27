#!/bin/bash
# reponses au questions de mysql-server-5.5 et de phpmyadmin en utilisant les outils fournis dans le paquage debconf-utils
# -----------------------------------
# installation de mysql-server, mysql-client, apache2,  php5, libapache2-mod-php5, php5-mysql, phpmyadmin. Dans le meme ordre

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

DBROOT="root"
DBPASS="your_password"
MYPASS="your_password2"

echo "Include /etc/phpmyadmin/apache.conf" | tee --append /etc/apache2/apache2.conf
service apache2 restart

debconf-set-selections <<< "mysql-server mysql-server/root_password password $DBPASS"
debconf-set-selections <<< "mysql-server mysql-server/root_password_again password $DBPASS"
apt-get -y install mysql-server
service mysql restart
apt-get -y install apache2 php5 libapache2-mod-php5 php5-mysql mysql-client

debconf-set-selections <<<  "phpmyadmin phpmyadmin/reconfigure-webserver multiselect apache2"
debconf-set-selections <<<  "phpmyadmin phpmyadmin/dbconfig-install boolean true" 
debconf-set-selections <<<  "phpmyadmin phpmyadmin/mysql/admin-user string $DBROOT" 
debconf-set-selections <<< "phpmyadmin phpmyadmin/mysql/admin-pass password $DBPASS" 
debconf-set-selections <<< "phpmyadmin phpmyadmin/mysql/app-pass password $DBPASS" 
debconf-set-selections <<< "phpmyadmin phpmyadmin/app-password-confirm password $DBPASS"

apt-get -y install phpmyadmin

service apache2 restart