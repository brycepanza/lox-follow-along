/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#ifndef VS_H
#define VM_H

#include "chunk.h"

// exit states for bytecode execution
typedef enum {
    INTERPRET_OK,               // valid state
    INTERPRET_COMPILE_ERROR,    // error in bytecode generation
    INTERPRET_RUNTIME_ERROR     // error in bytecode execution
} InterpretResult;

// structure for virtual machine to execute bytecode
typedef struct {
    Chunk *chunk;               // chunk of bytecode to execute
    uint8_t *instruction_ptr;   // instruction pointer for next instruction to execute
} Vm;

void init_vm();

void free_vm();

InterpretResult interpret(Chunk *chunk);

#endif