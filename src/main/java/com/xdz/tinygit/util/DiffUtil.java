package com.xdz.tinygit.util;

import difflib.Delta;
import difflib.DiffRow;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: diff content<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/18 18:16<br/>
 * Version: 1.0<br/>
 */
public class DiffUtil {

    private static final String INSERTION = "<span style=\"background-color: #45EA85\">${text}</span>";
    private static final String DELETION = "<span style=\"background-color: #FB504B\">${text}</span>";

    @SneakyThrows
    public static String diff(String path1, String path2) {
        Path p1 = Paths.get(path1);
        Path p2 = Paths.get(path2);
        List<String> original = Files.exists(p1) ? Files.readAllLines(p1) : new ArrayList<>();
        List<String> revised = Files.exists(p2) ? Files.readAllLines(p2) : new ArrayList<>();

        Patch<String> patch = DiffUtils.diff(original, revised);

//        for (Delta<String> delta : patch.getDeltas()) {
//            List<?> list = delta.getRevised().getLines();
//            for (Object object : list) {
//                System.out.println(object);
//            }
//        }

        DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();
        builder.showInlineDiffs(false);
        DiffRowGenerator generator = builder.build();

        StringBuilder left = new StringBuilder("</br>");
        StringBuilder right = new StringBuilder("</br>");
        for (Delta<String> delta : patch.getDeltas()) {
            List<DiffRow> generateDiffRows = generator.generateDiffRows(delta.getOriginal().getLines(), delta.getRevised().getLines());
            for (DiffRow row : generateDiffRows) {
                DiffRow.Tag tag = row.getTag();
                if (tag == DiffRow.Tag.INSERT) {
                    left.append("</br>").append(INSERTION.replace("${text}", "" + row.getNewLine() + "</br>"));
                } else if (tag == DiffRow.Tag.CHANGE) {
                    left.append(DELETION.replace("${text}", "" + row.getOldLine() + "</br>"));
                    right.append(INSERTION.replace("${text}", "" + row.getNewLine() + "</br>"));
                } else if (tag == DiffRow.Tag.DELETE) {
                    left.append(DELETION.replace("${text}", "" + row.getOldLine() + "</br>"));
                    right.append("</br>");
                } else if (tag == DiffRow.Tag.EQUAL) {
                    left.append(row.getOldLine()).append("</br>");
                    right.append(row.getNewLine()).append("</br>");
                } else {
                    throw new IllegalStateException("Unknown pattern tag: " + tag);
                }
            }
        }

        String template = FileUtil.read("diff-template.html");
        assert template != null;
        String output = template.replace("${left}", left.toString())
                .replace("${right}", right.toString());
        // Write file to disk.
        FileUtil.write("diff.html", output);
//        System.out.println("HTML diff generated: diff.html");
        return output;
    }

    public static void main(String[] args) {
        diff("file", ".tig/objects/bc58195f403c395b94001c9e70acd19cd9e6cee3");
    }
}
