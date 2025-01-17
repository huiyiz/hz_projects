/*********************
**  Color Palette generator
** Skeleton by Justin Yokota
**********************/

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <math.h>
#include <string.h>
#include "ColorMapInput.h"

//You don't need to call this function but it helps you understand how the arguments are passed in 
void usage(char* argv[])
{
	printf("Incorrect usage: Expected arguments are %s <inputfile> <outputfolder> <width> <heightpercolor>", argv[0]);
}

//Creates a color palette image for the given colorfile in outputfile. Width and heightpercolor dictates the dimensions of each color. Output should be in P3 format
int P3colorpalette(char* colorfile, int width, int heightpercolor, char* outputfile)
{
    //YOUR CODE HERE
	if (colorfile == NULL || width < 1 || heightpercolor < 1) {
	    return 1;
	}
    FILE* outputf = fopen(outputfile, "w");
	int* colorcount = (int *) calloc(1, sizeof(int));
    uint8_t** ptr2d = FileToColorMap(colorfile, colorcount);
    if (ptr2d == NULL) {
        free(colorcount);
        return 1;
    }
    int color_num = *colorcount;
    uint8_t* curr_ptr;

    fprintf(outputf, "P3 %d %d 255\n", width, heightpercolor * (*colorcount));
    // printf("P3 %d %d 255\n", width, heightpercolor * (*colorcount));

    for (int color = 0; color < color_num; color++) {
        for (int vert = 0; vert < heightpercolor; vert++) {
            for (int hori = 0; hori < width - 1; hori++) {
                curr_ptr = *(ptr2d + color);
                // CHECK
                // uint8_t uint1, uint2, uint3;
                // uint1 = *curr_ptr;
                // uint2 = *(curr_ptr+1);
                // uint3 = *(curr_ptr+2);
                // CHECK
                // printf("%hhu %hhu %hhu ", *curr_ptr, *(curr_ptr+1), *(curr_ptr+2));
                fprintf(outputf, "%hhu %hhu %hhu ", *curr_ptr, *(curr_ptr+1), *(curr_ptr+2));
            }
            curr_ptr = *(ptr2d + color);
            fprintf(outputf, "%hhu %hhu %hhu\n", *curr_ptr, *(curr_ptr+1), *(curr_ptr+2)); // last one has no blank but new line
        }
        // CHECK LAST LINE SHOULD NOT CONTAIN \n.
    }

    for (int i = 0; i < *colorcount; i++) {
        free(*(ptr2d + i));
    }
    free(ptr2d);
    free(colorcount);
    fclose(outputf);
	return 0;
}

//Same as above, but with P6 format
int P6colorpalette(char* colorfile, int width, int heightpercolor, char* outputfile)
{
	//YOUR CODE HERE
    if (colorfile == NULL || width < 1 || heightpercolor < 1) {
        return 1;
    }
    FILE* outputf = fopen(outputfile, "w");
    int* colorcount = (int *) calloc(1, sizeof(int));
    uint8_t** ptr2d = FileToColorMap(colorfile, colorcount);
    if (ptr2d == NULL) {
        free(colorcount);
        return 1;
    }
    int color_num = *colorcount;
    uint8_t* curr_ptr;

    printf("P6 %d %d 255\n", width, heightpercolor * (*colorcount));
    fprintf(outputf, "P6 %d %d 255\n", width, heightpercolor * (*colorcount));

    for (int color = 0; color < color_num; color++) {
        for (int vert = 0; vert < heightpercolor; vert++) {
            for (int hori = 0; hori < width; hori++) {
                curr_ptr = *(ptr2d + color);
                fwrite(curr_ptr, sizeof(uint8_t), 3, outputf);
            }
        }
    }
    for (int i = 0; i < *colorcount; i++) {
        free(*(ptr2d + i));
    }
    free(ptr2d);
    free(colorcount);
    fclose(outputf);
	return 0;
}


//The one piece of c code you don't have to read or understand. Still, might as well read it, if you have time.
int main(int argc, char* argv[])
{
	if (argc != 5)
	{
		usage(argv);
		return 1;
	}
	int width = atoi(argv[3]);
	int height = atoi(argv[4]);
	char* P3end = "/colorpaletteP3.ppm";
	char* P6end = "/colorpaletteP6.ppm";
	char buffer[strlen(argv[2]) + strlen(P3end)];
	sprintf(buffer, "%s%s", argv[2], P3end);
	int failed = P3colorpalette(argv[1], width, height, buffer);
	if (failed)
	{
		printf("Error in making P3colorpalette");
		return 1;
	}
	sprintf(buffer, "%s%s", argv[2], P6end);
	failed = P6colorpalette(argv[1], width, height, buffer);
	if (failed)
	{
		printf("Error in making P6colorpalette");
		return 1;
	}
	printf("P3colorpalette and P6colorpalette ran without error, output should be stored in %s%s, %s%s", argv[2], P3end, argv[2], P6end);
	return 0;
}




