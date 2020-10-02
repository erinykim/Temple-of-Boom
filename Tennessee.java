package student;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import game.EscapeState;
import game.ExploreState;
import game.Explorer;
import game.Node;
import game.NodeStatus;

public class Tennessee extends Explorer {
    /** Get to the orb in as few steps as possible. Once you get there, 
     * you must return from the function in order to pick
     * it up. If you continue to move after finding the orb rather 
     * than returning, it will not count.
     * If you return from this function while not standing on top of the orb, 
     * it will count as a failure.
     * 
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the orb in fewer steps.
     * 
     * At every step, you know only your current tile's ID and the ID of all 
     * open neighbor tiles, as well as the distance to the orb at each of these tiles
     * (ignoring walls and obstacles). 
     * 
     * In order to get information about the current state, use functions
     * currentLocation(), neighbors(), and distanceToOrb() in ExploreState.
     * You know you are standing on the orb when distanceToOrb() is 0.
     * 
     * Use function moveTo(long id) in ExploreState to move to a neighboring 
     * tile by its ID. Doing this will change state to reflect your new position.
     * 
     * A suggested first implementation that will always find the orb, but likely won't
     * receive a large bonus multiplier, is a depth-first search.*/
	List<Long> visited = new ArrayList<Long>();
	List<Long> history = new ArrayList<Long>();
	List<Long> intersect = new ArrayList<Long>();
	@Override public void getOrb(ExploreState state) {
        //TODO : Get the orb
    	visited.add(state.currentLocation());
    	heapDfs(state);
    	return;
    	
    }
	/** This is a depth-first-search for a min-heap. It collects information on the adjacent neighbors
	 * as a min-heap. When Tennessee encounters an intersection, it records the intersection tile id
	 * so that it can go back to that tile when he encounters a block. This method is also responsible
	 * for moving Tennessee from tile to tile.
	 */
    public void heapDfs(ExploreState state){
	    	int unvisited = unvisit(state);
	    	Heap<NodeStatus> neighborHeap = new Heap<NodeStatus>();
	    	
	    	neighborHeap = unvisitedHeap(state);
	    	if(state.distanceToOrb()!= 0){
		    	if(unvisited > 0 ){
		    		if(unvisited > 1 ){
		    			intersect.add(state.currentLocation());
		    		}
			    	while(neighborHeap.size()> 0){
			    		NodeStatus cur_neighbors = neighborHeap.poll();
			    		if(!visited.contains(cur_neighbors.getId())){
			    			visited.add(cur_neighbors.getId());
			    			history.add(cur_neighbors.getId());
			    			state.moveTo(cur_neighbors.getId());
			    			heapDfs(state);
				    		if(state.distanceToOrb()== 0){
				    			return;
				    		}
			    			if(unvisit(state) == 0){
			    				returnToIntersect(state);
			    			}
			    		}
			    	}
			    } else{
			    	returnToIntersect(state);
			    }
	    	}
    	return;
    }
    /** This method helps Tennessee to back-track to the intersection that he previously encountered.
     *  It first makes a sub array of the path from the intersection point to the current tile that
     *  he is standing on. Then, it reverses that sub array to travel back to its last intersection point.
     */
    private void returnToIntersect(ExploreState state){
		if(intersect.size()> 0){
			Long last_intersect = intersect.get(intersect.size()-1);
			int cur_index = history.lastIndexOf(state.currentLocation());
			int last_index = history.lastIndexOf(last_intersect.longValue());
			List<Long> sub_path = new ArrayList<Long>(history.subList(last_index, cur_index));
			Collections.reverse(sub_path);
			for(Long id : sub_path){
				history.remove(last_index+1);
				state.moveTo(id);
				if(id.equals(last_intersect.longValue())){
					if(unvisit(state) == 0){
						intersect.remove(intersect.size()-1);
					}
				}
			}
		}
    }
    /** This is a helper method just for creating a heap of unvisited tiles to be used in 
     * the heapDfs method. 
     */
    private Heap<NodeStatus> unvisitedHeap(ExploreState state){
    	Heap<NodeStatus> heapNeighbor = new Heap<NodeStatus>();
    	for(NodeStatus cur_neighbor : state.neighbors()){
    		if(!visited.contains(cur_neighbor.getId())){
    			heapNeighbor.add(cur_neighbor, cur_neighbor.getDistanceToTarget());
    		}
    	}
    	return heapNeighbor;
    }
    
