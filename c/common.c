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

int main(int argc, const char *argv[]) {

    Chunk build_chunk;

    init_chunk(&build_chunk);

    int constant = add_constant(&build_chunk, 1.2);
    write_chunk(&build_chunk, OP_CONSTANT); // opcode
    write_chunk(&build_chunk, constant);    // value

    write_chunk(&build_chunk, OP_RETURN);

    // debug view chunks
    disassemble_chunk(&build_chunk, "test chunk");

    free_chunk(&build_chunk);

    return 0;
}