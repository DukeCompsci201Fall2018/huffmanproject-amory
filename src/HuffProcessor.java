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

public class HuffProcessor {

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
		int[] c = readForCounts(in);
		HuffNode rt = makeTreeFromCounts(c);
		String[] codings = makeCodingsFromTree(rt);
		out.writeBits(BITS_PER_INT, HUFF_TREE);
		writeHeader(rt, out);
		in.reset();
		writeCompressedBits(codings, in, out);
		out.close();
	}
	private void writeCompressedBits(String[] codings, BitInputStream in, BitOutputStream out) {
		// TODO Auto-generated method stub
		
	}

	private void writeHeader(HuffNode root, BitOutputStream out) {
		// TODO Auto-generated method stub
		
	}

	private String[] makeCodingsFromTree(HuffNode root) {
		// TODO Auto-generated method stub
		return null;
	}

	private HuffNode makeTreeFromCounts(int[] counts) {
		PriorityQueue<HuffNode> cue = new PriorityQueue<>();
		counts[PSEUDO_EOF] = 1;
		for (int i = 0; i < counts.length; i += 1) {
			if (counts[i] != 0) {
				cue.add(new HuffNode(i, counts[i], null, null));
			}
		}
		while (cue.size() > 1) {
			HuffNode l = cue.remove();
			HuffNode r = cue.remove();
			//make a new HuffNode t with weight from
			//l.weight+r.weight and l, r subtrees
			HuffNode t = new HuffNode(-1, l.myWeight + r.myWeight, l, r);
		    cue.add(t);
		}
		HuffNode root = cue.remove();
		return root;
	}

	private int[] readForCounts(BitInputStream in) {
		// TODO Auto-generated method stub
		return null;
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
		int b = in.readBits(BITS_PER_INT);
		if (b != HUFF_TREE) {
			throw new HuffException("illegal header starts with " + b);
		}
		HuffNode root = readTreeHeader(in);
		readCompressedBits(root, in, out);
		out.close();		
	}
	
	private void readCompressedBits(HuffNode root, BitInputStream in, BitOutputStream out) {
		HuffNode curr = root;
		while (true) {
			int b = in.readBits(1);
			if (b == -1) {
				throw new HuffException("bad input, no PSEUDO_EOF");
			}
			else {
				if (b == 0)
					curr = curr.myLeft;
				else
					curr = curr.myRight;
				if (b == 1) {
					if (curr.myValue == PSEUDO_EOF) 
						break;
					else {
						out.writeBits(BITS_PER_WORD, curr.myValue);
						curr = root;
					}
				}
			}
		}
		
	}
    //helper method for reading the tree header
	private HuffNode readTreeHeader(BitInputStream in) {
		int b = in.readBits(1);
		if (b == -1)
			throw new HuffException("out of bits in reading tree header");
		if (b == 0) {
			HuffNode l = readTreeHeader(in);
			HuffNode r = readTreeHeader(in);
			return new HuffNode(0, 0, l, r);
		}
		return null;
	}

	
}