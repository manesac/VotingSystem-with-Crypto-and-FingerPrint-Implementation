package com.me.util;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class FingerPrint {

	public enum direction {
		NONE, HORIZONTAL, VERTICAL, POSITIVE, NEGATIVE
	};

	boolean binMap[][];  // Binary picture
	int[][] greyMap; 	// Grey level picture
	int greymean; 	   // Global mean of greylevel map

	int width;       // Dimensions
	int height;

	BufferedImage originalImage; // Original image

	public static void main(String[] args) throws IOException {
//		System.out.println("fingerprint 101");
//		for (int i = 4; i < 6; i++) {
//			(new FingerPrint(new File("fingerprint/101_"+i+".jpg"))).execute();
//		}
//		
//		System.out.println("fingerprint 102");
//		for (int i = 1; i < 4; i++) {
//			(new FingerPrint(new File("fingerprint/102_"+i+".jpg"))).execute();
//		}
		
//		System.out.println("fingerprint 105");
//		(new FingerPrint(new File("fingerprint/105_1.jpg"))).execute();
//		(new FingerPrint(new File("fingerprint/105_2.jpg"))).execute();
//		
//		System.out.println("fingerprint 107");
//		(new FingerPrint(new File("fingerprint/107_1.jpg"))).execute();
////		(new FingerPrint(new File("fingerprint/107_2.jpg"))).execute();
//		(new FingerPrint(new File("fingerprint/107_3.jpg"))).execute();
		
//		System.out.println("fingerprint 108");
//		for (int i = 2; i < 9; i++) {
//			(new FingerPrint(new File("fingerprint/108_"+i+".jpg"))).execute();
//		}
		for (int i = 1; i < 11; i++) {
			System.out.println("fingerprint 10"+i);
			for (int j = 1; j < 9; j++) {
				System.out.print(j+" => ");
				(new FingerPrint(ImageIO.read(new File("fp/"+(100+i)+"_"+j+".jpg")))).execute();
			}
			
		}
	}
	
	/** Constructor
	 * image file is passed 
	 * and respective grayScale image is found alone with its global mean
	 */
	public FingerPrint(BufferedImage bf) {

		originalImage = bf;

		width = originalImage.getWidth();
		height = originalImage.getHeight();

		greyMap = new int[width][height];
		binMap = new boolean[width][height];

		int curColor;
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				curColor = originalImage.getRGB(i, j);
				int R = (curColor >> 16) & 0xFF;
				int G = (curColor >> 8) & 0xFF;
				int B = (curColor) & 0xFF;

				int greyVal = (R + G + B) / 3;
				greyMap[i][j] = greyVal;
			}
		}
		greymean = getGreylevelMean(greyMap, width, height);
	}
	
	/**
	 *  Complete Execution process of FingerPrint 
	 *  Provides the minute value of number of End Pont and Intersection Point in array
	 */
	public int[] execute(){
		binarizeLocalMean();
		skeletonize();
		Point core = getCore(getDirections());
		int[] minute = getMinutiae(core, width/4-5);
//		for (int i : minute) {
//			System.out.print(i+"   ");
//		}
//		System.out.println();
		return minute;	
	}

	/**
	 * Calculate binarized image value(0,1) saved in boolean 
	 */
	public void binarizeLocalMean() {
		// Variables
		int windowSize = 20;
		int step = 20;
		float localGreyMean;
		int ik, jk;

		// Iterate on the binary matrix
		for (int i = 0; i < width; i += step) {
			for (int j = 0; j < height; j += step) {
				// Get local grey seum
				localGreyMean = 0;
				ik = 0;
				jk = 0;

				for (ik = i; ik < (i + windowSize); ++ik) {
					if (ik >= width)
						break;

					for (jk = j; jk < (j + windowSize); ++jk) {
						if (jk >= height)
							break;

						localGreyMean += greyMap[ik][jk];
					}
				}

				// Calculate the mean
				if (jk * ik != 0) {
					localGreyMean = localGreyMean / ((ik - i) * (jk - j));
				}

				// If the local grey mean is too high (too permissive)
				// we take the global greymean
				if (localGreyMean > greymean) {
					localGreyMean = 0.75f * greymean + 0.25f * localGreyMean;
				}

				// Binarize all the pixels in the window
				for (ik = i; ik < (i + windowSize); ++ik) {
					if (ik >= width)
						break;

					// Get greymean
					for (jk = j; jk < (j + windowSize); ++jk) {
						if (jk >= height)
							break;

						binMap[ik][jk] = !(greyMap[ik][jk] > (localGreyMean));
					}
				}
			}
		}
	}

	/**
	 * Use the Zhang-Suen algorithm on the binary picture to find thinned image
	 */
	public void skeletonize() {
		// Bounds
		int fstLin = 1;
		int lstLin = width - 1;
		int fstCol = 1;
		int lstCol = height - 1;

		// Variables
		boolean[][] prevM = new boolean[width][height];
		;
		boolean[][] newM = new boolean[width][height];
		;
		boolean[] neighbors;
		int A, B;

		// Initialize
		copyMatrix(binMap, prevM);

		// We skeletonize until there are no changes between two iterations
		while (true) {
			copyMatrix(prevM, newM);

			// First subiteration, for NW and SE neigbors
			for (int i = fstLin; i < lstLin; ++i) {
				for (int j = fstCol; j < lstCol; ++j) {
					neighbors = getNeigbors(newM, i, j);

					// Get the decision values
					B = getSum(neighbors);
					A = getTransitions(neighbors);

					// Decide if we remove the pixel
					if ((B >= 2) && (B <= 6)) {
						if (A == 1) {
							if ((neighbors[0] && neighbors[2] && neighbors[4]) == false) {
								if ((neighbors[2] && neighbors[4] && neighbors[5]) == false) {
									newM[i][j] = false;
								}
							}
						}
					}
				}
			}

			// Second subiteration, for NE and SW neigbors
			for (int i = fstLin; i < lstLin; ++i) {
				for (int j = fstCol; j < lstCol; ++j) {
					neighbors = getNeigbors(newM, i, j);

					// Get the decision values
					B = getSum(neighbors);
					A = getTransitions(neighbors);

					// Decide if we remove the pixel
					if ((B >= 2) && (B <= 6)) {
						if (A == 1) {
							if ((neighbors[0] && neighbors[2] && neighbors[6]) == false) {
								if ((neighbors[0] && neighbors[4] && neighbors[6]) == false) {
									newM[i][j] = false;
								}
							}
						}
					}
				}
			}

			// Stop conditions
			if (equal(newM, prevM, width, height)) {
				break;
			} else {
				copyMatrix(newM, prevM);
			}
		}

		// Return matrix
		copyMatrix(newM, binMap);
	}
	
	/**
	 * Calculate and return the directions of ridges (for each pixel)
	 * check if its horizontal(-),vertical(|), positive(\), negative(/)
	 */
	public direction[][] getDirections() {
		// Direction patterns
		direction[][] dirMatrix = new direction[width][height];

		int minI = 1;
		int maxI = width - 2;
		int minJ = 1;
		int maxJ = height - 2;

		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				if ((binMap[i][j] == false) || (i < minI) || (i > maxI) || (j < minJ) || (j > maxJ))
					dirMatrix[i][j] = direction.NONE;
				else if ((binMap[i - 1][j + 1] == true) && (binMap[i + 1][j - 1] == true))
					dirMatrix[i][j] = direction.POSITIVE;
				else if ((binMap[i - 1][j - 1] == true) && (binMap[i + 1][j + 1] == true))
					dirMatrix[i][j] = direction.NEGATIVE;
				else if ((binMap[i][j - 1] == true) && (binMap[i][j + 1] == true))
					dirMatrix[i][j] = direction.VERTICAL;
				else if ((binMap[i - 1][j] == true) && (binMap[i + 1][j] == true))
					dirMatrix[i][j] = direction.HORIZONTAL;
				else
					dirMatrix[i][j] = direction.NONE;
			}
		}

		return dirMatrix;
	}

	/**
	 * gets center point of the fingerprint
	 */
	public Point getCore(direction[][] dirMatrix) {

		class coreInfos {
			private int nbVer, nbHor, nbPos, nbNeg;

			public float getIndex() {

				float perVer, perHor, perPos, perNeg;
				float total = nbVer + nbHor + nbPos + nbNeg;
				float res;

				if (total == 0)
					return 1;

				perVer = nbVer / total;
				perHor = nbHor / total;
				perPos = nbPos / total;
				perNeg = nbNeg / total;

				res = Math.abs(perVer - .25f) + Math.abs(perHor - .25f) + Math.abs(perPos - .25f)
						+ Math.abs(perNeg - .25f);

				return res;
			}

			public void reset() {
				nbVer = 0;
				nbHor = 0;
				nbPos = 0;
				nbNeg = 0;
			}

			public void copyFrom(coreInfos r) {
				nbVer = r.nbVer;
				nbHor = r.nbHor;
				nbPos = r.nbPos;
				nbNeg = r.nbNeg;
			}

			// Increment values
			public void incVertical() {
				++nbVer;
			}

			public void incHorizontal() {
				++nbHor;
			}

			public void incPositive() {
				++nbPos;
			}

			public void incNegative() {
				++nbNeg;
			}
		}

		// Variables
		Point core = new Point();
		int windowSize = width / 8;

		int minIK, maxIK, minJK, maxJK;

		coreInfos bestCandidate = new coreInfos();
		coreInfos currentCandidate = new coreInfos();

		// Bounds
		int minI = windowSize;
		int maxI = width - windowSize;
		int minJ = windowSize;
		int maxJ = height - windowSize;

		bestCandidate.reset();

		// Iterate on the picture
		for (int i = minI; i < maxI; ++i) {
			for (int j = minJ; j < maxJ; ++j) {
				// Reset current infos
				currentCandidate.reset();

				minIK = i - windowSize;
				maxIK = i + windowSize;
				minJK = j - windowSize;
				maxJK = j + windowSize;

				// Calculate direction proportions
				for (int ik = minIK; ik < maxIK; ++ik) {
					for (int jk = minJK; jk < maxJK; ++jk) {
						// Increment the good value
						switch (dirMatrix[ik][jk]) {
						case HORIZONTAL:
							currentCandidate.incHorizontal();
							break;

						case POSITIVE:
							currentCandidate.incPositive();
							break;

						case NEGATIVE:
							currentCandidate.incNegative();
							break;

						case VERTICAL:
							currentCandidate.incVertical();
							break;
						}
					}
				}

				// Check if we keep the core
				if (currentCandidate.getIndex() <= bestCandidate.getIndex()) {
					bestCandidate.copyFrom(currentCandidate);
					core.x = i;
					core.y = j;
				}
			}
		}
		return core;
	}
	
	/**
	 * Calculate the no of End Points and Intersection Points
	 */
	public int[] getMinutiae(Point core, int coreRadius) {
		// Variables
		ArrayList<Point> minutiae = new ArrayList<Point>();
		int nbOnNeighbors;
		boolean[] neighbors;
		Point currentPoint;
		int[] minute = new int[2];

		// Define bounds
		int minI = core.x - coreRadius;
		int maxI = core.x + coreRadius;
		int minJ = core.y - coreRadius;
		int maxJ = core.y + coreRadius;

		if (minI < 1)
			minI = 1;

		if (maxI > width - 2)
			maxI = width - 2;

		if (minJ < 1)
			minJ = 1;

		if (maxJ > height - 2)
			maxJ = height - 2;

		// Iterate on binary picture
		for (int i = minI; i < maxI; ++i) {
			for (int j = minJ; j < maxJ; ++j) {
				currentPoint = new Point(i, j);

				if (getDistance(currentPoint, core) < coreRadius) {
					// Get neighbors status
					neighbors = getNeigbors(binMap, i, j);
					// Calculate Rutovitz’s Crossing-Number
					nbOnNeighbors = getSum(getFourNeigbors(i, j));

					if ((binMap[i][j] == true) && ((nbOnNeighbors == 3) || (nbOnNeighbors == 4))) {
						minute[0]++;
					}

					if ((binMap[i][j] == true) && (nbOnNeighbors == 1) && ((neighbors[0] == true)
							|| (neighbors[2] == true) || (neighbors[4] == true) || (neighbors[6] == true))) {
						minute[1]++;
					}
				}
			}
		}

		return minute;
	}

	private boolean[] getFourNeigbors(int i, int j) {
		boolean[] neighbors = new boolean[4];
		neighbors[0] = binMap[i + 0][j - 1];
		neighbors[1] = binMap[i + 1][j + 0];
		neighbors[2] = binMap[i + 0][j + 1];
		neighbors[3] = binMap[i - 1][j + 0];

		return neighbors;
	}

	private int getGreylevelMean(int[][] greymap, int w, int h) {
		int total = 0;
		for (int i = 0; i < w; ++i) {
			for (int j = 0; j < h; ++j) {
				total += greymap[i][j];
			}
		}

		return total / (w * h);
	}

	private int booleanToInt(boolean b) {
		return (b == true) ? 1 : 0;
	}

	private void copyMatrix(boolean[][] src, boolean[][] dst) {
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				dst[i][j] = src[i][j];
			}
		}
	}

	private boolean[] getNeigbors(boolean[][] mat, int i, int j) {
		boolean[] neigbors = new boolean[8];

		neigbors[0] = mat[i - 1][j + 0];
		neigbors[1] = mat[i - 1][j + 1];
		neigbors[2] = mat[i + 0][j + 1];
		neigbors[3] = mat[i + 1][j + 1];
		neigbors[4] = mat[i + 1][j + 0];
		neigbors[5] = mat[i + 1][j - 1];
		neigbors[6] = mat[i + 0][j - 1];
		neigbors[7] = mat[i - 1][j - 1];

		return neigbors;
	}

	private float getDistance(Point a, Point b) {
		int deltaX = b.x - a.x;
		int deltaY = b.y - a.y;

		return (float) Math.sqrt(Math.abs(deltaX * deltaX) + Math.abs(deltaY * deltaY));
	}

	private int getTransitions(boolean[] neighbors) {
		int nbTransitions = 0;

		for (int k = 0; k < 7; ++k) {
			if ((neighbors[k] == false) && ((neighbors[k + 1] == true)))
				++nbTransitions;
		}

		if ((neighbors[7] == false) && ((neighbors[0] == true)))
			++nbTransitions;

		return nbTransitions;
	}

	// Rutovitz’s Crossing-Number
	private int getSum(boolean[] vals) {
		int max = vals.length;
		int sum = 0;

		for (int k = 0; k < max; ++k) {
			sum += booleanToInt(vals[k]);
		}

		return sum;
	}

	// Indicates if two boolean matrices are equals
	private boolean equal(boolean[][] A, boolean[][] B, int w, int h) {
		for (int i = 0; i < w; ++i) {
			for (int j = 0; j < h; ++j) {
				// If a value is different, matrices are different
				if (A[i][j] != B[i][j])
					return false;
			}
		}

		return true;
	}
}
