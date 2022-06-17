package com.xdz.tinygit.cmd.model;

import picocli.CommandLine;

/**
 * Description: tiny-git command<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/16 23:53<br/>
 * Version: 1.0<br/>
 */
@CommandLine.Command(
        name = "tig",
        mixinStandardHelpOptions = true,
        subcommands = {
                TigSubCmd.InitCmd.class,
                TigSubCmd.CheckoutCmd.class,
                TigSubCmd.CommitCmd.class,
                TigSubCmd.DiffCmd.class,
                TigSubCmd.LogCmd.class,
                TigSubCmd.BranchCmd.class,
                TigSubCmd.MergeCmd.class
        }
)
public class TigCmd {

}
