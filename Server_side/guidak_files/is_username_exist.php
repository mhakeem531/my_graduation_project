<?php

/**
 * this ecript used when programatically select username as string befor '@' sign
 * if this username already token so we will prombet from user to entert another one
 * 
 */
if(isset($_POST['username'])){
    $user_name = $_POST['username'];

    require "test_connection.php";

    $sql = "SELECT * FROM `xyz_usersM` WHERE `userName` ='". $user_name. "'";
    $result = mysqli_query($dbconnect, $sql);


    if (mysqli_num_rows($result) > 0) {
        echo "1";
    } else {
        echo "0";
    }
    $dbconnect->close();
    

}else{
    echo "error";
}
?>
