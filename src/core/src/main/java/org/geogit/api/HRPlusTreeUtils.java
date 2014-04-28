package org.geogit.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Base class for HRPlus tree implementation of the spatial index.
 * This class contains utility methods for manipulating HRPlus trees.
 * Utilities are factored out because we distinguish
 * between a plain HRPlus tree and an HRPlus tree partition. The latter is
 * used to speed up insertions.
 * 
 * For the original idea, see
 *     [1] http://www.cs.ust.hk/faculty/dimitris/PAPERS/ssdbm01.pdf
 * and for details on insertion, see Section 4 of:
 *     [2] http://dbs.mathematik.uni-marburg.de/publications/myPapers/1990/BKSS90.pdf
 * 
 * @author jillian
 *
 */
public class HRPlusTreeUtils {
    
    // Maximum degree for any node. We split if a node has greater degree during an insert.
    private static final int MIN_DEGREE = 1;
    private static final int MAX_DEGREE = 3;
    
    /**
     * Return the minimum number of nodes a container in this tree may hold.
     * This is the parameter `m` in the paper [2]
     */
    public int getMinDegree() {
        return MIN_DEGREE;
    }

    /**
     * Return the minimum number of nodes a container in this tree may hold.
     * If, on insert, a container gets more nodes, then it will be split.
     * 
     * This is the parameter `M` in the paper [2]
     */
    public int getMaxDegree() {
        return MAX_DEGREE;
    }

    /**
     * Create an envelope covering all the points in @param nodes. Used to 
     * create a container node for the HRPlus tree.
     * @param nodes
     * @return
     */
    protected static Envelope boundingBoxOf(List<HRPlusNode> nodes){
        Envelope envelope = new Envelope();
        // Iterate over nodes, expand envelope to include each.
        // We don't care about the order of iteration. Nothing fancy here.
        for(HRPlusNode node : nodes){
            node.expand(envelope);
        }
        return envelope;
    }

    /**
     * Find the overlap between bounding boxes surrounding two groups of nodes.
     * 
     * @param firstGroup
     * @param secondGroup
     * @return the area of the enclosing bounding box
     */
    protected static double getOverlap(List<HRPlusNode> firstGroup,
            List<HRPlusNode> secondGroup){
        return boundingBoxOf(firstGroup).intersection(boundingBoxOf(secondGroup)).getArea();
    }

    /**
     * Get total area of the envelopes covering two groups of nodes.
     * 
     * @param firstGroup
     * @param secondGroup
     * @return combined area of the two envelopes
     */
    protected static double getTotalAreaOfTwoRegions(List<HRPlusNode> firstGroup, List<HRPlusNode> secondGroup){
        return boundingBoxOf(firstGroup).getArea() + boundingBoxOf(secondGroup).getArea();
    }
    
    /**
     * Get total perimeter of the envelopes covering two groups of nodes.
     * 
     * @param firstGroup
     * @param secondGroup
     * @return combined perimeter of the two envelopes
     */
    protected static double getTotalMarginOfTwoRegions(List<HRPlusNode> firstGroup, List<HRPlusNode> secondGroup){
        return marginOf(boundingBoxOf(firstGroup)) + marginOf(boundingBoxOf(secondGroup));
    }

    /**
     * margin = perimeter
     * @param envelope
     * @return the perimeter of the envelope
     */
    protected static double marginOf(Envelope envelope){
        double height = envelope.getHeight();
        double width = envelope.getWidth(); 
        return height + height + width + width;
    }

    /**
     * The paper [2] suggests minimizing perimeters as a useful way to optimize an index.
     * Sum the perimeters of all possible partitions of nodes along one axis.
     * 
     * @param nodes
     * @return
     */
    protected static double sumOfMargins(List<HRPlusNode> nodes){
    	if (nodes.isEmpty() || nodes.size() == 1) {
    		return marginOf(boundingBoxOf(nodes));
    	} else {
    		// Begin with a one-element list and an (n-1) element list.
	        List<HRPlusNode> firstGroup = new ArrayList<HRPlusNode>();
	        firstGroup.addAll((nodes.subList(0, 1)));
	        List<HRPlusNode> secondGroup = new ArrayList<HRPlusNode>();
	        secondGroup.addAll(nodes.subList(1, nodes.size()));
	
	        double marginValueSum = 0;
	        // Iteratively add one element of the second group to the first.
	        while(!secondGroup.isEmpty()){
	            marginValueSum +=  marginOf(boundingBoxOf(firstGroup)) + marginOf(boundingBoxOf(secondGroup));
	            HRPlusNode removed = secondGroup.remove(0);
	            firstGroup.add(removed);
	        }
	        return marginValueSum;
    	}
    }

