package com.nightjar.tomcat.plugin.catalina.ui;

import com.nightjar.tomcat.plugin.catalina.Const;
import com.nightjar.tomcat.plugin.catalina.DBType;
import com.nightjar.tomcat.plugin.catalina.io.Generator;
import com.nightjar.util.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configurator {

    private static final Logger log = LoggerFactory.getLogger(Configurator.class);

    /* UI elements */
    private Display display;
    private Shell shell;

    private Combo combo;
    private Button log4jdbc;
    private Button mysql;
    private Button oracle;
    private Button sqlserver;
    private Button makefile;

//    private DirectoryDialog directoryDialog;
    private FileDialog fileDialog;

    private Generator generator;

    public Configurator() {
    }

    public Configurator(Generator generator) {
        this.generator = generator;
    }

    /**
     * Opens the main program.
     */
    private void open() {
        log.debug("open ...");

        // Create the window
        shell = new Shell(SWT.CLOSE | SWT.BORDER | SWT.TITLE);
        createComponents();
        shell.open();
        loadContents();
    }

    /**
     * Closes the main program.
     */
    private void close() {
    }

    /**
     * Construct the UI
     */
    private void createComponents() {
        log.debug("createComponents ...");

        shell.setText(Bundle.getResourceString(Const.WINDOW_TITLE, new Object[] { "" }));

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 5;
        shell.setLayout(gridLayout);

        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);
        createFileMenu(menu);
        createHelpMenu(menu);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        gridData.horizontalIndent = -15;
        createCombo(shell, gridData);

        gridData = new GridData(GridData.FILL);
        createCheckbox(shell, gridData);
        createRadio(shell);

        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 2;
        createButton(shell, gridData);

        shell.pack();
    }

    /**
     * Creates the File Menu.
     *
     */
    private void createFileMenu(Menu parent) {
        Menu menu = new Menu(parent);
        MenuItem header = new MenuItem(parent, SWT.CASCADE);
        header.setText(Bundle.getResourceString(Const.MENU_FILE_TEXT));
        header.setMenu(menu);

        MenuItem item;
        // Open
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Bundle.getResourceString(Const.MENU_FILE_OPEN_TEXT));
        item.setAccelerator(SWT.MOD1 + 'O');
        item.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> openFile()));

        // Exit
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Bundle.getResourceString(Const.MENU_FILE_EXIT_TEXT));
        item.setAccelerator(SWT.MOD1 + 'Q');
        item.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> exitApp()));
    }

    /**
     * Creates the Help Menu.
     *
     * @param parent the parent menu
     */
    private void createHelpMenu(Menu parent) {
        Menu menu = new Menu(parent);
        MenuItem header = new MenuItem(parent, SWT.CASCADE);
        header.setText(Bundle.getResourceString(Const.MENU_HELP_TEXT));
        header.setMenu(menu);

        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(Bundle.getResourceString(Const.MENU_HELP_ABOUT_TEXT));
        item.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> openAboutDialog()));
    }

    /**
     * Creates the combo view.
     *
     * @param shell
     * @param layoutData
     */
    private void createCombo(Shell shell, Object layoutData) {
        Label label = new Label(shell, SWT.LEFT);
        label.setText("Version: ");
        combo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(layoutData);
        combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> changeComboSelectedItem()));
    }

    /**
     * Creates the check box view.
     *
     * @param parent
     * @param layoutData
     */
    private void createCheckbox(Shell parent, Object layoutData) {
        log4jdbc = new Button(parent, SWT.CHECK);
        log4jdbc.setLayoutData(layoutData);
    }

    /**
     * Create the radio view.
     *
     * @param parent
     */
    private void createRadio(Shell parent) {
        mysql = new Button(parent, SWT.RADIO);
        mysql.setText(Bundle.getResourceString(Const.RADIO_MYSQL_TEXT));

        oracle = new Button(parent, SWT.RADIO);
        oracle.setText(Bundle.getResourceString(Const.RADIO_ORACLE_TEXT));

        sqlserver = new Button(parent, SWT.RADIO);
        sqlserver.setText(Bundle.getResourceString(Const.RADIO_SQLSERVER_TEXT));
    }

    /**
     * Creates the Button view.
     *
     * @param parent
     * @param layoutData
     */
    private void createButton(Shell parent, Object layoutData) {
        makefile = new Button(parent, SWT.PUSH);
        makefile.setText(MessageFormat.format("   {0}   ", Bundle.getResourceString(Const.BUTTON_CREATE_TEXT)));
        makefile.setLayoutData(layoutData);
        makefile.addMouseListener(MouseListener.mouseDownAdapter(e -> createConfigFile()));
    }

    /**
     * Creates the combo view
     */
    private void loadContents() {
        log.debug("loadContents ...");

        initComponents();

        if (generator == null) return;

        String[] versionNames = generator.loadConfig();
        for (int i = 0; i < versionNames.length; i++) {
            combo.add(versionNames[i]);
            combo.setData(String.valueOf(i+1), versionNames[i]);
        }
    }

    /**
     * Initial components
     */
    private void initComponents() {
        log.debug("loadContents ...");

        combo.add("");
        combo.setData("0", "");
        combo.select(0);
        log4jdbc.setEnabled(false);
        mysql.setEnabled(false);
        oracle.setEnabled(false);
        sqlserver.setEnabled(false);
        makefile.setEnabled(false);
    }

    public void startup() {
        log.debug("startup ..");
        display = new Display();
        open();
        Image image = createAppIcon();
        shell.setImage(image);

//        Tray tray = display.getSystemTray();
//        if (tray != null) {
//            TrayItem trayItem = new TrayItem(tray, SWT.NONE);
//            trayItem.setImage(image);
//        }

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        close();
        if (image != null) image.dispose();
        display.dispose();
    }

    /**
     * Create App Icon
     *
     * @return image file
     */
    private Image createAppIcon() {
        Image image = null;
        try (InputStream in = Configurator.class.getResourceAsStream("/tomcat_logo.png")) {
            image = new Image(display, in);
        } catch (IOException e) {
            log.error("Can not load image image ..", e);
        }
        return image;
    }

