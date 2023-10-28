package org.example;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreshnessAnalyzer {

    private final String repoUrl;
    private final String cloneDirectoryPath;
    private final Map<String, RevCommit> latestCommit = new HashMap<>();

    public FreshnessAnalyzer(String repoUrl, String cloneDirectoryPath) {
        this.repoUrl = repoUrl;
        this.cloneDirectoryPath = cloneDirectoryPath;
    }

    public void cloneRepository() throws GitAPIException {
        System.out.println("Cloning repository...");
        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(cloneDirectoryPath))
                .call();
    }

    public void gatherLatestCommits() throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Git git = new Git(builder.readEnvironment().findGitDir(new File(cloneDirectoryPath)).build())) {
            for (RevCommit commit : git.log().all().call()) {
                RevCommit parent = commit.getParentCount() > 0 ? commit.getParent(0) : null;
                if (parent != null) {
                    gatherDiffsBetweenCommits(git, commit, parent);
                }
            }
        }
    }

    private void gatherDiffsBetweenCommits(Git git, RevCommit commit, RevCommit parent) throws IOException, GitAPIException {
        TreeWalk tw = new TreeWalk(git.getRepository());
        tw.addTree(commit.getTree());
        tw.addTree(parent.getTree());
        tw.setRecursive(true);

        List<DiffEntry> diffs = git.diff().setOldTree(new CanonicalTreeParser(null, tw.getObjectReader(), parent.getTree())).setNewTree(new CanonicalTreeParser(null, tw.getObjectReader(), commit.getTree())).call();
        for (DiffEntry diff : diffs) {
            if (!latestCommit.containsKey(diff.getNewPath())) {
                latestCommit.put(diff.getNewPath(), commit);
            }
        }
    }

    public double getOudatedRate() {
        int outdatedTestFilesCount = 0;
        int totalTestFilesCount = 0;

        for (String filePath : latestCommit.keySet()) {
            if (filePath.endsWith("Test.java")) {
                totalTestFilesCount++;
                String correspondingSrcFile = filePath.replace("Test.java", ".java");
                if (latestCommit.containsKey(correspondingSrcFile)) {
                    if (latestCommit.get(filePath).getCommitTime() < latestCommit.get(correspondingSrcFile).getCommitTime()) {
                        outdatedTestFilesCount++;
                    }
                }
            }
        }

        return (double) outdatedTestFilesCount / totalTestFilesCount;
    }
}
