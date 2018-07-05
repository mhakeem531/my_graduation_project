<?php

header('Content-type : bitmap; charset=utf-8');
if(isset($_POST['user_id'],$_POST['file_name'], $_POST['file'])){

    $user_id = $_POST['user_id'];
    $file_name = $_POST['file_name'];
    $file = $_POST['file'];

    include 'test_connection.php';

    $server_ip= gethostbyname(gethostname());

    $complete_file_path = "feedback_attached_photos/" . $file_name;// . ".jpg";

    $InsertSQL = "INSERT INTO xyz_feedbackM (photoPath, userId, postedAt) values('$complete_file_path','$user_id',NOW());";
    
    if(mysqli_query($Res, $InsertSQL)){

        file_put_contents($complete_file_path,base64_decode($file));

        echo "Your Image Has Been Uploaded.";

      /*  echo "Your Image Has Been Uploaded.0";
        $decode = base64_decode($file);
        echo "Your Image Has Been Uploaded.1";
        $file2 = fopen($complete_file_path, 'wb');
        echo "Your Image Has Been Uploaded.2";
        $is_written = fwrite($file2, $decode);
        echo "Your Image Has Been Uploaded.3";
        fclose($file2);
        echo "Your Image Has Been Uploaded.4";
        if($is_written > 0){

            echo "Your Image Has Been Uploaded.";
        }*/

    } else {

        echo 'failed uploaded';   
    } 

}else{
    echo "unknown error";
}