package com.papermelody.model;

import java.io.Serializable;

/**
 * Created by HgS_1217_ on 2017/4/10.
 */

public abstract class Music implements Serializable {
    /**
     * 音乐类
     */

    abstract public String getFilename();

    abstract public String getPath();

    abstract public String getMusicName();
}
