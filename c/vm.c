/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>

#include "common.h"
#include "debug.h"
#include "vm.h"

// global scope for virtual machine
Vm vm;

// set up state for global virtual machine
void init_vm() {

}

// clear state and free resources
void free_vm() {
    
}

// execute current state in global virtual machine
    // interfaced by library function interpret() - pass exit status to caller
static InterpretResult run() {

// scoped macro for pointer advacement
#define READ_BYTE() (*vm.instruction_ptr++)
// reads specified constant in values table and advances program counter
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])

    // master loop for single-instruction execution
    for (;;) {
    
// check for existing macro definition
#ifdef DEBUG_TRACE_EXECUTION
    // utilize debug library function for logging current instruction
        // array as contiguous block - pointer addresses adjacent, recast difference for parameter type
    disassemble_instruction(vm.chunk, (int)(vm.instruction_ptr - vm.chunk->code));
#endif

        // hold current instruction and advance progrma couner
        uint8_t instruction = READ_BYTE();

        // check for type
        switch(instruction) {
            case OP_CONSTANT:
                Value constant = READ_CONSTANT();
                print_value(constant);
                printf("\n");
                break;  // continue execution, no exit
            case OP_RETURN:
                return INTERPRET_OK;    // interpret success exit condition
        }
    }
// close macros
#undef READ_BYTE
#undef READ_CONSTANT
}

// called for execution of a given chunk of bytecode
InterpretResult interpret(Chunk *chunk) {
    vm.chunk = chunk;                       // assign to global vm by pointer
    vm.instruction_ptr = vm.chunk->code;    // set program counter to start of instructions

    // execute code in vm and pass exit status to caller
    return run();
}