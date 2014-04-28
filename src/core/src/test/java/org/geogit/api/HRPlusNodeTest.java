package org.geogit.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusNodeTest {
    
    double DOUBLE_EPSILON = 0.000001;
    
    @Test
    public void testGetObjectId(){
        ObjectId oid = new ObjectId();
        HRPlusNode node = new HRPlusNode(oid, new Envelope(),new ObjectId());
        assertEquals(oid, node.getObjectId());
    }

    @Test
    public void testGetMinX(){
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(-3,99,-999,11),new ObjectId());
        assertEquals(-3, node.getMinX(), DOUBLE_EPSILON);
    }
    
    @Test
    public void testGetMinY(){
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(-3,99,-999,11),new ObjectId());
        assertEquals(-999, node.getMinY(), DOUBLE_EPSILON);
    }
    
    @Test
    public void testGetMaxX(){
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(-3,99,-999,11),new ObjectId());
        assertEquals(99, node.getMaxX(), DOUBLE_EPSILON);
    }
    
    @Test
    public void testGetMaxY(){
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(-3,99,-999,11),new ObjectId());
        assertEquals(11, node.getMaxY(), DOUBLE_EPSILON);
    }
    
    @Test
    public void testExpandNull(){
        // Initialize node with an envelope
        Envelope env = new Envelope(0,1,0,1);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        // Expand by same envelope
        node.expand(env);
        // Check that no expansion happened
        assertEquals(node.getBounds(), env);
    }
    
    @Test
    public void testExpandSmall(){
        Envelope env = new Envelope(0,1,0,1);
        Envelope toExpand = new Envelope();
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        node.expand(toExpand);
        
        assertEquals(toExpand, env);
    }
    
    @Test
    public void testExpandLargeLeft(){
        Envelope env = new Envelope(-100,100,-500,500);
        Envelope toExpand = new Envelope(900, 899, 900, 899);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        node.expand(toExpand);
        
        assertEquals(toExpand, new Envelope(900, -100, 900, -500));
    }    
    
    @Test
    public void testExpandLargeRight(){
        Envelope env = new Envelope(-100,100,-500,500);
        Envelope toExpand = new Envelope(-900, -899, -900, -899);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        node.expand(toExpand);
        
        assertEquals(toExpand, new Envelope(-900, 100, -900, 500));
    }
    
    @Test
    public void testExpandMultiNode(){
        Envelope toExpand = new Envelope(-5, 15, 30, 32);
        Envelope envA = new Envelope(-30, -25, -3, 4);
        Envelope envB = new Envelope(300, 298, -5, 31);
        Envelope envC = new Envelope(-30, -5, -6, -4);
        HRPlusNode nodeA = new HRPlusNode(new ObjectId(), envA,new ObjectId());
        HRPlusNode nodeB = new HRPlusNode(new ObjectId(), envB,new ObjectId());
        HRPlusNode nodeC = new HRPlusNode(new ObjectId(), envC,new ObjectId());
        nodeA.expand(toExpand);
        nodeB.expand(toExpand);
        nodeC.expand(toExpand);
        
        assertEquals(toExpand, new Envelope(-30, 300, -6, 32));
    }
    
    @Test
    public void testGetChildNull(){
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(),new ObjectId());
        assertEquals(null, node.getChild());
    }
    
    @Test
    public void testGetChildNotNull(){
        HRPlusContainerNode child = new HRPlusContainerNode(new ObjectId());
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(),new ObjectId());
        node.setChild(child);
        assertEquals(child, node.getChild());
    }
    

    @Test
    public void testIsLeafTrue(){
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(),new ObjectId());
        assertTrue(node.isLeaf());        
    }
    
    @Test
    public void testIsLeafFalse(){
        HRPlusContainerNode child = new HRPlusContainerNode(new ObjectId());
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(),new ObjectId());
        node.setChild(child);
        assertFalse(node.isLeaf()); 
    }
    
    @Test
    public void testGetParentContainerId(){
        // TODO this field is never set!
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope(),new ObjectId());
        assertEquals(null, node.getParentContainerId());
    }
    
    @Test
    public void testGetBoundsNull(){
        Envelope env = new Envelope(0,0,0,0);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        assertEquals(env, node.getBounds());
    }
    
    @Test
    public void testGetBoundsNonEmpty(){
        Envelope env = new Envelope(-42,42,-42,42);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        assertEquals(env, node.getBounds());
    }
    
    @Test
    public void testGetOverlapLeafHasCompleteOverlap(){
        //  The overlap of a leaf is an envelope with the same bounds as the node
        Envelope env = new Envelope(0, 10, 0, 10);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        Envelope toOverlap = new Envelope(0, 1, 0, 1);

        Envelope overlap = node.getOverlap(toOverlap);

        assertEquals(toOverlap, overlap);
        assertTrue(node.isLeaf());
    }
    
    @Test
    public void testGetOverlapLeafHasSomeOverlap(){
        //  The overlap of a leaf is an envelope with the same bounds as the node
        Envelope env = new Envelope(0, 10, 0, 10);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        Envelope toOverlap = new Envelope(-1, 1, -1, 1);

        Envelope overlap = node.getOverlap(toOverlap);

        assertEquals(new Envelope(0,1,0,1), overlap);
        assertTrue(node.isLeaf());
    }
    
    @Test
    public void testGetOverlapLeafHasNoOverlap(){
        //  Create a node
        Envelope env = new Envelope(0, 10, 0, 10);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        // Create a disjoint envelope that we'll expand to cover the lea
        Envelope toExpand = new Envelope(0, -10, 0, -10);

        Envelope overlap = node.getOverlap(toExpand);

        assertEquals(new Envelope(0,0,0,0), overlap);
        assertTrue(node.isLeaf());
    }
    
    @Test
    public void testGetOverlapNonLeafHasSomeOverlap(){
        // Set up children.
        Envelope childEnv = new Envelope(-2,-1,-2,-1);
        HRPlusNode childNode = new HRPlusNode(new ObjectId(), childEnv,new ObjectId());
        HRPlusContainerNode child = new HRPlusContainerNode(new ObjectId());
        child.addNode(childNode);
        // Set up parent. Envelope is disjoint from child.
        Envelope env = new Envelope(0, 10, 0, 10);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env, new ObjectId());
        node.setChild(child);
        // Intersection should be with child's box, not with node's box
        Envelope toIntersect = new Envelope(-5,5,-5,5);
        assertEquals(childEnv, node.getOverlap(toIntersect));
        assertFalse(node.isLeaf());
    }
    
    @Test
    public void testGetOverlapNonLeafHasNoOverlap(){
        // Set up children.
        Envelope childEnv = new Envelope(-2,-1,-2,-1);
        HRPlusNode childNode = new HRPlusNode(new ObjectId(), childEnv,new ObjectId());
        HRPlusContainerNode child = new HRPlusContainerNode(new ObjectId());
        child.addNode(childNode);
        // Set up parent. Envelope is disjoint from child.
        Envelope env = new Envelope(0, 10, 0, 10);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        node.setChild(child);
        // Intersection should be with child's box, not with node's box.
        // In this case, the child does not intersect and the node is an exact match
        assertEquals(new Envelope(), node.getOverlap(env));
        assertFalse(node.isLeaf());
    }
    
    @Test
    public void testQueryLeafSuccess(){
        Envelope env = new Envelope(-5,5,-5,5);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        
        node.query(new Envelope(-6,-4,-6,-4), matches);
        
        assertEquals(1, matches.size());
        assertEquals(node, matches.get(0));
        assertTrue(node.isLeaf());
    }
    
    @Test
    public void testQueryLeafFailure(){
        Envelope env = new Envelope(-5,5,-5,5);
        HRPlusNode node = new HRPlusNode(new ObjectId(), env,new ObjectId());
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        
        node.query(new Envelope(-6,-6,-6,-6), matches);
        
        assertEquals(0, matches.size());
        assertTrue(node.isLeaf());
    }
    

    @Test
    public void testQueryNodeSuccess(){
        // Set up children.
        HRPlusNode childA = new HRPlusNode(ObjectId.forString("node 1"), new Envelope(-2,-3,-2,-3),ObjectId.forString("v1"));
        HRPlusNode childB = new HRPlusNode(ObjectId.forString("node 2"), new Envelope(2,3,2,3),ObjectId.forString("v1"));
        HRPlusNode childC = new HRPlusNode(ObjectId.forString("node 3"), new Envelope(-2,-3,2,3),ObjectId.forString("v1"));
       
        HRPlusContainerNode child = new HRPlusContainerNode(ObjectId.forString("v1"));
        child.addNode(childA); 
        child.addNode(childB); 
        child.addNode(childC);
        // Set up parent.
        HRPlusNode node = new HRPlusNode(ObjectId.forString("parent node"), new Envelope(2,3,-2,-3),ObjectId.forString("v1"));
        node.setChild(child);
        // Execute query
        List<HRPlusNode> matches = new ArrayList<HRPlusNode>();
        Envelope env = new Envelope(-5,5,-5,5);
        node.query(env, matches);
        
        assertEquals(4, matches.size());
        assertTrue(matches.contains(node));
        assertTrue(matches.contains(childA));
        assertTrue(matches.contains(childB));
        assertTrue(matches.contains(childC));
        assertFalse(node.isLeaf());
    }
  
  /*  @Test
    public void testGetType(){
        // TODO implement that method!
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope());
        assertEquals(null, node.getType());
    }
    
    @Test
    public void testGetId(){
        // TODO implement that method!
        HRPlusNode node = new HRPlusNode(new ObjectId(), new Envelope());
        assertEquals(null, node.getId());
    }*/
}