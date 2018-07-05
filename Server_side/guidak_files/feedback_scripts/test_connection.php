<?php
$dbusername = 'root';
$dbserver = '127.0.0.1';
$dbpassword='';
$dbname='xyz_guidakDBM';

$Res= new mysqli($dbserver,$dbusername , $dbpassword , $dbname) or die("unable to connect");
mysqli_set_charset($Res,"utf8");
?>

