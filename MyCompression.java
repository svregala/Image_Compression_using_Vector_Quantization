/*
Name: Steve Regala
ID: 7293040280
CSCI 576 Homework 2: Image Compression
3/10/2023
*/

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class MyCompression {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
	BufferedImage img_compressed;
	int width = 352;
	int height = 288;
	int vector_space_size = 256;
	int num_vect_N;
	int vect_size_M;
	ArrayList<Point> possible_2pixel_vectors;

	public void showIms(String[] args) throws FileNotFoundException {

		// Read a parameter from command line
		String filename = args[0];
		vect_size_M = Integer.parseInt(args[1]);	// typically 2
		num_vect_N = Integer.parseInt(args[2]);

		//System.out.println("The first parameter was: " + filename);
		byte[] bytes_arr = null;
		long len = 0;

		try {
			File file = new File(filename);
			InputStream stream = new FileInputStream(file);

			len = file.length();
			bytes_arr = new byte[(int) len];

			int off = 0;
			int numRead = stream.read(bytes_arr, off, bytes_arr.length);	// -1 denotes no more bytes to read
			while (off < bytes_arr.length && numRead >= 0) {
				off += numRead;
				numRead = stream.read(bytes_arr, off, bytes_arr.length - off);
			}
			stream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		Step 1: Vector representation of image +
		forming array of all possible 2-pixel vectors in vector space
		*/
		ArrayList<Point> vector_representation = new ArrayList<>();
		for(int k=0; k< bytes_arr.length; k+=2){
			Point object_pair = new Point(Byte.toUnsignedInt(bytes_arr[k]),Byte.toUnsignedInt(bytes_arr[k+1]));
			vector_representation.add(object_pair);
		}

		possible_2pixel_vectors = new ArrayList<>();
		for(int i=0; i<vector_space_size; i++){
			for(int j=0; j<vector_space_size; j++){
				Point object_pair = new Point(i,j);
				possible_2pixel_vectors.add(object_pair);
			}
		}

		/*
		Step 2: Initialization of codewords (pick N random codewords)
		K-Means algorithm: use of centroids
		*/
		ArrayList<Point> centroid_array = new ArrayList<>();
		HashSet<Integer> ind_record = new HashSet<>();
		Random random = new Random();
		while(centroid_array.size()<num_vect_N){
			//int index_to_add = random.nextInt(vector_space_size);
			int index_to_add = random.nextInt(vector_representation.size());
			if(!ind_record.contains(index_to_add)){
				ind_record.add(index_to_add);
				//centroid_array.add(possible_2pixel_vectors.get(index_to_add));
				centroid_array.add(vector_representation.get(index_to_add));
			}
		}

		/*
		Step 3: K-Means Algorithm
		- Clusterize all vectors - assign each vector to a codeword using Euclidean distance
		- Take each input vector and finding distance between it and each codeword
		- Input vector belongs to cluster of codeword that yields minimum distance
		Step 4: New centroid computation
		- compute a new set of codewords --> done by obtaining the average of each cluster
		- add the component of each vector and divide by the number of vectors in the cluster, given equation
		*/
		int count_iterations = 1;
		ArrayList<Point> old_centroid;
		HashMap<Point, ArrayList<Point>> centroid_to_vector;

		do{
			// Group each vector in vector representation by its minimum Euclidean distance with the centroids/codewords
			// Can be understood as the clusters, key represents each centroid
			// STEP 3 ---
			centroid_to_vector = form_pairs(centroid_array, vector_representation);

			// Find the new centroids
			// STEP 4 ---
			old_centroid = new ArrayList<>(centroid_array);
			centroid_array = new_centroids(centroid_to_vector, centroid_array);

			count_iterations++;
		}while(sum_squared_errors(old_centroid, centroid_array)>0); // threshold is 0

		System.out.println("Algorithm took this many iterations: " + count_iterations);
		// print out list of centroid points
		/*for(int i=0; i<centroid_array.size();i++){
			System.out.println(centroid_array.get(i));
		}
		System.out.println(centroid_array.size());*/


		/*
		Step 5: Quantize input vectors to produce output image
		- map all input vectors to one of the codewords and produce your output image
		*/
		centroid_to_vector = form_pairs(centroid_array, vector_representation);
		// create a map with keys found in vector representation of image, and values as the corresponding centroids
		HashMap<Point, Point> vector_to_centroid = new HashMap<>();
		for(Map.Entry<Point, ArrayList<Point>> entry: centroid_to_vector.entrySet()){
			for(int i=0; i<entry.getValue().size(); i++){
				Point vect = entry.getValue().get(i);
				if(!vector_to_centroid.containsKey(vect)){
					vector_to_centroid.put(vect, entry.getKey());
				}
			}
		}

		byte[] bytes_processed = new byte[(int)len];
		for(int i=0; i<vector_representation.size();i+=1){
			Point curr_vect = vector_representation.get(i);
			Point code_word = vector_to_centroid.get(curr_vect);
			bytes_processed[i*2] = (byte) code_word.x;
			bytes_processed[i*2+1] = (byte) code_word.y;
		}
		img_compressed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int index = 0;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){

				byte r = (byte) bytes_processed[index];
				byte g = (byte) bytes_processed[index];
				byte b = (byte) bytes_processed[index];

				int pix = 0xff000000 | ((r& 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img_compressed.setRGB(x,y,pix);
				index++;
			}
		}

		// DISPLAY BOTH IMAGES ----------------------
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int ind = 0;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){

				byte r = (byte) bytes_arr[ind];
				byte g = (byte) bytes_arr[ind];
				byte b = (byte) bytes_arr[ind];

				int pix = 0xff000000 | ((r& 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x,y,pix);
				ind++;
			}
		}

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after compression-decompression (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel(new ImageIcon(img));
		lbIm2 = new JLabel(new ImageIcon(img_compressed));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		frame.getContentPane().add(lbIm2, c);

		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// Main function
	public static void main(String[] args) throws FileNotFoundException {
		MyCompression ren = new MyCompression();
		ren.showIms(args);
	}

	// Calculate SSE between old centroid and newly formed centroids
	private int sum_squared_errors(ArrayList<Point> prev, ArrayList<Point> curr){
		int result = 0;
		for(int i=0; i< curr.size(); i++){
			int prev_X = prev.get(i).x;
			int prev_Y = prev.get(i).y;

			int curr_X = curr.get(i).x;
			int curr_Y = curr.get(i).y;

			int x_diff = prev_X - curr_X;
			int y_diff = prev_Y - curr_Y;

			result += Math.pow(x_diff,2) + Math.pow(y_diff,2);
		}
		return result;
	}

	// Pair up the image vector representation points with its corresponding Euclidean distance
	private HashMap<Point, ArrayList<Point>> form_pairs(ArrayList<Point> cent_arr, ArrayList<Point> vect_rep){
		HashMap<Point, ArrayList<Point>> result = new HashMap<>();

		// populate result with keys (centroid array points) and values (empty array lists)
		for(int j=0; j<cent_arr.size();j++){
			result.put(cent_arr.get(j), new ArrayList<>());
		}

		for(int i=0; i<vect_rep.size(); i++){
			Point key_centroid = find_closest_centroid(cent_arr, vect_rep.get(i));
			ArrayList<Point> to_add = result.get(key_centroid);
			to_add.add(vect_rep.get(i));
			result.put(key_centroid, to_add);
		}

		return result;
	}

	// Helper function - return the closest centroid to the given point
	private Point find_closest_centroid(ArrayList<Point> cent, Point vec_pt){
		double curr_minimum = euclidean_dist(cent.get(0),vec_pt);
		Point min_result = cent.get(0);

		for(int i=1; i<cent.size(); i++){
			double test_minimum = euclidean_dist(cent.get(i),vec_pt);
			if(curr_minimum > test_minimum){
				curr_minimum = test_minimum;
				min_result = cent.get(i);
			}
		}

		return min_result;
	}

	// Return Euclidean distance between two points
	private double euclidean_dist(Point curr_centroid, Point curr_vect){
		double result = 0.0;

		double x1 = curr_centroid.getX();
		double y1 = curr_centroid.getY();

		double x2 = curr_vect.getX();
		double y2 = curr_vect.getY();

		double inside = Math.pow(x2-x1,2) + Math.pow(y2-y1,2);
		result = Math.sqrt(inside);

		return result;
	}

	// Return new centroids, given cluster map
	// iterate through map
	private ArrayList<Point> new_centroids(HashMap<Point, ArrayList<Point>> clusters, ArrayList<Point> cent_arr){
		ArrayList<Point> result = new ArrayList<>();
		HashSet<Point> existing_centroids = new HashSet<>();
		ArrayList<Integer> empty_index = new ArrayList<>();

		for(int i=0; i<cent_arr.size(); i++){
			Point curr_centroid = cent_arr.get(i);
			ArrayList<Point> temp_arr = clusters.get(curr_centroid);
			int arr_size = temp_arr.size();

			if(arr_size!=0){
				existing_centroids.add(curr_centroid);

				double x_sum = 0.0;
				double y_sum = 0.0;
				for(int j=0; j<arr_size; j++){
					x_sum+=temp_arr.get(j).getX();
					y_sum+=temp_arr.get(j).getY();
				}
				double x_avg = x_sum/arr_size;
				double y_avg = y_sum/arr_size;
				//System.out.println(x_avg + " === " + y_avg);

				Point new_point = new Point((int)x_avg, (int)y_avg);
				result.add(new_point);

			}else{ // handle case for when centroid key has empty arraylist
				empty_index.add(i);
			}
		}

		if(empty_index.size()>0){
			for(int k=0; k<empty_index.size(); k++){
				int curr_index = empty_index.get(k);
				int found=0;

				while(found==0){
					Random random = new Random();
					int index_to_add = random.nextInt(vector_space_size);
					Point possible_point = possible_2pixel_vectors.get(index_to_add);
					if(!existing_centroids.contains(possible_point)){
						existing_centroids.add(possible_point);
						result.add(curr_index, possible_point);
						found=1;
					}
				}
			}
		}

		return result;
	}

}