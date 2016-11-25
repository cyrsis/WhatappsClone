<?php
/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 19/02/2016
 * Time: 22:48
 */

class ProfileController {


  public $_GB;

  public function __construct($_GB)
  {
      $this->_GB = $_GB;
  }


  public  function uploadProfileImage($image,$userID){

      $fields = "`image` = '" . $image . "'";
      $result = $this->_GB->_DB->update('users', $fields, "`id` = '{$userID}' ");
      // check if row inserted or not
      if ($result) {
          $array = array(
              'success' => true,
              'message' => 'Image profile  is updated successfully '
          );
          $this->_GB->Json($array);
      } else {
          $array = array(
              'success' => false,
              'message' => 'Failed to update image profile '
          );
          $this->_GB->Json($array);
      }
    }
    public  function uploadProfileGroupImage($image,$groupID){
        $query = $this->_GB->_DB->select('groups', 'image', "`id` = '{$groupID}'");
        if ($this->_GB->_DB->numRows($query) != 0) {
            $fetch = $this->_GB->_DB->fetchAssoc($query);
            $imageFile = $this->_GB->_DB->select('images', '*', "`image_hash` = '{$fetch['image']}'");
            $fet = $this->_GB->_DB->fetchAssoc($imageFile);
            $path = 'uploads/' . $fet['image_path'] . '/' . $fet['image_new_name'];
            @unlink($path);
            $delete =   $this->_GB->_DB->delete('images', "`image_hash`= '{$fetch['image']}'");
            if ($delete){

              $fields = "`image` = '" . $image . "'";
                $result = $this->_GB->_DB->update('groups', $fields, "`id` = '{$groupID}' ");
                // check if row inserted or not
                if ($result){
                    $this->_GB->Json(array(
                        'success' => true,
                        'message' => 'group image has been updated successfully',
                        'groupID' => $groupID,
                        'groupImage' => $this->_GB->getSafeImage($image)));
                }else{
                    $this->_GB->Json(array('success' => false,
                        'message' => 'Oops Failed ? something went wrong',
                        'groupID' => null,
                        'groupImage' =>null));
               }
            }else{
                $this->_GB->Json(array('success' => false,
                    'message' => 'Oops Failed ? something went wrong',
                    'groupID' => null,
                    'groupImage' =>null));
            }
        }

    }
}
