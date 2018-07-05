<?php

if (isset($_POST['mail'])) {
    // Include Database handler
    require_once 'include/DB_Functions.php';
    $db = new DB_Functions();
    $email = $_POST['mail'];
    if(!$db->validEmail($email)){
        echo "not";            
    }else{
        echo "good";
    }
}else{
    echo "error";
}
?>