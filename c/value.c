/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>

#include "memory.h"
#include "value.h"

// set a given dynamic array struct to default state
void init_value_array(ValueArray *zero_arr) {
    zero_arr->values = NULL;
    zero_arr->capacity = 0;
    zero_arr->count = 0;
}

// append new value to a dynamic array
void write_value_array(ValueArray *arr, Value val) {
    // check for limit reached
    if (arr->capacity <= arr->count) {
        int old_capacity = arr->capacity;
        // resize
        arr->capacity = GROW_CAPACITY(old_capacity);
        arr->values = GROW_ARRAY(Value, arr->values, old_capacity, arr->capacity);
    }

    // insert new element
    arr->values[arr->count] = val;
    // update dynamic array state
    arr->count++;
}

// clear allocated space by dynamic array and reset state variables
void free_value_array(ValueArray *arr) {
    // deallocate space
    FREE_ARRAY(Value, arr->values, arr->count);
    // set struct state to default
    init_value_array(arr);
}

void print_value(Value value) {
    switch (value.type) {
        case VAL_BOOL:
            printf(AS_BOOL(value) ? "true" : "false");
            break;
        case VAL_NIL: printf("nil"); break;
        case VAL_NUMBER: printf("%g", AS_NUMBER(value)); break;
    }
}