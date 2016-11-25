<?php

/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 20/02/2016
 * Time: 00:01
 */


ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

class Helper
{

    public $_DB;

    function __construct($_DB)
    {
        $this->_DB = $_DB;
    }


    /**
     * Check the json response message
     * @param $array
     */
    public function Json($array)
    {
        ob_clean();
        header('Content-Type: application/json; charset=utf-8');
        if (is_array($array)) {
            echo json_encode($array);
        } else {
            echo $array;
        }
    }


    /**
     * Function to get the date by days
     * @param $date
     * @return bool|string
     */
    public function Date($date)
    {
        //$date = strtotime($dt);

        $time_dd = date("d", $date);
        $time_MM = date("M", $date);
        $now = time();
        $c_dd = date("d", $now);
        $c_MM = date("M", $now);
        if ($time_MM == $c_MM) {
            if ($time_dd == $c_dd) {
                //days
                $newFormat = date('H:i', $date);
                return $newFormat;
            } else if ($time_dd == $c_dd - 1) {
                //yesterday
                $yesterday = 'Yesterday ';
                $newFormat = date('H:i', $date);
                return $yesterday . '' . $newFormat;
            } else if ($time_dd > $c_dd - 6 && $time_dd < $c_dd - 1) {
                //week
                $newFormat = date('l H:i', $date);
                return $newFormat;
            } else {
                //month
                $newFormat = date('D M H:i', $date);
                return $newFormat;
            }
        }
        //month
        $newFormat = date('D M Y', $date);
        return $newFormat;
    }

    /**
     * function to get a safe image
     * @param $Hash
     * @return null|string
     */
    public function getSafeImage($Hash)
    {
        $Hash = $this->_DB->escapeString($Hash);
        $query = $this->_DB->select('images', '*', "`image_hash` = '$Hash'");
        if ($this->_DB->numRows($query) != 0) {
            $fetch = $this->_DB->fetchAssoc($query);
            $path = 'uploads/imagesFiles/' . $fetch['image_path'] . '/' . $fetch['image_new_name'];
            return $path;
        } else {
            return null;
        }

    }