    /**
     * Take a list of nodes, sorted by position on a one-dimensional axis.
     * Return a subset of these nodes. Specifically, the subset of nodes
     * that is closest together based on the one-dimensional axis and the
     * total perimeter and area of their enclosing envelope.
     * 
     * @param minSort nodes sorted by minimum position along some axis 
     * @param maxSort same nodes, sorted by maximum position along the same axis.
     * @return sublist containing the nodes closest together
     */
    protected static List<HRPlusNode> partitionByMinOverlap(List<HRPlusNode>minSort, 
            List<HRPlusNode>maxSort){
        // Create two partitions corresponding to the two arguments.
        // This determines the minimum area/perimeter split of the nodes.
        HRPlusTreePartition minPartition = new HRPlusTreePartition(minSort);
        HRPlusTreePartition maxPartition = new HRPlusTreePartition(maxSort);
        // Extract fields from the partitions 
        double overlapMinSort = minPartition.getOverlap();
        double areaValueMinSort = minPartition.getArea();
        double overlapMaxSort = maxPartition.getOverlap();
        double areaValueMaxSort = maxPartition.getArea();
        // Choose the partition with the smallest overlap.
        // If overlaps are equal, choose the partition with the smallest area.
        if (overlapMinSort < overlapMaxSort || 
                (overlapMinSort == overlapMaxSort && 
                areaValueMinSort <= areaValueMaxSort)) {
            int splitPointMinSort = minPartition.getSplitPoint();
            // Get the spatially smallest partition of nodes from the minsort list
            return minSort.subList(0, splitPointMinSort);
        } else {
            int splitPointMaxSort = maxPartition.getSplitPoint();
            // Get the smallest (least perimeter/area) partition of nodes from the maxsort list
            return maxSort.subList(0, splitPointMaxSort);
        }
    }

    /**
     * Sort a list of nodes by their minimum x coordinate.
     * @param nodes
     * @return
     */
    protected static List<HRPlusNode> minXSort(List<HRPlusNode> nodes){
        List<HRPlusNode> minXSort = new ArrayList<HRPlusNode>(nodes);
        Collections.sort(minXSort, new Comparator<HRPlusNode>() {
            public int compare(HRPlusNode n1, HRPlusNode n2) {
                return Double.compare(n1.getMinX(), n2.getMinX());
            }
        });
        return minXSort;
    }

    /**
     * Sort a list of nodes by their minimum y coordinate.
     * @param nodes
     * @return
     */
    protected static List<HRPlusNode> minYSort(List<HRPlusNode> nodes){
        List<HRPlusNode> minYSort = new ArrayList<HRPlusNode>(nodes);
        Collections.sort(minYSort, new Comparator<HRPlusNode>() {
            public int compare(HRPlusNode n1, HRPlusNode n2) {
                return Double.compare(n1.getMinY(), n2.getMinY());
            }
        });
        return minYSort;
    }

    /**
     * Sort a list of nodes by their maximum x coordinate
     * @param nodes
     * @return
     */
    protected static List<HRPlusNode> maxXSort(List<HRPlusNode> nodes){
        List<HRPlusNode> maxXSort = new ArrayList<HRPlusNode>(nodes);
        Collections.sort(maxXSort, new Comparator<HRPlusNode>() {
            public int compare(HRPlusNode n1, HRPlusNode n2) {
                return Double.compare(n1.getMaxX(), n2.getMaxX());
            }
        });
        return maxXSort;
    }

    /**
     * Sort a list of nodes by their maximum y coordinate.
     * @param nodes
     * @return
     */
    protected static List<HRPlusNode> maxYSort(List<HRPlusNode> nodes){
        List<HRPlusNode> maxYSort = new ArrayList<HRPlusNode>(nodes);
        Collections.sort(maxYSort, new Comparator<HRPlusNode>() {
            public int compare(HRPlusNode n1, HRPlusNode n2) {
                return Double.compare(n1.getMaxY(), n2.getMaxY());
            }
        });
        return maxYSort;
    }

}
