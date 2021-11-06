CREATE DATABASE IF NOT EXISTS privilege_gateway DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

CREATE USER 'dbuser'@'localhost' IDENTIFIED BY '12345678';
CREATE USER 'dbuser'@'%' IDENTIFIED BY '12345678';

GRANT ALL ON privilege_gateway.* TO 'dbuser'@'localhost';
GRANT ALL ON privilege_gateway.* TO 'dbuser'@'%';

FLUSH PRIVILEGES;