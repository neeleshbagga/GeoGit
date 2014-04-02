package org.geogit.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geogit.storage.ObjectDatabase;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusTree extends HRPlusTreeUtils{
	
	private static final int DEGREE = 3;
		
	private ObjectId objectId;
	
	private ObjectDatabase db;
	
	private Map<ObjectId, List<HRPlusContainerNode>> rootMap = new HashMap<ObjectId, List<HRPlusContainerNode>>();
	 
	 
	public void insert(final ObjectId layerId, Envelope bounds){
		
		HRPlusNode newNode = new HRPlusNode(layerId, bounds);
		
		HRPlusContainerNode containerNode = chooseSubtree(newNode);
		
		containerNode.addNode(newNode);
		
		List<HRPlusContainerNode> newContainerNodes = null;
		
		if(containerNode.getNumNodes() >= HRPlusTree.DEGREE){
			newContainerNodes = treatOverflow(containerNode, layerId);
		}
		
		List<HRPlusContainerNode> newRoots = adjustTree(containerNode, newContainerNodes, layerId);
		
		for(HRPlusContainerNode newRoot : newRoots){
			this.addRootTableEntry(newRoot);
		}
		
	}
	
	private void addRootTableEntry(HRPlusContainerNode newRoot){
		for(ObjectId layerId : newRoot.getLayerIds()){
			List<HRPlusContainerNode> roots = this.rootMap.get(layerId);
			if(roots == null){
				roots = new ArrayList<HRPlusContainerNode>();
			}
			
			roots.add(newRoot);
			
			this.rootMap.put(layerId, roots);
		}
	}
	
	
	private List<HRPlusContainerNode> treatOverflow(HRPlusContainerNode containerNode, ObjectId layerId){
		List<HRPlusContainerNode> newContainerNodes = new ArrayList<HRPlusContainerNode>();
		if(containerNode.allNodesContainLayerId(layerId)){
			HRPlusContainerNode newContainerNode = keySplitContainerNode(containerNode);
			newContainerNodes.add(newContainerNode);
			
		}else{
			HRPlusContainerNode newContainerNode = new HRPlusContainerNode();
			List<HRPlusNode> nodesForLayer = containerNode.getNodesForLayer(layerId);
			for(HRPlusNode node : nodesForLayer){
				HRPlusNode transferNode = containerNode.removeNode(node.getObjectId());
				newContainerNode.addNode(transferNode);
					
			}
			if(newContainerNode.getNumNodes() > HRPlusTree.DEGREE){
				HRPlusContainerNode secondNewContainerNode = keySplitContainerNode(newContainerNode);
				newContainerNodes.add(secondNewContainerNode);
			}
			newContainerNodes.add(newContainerNode);
		}
		
		return newContainerNodes;
	}
	

	
	private HRPlusContainerNode keySplitContainerNode(HRPlusContainerNode containerNode){
		
		//Uses R* splitting algorithm
		List<SimpleHRPlusNode> minXSort = minXSort(containerNode.getSimpleNodes());
		List<SimpleHRPlusNode> maxXSort = maxXSort(containerNode.getSimpleNodes());
		List<SimpleHRPlusNode> minYSort = minYSort(containerNode.getSimpleNodes());
		List<SimpleHRPlusNode> maxYSort = maxYSort(containerNode.getSimpleNodes());
		
		double xMarginSum = sumOfMargins(minXSort) + sumOfMargins(maxXSort);
		double yMarginSum = sumOfMargins(minYSort) + sumOfMargins(maxYSort);
		
				
		List<SimpleHRPlusNode> partition;
		
		//choose the split axis based on the min margin sum
		if(xMarginSum <= yMarginSum){
			//choose distribution with the minimum overlap value
			partition = partitionByMinOverlap(minXSort, maxXSort);
			
		}else{
			partition = partitionByMinOverlap(minYSort, maxYSort);
		}

		HRPlusContainerNode newContainerNode = new HRPlusContainerNode();
		
		for(SimpleHRPlusNode node : partition){
			HRPlusNode transferNode = containerNode.removeNode(node.getObjectId());
			newContainerNode.addNode(transferNode);
		}
		
		return newContainerNode;

	}
	
	private boolean isRoot(HRPlusContainerNode containerNode){
		if(containerNode == null || this.rootMap == null){
			return false;
		}
		
		return this.rootMap.values().contains(containerNode.getObjectId());
	}

	
	private List<HRPlusContainerNode> adjustTree(HRPlusContainerNode containerNode, List<HRPlusContainerNode> siblingContainerNodes, ObjectId layerId){
		
		HRPlusNode parent;
		while(!this.isRoot(containerNode)){
			parent = lookupHRPlusNode(containerNode.getParentId());
			HRPlusContainerNode parentContainer = lookupHRPlusContainerNode(parent.getParentContainerId());
			
			Envelope containerMBR = containerNode.getMBR();
			parent.setBounds(containerMBR);
			
			for(HRPlusContainerNode siblingContainerNode : siblingContainerNodes){
				HRPlusNode newNode = new HRPlusNode(siblingContainerNode.getLayerIds(), siblingContainerNode.getMBR());
				newNode.setChild(siblingContainerNode);
				parentContainer.addNode(newNode);				
			}
			
			List<HRPlusContainerNode> newContainerNodes = null;
			
			if(parentContainer.getNumNodes() > HRPlusTree.DEGREE){
				newContainerNodes = treatOverflow(parentContainer, layerId);
			}
			
			containerNode = parentContainer;
			siblingContainerNodes = newContainerNodes;
			
		}
		
		return siblingContainerNodes;
		
	}
	
	public HRPlusContainerNode lookupHRPlusContainerNode(ObjectId objectId){
		HRPlusContainerNode containerNode = new HRPlusContainerNode();
		return containerNode;
	}
	
	
	public HRPlusNode lookupHRPlusNode(ObjectId objectId){
		HRPlusNode node = this.db.get(objectId);
		return node;
	}
	
	private List<HRPlusContainerNode> getRootsForLayerId(ObjectId layerId){
		return rootMap.get(layerId);
	}
	
	/*
	 * Choose the subtree to insert the new node at
	 */
	private HRPlusContainerNode chooseSubtree(final HRPlusNode newNode){
		
		List<HRPlusContainerNode> containerNodes = getRootsForLayerId(newNode.getFirstLayerId());
		if(containerNodes.isEmpty()){
			HRPlusContainerNode newContainerNode = new HRPlusContainerNode();
			return newContainerNode;
		}
		
		//Choose the container node with the largest intersection area with the new node
		double maxIntersectionArea = Double.MIN_VALUE;
		HRPlusContainerNode maxIntersectionContainerNode = containerNodes.get(0);
		for(HRPlusContainerNode containerNode: containerNodes){
			double intersectionArea = containerNode.getMBR().intersection(newNode.getBounds()).getArea();
			if( intersectionArea > maxIntersectionArea){
				maxIntersectionArea = intersectionArea;
				maxIntersectionContainerNode = containerNode;
			}
		}
		
		HRPlusContainerNode containerNode = maxIntersectionContainerNode;
		
		while(containerNode.isLeaf()){
			
			List<HRPlusNode> nodesForLayer = containerNode.getNodesForLayer(newNode.getFirstLayerId());

			double minEnlargement = Double.MAX_VALUE;
			double enlargement;
			Envelope currentEnvelope = new Envelope();
			Envelope newEnvelope = new Envelope();
			
			HRPlusNode insertionNode = nodesForLayer.get(0);
			
			if(containerNodes.get(0).isOneStepAboveLeafLevel()){
				
				//find the node such that inserting newNode causes the minimum overlap enlargement
				for(HRPlusNode node : nodesForLayer){
					node.getOverlap(currentEnvelope);
					newNode.expand(newEnvelope);

					enlargement = currentEnvelope.intersection(newEnvelope).getArea() - currentEnvelope.getArea();
					if(enlargement < minEnlargement){
						insertionNode = node;
					}	
				}
			}else{
				//find the node such that inserting newNode causes the minimum area enlargement
				for(HRPlusNode node : nodesForLayer){
					node.expand(currentEnvelope);
					node.expand(newEnvelope);
					newNode.expand(newEnvelope);
					
					enlargement = newEnvelope.getArea() - currentEnvelope.getArea();
					if(enlargement < minEnlargement){
						insertionNode = node;
					}	
				}
			}
			
			insertionNode.addLayerId(newNode.getFirstLayerId());
			containerNode = insertionNode.getChild();
				
		}
		return containerNode;

	}

}
