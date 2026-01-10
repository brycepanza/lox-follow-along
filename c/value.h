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

// value types recognized
typedef enum {
    VAL_BOOL,
    VAL_NIL,
    VAL_NUMBER
} ValueType;

// values as tagged unions
typedef struct {
    ValueType type;     // tag for recognized type
    union {
        bool boolean;
        double number;
    } as;
} Value;

// comparison macros for manual type checking - requires Value struct as parameter
#define IS_BOOL(value)      ((value).type == VAL_BOOL)
#define IS_NIL(value)       ((value).type == VAL_NIL)
#define IS_NUMBER(value)    ((value).type == VAL_NUMBER)

// macros for recognizing union payload type for value extraction
#define AS_BOOL(value)      ((value).as.boolean)
#define AS_NUMBER(value)    ((value).as.number)

// macros for assigning and casting proper shape - no type checking
    // type and value conversions happen together - valid type state
#define BOOL_VAL(value)     ((Value){VAL_BOOL, {.boolean = value}})
#define NIL_VAL             ((Value){VAL_NIL, {.number = 0}})
#define NUMBER_VAL(value)   ((Value){VAL_NUMBER, {.number = value}})

// dynamic array for Lox values
typedef struct {
    int count;      // used space
    int capacity;   // total allocated space
    Value *values;  // point to start of allocation
} ValueArray;

bool values_equal(Value a, Value b);

void init_value_array(ValueArray *zero_arr);

void write_value_array(ValueArray *arr, Value val);

void free_value_array(ValueArray *arr);

void print_value(Value value);

#endif