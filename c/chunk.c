/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdlib.h>

#include "chunk.h"
#include "memory.h"

// set default state to a given chunk of bytecode
void init_chunk(Chunk *zero_chunk) {
    // default to empty
    zero_chunk->count = 0;
    zero_chunk->capacity = 0;
    zero_chunk->code = NULL;
    zero_chunk->lines = NULL;
    init_value_array(&zero_chunk->constants);
}

// safely free an occupied chunk
void free_chunk(Chunk *target_chunk) {
    // call macro to free allocated memory
    FREE_ARRAY(uint8_t, target_chunk->code, target_chunk->capacity);
    FREE_ARRAY(int, target_chunk->lines, target_chunk->capacity);
    // release constants
    free_value_array(&target_chunk->constants);
    // zero-out allocated memory - set to default state
    init_chunk(target_chunk);
}

// make an insertion to a targeted chunk of opcodes and literats
void write_chunk(Chunk *target_chunk, uint8_t byte, int line) {
    // check for array allocation bound reached
    if (target_chunk->capacity <= target_chunk->count) {

        // hold current allocation size
        int old_capacity = target_chunk->capacity;
        // increase capacity size based on current allocation
        target_chunk->capacity = GROW_CAPACITY(old_capacity);
        // resize allocation
        target_chunk->code = GROW_ARRAY(uint8_t,
                                        target_chunk->code,
                                        old_capacity,
                                        target_chunk->capacity);
        // corresponding line size increase
        target_chunk->lines = GROW_ARRAY(int,
                                        target_chunk->lines,
                                        old_capacity,
                                        target_chunk->capacity);

    }

    // add new node with specified byte
    target_chunk->code[target_chunk->count] = byte;
    // provide correct line in found in source code
    target_chunk->lines[target_chunk->count] = line;
    // increase allocated size
    target_chunk->count++;
}

// append a constant value to an array of constants in a given chunk and return size of constants array
int add_constant(Chunk *chunk, Value new_const) {
    // append value
    write_value_array(&chunk->constants, new_const);
    // send updated size to caller
    return chunk->constants.count - 1;
}