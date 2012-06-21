package com.urbancode.uprovision.xml;

public class XmlParsingException extends Exception {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    static private final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************

    //----------------------------------------------------------------------------------------------
    public XmlParsingException() {
        super();
    }

    //----------------------------------------------------------------------------------------------
    public XmlParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    //----------------------------------------------------------------------------------------------
    public XmlParsingException(String message) {
        super(message);
    }

    //----------------------------------------------------------------------------------------------
    public XmlParsingException(Throwable cause) {
        super(cause);
    }


}
