<?php

/**
 * this ecript used when request username from user
 * if this username already token so we will prombet from user to entert another one
 * if enterd username is unique it's value will updated in the table
 * 
 */
if(isset($_POST['mail']) && isset($_POST['username'])){
    $user_name = $_POST['username'];
    $mail = $_POST['mail'];
    $date = date("Y-m-d H:i:s");

    require "test_connection.php";

    $sql = "SELECT * FROM `xyz_usersM` WHERE `userName` ='". $user_name. "'";
    $result = mysqli_query($dbconnect, $sql);


    if (mysqli_num_rows($result) > 0) {

        /**
         * here :
         * username is not unique another value is needed
         * 
        */
        echo "1";

    } else {

        /**
         * here :
         * username is  unique it's value will added to table
         * 
        */

        $sql = "UPDATE `xyz_usersM` SET `userName` = '". $user_name 
               ."', `updated_at` = '".$date 
               ."'  WHERE `email` = '" . $mail . "'"; 

        if ($dbconnect->query($sql) === TRUE) {
            echo "2";

            } else {
                echo "3";
            }
    }

    $dbconnect->close();
    

}else{
    echo "error";
}
?>