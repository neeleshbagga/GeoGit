package org.geogit.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.geogit.api.RevObject.TYPE;
import org.junit.Test;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Class for testing the the HRPlusTree
 * 
 * @author neelesh
 * 
 */

public class HRPlusTreeTest {

	// assuming insert works - TODO: test insert(going into same container+diff
	// container)
	private HRPlusTree initHRPlusTree() {
		return new HRPlusTree();
	}

	@Test
	public void testGetNodes() {
		HRPlusTree tree = new HRPlusTree();
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		ObjectId id1 = ObjectId.forString("building1");
		ObjectId id2 = ObjectId.forString("building2");
		ObjectId id3 = ObjectId.forString("building3");
		ObjectId versionId1 = ObjectId.forString("Version1");
		// Feature Nodes in Version 1
		Envelope a1 = new Envelope(-12, -10, -2, 2);
		Envelope b1 = new Envelope(12, -10, -2, 2);
		Envelope c1 = new Envelope(-10, 12, -2, 2);

		HRPlusNode nodeA1 = new HRPlusNode(id1, a1, versionId1);
		HRPlusNode nodeB1 = new HRPlusNode(id2, b1, versionId1);
		;
		HRPlusNode nodeC1 = new HRPlusNode(id3, c1, versionId1);
		;

		tree.insert(id1, a1, versionId1);
		tree.insert(id2, b1, versionId1);
		tree.insert(id3, c1, versionId1);
		nodes.add(nodeA1);
		nodes.add(nodeB1);
		nodes.add(nodeC1);

		List<HRPlusNode> result = tree.getNodes();
		assertEquals(nodes.size(), result.size());
		assertTrue(result.containsAll(nodes));

	}

	@Test
	public void testgetNumRoots() {
		HRPlusTree tree = new HRPlusTree();
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		// 1 Feature Node per Version (4 versions)
		Envelope a1 = new Envelope(-12, -10, -2, 2);
		Envelope a2 = new Envelope(12, -10, -2, 2);
		Envelope a3 = new Envelope(-10, 12, -2, 2);
		Envelope a4 = new Envelope(12, 10, -2, 2);
		ObjectId id1 = ObjectId.forString("building1");
		ObjectId id2 = ObjectId.forString("building2");
		ObjectId id3 = ObjectId.forString("building3");
		ObjectId id4 = ObjectId.forString("building4");
		ObjectId versionId1 = ObjectId.forString("Version1");
		ObjectId versionId2 = ObjectId.forString("Version2");

		HRPlusNode nodeA1 = new HRPlusNode(id1, a1, versionId1);
		HRPlusNode nodeB1 = new HRPlusNode(id2, a1, versionId1);
		HRPlusNode nodeA2 = new HRPlusNode(id3, a1, versionId2);
		HRPlusNode nodeB2 = new HRPlusNode(id4, a1, versionId2);

		tree.insert(id1, a1, versionId1);
		tree.insert(id2, a1, versionId1);
		tree.insert(id3, a1, versionId2);
		tree.insert(id4, a1, versionId2);

		nodes.add(nodeA1);
		nodes.add(nodeB1);
		nodes.add(nodeA2);
		nodes.add(nodeB2);

		assertEquals(2, tree.getNumRoots());

	}

