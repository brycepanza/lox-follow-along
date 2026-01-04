/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#ifndef MEMORY_H
#define MEMORY_H

#include "common.h"

#define MIN_CAPACITY 8      // minimum allocation size
#define RESIZE_FACTOR 2     // resize array on allocation limit reached

// macro to assign proper array size
    // single byte on empty array, double current size if not
#define GROW_CAPACITY(capacity) \
    ((capacity) < MIN_CAPACITY ? MIN_CAPACITY : (capacity) * RESIZE_FACTOR)

// macro for interfacing actual array reallocation function
    // passes correct byte counts to array reallocation function based on given size
#define GROW_ARRAY(type, arr_ptr, old_count, new_count) \
    (type *)reallocate(arr_ptr, sizeof(type) * old_count, sizeof(type) * new_count)

// macro to set reset allocation size for a given dynamic array - wrapper for reallocate()
#define FREE_ARRAY(type, arr_ptr, old_count) \
    reallocate(arr_ptr, sizeof(type) * old_count, 0)

void *reallocate(void *arr_ptr, size_t old_size, size_t new_size);

#endif