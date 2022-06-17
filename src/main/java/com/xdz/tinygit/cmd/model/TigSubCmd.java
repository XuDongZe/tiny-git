package com.xdz.tinygit.cmd.model;

import picocli.CommandLine;

/**
 * Description: tig sub-command<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/16 23:53<br/>
 * Version: 1.0<br/>
 */
public class TigSubCmd {
    // tig init
    @CommandLine.Command(name = "init")
    public static class InitCmd implements Runnable {
        @Override
        public void run() {
            System.out.println("init method will be called");
        }
    }

    // tig commit <msg>
    @CommandLine.Command(name = "commit")
    public static class CommitCmd implements Runnable {
        @CommandLine.Parameters(arity = "1", description = "custom message for this commit")
        public String msg;

        @Override
        public void run() {
            System.out.println("commit msg is :" + msg);
        }
    }

    // tig checkout <start-point> [-b <branch-name>]
    @CommandLine.Command(name = "checkout")
    public static class CheckoutCmd implements Runnable {
        @CommandLine.Parameters(arity = "1", paramLabel = "<start-point>", description = "indicate which commit to checkout from")
        public String startPoint;
        @CommandLine.Option(names = {"-b", "--branch"}, paramLabel = "<branch-name>", arity = "1")
        public String branchName;

        @Override
        public void run() {
            System.out.println("checkout startPoint: " + startPoint + " branchName: " + branchName);
        }
    }

    // tig diff
    @CommandLine.Command(name = "diff")
    public static class DiffCmd implements Runnable {

        @Override
        public void run() {
            System.out.println("diff method is called");
        }
    }

    // git log
    @CommandLine.Command(name = "log")
    public static class LogCmd implements Runnable {

        @Override
        public void run() {
            System.out.println("log method is called");
        }
    }

    // git branch
    @CommandLine.Command(name = "branch")
    public static class BranchCmd implements Runnable {

        @Override
        public void run() {
            System.out.println("branch is called");
        }
    }

    // git merge <branch-name>
    @CommandLine.Command(name = "merge")
    public static class MergeCmd implements Runnable {
        @CommandLine.Parameters(paramLabel = "<branch-name>", arity = "1", description = "merge to which branch")
        public String branchName;

        @Override
        public void run() {
            System.out.println("merge branchName: " + branchName);
        }
    }
}
