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

// set default state to a given chunk
void init_chunk(Chunk *zero_chunk) {
    // default to empty
    zero_chunk->count = 0;
    zero_chunk->capacity = 0;
    zero_chunk->code = NULL;
}

// safely free an occupied chunk
void free_chunk(Chunk *target) {
    // call macro to free allocated memory
    FREE_ARRAY(uint8_t, target->code, target->capacity);
    // zero-out allocated memory - set to default state
    init_chunk(target);
}

void write_chunk(Chunk *target, uint8_t opcode) {
    // check for array allocation bound reached
    if (target->capacity <= target->count) {

        // hold current allocation size
        int old_capacity = target->capacity;
        // increase capacity size based on current allocation
        target->capacity = GROW_CAPACITY(old_capacity);
        // resize allocation
        target->code = GROW_ARRAY(uint8_t, target->code, old_capacity, target->capacity);

    }

    // add new node with specified opcode
    target->code[target->count] = opcode;
    // increase allocated size
    target->count++;
}