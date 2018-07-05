<?php
$dbname = 'xyz_guidakDBM';
$dbserver = '127.0.0.1';
$dbusername = 'root';
$dbpassword = '';

$dbconnect = mysqli_connect($dbserver, $dbusername, $dbpassword, $dbname);
/*if($dbconnect->connect_error){
    die("Connection failed: ");
}else{
    echo "Connection Success (local host)";
}*/
?>
