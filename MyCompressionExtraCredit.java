/*
Name: Steve Regala
ID: 7293040280
CSCI 576 Homework 2: Image Compression Extra Credit
3/10/2023
*/

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.*;
import javax.swing.*;


public class MyCompressionExtraCredit {

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
   ArrayList<int[][]> possible_pixel_vectors;

   public void showIms(String[] args) throws FileNotFoundException {

      // Read a parameter from command line
      String filename = args[0];
      //vect_size_M = Integer.parseInt(args[1]);	// typically 2, for extra credit, let it be 2,4,9,16, i.e. perfect squares
      vect_size_M = (int)Math.sqrt(Double.parseDouble(args[1]));
      // set M to sqrt(input M) --> 4==>2x2 block, 9==>3x3 block
      num_vect_N = Integer.parseInt(args[2]);

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
      ArrayList<int[][]> vector_representation = new ArrayList<>();
      ArrayList<int[][]> centroid_array = new ArrayList<>();
      for(int y=0; y<height; y+=vect_size_M){
         for(int x=0; x<width; x+=vect_size_M){
            int[][] inner_most = new int[vect_size_M][vect_size_M];

            // CASES for 2x2, 4x4, and 8x8
            for(int i=0; i<vect_size_M; i++){
               for(int j=0; j<vect_size_M; j++){
                  inner_most[i][j] = Byte.toUnsignedInt(bytes_arr[width*y + x + j + i*width]);
               }
            }

            vector_representation.add(inner_most);
         }
      }


      /*
      Step 2: Initialization of codewords (pick N random codewords)
      K-Means algorithm: use of centroids
      */
      HashSet<int[][]> ind_record = new HashSet<>();
      Random random = new SecureRandom();
      while(centroid_array.size()<num_vect_N){
         int[][] rand_centroid = new int[vect_size_M][vect_size_M];
         int random_index = random.nextInt(vector_representation.size());

         for(int i=0; i<vect_size_M; i++){
            for(int j=0; j<vect_size_M; j++){
               //rand_centroid[i][j] = random.nextInt(vector_space_size);
               rand_centroid[i][j] = vector_representation.get(random_index)[i][j];
            }
         }

         if(!ind_record.contains(rand_centroid)){
            ind_record.add(rand_centroid);
            centroid_array.add(rand_centroid);
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
      ArrayList<int[][]> old_centroid;
      HashMap<int[][], ArrayList<int[][]>> centroid_to_vector;

      do{
         // Group each vector in vector representation by its minimum Euclidean distance with the centroids/codewords
         // Can be understood as the clusters, key represents each centroid
         // STEP 3 ---
         //System.out.println("Iteration #: " + count_iterations);
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
      HashMap<int[][], int[][]> vector_to_centroid = new HashMap<>();
      for(Map.Entry<int[][], ArrayList<int[][]>> entry: centroid_to_vector.entrySet()){
         for(int i=0; i<entry.getValue().size(); i++){
            int[][] vect = entry.getValue().get(i);
            if(!vector_to_centroid.containsKey(vect)){
               vector_to_centroid.put(vect, entry.getKey());
            }
         }
      }

      byte[] bytes_processed = new byte[(int)len];
      int count=0;
      for(int y=0; y<height; y+=vect_size_M){
         for(int x=0; x<width; x+=vect_size_M){
            int[][] curr_vect = vector_representation.get(count);
            int[][] code_word = vector_to_centroid.get(curr_vect);

            for(int i=0; i<vect_size_M; i++){
               for(int j=0; j<vect_size_M; j++){
                  bytes_processed[width*y + x + j + i*width] = (byte) code_word[i][j];
               }
            }

            count++;
         }
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
      MyCompressionExtraCredit ren = new MyCompressionExtraCredit();
      //System.out.println("Now running program");
      ren.showIms(args);
   }

   // Calculate SSE between old centroid and newly formed centroids
   private int sum_squared_errors(ArrayList<int[][]> prev, ArrayList<int[][]> curr){
      int result = 0;
      for(int i=0; i<curr.size(); i++){

         for(int j=0; j<vect_size_M; j++){
            for(int k=0; k<vect_size_M; k++){
               result+= Math.pow(prev.get(i)[j][k] - curr.get(i)[j][k], 2);
            }
         }

      }
      //System.out.println(result);
      return result;
   }

   // Pair up the image vector representation points with its corresponding Euclidean distance
   private HashMap<int[][], ArrayList<int[][]>> form_pairs(ArrayList<int[][]> cent_arr, ArrayList<int[][]> vect_rep){
      HashMap<int[][], ArrayList<int[][]>> result = new HashMap<>();

      // populate result with keys (centroid array points) and values (empty array lists)
      for(int j=0; j<cent_arr.size();j++){
         result.put(cent_arr.get(j), new ArrayList<>());
      }

      for(int i=0; i<vect_rep.size(); i++){
         int[][] key_centroid = find_closest_centroid(cent_arr, vect_rep.get(i));
         ArrayList<int[][]> to_add = result.get(key_centroid);
         to_add.add(vect_rep.get(i));
         result.put(key_centroid, to_add);
      }

      return result;
   }

   // Helper function - return the closest centroid to the given point
   private int[][] find_closest_centroid(ArrayList<int[][]> cent, int[][] vec_pt){
      double curr_minimum = euclidean_dist(cent.get(0),vec_pt);
      int[][] min_result = cent.get(0);

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
   private double euclidean_dist(int[][] curr_centroid, int[][] curr_vect){
      double result = 0.0;

      for(int i=0; i<vect_size_M; i++){
         for(int j=0; j<vect_size_M; j++){
            result += Math.pow(curr_centroid[i][j]-curr_vect[i][j], 2);
         }
      }

      result = Math.sqrt(result);

      return result;
   }

   // Return new centroids, given cluster map
   // iterate through map
   private ArrayList<int[][]> new_centroids(HashMap<int[][], ArrayList<int[][]>> clusters, ArrayList<int[][]> cent_arr){
      ArrayList<int[][]> result = new ArrayList<>();
      HashSet<int[][]> existing_centroids = new HashSet<>();
      ArrayList<Integer> empty_index = new ArrayList<>();

      for(int i=0; i<cent_arr.size(); i++){
         int[][] curr_centroid = cent_arr.get(i);
         ArrayList<int[][]> temp_arr = clusters.get(curr_centroid);
         int arr_size = temp_arr.size();

         if(arr_size!=0){
            existing_centroids.add(curr_centroid);

            int[][] holder = new int[vect_size_M][vect_size_M];

            for(int j=0; j<arr_size; j++){
               for(int m=0; m<vect_size_M; m++){
                  for(int n=0; n<vect_size_M; n++){
                     holder[m][n] += temp_arr.get(j)[m][n];
                  }
               }
            }

            int[][] averages = new int[vect_size_M][vect_size_M];
            for(int m=0; m<vect_size_M; m++){
               for(int n=0; n<vect_size_M; n++){
                  averages[m][n] += holder[m][n]/arr_size;
               }
            }
            result.add(averages);

         }else{ // handle case for when centroid key has empty arraylist
            empty_index.add(i);
         }
      }

      if(empty_index.size()>0){
         for(int k=0; k<empty_index.size(); k++){
            int curr_index = empty_index.get(k);
            int found=0;

            while(found==0){
               Random random = new SecureRandom();
               int[][] random_centroid = new int[vect_size_M][vect_size_M];
               for(int i=0; i<vect_size_M; i++){
                  for(int j=0; j<vect_size_M; j++){
                     random_centroid[i][j] = random.nextInt(vector_space_size);
                  }
               }
               if(!existing_centroids.contains(random_centroid)){
                  existing_centroids.add(random_centroid);
                  result.add(curr_index, random_centroid);
                  found=1;
               }
            }
         }
      }

      return result;
   }

}