	@Test
	public void testInsertNodesDiffVersion() {

		// Simulate two different commits with 3 feature nodes each.
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");
		ObjectId versionId2 = ObjectId.forString("Version2");

		// Feature Nodes in Version 1
		Envelope a1 = new Envelope(-12, -10, -2, 2);
		Envelope b1 = new Envelope(-8, -6, -2, 2);
		Envelope c1 = new Envelope(-4, -2, -2, 2);
		ObjectId id1_v1 = ObjectId.forString("building1V1");
		ObjectId id2_v1 = ObjectId.forString("building2V1");
		ObjectId id3_v1 = ObjectId.forString("building3V1");
		HRPlusNode nodeA1 = new HRPlusNode(id1_v1, a1, versionId1);
		HRPlusNode nodeB1 = new HRPlusNode(id2_v1, b1, versionId1);
		HRPlusNode nodeC1 = new HRPlusNode(id3_v1, c1, versionId1);

		// Feature Nodes in Version 2
		Envelope a2 = new Envelope(-14, -12, -2, 2);
		Envelope b2 = new Envelope(-10, -8, -2, 2);
		Envelope c2 = new Envelope(-6, -4, -2, 2);
		ObjectId id1_v2 = ObjectId.forString("building1V2");
		ObjectId id2_v2 = ObjectId.forString("building2V2");
		ObjectId id3_v2 = ObjectId.forString("building3V2");
		HRPlusNode nodeA2 = new HRPlusNode(id1_v2, a2, versionId2);
		HRPlusNode nodeB2 = new HRPlusNode(id2_v2, b2, versionId2);
		HRPlusNode nodeC2 = new HRPlusNode(id3_v2, c2, versionId2);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA1);
		nodes.add(nodeB1);
		nodes.add(nodeC1);
		nodes.add(nodeA2);
		nodes.add(nodeB2);
		nodes.add(nodeC2);

		tree.insert(id1_v1, a1, versionId1);
		tree.insert(id2_v1, b1, versionId1);
		tree.insert(id3_v1, c1, versionId1);
		tree.insert(id1_v2, a2, versionId2);
		tree.insert(id2_v2, b2, versionId2);
		tree.insert(id3_v2, c2, versionId2);

		// Test: Check if tree has all nodes that are inserted.
		List<HRPlusNode> result = tree.getNodes();
		assertEquals(nodes.size(), result.size());
		assertTrue(result.containsAll(nodes));

		// Test: Check if no of entry points = no of versions of the feature
		// type(no of commits)
		assertEquals(2, tree.getNumRoots());

