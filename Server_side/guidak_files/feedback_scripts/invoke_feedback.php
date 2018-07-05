<?php

 include 'test_connection.php';
 /**
 select feedback.text, feedback.uid, user.displayedname, user.username
 from feedback, user
 where user.id = feedback.uid
 limit 20;
 http://www.hendiware.com/android-webservices-%D8%A8%D8%A7%D9%84%D8%B9%D8%B1%D8%A8%D9%8A%D8%A9-%D8%A7%D9%84%D8%AF%D8%B1%D8%B3-%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%89/
*/

 $feedback = array();
 $select_all_feedback_sql="SELECT xyz_feedbackM.id, xyz_usersM.displayedName,	
                                  xyz_usersM.profilePhoto, xyz_feedbackM.postedAt,
                                  xyz_feedbackM.feedbackText, xyz_feedbackM.photoPath
                            FROM xyz_feedbackM, xyz_usersM
                            WHERE xyz_feedbackM.userId = xyz_usersM.id 
                            ORDER BY xyz_feedbackM.postedAt DESC
                            LIMIT 100;";
 

 mysqli_set_charset($Res,"utf8");
 $all_feedback = $Res->query($select_all_feedback_sql);

 if($all_feedback -> num_rows > 0){
     while($row = $all_feedback->fetch_assoc()){
        $temp['id'] = $row['id'];
        $temp['user_displayed_name'] = $row['displayedName'];
        $temp['profile_photo'] = $row['profilePhoto'];
        $temp['time'] = $row['postedAt'];
        $temp['text'] = $row['feedbackText'];
        $temp['photo_attached_url'] = $row['photoPath'];
    
        array_push($feedback, $temp);
     }
 }
 /**
 while($row = mysql_fetch_array($all_feedback, MYSQLI_ASSOC)){

    $temp['id'] = $row['id'];
    $temp['user_displayed_name'] = $row['displayedName'];
    $temp['profile_photo'] = $row['profilePhoto'];
    $temp['time'] = $row['postedAt'];
    $temp['text'] = $row['feedbackText'];
    $temp['photo_attached_url'] = $row['photoPath'];

    array_push($feedback, $temp);


 }
*/


echo json_encode(array("feedback"=>$feedback),JSON_UNESCAPED_UNICODE);


?>

