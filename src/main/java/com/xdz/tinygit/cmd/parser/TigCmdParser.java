package com.xdz.tinygit.cmd.parser;

import com.xdz.tinygit.cmd.TigCmd;
import picocli.CommandLine;

/**
 * Description: tig cmd<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/16 23:55<br/>
 * Version: 1.0<br/>
 */
public class TigCmdParser {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new TigCmd()).execute(args);
        System.exit(exitCode);
    }
}
