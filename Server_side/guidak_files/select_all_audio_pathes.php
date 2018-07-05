<?php
require "test_connection.php";
$select_audio_file_path_sql="select * from xyz_audio_files_pathesM;";
$result = mysqli_query($dbconnect, $select_audio_file_path_sql);

if(mysqli_num_rows($result) > 0){
    while($row = $result->fetch_assoc()){
          echo "<br>" . $row['id'] . " " . $row['statueName'] . "  " . $row['language'] . "  " . $row['audioFilePathe'];
    
    
    }
}
else{
   echo "no";
}
?>
