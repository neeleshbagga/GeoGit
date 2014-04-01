package org.geogit.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusTree {
	
	private static final int degree = 3;
	
	private int treeHeight= 0;
	
	private ObjectId objectId;
	
	private Map<ObjectId, HRPlusContainerNode> rootMap = new HashMap<ObjectId, HRPlusContainerNode>();
	 
	 

	public void insert(final ObjectId layerId){
		
		HRPlusNode newNode = new HRPlusNode(layerId);
		
		HRPlusContainerNode containerNode = chooseSubtree(newNode);
		
		containerNode.addNode(newNode);
		
		List<HRPlusContainerNode> newNodes = null;
		
		if(containerNode.getNumNodes() >= degree){
			newNodes = treatOverflow(containerNode, layerId);
			
			
		}
		
		adjustTree(containerNode, newNodes);
		
		
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
			if(newContainerNode.getNumNodes() > degree){
				HRPlusContainerNode secondNewContainerNode = keySplitContainerNode(newContainerNode);
				newContainerNodes.add(secondNewContainerNode);
			}
			newContainerNodes.add(newContainerNode);
		}
		
		return newContainerNodes;
	}
	
	private double marginOf(Envelope envelope){
		return 2*envelope.getHeight() + 2*envelope.getWidth();
	}
	
	private Envelope boundingBoxOf(List<SimpleHRPlusNode> nodes){
		Envelope envelope = new Envelope();
		for(SimpleHRPlusNode node : nodes){
			node.expand(envelope);
		}
		
		return envelope;
		
	}
	
	private List<SimpleHRPlusNode> partitionByMinOverlap(List<SimpleHRPlusNode>minSort, List<SimpleHRPlusNode>maxSort){
		
		List<SimpleHRPlusNode> firstGroup = minSort.subList(0, minSort.size()/2);
		List<SimpleHRPlusNode> secondGroup = minSort.subList(minSort.size() - firstGroup.size(), minSort.size() -1);
		
		double minOverlapMinSort = boundingBoxOf(firstGroup).intersection(boundingBoxOf(secondGroup)).getArea();
		double minOverlapMinSortAreaValue = boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
		int minOverlapMinSortSplitPoint = firstGroup.size();
		
		double overlap;
		double areaValue;
		
		while(!secondGroup.isEmpty()){
			firstGroup.add(secondGroup.remove(0));
			overlap = boundingBoxOf(firstGroup).intersection(boundingBoxOf(secondGroup)).getArea();
			minOverlapMinSortAreaValue = boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
			if(overlap < minOverlapMinSort){
				minOverlapMinSort = overlap;
				minOverlapMinSortSplitPoint = firstGroup.size();
			}else if(overlap == minOverlapMinSort){
				areaValue = boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
				if(areaValue <minOverlapMinSortAreaValue){
					minOverlapMinSort = overlap;
					minOverlapMinSortSplitPoint = firstGroup.size();
					minOverlapMinSortAreaValue = areaValue;
				}
				
			}
			
		}
		
		firstGroup = maxSort.subList(0, maxSort.size()/2);
		secondGroup = maxSort.subList(maxSort.size() - firstGroup.size(), maxSort.size() -1);
		
		double minOverlapMaxSort = boundingBoxOf(firstGroup).intersection(boundingBoxOf(secondGroup)).getArea();
		double minOverlapMaxSortAreaValue = boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
		int minOverlapMaxSortSplitPoint = firstGroup.size();
		
		while(!secondGroup.isEmpty()){
			firstGroup.add(secondGroup.remove(0));
			overlap = boundingBoxOf(firstGroup).intersection(boundingBoxOf(secondGroup)).getArea();
			minOverlapMaxSortAreaValue = boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
			if(overlap < minOverlapMinSort){
				minOverlapMaxSort = overlap;
				minOverlapMaxSortSplitPoint = firstGroup.size();
			}else if(overlap == minOverlapMinSort){
				areaValue = boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
				if(areaValue < minOverlapMaxSortAreaValue){
					minOverlapMaxSort = overlap;
					minOverlapMaxSortSplitPoint = firstGroup.size();
					minOverlapMaxSortAreaValue = areaValue;
				}
				
			}
			
		}
		
		List<SimpleHRPlusNode> partition = new ArrayList<SimpleHRPlusNode>();
		
		if(minOverlapMinSort < minOverlapMaxSort || (minOverlapMinSort == minOverlapMaxSort && minOverlapMinSortAreaValue <= minOverlapMaxSortAreaValue)){
			partition = minSort.subList(0, minOverlapMinSortSplitPoint - 1);
		}else{
			partition = maxSort.subList(0, minOverlapMaxSortSplitPoint - 1);
		}
		
		return partition;
		
	}
	
	private double sumOfMargins(List<SimpleHRPlusNode> nodes){
		List<SimpleHRPlusNode> firstGroup = nodes.subList(0, nodes.size()/2);
		List<SimpleHRPlusNode> secondGroup = nodes.subList(nodes.size() - firstGroup.size(), nodes.size() -1);
		
		double marginValueSum = 0;
		
		while(!secondGroup.isEmpty()){
			marginValueSum +=  marginOf(boundingBoxOf(firstGroup)) + marginOf(boundingBoxOf(secondGroup));
			firstGroup.add(secondGroup.remove(0));
		}
		return marginValueSum;
	}
	
	private HRPlusContainerNode keySplitContainerNode(HRPlusContainerNode containerNode){
		
		//Uses R* splitting algorithm
		List<SimpleHRPlusNode> minXSort = containerNode.getSimpleNodes();
		List<SimpleHRPlusNode> maxXSort = containerNode.getSimpleNodes();
		List<SimpleHRPlusNode> minYSort = containerNode.getSimpleNodes();
		List<SimpleHRPlusNode> maxYSort = containerNode.getSimpleNodes();
		
				
		Collections.sort(minXSort, new Comparator<SimpleHRPlusNode>() {
		    public int compare(SimpleHRPlusNode n1, SimpleHRPlusNode n2) {
		    	return Double.compare(n1.getMinX(), n1.getMinX());
		    }
		});
		
		Collections.sort(maxXSort, new Comparator<SimpleHRPlusNode>() {
		    public int compare(SimpleHRPlusNode n1, SimpleHRPlusNode n2) {
		    	return Double.compare(n1.getMaxX(), n1.getMaxX());
		    }
		});;
		
		 Collections.sort(minYSort, new Comparator<SimpleHRPlusNode>() {
		    public int compare(SimpleHRPlusNode n1, SimpleHRPlusNode n2) {
		    	return Double.compare(n1.getMinY(), n1.getMinY());
		    }
		});
		
		 Collections.sort(maxYSort, new Comparator<SimpleHRPlusNode>() {
		    public int compare(SimpleHRPlusNode n1, SimpleHRPlusNode n2) {
		    	return Double.compare(n1.getMaxY(), n1.getMaxY());
		    }
		});
		
		
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

	
	private HRPlusNode adjustTree(HRPlusContainerNode containerNode, List<HRPlusContainerNode> newLeaves){
		
		HRPlusNode parent;
		Envelope envelope;
		while(!this.isRoot(containerNode)){
			parent = lookupHRPlusNode(containerNode.getParentId());
			node.expand(envelope);
			
			parent.expandBounds(envelope);
			
			//some stuff
			
			node = parent;
			
			
			
			
		}
		return leaf;
		
	}
	
	public HRPlusContainerNode lookupHRPlusContainerNode(ObjectId objectId){
		HRPlusContainerNode containerNode = new HRPlusContainerNode();
		return containerNode;
	}
	
	
	public HRPlusNode lookupHRPlusNode(ObjectId objectId){
		HRPlusNode node = new HRPlusNode(objectId);
		return node;
	}
	
	
	private HRPlusContainerNode getRootForLayerId(ObjectId layerId){
		return rootMap.get(layerId);
	}
	
	/*
	 * Choose the subtree to insert the new node at
	 */
	private HRPlusContainerNode chooseSubtree(final HRPlusNode newNode){
		
		
		HRPlusContainerNode containerNode = getRootForLayerId(newNode.getFirstLayerId());
		
		while(!containerNode.isLeaf()){
			
			List<HRPlusNode> nodesForLayer = containerNode.getNodesForLayer(newNode.getFirstLayerId());
			
			double minEnlargement = Double.MAX_VALUE;
			double enlargement;
			Envelope currentEnvelope = new Envelope();
			Envelope newEnvelope = new Envelope();
			
			HRPlusNode insertionNode = nodesForLayer.get(0);
			
			if(containerNode.isOneStepAboveLeafLevel()){
				
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
