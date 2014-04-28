package org.geogit.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

public class HRPlusContainerNode implements RevObject {

	// Map of nodes inhabiting this container
	private Map<ObjectId, HRPlusNode> nodeMap = new HashMap<ObjectId, HRPlusNode>();
	// Self and parent ids
	// TODO these are never set!
	private ObjectId objectId;
	private ObjectId parentId;

	private ObjectId versionId;

	/*
	 * /** Get all the layer ids contained by this container node and all
	 * sub-nodes.
	 * 
	 * @return
	 */
	/*
	 * public List<ObjectId> getLayerIds(){ // Should this rather be a set? To
	 * avoid duplicates. List<ObjectId> layerIds = new ArrayList<ObjectId>();
	 * for(HRPlusNode node : this.nodeMap.values()){
	 * layerIds.addAll(node.getLayerIds()); } return layerIds; }
	 */

	// TODO: Assuming that a container is associated with only 1 version id
	public HRPlusContainerNode(ObjectId versionId) {
		this.versionId = versionId;
		// TODO: Set its own object id, and parentId
	}

	public ObjectId getObjectId() {
		return this.objectId;
	}

	public ObjectId getParentId() {
		return this.parentId;
	}

	public ObjectId getVersionId() {
		return this.versionId;
	}

	public int getNumNodes() {
		return this.nodeMap.size();
	}

	public void addNode(HRPlusNode node) {
		nodeMap.put(node.getObjectId(), node);
	}

	public HRPlusNode removeNode(ObjectId objectId) {
		return nodeMap.remove(objectId);
	}

	public List<HRPlusNode> getNodes() {
		return new ArrayList<HRPlusNode>(this.nodeMap.values());
	}

	/**
	 * @param layerId
	 * @return true if every node in this container and below contains @param
	 *         layerId
	 */
	/*
	 * public boolean allNodesContainLayerId(ObjectId layerId){ // if all
	 * entries are from the current layerId boolean allTrue = true;
	 * for(HRPlusNode node : this.nodeMap.values()){ allTrue = allTrue &&
	 * (node.getLayerIds() != null && node.getLayerIds().contains(layerId)); }
	 * return allTrue; }
	 */

	/**
	 * @param layerId
	 * @return a list of nodes that exist in @param layerId
	 */
	/*
	 * public List<HRPlusNode> getNodesForLayer(ObjectId layerId){
	 * List<HRPlusNode> nodesForLayer = new ArrayList<HRPlusNode>();
	 * for(HRPlusNode node : this.nodeMap.values()){
	 * if(node.getLayerIds().contains(layerId)){ nodesForLayer.add(node); } }
	 * return nodesForLayer; }
	 */

	/**
	 * Determine whether this container is a leaf. A container may be non-empty
	 * but still be a leaf.
	 * 
	 * @return
	 */
	public boolean isLeaf() {
		// HRPlus tree is balanced by construction, so if one contained node has
		// no children,
		// they all do and this container is a leaf
		return this.nodeMap.isEmpty() || this.getNodes().get(0).isLeaf();
	}

	/**
	 * Check if the container does not have any nodes
	 */
	public boolean isEmpty() {
		return (this.nodeMap.isEmpty());
	}

	/**
	 * Check whether any children of this node are leaves.
	 * 
	 * @return
	 */
	public boolean isOneStepAboveLeafLevel() {
		if (this.isLeaf()) {
			return false;
		}
		// Not a leaf, so getNodes will return a non-empty list.
		HRPlusContainerNode nextLevel = this.getNodes().get(0).getChild();
		return nextLevel.isLeaf();
	}

	/**
	 * Compute the minimum bounding rectangle for nodes in this tree.
	 * 
	 * @return
	 */
	public Envelope getMBR() {
		Envelope env = new Envelope();
		for (HRPlusNode node : nodeMap.values()) {
			node.expand(env);
		}
		return env;
	}

	/**
	 * @param env
	 * @return the envelope obtained by intersecting @param env with this
	 *         container's MBR
	 */
	public Envelope getOverlap(Envelope env) {
		return this.getMBR().intersection(env);
	}

	/**
	 * Search this container for nodes within the envelope @param env and
	 * recurse into their containers.
	 * 
	 * @param env
	 * @param matches
	 *            list of nodes across the entire tree that fit in this envelope
	 */
	public void query(Envelope env, List<HRPlusNode> matches) {
		if (this.getMBR().intersects(env)) {
			for (HRPlusNode n : this.getNodes()) {
				n.query(env, matches);
			}
		}
		return;
	}

	/**
	 * Return all the nodes in this container and all the nodes in the child
	 * containers
	 * 
	 * @return all the nodes below this container
	 */
	public List<HRPlusNode> getNodesForContainer() {
		List<HRPlusNode> nodes = new ArrayList<HRPlusNode>();
		// add all the nodes in this container
		nodes.addAll(this.getNodes());

		if (this.isLeaf()) // if its a leaf we are done, otherwise recurse
			return nodes;
		else {
			for (HRPlusNode node : this.getNodes()) {
				// get the nodes for child container this node points to
				nodes.addAll(node.getChild().getNodesForContainer());
			}
		}
		return nodes;
	}

	
	@Override
	public TYPE getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectId getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
