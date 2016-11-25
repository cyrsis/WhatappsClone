<?php

/**
 * Created by Abderrahim El imame.
 * Email : abderrahim.elimame@gmail.com
 * Date: 19/02/2016
 * Time: 00:03
 */
class Pagination
{
    // pages total
    public $pages;
    // the $_GET[] page
    public $limit;
    // how much one in the page
    public $urls;
    // numbe of result
    private $page;
    // limit for query
    private $per_page;
    // generation of the urls
    private $result_num;

    public function __construct($page, $result_num, $per_page, $url)
    {
        $this->per_page = $per_page;
        $this->result_num = $result_num;
        $this->pages = ceil($this->result_num / $this->per_page);
        if ($page > $this->pages && $page > 0)
            $this->page = 1;
        else
            $this->page = $page;
        $this->create_limit();
        $this->create_urls($url);
    }

    /**
     * Function to create limits
     */
    private function create_limit()
    {
        if ($this->page > $this->pages or empty($this->page)) {
            $limit = "0, $this->per_page";
        } else {
            $start = ($this->page - 1) * $this->per_page;
            $limit = "$start, $this->per_page";
        }
        $this->limit = $limit;
    }

    /**
     * Function to generate new urls
     * @param $url
     */
    private function create_urls($url)
    {
        $return = '';
        if ($this->result_num > $this->per_page) {
            $return .= '<div class="center"><ul class="pagination">';
            if ($this->pages >= 1) {
                $next = ($this->page - 1);
                $return .= ($next > 0 && $next < $this->pages) ? '<li><a href="' . str_ireplace('#i#', $next, $url) . '">&laquo;</a></li>' : '';
                for ($x = 1; $x <= $this->pages; $x++) {
                    $return .= ($this->page == $x) ? '<li class="active"><a  href="javascript:;">' . $x . '</a></li>' :
                        '<li><a href="' . str_ireplace('#i#', $x, $url) . '">' . $x . '</a></li>';
                }
                $prev = ($this->page + 1);
                $return .= ($prev <= $this->pages) ? '<li><a href="' . str_ireplace('#i#', $prev, $url) . '">&raquo;</a></li>' : '';
            }
            $return .= '</ul></div>';
        }
        $this->urls = $return;
    }
}

?>