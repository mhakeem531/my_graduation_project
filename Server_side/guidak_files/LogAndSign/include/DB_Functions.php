<?php

class DB_Functions {

    private $conn;
 
    // constructor
    function __construct() {

        require_once 'DB_Connect.php';

        // connecting to database
        $db = new Db_Connect();

        $this->conn = $db->connect();
    }
 
    // destructor
    function __destruct() {
    }
 
    /**
     * Storing new user
     * returns user details
     */
    public function storeUser($email, $username, $password, $displayed_name, $token) {
    
        $uuid = uniqid('', true);
        
        $hash = $this->hashSSHA($password);
        
        $encrypted_password = $hash["encrypted"]; // encrypted password
        
        $salt = $hash["salt"]; // salt
 
    /** 
      $stmt = $this->conn->prepare(
                                     "INSERT INTO xyz_usersM
                                                           (unique_id, 
                                                            email, 
                                                            userName, 
                                                            password, 
                                                            salt, 
                                                            displayedName, 
                                                            created_at) 
                                      VALUES(? , ?, ?, ?, ?, ?, NOW());"
                                    );
      $stmt->bind_param("sssss", '$uuid', '$email', '$username', '$encrypted_password', '$salt', '$displayed_name');
      **********************************************/
                                    
                                    
        $stmt = $this->conn->prepare(
                                     "INSERT INTO xyz_usersM
                                                           (unique_id, 
                                                            email, 
                                                            userName, 
                                                            password, 
                                                            salt, 
                                                            displayedName, 
                                                            created_at,
                                                            token) 
                                      VALUES('$uuid' , '$email', '$username', '$encrypted_password', '$salt', '$displayed_name', NOW(), '$token');"
                                    );
        

        $result = $stmt->execute();

        $stmt->close();
 
        // check for successful store
        if ($result) {

            $stmt = $this->conn->prepare("SELECT * FROM xyz_usersM WHERE email = ?");

            $stmt->bind_param("s", $email);

            $stmt->execute();

            $user = $stmt->get_result()->fetch_assoc();

            $stmt->close();
 
            return $user;
            
        } else {
            return false;
        }
    }

    /**
     * Storing new user
     * returns user details
     */
    public function storeUserWithGoogle($email, $username, $displayed_name, $token, $google_plus_state) {
    
        $uuid = uniqid('', true);
                                    
                                    
        $stmt = $this->conn->prepare(
                                     "INSERT INTO xyz_usersM
                                                           (unique_id, 
                                                            email, 
                                                            googlePlusOrRegister,
                                                            userName, 
                                                            displayedName, 
                                                            created_at,
                                                            token) 
                                      VALUES('$uuid' , '$email', '$google_plus_state', '$username', '$displayed_name', NOW(), '$token');"
                                    );
        

        $result = $stmt->execute();

        $stmt->close();
 
        // check for successful store
        if ($result) {

          /*  $stmt = $this->conn->prepare("SELECT * FROM xyz_usersM WHERE email = ?");

            $stmt->bind_param("s", $email);

            $stmt->execute();

            $user = $stmt->get_result()->fetch_assoc();

            $stmt->close();
 
            return $user;*/
            return true;
            
        } else {
            return false;
        }
    }
 





















    /**
     * Get user by email and password
     */
    public function getUserByEmailAndPassword($email, $password) {
 
        $stmt = $this->conn->prepare("SELECT * FROM xyz_usersM WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        if ($stmt->execute()) {

            $user = $stmt->get_result()->fetch_assoc();

            $stmt->close();
 
            // verifying user password
            $salt = $user['salt'];
            
            $encrypted_password = $user['password'];
            
            $hash = $this->checkhashSSHA($salt, $password);
            
            // check for password equality
            if ($encrypted_password == $hash) {
            
                // user authentication details are correct
                return $user;
            }
        } else {
            return NULL;
        }
    }
 
