/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main(int argc, const char *argv[]) {

    // initialize environment for bytecode execution
    init_vm();

    Chunk build_chunk;

    init_chunk(&build_chunk);

    int constant = add_constant(&build_chunk, 1.2);
    write_chunk(&build_chunk, OP_CONSTANT, 123); // opcode
    write_chunk(&build_chunk, constant, 123);    // value

    constant = add_constant(&build_chunk, 3.4);
    write_chunk(&build_chunk, OP_CONSTANT, 123);
    write_chunk(&build_chunk, constant, 123);

    write_chunk(&build_chunk, OP_ADD, 123);

    constant = add_constant(&build_chunk, 5.6);
    write_chunk(&build_chunk, OP_CONSTANT, 123);
    write_chunk(&build_chunk, constant, 123);

    write_chunk(&build_chunk, OP_DIVIDE, 123);

    write_chunk(&build_chunk, OP_NEGATE, 123);

    write_chunk(&build_chunk, OP_RETURN, 123);

    free_vm();

    interpret(&build_chunk);

    free_chunk(&build_chunk);

    return 0;
}