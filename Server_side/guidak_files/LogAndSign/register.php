<?php



require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if (isset($_POST['email']) 
    && isset($_POST['displayed_name']) 
    && isset($_POST['password']) 
    && isset($_POST['user_name'])
    && isset($_POST['token'])) {

    // receiving the post params
    $displayed_name = $_POST['displayed_name'];
    
    $email = $_POST['email'];
    
    $password = $_POST['password'];
    
    $user_name = $_POST['user_name'];

    $token = $_POST['token'];

    // check if user is already existed with the same email
    if ($db->isUserExisted($email)) {
        // user already existed
        $response["error"] = TRUE;
        
        $response["error_msg"] = "User already existed with " . $email;
        
        echo json_encode($response);
        
    } else {
    
        // create a new user
        $user = $db->storeUser($email, $user_name, $password, $displayed_name, $token);
       
        if ($user) {
        
            // user stored successfully
            $response["error"] = FALSE;
            $response["uid"] = $user["id"];
            $response["user"]["userName"] = $user["userName"];
            $response["user"]["displayedName"] = $user["displayedName"];
            $response["user"]["email"] = $user["email"];
            $response["user"]["created_at"] = $user["created_at"];
            $response["user"]["updated_at"] = $user["updated_at"];
            echo json_encode($response);
            
        } else {
        
            // user failed to store
            $response["error"] = TRUE;
            $response["error_msg"] = "Unknown error occurred in registration!";
            echo json_encode($response);
        }
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters (name, email or password) is missing!";
    echo json_encode($response);
}
?>

