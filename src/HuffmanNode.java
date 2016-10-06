/* EECS 233 Programming Project 2
 * Daniel Grigsby
 * 
 * A HuffmanNode, Comparable and with appropriate pointers/fields */
public class HuffmanNode implements Comparable<HuffmanNode> {
	
	// Fields
	public Character inChar;
	HuffmanNode left;
 	HuffmanNode right;
	int frequency; 
	
	// Constructors
	public HuffmanNode(Character inChar, int frequency, HuffmanNode left, HuffmanNode right){
		this.inChar = inChar;
		this.frequency = frequency;
		this.left = left;
		this.right = right; 
	}
	
	// Methods
	/* Checks if the node is a leaf, so we can tell if it represents a real character */
	public boolean isLeafNode(){
		return left == null && right == null;
	}
	
	/* Allows us to determine relative frequencies of characters */ 
	@Override
	public int compareTo(HuffmanNode node) {
		return this.frequency - node.frequency;
	}
}