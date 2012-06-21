package org.urbancode.terraform.tasks.aws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.urbancode.terraform.tasks.EnvironmentCreationException;
import org.urbancode.terraform.tasks.EnvironmentDestructionException;
import org.urbancode.terraform.tasks.aws.helpers.AWSHelper;
import org.urbancode.terraform.tasks.common.SubTask;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.RouteTableAssociation;

public class RouteTableTask extends SubTask {
    
    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private Logger log = Logger.getLogger(RouteTableTask.class);
    
    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    
    private AmazonEC2 ec2Client;
    private AWSHelper helper;
    private ContextAWS context;
    
    private String routeTableId;
    private String target;
    private String vpcId;
    private String subnetName;
    private String assId;
    
    private List<RouteTask> routes;
    
    private boolean isMainTable = false;
    
    //----------------------------------------------------------------------------------------------
    RouteTableTask(ContextAWS context) {
        this.context = context;
        helper = context.getAWSHelper();
        routes = new ArrayList<RouteTask>();
    }

    //----------------------------------------------------------------------------------------------
    public String getAssocId() {
        return assId;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getSubnetName() {
        return subnetName;
    }
    
    //----------------------------------------------------------------------------------------------
    public String getId() {
        return routeTableId;
    }
    
    //----------------------------------------------------------------------------------------------
    public List<RouteTask> getRoutes() {
        return Collections.unmodifiableList(routes);
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean getDefault() {
        return isMainTable;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setDefault(boolean isMainTable) {
        this.isMainTable = isMainTable;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setAssocId(String assocId) {
        this.assId = assocId;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setMainTable(boolean isMainTable) {
        this.isMainTable = isMainTable;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setRouteTarget(String target) {
        this.target = target;
    }
    
    //----------------------------------------------------------------------------------------------
    public void setId(String id) {
        this.routeTableId = id;
    }
    
    //----------------------------------------------------------------------------------------------
    public RouteTask createRoute() {
        RouteTask route = new RouteTask(context);
        routes.add(route);
        return route;
    }
    
    //----------------------------------------------------------------------------------------------
    public boolean existsInAws() {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        boolean result = false;
        
        List<String> id = new ArrayList<String>();
        id.add(getId());
        
        List<RouteTable> tables = helper.describeRouteTables(id, ec2Client);
        if (tables != null && !tables.isEmpty()) {
            result = true;
        }
        
        return result;
    }
    
    private String setupMainTable(boolean defaultRoute) {
        String result = null;
        
        // grab id of first (only) route table in vpc
        List<RouteTable> tables = helper.describeRouteTables(null, ec2Client);
        RouteTable foundTable;
        //assume one route table per vpc 
        
        for (RouteTable table : tables) {
            if (table.getVpcId().equals(vpcId)) {
                log.info("Found route table.");
                foundTable = table;
                routeTableId = table.getRouteTableId();
                break;
            }
        }
        
        if (defaultRoute) {
            // set default route
            RouteTask dRoute = new RouteTask(context);
            dRoute.setRouteTableId(routeTableId);
            dRoute.setTarget(target);
            dRoute.setDest("0.0.0.0/0");
            dRoute.create();
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void create() 
    throws Exception { 
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        
        try {
            log.info("Creating RouteTable...");
            
            if (isMainTable) {
                setupMainTable(true);
            }
            else {
                setId(helper.createRouteTable(vpcId, ec2Client));
            }
            log.info("RouteTableId created with routeTableId: " + routeTableId);
            
            // create any other routes
            for (RouteTask route : getRoutes()) {
                route.setRouteTableId(routeTableId);
                route.create();   
            }
        
            String subnetId = ((EnvironmentTaskAWS)context.getEnvironment()).getVpc().findSubnetForName(subnetName).getId();
            assId = helper.associateRouteTableWithSubnet(routeTableId, subnetId, ec2Client);
            
        } catch (Exception e) {
            log.error("RouteTable not created",e );
            throw new EnvironmentCreationException("Could not create RouteTable completely", e);
        }
        finally {
            ec2Client = null;
        }
    }
    
    //----------------------------------------------------------------------------------------------
    @Override
    public void destroy() throws Exception {
        if (ec2Client == null) {
            ec2Client = context.getEC2Client();
        }
        log.info("Destroying RouteTable...");
        
        try {
            // We need to check to see if the route table is associated with anything before disassociating
            String subnetId = ((EnvironmentTaskAWS)context.getEnvironment()).getVpc().findSubnetForName(subnetName).getId();
            List<String> id = new ArrayList<String>();
            id.add(routeTableId);
            List<RouteTable> table = helper.describeRouteTables(id, ec2Client);
            List<RouteTableAssociation> asses = table.get(0).getAssociations();
            if (asses != null && !asses.isEmpty()) {
                for (RouteTableAssociation ass : asses) {
                    if (subnetId.equals(ass.getSubnetId())) {
                        if (ass.getRouteTableAssociationId() != null && !ass.getRouteTableAssociationId().equals("")) {
                            helper.disassociateRouteTableFromSubnet(assId, ec2Client);
                        }
                    }
                }
            }
            
            if (!isMainTable) {
                helper.deleteRouteTable(routeTableId, ec2Client);
            }
            
            setAssocId(null);
            setId(null);
        }
        catch (Exception e) {
            log.error("Unable to destroy RouteTable completely", e);
            throw new EnvironmentDestructionException("Could not destroy RouteTable", e);
        }
        finally {
            ec2Client = null;
        }
    }
}