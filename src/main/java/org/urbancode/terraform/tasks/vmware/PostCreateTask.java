
package org.urbancode.terraform.tasks.vmware;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.common.ExtensionTask;
import org.urbancode.terraform.tasks.vmware.util.VirtualHost;

import com.urbancode.commons.util.processes.Processes;
import com.vmware.vim25.mo.VirtualMachine;

public class PostCreateTask extends ExtensionTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final Logger log = Logger.getLogger(PostCreateTask.class);
    static protected final String confDir = System.getProperty("user.home") + File.separator +
            ".uprovision" + File.separator + "conf" + File.separator;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    protected EnvironmentTaskVmware environment;
    protected CloneTask cloneTask;
    protected VirtualMachine vmToConfig;

    // TODO hard-coded user and password
    protected String vmUser = "root";
    protected String vmPassword = "password";

    //----------------------------------------------------------------------------------------------
    public PostCreateTask() {
    }

    //----------------------------------------------------------------------------------------------
    public PostCreateTask(CloneTask cloneTask) {
        this.cloneTask = cloneTask;
        this.environment = cloneTask.fetchEnvironment();

        this.vmToConfig = cloneTask.fetchVm();
    }

    //----------------------------------------------------------------------------------------------
    public void setValues(CloneTask cloneTask) {
        this.cloneTask = cloneTask;
        this.environment = cloneTask.fetchEnvironment();

        this.vmToConfig = cloneTask.fetchVm();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(String user, String password, String vmRunCommand, String... args)
    throws IOException, InterruptedException {
        runCommand(user, password, vmRunCommand, Arrays.asList(args));
    }

    //----------------------------------------------------------------------------------------------
    public void runCommand(String vmUser, String vmPassword, String vmRunCommand, List<String> args)
    throws IOException, InterruptedException {
        VirtualHost host = environment.fetchVirtualHost();
        host.waitForVmtools(vmToConfig);
        String vmx = host.getVmxPath(vmToConfig);
        String url = host.getUrl();
        String virtualHostUser = host.getUser();
        String virtualHostPassword = host.getPassword();
        Processes processes = new Processes();
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("vmrun");
        commandLine.add("-T");
        commandLine.add("server");
        commandLine.add("-h");
        commandLine.add(url);
        commandLine.add("-u");
        commandLine.add(virtualHostUser);
        commandLine.add("-p");
        commandLine.add(virtualHostPassword);
        commandLine.add("-gu");
        commandLine.add(vmUser);
        commandLine.add("-gp");
        commandLine.add(vmPassword);
        commandLine.add(vmRunCommand);
        commandLine.add(vmx);
        commandLine.addAll(args);
        log.debug("running command " + vmRunCommand + " " + args.get(0));
        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        processes.discardOutput(process);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with code " + exitCode);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void copyFileFromGuestToHost(String origin, String destination)
    throws IOException, InterruptedException {
        List<String> args = new ArrayList<String>();

        //destination path is relative
        File temp = new File(destination);
        String absDestination = temp.getAbsolutePath();
        args.add(origin);
        args.add(absDestination);
        runCommand(vmUser, vmPassword, "copyFileFromGuestToHost", args);
    }

    //----------------------------------------------------------------------------------------------
    public void copyFileFromHostToGuest(String origin, String destination)
    throws IOException, InterruptedException {
        List<String> args = new ArrayList<String>();

        //destination path is relative
        File temp = new File(origin);
        String absOrigin = temp.getAbsolutePath();
        args.add(absOrigin);
        args.add(destination);
        runCommand(vmUser, vmPassword, "copyFileFromHostToGuest", args);
    }

    //----------------------------------------------------------------------------------------------
    public void writeToFile(String fname, String text, boolean append)
    throws IOException {
        FileWriter out = new FileWriter(fname, append);
        out.write(text);
        out.close();
    }

    //----------------------------------------------------------------------------------------------
    public String join(String[] strArray, String delimiter) {
        String result = "";
        int noOfItems = 0;
        for (String item : strArray) {
            result += item;
            if (++noOfItems < strArray.length) {
                result += delimiter;
            }
        }
        return result;
    }

}