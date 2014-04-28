package org.geogit.api;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

/**
 * TODO these tests are brittle. They depend on the values of 
 * MAX_DEGREE and MIN_DEGREE being 3 and 1, respectively, in the HRPlusTree implementation.
 * 
 * @author ben
 *
 */
public class HRPlusTreePartitionTest {
    
    double DOUBLE_EPSILON = 0.000001;

    @Test(expected=IllegalArgumentException.class)
    public void testZeroNodes() {
        List<HRPlusNode> sortedNodes = new ArrayList<HRPlusNode>();
        // Constructor should raise Preconditions exception
        HRPlusTreePartition pt = new HRPlusTreePartition(sortedNodes);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testOneNode() {
        Envelope envA = new Envelope(-2,2,-2,2);
        HRPlusNode nodeA = new HRPlusNode(new ObjectId(), envA,new ObjectId());
        List<HRPlusNode> sortedNodes = new ArrayList<HRPlusNode>();

        sortedNodes.add(nodeA);
        // Constructor should raise Preconditions exception
        HRPlusTreePartition pt = new HRPlusTreePartition(sortedNodes);
    }
    
    @Test
    public void testUniformNonOverlappingNodes() {
        // 4 envelopes, one per quadrant
        Envelope envA = new Envelope(-2,-4,-2,-4);
        Envelope envB = new Envelope(-2,-4,2,4);
        Envelope envC = new Envelope(2,4,-2,-4);
        Envelope envD = new Envelope(2,4,2,4);
        HRPlusNode nodeA = new HRPlusNode(new ObjectId(), envA, new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(new ObjectId(), envB, new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(new ObjectId(), envC, new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(new ObjectId(), envD, new ObjectId());
        
        List<HRPlusNode> sortedNodes = new ArrayList<HRPlusNode>();
        
        sortedNodes.add(nodeA); sortedNodes.add(nodeB);
        sortedNodes.add(nodeC); sortedNodes.add(nodeD);
        
        HRPlusTreePartition pt = new HRPlusTreePartition(sortedNodes);
        // Partition should have nodeA in first group and the rest in second
        assertEquals(32, pt.getArea(), DOUBLE_EPSILON);
        assertEquals(40, pt.getMargin(), DOUBLE_EPSILON);
        assertEquals(0, pt.getOverlap(), DOUBLE_EPSILON);
        assertEquals(2, pt.getSplitPoint());
    }
    
    @Test
    public void testUniformOverlappingNodes() {
        // Two envelopes mirroring each other across y-axis, intersecting across origin
        Envelope envA = new Envelope(-8,2,-1,1);
        Envelope envB = new Envelope(-2,2,-1,8);
        Envelope envC = new Envelope(-2,8,-1,1);
        Envelope envD = new Envelope(-2,2,1,-8);
        HRPlusNode nodeA = new HRPlusNode(new ObjectId(), envA, new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(new ObjectId(), envB, new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(new ObjectId(), envC, new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(new ObjectId(), envD, new ObjectId());
        
        List<HRPlusNode> sortedNodes = new ArrayList<HRPlusNode>();
        
        sortedNodes.add(nodeA); sortedNodes.add(nodeB);
        sortedNodes.add(nodeC); sortedNodes.add(nodeD);
        
        HRPlusTreePartition pt = new HRPlusTreePartition(sortedNodes);
        
        assertEquals(180, pt.getArea(), DOUBLE_EPSILON);
        assertEquals(76, pt.getMargin(), DOUBLE_EPSILON);
        assertEquals(8, pt.getOverlap(), DOUBLE_EPSILON);
        assertEquals(1, pt.getSplitPoint());
    }
    
    @Test
    public void testThreeOverlappingOneNonOverlapping(){
        // Three overlap in II quadrant, one non-overlapping in I quadrant.
        Envelope envA = new Envelope(-8,-4,4,8);
        Envelope envB = new Envelope(-11,-6,7,8);
        Envelope envC = new Envelope(-10,-9,5,8);
        Envelope envD = new Envelope(5,8,5,8);
        HRPlusNode nodeA = new HRPlusNode(new ObjectId(), envA, new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(new ObjectId(), envB,new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(new ObjectId(), envC,new ObjectId());
        HRPlusNode nodeD = new HRPlusNode(new ObjectId(), envD,new ObjectId());
        
        List<HRPlusNode> sortedNodes = new ArrayList<HRPlusNode>();
        
        sortedNodes.add(nodeA); sortedNodes.add(nodeB);
        sortedNodes.add(nodeC); sortedNodes.add(nodeD);
        
        HRPlusTreePartition pt = new HRPlusTreePartition(sortedNodes);
        
        assertEquals(37, pt.getArea(), DOUBLE_EPSILON);
        assertEquals(34, pt.getMargin(), DOUBLE_EPSILON);
        assertEquals(0, pt.getOverlap(), DOUBLE_EPSILON);
        assertEquals(3, pt.getSplitPoint());
    }

}
