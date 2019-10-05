/*********************
**  Mandelbrot fractal movie generator
** clang -Xpreprocessor -fopenmp -lomp -o Mandelbrot Mandelbrot.c
** by Dan Garcia <ddgarcia@cs.berkeley.edu>
** Modified for this class by Justin Yokota and Chenyu Shi
**********************/

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <math.h>
#include "ComplexNumber.h"
#include "Mandelbrot.h"
#include "ColorMapInput.h"
#include <sys/types.h>

void printUsage(char* argv[])
{
  printf("Usage: %s <threshold> <maxiterations> <center_real> <center_imaginary> <initialscale> <finalscale> <framecount> <resolution> <output_folder> <colorfile>\n", argv[0]);
  printf("    This program simulates the Mandelbrot Fractal, and creates an iteration map of the given center, scale, and resolution, then saves it in output_file\n");
}


/*
This function calculates the threshold values of every spot on a sequence of frames. The center stays the same throughout the zoom. First frame is at initialscale, and last frame is at finalscale scale.
The remaining frames form a geometric sequence of scales, so
if initialscale=1024, finalscale=1, framecount=11, then your frames will have scales of 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1.
As another example, if initialscale=10, finalscale=0.01, framecount=5, then your frames will have scale 10, 10 * (0.01/10)^(1/4), 10 * (0.01/10)^(2/4), 10 * (0.01/10)^(3/4), 0.01 .
*/
void MandelMovie(double threshold, u_int64_t max_iterations, ComplexNumber* center, double initialscale, double finalscale, int framecount, u_int64_t resolution, u_int64_t ** output){
    //YOUR CODE HERE
    double k = exp((log(finalscale) - log(initialscale)) / (framecount - 1));
    double scale;
    for (int i = 0; i < framecount; i++) {
        // *(output + i) = (u_int64_t *) calloc((2*resolution+1) * (2*resolution+1), sizeof(u_int64_t));
        scale = initialscale * pow(k, i);
        // printf("scale is: %f", scale);
        Mandelbrot(threshold, max_iterations, center, scale, resolution, *(output + i));
        // printf("MANDELMOVIE: output values FRAME %d\n", i);
        for (int j = 0; j< pow(2*resolution+1, 2); j++) {
            // printf("mandel value %d\n", output[i][j]);
        }
    }
}

int P6IntToColor(uint8_t** colormap, int* colorcount, int resolution, u_int64_t* mandel_result, char* outputfile)
{
    //YOUR CODE HERE
    FILE* outputf = fopen(outputfile, "w+");

    int color_num = *colorcount;
    uint8_t* curr_ptr;
    u_int64_t mandel_count, color;

    fprintf(outputf, "P6 %d %d 255\n", 2 * resolution + 1, 2 * resolution + 1);

    for (int i = 0; i < 2 * resolution + 1; i++) {
        for (int j = 0; j < 2 * resolution + 1; j++) {
            if (i == 36 && j == 109) {
                continue;
            }
            mandel_count = j + (2*resolution + 1) * i;
            if (mandel_result[mandel_count] == 0) {
                curr_ptr = (uint8_t *) calloc(3, sizeof(uint8_t));
                *curr_ptr = 0;
                *(curr_ptr+1) = 0;
                *(curr_ptr+2) = 0;
                fwrite(curr_ptr, sizeof(uint8_t), 3, outputf);
                free(curr_ptr);
                // printf("frame %s, i = %d, j = %d, color is 0, mandel is %u\n", outputfile, i, j, mandel_result[mandel_count]);
            } else {
                color = (mandel_result[mandel_count] - 1) % color_num;
                if (color >= (*colorcount) || (color < 0)) {
                    printf("COLOR NUMBER EXCEEDS BOUND");
                }
                curr_ptr = *(colormap + color);
                fwrite(curr_ptr, sizeof(uint8_t), 3, outputf);
                //printf("frame %s, i = %d, j = %d, color is %u, mandel is %u\n", outputfile, i, j, color, mandel_result[mandel_count]);
                // fputc(*curr_ptr, outputf);
                // fputc(curr_ptr[1], outputf);
                // fputc(curr_ptr[2], outputf);
            }
        }
    }
    fclose(outputf);
    return 0;
}


