package com.xdz.tinygit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Description: commit meta data<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/18 17:00<br/>
 * Version: 1.0<br/>
 */
@Data
@NoArgsConstructor
public class CommitMeta {
    /**
     * commit key & pointer to bd
     */
    private String sha1;
    /**
     * from which base commit then change to get content.
     */
    private String parentSha1;

    /**
     * commit msg
     */
    private String msg;

    /**
     * author name
     */
    private String author;
    /**
     * ms
     */
    private long ts;

    public String toLog() {
        return sha1 + "\t" + msg + "\t" + author + "\t" + SimpleDateFormat.getInstance().format(new Date(ts));
    }
}
