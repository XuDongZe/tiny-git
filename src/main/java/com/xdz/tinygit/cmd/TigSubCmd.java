package com.xdz.tinygit.cmd;

import com.alibaba.fastjson.JSONObject;
import com.xdz.tinygit.model.CommitMeta;
import com.xdz.tinygit.storage.FileKVStorage;
import com.xdz.tinygit.storage.IKVStorage;
import com.xdz.tinygit.util.DiffUtil;
import com.xdz.tinygit.util.DigestUtil;
import com.xdz.tinygit.util.FileUtil;
import com.xdz.tinygit.util.SettingsUtil;
import picocli.CommandLine;

/**
 * Description: tig sub-command<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/16 23:53<br/>
 * Version: 1.0<br/>
 */
public class TigSubCmd {
    private static final IKVStorage<String, String> storage = new FileKVStorage();

    // commit meta default creator

    private static CommitMeta createCommitMeta(String sha1, String parentSha1, String msg) {
        CommitMeta meta = new CommitMeta();
        meta.setSha1(sha1);
        meta.setParentSha1(parentSha1);
        meta.setMsg(msg);
        meta.setAuthor(SettingsUtil.getSettings("author"));
        meta.setTs(System.currentTimeMillis());
        return meta;
    }

    private static CommitMeta createCommitMeta(String sha1, String msg) {
        return createCommitMeta(sha1, loadMasterHead(), msg);
    }

    // commit meta serialize

    private static String metaToStr(CommitMeta meta) {
        return JSONObject.toJSONString(meta);
    }

    private static CommitMeta strToMeta(String str) {
        return JSONObject.parseObject(str, CommitMeta.class);
    }

    // commit meta head pointer

    private static String masterHeadKey() {
        return SettingsUtil.getSettings("path.branch.master");
    }

    private static void storeHead(String sha1) {
        storage.store(masterHeadKey(), sha1);
    }

    private static String loadMasterHead() {
        return storage.load(masterHeadKey());
    }

    // logs db

    private static String sha1ToCommitMetaKey(String sha1) {
        return SettingsUtil.getSettings("path.logs") + "/" + sha1;
    }

    private static CommitMeta loadCommitMeta(String sha1) {
        return strToMeta(storage.load(sha1ToCommitMetaKey(sha1)));
    }

    private static void storeMeta(CommitMeta meta) {
        storage.store(sha1ToCommitMetaKey(meta.getSha1()), metaToStr(meta));
    }

    // objects db

    private static String sha1ToObjectKey(String sha1) {
        return SettingsUtil.getSettings("path.objects") + "/" + sha1;
    }

    private static String loadObject(String sha1) {
        return storage.load(sha1ToObjectKey(sha1));
    }

    private static void storeObject(String sha1, String content) {
        storage.store(sha1ToObjectKey(sha1), content);
    }

    // commit & checkout

    private static void commitToTigResp(CommitMeta meta, String content) {
        storeObject(meta.getSha1(), content);
        storeMeta(meta);
        storeHead(meta.getSha1());
    }

    private static void updateWorkingCopy(String sha1) {
        String content = loadObject(sha1);
        storage.store(SettingsUtil.getSettings("path.file"), content);
    }

    /***
     * sub command implement
     ***/

    // tig init
    @CommandLine.Command(name = "init")
    public static class InitCmd implements Runnable {
        @Override
        public void run() {
            String dir = SettingsUtil.getSettings("dir");
            if (FileUtil.exist(dir)) {
                System.out.println("already init: " + dir);
                System.exit(1);
            }
            FileUtil.createDir(SettingsUtil.getSettings("path.objects"));
            FileUtil.createDir(SettingsUtil.getSettings("path.logs"));

            CommitMeta meta = createCommitMeta("0", null, "init resp");
            storeMeta(meta);
            storeHead(meta.getSha1());
            System.out.println("init done");
        }
    }

    // tig commit <msg>
    @CommandLine.Command(name = "commit")
    public static class CommitCmd implements Runnable {
        @CommandLine.Parameters(arity = "1", description = "custom message for this commit")
        public String msg;

        @Override
        public void run() {
            // fixed file
            String content = storage.load(SettingsUtil.getSettings("path.file"));
            CommitMeta meta = createCommitMeta(DigestUtil.sha1Hex(content), msg);
            commitToTigResp(meta, content);
            System.out.println(meta.getSha1());
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
            String sha1 = loadMasterHead();
            updateWorkingCopy(sha1);
            System.out.println(sha1);
        }
    }

    // tig diff
    @CommandLine.Command(name = "diff")
    public static class DiffCmd implements Runnable {

        @Override
        public void run() {
            System.out.println("diff method is called");
            String source = SettingsUtil.getSettings("path.file");
            String target = SettingsUtil.getSettings("path.objects") + "/" + loadMasterHead();
            String diffResult = DiffUtil.diff(source, target);
            System.out.println(diffResult);
        }
    }

    // git log
    @CommandLine.Command(name = "log")
    public static class LogCmd implements Runnable {

        @Override
        public void run() {
            CommitMeta head = loadCommitMeta(loadMasterHead());
            System.out.println("sha1\tmsg\tauthor\ttime");
            while (head != null) {
                System.out.println(head.toLog());
                // so must init
                if (head.getParentSha1() == null) {
                    break;
                }
                head = loadCommitMeta(head.getParentSha1());
            }
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

    public static void main(String[] args) {
//        new TigSubCmd.InitCmd().run();
//        new TigSubCmd.CommitCmd().run();
        new LogCmd().run();
    }
}