    /**
     * Function to uploads new images
     * @param $array
     * @param string $dir
     * @return null|string
     */
    public function uploadImage($array, $dir = './')
    {
        if (!empty($array)) {
            $tmp = $array["tmp_name"];
            $name = $array["name"];
            $new_name = md5(time() . '-' . $name) . '.jpg';
            $day_folder = date('d-m-y', time());
            if (is_dir($dir . 'uploads/imagesFiles/' . $day_folder)) {
                $path = $day_folder;
            } else {
                if (mkdir($dir . 'uploads/imagesFiles/' . $day_folder)) {
                    $path = $day_folder;
                } else {
                    $path = '';
                }
            }
            if (move_uploaded_file($tmp, $dir . 'uploads/imagesFiles/' . $path . '/' . $new_name)) {
                $imgHash = md5($tmp . $new_name . uniqid() . time());
                $imageData = array(
                    'image_original_name' => $this->_DB->escapeString($name),
                    'image_new_name' => $new_name,
                    'image_path' => $path,
                    'image_hash' => $imgHash,
                    'image_type' => 0
                );
                $query = $this->_DB->insert('images', $imageData);
                if ($query) {
                    return $imgHash;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Function to uploads new files audio
     * @param $array
     * @param string $dir
     * @return null|string
     */
    public function uploadAudio($array, $dir = './')
    {
        if (!empty($array)) {
            $tmp = $array["tmp_name"];
            $name = $array["name"];
            $new_name = md5(time() . '-' . $name) . '.mp3';
            $day_folder = date('d-m-y', time());
            if (is_dir($dir . 'uploads/audioFiles/' . $day_folder)) {
                $path = $day_folder;
            } else {
                if (mkdir($dir . 'uploads/audioFiles/' . $day_folder)) {
                    $path = $day_folder;
                } else {
                    $path = '';
                }
            }
            if (move_uploaded_file($tmp, $dir . 'uploads/audioFiles/' . $path . '/' . $new_name)) {
                $audioHash = md5($tmp . $new_name . uniqid() . time());
                $audioData = array(
                    'audio_original_name' => $this->_DB->escapeString($name),
                    'audio_new_name' => $new_name,
                    'audio_path' => $path,
                    'audio_hash' => $audioHash
                );
                $query = $this->_DB->insert('audios', $audioData);
                if ($query) {
                    return $audioHash;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * function to get audio file url
     * @param $Hash
     * @return null|string
     */
    public function getAudioFileUrl($Hash)
    {
        $Hash = $this->_DB->escapeString($Hash);
        $query = $this->_DB->select('audios', '*', "`audio_hash` = '$Hash'");
        if ($this->_DB->numRows($query) != 0) {
            $fetch = $this->_DB->fetchAssoc($query);
            $path = 'uploads/audioFiles/' . $fetch['audio_path'] . '/' . $fetch['audio_new_name'];
            return $path;
        } else {
            return null;
        }

    }

    /**
     * Function to uploads new  documents
     * @param $array
     * @param string $dir
     * @return null|string
     */
    public function uploadDocument($array, $dir = './')
    {
        if (!empty($array)) {
            $tmp = $array["tmp_name"];
            $name = $array["name"];
            $tempExtension = explode(".", $array["name"]);
            $extension = end($tempExtension);
            $new_name = md5(time() . '-' . $name) . "." . $extension;
            $day_folder = date('d-m-y', time());

            if (is_dir($dir . 'uploads/documentFiles/' . $day_folder)) {
                $path = $day_folder;
            } else {
                if (mkdir($dir . 'uploads/documentFiles/' . $day_folder)) {
                    $path = $day_folder;
                } else {
                    $path = '';
                }
            }
            if (move_uploaded_file($tmp, $dir . 'uploads/documentFiles/' . $path . '/' . $new_name)) {
                $documentHash = md5($tmp . $new_name . uniqid() . time());
                $documentData = array(
                    'document_original_name' => $this->_DB->escapeString($name),
                    'document_new_name' => $new_name,
                    'document_path' => $path,
                    'document_hash' => $documentHash
                );
                $query = $this->_DB->insert('documents', $documentData);
                if ($query) {
                    return $documentHash;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }


    /**
     * function to get document file url
     * @param $Hash
     * @return null|string
     */
    public function getDocumentFileUrl($Hash)
    {
        $Hash = $this->_DB->escapeString($Hash);
        $query = $this->_DB->select('documents', '*', "`document_hash` = '$Hash'");
        if ($this->_DB->numRows($query) != 0) {
            $fetch = $this->_DB->fetchAssoc($query);
            $path = 'uploads/documentFiles/' . $fetch['document_path'] . '/' . $fetch['document_new_name'];
            return $path;
        } else {
            return null;
        }

    }

    /**
     * Function to uploads new  videos
     * @param $array
     * @param string $dir
     * @return null|string
     */
    public function uploadVideo($array, $dir = './')
    {
        if (!empty($array)) {
            $tmp = $array["tmp_name"];
            $name = $array["name"];
            $tempExtension = explode(".", $array["name"]);
            $extension = end($tempExtension);
            $new_name = md5(time() . '-' . $name) . "." . $extension;
            $day_folder = date('d-m-y', time());

            if (is_dir($dir . 'uploads/videosFiles/videos/' . $day_folder)) {
                $path = $day_folder;
            } else {
                if (mkdir($dir . 'uploads/videosFiles/videos/' . $day_folder)) {
                    $path = $day_folder;
                } else {
                    $path = '';
                }
            }
            if (move_uploaded_file($tmp, $dir . 'uploads/videosFiles/videos/' . $path . '/' . $new_name)) {
                $videoHash = md5($tmp . $new_name . uniqid() . time());
                $videoData = array(
                    'video_original_name' => $this->_DB->escapeString($name),
                    'video_new_name' => $new_name,
                    'video_path' => $path,
                    'video_hash' => $videoHash
                );
                $query = $this->_DB->insert('videos', $videoData);
                if ($query) {
                    return $videoHash;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }


    /**
     * function to get video file url
     * @param $Hash
     * @return null|string
     */
    public function getVideoFileUrl($Hash)
    {
        $Hash = $this->_DB->escapeString($Hash);
        $query = $this->_DB->select('videos', '*', "`video_hash` = '$Hash'");
        if ($this->_DB->numRows($query) != 0) {
            $fetch = $this->_DB->fetchAssoc($query);
            $path = 'uploads/videosFiles/videos/' . $fetch['video_path'] . '/' . $fetch['video_new_name'];
            return $path;
        } else {
            return null;
        }

    }

    /**
     * Function to uploads new  documents
     * @param $array
     * @param string $dir
     * @return null|string
     */
    public function uploadVideoThumbnail($array, $dir = './')
    {
        if (!empty($array)) {
            $tmp = $array["tmp_name"];
            $name = $array["name"];
            $tempExtension = explode(".", $array["name"]);
            $extension = end($tempExtension);
            $new_name = md5(time() . '-' . $name) . "." . $extension;
            $day_folder = date('d-m-y', time());

            if (is_dir($dir . 'uploads/videosFiles/thumbnail/' . $day_folder)) {
                $path = $day_folder;
            } else {
                if (mkdir($dir . 'uploads/videosFiles/thumbnail/' . $day_folder)) {
                    $path = $day_folder;
                } else {
                    $path = '';
                }
            }

            if (move_uploaded_file($tmp, $dir . 'uploads/videosFiles/thumbnail/' . $path . '/' . $new_name)) {
                $videoThumbnailHash = md5($tmp . $new_name . uniqid() . time());
                $videoThumbnailData = array(
                    'image_original_name' => $this->_DB->escapeString($name),
                    'image_new_name' => $new_name,
                    'image_path' => $path,
                    'image_hash' => $videoThumbnailHash
                );
                $query = $this->_DB->insert('images', $videoThumbnailData);
                if ($query) {
                    return $videoThumbnailHash;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }


    /**
     * function to get document file url
     * @param $Hash
     * @return null|string
     */
    public function getVideoThumbnailFileUrl($Hash)
    {
        $Hash = $this->_DB->escapeString($Hash);
        $query = $this->_DB->select('images', '*', "`image_hash` = '$Hash'");
        if ($this->_DB->numRows($query) != 0) {
            $fetch = $this->_DB->fetchAssoc($query);
            $path = 'uploads/videosFiles/thumbnail/' . $fetch['image_path'] . '/' . $fetch['image_new_name'];
            return $path;
        } else {
            return null;
        }

    }




    /**********************************
     *       Method for Sessions
     *********************************/
    /**
     * Function to set session
     * @param $key
     * @param $value
     */
    function setSession($key, $value)
    {
        $_SESSION[$key] = $value;
    }

    /**
     * Function to get session
     * @param $key
     * @return bool
     */
    function getSession($key)
    {
        if (isset($_SESSION[$key])) {
            return $_SESSION[$key];
        } else {
            return false;
        }
    }

    /**
     * Function to unset session
     * @param $key
     * @return bool
     */
    function unsetSession($key)
    {
        if (isset($_SESSION[$key])) {
            unset($_SESSION[$key]);
        } else {
            return false;
        }
    }

    /**
     * Display Error Message
     * @param $messageError
     * @param string $error_type
     * @return string
     */
    function ErrorDisplay($messageError, $error_type = 'no')
    {
        switch ($error_type) {
            case 'no':
                $msg = '<div class="card-display-error  mdl-shadow--2dp "> <div class="mdl-card__title mdl-color-text--white"> ';
                $msg .= $messageError;
                $msg .= '</div></div>';
                return $msg;
                break;
            case 'yes':
                $msg = '<div class="card-display-error-success  mdl-shadow--2dp "> <div class="mdl-card__title mdl-color-text--white"> ';
                $msg .= $messageError;
                $msg .= '</div></div>';
                return $msg;
                break;
        }
    }

    /**
     * Function to refresh pages
     * @param $url
     * @param string $time
     * @return string
     */
    public function refreshPage($url, $time = '0')
    {
        return "<meta http-equiv=\"refresh\" content=\"$time;URL='$url'\" /> ";
    }

    /**
     * Function to get Settings
     * @param $name
     * @return mixed
     */
    public function getSettings($name)
    {
        $query = $this->_DB->select('settings', '`value`', "`name` = '{$name}'");
        $fetch = $this->_DB->fetchAssoc($query);
        return $fetch['value'];
    }

    /**
     * Function to update Config information
     * @param $name
     * @param $value
     */
    public function updateSettings($name, $value)
    {
        $value = $this->_DB->escapeString($value);
        $this->_DB->update('settings', "`value` = '{$value}'", "`name` = '{$name}'");
    }



    /**
     * Function to uploads new images
     * @param $array
     * @param string $dir
     * @return null|string
     */
    public function uploadAdminImage($array, $dir = '../')
    {
        if (!empty($array)) {
            $tmp = $array["tmp_name"];
            $name = $array["name"];
            $new_name = md5(time() . '-' . $name) . '.jpg';
            $day_folder = date('d-m-y', time());
            if (is_dir($dir . 'uploads/imagesFiles/' . $day_folder)) {
                $path = $day_folder;
            } else {
                if (mkdir($dir . 'uploads/imagesFiles/' . $day_folder)) {
                    $path = $day_folder;
                } else {
                    $path = '';
                }
            }
            if (move_uploaded_file($tmp, $dir . 'uploads/imagesFiles/' . $path . '/' . $new_name)) {
                $imgHash = md5($tmp . $new_name . uniqid() . time());
                $imageData = array(
                    'image_original_name' => $this->_DB->escapeString($name),
                    'image_new_name' => $new_name,
                    'image_path' => $path,
                    'image_hash' => $imgHash,
                    'image_type' => 0
                );
                $query = $this->_DB->insert('images', $imageData);
                if ($query) {
                    return $imgHash;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}