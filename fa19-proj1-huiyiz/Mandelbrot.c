/*********************
**  Mandelbrot fractal
** clang -Xpreprocessor -fopenmp -lomp -o Mandelbrot Mandelbrot.c
** by Dan Garcia <ddgarcia@cs.berkeley.edu>
** Modified for this class by Justin Yokota and Chenyu Shi
**********************/

#include <stdio.h>
#include <stdlib.h>
#include "ComplexNumber.h"
#include "Mandelbrot.h"
#include <sys/types.h>

/*
This function returns the number of iterations before the initial point >= the threshold.
If the threshold is not exceeded after maxiters, the function returns 0.
*/
u_int64_t MandelbrotIterations(u_int64_t maxiters, ComplexNumber * point, double threshold)
{
    //YOUR CODE HERE
	ComplexNumber* curr = newComplexNumber(0.0, 0.0);
	ComplexNumber* temp;
	for (int i = 1; i <= maxiters; i++) {
		temp = ComplexProduct(curr, curr);
		freeComplexNumber(curr);
		curr = ComplexSum(temp, point);
		freeComplexNumber(temp);
		if (!(ComplexAbs(curr) < threshold)) {
			freeComplexNumber(curr);
			return i; 
		}
	}
	freeComplexNumber(curr);
	return 0;
}

/*
This function calculates the Mandelbrot plot and stores the result in output.
The number of pixels in the image is resolution * 2 + 1 in one row/column. It's a square image.
Scale is the the distance between center and the top pixel in one dimension.
*/
void Mandelbrot(double threshold, u_int64_t max_iterations, ComplexNumber* center, double scale, u_int64_t resolution, u_int64_t * output){
    //YOUR CODE HERE
    u_int64_t result;
    int count = 0;
    if (resolution == 0) {
        result = MandelbrotIterations(max_iterations, center, threshold);
        *output = result;
    }

    //
    double real, imag;
    ComplexNumber* point_C;

    for (u_int64_t i = 0; i < 2 * resolution + 1; i++) {
        for (u_int64_t j = 0; j < 2 * resolution + 1; j++) {
            real = Re(center) - scale + j * scale / resolution;
            imag = Im(center) + scale - i * scale / resolution;
            point_C = newComplexNumber(real, imag);
            result = MandelbrotIterations(max_iterations, point_C, threshold);
            output[j + i * (2*resolution+1)] = result;
            count += 1;
            freeComplexNumber(point_C);
            // printf("MANDELBROT: result for i = %u, j = %u is %f\n", i, j, *(output+count));
        }
    }
    //

    /*
    double quant = scale / resolution;
	double real = Re(center) - scale;
	double imag = Im(center) + scale;
	ComplexNumber* point_C;

	for (u_int64_t i = 0; i < 2 * resolution + 1; i++) {
	    for (u_int64_t j = 0; j < 2 * resolution + 1; j++) {
            point_C = newComplexNumber(real + j*quant, imag - i*quant);
            result = MandelbrotIterations(max_iterations, point_C, threshold);
            *(output+count) = result;
            count += 1;
            freeComplexNumber(point_C);
	    }
	}
    */


}


