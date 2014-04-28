package org.geogit.api;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Based on the algorithm from
 *     [2] http://dbs.mathematik.uni-marburg.de/publications/myPapers/1990/BKSS90.pdf
 * Create a spatial partition of a set of nodes.
 * 
 * @author jillian
 *
 */
public class HRPlusTreePartition extends HRPlusTreeUtils{

    // Note that these fields care about the bounding boxes ENCLOSING the points inside the partitions.
    // Area of both partitions, combined.
    double area;
    // Area of shared space between partitions.
    double overlap;
    // Perimeter of both partitions, combined
    double margin;
    // index of the input list where nodes were divided.
    int splitPoint;

    /**
     * Accept a list of nodes sorted by position along a one-dimensional axis.
     * Create a partition of these nodes, split at one point along the axis.
     * 
     * The paper by Beckmann et. al [2] recommends a 'goodness' measure
     * minimizing overlap with area as the tiebreaker.
     * 
     * Record the overlap between the two segments of the partition,
     * the total area covered by the nodes in the partition, and
     * the index of the list at which nodes are divided.
     * 
     * @param sortedNodes
     */
    public HRPlusTreePartition(List<HRPlusNode> sortedNodes){
        // Partition should ONLY be called on a group of nodes that overflowed the container.
        // Anything else and we don't have a meaningful way to subdivide groups and
        // the 'goodness' metric becomes useless.
        Preconditions.checkArgument(sortedNodes.size() == this.getMaxDegree()+1, 
                "expected [%s] nodes, got [%s] nodes", sortedNodes.size(), this.getMaxDegree()+1);
        // Following the paper [2],
        //     let M = this.getMaxDegree()
        //     and m = this.getMinDegree()
        // We'll pick the best distribution of these M+1 nodes by iterating
        // over (M - 2m+1) distributions and choosing the one with the
        // best 'goodness' rating. The kth distribution has the first
        // (m-1)+k nodes in the first group and the others in the second.
        List<HRPlusNode> firstGroup = new ArrayList<HRPlusNode>();
        List<HRPlusNode> secondGroup = new ArrayList<HRPlusNode>();
        int numInFirstGroup = this.getMinDegree(); // -1 + (1=k)
        firstGroup.addAll((sortedNodes.subList(0, numInFirstGroup)));
        secondGroup.addAll(sortedNodes.subList(numInFirstGroup, this.getMaxDegree()+1));
        // Initialize fields for first group
        this.area = getTotalAreaOfTwoRegions(firstGroup, secondGroup);
        this.margin = getTotalMarginOfTwoRegions(firstGroup, secondGroup);
        this.overlap = getOverlap(firstGroup, secondGroup);
        this.splitPoint = 1;
        // Track the overlap and area
        double curOverlap = this.overlap;
        double curArea = this.area;
        // Iteratively add elements of second group to the first group.
        while(!secondGroup.isEmpty()){
            if(curOverlap < this.overlap) {
                // Keep the smallest overlap
                this.area = curArea;
                this.overlap = curOverlap;
                this.margin = getTotalMarginOfTwoRegions(firstGroup, secondGroup);
                this.splitPoint = firstGroup.size();
            } else if(curOverlap == this.overlap && curArea < this.area){
                // Same overlap? Choose the smallest total area as tiebreaker
                this.area = curArea;
                this.margin = getTotalMarginOfTwoRegions(firstGroup, secondGroup);
                this.overlap = curOverlap;
                this.splitPoint = firstGroup.size();
            }
            // Move a node from one group to the next. This is the effect
            // achieved by incrementing `k`.
            firstGroup.add(secondGroup.remove(0));
            curOverlap = getOverlap(firstGroup, secondGroup);
            curArea = getTotalAreaOfTwoRegions(firstGroup, secondGroup);
        }
    }

    /**
     * How much area is required to cover both partitions, in total?
     * Sum of the area of the bounding box enclosing the first with the area of the 
     * box enclosing the second.
     * @return
     */
    public double getArea() {
        return this.area;
    }
    
    /**
     * What is the total perimeter of the bounding boxes enclosing the two partitions?
     * Sum of the first perimeter with the second.
     * @return
     */
    public double getMargin() {
        return this.margin;
    }

    /**
     * How much area do bounding boxes of the partitions share?
     * @return
     */
    public double getOverlap() {
        return this.overlap;
    }

    /**
     * Index at which to take a subList to divide the list of nodes (given
     * as input to the constructor) according to this partition.
     * In other words, this is the number of nodes in the first group.
     * @return
     */
    public int getSplitPoint() {
        return this.splitPoint;
    }
}
