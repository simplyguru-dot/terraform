package com.urbancode.uprovision.tasks.common;


public abstract class PostCreateSubTask extends SubTask {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    protected String script;
    
    //----------------------------------------------------------------------------------------------
    protected PostCreateSubTask() {
        super(null);
    }
    
    //----------------------------------------------------------------------------------------------
    protected PostCreateSubTask(Context context) {
        super(context);
    }
    
    //----------------------------------------------------------------------------------------------
    public abstract void setCmds(String script);
    
    //----------------------------------------------------------------------------------------------
    public abstract String getCmds();
    
    //----------------------------------------------------------------------------------------------
    @Override
    abstract public void create();

    //----------------------------------------------------------------------------------------------
    @Override
    abstract public void destroy();
}
