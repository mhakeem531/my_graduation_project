<?php

/**
 * step one in google+ login
 * 1-check if this gmail is used befor or not
 * 2-if it used so how it used(by register form or by google+ button)
 */


require_once 'include/DB_Functions.php';
$db = new DB_Functions();

if (isset($_POST['email']) ) {

    // receiving the post params
    
    
    $email = $_POST['email'];

    $google_plus_state = 1;

    // check if user is already existed with the same email
    if ($db->isUserExisted($email)) {


        /********************************************************************************************
         *0 -->> this gmail is used before but with registration form not by "google plus" button
         *1 -->>   ~    ~    ~   ~    ~     and within google plus button from app
         **********************************************************************************************/
        if($db->getGmailRegisterState($email) == 0){
            //here we will tell user to log in with the form not with google+ button
            echo "0";
        }else if($db->getGmailRegisterState($email) == 1){

            //update token
            //fetch username from DB 
            //make log in as well 
            $username = $db->getUsernameByMail($email);
            echo $username;
        }
        
    } else {

        echo "3";
        
    }
} else {
    echo "something went wrong";
}
?>

