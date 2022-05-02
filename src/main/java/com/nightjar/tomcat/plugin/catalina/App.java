package com.nightjar.tomcat.plugin.catalina;

import com.nightjar.tomcat.plugin.catalina.io.Generator;
import com.nightjar.tomcat.plugin.catalina.ui.Configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("application start ..");
        new Configurator(new Generator()).startup();
        log.info("application end ..");
    }
}
