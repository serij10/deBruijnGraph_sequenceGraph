package ie.gmit.SequenceGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Graph {

	protected int dimension;

	Map<String, Connector> map = new HashMap<String, Connector>();

	public final TreeSet<Node> nodes;

	protected Graph(int dimension, Map<String, Connector> map, TreeSet<Node> multinodes) {

		this.dimension = dimension;
		this.map = map;
		this.nodes = multinodes;
	}

	public Graph(int dimension) {

		this.dimension = dimension;
		this.map = new HashMap<String, Connector>();
		this.nodes = new TreeSet<Node>();
	}

	public void addSequence(Sequence sequence) {

		// Return if sequence is shorter then the tuple

		if (sequence.getLength() <= dimension)
			return;

		// The sequence spectrum Array
		String[] spectrum = new String[sequence.getLength() - dimension + 1];

		// supporting node.
		Node n = null;

		// An empty connector used in getConnectors method path
		Connector auxiliarConnector = new Connector(Integer.MAX_VALUE, null);

		// The connectors of to the l-tuples in the sequence in the sequence.
		// an empty array of connectors of size spectrum.length
		Connector[] cPath = new Connector[sequence.getLength() - dimension + 1];

		// The nodes containing the sequence
		ArrayList<Node> nodePath = new ArrayList<Node>();

		int index = 0;
		// getting the sequence spectrum from the sequence
		for (String label : sequence.spectrum(dimension))
			spectrum[index++] = label;

		getConnectors(auxiliarConnector, spectrum, cPath, 0, spectrum.length);

		// Creating new Nodes
		
		index = 0;
		
		int startingPoint;
		// The position of the spectrum where a contiguous region of nodes of
		// the same type (new / old).
		while (index < spectrum.length) {

			// Entered a region of the spectrum that is not found in the  graph.
			if (cPath[index].isNew()) {
				
				startingPoint = index;
				// mark connectors as visited -other negative number than -1 from creation (index < -1)
				// 
				while (index < spectrum.length && cPath[index].isNew())

				{
					
					cPath[index].index -= 1;
					index++;

				}
				/*
				 * we reached end of spectrum or there is repeat in the sequence
				 * or connector points to old node
				 * the new region ends here
				 * we can create new node				 
				 * 
				 * 
				 */
				n = new Node(dimension);
				n.size = index - startingPoint + dimension - 1;

				
				n.label = spectrum[startingPoint];
				cPath[startingPoint].index = 0;

				cPath[startingPoint].node = n;

				int nodeIndex = 1;
				for (int i = startingPoint + 1; i < index; i++) {

					n.label += spectrum[i].substring(spectrum[i].length() - 1);
					cPath[i].index = nodeIndex++;
					cPath[i].node = n;
				}
			
				nodes.add(n);
			}
			if (index < spectrum.length) {

				// Visiting part of the spectrum that is already in the map
				
				startingPoint = index;				
				n = cPath[index].node();
				int position = cPath[index].index();
				
				index = startingPoint;
				/*
				 * contiguous(path, i, j) Let path be an array of connectors.
				 * This boolean func- tion returns true if the connectors in
				 * path[i . . . j] correspond to a substring of a node.*
				 * 
				 * index = min(x : x ≥ start ∧ ¬contiguous(cPath, start, x))
				 */

				while (index < spectrum.length && !cPath[index].isNew()
						&& (cPath[index] == auxiliarConnector || (cPath[index].node() == n && index - startingPoint == cPath[index].index() - position))) {

					index++;

				}
				//4 Cases of node being cut			
				/* Case A b is not the leftmost tuple(!cPath[startingPoint].isFirst())				
				 *  CUT LEFT
				 *  
				 */
				if (startingPoint > 0 && !cPath[startingPoint].isFirst()) {
					
					nodes.remove(n);
					n.cut(cPath[startingPoint].offset(), true);
					nodes.add(n);
					nodes.add(n.followThis);

				}
				
				/*
				 * CASE B
				 * CUT Right 
				 * a is not the rightmost tuple of n
				 * cut it to make it so
				 *
				 */
				else if (index != spectrum.length && !cPath[index - 1].isLast(dimension)) {
					
					nodes.remove(n);
					n.cut(cPath[index - 1].offset(), false);
					nodes.add(n);
					nodes.add(n.followThis);
					n = n.followThis;

				}
				/* CASE C
				 * tuple is the leftmost in the string to be inserted
				 * but not in the node
				 * CUT LEFT to make the tuple in N the leftmost  
				 * 
				 */
				
				else if (startingPoint == 0 && index != spectrum.length) {
					
					nodes.remove(n);
					n.cut(cPath[startingPoint].offset(), true);
					nodes.add(n);
					nodes.add(n.followThis);
				}
				/*
				 * Case D CUT Right
				 * new tuple is the right most in the new sequence (index == spectrum.length)
				 * the corresponding one in the old one is not (!cPath[index - 1].isLast(dimension))
				 * node has to cut so that it will be
				 */
			
				
				else if (startingPoint != 0 && index == spectrum.length && !cPath[index - 1].isLast(dimension)) {
					// System.out.println("CASE D");
					nodes.remove(n);
					n.cut(cPath[index - 1].offset(), false);
					nodes.add(n);
					nodes.add(n.followThis);
					n = n.followThis;
				} //
				n.mark();
			} // if (index < spectrum.length)
		}// while (index < spectrum.length)

		// NODE PATH update Connectors
	
		getConnectors(auxiliarConnector, spectrum, cPath, 0, sequence.getLength() - dimension + 1);
		n = cPath[0].node();
		startingPoint = 0;
		int position = cPath[0].index();
		nodePath.add(n);
		for (int i = 1; i < spectrum.length; i++)
			if (cPath[i] != auxiliarConnector && (cPath[i].node() != n || cPath[i].index() - position != i - startingPoint)) {
				n = cPath[i].node();
				startingPoint = i;
				position = cPath[i].index();
				nodePath.add(n);
			}

		// CONNECTING THE PATH
		n = nodePath.get(0);
		Node next = null;
		for (int i = 1; i < nodePath.size(); i++) {
			next = nodePath.get(i);
			n.connectTo(next);
			n = next;
		}

	}
	
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	private void getConnectors(Connector auxiliarConnector, String[] spectrum, Connector[] connPath, int i, int j) {

		Connector connector;

		String[] spectrumArray = spectrum;
		int index = i;
		while (index < j) {
			connector = map.get(spectrumArray[index]);
			if (connector == null) {
				do {
					// Add new pair to the map-tuple from the spectrum and new
					// empty connector
				
					map.put(spectrum[index], (connector = connPath[index] = new Connector(-1, null)));
					connPath[index] = connector;

				} while (index < j && connector == null);

			}
			if (index < j) {

				
				connPath[index] = connector;
				

				if (connector.node != null) {
					String label = connector.node().label;
				
					String auxIndex = label.substring(0, (connector.offset() + dimension));
					
					int i2 = 0;
					while ((++index < j) && auxIndex == spectrum[index]) {
						connPath[index] = auxiliarConnector;
						auxIndex = label.substring(i2++, (connector.offset() + dimension));

					}
					connPath[index - 1] = map.get(spectrum[index - 1]);
				} else
					index++;

			}
		}

	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		for (Node n : nodes)
			sb.append(n.toString());
		return sb.toString();
	}

	public long size() {

		return nodes.size();
	}

	
}
