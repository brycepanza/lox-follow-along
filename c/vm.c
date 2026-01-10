/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include <stdarg.h>
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

// error generation during execution
static void runtime_error(const char *format, ...) {
    // variant arguments to iterate over
    va_list args;
    // initialize list of arguments
    va_start(args, format);
    // log applied format to standard error
    vfprintf(stderr, format, args);
    // end resource utilization
    va_end(args);
    // log cleanup
    fputs("\n", stderr);

    // get location of error (with chunk->code as pointer to allocation start)
    size_t instruction = vm.instruction_ptr - vm.chunk->code - 1;
    // get line number associated with instruction as index
    int line = vm.chunk->lines[instruction];
    // log location
    fprintf(stderr, "[line %d] in script\n", line);
    // clear stack after error
    reset_stack();
}

// set up state for global virtual machine
void init_vm() {
    // collapse stack to default state
    reset_stack();
}

// clear state and free resources
void free_vm() {
    
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

// return copy of lox Value structure a given distance from top
static Value peek(int distance) {
    return vm.stack_top[-1 - distance];     // <-- no bounds checking ?
}


// execute current state in global virtual machine
    // interfaced by library function interpret() - pass exit status to caller
static InterpretResult run() {

// scoped macro for pointer advacement
#define READ_BYTE() (*vm.instruction_ptr++)
// reads specified constant in values table and advances program counter
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
// apply a given operation to the top two values in the values stack
    // requires a casting macro for Value type and an operation to apply
#define BINARY_OP(value_type, op) \
    do { \
        /* check for invalid types with operation on stack state */ \
        if (!IS_NUMBER(peek(0)) || !IS_NUMBER(peek(1))) { \
            /* exit with error */ \
            runtime_error("Operands must be numbers."); \
            return INTERPRET_RUNTIME_ERROR; \
        } \
        /* get values for opreation from stack */ \
        double a = AS_NUMBER(pop()); \
        double b = AS_NUMBER(pop()); \
        push(value_type(a op b)); \
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
            case OP_ADD:        BINARY_OP(NUMBER_VAL, +); break;    // apply operation with preprocessor >>>
            case OP_SUBTRACT:   BINARY_OP(NUMBER_VAL, -); break;
            case OP_MULTIPY:    BINARY_OP(NUMBER_VAL, *); break;
            case OP_DIVIDE:     BINARY_OP(NUMBER_VAL, /); break;    // <<<
            case OP_NEGATE:
                // check for invalid type for operation
                if (!IS_NUMBER(peek(0))) {
                    // exit with error
                    runtime_error("Operand must be a number.");
                    return INTERPRET_RUNTIME_ERROR;
                }
                // modify value in stack to negate
                push(NUMBER_VAL(-AS_NUMBER(pop())));
                break;
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