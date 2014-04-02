package org.geogit.api;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusNode extends SimpleHRPlusNode{
				 
	 private ObjectId parentContainerId;
	
	 private List<ObjectId> layerIds = new ArrayList<ObjectId>();
	 
	 private HRPlusContainerNode child;
	 
	 
	 public HRPlusContainerNode getChild(){
		 return this.child;
	 }
	 
	 public void setChild(HRPlusContainerNode child){
		 this.child = child;
	 }
	 
	 public HRPlusNode(ObjectId layerId, Envelope bounds){
		 super();
		 this.layerIds.add(layerId);
		 this.setBounds(bounds);
	 }
	 
	 public HRPlusNode(List<ObjectId> layerIds, Envelope bounds){
		 super();
		 this.layerIds.addAll(layerIds);
		 this.setBounds(bounds);
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
	
	
	public ObjectId getParentContainerId(){
		return this.parentContainerId;
	}
	
	public Envelope getBounds(){
		Envelope env = new Envelope();
		this.expand(env);
		return env;
	}
	
	public void setBounds(Envelope env){
		this.bounds[0] = env.getMinX();
		this.bounds[1] = env.getMinY();
		this.bounds[2] = env.getMaxX();
		this.bounds[3] = env.getMaxY();
	}
	
	public void getOverlap(Envelope env){
		if(isLeaf()){
			expand(env);
		}
		
		this.child.getOverlap(env);
	}

}
