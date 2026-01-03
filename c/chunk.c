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
}

// safely free an occupied chunk
void free_chunk(Chunk *target_chunk) {
    // call macro to free allocated memory
    FREE_ARRAY(uint8_t, target_chunk->code, target_chunk->capacity);
    // zero-out allocated memory - set to default state
    init_chunk(target_chunk);
}

// make an insertion to a targeted chunk of opcodes
void write_chunk(Chunk *target_chunk, uint8_t opcode) {
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

    }

    // add new node with specified opcode
    target_chunk->code[target_chunk->count] = opcode;
    // increase allocated size
    target_chunk->count++;
}