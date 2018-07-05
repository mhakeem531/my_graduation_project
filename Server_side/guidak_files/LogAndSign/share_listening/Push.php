<?php 
 
class Push {
    //notification title
    private $audio_file_url;
 
    //notification message 
    private $image_file_url;

    private $statue_description;

 
    //initializing values in this constructor
    function __construct($audio_file_url, $image_file_url, $statue_description) {
         $this->audio_file_url = $audio_file_url;
         $this->image_file_url = $image_file_url; 
         $this->statue_description = $statue_description;

    }
    
    //getting the push notification
    public function getPush() {
        $res = array();
        $res['data']['audioFileUrl'] = $this->audio_file_url;
        $res['data']['imageFileUrl'] = $this->image_file_url;
        $res['data']['statue_description'] = $this->statue_description;
        return $res;
    }
 
}
?>