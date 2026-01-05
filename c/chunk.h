/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#ifndef CHUNK_H
#define CHUNK_H

#include "common.h"
#include "value.h"

// define types of opcodes used
typedef enum {
    OP_CONSTANT,    // constant value
    OP_ADD,         // binary operator, creates value >>>
    OP_SUBTRACT,
    OP_MULTIPY,
    OP_DIVIDE,      // <<<
    OP_NEGATE,      // negative sign for a literal
    OP_RETURN       // return from current function
} OpCode;

// holds chunk of bytecode as opcodes and metadata
typedef struct {
    int count;              // number of occupied elements in dynamic array - next open index
    int capacity;           // number of allocated spots in dynamic array
    uint8_t *code;          // pointer to single-byte opcode identifier
    int *lines;             // associates a source code line number for each opcode at the same index
    ValueArray constants;   // dynamic array of constant values
} Chunk;

// function prototypes

void init_chunk(Chunk *zero_chunk);

void free_chunk(Chunk *target_chunk);

void write_chunk(Chunk *target_chunk, uint8_t opcode, int line);

int add_constant(Chunk *chunk, Value new_const);

#endif