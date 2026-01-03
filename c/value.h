/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#ifndef VALUE_H
#define VALUE_H

#include "common.h"

typedef double Value;

// dynamic array for Lox values
typedef struct {
    int count;      // used space
    int capacity;   // total allocated space
    Value *values;  // point to start of allocation
} ValueArray;

void init_value_array(ValueArray *zero_arr);

void write_value_array(ValueArray *arr, Value val);

void free_value_array(ValueArray *arr);

void print_value(Value value);

#endif