    /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {
        $stmt = $this->conn->prepare("SELECT email from xyz_usersM WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        $stmt->execute();
 
        $stmt->store_result();
 
        if ($stmt->num_rows > 0) {
            // user existed 
            $stmt->close();
            return true;
        } else {
            // user not existed
            $stmt->close();
            return false;
        }
    }

     /**
     * if user user gmail in logging we will see if it used befor by registeration form or not
     */
    public function getGmailRegisterState($email) {
        $stmt = $this->conn->prepare("SELECT googlePlusOrRegister from xyz_usersM WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        $stmt->execute();
 
        $state = $stmt->get_result()->fetch_assoc();
 
        return $state['googlePlusOrRegister'];
        /*if ($stmt->num_rows > 0) {
            // user existed 
            $stmt->close();
            return true;
        } else {
            // user not existed
            $stmt->close();
            return false;
        }*/
    }

    public function getUsernameByMail($email){
        $stmt = $this->conn->prepare("SELECT userName from xyz_usersM WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        $stmt->execute();
 
        $state = $stmt->get_result()->fetch_assoc();
 
        return $state['userName'];
    }
 
    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {
 
        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }
 
    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {
 
        $hash = base64_encode(sha1($password . $salt, true) . $salt);
 
        return $hash;
    }



    //getting all tokens to send push to all devices
    public function getAllTokens($array_holds_group_usernames){
        $tokens = array(); 
        
        foreach($array_holds_group_usernames as $username){

            $stmt = $this->conn->prepare("SELECT token FROM xyz_usersM WHERE userName = ?");

            $stmt->bind_param("s", $username);

            $stmt->execute(); 

            $result = $stmt->get_result();

            $token = $result->fetch_assoc();

            array_push($tokens, $token['token']);
        }

        return $tokens; 
    }



     /**
     * Checks whether the email is valid or fake
     */

     public function validEmail($email){
    
        $isValid = true;
        $atIndex = strrpos($email, "@");
   
        if (is_bool($atIndex) && !$atIndex){
            $isValid = false;
        }
        else{
            $domain = substr($email, $atIndex+1);
            $local = substr($email, 0, $atIndex);
            $localLen = strlen($local);
            $domainLen = strlen($domain);
            if ($localLen < 1 || $localLen > 64){
                // local part length exceeded
                $isValid = false;
            }
            else if ($domainLen < 1 || $domainLen > 255){
                // domain part length exceeded
                $isValid = false;
            }else if ($local[0] == '.' || $local[$localLen-1] == '.'){
                // local part starts or ends with '.'
                $isValid = false;
       
            }else if (preg_match('/\\.\\./', $local)){
                // local part has two consecutive dots
                $isValid = false;
            }else if (!preg_match('/^[A-Za-z0-9\\-\\.]+$/', $domain)){
                // character not valid in domain part

                $isValid = false;
            }else if (preg_match('/\\.\\./', $domain)){
                // domain part has two consecutive dots
                $isValid = false;
            }else if(!preg_match('/^(\\\\.|[A-Za-z0-9!#%&`_=\\/$\'*+?^{}|~.-])+$/', str_replace("\\\\","",$local))){
                // character not valid in local part unless 
                // local part is quoted
                if (!preg_match('/^"(\\\\"|[^"])+"$/',str_replace("\\\\","",$local))){
                    $isValid = false;
                }
            }
            /**
              
               The checkdnsrr function looks up the dns record for a given type, and a given host. 
               This PHP function checks the DNS records for the given host to see if there are any records of the specified type.
               Note that the type parameter is optional, and if you don’t supply it then the type 
               defaults to "MX" (which means Mail Exchange). If any records are found, the function returns TRUE. 
               Otherwise, it returns FALSE.
               The second argument tells checkdnsrr what type of DNS record to look for. As we’re interested
               only in whether the given domain can handle email, we use the "MX" argument, which means 
               "look for the Mail Exchange record."
                  A : Address: Defined in RFC 1035.
                  ALL : Any of the valid types.
                  CNAME : Canonical Name: Defined in RFC 1035.
                  MX : Mail Exchanger: Defined in RFC 1035.
                  NS : Name Server: Defined in RFC 1035.
                  PTR : Pointer: Defined in RFC 1035.
                  SOA : Start of Authority: Defined in RFC 1035.
                
             */
            if ($isValid && !(checkdnsrr($domain,"MX") || checkdnsrr($domain,"A"))){         
                // domain not found in DNS
                $isValid = false;
            }
          }
        return $isValid;
    }


    public function getUserIDByEmail($email) {
 
        $stmt = $this->conn->prepare("SELECT id FROM xyz_usersM WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        if ($stmt->execute()) {

            $user = $stmt->get_result()->fetch_assoc();

            $stmt->close();
 
            return $user['id'];
        } else {
            return NULL;
        }
    }
 
}
 
?>
