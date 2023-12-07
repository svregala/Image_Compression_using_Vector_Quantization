# Image_Compression_using_Vector_Quantization
 
For the full, detailed project description, please take a look at Assignment2_Spring_2023.pdf. Before reading further, I highly suggest reading pages 3-6 from the pdf.

**Synopsis**: In this project, we better our understanding of image compression. We perform image compression using vector quantization. To compress an image with M=2 (2 adjacent pixels side by side), we first initialize our code words, N, which is the number of vectors (assume it's a power of 2 -- thus after quantization, each vector will need an index with logN bits). Then, we use K-Means clustering to cluster the vectors around each code word. Then, we refine and update the code words depending on the clusters that were created in K-Means clustering (programmed manually with no use of external libraries). After repeating the previous 2 steps until convergence, we quantize the input vectors to produce an output image. The extra credit is similar except it is able to handle values of M = 4, 16, and 64, in addition to M=2.

--- For the MAIN implementation, please use MyCompression.java ---
To compile, please type in: javac MyCompression.java
To invoke some examples, please type variations of images of the following:

1. java MyCompression image1-onechannel.rgb 2 8
2. java MyCompression image2-onechannel.rgb 2 16
3. java MyCompression image3-onechannel.rgb 2 32
4. java MyCompression image4-onechannel.rgb 2 64
5. java MyCompression image1-onechannel.rgb 2 128


--- For the EXTRA CREDIT implementation, please use MyCompressionExtraCredit.java ---
To compile, please type in: javac MyCompressionExtraCredit.java
To invoke some examples, please type variations of images of the following:

Note: Use M that is divisible by the image resolution (355x288)

M=4, N=choose
1. java MyCompressionExtraCredit image1-onechannel.rgb 4 8
2. java MyCompressionExtraCredit image2-onechannel.rgb 4 16
3. java MyCompressionExtraCredit image3-onechannel.rgb 4 32
4. java MyCompressionExtraCredit image4-onechannel.rgb 4 64
5. java MyCompressionExtraCredit image1-onechannel.rgb 4 128

M=16, N=choose
1. java MyCompressionExtraCredit image1-onechannel.rgb 16 8
2. java MyCompressionExtraCredit image2-onechannel.rgb 16 16
3. java MyCompressionExtraCredit image3-onechannel.rgb 16 32
4. java MyCompressionExtraCredit image4-onechannel.rgb 16 64
5. java MyCompressionExtraCredit image1-onechannel.rgb 16 128

M=64, N=choose
1. java MyCompressionExtraCredit image1-onechannel.rgb 64 8
2. java MyCompressionExtraCredit image2-onechannel.rgb 64 16
3. java MyCompressionExtraCredit image3-onechannel.rgb 64 32
4. java MyCompressionExtraCredit image4-onechannel.rgb 64 64
5. java MyCompressionExtraCredit image1-onechannel.rgb 64 128
