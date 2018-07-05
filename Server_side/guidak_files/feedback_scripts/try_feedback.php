<?php
 $feedback = array();
 $count = 1;
 $row = array("id" => 1, 
                      "displayedName" => "mohamed hakeem",
                      "profilePhoto" => "http://192.168.1.16/guidak_images/Neferefre.jpg", 
                      "postedAt" => "30-12-2018 10:20", 
                      "feedbackText" => "hello it's me", 
                      "photoPath" => "http://192.168.1.16/guidak_images/Hesy_RaPlates.jpg" );
 while($count < 20){

    $temp['id'] = $row['id'];
    $temp['user_displayed_name'] = $row['displayedName'];
    $temp['profile_photo'] = $row['profilePhoto'];
    $temp['time'] = $row['postedAt'];
    $temp['text'] = $row['feedbackText'];
    $temp['photo_attached_url'] = $row['photoPath'];
    array_push($feedback, $temp);
    $count ++;
 }

echo json_encode(array("feedback"=>$feedback),JSON_UNESCAPED_UNICODE);
?>