    /** This is a helper method to keep track of the number of unvisited tiles as Tennessee moves.
     */
    private int unvisit(ExploreState state){
    	int result = 0;
    	for(NodeStatus cur_neighbors:state.neighbors()){
    		if(!visited.contains(cur_neighbors.getId())){
    			result++;
    		}
    	}
    	return result;
    }
    
    /** Get out the cavern before the ceiling collapses, trying to collect as much
     * gold as possible along the way. Your solution must ALWAYS get out before time runs
     * out, and this should be prioritized above collecting gold.
     * 
     * You now have access to the entire underlying graph, which can be accessed through EscapeState.
     * currentNode() and getExit() will return Node objects of interest, and getNodes()
     * will return a collection of all nodes on the graph. 
     * 
     * Note that the cavern will collapse in the number of steps given by stepsRemaining(),
     * and for each step this number is decremented by the weight of the edge taken. You can use
     * stepsRemaining() to get the time still remaining, seizeGold() to pick up any gold
     * on your current tile (this will fail if no such gold exists), and moveTo() to move
     * to a destination node adjacent to your current node.
     * 
     * You must return from this function while standing at the exit. Failing to do so before time
     * runs out or returning from the wrong location will be considered a failed run.
     * 
     * You will always have enough time to escape using the shortest path from the starting
     * position to the exit, although this will not collect much gold. For this reason, using 
     * Dijkstra's to plot the shortest path to the exit is a good starting solution. */
    @Override public void getOut(EscapeState state) {
        //TODO: Escape from the cavern before time runs out
    	if (state.currentNode().getTile().getGold() > 0) {
    		state.seizeGold();
    	}
    	while (stepsToExit(state) < state.stepsRemaining()){
    		//look for gold
    		Collection<Node> neighbors = state.currentNode().getNeighbors();
    		Node exit = state.getExit();
    		int max_Gold = Integer.MIN_VALUE;
    		Node max_Neighbor = null;
    		int max_Neighbor_step = 0;
    		for (Node neighbor: neighbors) {
    			int cur_Gold = neighbor.getTile().getGold();
    			if (cur_Gold > max_Gold) {
    				max_Gold = cur_Gold;
    				max_Neighbor = neighbor;
    				max_Neighbor_step = max_Neighbor.getEdge(state.currentNode()).length;
    			}
    		}
    		
    		if (max_Gold != 0 && ((stepsToExit(state) + max_Neighbor_step*2) < state.stepsRemaining())) {
	    		state.moveTo(max_Neighbor);
	    		if (state.currentNode().getTile().getGold() >0) {
	    			state.seizeGold();
	    		}
    		}
    		else {
    			List<Node> path = Paths.dijkstra(state.currentNode(), exit);
    			if (state.currentNode().equals(state.getExit())) {
    				return;
    			}
    			if (path.size() >=2) {
    				state.moveTo(path.get(1));
    			}
    			else {
    				state.moveTo(path.get(0));
    			}
    			if (state.currentNode().getTile().getGold()>0){
    				state.seizeGold();
    			}
    		}	
    	}
    	goToExit(state);
    }
    
    /* This helper method makes Tennessee move to the exit while picking up any gold on the way.
     * It uses the dijkstra's method in class Paths.
     */
    private void goToExit(EscapeState state) {
    	List<Node> path = Paths.dijkstra(state.currentNode(), state.getExit());
		if (state.currentNode().getTile().getGold()>0) {
			state.seizeGold();
		}			
    	path.remove(0);
    	
    	for (Node n: path) {
    		state.moveTo(n);
    		if (n.getTile().getGold()>0) {
    			state.seizeGold();
    		}
    	}
    }
    
    
    /* This method calculates the steps left that Tennessee has until he reaches the exit.
     * It uses the dijkstra's method in class Paths.
     */
    private int stepsToExit(EscapeState state) {
    	List<Node> path = Paths.dijkstra(state.currentNode(), state.getExit());
    	int result = 0;
    	for (int i = 0; i < path.size()-1; i++) {
    		Node cur_n = path.get(i);
    		Node next_n = path.get(i+1);
    		result = result+cur_n.getEdge(next_n).length;
    	}
    	return result;
    	
    }
}
