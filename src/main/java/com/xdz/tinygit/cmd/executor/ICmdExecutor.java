package com.xdz.tinygit.cmd.executor;

/**
 * Description: command executor<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/17 13:38<br/>
 * Version: 1.0<br/>
 */
public interface ICmdExecutor {
    void execute(Object... params);

    default Object call(Object... params) {
        execute(params);
        return null;
    }
}
