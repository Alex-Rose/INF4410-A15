#!/bin/bash

# reponses au questions de mysql-server-5.5 et de phpmyadmin en utilisant les outils fournis dans le paquage debconf-utils

# -----------------------------------

# installation de mysql-server, mysql-client, apache2,  php5, libapache2-mod-php5, php5-mysql, phpmyadmin. Dans le meme ordre

echo "Include /etc/phpmyadmin/apache.conf" | sudo tee --append /etc/apache2/apache2.conf
sudo service apache2 restart


sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password password your_password'
sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password_again password your_password'
sudo apt-get -y install mysql-server
sudo service mysql restart
sudo apt-get -y install apache2 php5 libapache2-mod-php5 php5-mysql 

sudo debconf-set-selections <<<  "phpmyadmin phpmyadmin/reconfigure-webserver multiselect apache2"
sudo debconf-set-selections <<<  "phpmyadmin phpmyadmin/dbconfig-install boolean true" 
sudo debconf-set-selections <<<  "phpmyadmin phpmyadmin/mysql/admin-user string root" 
sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/mysql/admin-pass password your_password" 
sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/mysql/app-pass password your_password2" 
sudo debconf-set-selections <<< "phpmyadmin phpmyadmin/app-password-confirm password your_password2"

sudo apt-get -y install phpmyadmin

sudo service apache2 reload