//    private void openFolder() {
//        log.debug("openFolder ..");
//        if (directoryDialog == null) {
//            directoryDialog = new DirectoryDialog(shell, SWT.OPEN);
//        }
//        String name = directoryDialog.open();
//        log.debug(name);
//    }

    private void openFile() {
        if (fileDialog == null) {
            fileDialog = new FileDialog(shell, SWT.OPEN);
        }

        fileDialog.setFilterExtensions(new String[] {"*.csv", "*.*"});
        String name = fileDialog.open();
        log.debug(name);
    }

    private void exitApp() {
        log.debug("exitApp ..");
        shell.close();
    }

    private void openAboutDialog() {
        log.debug("about menu item selected ..");
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(Bundle.getResourceString(Const.DIALOG_ABOUT_TITLE));
        messageBox.setMessage(Bundle.getResourceString(Const.DIALOG_ABOUT_DESCRIPTION, new Object[] { System.getProperty("os.name") }));
        messageBox.open();
    }

    private void changeComboSelectedItem() {
        log.debug("about menu item selected ..");
        int selectionIndex = combo.getSelectionIndex();
//        String key = String.valueOf(selectionIndex);
//        Object value = combo.getData(key);
//        log.debug(MessageFormat.format("{0} => {1}", key, value));

        if (selectionIndex > 0) {
            log4jdbc.setEnabled(true);
            mysql.setEnabled(true);
            oracle.setEnabled(true);
            sqlserver.setEnabled(true);
            makefile.setEnabled(true);

            log4jdbc.setSelection(false);
            mysql.setSelection(true);
            oracle.setSelection(false);
            sqlserver.setSelection(false);
        } else {
            log4jdbc.setEnabled(false);
            mysql.setEnabled(false);
            oracle.setEnabled(false);
            sqlserver.setEnabled(false);
            makefile.setEnabled(false);
        }
    }

    private void createConfigFile() {
        log.debug("makefile mouse down ..");

        if (generator == null) return;

        int index = combo.getSelectionIndex();
        String key = String.valueOf(index);
        String value = (String) combo.getData(key);

        DBType db = null;
        if (mysql.getSelection()) {
            db = DBType.MYSQL;
        } else if (oracle.getSelection()) {
            db = DBType.ORACLE;
        } else if (sqlserver.getSelection()) {
            db = DBType.SQLSERVER;
        }

        boolean logsql = log4jdbc.getSelection();

        int ret = generator.createConfig(value, db, logsql);
        if (ret != 0) {
            String message = "";
            switch (ret) {
                case Const.RET_CREATE_CONFIG_FAILED:
                    message = Bundle.getResourceString(Const.CREATE_CONFIG_FAILED);
                    break;
                case Const.RET_CHECK_CONFIG_FAILED:
                    message = Bundle.getResourceString(Const.CHECK_CONFIG_FAILED);
                    break;
            }
            MessageBox messageBox = new MessageBox(shell, SWT.OK);
            messageBox.setMessage(message);
            messageBox.open();
        }
    }

    public static void main(String[] args) {
        new Configurator().startup();
    }

}
