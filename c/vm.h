/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#ifndef VM_H
#define VM_H

#include "chunk.h"
#include "value.h"

#define STACK_MAX 256

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
    Value stack[STACK_MAX];     // stack for local frames in bytecode evaluation
    Value *stack_top;           // next available index in stack - zero at empty stack
} Vm;

void init_vm();

void free_vm();

InterpretResult interpret(const char *source_code);

void push(Value append_val);

Value pop();

#endif