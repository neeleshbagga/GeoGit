package org.geogit.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusTreeUtils {
	
	protected static Envelope boundingBoxOf(List<SimpleHRPlusNode> nodes){
		Envelope envelope = new Envelope();
		for(SimpleHRPlusNode node : nodes){
			node.expand(envelope);
		}
		
		return envelope;
		
	}
	
	protected static double getOverlap(List<SimpleHRPlusNode> firstGroup, List<SimpleHRPlusNode> secondGroup){
		return boundingBoxOf(firstGroup).intersection(boundingBoxOf(secondGroup)).getArea();
	}
	

	protected static double getTotalAreaOfTwoRegions(List<SimpleHRPlusNode> firstGroup, List<SimpleHRPlusNode> secondGroup){
		return boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
	}
	
	protected static double marginOf(Envelope envelope){
		return 2*envelope.getHeight() + 2*envelope.getWidth();
	}
	

	protected static double sumOfMargins(List<SimpleHRPlusNode> nodes){
		List<SimpleHRPlusNode> firstGroup = nodes.subList(0, nodes.size()/2);
		List<SimpleHRPlusNode> secondGroup = nodes.subList(nodes.size() - firstGroup.size(), nodes.size() -1);
		
		double marginValueSum = 0;
		
		while(!secondGroup.isEmpty()){
			marginValueSum +=  marginOf(boundingBoxOf(firstGroup)) + marginOf(boundingBoxOf(secondGroup));
			firstGroup.add(secondGroup.remove(0));
		}
		return marginValueSum;
	}
	
	
	protected static List<SimpleHRPlusNode> partitionByMinOverlap(List<SimpleHRPlusNode>minSort, List<SimpleHRPlusNode>maxSort){
		
		HRPlusTreePartition minPartition = new HRPlusTreePartition(minSort);
		
		double overlapMinSort = minPartition.getOverlap();
		double areaValueMinSort = minPartition.getArea();
		int splitPointMinSort = minPartition.getSplitPoint();
		
		
		HRPlusTreePartition maxPartition = new HRPlusTreePartition(maxSort);
		
		double overlapMaxSort = maxPartition.getOverlap();
		double areaValueMaxSort = maxPartition.getArea();
		int splitPointMaxSort = maxPartition.getSplitPoint();

		List<SimpleHRPlusNode> partition = new ArrayList<SimpleHRPlusNode>();
		
		if(overlapMinSort < overlapMaxSort || (overlapMinSort == overlapMaxSort && areaValueMinSort <= areaValueMaxSort)){
			partition = minSort.subList(0, splitPointMinSort - 1);
		}else{
			partition = maxSort.subList(0, splitPointMaxSort - 1);
		}
		
		return partition;
		
	}
	
	protected static List<SimpleHRPlusNode> minXSort(List<SimpleHRPlusNode> nodes){
		List<SimpleHRPlusNode> minXSort = new ArrayList<SimpleHRPlusNode>(nodes);
		Collections.sort(minXSort, new Comparator<SimpleHRPlusNode>() {
		    public int compare(SimpleHRPlusNode n1, SimpleHRPlusNode n2) {
		    	return Double.compare(n1.getMinX(), n1.getMinX());
		    }
		});
		return minXSort;
	}
	
	protected static List<SimpleHRPlusNode> minYSort(List<SimpleHRPlusNode> nodes){
		List<SimpleHRPlusNode> minYSort = new ArrayList<SimpleHRPlusNode>(nodes);
		Collections.sort(minYSort, new Comparator<SimpleHRPlusNode>() {
		    public int compare(SimpleHRPlusNode n1, SimpleHRPlusNode n2) {
		    	return Double.compare(n1.getMinY(), n1.getMinY());
		    }
		});
		return minYSort;
	}
	
	protected static List<SimpleHRPlusNode> maxXSort(List<SimpleHRPlusNode> nodes){
		List<SimpleHRPlusNode> maxXSort = new ArrayList<SimpleHRPlusNode>(nodes);
		Collections.sort(maxXSort, new Comparator<SimpleHRPlusNode>() {
		    public int compare(SimpleHRPlusNode n1, SimpleHRPlusNode n2) {
		    	return Double.compare(n1.getMaxX(), n1.getMaxX());
		    }
		});
		return maxXSort;
	}
	
	protected static List<SimpleHRPlusNode> maxYSort(List<SimpleHRPlusNode> nodes){
		List<SimpleHRPlusNode> maxYSort = new ArrayList<SimpleHRPlusNode>(nodes);
		Collections.sort(maxYSort, new Comparator<SimpleHRPlusNode>() {
		    public int compare(SimpleHRPlusNode n1, SimpleHRPlusNode n2) {
		    	return Double.compare(n1.getMaxY(), n1.getMaxY());
		    }
		});
		return maxYSort;
	}
	

}
