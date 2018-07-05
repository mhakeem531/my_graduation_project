<?php

/**
 * this file will be passed
 *       1-audio file url
 *       2-image file url
 *       3-array of all usernames in the group
 *   usernames array will be passed to db->getAllTokens() to return an array of all tokens for these usernames 
 * 
 */
//importing required files 
require_once '../include/DB_Functions.php';
require_once 'Firebase.php';
require_once 'Push.php'; 
 
$db = new DB_Functions();
$response = array(); 
if($_SERVER['REQUEST_METHOD']=='POST'){ 

    //hecking the required params 
    if(isset($_POST['audio_file_url']) and isset($_POST['image_file_url']) and isset($_POST['statue_description']) and isset($_POST['members_usernames'])) {

        $members_usernames = $_POST['members_usernames'];

        //getting all group tokens from database object 
        $devicetoken = $db->getAllTokens($members_usernames);

        //creating a new push
        $push = null; 

            $push = new Push(
                $_POST['audio_file_url'],
                $_POST['image_file_url'],
                $_POST['statue_description']
            );
    
        //getting the push from push object
        $mPushNotification = $push->getPush(); 

        //creating firebase class object 
        $firebase = new Firebase(); 

        //sending push notification and displaying result 
        echo $firebase->send($devicetoken, $mPushNotification);

    }else{

        $response['error']=true;

        $response['message']='Parameters missing';
    }
}else{
    $response['error']=true;
    $response['message']='Invalid request';
}

echo json_encode($response);