/*********************
**  Color Map generator
** Skeleton by Justin Yokota
**********************/

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <math.h>
#include <string.h>
#include "ColorMapInput.h"


/**************
**This function reads in a file name colorfile.
**It then uses the information in colorfile to create a color array, with each color represented by an int[3].
***************/
uint8_t** FileToColorMap(char* colorfile, int* colorcount)
{
	//YOUR CODE HERE
	if  (colorfile == NULL) {
	    return NULL;
	}

	FILE* file = fopen(colorfile, "r");
    if (fscanf(file, "%d ", colorcount) != 1) {
        fclose(file);
        return NULL;
    }

    // uint8_t* first = calloc(3, sizeof(uint8_t));
    uint8_t** toReturn = (uint8_t **) calloc(*colorcount, sizeof(uint8_t*));
    // toReturn = &first;
    uint8_t* curr_ptr = *toReturn;
    int lin, curr;
    int col_count = *colorcount;

    if (toReturn == NULL) {
        fclose(file);
        return NULL;
    }

    for (curr = 0; curr < col_count; curr++) {
        *(toReturn + curr) = (uint8_t *) calloc(3, sizeof(uint8_t));
        curr_ptr = *(toReturn + curr);
        if (curr_ptr == NULL) {
            for (int i = 0; i < curr; i++) {
                free(*(toReturn + i));
            }
            free(toReturn);
            fclose(file);
            return NULL;
        }
        int lin = fscanf(file, "%hhu %hhu %hhu ", curr_ptr, curr_ptr+1, curr_ptr+2);
        if (lin == 0) {
            for (int i = 0; i < curr; i++) {
                free(*(toReturn + i));
            }
            free(toReturn);
            fclose(file);
            return NULL;
        }
        // IF RANGE IS NOT BETWEEN [0, 255]
        if (*curr_ptr < 0 || *curr_ptr > 255 || *(curr_ptr+1) < 0 || *(curr_ptr+1) > 255 || *(curr_ptr+2) < 0 || *(curr_ptr+2) > 255) {
            for (int i = 0; i < curr; i++) {
                free(*(toReturn + i));
            }
            free(toReturn);
            fclose(file);
            return NULL;
        }
    }

	fclose(file);
    return toReturn;
}


