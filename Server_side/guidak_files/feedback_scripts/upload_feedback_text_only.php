<?php

header('charset=utf-8');
if(isset($_POST['user_id'],$_POST['feedback_text'])){

    $user_id = $_POST['user_id'];
    $feedback_text = $_POST['feedback_text'];


    include 'test_connection.php';

    $InsertSQL = "INSERT INTO xyz_feedbackM (feedbackText, userId, postedAt) values('$feedback_text','$user_id',NOW());";
    
    if(mysqli_query($Res, $InsertSQL)){

        echo "feedback text submitted.";

    } else {

        echo 'failed to submit';   
    } 

}else{
    echo "unknown error";
}