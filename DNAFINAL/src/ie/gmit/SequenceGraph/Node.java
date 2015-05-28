package ie.gmit.SequenceGraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Node implements Comparable<Node> {

	protected int dimension;
	public String label;

	@Override
	public int compareTo(Node o) {

		return this.getLabel().compareTo(o.getLabel());
	}

	int offset = 0;

	protected boolean inRepeat = false;

	// The size of the label
	public int size = 0;

	protected TreeSet<Node> in = new TreeSet<Node>();

	protected TreeSet<Node> out = new TreeSet<Node>();

	Node followThis = null;

	protected boolean mark = false;

	public Node(int dimension) {

		this.dimension = dimension;
	}

	public void addNext(Node n) {

		out.add(n);
	}

	public void addPrevious(Node n) {

		in.add(n);
	}

	
	void connectTo(Node nextNode) {

		this.out.add(nextNode);
		nextNode.in.add(this);

	}

	void unbindTo(Node nextNode) {

		this.out.remove(nextNode);
		nextNode.in.remove(this);
	}

	public boolean compare(Node other) {
		return this.label == other.label;
	}

	void cut(int point, boolean first) {

		Node followThis = new Node(dimension);
		String newLabel = null;

		// CUT LEFT
		if (first) {

			followThis.size = point + dimension - 1;

			followThis.label = label.substring(0, followThis.size);
			newLabel = label.substring(point, size);
			this.size = size - point;
		}
		// CUT RIGHT
		else {
			followThis.size = point + dimension;
			followThis.label = label.substring(0, followThis.size);
			newLabel = label.substring((point + 1), size);
			this.size = size - point - 1;
		}

		for (Node n : in) {
			n.out.remove(this);
			n.out.add(followThis);
		}

		this.label = newLabel;
		followThis.followThis = this.followThis;
		followThis.offset = this.offset;
		this.offset -= first ? point : (point + 1);

		TreeSet<Node> tmp = followThis.in;
		followThis.in = this.in;
		this.in = tmp;

		followThis.connectTo(this);

		this.followThis = followThis;

		followThis.mark = this.mark;
		followThis.inRepeat = this.inRepeat;
		

	}

	public String getLabel() {

		return label;
	}

	public Set<Node> getNexts() {

		return out;
	}

	public Set<Node> getPrevious() {

		return in;
	}

	public int inDegree() {

		return in.size();
	}

	public boolean inRepeat() {

		return inRepeat;
	}

	public boolean isMarked() {

		return mark;
	}

	public boolean isSink() {

		return outDegree() == 0;
	}

	public boolean isSource() {

		return inDegree() == 0;
	}

	public void mark() {

		mark = true;
	}

	public void markRepeat() {

		inRepeat = true;
	}

	public String nextLabel() {

		if (out.size() > 0)
			return ((TreeSet<Node>) out).first().getLabel();
		return null;
	}

	public int offset() {

		return offset;
	}

	public int outDegree() {

		return out.size();
	}

	public String previousLabel() {

		if (in.size() > 0)
			return in.floor(this).getLabel();
		return null;
	}

	public void removeNext(Node n) {

		out.remove(n);
	}

	public void removePrevious(Node n) {

		in.remove(n);
	}

	void setLabel(String label) {

		this.label = label;
		this.size = label.length();
	}

	/*@Override
	public String toString() {

		String in = "[ ";
		for (Node n : this.in)
			in += n == null ? "null " : n.label + " ";
		in += "]";

		String out = "[ ";
		for (Node n : this.out)
			out += n == null ? "null " : n.label + " ";
		out += "]";

		return "\n" + in + " " + label + " " + label.length() + " " + out;
	}*/

	public void unmark() {

		mark = false;
	}

	public Node next() {

		for (Node node : out)
			if (node != null)
				return node;
		return null;
	}

	public boolean preceeds(Node n) {

		for (Node node : out)
			if (node == n)
				return true;
		return false;
	}

}
