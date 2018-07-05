<?php
/**
 * if our generated username is not unique
 * we will use this script after rquest user to try enter uniquer username
 * 
 * then by this user mail we will update value of it's username and value of updatedAt in the table 
 */
if(isset($_POST['mail']) && isset($_POST['username'])){
    $user_name = $_POST['username'];
    $mail = $_POST['mail'];
    $date = date("Y-m-d H:i:s");

    require "test_connection.php";

    $sql = "UPDATE `xyz_usersM` SET `userName` = '". $user_name 
           ."', `updated_at` = '".$date 
           ."'  WHERE `email` = '" . $mail . "'"; 

    if ($dbconnect->query($sql) === TRUE) {
        echo "1";
    } else {
        echo "0";
    }

    $dbconnect->close();

}else{
    echo "error";
}
?>