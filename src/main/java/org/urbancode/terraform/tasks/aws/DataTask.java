package org.urbancode.terraform.tasks.aws;

import org.apache.log4j.Logger;

public class DataTask extends BootActionSubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(DataTask.class);

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    private String value;
    private String name;

    //----------------------------------------------------------------------------------------------
    public DataTask() {

    }

    //----------------------------------------------------------------------------------------------
    public void setValue(String value) {
        this.value = value;
    }

    //----------------------------------------------------------------------------------------------
    public void setName(String name) {
        this.name = name;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setCmds(String script) {
        this.script = script;

    }

    //----------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    //----------------------------------------------------------------------------------------------
    public String getValue() {
        return value;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    protected String getCmds() {
        return script;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void create() {
        script = name + "=" + value;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() {
        script = null;
    }

}
