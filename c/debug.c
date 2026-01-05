/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>

#include "debug.h"
#include "value.h"

// display opcodes in a given chunk of bytecode
void disassemble_chunk(Chunk *chunk, const char *name) {
    printf("== %s ==\n", name);

    // iterate for allocated opcodes
    for (int offset = 0; offset < chunk->count;) {
        // view instruction and move index variable
        offset = disassemble_instruction(chunk, offset);
    }
}

static int constant_instruction(const char *name, Chunk *chunk, int offset) {
    // hold index of constant for storage information
    uint8_t constant_index = chunk->code[offset + 1];
    printf("%-16s %4d '", name, constant_index);
    print_value(chunk->constants.values[constant_index]);
    printf("'\n");

    // two nodes traversed
    return offset + 2;
}

// helper function for logging and index adjusting on simple instructions
static int simple_instruction(const char *name, int offset) {
    printf("%s\n", name);
    return offset + 1;      // single byte occupied
}

// display opcode instruction at a given offset and returns the next index in line
int disassemble_instruction(Chunk *chunk, int offset) {

    printf("%04d ", offset);

    if (offset > 0 && chunk->lines[offset] == chunk->lines[offset - 1]) {
        printf("   | ");
    } else {
        printf("%4d ", chunk->lines[offset]);
    }

    // get specified opcode
    uint8_t instruction = chunk->code[offset];

    // instruction handling based on type
    switch (instruction) {
        case OP_CONSTANT:
            return constant_instruction("OP_CONSTANT", chunk, offset);
        case OP_ADD:
            return simple_instruction("OP_ADD", offset);
        case OP_SUBTRACT:
            return simple_instruction("OP_SUBTRACT", offset);
        case OP_MULTIPY:
            return simple_instruction("OP_MULTIPLY", offset);
        case OP_DIVIDE:
            return simple_instruction("OP_DIVIDE", offset);
        case OP_NEGATE:
            return simple_instruction("OP_NEGATE", offset);
        case OP_RETURN:
            return simple_instruction("OP_RETURN", offset);
        default:
            printf("Unknown opcode %d\n", instruction);
            return offset + 1;  // unrecognized instruction, move to next byte
    }
}