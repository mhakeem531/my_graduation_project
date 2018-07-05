<?php
class DB_Connect {
    
    private $conn;
 
    // Connecting to database
    public function connect() {

       //require_once 'login_try/include/Config.php';
        require_once 'Config.php';

        // Connecting to mysql database
        $this->conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE);

    /**
       if($this->conn->connect_error){
           die("Connection failed: ");
        }else{
            echo "Connection Success (local host)";
        }
    */
        // return database handler
        return $this->conn;
    }
}
 
?>
