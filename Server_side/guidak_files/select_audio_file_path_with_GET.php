<?php
if(isset($_POST['statueName'],$_POST['language'])){
    $statueName=$_POST['statueName'];
    $language=$_POST['language'];
    include 'test_connection.php';

    $satue_info_array = array();

    //select audio file path
    $select_audio_file_path_sql="SELECT audioFilePathe, description  FROM xyz_audio_files_pathesM WHERE statueName like '$statueName' AND language like '$language';";
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
    $select_image_file_path_sql="SELECT imagePath  FROM xyz_staute_image_pathM WHERE statueName like '$statueName';";
    $result = mysqli_query($dbconnect, $select_image_file_path_sql);

    if(mysqli_num_rows($result) > 0){
        $row = $result->fetch_assoc();
       // echo $row['imagePath'];

        $temp['imagePath'] = $row['imagePath'];

     //   array_push($satue_info_array, $temp);
    }
    else{

        //echo "no";

        $temp2['imagePath'] = "no image avaliable";

       // array_push($satue_info_array, $temp2);

    }

    array_push($satue_info_array, $temp);

    echo json_encode(array("statue"=>$satue_info_array), JSON_UNESCAPED_UNICODE);






}else{
    echo "Error in query";
}

?>