package org.geogit.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusContainerNode {
	
	private Map<ObjectId, HRPlusNode> nodeMap = new HashMap<ObjectId, HRPlusNode>();
	ObjectId objectId;
	ObjectId parentId;
	
	public ObjectId getObjectId(){
		return this.objectId;
	}
	
	public ObjectId getParentId(){
		return this.parentId;
	}
	
	public int getNumNodes(){
		return this.nodeMap.size();
	}
	
	public void addNode(HRPlusNode node){
		nodeMap.put(node.getObjectId(), node);
	}
	
	public HRPlusNode removeNode(ObjectId objectId){
		return nodeMap.remove(objectId);
	}
	
	public List<HRPlusNode> getNodes(){
		return new ArrayList<HRPlusNode>(this.nodeMap.values());
	}
	
	public List<SimpleHRPlusNode> getSimpleNodes(){
		List<SimpleHRPlusNode> simpleNodes = new ArrayList<SimpleHRPlusNode>();
		for(HRPlusNode node : this.nodeMap.values()){
			simpleNodes.add(new SimpleHRPlusNode(node));
		}
		return simpleNodes;
	}
	
	public boolean allNodesContainLayerId(ObjectId layerId){
		
		//if all entries are from the current layerId
		for(HRPlusNode node : this.nodeMap.values()){
			if(node.getLayerIds() == null || !node.getLayerIds().contains(layerId)){
				return false;
			}
		}
		
		return true;
		
	}
	
	public List<HRPlusNode> getNodesForLayer(ObjectId layerId){
		List<HRPlusNode> nodesForLayer = new ArrayList<HRPlusNode>();
		for(HRPlusNode node : this.nodeMap.values()){
			if(node.getLayerIds().contains(layerId)){
				nodesForLayer.add(node);
			}
		}
		return nodesForLayer;
	}
	
	public boolean isLeaf(){
		if(this.nodeMap.isEmpty()){
			return true;
		}
		
		//tree is completely balanced, so if one contained node has no children, this container is a leaf
		return this.getNodes().get(0).isLeaf();
	}
	
	public boolean isOneStepAboveLeafLevel(){
		if(this.nodeMap.isEmpty() || this.getNodes().get(0).isLeaf()){
			return false;
		}
		
		HRPlusContainerNode nextLevel = this.getNodes().get(0).getChild();
		return nextLevel.isLeaf();	
	}
	
	public void getOverlap(Envelope env){
		env = new Envelope(0, 0, 0, 0);
		for(HRPlusNode node : nodeMap.values()){
			Envelope nodeEnv = new Envelope();
			node.expand(nodeEnv);
			env = env.intersection(nodeEnv);
		}
	}
	


}
