package org.geogit.api;

import java.util.List;

public class HRPlusTreePartition extends HRPlusTreeUtils{
	
	double overlap = Double.MAX_VALUE;
	double area = Double.MAX_VALUE;
	int splitPoint = 0;
	
	public double getOverlap() {
		return overlap;
	}

	public double getArea() {
		return area;
	}



	public int getSplitPoint() {
		return splitPoint;
	}

	public HRPlusTreePartition(List<SimpleHRPlusNode> sortedNodes){
		List<SimpleHRPlusNode> firstGroup = sortedNodes.subList(0, sortedNodes.size()/2);
		List<SimpleHRPlusNode> secondGroup = sortedNodes.subList(sortedNodes.size() - firstGroup.size(), sortedNodes.size() -1);
		
		double curOverlap;
		double curArea;
		
		while(!secondGroup.isEmpty()){
			firstGroup.add(secondGroup.remove(0));
			curOverlap = getOverlap(firstGroup, secondGroup);
			if(curOverlap < this.overlap){
				this.overlap = curOverlap;
				this.splitPoint = firstGroup.size();
			}else if(curOverlap == this.overlap){
				curArea = getTotalAreaOfTwoRegions(firstGroup, secondGroup);
				if(curArea < this.area){
					this.overlap = curOverlap;
					this.splitPoint = firstGroup.size();
					this.area = curArea;
				}
				
			}
			
		}
		
	}
	
	
	

	
	

	
	

}
