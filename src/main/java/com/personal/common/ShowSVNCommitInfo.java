package com.personal.common;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.util.*;

public class ShowSVNCommitInfo {


    /***
     * 这里再提供一个算法，用于解析历史提交记录中所有的增删改，从而得到最终哪些文件被删除、哪些文件被
     */
    public void getFinalFile() throws Exception {
        List<SvnEntity> svnEntities = getSvnEntity("", "", "");
        List<String> deleteFileList = new ArrayList<>();
        List<String> analyzeFileList = new ArrayList<>();
        String projectName = ""; //项目名，用于截取svn仓库中的存储路径，保证路径的一致性
        String pathInProject = "";

        for (SvnEntity svnEntity : svnEntities) {
            int index = svnEntity.getPath().indexOf(projectName);
            if (index == -1) {
                pathInProject = projectName + svnEntity.getPath();
            } else {
                pathInProject = svnEntity.getPath().substring(index, svnEntity.getPath().length());
            }

            classifierFileType(deleteFileList, analyzeFileList, pathInProject, svnEntity);
        }
    }

    private void classifierFileType(List<String> deleteFileList, List<String> analyzeFileList, String pathInProject, SvnEntity svnEntity) {
        switch (svnEntity.getType()) {
            case 'A':
                if (deleteFileList.contains(pathInProject)) {
                    deleteFileList.remove(pathInProject);
                }
                analyzeFileList.add(pathInProject);
                break;
            case 'D':
                if (analyzeFileList.contains(pathInProject)) {
                    deleteFromAnalyzeList(pathInProject, analyzeFileList);
                }
                deleteFileList.add(pathInProject);
                break;
            case 'M':
                if (!analyzeFileList.contains(pathInProject)) {
                    analyzeFileList.add(pathInProject);
                }
                break;
            default:
                break;
        }
    }

    /***
     * 该方法保证再删除文件夹的时候，将其下面的子元素一并删除
     * 这里不能使用forEach，因为再循环的过程中报错，必须使用Iterator的remove()方法
     * @param path
     * @param analyzeFileList
     */
    private void deleteFromAnalyzeList(String path, List<String> analyzeFileList) {
        Iterator<String> iterator = analyzeFileList.iterator();
        while (iterator.hasNext()) {
            String analyzeFilePath = iterator.next();
            if (analyzeFilePath.startsWith(path)) {
                iterator.remove();
            }
        }
    }

    /***
     * 给出svn仓库的地址以及用户名密码，获取历史提交的信息
     * @param url
     * @param svnUserName
     * @param svnPassword
     * @return
     * @throws Exception
     */
    private List<SvnEntity> getSvnEntity(String url, String svnUserName, String svnPassword) throws Exception {
        long startRevision = 0;
        long endRevision = -1;
        int currentRevision = 0;//该数值用于表示当前版本号，只会显示当前版本号之后的svn提交信息

        SVNURL svnurl = SVNURL.parseURIEncoded(url);
        DAVRepositoryFactory.setup();
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnUserName, svnPassword);
        SVNRepository repos = SVNRepositoryFactory.create(svnurl);
        repos.setAuthenticationManager(authManager);
        Collection logEntries = null;
        List<SvnEntity> result;

        logEntries = repos.log(new String[]{""}, null, startRevision, endRevision, true, true);

        //得到历史提交记录
        result = printCommitInfo(currentRevision, logEntries);
        return result;
    }

    /***
     * 通过遍历SVNLogEntry【每一次的提交对象】中的changedPaths来确定每一次增删改的内容
     * @param currentRevision
     * @param logEntries
     */
    private List<SvnEntity> printCommitInfo(int currentRevision, Collection logEntries) {
        List<SvnEntity> result = new ArrayList<>();
        for (Iterator entryIterator = logEntries.iterator(); entryIterator.hasNext(); ) {
            SVNLogEntry logEntry = (SVNLogEntry) entryIterator.next();
            System.out.println("----------------");
            System.out.println("revision:" + logEntry.getRevision());

            if (logEntry.getRevision() <= currentRevision) {
                continue;
            }

            if (logEntry.getChangedPaths().size() > 0) {
                for (Map.Entry entry : logEntry.getChangedPaths().entrySet()) {
                    SVNLogEntryPath entryPath = (SVNLogEntryPath) entry.getValue();
                    System.out.println(" " + entryPath.getType()
                            + " "
                            + entryPath.getPath()
                            + ((entryPath.getCopyPath() != null) ? "(from "
                            + entryPath.getCopyPath()
                            + "revision "
                            + entryPath.getCopyRevision() + ")" : ""));
                    char type = entryPath.getType();
                    String path = entryPath.getPath();

                    SvnEntity entity = new SvnEntity(type, path, logEntry.getRevision());
                    result.add(entity);
                }
            }
        }
        return result;
    }

    class SvnEntity {
        private char type;
        private String path;
        private long revision;

        public SvnEntity(char type, String path, long revision) {
            this.type = type;
            this.path = path;
            this.revision = revision;
        }

        public char getType() {
            return type;
        }

        public void setType(char type) {
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public long getRevision() {
            return revision;
        }

        public void setRevision(long revision) {
            this.revision = revision;
        }
    }
}
