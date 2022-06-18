package com.xdz.tinygit.cmd;

import com.xdz.tinygit.storage.FileKVStorage;
import com.xdz.tinygit.storage.IKVStorage;
import com.xdz.tinygit.util.DigestUtil;
import com.xdz.tinygit.util.FileUtil;
import com.xdz.tinygit.util.SettingsUtil;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;

/**
 * Description: tig sub-command<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/16 23:53<br/>
 * Version: 1.0<br/>
 */
public class TigSubCmd {
    private static final IKVStorage<String, String> storage = new FileKVStorage();

    private static void setMasterCommit(String sha1) {
        storage.store(SettingsUtil.getSettings("dir.master"), sha1);
    }

    private static String getMasterCommit() {
        return FileUtil.read(SettingsUtil.getSettings("dir.master"));
    }

    private static String sha1ToKey(String sha1) {
        return SettingsUtil.getSettings("dir.objects") + "/" + sha1;
    }

    private static String loadFromDB(String sha1) {
        return storage.load(sha1ToKey(sha1));
    }

    private static String storeToDB(String content) {
        String sha1 = DigestUtil.sha1Hex(content);
        storage.store(sha1ToKey(sha1), content);
        return sha1;
    }

    private static void updateWorkingCopy(String sha1) {
        String content = loadFromDB(sha1);
        storage.store(SettingsUtil.getSettings("v0.file"), content);
    }

    // tig init
    @CommandLine.Command(name = "init")
    public static class InitCmd implements Runnable {
        @Override
        public void run() {
            FileUtil.createDir(SettingsUtil.getSettings("dir.objects"));
            setMasterCommit("0");
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
            // fixed file
            String sha1 = storeToDB(storage.load(SettingsUtil.getSettings("v0.file")));
            setMasterCommit(sha1);
            System.out.println("commit sha1: " + sha1);
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
            String sha1 = getMasterCommit();
            updateWorkingCopy(sha1);
            System.out.println("checkout done. sha1: " + sha1);
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

    public static void main(String[] args){
//        new TigSubCmd.InitCmd().run();
//        new TigSubCmd.CommitCmd().run();
    }
}
