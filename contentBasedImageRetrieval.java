package contentBasedImageRetrieval_BE_Project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import static java.lang.Double.isNaN;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static javax.swing.UIManager.getColor;

public class contentBasedImageRetrieval extends javax.swing.JFrame {
	private JLabel photographLabel = new JLabel();
	private JButton[] button;
	private JCheckBox[] checkbox;
	private int[] buttonOrder = new int[1000];
	private int[] buttonOrder1 = new int[1000];
	private double[] imageSize1 = new double[1000];
	private double[] imageSize2 = new double[1000];
	private boolean isRelevanceOn = false;
	private boolean isTextureOn = false;
	private Double[][] colorCodeMatrix1 = new Double[1000][256];
	private Double[][] textureMatrix1 = new Double[1000][16];
	private Double[][] intensityColorCodeMatrix1 = new Double[1000][272];
	private Double[] weightVector = new Double[272];
	private Double[] weightVector1 = new Double[256];

	private Double[][] colorCodeMatrix2 = new Double[1000][6];
	private Double[][] textureMatrix2 = new Double[1000][6];
	private Double[][] intensityColorCodeMatrix2 = new Double[1000][12];
	private Double[] weightVector3 = new Double[12];

	private TreeMap<Double, LinkedList<Integer>> map;
	private Vector<Integer> relevantImages = new Vector<Integer>();
	int picNo = 0;
	int imageCount = 0;
	int pageNo = 1;

	public contentBasedImageRetrieval() {
		initComponents();
		button = new JButton[1000];
		checkbox = new JCheckBox[1000];
		jPanel1.setLayout(new GridLayout(4, 5, 5, 5));
		jPanel2.setLayout(new GridLayout(1, 1, 5, 5));
		jPanel2.add(photographLabel);
		jCheckBox1.setVisible(false);
		jButton5.setEnabled(false);
		jButton6.setEnabled(false);
		jTextField1.setHorizontalAlignment(JTextField.CENTER);
		jCheckBox1.addItemListener(new itemChangeHandler());
		initbutton();
		
		initializeWeights1();
		readColorCodeFile1();
		readTextureFile1();
		readFileSizeFile1();
		normalizeFeatures1();

		initializeWeights2();
		readColorCodeFile2();
		readTextureFile2();
		readFileSizeFile2();
		normalizeFeatures2();
	}

	private void initbutton() {
		for (int i = 0; i < 1000; i++) {
			ImageIcon icon;
			icon = new ImageIcon("C:\\Users\\saura_000\\Desktop\\image.orig\\" + i + ".jpg");
			if (icon != null) {
				ImageIcon smallIcon = new ImageIcon(
						icon.getImage().getScaledInstance(200, 80, java.awt.Image.SCALE_SMOOTH));
				button[i] = new JButton(smallIcon);
				button[i].addActionListener(new IconButtonHandler(i, icon));
				buttonOrder[i] = i;
				buttonOrder1[i] = i;
				checkbox[i] = new JCheckBox(smallIcon);
				checkbox[i].addItemListener(new itemChangeHandler());
			}
		}
	}

