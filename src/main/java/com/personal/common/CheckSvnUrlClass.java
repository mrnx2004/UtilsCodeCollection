package com.personal.common;

import com.alibaba.fastjson.JSONObject;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * 2018.11.11  by Mrnx
 */
public class CheckSvnUrlClass {
    /**
     * 这一段代码中可以学习到通过捕捉错误代码，可以输出友好的错误信息给用户
     *
     * @param svnUrl
     * @param svnUser
     * @param svnpassword
     */
    public void checkSvnUrl(String svnUrl, String svnUser, String svnpassword) {
        JSONObject result = new JSONObject();
        try {
            checkSvnUrlImplement(svnUrl, svnUser, svnpassword);
            System.out.println("连接成功");
            result.put("message", "SVN连接成功");
            result.put("process_code", true);
        } catch (SVNException ex) {
            String message = ex.getErrorMessage().getErrorCode().toString();
            if (message.startsWith("170001")) {
                System.out.println("用户名密码错误");
                result.put("message", "SVN用户名密码错误");
            } else {
                System.out.println("连接失败");
                result.put("message", "SVN连接失败");
            }
            result.put("process_code", false);
        }
    }

    /**
     * 这一段代码是核心代码，用于检测svn是否可以连接成功
     *
     * @param svnUrl
     * @param svnUser
     * @param svnPassword
     * @return 成功返回true
     * @throws SVNException 失败抛出异常
     */
    private boolean checkSvnUrlImplement(String svnUrl, String svnUser, String svnPassword) throws SVNException {
        DAVRepositoryFactory.setup();
        SVNRepository repository = SVNRepositoryFactory.create(
                SVNURL.parseURIEncoded(svnUrl));
        ISVNAuthenticationManager auth = SVNWCUtil.createDefaultAuthenticationManager(
                svnUser, svnPassword);
        repository.setAuthenticationManager(auth);
        SVNNodeKind nodeKind = repository.checkPath("", -1);
        if (nodeKind == SVNNodeKind.NONE) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "No project at URL");
            throw new SVNException(err);
        }
        return true;
    }
}
