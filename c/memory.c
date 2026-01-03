/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdlib.h>

#include "memory.h"

// resize a given dynamic array to a specified number of bytes
void *reallocate(void *arr_ptr, size_t old_size, size_t new_size) {
    // check for deallocation
    if (!new_size) {                // negative value given ?
        // dealloacte occupied memory
        free(arr_ptr);
        arr_ptr = NULL;
        // exit
        return NULL;
    }

    // give new size allocation
    void *realloc_ptr = realloc(arr_ptr, new_size); // realloc attempts to append memory region

    // check for allocation failure and exit with a failure
    if (!realloc_ptr) exit(1);

    // pass pointer to newly allocated memory to caller
    return realloc_ptr;
}