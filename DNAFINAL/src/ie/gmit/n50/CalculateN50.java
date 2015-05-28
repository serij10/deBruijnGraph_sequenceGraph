package ie.gmit.n50;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class CalculateN50 {
	private final double nxPosition; // default

	public CalculateN50() {
		this(0.5);
	}

	public CalculateN50(double position) {
		this.nxPosition = position;
	}

	private void printResults(PrintStream out, List<Integer> revSortedList, long total) {
		int tCnt = revSortedList.size();
		long tSum = total;
		// for(Integer i : revSortedList){
		// tSum += i;
		// }
		double cOff = ((double) tSum) * this.nxPosition;

		long sum = 0;
		int i = 0;
		Integer sLen = 0;
		for (i = 0; i < tCnt; ++i) {
			sLen = revSortedList.get(i);
			sum += sLen;
			if (sum >= cOff) {
				break;
			}
		}
		out.println("N" + Double.valueOf(this.nxPosition * 100).intValue() + ":\t" + sLen + "\t(" + (i + 1) + " sequences)" + "\t(" + sum + " bp combined)");
	}

	public void calculate(String min,int minSequence,File file) throws IOException{
		List<CalculateN50> nxList = new ArrayList<CalculateN50>();
		nxList.add(new CalculateN50());
		ParseFasta pfa = new ParseFasta();
		long genomeSize = -1;
		nxList.add(new CalculateN50(Double.valueOf(50) / 100));
		List<File> list = new ArrayList<File>();
		list.add(file);
		boolean isStream = false;
		long gSize = 0;
		for (File f : list) {
			try {
				if (f.getName().equals("-")) {
					isStream = true;
					pfa.setIn(System.in);
				} else {
					pfa.setIn(openFile(f));
				}
				pfa.process();
				// TODO process results
				System.out.println("Results for " + f);
				System.out.println("Total number of sequences:          " + pfa.getLen().size());
				System.out.println("Total length of sequences:          " + pfa.getTotalCnt() + " bp");
				// Sort
				Collections.sort(pfa.getLen());
				System.out.println("Shortest sequence length :          " + (pfa.getLen().isEmpty() ? 0 : pfa.getLen().get(0)) + " bp");

				// Reverse
				Collections.reverse(pfa.getLen());
				System.out.println("Longest sequence length  :          " + (pfa.getLen().isEmpty() ? 0 : pfa.getLen().get(0)) + " bp");

				gSize = 0;
				if (genomeSize < 0) {
					gSize = pfa.getTotalCnt();
				} else {
					gSize = genomeSize;
					System.out.println("-> with a provided genome size of:  " + gSize + " bp");
				}
				System.out.println("Total number of Ns in sequences:    " + pfa.getNsCnt());
				/*for (CalculateN50 nx : nxList) {
					nx.printResults(System.out, pfa.getLen(), gSize);
				}*/
				nxList.get(0).printResults(System.out, pfa.getLen(), gSize);
				if (!isStream) {
					pfa.getIn().close();
				}
				pfa.setIn(null);
			} finally {
				if (pfa.getIn() != null) {
					try {
						pfa.getIn().close();
					} catch (Exception e) {
						// ignore
					}
					pfa.setIn(null);
				}
			}
			System.out.println("");
			pfa.reset();
		}
	}
	
	private static InputStream openFile(File f) throws IOException {
		InputStream in = new FileInputStream(f);
		if (f.getName().endsWith(".gz") || f.getName().endsWith(".gzip")) {
			in = new GZIPInputStream(in);
		}
		in = new BufferedInputStream(in);
		return in;
	}

	private static void fail(String msg) {
		System.err.println(msg);
		System.exit(1);
	}

}
