package contentBasedImageRetrieval_BE_Project;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Object.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class readImage {

	int imageCount = 0;
	double colorCodeMatrix1[][] = new double[1000][256];
	double imageSizeList1[] = new double[1000];
	double textureMatrix1[][] = new double[1000][16];

	double coOccurenceMatrix[][];

	double colorCodeMatrix2[][] = new double[1000][6];
	double imageSizeList2[] = new double[1000];
	double textureMatrix2[][] = new double[1000][6];

	private static float q_h = 16;
	private static float q_s = 4;
	private static float q_v = 4;
	int[] histogram = new int[(int) (q_h * q_s * q_v)];

	public readImage() {
		BufferedImage image = null;
		while (imageCount < 1000) {
			try {
				String filename = "C:\\Users\\saura_000\\Desktop\\image.orig\\" + imageCount + ".jpg";
				image = ImageIO.read(new File(filename));
				int width = image.getWidth();
				int height = image.getHeight();
				imageSizeList1[imageCount] = width * height;
				imageSizeList2[imageCount] = width * height;
				getColorCode1(image, height, width);
				getColorCode2(image, height, width);
				getTexture1(image, height, width);
				getTexture2(image, height, width);
				imageCount++;
			} catch (IOException e) {
				System.out.println("Error occurred when reading the file.");
			}
		}
		writeColorCode1();
		writeColorCode2();
		writeTexture1();
		writeTexture2();
		writeSize1();
		writeSize2();
	}

	public void getTexture2(BufferedImage image, int height, int width) {
		double r[][] = new double[height][width];
		double g[][] = new double[height][width];
		double b[][] = new double[height][width];
		double rh[][] = new double[height][width];
		double rl[][] = new double[height][width];
		double gh[][] = new double[height][width];
		double gl[][] = new double[height][width];
		double bh[][] = new double[height][width];
		double bl[][] = new double[height][width];
		double w[] = new double[6];
		double sum1, sum2, sum3;
		sum1 = sum2 = sum3 = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				Color c = new Color(image.getRGB(col, row));
				r[row][col] = c.getRed();
				sum1 += r[row][col];
				g[row][col] = c.getGreen();
				sum2 += g[row][col];
				b[row][col] = c.getBlue();
				sum3 += b[row][col];
			}
		}
		sum1 = sum1 / (height * width);
		sum2 = sum2 / (height * width);
		sum3 = sum3 / (height * width);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (r[row][col] > sum1)
					rh[row][col] = r[row][col];
				else
					rl[row][col] = r[row][col];
			}
		}
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (g[row][col] > sum2)
					gh[row][col] = g[row][col];
				else
					gl[row][col] = g[row][col];
			}
		}
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (b[row][col] > sum3)
					bh[row][col] = b[row][col];
				else
					bl[row][col] = b[row][col];
			}
		}
		double mrh, mrl, mgh, mgl, mbh, mbl;
		mrh = mrl = mgh = mgl = mbh = mbl = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				mrh += rh[row][col];
				mrl += rl[row][col];
				mgh += gh[row][col];
				mgl += gl[row][col];
				mbh += bh[row][col];
				mbl += bl[row][col];
			}
		}
		mrh = mrh / (height * width);
		mrl = mrl / (height * width);
		mgh = mgh / (height * width);
		mgl = mgl / (height * width);
		mbh = mbh / (height * width);
		mbl = mbl / (height * width);

		w[0] = mrh;
		w[1] = mrl;
		w[2] = mgh;
		w[3] = mgl;
		w[4] = mbh;
		w[5] = mbl;

		double srh = 0, srl = 0, sgh = 0, sgl = 0, sbh = 0, sbl = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (r[row][col] > sum1)
					srh += Math.pow((rh[row][col] - w[0]), 2);
				else
					srl += Math.pow((rl[row][col] - w[1]), 2);
			}
		}
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (g[row][col] > sum2)
					sgh += Math.pow((gh[row][col] - w[2]), 2);
				else
					sgl += Math.pow((gl[row][col] - w[3]), 2);
			}
		}
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (b[row][col] > sum3)
					sbh += Math.pow((bh[row][col] - w[4]), 2);
				else
					sbl += Math.pow((bl[row][col] - w[5]), 2);
			}
		}

		srh = srh / (height * width);
		srh = Math.sqrt(srh);
		srl = srl / (height * width);
		srl = Math.sqrt(srl);
		sgh = sgh / (height * width);
		sgh = Math.sqrt(sgh);
		sgl = sgl / (height * width);
		sgl = Math.sqrt(sgl);
		sbh = sbh / (height * width);
		sbh = Math.sqrt(sbh);
		sbl = sbl / (height * width);
		sbl = Math.sqrt(sbl);

		textureMatrix2[imageCount][0] = srh;
		textureMatrix2[imageCount][1] = srl;
		textureMatrix2[imageCount][2] = sgh;
		textureMatrix2[imageCount][3] = sgl;
		textureMatrix2[imageCount][4] = sbh;
		textureMatrix2[imageCount][5] = sbl;

	}

	public void getColorCode2(BufferedImage image, int height, int width) {
		double r[][] = new double[height][width];
		double g[][] = new double[height][width];
		double b[][] = new double[height][width];
		double rh[][] = new double[height][width];
		double rl[][] = new double[height][width];
		double gh[][] = new double[height][width];
		double gl[][] = new double[height][width];
		double bh[][] = new double[height][width];
		double bl[][] = new double[height][width];
		double sum1, sum2, sum3;
		sum1 = sum2 = sum3 = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				Color c = new Color(image.getRGB(col, row));
				r[row][col] = c.getRed();
				sum1 += r[row][col];
				g[row][col] = c.getGreen();
				sum2 += g[row][col];
				b[row][col] = c.getBlue();
				sum3 += b[row][col];
			}
		}
		sum1 = sum1 / (height * width);
		sum2 = sum2 / (height * width);
		sum3 = sum3 / (height * width);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (r[row][col] > sum1)
					rh[row][col] = r[row][col];
				else
					rl[row][col] = r[row][col];
			}
		}
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (g[row][col] > sum2)
					gh[row][col] = g[row][col];
				else
					gl[row][col] = g[row][col];
			}
		}
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (b[row][col] > sum3)
					bh[row][col] = b[row][col];
				else
					bl[row][col] = b[row][col];
			}
		}
		double mrh, mrl, mgh, mgl, mbh, mbl;
		mrh = mrl = mgh = mgl = mbh = mbl = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				mrh += rh[row][col];
				mrl += rl[row][col];
				mgh += gh[row][col];
				mgl += gl[row][col];
				mbh += bh[row][col];
				mbl += bl[row][col];
			}
		}
		mrh = mrh / (height * width);
		mrl = mrl / (height * width);
		mgh = mgh / (height * width);
		mgl = mgl / (height * width);
		mbh = mbh / (height * width);
		mbl = mbl / (height * width);

		colorCodeMatrix2[imageCount][0] = mrh;
		colorCodeMatrix2[imageCount][1] = mrl;
		colorCodeMatrix2[imageCount][2] = mgh;
		colorCodeMatrix2[imageCount][3] = mgl;
		colorCodeMatrix2[imageCount][4] = mbh;
		colorCodeMatrix2[imageCount][5] = mbl;
	}

	public void writeColorCode2() {
		try {
			FileWriter fstream = new FileWriter("colorCodes2.txt", false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < 1000; i++) {
				for (int j = 0; j < 6; j++) {
					out.write(colorCodeMatrix2[i][j] + " ");
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.print("File couldn't be created exception!");
		}
	}

	public void writeTexture2() {
		try {
			FileWriter fstream = new FileWriter("texture2.txt", false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < 1000; i++) {
				for (int j = 0; j < 6; j++) {
					out.write(textureMatrix2[i][j] + " ");
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.print("File couldn't be created exception!");
		}
	}

	public void writeSize2() {
		try {
			FileWriter fstream = new FileWriter("fileSize2.txt", false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < 1000; i++) {
				out.write("" + imageSizeList2[i]);
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.print("File couldn't be created exception!");
		}
	}

	public void getTexture1(BufferedImage image, int height, int width) {
		coOccurenceMatrix = new double[height][width];
		for (int row = 0; row < height - 1; row++) {
			for (int col = 0; col < width - 1; col++) {
				Color c = new Color(image.getRGB(col, row));
				int intensity1 = (int) (0.2989 * (double) c.getRed() + 0.5870 * (double) c.getGreen()
						+ 0.1140 * (double) c.getBlue());
				coOccurenceMatrix[row][col] = intensity1;
			}
		}
		GLCM b = new GLCM();
		b.createGLCM(coOccurenceMatrix, 0, 1, 8, 0, 256);
		b.computeGLCM();
		textureMatrix1[imageCount][0] = b.computeContrast();
		textureMatrix1[imageCount][1] = b.computeCorrelation();
		textureMatrix1[imageCount][2] = b.computeEnergy();
		textureMatrix1[imageCount][3] = b.computeHomogeneity();

		b = new GLCM();
		b.createGLCM(coOccurenceMatrix, -1, 1, 8, 0, 256);
		b.computeGLCM();
		textureMatrix1[imageCount][4] = b.computeContrast();
		textureMatrix1[imageCount][5] = b.computeCorrelation();
		textureMatrix1[imageCount][6] = b.computeEnergy();
		textureMatrix1[imageCount][7] = b.computeHomogeneity();

		b = new GLCM();
		b.createGLCM(coOccurenceMatrix, -1, 0, 8, 0, 256);
		b.computeGLCM();
		textureMatrix1[imageCount][8] = b.computeContrast();
		textureMatrix1[imageCount][9] = b.computeCorrelation();
		textureMatrix1[imageCount][10] = b.computeEnergy();
		textureMatrix1[imageCount][11] = b.computeHomogeneity();

		b = new GLCM();
		b.createGLCM(coOccurenceMatrix, -1, -1, 8, 0, 256);
		b.computeGLCM();
		textureMatrix1[imageCount][12] = b.computeContrast();
		textureMatrix1[imageCount][13] = b.computeCorrelation();
		textureMatrix1[imageCount][14] = b.computeEnergy();
		textureMatrix1[imageCount][15] = b.computeHomogeneity();

	}

	public void getColorCode1(BufferedImage image, int height, int width) {
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				Color c = new Color(image.getRGB(col, row));
				float[] hsv = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsv);
				histogram[(int) quant(hsv)]++;
			}
		}
		normalize(histogram);
		for (int i = 0; i < 256; i++) {
			colorCodeMatrix1[imageCount][i] = histogram[i];
		}
	}

	private void normalize(int[] histogram) {
		int max = 0;
		for (int i = 0; i < 256; i++) {
			max = Math.max(histogram[i], max);
		}
		for (int i = 0; i < 256; i++) {
			histogram[i] = (histogram[i] * 256) / max;
		}
	}

	private float quant(float[] pixel) {
		float qH = (float) Math.floor(pixel[0] * q_h);
		float qS = (float) Math.floor(pixel[1] * q_s);
		float qV = (float) Math.floor(pixel[2] * q_v);
		if (qH == q_h)
			qH = (q_h - 1);
		if (qS == q_s)
			qS = (q_s - 1);
		if (qV == q_v)
			qV = (q_v - 1);
		float value = ((qH) * (q_v * q_s) + qS * q_v + qV);
		return value;

	}

	public void writeColorCode1() {
		try {
			FileWriter fstream = new FileWriter("colorCodes1.txt", false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < 1000; i++) {
				for (int j = 0; j < 256; j++) {
					out.write(colorCodeMatrix1[i][j] + " ");
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.print("File couldn't be created exception!");
		}
	}

	public void writeTexture1() {
		try {
			FileWriter fstream = new FileWriter("texture1.txt", false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < 1000; i++) {
				for (int j = 0; j < 16; j++) {
					out.write(textureMatrix1[i][j] + " ");
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.print("File couldn't be created exception!");
		}
	}

	public void writeSize1() {
		try {
			FileWriter fstream = new FileWriter("fileSize1.txt", false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < 1000; i++) {
				out.write("" + imageSizeList1[i]);
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.print("File couldn't be created exception!");
		}
	}
}

class GLCM {
	int width;
	int height;
	int levels;
	int[][] leveled;
	private double[][] GLCM;
	int dx;
	int dy;
	private Double energy;
	private Double contrast;
	private Double homogeneity;
	private Double correlation;

	public void createGLCM(double[][] matrix, int dx, int dy, int levels, double min, double max) {
		int[][] leveled = computeLeveled(matrix, levels, min, max);
		this.leveled = leveled;
		this.levels = levels;
		width = leveled.length;
		height = leveled[0].length;
		this.dx = dx;
		this.dy = dy;
	}

	public double[][] computeGLCM() {
		if (GLCM == null) {
			GLCM = new double[levels][levels];
			int sum = 0;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					// range
					if (x + dx >= 0 && y + dy >= 0 && x + dx < width && y + dy < height) {
						int v1 = leveled[x][y];
						int v2 = leveled[x + dx][y + dy];
						sum++;
						GLCM[v1][v2]++;
					}
				}
			}
			for (int x = 0; x < levels; x++) {
				for (int y = 0; y < levels; y++) {
					GLCM[x][y] /= sum;
				}
			}
		}
		return GLCM;
	}

	public double computeContrast() {
		if (contrast == null) {
			contrast = 0d;
			for (int x = 0; x < levels; x++) {
				for (int y = 0; y < levels; y++) {
					contrast += (x - y) * (x - y) * GLCM[x][y];
				}
			}
		}
		return contrast;
	}

	public double computeCorrelation() {
		if (correlation == null) {
			correlation = 0d;
			double meanX = 0;
			double meanY = 0;
			for (int x = 0; x < levels; x++) {
				for (int y = 0; y < levels; y++) {
					meanX += x * GLCM[x][y];
					meanY += y * GLCM[x][y];
				}
			}
			double stdX = 0;
			double stdY = 0;
			for (int x = 0; x < levels; x++) {
				for (int y = 0; y < levels; y++) {
					stdX += (x - meanX) * (x - meanX) * GLCM[x][y];
					stdY += (y - meanY) * (y - meanY) * GLCM[x][y];
				}
			}
			stdX = Math.sqrt(stdX);
			stdY = Math.sqrt(stdY);
			for (int x = 0; x < levels; x++) {
				for (int y = 0; y < levels; y++) {
					double num = (x - meanX) * (y - meanY) * GLCM[x][y];
					double denum = stdX * stdY;
					correlation += num / denum;
				}
			}
		}
		return correlation;
	}

	public double computeEnergy() {
		if (energy == null) {
			energy = 0d;
			for (int x = 0; x < levels; x++) {
				for (int y = 0; y < levels; y++) {
					energy += GLCM[x][y] * GLCM[x][y];
				}
			}
		}
		return energy;
	}

	public double computeHomogeneity() {
		if (homogeneity == null) {
			homogeneity = 0d;
			for (int x = 0; x < levels; x++) {
				for (int y = 0; y < levels; y++) {
					homogeneity += GLCM[x][y] / (1 + Math.abs(x - y));
				}
			}
		}
		return homogeneity;
	}

	public static int[][] computeLeveled(double[][] matrix, int levels, double min, double max) {
		int[][] _leveled = new int[matrix.length][matrix[0].length];
		for (int x = 0; x < matrix.length; x++) {
			for (int y = 0; y < matrix[0].length; y++) {
				int l = (int) (Math.floor((matrix[x][y] - min) * levels / (double) max));
				if (l < 0)
					l = 0;
				else if (l >= levels)
					l = levels - 1;
				_leveled[x][y] = l;
			}
		}
		return _leveled;
	}
}