	private class itemChangeHandler implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			Object source = e.getItemSelectable();
			if (source == jCheckBox1) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					isRelevanceOn = false;
					setAllChecked(false);
					initializeWeights1();
					initializeWeights2();
					displayFirstPage();
				} else if (e.getStateChange() == ItemEvent.SELECTED) {
					isRelevanceOn = true;
					displayFirstPage();
				}
			}
			for (int i = 0; i < 1000; i++) {
				if (source == checkbox[i]) {
					if (e.getStateChange() == ItemEvent.DESELECTED) {
						checkbox[i].setBorderPainted(false);
						relevantImages.remove((Integer) i);
					} else if (e.getStateChange() == ItemEvent.SELECTED) {
						checkbox[i].setBorderPainted(true);
						checkbox[i].setBorder(BorderFactory.createLineBorder(Color.GREEN, 5, true));
						relevantImages.add(i);
					}
				}
			}
		}
	}

	private void readColorCodeFile1() {
		Scanner read;
		Double colorCodeBin;
		try {
			read = new Scanner(new File("colorCodes1.txt"));
			for (int i = 0; i < 1000; i++) {
				for (int j = 0; j < 256; j++) {
					if (read.hasNext()) {
						colorCodeBin = read.nextDouble();
						colorCodeMatrix1[i][j] = colorCodeBin;
					}
				}
			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file colorCodes.txt does not exist");
		}
	}

	private void readTextureFile1() {
		Scanner read;
		Double textureValue;
		try {
			read = new Scanner(new File("texture1.txt"));
			for (int i = 0; i < 1000; i++) {
				for (int j = 0; j < 16; j++) {
					if (read.hasNext()) {
						textureValue = read.nextDouble();
						textureMatrix1[i][j] = textureValue;
					}
				}
			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file texture.txt does not exist");
		}
	}

	private void normalizeFeatures1() {
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 256; j++) {
				intensityColorCodeMatrix1[i][j] = colorCodeMatrix1[i][j] / (double) imageSize1[i];
			}
			for (int j = 256; j < 272; j++) {
				intensityColorCodeMatrix1[i][j] = textureMatrix1[i][j - 256] / (double) imageSize1[i];
			}
		}
		normalize1();
	}

	private void normalize1() {
		for (int i = 0; i < 272; i++) {
			double total = 0.0;
			double average = 0.0;
			double standardDev = 0.0;
			double varianceTotal = 0.0;
			for (int j = 0; j < 1000; j++) {
				total += intensityColorCodeMatrix1[j][i];
			}
			average = total / 100.0;
			for (int j = 0; j < 1000; j++) {
				varianceTotal += Math.pow(intensityColorCodeMatrix1[j][i] - average, 2);
			}
			standardDev = Math.sqrt(varianceTotal / 99.0);
			for (int j = 0; j < 1000; j++) {
				if (standardDev != 0) {
					intensityColorCodeMatrix1[j][i] = (intensityColorCodeMatrix1[j][i] - average) / standardDev;
				}
			}
		}
	}

	private void readFileSizeFile1() {
		Scanner read;
		Double currentImageSize;
		try {
			read = new Scanner(new File("fileSize1.txt"));
			for (int i = 0; i < 1000; i++) {
				if (read.hasNext()) {
					currentImageSize = read.nextDouble();
					imageSize1[i] = currentImageSize;
				}
			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file colorCodes.txt does not exist");
		}
	}

	private void initializeWeights1() {
		for (int i = 0; i < 272; i++) {
			weightVector[i] = (double) 1.0 / 272;
		}
		for (int i = 0; i < 256; i++) {
			weightVector1[i] = (double) 1.0 / 256;
		}
	}

	private void updateWeights1() {
		double weightSum = 0.0;
		double standardDevArray[] = new double[272];
		double averageArray[] = new double[272];
		double minStandardDev = Double.MAX_VALUE;
		for (int i = 0; i < 272; i++) {
			double total = 0.0;
			double average = 0.0;
			double standardDev = 0.0;
			double varianceTotal = 0.0;
			int size = relevantImages.size();
			boolean sameStdDev = true;
			double temp = 0.0;
			for (int j = 0; j < size - 1; j++) {
				temp = intensityColorCodeMatrix1[relevantImages.get(j)][i];
				if (temp != intensityColorCodeMatrix1[relevantImages.get(j + 1)][i]) {
					sameStdDev = false;
				}
			}
			if (sameStdDev) {
				average = temp;
			} else {
				for (int j = 0; j < size; j++) {
					total += intensityColorCodeMatrix1[relevantImages.get(j)][i];
				}
				if (size != 0) {
					average = total / (double) size;
				}
			}
			for (int j = 0; j < size; j++) {
				varianceTotal += Math.pow(intensityColorCodeMatrix1[relevantImages.get(j)][i] - average, 2);
			}
			if (size != 0) {
				standardDev = Math.sqrt(varianceTotal / (double) (size));
			}
			standardDevArray[i] = standardDev;
			averageArray[i] = average;
			if (standardDev != 0) {
				minStandardDev = Math.min(standardDev, minStandardDev);
			}
		}
		for (int i = 0; i < 272; i++) {
			if (standardDevArray[i] == 0) {
				if (averageArray[i] == 0) {
					weightVector[i] = 0.0;
				} else {
					weightVector[i] = 0.5 * minStandardDev;
				}
			} else {
				weightVector[i] = 1 / standardDevArray[i];
			}
			weightSum += weightVector[i];
		}
		for (int i = 0; i < 272; i++) {
			weightVector[i] = weightVector[i] / weightSum;
		}
	}

	private void updateColorWeights1() {
		double weightSum = 0.0;
		double standardDevArray[] = new double[256];
		double averageArray[] = new double[256];
		double minStandardDev = Double.MAX_VALUE;
		for (int i = 0; i < 256; i++) {
			double total = 0.0;
			double average = 0.0;
			double standardDev = 0.0;
			double varianceTotal = 0.0;
			int size = relevantImages.size();
			boolean sameStdDev = true;
			double temp = 0.0;
			for (int j = 0; j < size - 1; j++) {
				temp = colorCodeMatrix1[relevantImages.get(j)][i];
				if (temp != colorCodeMatrix1[relevantImages.get(j + 1)][i]) {
					sameStdDev = false;
				}
			}
			if (sameStdDev) {
				average = temp;
			} else {
				for (int j = 0; j < size; j++) {
					total += colorCodeMatrix1[relevantImages.get(j)][i];
				}
				if (size != 0) {
					average = total / (double) size;
				}
			}
			for (int j = 0; j < size; j++) {
				varianceTotal += Math.pow(colorCodeMatrix1[relevantImages.get(j)][i] - average, 2);
			}
			if (size != 0) {
				standardDev = Math.sqrt(varianceTotal / (double) (size));
			}
			standardDevArray[i] = standardDev;
			averageArray[i] = average;
			if (standardDev != 0) {
				minStandardDev = Math.min(standardDev, minStandardDev);
			}
		}
		for (int i = 0; i < 256; i++) {
			if (standardDevArray[i] == 0) {
				if (averageArray[i] == 0) {
					weightVector1[i] = 0.0;
				} else {
					weightVector1[i] = 0.5 * minStandardDev;
				}
			} else {
				weightVector1[i] = 1 / standardDevArray[i];
			}
			weightSum += weightVector1[i];
		}
		for (int i = 0; i < 256; i++) {
			weightVector1[i] = weightVector1[i] / weightSum;
		}
	}

	private void setAllChecked(boolean checked) {
		for (int i = 0; i < 1000; i++) {
			checkbox[i].setSelected(checked);
		}
	}

	private void readColorCodeFile2() {
		Scanner read;
		Double colorCodeBin;
		try {
			read = new Scanner(new File("colorCodes2.txt"));
			for (int i = 0; i < 1000; i++) {
				for (int j = 0; j < 6; j++) {
					if (read.hasNext()) {
						colorCodeBin = read.nextDouble();
						colorCodeMatrix2[i][j] = colorCodeBin;
					}
				}
			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file colorCodes.txt does not exist");
		}
	}

	private void readTextureFile2() {
		Scanner read;
		Double textureValue;
		try {
			read = new Scanner(new File("texture2.txt"));
			for (int i = 0; i < 1000; i++) {
				for (int j = 0; j < 6; j++) {
					if (read.hasNext()) {
						textureValue = read.nextDouble();
						textureMatrix2[i][j] = textureValue;
					}
				}
			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file texture.txt does not exist");
		}
	}

	private void normalizeFeatures2() {
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 6; j++) {
				intensityColorCodeMatrix2[i][j] = colorCodeMatrix2[i][j] / (double) imageSize2[i];
			}
			for (int j = 6; j < 12; j++) {
				intensityColorCodeMatrix2[i][j] = textureMatrix2[i][j - 6] / (double) imageSize2[i];
			}
		}
		normalize2();
	}

	private void normalize2() {
		for (int i = 0; i < 12; i++) {
			double total = 0.0;
			double average = 0.0;
			double standardDev = 0.0;
			double varianceTotal = 0.0;
			for (int j = 0; j < 1000; j++) {
				total += intensityColorCodeMatrix2[j][i];
			}
			average = total / 100.0;
			for (int j = 0; j < 1000; j++) {
				varianceTotal += Math.pow(intensityColorCodeMatrix2[j][i] - average, 2);
			}
			standardDev = Math.sqrt(varianceTotal / 1212.0);
			for (int j = 0; j < 1000; j++) {
				if (standardDev != 0) {
					intensityColorCodeMatrix2[j][i] = (intensityColorCodeMatrix2[j][i] - average) / standardDev;
				}
			}
		}
	}

	private void readFileSizeFile2() {
		Scanner read;
		Double currentImageSize;
		try {
			read = new Scanner(new File("fileSize2.txt"));
			for (int i = 0; i < 1000; i++) {
				if (read.hasNext()) {
					currentImageSize = read.nextDouble();
					imageSize2[i] = currentImageSize;
				}
			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file colorCodes.txt does not exist");
		}
	}

	private void initializeWeights2() {
		for (int i = 0; i < 12; i++) {
			weightVector3[i] = (double) 1.0 / 12;
		}
	}

	private void updateWeights2() {
		double weightSum = 0.0;
		double standardDevArray[] = new double[12];
		double averageArray[] = new double[12];
		double minStandardDev = Double.MAX_VALUE;
		for (int i = 0; i < 12; i++) {
			double total = 0.0;
			double average = 0.0;
			double standardDev = 0.0;
			double varianceTotal = 0.0;
			int size = relevantImages.size();
			boolean sameStdDev = true;
			double temp = 0.0;
			for (int j = 0; j < size - 1; j++) {
				temp = intensityColorCodeMatrix2[relevantImages.get(j)][i];
				if (temp != intensityColorCodeMatrix2[relevantImages.get(j + 1)][i]) {
					sameStdDev = false;
				}
			}
			if (sameStdDev) {
				average = temp;
			} else {
				for (int j = 0; j < size; j++) {
					total += intensityColorCodeMatrix2[relevantImages.get(j)][i];
				}
				if (size != 0) {
					average = total / (double) size;
				}
			}
			for (int j = 0; j < size; j++) {
				varianceTotal += Math.pow(intensityColorCodeMatrix2[relevantImages.get(j)][i] - average, 2);
			}
			if (size != 0) {
				standardDev = Math.sqrt(varianceTotal / (double) (size));
			}
			standardDevArray[i] = standardDev;
			averageArray[i] = average;
			if (standardDev != 0) {
				minStandardDev = Math.min(standardDev, minStandardDev);
			}
		}
		for (int i = 0; i < 12; i++) {
			if (standardDevArray[i] == 0) {
				if (averageArray[i] == 0) {
					weightVector3[i] = 0.0;
				} else {
					weightVector3[i] = 0.5 * minStandardDev;
				}
			} else {
				weightVector3[i] = 1 / standardDevArray[i];
			}
			weightSum += weightVector3[i];
		}
		for (int i = 0; i < 12; i++) {
			weightVector3[i] = weightVector3[i] / weightSum;
		}
	}

	private void displayFirstPage() {
		int imageButNo = 0;
		jPanel1.removeAll();
		for (int i = 0; i < 20; i++) {
			imageButNo = buttonOrder[i];
			if (isRelevanceOn) {
				jPanel1.add(checkbox[imageButNo]);
			} else {
				jPanel1.add(button[imageButNo]);
			}
			imageCount++;
		}
		jPanel1.revalidate();
		jPanel1.repaint();
	}

	private class IconButtonHandler implements ActionListener {
		int pNo = 0;
		ImageIcon iconUsed;

		IconButtonHandler(int i, ImageIcon j) {
			pNo = i;
			iconUsed = j;
		}

		public void actionPerformed(ActionEvent e) {
			photographLabel.setIcon(
					new ImageIcon(iconUsed.getImage().getScaledInstance(200, 130, java.awt.Image.SCALE_SMOOTH)));
			picNo = pNo;
		}
	}
	@SuppressWarnings("unchecked")
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jButton4 = new javax.swing.JButton();
		jTextField1 = new javax.swing.JTextField();
		jButton5 = new javax.swing.JButton();
		jButton6 = new javax.swing.JButton();
		jCheckBox1 = new javax.swing.JCheckBox();
		jLabel3 = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 200, Short.MAX_VALUE));

		jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18));
		jLabel1.setText("Content Based Image Retrieval Using Color and Texture Feature");

		jLabel2.setText("Query Image");

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 170, Short.MAX_VALUE));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 126, Short.MAX_VALUE));

		jButton1.setText("Color (Using HSV Bins)");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		jButton2.setText("Color (Using HSV Bins) and Texture (Using GLCM) ");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		jButton3.setText("Color (Using mean) and Texture (Using standard deviation)");
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});

		jButton4.setText("Browse");
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton4ActionPerformed(evt);
			}
		});

		jTextField1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
		jTextField1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jTextField1ActionPerformed(evt);
			}
		});

		jButton5.setText("Previous Page");
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton5ActionPerformed(evt);
			}
		});

		jButton6.setText("Next Page");
		jButton6.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton6ActionPerformed(evt);
			}
		});

		jCheckBox1.setText("Relevance Feedback");
		jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBox1ActionPerformed(evt);
			}
		});

		jLabel3.setText("Enter Page Number");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
								.createSequentialGroup().addGroup(layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
												layout.createSequentialGroup()
														.addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE,
																210, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(417, 417, 417).addComponent(jCheckBox1)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136,
																Short.MAX_VALUE)
														.addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE,
																210, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(jLabel1)
										.addGap(454, 454, 454))))
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup()
												.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGap(111, 111, 111))
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup().addComponent(jLabel2).addGap(170, 170, 170)))
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 354,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 354,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(jButton3)
								.addGroup(layout.createSequentialGroup()
										.addGroup(layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(jTextField1))
										.addGap(18, 18, 18).addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGap(19, 19, 19)));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
						.createSequentialGroup().addContainerGap()
						.addComponent(jLabel1).addGroup(layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup().addGap(24, 24, 24)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE,
														62, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 62,
														javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(41, 41, 41)
										.addGroup(layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 65,
														Short.MAX_VALUE)
												.addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addGroup(layout.createSequentialGroup()
														.addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
																21, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(jTextField1))))
								.addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addComponent(jLabel2)
										.addGap(18, 18, 18).addComponent(jPanel2,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addGap(18, 18, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
								.createSequentialGroup()
								.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 59,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 58,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(78, 78, 78))
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
										.addComponent(jCheckBox1).addGap(104, 104, 104)))));

		pack();
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {

		if (picNo != 0) {
			jButton5.setEnabled(false);
			jCheckBox1.setVisible(true);
			jButton6.setEnabled(false);
			double[] distance = new double[1000];
			map = new TreeMap<Double, LinkedList<Integer>>();
			double d = 0;
			int buttonNo = 0;
			if (isRelevanceOn) {
				updateColorWeights1();
			}
			for (int i = 0; i < 1000; i++) {
				d = 0;
				for (int j = 0; j < 256; j++) {
					d += weightVector1[j] * Math.pow(Math.abs(
							colorCodeMatrix1[picNo][j] / imageSize1[picNo] - colorCodeMatrix1[i][j] / imageSize1[i]),
							2);
				}
				distance[i] = Math.sqrt(d);
				if (map.get(d) == null) {
					map.put(d, new LinkedList<Integer>());
				}
				map.get(d).add(i);
			}
			for (Map.Entry<Double, LinkedList<Integer>> entry : map.entrySet()) {
				LinkedList<Integer> list = entry.getValue();
				while (list.size() > 0) {
					buttonOrder[buttonNo] = list.pop();
					buttonNo++;
				}
			}
			imageCount = 0;
			displayFirstPage();
		}
	}

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {

		if (picNo != 0) {
			jButton5.setEnabled(false);
			jCheckBox1.setVisible(true);
			jButton6.setEnabled(false);
			double[] distance = new double[1000];
			map = new TreeMap<Double, LinkedList<Integer>>();
			double d = 0;
			int buttonNo = 0;
			if (isRelevanceOn) {
				updateWeights1();
			}
			for (int i = 0; i < 1000; i++) {
				d = 0;
				for (int j = 0; j < 272; j++) {
					double t = weightVector[j]
							* Math.pow(Math.abs(intensityColorCodeMatrix1[picNo][j] / imageSize1[picNo]
									- intensityColorCodeMatrix1[i][j] / imageSize1[i]), 2);
					if (isNaN(t))
						t = 999;
					d += t;
				}
				distance[i] = Math.sqrt(d);
				if (map.get(d) == null) {
					map.put(d, new LinkedList<Integer>());
				}
				map.get(d).add(i);
			}
			for (Map.Entry<Double, LinkedList<Integer>> entry : map.entrySet()) {
				LinkedList<Integer> list = entry.getValue();
				while (list.size() > 0) {
					buttonOrder[buttonNo] = list.pop();
					buttonNo++;
				}
			}
			imageCount = 0;
			displayFirstPage();
		}
	}

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {

		if (picNo != 0) {
			jButton5.setEnabled(false);
			jCheckBox1.setVisible(true);
			jButton6.setEnabled(false);
			double[] distance = new double[1000];
			map = new TreeMap<Double, LinkedList<Integer>>();
			double d = 0;
			int buttonNo = 0;
			if (isRelevanceOn) {
				updateWeights2();
			}
			for (int i = 0; i < 1000; i++) {
				d = 0;
				for (int j = 0; j < 12; j++) {
					d += weightVector3[j] * Math.pow(Math.abs(intensityColorCodeMatrix2[picNo][j] / imageSize2[picNo]
							- intensityColorCodeMatrix2[i][j] / imageSize2[i]), 2);
				}
				distance[i] = Math.sqrt(d);
				if (map.get(d) == null) {
					map.put(d, new LinkedList<Integer>());
				}
				map.get(d).add(i);
			}
			for (Map.Entry<Double, LinkedList<Integer>> entry : map.entrySet()) {
				LinkedList<Integer> list = entry.getValue();
				while (list.size() > 0) {
					buttonOrder[buttonNo] = list.pop();
					buttonNo++;
				}
			}
			imageCount = 0;
			displayFirstPage();
		}
	}

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
		jButton5.setEnabled(true);
		jButton6.setEnabled(true);
		if (isRelevanceOn)
			isRelevanceOn = false;
		if (jCheckBox1.isSelected())
			jCheckBox1.setSelected(false);
		jCheckBox1.setVisible(false);
		if (Integer.parseInt(jTextField1.getText()) > -1) {
			imageCount = 20 * (Integer.parseInt(jTextField1.getText()) - 1);
			int ct = 0;
			for (int i = 0; i < 1000; i++) {
				if (buttonOrder[i] != buttonOrder1[i]) {
					ct = 1;
					break;
				}
			}
			if (ct == 1) {
				buttonOrder = new int[1000];
				initbutton();
			}
			jButton6.doClick();
		}
	}

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
		int imageButNo = 0;
		int startImage = imageCount - 40;
		int endImage = imageCount - 20;
		if (startImage >= 0) {
			jPanel1.removeAll();
			for (int i = startImage; i < endImage; i++) {
				imageButNo = buttonOrder[i];
				if (isRelevanceOn) {
					jPanel1.add(checkbox[imageButNo]);
				} else {
					jPanel1.add(button[imageButNo]);
				}
				imageCount--;
			}
			jPanel1.revalidate();
			jPanel1.repaint();
		}
	}

	private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
		int imageButNo = 0;
		int endImage = 0;
		int temp = 0;
		int count = 0;
		if ((imageCount + 20) != 9920)
			endImage = imageCount + 20;
		else {
			temp = 1;
			count = imageCount;
			endImage = imageCount + 8;
		}
		if (endImage <= 1000) {
			jPanel1.removeAll();
			for (int i = imageCount; i < endImage; i++) {
				imageButNo = buttonOrder[i];
				if (isRelevanceOn) {
					jPanel1.add(checkbox[imageButNo]);
				} else {
					jPanel1.add(button[imageButNo]);
				}
				imageCount++;
			}
			if (temp == 1)
				imageCount = count;
			jPanel1.revalidate();
			jPanel1.repaint();
		}
	}
	public static void main(String args[]) {
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new readImage();
				new NewJFrame().setVisible(true);
			}
		});
	}
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JButton jButton6;
	private javax.swing.JCheckBox jCheckBox1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JTextField jTextField1;
}
