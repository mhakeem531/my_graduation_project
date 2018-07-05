<?php
if(isset($_POST['statueName'],$_POST['language'])){
    $statueName=$_POST['statueName'];
    $language=$_POST['language'];
    include 'test_connection.php';
    $select_audio_file_path_sql="SELECT audioFilePathe FROM xyz_audio_files_pathesM WHERE statueName like '$statueName' AND language like '$language';";
    $result = mysqli_query($dbconnect, $select_audio_file_path_sql);

    if(mysqli_num_rows($result) > 0){
        $row = $result->fetch_assoc();
        echo $row['audioFilePathe'];
    }
    else{

        echo "no";
    }
}else{
    echo "Error in query";
}

?>
