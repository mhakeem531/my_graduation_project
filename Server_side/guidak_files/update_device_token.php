<?php

/**
 * if user unistall app and reinstall it 
 * so new device token will be generated 
 * so this new value needed to be updated for this user
 * essam@essam.com
 * 
 * 
 */

if(isset($_POST['mail']) && isset($_POST['device_token'])){

    $mail = $_POST['mail'];
    $device_token = $_POST['device_token'];

    require "test_connection.php";

    $sql = "UPDATE `xyz_usersM` SET `token` = '". $device_token 
    ."'  WHERE `email` = '" . $mail . "'"; 

    if($dbconnect->query($sql) === TRUE){
        echo "updated";
    }else{
        echo "not_updated";
    }

}else{
    echo "error";
}

?>