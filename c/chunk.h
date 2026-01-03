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

// define types of opcodes used
typedef enum {
    OP_RETURN   // return from current function
} OpCode;

// holds chunk of bytecode as opcodes and metadata
typedef struct {
    int count;      // number of occupied elements in dynamic array
    int capacity;   // number of allocated spots in dynamic array
    uint8_t *code;  // pointer to single-byte opcode identifier
} Chunk;

// function prototypes

void init_chunk(Chunk *zero_chunk);

void free_chunk(Chunk *target_chunk);

void write_chunk(Chunk *target_chunk, uint8_t opcode);

#endif