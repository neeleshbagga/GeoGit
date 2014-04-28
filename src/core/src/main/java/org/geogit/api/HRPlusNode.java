package org.geogit.api;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusNode /*implements RevObject*/ {
    // TODO @field objectId is never set
    private ObjectId objectId;
    // TODO @field parentContainerId is never set
    private ObjectId parentContainerId;

    private ObjectId versionId;
    
    // Bounds are either 2 values, for one-dimensional envelopes,
    // or 4 values, for two-dimensional envelopes
    protected Envelope bounds;
    private HRPlusContainerNode child;
   
    // TODO why did she use a list? do we even need 1 layerId?
    //private List<ObjectId> layerIds = new ArrayList<ObjectId>();
    
    
    public HRPlusNode(ObjectId objectId, Envelope bounds, ObjectId versionId) {
      //  this.layerIds.add(layerId);
        this.bounds = bounds;
        this.objectId = objectId;  
        this.versionId = versionId;
    }

   /* public HRPlusNode(List<ObjectId> layerIds, Envelope bounds) {
        this.layerIds.addAll(layerIds);
        this.setBounds(bounds);
    }*/

    public ObjectId getObjectId(){
        return this.objectId;
    }

    public double getMinX(){
        return this.bounds.getMinX();
    }

    public double getMinY(){
        return this.bounds.getMinY();
    }

    public double getMaxX(){
        return this.bounds.getMaxX();
    }

    public double getMaxY(){
        return this.bounds.getMaxY();
    }

    /**
     * Increase @param env to include the bounds of this node.
     * @param env
     */
    public void expand(Envelope env) {
        env.expandToInclude(this.getMinX(), this.getMinY());
        env.expandToInclude(this.getMaxX(), this.getMaxY());
    } 

    public HRPlusContainerNode getChild(){
        return this.child;
    }

    public void setChild(HRPlusContainerNode child){
        this.child = child;
    }

    /*public List<ObjectId> getLayerIds() {
        return this.layerIds;
    }

    public void addLayerId(ObjectId layerId){
        this.layerIds.add(layerId);
    }

    public ObjectId getFirstLayerId(){
        if(this.layerIds == null){
            return null;
        }
        return this.layerIds.get(0);
    }*/

    public boolean isLeaf(){
        return this.child == null;
    }

    public ObjectId getParentContainerId(){
        return this.parentContainerId;
    }

    /**
     * @return an envelope with bounds identical to the bounds of this node
     */
    public Envelope getBounds(){
        return new Envelope(this.getMinX(), this.getMaxX(), this.getMinY(), this.getMaxY());
    }

    
    public void setBounds(Envelope env){
       this.bounds = env;
    }

    /**
     * @param env
     * @return the envelope obtained by intersecting 
     */
    public Envelope getOverlap(Envelope env){
        if(isLeaf()){
            return this.getBounds().intersection(env);
        } else {
            // 2014-04-17: Not certain why we move to the child. Why not use this node's bounds?
            return this.child.getOverlap(env);
        }
    }
    
    /**
     * Bounding box query. If this node fits in container, add it to list 
     * and recurse on its child (if it has a child). Else end search.
     * @param env
     * @param matches
     */
    public void query(Envelope env, List<HRPlusNode> matches) {
        if (this.getBounds().intersects(env)) {
            // A match!
            matches.add(this);
        }
        
        if (!this.isLeaf()) {
            // Not a leaf, continue searching child
            this.getChild().query(env, matches);
        }
        return;
    }

  /*  @Override
    public TYPE getType() {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public ObjectId getId() {
		// TODO Auto-generated method stub
		return null;
	}*/
    @Override
public boolean equals(Object node){
    	if(node==null)
    		return false;
    	if (this.getClass() != node.getClass())
    	   {
    	      return false;
    	   }
    	
	    if(this.objectId != ((HRPlusNode)node).objectId)
	    		return false;
	    if(this.versionId != ((HRPlusNode)node).versionId)
    		return false;
	    if(this.bounds != ((HRPlusNode)node).bounds)
    		return false;
 
	        return true;
	    
	}

}
