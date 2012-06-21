package com.urbancode.uprovision.tasks.common;


abstract public class Task {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************    
    protected Context context;
    
    //----------------------------------------------------------------------------------------------
    protected Task() {
        this.context = null;
    }
    
    //----------------------------------------------------------------------------------------------
    protected Task(Context context) {
        this.context = context;
    }
    
    //----------------------------------------------------------------------------------------------
    protected Context getContext() {
        return context;
    }
    
    //----------------------------------------------------------------------------------------------
    abstract public void create() throws Exception;
    
    //----------------------------------------------------------------------------------------------
    abstract public void destroy() throws Exception;
}
