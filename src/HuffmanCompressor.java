/* EECS 233 Programming Project 2
 * Daniel Grigsby 
 * 
 * Compresses a file into an array of HuffmanNodes, places them into a Huffman tree, and encodes a text file
 * Then calculates the estimated storage saving percent using real bits, and prints the Huffman table
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HuffmanCompressor { 
	// Fields

	// The value for the HashMmap is a List of Longs containing the frequency and bits, the key is the character
	static Map<Character, List<Long>> table = new HashMap<Character, List<Long>>();
	/* I chose 255 for the array size based on what characters of UTF-8 I would expect to find. */ 
	public static final int charsToRecognize = 255;

	/* Reads and compresses an input file, and outputs it to a different file. */
	public static String huffmanCoder(String inputFileName, String outputFileName) {
		tableGenerator(huffEncoder(nodeGen(inputFileName)),1l);
		System.out.print(calcSavings(inputFileName, outputFileName));
		return "Process Completed"; // If it fails, then one of the exception catchers will print an error. Otherwise this line.
	}

	/* Scans text file and creates a arrayList of HuffmanNodes. Returns the ArrayList sorted by frequency 
	 * I chose an ArrayList because we would be best suited with random list accesses for insertion, however,
	 * I believe a Linked List would be more efficient, because there would be no wasted memory in the shrinking ArrayList
	 * I didn't do a LinkedList because having two sets of left,right pointers would be harder for the reader to understand.  */
	public static ArrayList<HuffmanNode> nodeGen(String fileName){
		int[] freqTable = new int[charsToRecognize]; 
		try {
			// These next few lines read the char frequencies into an array of ints that we will later use as characters with type casting
			BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			int c = 0;
			while((c = buff.read()) != -1){
				freqTable[c]++;
			}
			buff.close();
		}
		catch(ArrayIndexOutOfBoundsException e){
			System.out.println("ERROR: There are weird special characters in the text file (Line 50)");
		}
		catch(FileNotFoundException e){
			System.out.println("ERROR: File not found (Line 53): " + fileName);
		}
		catch (IOException e) {
			System.out.println("ERROR: Error reading file (Line 56): " + fileName);
		}
		ArrayList<HuffmanNode> huffList = new ArrayList<HuffmanNode>();
		for(int i = 0; i < freqTable.length; i++){
			if(freqTable[i] > 0) 
				huffList.add(new HuffmanNode((char)i, freqTable[i], null, null));
		}
		Collections.sort(huffList);
		System.out.println(" \nThe size of the Huffman Table is: " + huffList.size());
		return huffList;
	}

	/* Assembles a Huffman tree from a sorted ArrayList of HuffmanNodes and returns the root
	 * I believe his would work better with a priority queue , but the directions say ArrayList or LinkedList */
	public static HuffmanNode huffEncoder(ArrayList<HuffmanNode> list){
		// Merge array into tree
		while(list.size() > 1){
			Collections.sort(list); // O(k) complexity since max size is 255! Not that bad
			HuffmanNode left = list.remove(0);
			HuffmanNode right = list.remove(0);
			HuffmanNode root = new HuffmanNode(' ', left.frequency + right.frequency, left, right);
			list.add(root);	
		}
		return list.remove(0); 
	}

	/* Recursively generates a table (HashMap) of encoded values for the characters in the tree */
	public static void tableGenerator(HuffmanNode root, Long count) throws NumberFormatException{
		try{
			if(root.isLeafNode()){
				List<Long> temp = new ArrayList<Long>();
				temp.add((Long.valueOf(root.frequency)));
				temp.add(count);
				table.put(root.inChar,temp);
				// System.out.println(root.inChar + " : " + temp.get(1) + " : " + temp.get(0)); // debugging
			}
			else { // Ugly method of appending 0 or 1 to the end of a Double
				tableGenerator(root.left,Long.valueOf(String.valueOf(count) + String.valueOf(0)));
				tableGenerator(root.right,Long.valueOf(String.valueOf(count) + String.valueOf(1)));
			}
		}
		catch (NumberFormatException e){
			System.out.println("ERROR: The text file is too long to process, max is 9.9E8 characters (Line 98)");
		}
	}

	/* Scans the input file, encodes it, and calculates the storage savings. 
	 * Calculation assumes we are using traditional bits, instead of 0 and 1 characters to store data.
	 * Estimates savings by calculating average bits per character, and comparing it to 8, as used in UTF-8 */
	public static String calcSavings(String fileName, String outputFileName){
		try {
			BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			StringBuilder entireFile = new StringBuilder();
			int c = 0;
			while((c = buff.read()) != -1){
				entireFile.append(table.get((char)c).get(1));
			}
			buff.close();
			BufferedWriter bwriter =  new BufferedWriter(new FileWriter(new File(outputFileName)));
			bwriter.write(entireFile.toString());
			bwriter.close();
		}
		catch(NullPointerException e){
			System.out.println("The above error(s) resulted in the table being unable to form. \n\t\t The following table is invalid.");
		}
		catch(FileNotFoundException e){
			System.out.println("ERROR: File not found (Line 122): " + fileName);
		}
		catch (IOException e) {
			System.out.println("Error reading file (Line 125): " + fileName);
		}
		// Everything below this line is just getting a nice looking formatted output and calculating Saved bits. 
		// This is more efficient than incrementing a counter every time we append a character to the output file. Several dozen calculations instead of xx thousand 
		double freqSum = 0;
		double bitsPerCharacter = 0;
		StringBuilder output = new StringBuilder();
		StringBuilder encodingTable = new StringBuilder();
		for (Character n: table.keySet()){
			freqSum+=table.get(n).get(0);
			encodingTable.append(n.toString()+ " : " + table.get(n).get(0) + " : " + table.get(n).get(1).toString() + "\n"); 
			bitsPerCharacter+=(table.get(n).get(0))*table.get(n).get(1).toString().length();
		}
		bitsPerCharacter/=freqSum;
		double savings = 1-bitsPerCharacter/8;
		bitsPerCharacter = Math.floor(bitsPerCharacter * 100) / 100; 
		savings = Math.floor(savings * 1000) / 1000;
		output.append("Input: " + fileName + ", Output: " + outputFileName + "\nTotal Characters: " + (int)freqSum);
		output.append("\nBits per character: " + bitsPerCharacter + "\nEstimated savings: " + savings*100 + "% \n"  + "\n \nchar : frequency : bits \n" + encodingTable);
		try {
			BufferedWriter tableWriter =  new BufferedWriter(new FileWriter(new File("Table" +outputFileName)));
			tableWriter.write(output.toString());
			tableWriter.close();
		}catch(NullPointerException e){
			System.out.println("The above error(s) resulted in the table being unable to form. \n\t\t The following table is invalid.");
		}
		catch(FileNotFoundException e){
			System.out.println("ERROR: File not found (Line 156): " + "Table" +outputFileName);
		}
		catch (IOException e) {
			System.out.println("Error reading file (Line 159): " + "Table" +outputFileName);
		}
		return output.toString();
	}

	/* Main: Executes the program on specified input and output files names */
	public static void main(String[] args){
		// Run with any text file name you want, or used the commented out default
		// HuffmanCompressor.huffmanCoder("Gutenberg.txt", "EncodedGutenberg.txt");
		if(args.length != 2)
			System.out.println("Incorrect amount of arguments");
		HuffmanCompressor.huffmanCoder(args[0],args[1]);
	}
}
