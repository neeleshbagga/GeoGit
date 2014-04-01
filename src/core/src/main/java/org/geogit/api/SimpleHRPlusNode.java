package org.geogit.api;

import com.vividsolutions.jts.geom.Envelope;

public class SimpleHRPlusNode{
	
	private ObjectId objectId;
	
	
	protected float[] bounds;
		
	
	public SimpleHRPlusNode(HRPlusNode node) {
		this.bounds = node.bounds;
		this.objectId = node.getObjectId();
	}
	
	public ObjectId getObjectId(){
		 return this.objectId;
	}
	
	
	public SimpleHRPlusNode(ObjectId objectId){
		this.objectId = objectId;
	}

	public double getMinX(){
		return this.bounds[0];
	}
	
	public double getMinY(){
		return this.bounds[1];
	}
	
	public double getMaxX(){
		return this.bounds[2];
	}
	
	public double getMaxY(){
		return this.bounds[3];
	}
		
	public void expand(Envelope env) {
        env.expandToInclude(bounds[0], bounds[1]);
        if (bounds.length > 2) {
            env.expandToInclude(bounds[2], bounds[3]);
        }
    } 
	
}
