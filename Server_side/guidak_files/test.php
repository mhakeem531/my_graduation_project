<?php
if(isset($_GET['langCol'],$_GET['name'])){
    $langCol=$_GET['langCol'];
    $statueName=$_GET['name'];
    
    include 'test_connection.php';
    $select_audio_file_path_sql="SELECT $langCol FROM xyz_statue WHERE name like '$statueName';";

    $result = mysqli_query($dbconnect, $select_audio_file_path_sql);

    if(mysqli_num_rows($result) > 0){
        $row = $result->fetch_assoc();
        echo $row[$langCol];
    }
    else{

        echo "no";
    }
}else{
    echo "Error in query";
}

?>
