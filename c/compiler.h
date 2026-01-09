/*
#   #############################################
#   #                                           #
#   From Robert Nystrom's 'Crafting Interpreters'
#   #                                           #
#   #############################################
*/

#ifndef COMPILER_H
#define COMPILER_H

#include "vm.h"

bool compile(const char *source_code, Chunk *fill_chunk);

#endif