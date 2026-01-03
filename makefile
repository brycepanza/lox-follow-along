BUILD := ./build


####### c #######
CC := gcc
CFLAGS := -Wall

TARGET := clox

C_SRC := ./c/

SOURCES := $(shell find $(C_SRC) -name "*.c")

# Substitute .c endings with .o endings to create a list of object files
OBJECTS := $(SOURCES:.c=.o)

c_compile:
	mkdir -p $(BUILD)
	$(CC) $(CFLAGS) $(C_SRCS) -o $(TARGET)

# call c execution
c: c_compile
	./$(TARGET)


####### java #######
JC := javac

J_SRC := ./java/com/craftinginterpreters/lox
BUILD := ./build

J_SRCS := $(shell find $(J_SRC) -name  "*.java")

J_MAIN := com.craftinginterpreters.lox.Lox

# change entries if needed
ENTRY ?= J_MAIN

MANIFEST_PATH := manifest.txt
JAR := app.jar

all: jar

j_compile:
	mkdir -p $(BUILD)
	$(JC) -d $(BUILD) $(J_SRCS)

jar: j_compile
	echo "Main-Class: $($(ENTRY))" > $(MANIFEST_PATH)
	jar cfm $(JAR) $(MANIFEST_PATH) -C $(BUILD) .

# call java execution
java: jar
	java -jar $(JAR)


####### clean #######
clean:
	rm -rf $(BUILD) $(JAR) $(MANIFEST_PATH)


##### tool-specific derictives #####

TOOL_DIR := ./java/com/craftinginterpreters/tool
TOOL_BUILD_DIR := ./tool_build

TOOLS := $(shell find $(TOOL_DIR) -name  "*.java")

TOOL_MAIN := com.craftinginterpreters.tool.GenerateAst
TOOL_MANIFEST := tool_manifest.txt
TOOL_JAR = tool.jar

tool: tool_compile

tool_compile:
	mkdir -p $(TOOL_BUILD_DIR)
	$(JC) -d $(TOOL_BUILD_DIR) $(TOOLS)

tool_jar: tool_compile
	echo "Main-Class: $(TOOL_MAIN)\n\n" > $(TOOL_MANIFEST)
	jar cfm $(TOOL_JAR) $(TOOL_MANIFEST) -C $(TOOL_BUILD_DIR) .

tool_run: tool_jar
	java -jar $(TOOL_JAR) lox

tool_clean:
	rm -rf $(TOOL_BUILD_DIR) $(TOOL_JAR) $(TOOL_MANIFEST)
