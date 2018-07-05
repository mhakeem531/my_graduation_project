<?php

/**
 * step two in google+ login
 * we will insert new user to server with Gmail account
 */


require_once 'include/DB_Functions.php';
$db = new DB_Functions();

if (isset($_POST['email']) 
    && isset($_POST['displayed_name']) 
    && isset($_POST['user_name'])
    && isset($_POST['token'])) {

    /***
         * 
         * here mean that user for the first time user his gmail account and used it by 
         * google+ button
         * we need to store new user
         * --------------------
         * get token
         * get mail
         * get username
         * get displayed name
         * 
         */

          // receiving the post params
     
          $displayed_name = $_POST['displayed_name'];
 
          $email = $_POST['email'];
    
          $user_name = $_POST['user_name'];

          $token = $_POST['token'];

          $google_plus_state = 1;

    
        // create a new user
        $user = $db->storeUserWithGoogle($email, $user_name, $displayed_name, $token, $google_plus_state);
       
        if ($user) {
            echo "registerd with google plus";
            
        } else {

            echo "something went wrong";
        }
    } else {
    echo "something went wrong";
}
?>

