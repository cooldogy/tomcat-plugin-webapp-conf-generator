package com.nightjar.tomcat.plugin.catalina.io;

import com.nightjar.tomcat.plugin.catalina.Const;
import com.nightjar.tomcat.plugin.catalina.DBType;
import com.nightjar.tomcat.plugin.catalina.io.model.Context;
import com.nightjar.tomcat.plugin.catalina.io.model.Resource;
import com.nightjar.util.Bundle;
import com.nightjar.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {

    private static final Logger log = LoggerFactory.getLogger(Generator.class);

    private List<Context> contextList = new ArrayList<>();

    public String[] loadConfig() {
        log.debug("loadConfig ..");

        String filePath = Bundle.getResourceString(Const.CONFIG_DATA_PATH);
        return loadConfig(filePath);
    }

    public String[] loadConfig(String filePath) {
        log.debug("loadConfig with path ..");

        contextList.clear();
        List<String> lines = readFileByLine(filePath);
        for (String line : lines) {
            Context context = createContext(line);
            if (context != null) contextList.add(context);
        }

        return getVersionNames();
    }

    private List<String> readFileByLine(String filePath) {
        log.debug("readFileByLine ..");

        File file = new File(filePath);
        if (!file.exists()) {
            file = getResourceAsFile(filePath);
        }

        List<String> lineList = new ArrayList<>();
        if (file == null) return lineList;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (FileNotFoundException e) {
            log.error(MessageFormat.format("File({0}) can not be found ..", filePath), e);
        } catch (IOException e) {
            log.error(MessageFormat.format("File({0}) read error ..", filePath), e);
        }

        return lineList;
    }

    private File getResourceAsFile(String resourcePath) {
        try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath)) {

            if (in == null) {
                log.error(MessageFormat.format("File({0} can not be found ..)", resourcePath));
            }

            File tempFile = File.createTempFile(String.valueOf(in != null ? in.hashCode() : 0), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in != null ? in.read(buffer) : 0) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            return tempFile;
        } catch (IOException e) {
            log.error("Resource file can not found ..", e);
            return null;
        }
    }

    private String[] getVersionNames() {
//        Collections.sort(contextList, new Comparator<Context>() {
//            @Override
//            public int compare(Context lhs, Context rhs) {
//                return lhs.getVersionName().compareTo(rhs.getVersionName());
//            }
//        });
//        contextList.sort((lhs, rhs) -> lhs.getVersionName().compareTo(rhs.getVersionName()));
        contextList.sort(Comparator.comparing(Context::getVersionName));

        List<String> nameList = new ArrayList<>();
        for (Context context : contextList) {
            nameList.add(context.getVersionName());
        }

        return nameList.stream().toArray(String[]::new);
    }

    private Context createContext(String str) {
        log.debug("createContext ..");

        if (!checkConfigString(str)) {
            log.debug("Can not create context ...");
            return null;
        }

        String[] arr = str.split(",");

        Context context = new Context();
        context.setVersionName(arr[Const.VERSION_NAME_COLUMN]);
        context.setPath(arr[Const.PATH_COLUMN]);
        context.setWorkDir(arr[Const.WORK_DIR_COLUMN]);

        Map<DBType, Resource> resourceMap = new HashMap<>();

        Resource mysql = createResource(arr[Const.MYSQL_DEFAULT_VALUE_COLUMN],
                                        arr[Const.MYSQL_HOST_ADDR_COLUMN],
                                        arr[Const.MYSQL_PORT_COLUMN],
                                        arr[Const.MYSQL_DATABASE_NAME_COLUMN],
                                        arr[Const.MYSQL_USERNAME_COLUMN],
                                        arr[Const.MYSQL_PASSWORD_COLUMN],
                                        DBType.MYSQL);
        resourceMap.put(DBType.MYSQL, mysql);

        Resource oracle
                       = createResource(arr[Const.ORACLE_DEFAULT_SCHEMA_COLUMN],
                                        arr[Const.ORACLE_HOST_ADDR_COLUMN],
                                        arr[Const.ORACLE_PORT_COLUMN],
                                        arr[Const.ORACLE_DATABASE_NAME_COLUMN],
                                        arr[Const.ORACLE_USERNAME_COLUMN],
                                        arr[Const.ORACLE_PASSWORD_COLUMN],
                                        DBType.ORACLE);
        resourceMap.put(DBType.ORACLE, oracle);

        Resource sqlserver
                       = createResource(arr[Const.SQLSERVER_DEFAULT_SCHEMA_COLUMN],
                                        arr[Const.SQLSERVER_HOST_ADDR_COLUMN],
                                        arr[Const.SQLSERVER_PORT_COLUMN],
                                        arr[Const.SQLSERVER_DATABASE_NAME_COLUMN],
                                        arr[Const.SQLSERVER_USERNAME_COLUMN],
                                        arr[Const.SQLSERVER_PASSWORD_COLUMN],
                                        DBType.SQLSERVER);
        resourceMap.put(DBType.SQLSERVER, sqlserver);

        context.setResourceMap(resourceMap);

        return context;
    }

    private boolean checkConfigString(String str) {
        log.debug("checkConfigString ..");

        if (StringUtils.isEmpty(str)) {
            log.error("Data info is null or empty ...");
            return false;
        }

        String[] arr = str.split(",");
        if (arr.length != 21) {
            log.error("Data info is illegal ...");
            return false;
        }

        return true;
    }

    private Resource createResource(String defaultValue, 
            String host, String port, String databaseName,
            String username, String password, DBType dbType) {
        log.debug("createResource ..");

        Resource resource = new Resource();
        resource.setDefaultValue(defaultValue);
        resource.setHost(host);
        resource.setPort(port);
        resource.setDatabaseName(databaseName);
        resource.setUsername(username);
        resource.setPassword(password);
        resource.setDriverName(getAccessDriverName(dbType));
        resource.setJdbcUrl(createJdbcUrl(host, port, databaseName, dbType));

        return resource;
    }

    private String getAccessDriverName(DBType dbType) {
        log.debug("getAccessDriverName ..");

        String driver = null;

        switch (dbType) {
            case MYSQL:
                driver = Bundle.getResourceString(Const.DB_MYSQL_DRIVER);
                break;
            case ORACLE:
                driver = Bundle.getResourceString(Const.DB_ORACLE_DRIVER);
                break;
            case SQLSERVER:
                driver = Bundle.getResourceString(Const.DB_SQLSERVER_DRIVER);
                break;
        }

        if (driver == null) {
            log.error("DBType is illegal ...");
            return null;
        }

        return driver;
    }

    private String createJdbcUrl(String host, String _port, String schema, DBType dbType) {
        log.debug("createJdbcUrl ..");

        if (StringUtils.isEmpty(host) || StringUtils.isEmpty(schema)) {
            log.error("Access info is illegal ...");
            return null;
        }

        String port = null;
        String jdbcUrl = null;

        switch (dbType) {
            case MYSQL:
                port = Bundle.getResourceString(Const.DB_MYSQL_PORT);
                jdbcUrl = Bundle.getResourceString(Const.DB_MYSQL_URL);
                break;
            case ORACLE:
                port = Bundle.getResourceString(Const.DB_ORACLE_PORT);
                jdbcUrl = Bundle.getResourceString(Const.DB_ORACLE_URL);
                break;
            case SQLSERVER:
                port = Bundle.getResourceString(Const.DB_SQLSERVER_PORT);
                jdbcUrl = Bundle.getResourceString(Const.DB_SQLSERVER_URL);
                break;
        }

        if (StringUtils.isEmpty(port) || StringUtils.isEmpty(jdbcUrl)) {
            log.error("DBType is illegal ...");
            return null;
        }

        port = StringUtils.isEmpty(_port) ? port : _port;
        jdbcUrl = jdbcUrl.replace(Bundle.getResourceString(Const.URL_PATTERN_HOST), host)
                .replace(Bundle.getResourceString(Const.URL_PATTERN_PORT), port)
                .replace(Bundle.getResourceString(Const.URL_PATTERN_SCHEMA), schema);

        log.debug(MessageFormat.format("jdbcUrl is [{0}]", jdbcUrl));

        return jdbcUrl;
    }

    public int createConfig(String versionName, DBType dbType) {
        String dir = Bundle.getResourceString(Const.CONFIG_SETTING_DIR);
        return createConfig(versionName, dbType, false, dir);
    }

    public int createConfig(String versionName, DBType dbType, boolean logsql) {
        String dir = Bundle.getResourceString(Const.CONFIG_SETTING_DIR);
        return createConfig(versionName, dbType, logsql, dir);
    }

    public int createConfig(String versionName, DBType dbType, String dir) {
        return createConfig(versionName, dbType, false, dir);
    }

    public int createConfig(String versionName, DBType dbType, boolean logsql, String dir) {
        log.debug("createConfig ..");

        Context context = getContextByVersionName(versionName);
        if (context == null) {
            log.error("Config info can not be found ..");
            return Const.RET_CHECK_CONFIG_FAILED;
        }

        if (!checkResource(context.getResourceMap().get(dbType))) {
            log.info("Access Info is illegal ...");
            return Const.RET_CHECK_CONFIG_FAILED;
        }

        String confdir = makeConfigDir(dir);
        if (!clearConfigDir(confdir)) {
            log.error("Directory clear error ..");
        }

        String filename = makeConfigFileName(context.getVersionName());
        File file = new File(confdir, filename);

        if (file.exists()) {
            log.error(MessageFormat.format("File({0}) could not be created ..", file.getAbsolutePath()));
            return Const.RET_CREATE_CONFIG_FAILED;
        }

        log.info(MessageFormat.format("File({0}) will be created ..", file.getAbsolutePath()));

        String filepath = Bundle.getResourceString(Const.CONFIG_TEMPL_PATH);
//        try (BufferedReader br = new BufferedReader((new FileReader(filepath))); BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                bw.write(makeConfigInfo(line, context, db, logsql));
//                bw.newLine();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        StringBuilder sb = new StringBuilder();
        List<String> lineList = readFileByLine(filepath);
        for (String line : lineList) {
            sb.append(makeConfigInfo(line, context, dbType, logsql)).append(System.lineSeparator());
        }
        writeFile(file, sb.toString());
        
        return Const.RET_CREATE_CONFIG_SUCCESS;
    }

    private Context getContextByVersionName(String versionName) {
        log.debug("getContextByVersionName ..");

        for (Context context : contextList) {
            if (context.getVersionName().equals(versionName)) {
                return context;
            }
        }

        return null;
    }

    private boolean checkResource(Resource resource) {
        log.debug("checkResource ..");

        return !StringUtils.isEmpty(resource.getDriverName())
                && !StringUtils.isEmpty(resource.getJdbcUrl())
                && !StringUtils.isEmpty(resource.getUsername());
    }

    private String makeConfigDir(String dir) {
        log.debug("checkResource ..");

        File file = new File(dir);
        if (file.exists())
            return dir;

        file = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("")).getPath());
        return file.getAbsolutePath();
    }

    private boolean clearConfigDir(String path) {
        log.debug("clearConfigDir ..");

        File folder = new File(path);
//        File[] files = folder.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                return name.endsWith(Const.CONF_FILE_NAME_SUFFIX);
//            }
//        });
        File[] files = folder.listFiles((dir, name) ->
            !name.endsWith(Bundle.getResourceString(Const.CONFIG_TEMPL_PATH)) && name.endsWith(Const.CONF_FILE_NAME_SUFFIX)
        );
        for (File file : Objects.requireNonNull(files)) {
            log.info(MessageFormat.format("File({0}) will be removed ..", file.getAbsolutePath()));

            String backupFileName = makeBackupFileName(file.getName());
            File backupFile = new File(folder, backupFileName);
            if (backupFile.exists()) {
                log.info(MessageFormat.format("File({0}) exists ..", backupFile.getAbsolutePath()));
                if (!backupFile.delete()) {
                    log.error(MessageFormat.format("File({0}) can not be deleted ..", backupFile.getAbsolutePath()));
                    return false;
                }
                log.info(MessageFormat.format("File({0}) is deleted ..", backupFile.getAbsoluteFile()));
            }

            if (!file.renameTo(new File(folder, backupFileName))) {
                log.error(MessageFormat.format("File({0}) failed to move ..", backupFile.getAbsolutePath()));
                return false;
            }
        }

        return  true;
    }

    private String makeBackupFileName(String fileName) {
        log.debug("makeBackupFileName ..");
        return fileName + Const.BAK_FILE_NAME_SUFFIX;
    }

    private String makeConfigFileName(String versionName) {
        log.debug("makeConfigFileName ..");
        return versionName + Const.CONF_FILE_NAME_SUFFIX;
    }

    private String makeConfigInfo(String line, Context context, DBType dbType, boolean logsql) {
//        log.debug("makeConfigInfo ..");
        String str = "";
        String driver = context.getResourceMap().get(dbType).getDriverName();
        String jdbcUrl = context.getResourceMap().get(dbType).getJdbcUrl();

        try {
            if (logsql) {
                driver = Bundle.getResourceString(Const.DB_LOG4JDBC_DRIVER);
                jdbcUrl = jdbcUrl.replace(Const.DB_URL_JDBC, Const.DB_URL_LOG4JDBC);
            }
            str = line.replaceAll(StringUtils.escapeMetaCharacters(
                        Bundle.getResourceString(Const.TEMPL_PATTERN_PATH)), 
                        context.getPath())
                    .replaceAll(StringUtils.escapeMetaCharacters(
                        Bundle.getResourceString(Const.TEMPL_PATTERN_WORK_DIR)),
                        context.getWorkDir())
                    .replaceAll(StringUtils.escapeMetaCharacters(
                        Bundle.getResourceString(Const.TEMPL_PATTERN_PARM)),
                        context.getResourceMap().get(dbType).getDefaultValue())
                    .replaceAll(StringUtils.escapeMetaCharacters(
                        Bundle.getResourceString(Const.TEMPL_PATTERN_USERNAME)),
                        context.getResourceMap().get(dbType).getUsername())
                    .replaceAll(StringUtils.escapeMetaCharacters(
                        Bundle.getResourceString(Const.TEMPL_PATTERN_PASSWORD)),
                        context.getResourceMap().get(dbType).getPassword())
                    .replaceAll(StringUtils.escapeMetaCharacters(
                        Bundle.getResourceString(Const.TEMPL_PATTERN_DRIVER_NAME)),
                        driver)
                    .replaceAll(StringUtils.escapeMetaCharacters(
                        Bundle.getResourceString(Const.TEMPL_PATTERN_URL)),
                        jdbcUrl);
        } catch (Exception e) {
            log.error("Failed to make this line content ..", e);
        }

        return str;
    }

    private void writeFile(File file, String str) {
        log.debug("writeFile ..");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(str);
        } catch (IOException e) {
            log.error("File create failed ..", e);
        }

    }

}
