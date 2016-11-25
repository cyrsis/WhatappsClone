<?php

/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 19/02/2016
 * Time: 23:04
 */
class DataBase
{
    private $conn;
    public $DB_SERVER, $DB_USER, $DB_PASSWORD, $DB_NAME;
    public $DB_TABLE_PREFIX;

    // constructor to initialize  information
    function __construct($_Config)
    {
        $this->DB_SERVER = $_Config['DB_SERVER'];
        $this->DB_USER = $_Config['DB_USER'];
        $this->DB_PASSWORD = $_Config['DB_PASSWORD'];
        $this->DB_NAME = $_Config['DB_NAME'];
        $this->DB_TABLE_PREFIX = $_Config['DB_TABLE_PREFIX'];

    }

    /**
     * Connecting to mysql database
     */
    function connect()
    {
        $this->conn = @mysqli_connect($this->DB_SERVER, $this->DB_USER, $this->DB_PASSWORD);

        if (!$this->conn) {
            die('Can\'t Connect The Server');
        } else {
            mysqli_set_charset($this->conn, 'utf8');
        }

    }

    /**
     * Select database
     */
    function selectDB()
    {
        $select_db = @mysqli_select_db($this->conn, $this->DB_NAME);
        if (!$select_db) {
            die('(' . $this->DB_NAME . ') DataBase does not exist');
        }
    }

    function __destruct()
    {
        // closing db connection
        $this->close();
    }

    /**
     *  Function to close db connection
     * @return bool
     */
    function close()
    {
        // closing db connection
        $close = @mysqli_close($this->conn);
        return $close;
    }

    /**
     * Method to run SQL Queries
     * @param $SQLQuery
     * @return bool|mysqli_result
     */
    public function MySQL_Query($SQLQuery)
    {
        $SQLQuery = str_ireplace("prefix_", $this->DB_TABLE_PREFIX, $SQLQuery);
        $query = mysqli_query($this->conn, $SQLQuery);
        if (!$query)
            die($SQLQuery . '<br><br>' . mysqli_error($this->conn));
        else
            return $query;
    }

    /**
     * Method to fetch objects
     * @param $result
     * @return bool|null|object
     */
    function fetchObject($result)
    {
        if (!$Object = @mysqli_fetch_object($result)) {
            $this->ShowError();
            return false;
        } else {
            return $Object;
        }
    }
    /****************************
     * Method to run SQL queries
     ****************************/

    /**
     *  Checks for MySQL Errors
     * If error exists show it and return false
     * else return true
     */
    function ShowError()
    {
        $error = mysql_error();
        echo $error;
    }

    /**
     * Method to fetch values of array
     * @param $result
     * @return array|bool|null
     */
    function fetchArray($result)
    {
        if (!$array = @mysqli_fetch_array($result)) {
            $this->ShowError();
            return false;
        } else {
            return $array;
        }
    }

    /**
     * Method to fetch values of array assoc
     * @param $result
     * @return array|bool|null
     */
    function fetchAssoc($result)
    {
        if (!$array = @mysqli_fetch_assoc($result)) {
            $this->ShowError();
            return false;
        } else {
            return $array;
        }
    }


    /**
     *  Method to safely escape strings
     * @param $string
     * @return string
     */
    function escapeString($string)
    {
        if (get_magic_quotes_gpc()) {
            return $string;
        } else {
            $string = mysqli_real_escape_string($this->conn, $string);
            return $string;
        }
    }


    /**
     * Count rows of an array
     * @param $table
     * @param string $where
     * @return int
     */
    public function CountRows($table, $where = '')
    {
        if ($where != '') {
            $query = $this->select($table, '*', $where);
        } else {
            $query = $this->select($table, '`id`');
        }
        return $this->numRows($query);
    }

    /**
     * Method to number of rows
     * @param $result
     * @return int
     */
    function numRows($result)
    {
        $num = @mysqli_num_rows($result);
        return $num;
    }

    /**
     * free the result
     * @param $result
     * @return bool
     */
    public function free($result)
    {
        if (!@mysqli_free_result($result)) {
            $this->ShowError();
            return false;
        }
        return true;
    }



    /*******************************
     * Method for SQL Injection
     *********************************/
    /**
     * Function to select operation
     * @param $table
     * @param string $fields
     * @param string $where
     * @param string $orderby
     * @param string $limit
     * @return bool|mysqli_result
     */
    public function select($table, $fields = '*', $where = '', $orderby = '', $limit = '')
    {
        $query = "SELECT " . trim($fields) . " FROM `" . $this->DB_TABLE_PREFIX . "" . trim($table) . "`";
        if ($where != '') {
            $query .= " WHERE " . trim($where);
        }
        if ($orderby != '') {
            $query .= " ORDER BY " . trim($orderby);
        }
        if ($limit != '') {
            $query .= " LIMIT " . trim($limit);
        }
        $select = mysqli_query($this->conn, $query);
        return $select;
    }
    /**
     * Function to select distinct elements operation
     * @param $table
     * @param string $fields
     * @param string $where
     * @param string $orderby
     * @param string $limit
     * @return bool|mysqli_result
     */
    public function selectDistinct($table, $fields = '*', $where = '', $orderby = '', $limit = '')
    {
        $query = "SELECT  DISTINCT " . trim($fields) . " FROM `" . $this->DB_TABLE_PREFIX . "" . trim($table) . "`";
        if ($where != '') {
            $query .= " WHERE " . trim($where);
        }
        if ($orderby != '') {
            $query .= " ORDER BY " . trim($orderby);
        }
        if ($limit != '') {
            $query .= " LIMIT " . trim($limit);
        }
        $select = mysqli_query($this->conn, $query);
        return $select;
    }

    /**
     * Function to insert operation
     * @param $table
     * @param $dataArray
     * @return bool|mysqli_result
     */
    function insert($table, $dataArray)
    {
        if (is_array($dataArray)) {
            $fields = "`" . implode("`,`", array_keys($dataArray)) . "`";
            $values = implode("','", $dataArray);
            $query = "INSERT INTO `" . $this->DB_TABLE_PREFIX . $table . "` (" . $fields . ") VALUES ('" . $values . "')";
            return mysqli_query($this->conn, $query);
        } else {
            die($dataArray . ' is not an array');
        }
    }


    /**
     * Function to update operation
     * @param $table
     * @param $fields
     * @param $where
     * @return bool|mysqli_result
     */
    public function update($table, $fields, $where)
    {
        return mysqli_query($this->conn, "UPDATE `" . $this->DB_TABLE_PREFIX . $table . "` SET " . $fields . " WHERE " . $where . "");
    }

    /**
     * Function to update operation
     * @param $table
     * @param $where
     * @return bool|mysqli_result
     */

    public function delete($table, $where)
    {
        $delete = mysqli_query($this->conn, "DELETE FROM " . $this->DB_TABLE_PREFIX . $table . " WHERE " . $where . "");
        return $delete;
    }

    /**
     * get the last id inserted
     * @return int|string
     */
    public function last_Id()
    {
        return mysqli_insert_id($this->conn);
    }

}