package com.xdz.tinygit.cmd;

import picocli.CommandLine;

/**
 * Description: tiny-git command<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/16 23:53<br/>
 * Version: 1.0<br/>
 */
@CommandLine.Command(
        name = "mygit",
        mixinStandardHelpOptions = true,
        subcommands = {
                TigSubCmd.InitCmd.class,
                TigSubCmd.CommitCmd.class,
                TigSubCmd.CheckoutCmd.class,
                TigSubCmd.DiffCmd.class,
                TigSubCmd.LogCmd.class,
                TigSubCmd.BranchCmd.class,
                TigSubCmd.SwitchCmd.class,
                TigSubCmd.MergeCmd.class,
        }
)
public class TigCmd {

}
