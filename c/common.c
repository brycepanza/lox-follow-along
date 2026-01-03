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

    write_chunk(&build_chunk, OP_RETURN);

    // debug view chunks
    disassemble_chunk(&build_chunk, "test chunk");

    free_chunk(&build_chunk);

    return 0;
}