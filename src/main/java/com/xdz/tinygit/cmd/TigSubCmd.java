package com.xdz.tinygit.cmd;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.xdz.tinygit.model.CommitMeta;
import com.xdz.tinygit.storage.FileKVStorage;
import com.xdz.tinygit.storage.IKVStorage;
import com.xdz.tinygit.util.DiffUtil;
import com.xdz.tinygit.util.DigestUtil;
import com.xdz.tinygit.util.FileUtil;
import com.xdz.tinygit.util.SettingsUtil;
import picocli.CommandLine;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    // commit meta serialize

    private static String metaToStr(CommitMeta meta) {
        return JSONObject.toJSONString(meta);
    }

    private static CommitMeta strToMeta(String str) {
        return JSONObject.parseObject(str, CommitMeta.class);
    }

    // branch head pointer, default branchName is master

    private static String branchHeadKey(String branchName) {
        return SettingsUtil.getSettings("path.refs.heads") + "/" + branchName;
    }

    private static String loadBranchHead(String branchName) {
        return storage.load(branchHeadKey(branchName));
    }

    private static void storeBranchHead(String branchName, String sha1) {
        storage.store(branchHeadKey(branchName), sha1);
    }

    private static String defaultBranch() {
        return SettingsUtil.getSettings("path.default.branch");
    }

    private static String branchHeadKey() {
        return branchHeadKey(defaultBranch());
    }

    private static String loadBranchHead() {
        return storage.load(branchHeadKey());
    }

    private static void storeBranchHead(String sha1) {
        storage.store(branchHeadKey(), sha1);
    }

    // working copy head, pointer to one branch

    private static String workingCopyHeadKey() {
        return SettingsUtil.getSettings("path.HEAD");
    }

    private static String loadWorkingCopyHead() {
        return storage.load(workingCopyHeadKey());
    }

    private static void storeWorkingCopyHead(String sha1) {
        storage.store(workingCopyHeadKey(), sha1);
    }

    // logs db

    private static String metaKey(String sha1) {
        return SettingsUtil.getSettings("path.logs") + "/" + sha1;
    }

    private static CommitMeta loadMeta(String sha1) {
        return strToMeta(storage.load(metaKey(sha1)));
    }

    private static void storeMeta(CommitMeta meta) {
        storage.store(metaKey(meta.getSha1()), metaToStr(meta));
    }

    // objects db

    private static String objectKey(String sha1) {
        return SettingsUtil.getSettings("path.objects") + "/" + sha1;
    }

    private static String loadObject(String sha1) {
        return storage.load(objectKey(sha1));
    }

    private static void storeObject(String sha1, String content) {
        storage.store(objectKey(sha1), content);
    }

    // commit & checkout

    private static void commitToResp(String branch, CommitMeta meta, String content) {
        if (!isBranchFirstSha1(meta.getSha1())) {
            storeObject(meta.getSha1(), content);
        }
        storeMeta(meta);
        storeBranchHead(branch, meta.getSha1());
        // commit at the same branch, change branch head & working-copy head
        storeWorkingCopyHead(meta.getSha1());
    }

    private static void updateWorkingCopy(String sha1) {
        // branch first commit is init. no object
        if (!isBranchFirstSha1(sha1)) {
            String content = loadObject(sha1);
            storage.store(SettingsUtil.getSettings("path.file"), content);
        }
        storeWorkingCopyHead(sha1);
    }

    // branch

    private static List<String> listAllBranchNames() {
        return FileUtil.listAllDirectFile(SettingsUtil.getSettings("path.refs.heads"));
    }

    private static boolean isBranchExist(String branchName) {
        return listAllBranchNames().contains(branchName);
    }

    // key: branchName, value: head sha1
    private static BiMap<String, String> listBranchNameHeadMapping() {
        BiMap<String, String> biMap = HashBiMap.create();
        List<String> branchNames = listAllBranchNames();
        for (String branchName : branchNames) {
            biMap.put(branchName, loadBranchHead(branchName));
        }
        return biMap;
    }

    private static String queryBranchByHead(String sha1) {
        BiMap<String, String> biMap = listBranchNameHeadMapping();
        return biMap.inverse().get(sha1);
    }

    private static String queryWorkingCopyBranch() {
        return queryBranchByHead(loadWorkingCopyHead());
    }

    // check valid
    private static void checkBranchName(String branchName) {
        if (!isBranchExist(branchName)) {
            System.out.println("branch : " + branchName + " is not exist");
            System.exit(1);
        }
    }

    // start-points in all logs

    private static Map<String, CommitMeta> listAllCommitMetas() {
        Map<String, CommitMeta> map = new LinkedHashMap<>();
        for (String branchName : listAllBranchNames()) {
            CommitMeta head = loadMeta(loadBranchHead(branchName));
            while (head != null) {
                // we ignore init branch commit
                if (isBranchFirstSha1(head.getSha1())) {
                    break;
                }
                map.put(head.getSha1(), head);
                head = loadMeta(head.getParentSha1());
            }
        }
        return map;
    }

    private static boolean isSha1Exist(String sha1) {
        return listAllCommitMetas().containsKey(sha1);
    }

    private static void checkStartPoint(String startPoint) {
        if (!isSha1Exist(startPoint)) {
            System.out.println("start-point: " + startPoint + " is not exist");
            System.exit(1);
        }
    }

    private static String getRealStartPoint(String startPoint, String branchName) {
        if (startPoint == null && branchName == null) {
            System.out.println("please give one branchName or startPoint.");
            System.exit(1);
        }

        String realStartPoint = startPoint;
        if (branchName != null) {
            // high priority
            checkBranchName(branchName);
            realStartPoint = loadBranchHead(branchName);
        } else {
            checkStartPoint(startPoint);
        }
        return realStartPoint;
    }

    // branch-name mapping
    private static String generateBranchFirstSha1(String branchName) {
        return branchName + "-0";
    }

    // sha1 is valid commit sha1.
    private static boolean isBranchFirstSha1(String sha1) {
        return sha1.contains("-");
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
            String branch = defaultBranch();
            CommitMeta meta = createCommitMeta(generateBranchFirstSha1(branch), null, "init resp");
            commitToResp(branch, meta, null);
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
            String head = loadWorkingCopyHead();
            String branchName = queryBranchByHead(head);
            if (branchName == null) {
                System.out.println("working-copy is old, maybe others commit before you. try diff, merge cmd...");
                System.exit(1);
            }
            // fixed file
            String content = storage.load(SettingsUtil.getSettings("path.file"));
            CommitMeta meta = createCommitMeta(DigestUtil.sha1Hex(content), head, msg);
            commitToResp(branchName, meta, content);
            System.out.println(branchName + "\t" + meta.getSha1());
        }
    }

    // tig checkout <start-point> [-b <branch-name>]
    // i do not know how to let user know just one param (startPoint or branchName) is required.
    @CommandLine.Command(name = "checkout")
    public static class CheckoutCmd implements Runnable {
        @CommandLine.Parameters(paramLabel = "<start-point>", arity = "0..1", description = "checkout from which sha1")
        public String startPoint;
        @CommandLine.Option(names = {"-b", "--branch"}, paramLabel = "<branch-name>", arity = "0..1", description = "checkout from which branch head sha1")
        public String branchName;

        @Override
        public void run() {
            String realStartPoint = getRealStartPoint(startPoint, branchName);
            if (Objects.equals(realStartPoint, loadWorkingCopyHead())) {
                System.out.println("All files are up-to-date.");
                System.exit(1);
            }
            updateWorkingCopy(realStartPoint);
            System.out.println("checkout done. start-point: " + realStartPoint);
        }
    }

    // tig diff
    @CommandLine.Command(name = "diff")
    public static class DiffCmd implements Runnable {

        @CommandLine.Parameters(arity = "0..1", paramLabel = "<start-point>", description = "checkout from which sha1")
        public String startPoint;
        @CommandLine.Option(names = {"-b", "--branch"}, paramLabel = "<branch-name>", arity = "0..1", description = "checkout from which branch head sha1")
        public String branchName;

        @Override
        public void run() {
            System.out.println("diff method is called");
            String targetSha1 = getRealStartPoint(startPoint, branchName);
            String source = SettingsUtil.getSettings("path.file");
            String target = SettingsUtil.getSettings("path.objects") + "/" + targetSha1;
            String diffResult = DiffUtil.diff(source, target);
            System.out.println(diffResult);
        }
    }

    // git log
    @CommandLine.Command(name = "log")
    public static class LogCmd implements Runnable {

        @CommandLine.Option(names = {"-b", "--branch"}, paramLabel = "<branch-name>", arity = "0..1", description = "checkout from which branch head sha1")
        public String branchName;

        @Override
        public void run() {
            String realBranchName = branchName == null ? defaultBranch() : branchName;
            checkBranchName(realBranchName);

            CommitMeta head = loadMeta(loadBranchHead(realBranchName));
            System.out.println("sha1\tmsg\tauthor\ttime");
            while (head != null) {
                // new branch's first commit sha1 is "master-0", for sha1 can not contain '-'
                // master first commit parentSha1 is null
                // other branch first commit sha1 is "{branch-name}-0"
                // other branch first commit parentSha1 is the workingCopyBranch head when new branch create.
                if (isBranchFirstSha1(head.getSha1())) {
                    break;
                }
                System.out.println(head.toLog());
                head = loadMeta(head.getParentSha1());
            }
        }
    }

    // git branch
    @CommandLine.Command(name = "branch")
    public static class BranchCmd implements Runnable {

        @CommandLine.Option(names = {"-c", "--checkout"}, paramLabel = "<branch-name>", arity = "0..1", description = "new branch name, checkout from workingCopyBranch")
        public String newBranchName;

        @CommandLine.Option(names = {"-d", "--delete"}, paramLabel = "<branch-name>", arity = "0..1", description = "which exist branch to delete")
        public String deleteBranchName;

        // default: list
        // checkout -c
        // delete -d

        @Override
        public void run() {
            if (newBranchName == null && deleteBranchName == null) {
                // list file names of refs/heads. (file name is branch name)
                String workingCopyBranch = queryWorkingCopyBranch();
                for (String branch : listAllBranchNames()) {
                    String prefix = Objects.equals(workingCopyBranch, branch) ? "*" : "";
                    prefix += "\t";
                    System.out.println(prefix + branch);
                }
            } else if (newBranchName != null) {
                if (isBranchExist(newBranchName)) {
                    System.out.println("branch already exist.");
                    System.exit(1);
                }

                String oldWorkingCopyBranch = queryWorkingCopyBranch();

                // first create new branch and switch to it
                CommitMeta meta = createCommitMeta(generateBranchFirstSha1(newBranchName), loadWorkingCopyHead(), "create new branch: " + newBranchName);
                commitToResp(newBranchName, meta, null);
                // checkout from old branch
                updateWorkingCopy(loadBranchHead(oldWorkingCopyBranch));
                storeWorkingCopyHead(meta.getSha1());
                System.out.println(newBranchName + ":\t" + meta.getSha1());
            } else {
                checkBranchName(deleteBranchName);
                String workingCopyBranch = queryWorkingCopyBranch();
                if (Objects.equals(deleteBranchName, workingCopyBranch)) {
                    System.out.println("you are at branch: " + deleteBranchName + ". can not delete. you can checkout or switch to anther branch, and then delete this branch after that.");
                    System.exit(1);
                }

                // objects & logs
                CommitMeta head = loadMeta(loadBranchHead(deleteBranchName));
                while (head != null && !isBranchFirstSha1(head.getSha1())) {
                    storage.remove(metaKey(head.getSha1()));
                    storage.remove(objectKey(head.getSha1()));
                }
                if (head != null) {
                    storage.remove(metaKey(head.getSha1()));
                    storage.remove(objectKey(head.getSha1()));
                }
                // refs
                storage.remove(branchHeadKey(deleteBranchName));
                System.out.println("branch : " + deleteBranchName + " is deleted.");
            }
        }
    }

    @CommandLine.Command(name = "switch")
    public static class SwitchCmd implements Runnable {
        @CommandLine.Option(names = {"-b", "--branch"}, paramLabel = "<branch-name>", arity = "1", required = true, description = "which exist branch to switch to")
        public String branchName;

        @Override
        public void run() {
            System.out.println("switch cmd is called");
            checkBranchName(branchName);

            // switch to target branch
            String branchHead = loadBranchHead(branchName);
            storeWorkingCopyHead(branchHead);
            // todo what if now working copy is already have some changes?
            // todo what if now working copy is diff with branch now?
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
//        new LogCmd().run();
        BranchCmd branchCmd = new BranchCmd();
        branchCmd.newBranchName = "master";
        branchCmd.run();
    }
}
