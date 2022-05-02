package com.nightjar.tomcat.plugin.catalina.io.model;

import com.nightjar.tomcat.plugin.catalina.DBType;

import java.util.Map;

public class Context {

    private String versionName;
    private String path;
    private String workDir;
    private Map<DBType, Resource> resourceMap;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public Map<DBType, Resource> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<DBType, Resource> resourceMap) {
        this.resourceMap = resourceMap;
    }

}
