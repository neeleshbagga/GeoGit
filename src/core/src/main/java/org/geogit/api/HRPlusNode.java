package org.geogit.api;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusNode extends SimpleHRPlusNode{
				 
	 private ObjectId parentId;
	
	 private List<ObjectId> layerIds = new ArrayList<ObjectId>();
	 
	 private HRPlusContainerNode child;
	 
	 
	 public HRPlusContainerNode getChild(){
		 return this.child;
	 }
	 
	 public HRPlusNode(ObjectId objectId){
		 super(objectId);
	 }
	 	 
	public List<ObjectId> getLayerIds() {
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
	}
	
	public boolean isLeaf(){
		return this.child == null;
	}
	
	
	public ObjectId getParentId(){
		return this.parentId;
	}
	
	public void getOverlap(Envelope env){
		if(isLeaf()){
			expand(env);
		}
		
		this.child.getOverlap(env);
	}

}
