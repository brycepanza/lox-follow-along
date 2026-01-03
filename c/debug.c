/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>

#include "debug.h"

// display opcodes in a given chunk of bytecode
void disassemble_chunk(Chunk *chunk, const char *name) {
    printf("== %s ==\n", name);

    // iterate for allocated opcodes
    for (int offset = 0; offset < chunk->count;) {
        // view instruction and move index variable
        offset = disassemble_instruction(chunk, offset);
    }
}

// helper function for logging and index adjusting on simple instructions
static int simple_instruction(const char *name, int offset) {
    printf("%s\n", name);
    return offset + 1;      // single byte occupied
}

// display opcode instruction at a given offset and returns the next index in line
int disassemble_instruction(Chunk *chunk, int offset) {

    printf("%04d ", offset);

    // get specified opcode
    uint8_t instruction = chunk->code[offset];

    // instruction handling based on type
    switch (instruction) {
        case OP_RETURN:
            return simple_instruction("OP_RETURN", offset);
        default:
            printf("Unknown opcode %d\n", instruction);
            return offset + 1;  // unrecognized instruction, move to next byte
    }
}