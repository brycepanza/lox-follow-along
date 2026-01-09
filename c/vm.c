/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdio.h>

#include "common.h"
#include "compiler.h"
#include "debug.h"
#include "vm.h"

// global scope for virtual machine
Vm vm;

// set default stack state for global virtual machine struct
static void reset_stack() {
    vm.stack_top = vm.stack;    // point to base
}

// set up state for global virtual machine
void init_vm() {
    // collapse stack to default state
    reset_stack();
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
// apply a given operation to the top two values in the values stack
    // no type checking for operation
    // no check for valid stack state
#define BINARY_OP(op) \
    do { \
        double b = pop(); \
        double a = pop(); \
        push(a op b); \
    } while (false) // single pass

    // master loop for single-instruction execution
    for (;;) {
    
// check for existing macro definition
#ifdef DEBUG_TRACE_EXECUTION
    // log current stack state
    printf("\t\t");
    for (Value *slot = vm.stack; slot < vm.stack_top; slot++) {
        printf("[ ");
        print_value(*slot);
        printf(" ]");
    }
    printf("\n");
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
                push(constant); // add to stack
                break;  // continue execution, no exit
            case OP_ADD:        BINARY_OP(+); break;    // apply operation with preprocessor >>>
            case OP_SUBTRACT:   BINARY_OP(-); break;
            case OP_MULTIPY:    BINARY_OP(*); break;
            case OP_DIVIDE:     BINARY_OP(/); break;    // <<<
            case OP_NEGATE: push(-pop()); break;    // remove top value and append negative of popped value
            case OP_RETURN:
                // get value from stack
                print_value(pop());
                printf("\n");
                return INTERPRET_OK;    // interpret success exit condition
        }
    }
// close macros
#undef READ_BYTE
#undef READ_CONSTANT
#undef BINARY_OP
}

// called for execution of a given chunk of bytecode
InterpretResult interpret(const char *source_code) {
    // hold bytecode chunk for source code buffer
    Chunk chunk;
    init_chunk(&chunk);

    // attempt compiling and check for exit with errors
    if (!compile(source_code, &chunk)) {
        // deallocate resourced
        free_chunk(&chunk);
        // send error to caller
        return INTERPRET_COMPILE_ERROR;
    }

    // load compiled chunk to virtual machine
    vm.chunk = &chunk;
    // point to first loaded instruction
    vm.instruction_ptr = vm.chunk->code;

    // interpret compiled code and hold exit status
    InterpretResult result = run();

    // deallocate resource buffer
    free_chunk(&chunk);
    return result;
}

// append to virtual machine's stack of numbers
void push(Value append_val) {
    // put new value at location of stack pointer and advance stack pointer after write
    *(vm.stack_top++) = append_val;
}

Value pop() {
    // regress stack pointer to last write and pass dereferenced value to caller
    return *(--vm.stack_top);
}