		// Test: Check if nodes have been put into correct version
		List<HRPlusNode> result1 = tree.getNodes(versionId1);
		assertTrue(tree.getNodes(versionId1).containsAll(nodes.subList(0, 3)));
		assertTrue(tree.getNodes(versionId2).containsAll(nodes.subList(3, 6)));
	}

	@Test
	public void insertNodesSameVersion() {
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");

		Envelope a = new Envelope(-12, -10, -2, 2);
		Envelope b = new Envelope(-8, -6, -2, 2);
		Envelope c = new Envelope(-4, -2, -2, 2);

		ObjectId id1_v1 = ObjectId.forString("building1V1");
		ObjectId id2_v1 = ObjectId.forString("building2V1");
		ObjectId id3_v1 = ObjectId.forString("building3V1");
		HRPlusNode nodeA = new HRPlusNode(id1_v1, a, versionId1);
		HRPlusNode nodeB = new HRPlusNode(id2_v1, b, versionId1);
		HRPlusNode nodeC = new HRPlusNode(id3_v1, c, versionId1);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA);
		nodes.add(nodeB);
		nodes.add(nodeC);

		tree.insert(id1_v1, a, versionId1);
		tree.insert(id2_v1, b, versionId1);
		tree.insert(id3_v1, c, versionId1);

		// Test: Check if tree has all nodes that are inserted.
		List<HRPlusNode> result = tree.getNodes();
		assertEquals(nodes.size(), result.size());
		assertTrue(result.containsAll(nodes));

		// Test: Check if no of entry points = no of versions of the feature
		// type(no of commits)
		assertEquals(1, tree.getNumRoots());
	}

	@Test
	public void testOverflow() {
		// Idea - Keep adding nodes belonging to the same version which are very
		// close to each other.

		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");
		ObjectId id1 = ObjectId.forString("building1");
		ObjectId id2 = ObjectId.forString("building2");
		ObjectId id3 = ObjectId.forString("building3");
		ObjectId id4 = ObjectId.forString("building4");
		Envelope a = new Envelope(-12, -10, -2, 2);
		Envelope b = new Envelope(-11, -9, -2, 2);
		Envelope c = new Envelope(-10, -8, -2, 2);
		Envelope d = new Envelope(-9, -7, -2, 2);

		HRPlusNode nodeA = new HRPlusNode(versionId1, a, id1);
		HRPlusNode nodeB = new HRPlusNode(versionId1, b, id2);
		HRPlusNode nodeC = new HRPlusNode(versionId1, c, id3);
		HRPlusNode nodeD = new HRPlusNode(versionId1, d, id4);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA);
		nodes.add(nodeB);
		nodes.add(nodeC);
		nodes.add(nodeD);

		tree.insert(id1, a, versionId1);
		tree.insert(id2, b, versionId1);
		tree.insert(id3, c, versionId1);
		tree.insert(id4, d, versionId1);

		assertEquals(2, tree.getContainersForRoot(versionId1).size());

	}

	public void testOverflow2() {
		// Idea - Keep adding nodes belonging to the same version which are very
		// close to each other.

		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");
		ObjectId id1 = ObjectId.forString("building1");
		ObjectId id2 = ObjectId.forString("building2");
		ObjectId id3 = ObjectId.forString("building3");
		ObjectId id4 = ObjectId.forString("building4");
		ObjectId id5 = ObjectId.forString("building5");
		ObjectId id6 = ObjectId.forString("building6");
		ObjectId id7 = ObjectId.forString("building7");
		ObjectId id8 = ObjectId.forString("building8");
		ObjectId id9 = ObjectId.forString("building9");
		ObjectId id10 = ObjectId.forString("building10");
		ObjectId id11 = ObjectId.forString("building11");
		ObjectId id12 = ObjectId.forString("building12");

		Envelope a = new Envelope(-12, -10, -4, -2);
		Envelope b = new Envelope(-12, -10, 4, 2);
		Envelope c = new Envelope(12, 10, 4, 2);
		Envelope d = new Envelope(12, 10, -4, -2);

		// subsets of the above
		Envelope e = new Envelope(11, 9, 3, 2);
		Envelope f = new Envelope(-11, -9, 3, 2);
		Envelope g = new Envelope(11, 9, -3, -2);
		Envelope h = new Envelope(-11, -9, -3, -2);

		// subsets of the above
		Envelope i = new Envelope(10, 8, 3, 2);
		Envelope j = new Envelope(10, 8, -3, -2);
		Envelope k = new Envelope(-10, -8, 3, 2);
		Envelope l = new Envelope(-10, -8, -3, -2);

		HRPlusNode nodeA = new HRPlusNode(versionId1, a, id1);
		HRPlusNode nodeB = new HRPlusNode(versionId1, b, id2);
		HRPlusNode nodeC = new HRPlusNode(versionId1, c, id3);
		HRPlusNode nodeD = new HRPlusNode(versionId1, d, id4);
		HRPlusNode nodeE = new HRPlusNode(versionId1, e, id5);
		HRPlusNode nodeF = new HRPlusNode(versionId1, f, id6);
		HRPlusNode nodeG = new HRPlusNode(versionId1, g, id7);
		HRPlusNode nodeH = new HRPlusNode(versionId1, h, id8);
		HRPlusNode nodeI = new HRPlusNode(versionId1, i, id9);
		HRPlusNode nodeJ = new HRPlusNode(versionId1, j, id10);
		HRPlusNode nodeK = new HRPlusNode(versionId1, k, id11);
		HRPlusNode nodeL = new HRPlusNode(versionId1, l, id12);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA);
		nodes.add(nodeB);
		nodes.add(nodeC);
		nodes.add(nodeD);
		nodes.add(nodeE);
		nodes.add(nodeF);
		nodes.add(nodeG);
		nodes.add(nodeH);
		nodes.add(nodeI);
		nodes.add(nodeJ);
		nodes.add(nodeK);
		nodes.add(nodeL);

		tree.insert(id1, a, versionId1);
		tree.insert(id2, b, versionId1);
		tree.insert(id3, c, versionId1);
		tree.insert(id4, d, versionId1);
		tree.insert(id5, e, versionId1);
		tree.insert(id6, f, versionId1);
		tree.insert(id7, g, versionId1);
		tree.insert(id8, h, versionId1);
		tree.insert(id9, i, versionId1);
		tree.insert(id10, j, versionId1);
		tree.insert(id11, k, versionId1);
		tree.insert(id12, l, versionId1);

		// should not create more than 2 roots
		assertEquals(2, tree.getContainersForRoot(versionId1).size());

	}

	@Test
	public void testQueries() {
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");

		Envelope a = new Envelope(-12, -10, -2, 2);
		Envelope b = new Envelope(-8, -6, -2, 2);
		Envelope c = new Envelope(-4, -2, -2, 2);

		ObjectId id1_v1 = ObjectId.forString("building1V1");
		ObjectId id2_v1 = ObjectId.forString("building2V1");
		ObjectId id3_v1 = ObjectId.forString("building3V1");
		HRPlusNode nodeA = new HRPlusNode(id1_v1, a, versionId1);
		HRPlusNode nodeB = new HRPlusNode(id2_v1, b, versionId1);
		HRPlusNode nodeC = new HRPlusNode(id3_v1, c, versionId1);

		tree.insert(id1_v1, a, versionId1);
		tree.insert(id2_v1, b, versionId1);
		tree.insert(id3_v1, c, versionId1);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA);
		nodes.add(nodeB);
		nodes.add(nodeC);

		List<HRPlusNode> result = tree.query(new Envelope(-50, 50, -50, 50));
		assertTrue(result.containsAll(nodes));
		assertEquals(nodeA, tree.query(a).get(0));
		assertEquals(nodeB, tree.query(b).get(0));
		assertEquals(nodeC, tree.query(c).get(0));

	}

	@Test
	public void testQueries2() {
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId1 = ObjectId.forString("Version1");

		Envelope a = new Envelope(-10, -5, 5, 10);
		Envelope b = new Envelope(5, 10, 5, 10);
		Envelope c = new Envelope(-10, -5, -5, -10);
		Envelope d = new Envelope(10, 5, -5, -10);

		ObjectId id1_v1 = ObjectId.forString("building1V1");
		ObjectId id2_v1 = ObjectId.forString("building2V1");
		ObjectId id3_v1 = ObjectId.forString("building3V1");
		ObjectId id4_v1 = ObjectId.forString("building4V1");
		HRPlusNode nodeA = new HRPlusNode(id1_v1, a, versionId1);
		HRPlusNode nodeB = new HRPlusNode(id2_v1, b, versionId1);
		HRPlusNode nodeC = new HRPlusNode(id3_v1, c, versionId1);
		HRPlusNode nodeD = new HRPlusNode(id4_v1, d, versionId1);

		tree.insert(id1_v1, a, versionId1);
		tree.insert(id2_v1, b, versionId1);
		tree.insert(id3_v1, c, versionId1);
		tree.insert(id4_v1, d, versionId1);

		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		nodes.add(nodeA);
		nodes.add(nodeB);
		nodes.add(nodeC);
		nodes.add(nodeD);

		List<HRPlusNode> result1 = tree.query(new Envelope(-10, 10, -10, 10));
		List<HRPlusNode> result2 = tree.query(new Envelope(-4, 4, -4, 4));
		List<HRPlusNode> result3 = tree.query(new Envelope(-11, -4, 4, 11));
		List<HRPlusNode> result4 = tree.query(new Envelope(-11, -4, -4, -11));
		List<HRPlusNode> result5 = tree.query(new Envelope(11, 4, 4, 11));
		List<HRPlusNode> result6 = tree.query(new Envelope(11, 4, -4, -11));
		assertTrue(result1.containsAll(nodes));
		assertEquals(0, result2.size());
		assertTrue(nodeA.equals(result3.get(0)) && result3.size() == 1);
		assertTrue(nodeC.equals(result4.get(0)) && result4.size() == 1);
		assertTrue(nodeB.equals(result5.get(0)) && result5.size() == 1);
		assertTrue(nodeD.equals(result6.get(0)) && result6.size() == 1);

	}

	@Test
	public void testQueries16nodes() {
		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId = ObjectId.forString("Version1");

		Envelope[] envelopes = new Envelope[16];
		// Quadrant 1
		envelopes[0] = new Envelope(2, 4, 2, 4);
		envelopes[1] = new Envelope(2, 4, 6, 8);
		envelopes[2] = new Envelope(6, 8, 2, 4);
		envelopes[3] = new Envelope(6, 8, 6, 8);

		// Quadrant 2
		envelopes[4] = new Envelope(-2, -4, 2, 4);
		envelopes[5] = new Envelope(-2, -4, 6, 8);
		envelopes[6] = new Envelope(-6, -8, 2, 4);
		envelopes[7] = new Envelope(-6, -8, 6, 8);
		// Quadrant 3
		envelopes[8] = new Envelope(-2, -4, -2, -4);
		envelopes[9] = new Envelope(-2, -4, -6, -8);
		envelopes[10] = new Envelope(-6, -8, -2, -4);
		envelopes[11] = new Envelope(-6, -8, -6, -8);
		// Quadrant 4
		envelopes[12] = new Envelope(2, 4, -2, -4);
		envelopes[13] = new Envelope(2, 4, -6, -8);
		envelopes[14] = new Envelope(6, 8, -2, -4);
		envelopes[15] = new Envelope(6, 8, -6, -8);

		ObjectId id[] = new ObjectId[16];
		// list of nodes
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();

		// Creating the tree
		for (int i = 0; i < 16; i++) {
			id[i] = ObjectId.forString("building" + i);
			nodes.add(new HRPlusNode(id[i], envelopes[i], versionId));
			tree.insert(id[i], envelopes[i], versionId);
		}

		// 4 simple queries for each quadrant - test the bbox surrounding each
		// quadrant
		List<HRPlusNode> result1 = tree.query(new Envelope(2, 8, 2, 8));
		List<HRPlusNode> result2 = tree.query(new Envelope(-2, -8, 2, 8));
		List<HRPlusNode> result3 = tree.query(new Envelope(-2, -8, -2, -8));
		List<HRPlusNode> result4 = tree.query(new Envelope(2, 8, -2, -8));

		assertTrue(result1.containsAll(nodes.subList(0, 4)));
		assertTrue(result2.containsAll(nodes.subList(4, 8)));
		assertTrue(result3.containsAll(nodes.subList(8, 12)));
		assertTrue(result4.containsAll(nodes.subList(12, 16)));

		// bbox containing nodes in quadrant 1 and 2
		List<HRPlusNode> result5 = tree.query(new Envelope(-8, 8, 2, 8));
		assertTrue(result5.containsAll(nodes.subList(0, 8)));

		// bbox containing nodes in quadrant 3 and 4
		List<HRPlusNode> result6 = tree.query(new Envelope(-8, 8, -2, -8));
		assertTrue(result6.containsAll(nodes.subList(8, 16)));

		// bbox for quad 1 and 4 bbbox
		List<HRPlusNode> result7 = tree.query(new Envelope(2, 8, -8, 8));
		assertTrue(result7.containsAll(nodes.subList(0, 4))
				&& result7.containsAll(nodes.subList(12, 16)));

		// bbox for quad 2 and 3 bbbox
		List<HRPlusNode> result8 = tree.query(new Envelope(-2, -8, -8, 8));
		assertTrue(result8.containsAll(nodes.subList(4, 12)));

		// bbox for central square
		List<HRPlusNode> result9 = tree.query(new Envelope(-4, 4, -4, 4));
		assertTrue(result9.contains(nodes.get(0))
				&& result9.contains(nodes.get(4))
				&& result9.contains(nodes.get(8))
				&& result9.contains(nodes.get(12)));

		// bbox for row 1

		List<HRPlusNode> result10 = tree.query(new Envelope(-8, 8, 4, 8));
		assertTrue(result10.contains(nodes.get(1))
				&& result10.contains(nodes.get(4))
				&& result10.contains(nodes.get(5))
				&& result10.contains(nodes.get(7)));

		// bbox for row 3

		List<HRPlusNode> result11 = tree.query(new Envelope(-8, 8, 0, -6));
		assertTrue(result11.contains(nodes.get(10))
				&& result11.contains(nodes.get(8))
				&& result11.contains(nodes.get(14))
				&& result11.contains(nodes.get(12)));

		// bbox for col 2
		List<HRPlusNode> result12 = tree.query(new Envelope(0, 5, -10, 10));
		assertTrue(result12.contains(nodes.get(0))
				&& result12.contains(nodes.get(1))
				&& result12.contains(nodes.get(13))
				&& result12.contains(nodes.get(12)));

		// box for col 4
		List<HRPlusNode> result13 = tree.query(new Envelope(-5, -10, -12, 12));
		assertTrue(result13.contains(nodes.get(7))
				&& result13.contains(nodes.get(6))
				&& result13.contains(nodes.get(10))
				&& result13.contains(nodes.get(11)));

	}

	@Test
	public void testOverflow16nodes() {

		HRPlusTree tree = new HRPlusTree();
		ObjectId versionId = ObjectId.forString("Version1");

		Envelope[] envelopes = new Envelope[16];
		// Quadrant 1
		envelopes[0] = new Envelope(2, 4, 2, 4);
		envelopes[1] = new Envelope(2, 4, 6, 8);
		envelopes[2] = new Envelope(6, 8, 2, 4);
		envelopes[3] = new Envelope(6, 8, 6, 8);

		// Quadrant 2
		envelopes[4] = new Envelope(-2, -4, 2, 4);
		envelopes[5] = new Envelope(-2, -4, 6, 8);
		envelopes[6] = new Envelope(-6, -8, 2, 4);
		envelopes[7] = new Envelope(-6, -8, 6, 8);
		// Quadrant 3
		envelopes[8] = new Envelope(-2, -4, -2, -4);
		envelopes[9] = new Envelope(-2, -4, -6, -8);
		envelopes[10] = new Envelope(-6, -8, -2, -4);
		envelopes[11] = new Envelope(-6, -8, -6, -8);
		// Quadrant 4
		envelopes[12] = new Envelope(2, 4, -2, -4);
		envelopes[13] = new Envelope(2, 4, -6, -8);
		envelopes[14] = new Envelope(6, 8, -2, -4);
		envelopes[15] = new Envelope(6, 8, -6, -8);

		ObjectId id[] = new ObjectId[16];
		// list of nodes
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();

		// Creating the tree
		for (int i = 0; i < 4; i++) {
			id[i] = ObjectId.forString("building" + i);
			nodes.add(new HRPlusNode(id[i], envelopes[i], versionId));
			tree.insert(id[i], envelopes[i], versionId);
		}
		
		//TODO: go over with ben
		// should not create more than 8 roots
		assertEquals(8, tree.getContainersForRoot(versionId).size());
		
	}

	@Test(expected = IllegalArgumentException.class)
	public void testKeySplitContainerNodeNull() {
		HRPlusTree hr = new HRPlusTree();
		hr.keySplitContainerNode(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testKeySplitContainerNodeTooFewNodes() {
		HRPlusNode nodeA = new HRPlusNode(new ObjectId(), new Envelope(0, 1, 0,
				1), new ObjectId());
		HRPlusContainerNode cont = new HRPlusContainerNode(new ObjectId());
		cont.addNode(nodeA);

		HRPlusTree hr = new HRPlusTree();
		hr.keySplitContainerNode(cont);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testKeySplitContainerNodeTooManyNodes() {
		HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"),
				new Envelope(0, 1, 0, 1), new ObjectId());
		HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"),
				new Envelope(0, 1, 0, 1), new ObjectId());
		HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"),
				new Envelope(0, 1, 0, 1), new ObjectId());
		HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"),
				new Envelope(0, 1, 0, 1), new ObjectId());
		HRPlusNode nodeE = new HRPlusNode(ObjectId.forString("E"),
				new Envelope(0, 1, 0, 1), new ObjectId());

		HRPlusContainerNode cont = new HRPlusContainerNode(new ObjectId());
		cont.addNode(nodeA);
		cont.addNode(nodeB);
		cont.addNode(nodeC);
		cont.addNode(nodeD);
		cont.addNode(nodeE);

		HRPlusTree hr = new HRPlusTree();
		hr.keySplitContainerNode(cont);
	}

	@Test
	public void testKeySplitContainerNodeNonIntersectingSquare() {
		HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"),
				new Envelope(1, 2, 1, 2), new ObjectId());
		HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"),
				new Envelope(-1, -2, 1, 2), new ObjectId());
		HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"),
				new Envelope(-1, -2, -1, -2), new ObjectId());
		HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"),
				new Envelope(1, 2, -1, -2), new ObjectId());

		HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
		contA.addNode(nodeA);
		contA.addNode(nodeB);
		contA.addNode(nodeC);
		contA.addNode(nodeD);

		HRPlusTree hr = new HRPlusTree();
		HRPlusContainerNode contB = hr.keySplitContainerNode(contA);

		assertEquals(2, contA.getNumNodes());
		assertEquals(2, contB.getNumNodes());
		assertEquals(new Envelope(1, 2, -2, 2), contA.getMBR());
		assertEquals(new Envelope(-1, -2, -2, 2), contB.getMBR());
	}

	@Test
	public void testKeySplitContainerNodeNonIntersectingLine() {
		HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"),
				new Envelope(1, 2, 1, 2), new ObjectId());
		HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"),
				new Envelope(3, 4, 1, 2), new ObjectId());
		HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"),
				new Envelope(5, 6, 1, 2), new ObjectId());
		HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"),
				new Envelope(7, 8, 1, 2), new ObjectId());

		HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
		contA.addNode(nodeA);
		contA.addNode(nodeB);
		contA.addNode(nodeC);
		contA.addNode(nodeD);

		HRPlusTree hr = new HRPlusTree();
		HRPlusContainerNode contB = hr.keySplitContainerNode(contA);

		assertEquals(3, contA.getNumNodes());
		assertEquals(1, contB.getNumNodes());
		assertEquals(new Envelope(3, 8, 1, 2), contA.getMBR());
		assertEquals(new Envelope(1, 2, 1, 2), contB.getMBR());
	}

	@Test
	public void testKeySplitContainerNodeFullyOverlappingSquare() {
		HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"),
				new Envelope(1, 2, 1, 2), new ObjectId());
		HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"),
				new Envelope(1, 2, 1, 2), new ObjectId());
		HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"),
				new Envelope(1, 2, 1, 2), new ObjectId());
		HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"),
				new Envelope(1, 2, 1, 2), new ObjectId());

		HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
		contA.addNode(nodeA);
		contA.addNode(nodeB);
		contA.addNode(nodeC);
		contA.addNode(nodeD);

		HRPlusTree hr = new HRPlusTree();
		HRPlusContainerNode contB = hr.keySplitContainerNode(contA);

		assertEquals(3, contA.getNumNodes());
		assertEquals(1, contB.getNumNodes());
		assertEquals(new Envelope(1, 2, 1, 2), contA.getMBR());
		assertEquals(new Envelope(1, 2, 1, 2), contB.getMBR());
	}

	@Test
	public void testKeySplitContainerNodeParallelOverlapOne() {
		HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"),
				new Envelope(-10, -4, -10, 10), new ObjectId());
		HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"),
				new Envelope(-3, 3, -10, 10), new ObjectId());
		HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"),
				new Envelope(4, 10, -10, 10), new ObjectId());
		HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"),
				new Envelope(-11, -5, 8, 10), new ObjectId());

		HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
		contA.addNode(nodeA);
		contA.addNode(nodeB);
		contA.addNode(nodeC);
		contA.addNode(nodeD);

		HRPlusTree hr = new HRPlusTree();
		HRPlusContainerNode contB = hr.keySplitContainerNode(contA);

		assertEquals(2, contA.getNumNodes());
		assertEquals(2, contB.getNumNodes());
		assertEquals(new Envelope(-3, 10, -10, 10), contA.getMBR());
		assertEquals(new Envelope(-11, -4, -10, 10), contB.getMBR());
	}

	@Test
	public void testKeySplitContainerNodeParallelOverlapTwo() {
		HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"),
				new Envelope(-10, -4, -10, 10), new ObjectId());
		HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"),
				new Envelope(-3, 3, -10, 10), new ObjectId());
		HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"),
				new Envelope(4, 10, -10, 10), new ObjectId());
		HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"),
				new Envelope(-11, 4, 8, 10), new ObjectId());

		HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
		contA.addNode(nodeA);
		contA.addNode(nodeB);
		contA.addNode(nodeC);
		contA.addNode(nodeD);

		HRPlusTree hr = new HRPlusTree();
		HRPlusContainerNode contB = hr.keySplitContainerNode(contA);

		assertEquals(1, contA.getNumNodes());
		assertEquals(3, contB.getNumNodes());
		assertEquals(new Envelope(4, 10, -10, 10), contA.getMBR());
		assertEquals(new Envelope(-11, 4, -10, 10), contB.getMBR());
	}

	@Test
	public void testKeySplitContainerNodeParallelOverlapThree() {
		HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"),
				new Envelope(-10, -4, -10, 10), new ObjectId());
		HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"),
				new Envelope(-3, 3, -10, 10), new ObjectId());
		HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"),
				new Envelope(4, 10, -10, 10), new ObjectId());
		HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"),
				new Envelope(-11, 11, 8, 10), new ObjectId());

		HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
		contA.addNode(nodeA);
		contA.addNode(nodeB);
		contA.addNode(nodeC);
		contA.addNode(nodeD);

		HRPlusTree hr = new HRPlusTree();
		HRPlusContainerNode contB = hr.keySplitContainerNode(contA);

		assertEquals(3, contA.getNumNodes());
		assertEquals(1, contB.getNumNodes());
		assertEquals(new Envelope(-10, 10, -10, 10), contA.getMBR());
		assertEquals(new Envelope(-11, 11, 8, 10), contB.getMBR());
	}

	@Test
	public void testKeySplitContainerNodePairsOfIntersectingSquares() {
		HRPlusNode nodeA = new HRPlusNode(ObjectId.forString("A"),
				new Envelope(-10, -8, 6, 8), new ObjectId());
		HRPlusNode nodeB = new HRPlusNode(ObjectId.forString("B"),
				new Envelope(-9, -7, 5, 7), new ObjectId());
		HRPlusNode nodeC = new HRPlusNode(ObjectId.forString("C"),
				new Envelope(10, 8, -6, -8), new ObjectId());
		HRPlusNode nodeD = new HRPlusNode(ObjectId.forString("D"),
				new Envelope(9, 7, -5, -7), new ObjectId());

		HRPlusContainerNode contA = new HRPlusContainerNode(new ObjectId());
		contA.addNode(nodeA);
		contA.addNode(nodeB);
		contA.addNode(nodeC);
		contA.addNode(nodeD);

		HRPlusTree hr = new HRPlusTree();
		HRPlusContainerNode contB = hr.keySplitContainerNode(contA);

		assertEquals(2, contA.getNumNodes());
		assertEquals(2, contB.getNumNodes());
		assertEquals(new Envelope(7, 10, -5, -8), contA.getMBR());
		assertEquals(new Envelope(-10, -7, 5, 8), contB.getMBR());
	}

}
