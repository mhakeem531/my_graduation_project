<?php
if(isset($_POST['statueName'],$_POST['language'],$_POST['statues_table'],$_POST['images_table'])){
    $statueName=$_POST['statueName'];
    $language=$_POST['language'];
    $statues_table=$_POST['statues_table'];
    $images_table=$_POST['images_table'];

    include 'test_connection.php';

    $satue_info_array = array();

    //select audio file path
    $select_audio_file_path_sql="SELECT audioFilePathe, description  FROM $statues_table WHERE statueName like '$statueName' AND language like '$language';";
    
    //added 
    mysqli_set_charset($dbconnect,"utf8");
    
    $result = mysqli_query($dbconnect, $select_audio_file_path_sql);

    if(mysqli_num_rows($result) > 0){
        $row = $result->fetch_assoc();

        $temp['audioFilePathe'] = $row['audioFilePathe'];
        $temp['description'] = $row['description'];
    }
    else{
        $temp['audioFilePathe'] = "no audio file choosen";
        $temp['description'] = "no description avaliable";
    }

    //select image file path
    $select_image_file_path_sql="SELECT imagePath  FROM $images_table WHERE statueName like '$statueName';";
    $result = mysqli_query($dbconnect, $select_image_file_path_sql);

    if(mysqli_num_rows($result) > 0){
        $row = $result->fetch_assoc();
       

        $temp['imagePath'] = $row['imagePath'];
    }
    else{

        $temp['imagePath'] = "no image avaliable";

    }

    array_push($satue_info_array, $temp);

    echo json_encode(array("statue"=>$satue_info_array), JSON_UNESCAPED_UNICODE);

}else{
    echo "Error in query";
}

?>
