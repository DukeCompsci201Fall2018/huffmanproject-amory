import java.util.PriorityQueue;

/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

import java.util.PriorityQueue;

public class HuffProcessor {

/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private final int myDebugLevel;

	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;

	public HuffProcessor() {
		this(0);
	}

	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){
		
		int[] counts = readForCounts(in);
		HuffNode root = makeTreeFromCounts(counts);
		String [] codings = makeCodingsFromTree(root);
		System.out.println(codings);
		
		out.writeBits(BITS_PER_INT, HUFF_TREE);
		writeReader(root, out);
		
		in.reset();
		writeCompressedBits(codings, in, out);
		out.close();
	}
	/*
	 * 
	 */
	private void writeCompressedBits(String[] codings, BitInputStream in, BitOutputStream out) {
		while(true) {
			int c = in.readBits(BITS_PER_WORD);
			if(c == -1) {
				break;
			}
			String code = codings[c];
			out.writeBits(code.length(), Integer.parseInt(code,2));
		}
		String coder = codings[PSEUDO_EOF];
		out.writeBits(coder.length(), Integer.parseInt(coder,2));
		
	}
	
	private void writeReader(HuffNode root, BitOutputStream out) {
		if(root.myRight == null && root.myLeft == null) {
			out.writeBits(1,1);
			out.writeBits(BITS_PER_WORD +1, root.myValue);
			return;
		}
		out.writeBits(1, 0);
		writeReader(root.myLeft, out);
		writeReader(root.myRight, out);
	}

	private String[] makeCodingsFromTree(HuffNode root) {
		String[] codings = new String[ALPH_SIZE + 1];
	    codingHelper(root,"",codings);
	    return codings;

		
	}

	private String [] codingHelper(HuffNode root, String string, String[] encodings) {
		if (root.myRight == null && root.myLeft == null) {
	        encodings[root.myValue] = string;
	        System.out.println(encodings);
	   }
		else {
			codingHelper(root.myRight, string + "1", encodings);
			codingHelper(root.myLeft, string + "0", encodings);
		}
		
		return encodings;
	}

	private HuffNode makeTreeFromCounts(int[] counts) {
		PriorityQueue<HuffNode> cue = new PriorityQueue<>();
		
		for(int i = 0; i < counts.length; i ++) {
			if (counts[i] > 0) {
			    cue.add(new HuffNode(i,counts[i]));

			}
		}
		
		cue.add(new HuffNode(PSEUDO_EOF, 1));
		while (cue.size() > 1) {
		    HuffNode r = cue.remove();
		    HuffNode l = cue.remove();

		    HuffNode t = new HuffNode(-1, r.myWeight + l.myWeight, l, r);
		    cue.add(t);
		}
		HuffNode rt = cue.remove();
		return rt;
	}

	private int[] readForCounts(BitInputStream in) {
		int [] iaminsane = new int [ALPH_SIZE + 1];
		iaminsane[PSEUDO_EOF] = 1;
		while (true) {
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1) {
				break;
			}
			iaminsane[val]++;
		}
		return iaminsane;
	}

	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){

		int bits = in.readBits(BITS_PER_INT);
		if (bits !=HUFF_TREE) {
			throw new HuffException("Illegal header starts with" + bits);
		}
		HuffNode root = readTreeReader(in);
		readCompressedBits(root, in, out);
		out.close();
	
	}

	private HuffNode readTreeReader(BitInputStream in) {
		int b = in.readBits(1);
		if (b == -1) {
			throw new HuffException("Illegal header starts with" + b);
		}
		if (b == 0) {
			HuffNode l = readTreeReader(in);
			HuffNode r = readTreeReader(in);
			return new HuffNode(0,0,l,r);
		}
		else {
			int value = in.readBits(BITS_PER_WORD+1);
			return new HuffNode(value,0,null,null);
		}
	}

	private void readCompressedBits(HuffNode root, BitInputStream in, BitOutputStream out) {
		HuffNode curr = root; 
		   while (true) {
		       int b = in.readBits(1);
		       if (b == -1) {
		           throw new HuffException("bad input, no PSEUDO_EOF");
		       }
		       else { 
		           if (b == 0) curr = curr.myLeft;
		      else curr = curr.myRight;

		           if (curr.myLeft == null && curr.myRight == null) {
		               if (curr.myValue == PSEUDO_EOF) 
		                   break;   
		               else {
		                   out.writeBits(8, curr.myValue);
		                   curr = root; 
		               }
		           }
		       }
		   }


	}
} 

