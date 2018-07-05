<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions();
if (isset($_POST['email'])){

    $email = $_POST['email'];
    echo $db->getUserIDByEmail($email);  
}
?>