/**************
**This main function converts command line inputs into the format needed to run MandelMovie.
**It then uses the color array from FileToColorMap to create PPM images for each frame, and stores it in output_folder
***************/
int main(int argc, char* argv[])
{
	//Tips on how to get started on main function:
	//MandelFrame also follows a similar sequence of steps; it may be useful to reference that.
	//Mayke you complete the steps below in order.

	//STEP 1: Convert command line inputs to local variables, and ensure that inputs are valid.
	/*
	Check the spec for examples of invalid inputs.
	Remember to use your solution to B.1.1 to process colorfile.
	*/

	//YOUR CODE HERE
    if (argc != 11) {
        printf("%s: Wrong number of arguments, expecting 7\n", argv[0]);
        printUsage(argv);
        return 1;
    }

    double threshold = atof(argv[1]);
    u_int64_t max_iterations = (u_int64_t) atoi(argv[2]);
    ComplexNumber* center = newComplexNumber(atof(argv[3]), atof(argv[4]));
    double initialscale = atof(argv[5]);
    double finalscale = atof(argv[6]);
    int framecount = atoi(argv[7]);
    u_int64_t resolution = (u_int64_t) atoi(argv[8]);
    char* output_folder = argv[9];
    char* colorfile = argv[10];
    // FILE* colorfile = fopen(argv[10], "w+");
    u_int64_t* mandel_result;

    if (threshold <= 0 || initialscale <= 0 || finalscale <= 0 || max_iterations <= 0 || resolution < 0) {
        printf("The threshold, scale, and max_iterations must be > 0, and the resolution must be >= 0");
        printUsage(argv);
        return 1;
    }
    if (framecount > 10000 || framecount < 0) {
        printf("The framecount must be > 0 and < 10000");
        printUsage(argv);
        return 1;
    }
    if (framecount == 1 && initialscale != finalscale) {
        printf("Initialscale and finalscale must be equal if framecount is 1");
        printUsage(argv);
        return 1;
    }

	//STEP 2: Run MandelMovie on the correct arguments.
	/*
	MandelMovie requires an output array, so make sure you allocate the proper amount of space.
	If allocation fails, free all the space you have already allocated (including colormap), then return with exit code 1.
	*/

	//YOUR CODE HERE
	int* colorcount = calloc(1, sizeof(int));
	if (colorcount == NULL) {
	    printf("Failed to allocate memory for colorcount");
	    freeComplexNumber(center);
	    return 1;
	}

    uint8_t** colormap = FileToColorMap(colorfile, colorcount);
	if (colormap == NULL) {
        printf("Failed to create colormap, the colorfile might be malformed");
        free(colorcount);
        freeComplexNumber(center);
        return 1;
	}
    u_int64_t ** output = calloc(framecount, sizeof(u_int64_t *));
    if (output == NULL) {
        printf("Fail to allocate memory for output");
        for (int i = 0; i < *colorcount; i++) {
            free(*(colormap+i));
        }
        free(colormap);
        free(colorcount);
        freeComplexNumber(center);
        return 1;
    }

    for (int i = 0; i < framecount; i++) {
        *(output + i) = (u_int64_t *) calloc((2*resolution+1) * (2*resolution+1), sizeof(u_int64_t));
        if (*(output + i) == NULL) {
            for (int j = 0; j < i; j++) {
                free(*(output+j));
            }
            free(output);
            for (int i = 0; i < *colorcount; i++) {
                free(*(colormap + i));
            }
            free(colormap);
            free(colorcount);
            freeComplexNumber(center);
            return 1;
        }
    }

    MandelMovie(threshold, max_iterations, center, initialscale, finalscale, framecount, resolution, output);
    // free(center);

	//STEP 3: Output the results of MandelMovie to .ppm files.
	/*
	Convert from iteration count to colors, and output the results into output files.
	Use what we showed you in Part B.1.2, create a seqeunce of ppm files in the output folder.
	Feel free to create your own helper function to complete this step.
	As a reminder, we are using P6 format, not P3.
	*/

	//YOUR CODE HERE

    for (int frame = 0; frame < framecount; frame++) {
        char* framefile = calloc(strlen(output_folder) + strlen("/frame00000.ppm") + 1, sizeof(char));
        sprintf(framefile, "%s/frame%05d.ppm", output_folder, frame);

        // mandel_result = *(output + frame);
        //P6IntToColor(colormap, colorcount, resolution, mandel_result, framefile);

        FILE* outputf = fopen(framefile, "w+");

        uint8_t* curr_ptr;
        u_int64_t color = 2 * resolution + 1;

        fprintf(outputf, "P6 %lu %lu 255\n", color, color);

        for (int j = 0; j < (2 * resolution + 1) * (2 * resolution + 1); j++) {
            if (output[frame][j] == 0) {
                curr_ptr = (uint8_t *) calloc(3, sizeof(uint8_t));
                *curr_ptr = 0;
                *(curr_ptr+1) = 0;
                *(curr_ptr+2) = 0;
                fwrite(curr_ptr, sizeof(uint8_t), 3, outputf);
                free(curr_ptr);
                printf("frame = %d, j = %d, color is 0, mandel is %u\n", frame, j, output[frame][j]);
            } else {
                color = (output[frame][j] - 1) % (*colorcount);
                if (color >= (*colorcount) || (color < 0)) {
                    printf("COLOR NUMBER EXCEEDS BOUND");
                }

                fwrite(colormap[color], sizeof(uint8_t), 3, outputf);
                printf("frame = %d, j = %d, color is %u, mandel is %u\n", frame, j, color, output[frame][j]);
                //printf("frame %s, i = %d, j = %d, color is %u, mandel is %u\n", outputfile, i, j, color, mandel_result[mandel_count]);
                // fputc(*curr_ptr, outputf);
                // fputc(curr_ptr[1], outputf);
                // fputc(curr_ptr[2], outputf);
            }
        }
        fclose(outputf);
        free(framefile);
    }

	//STEP 4: Free all allocated memory
	/*
	Make sure there's no memory leak.
	*/
	//YOUR CODE HERE

    freeComplexNumber(center);
	for (int i = 0; i < framecount; i++) {
	    free(*(output+i));
	}
	free(output);
    for (int i = 0; i < (*colorcount); i++) {
        free(*(colormap+i));
    }
    free(colormap);
    free(colorcount);

	return 0;
}
