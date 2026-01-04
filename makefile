BUILD := ./build


####### c #######
CC := gcc
CFLAGS := -Wall

TARGET := clox

C_SRC := ./c

SOURCES = $(wildcard $(C_SRC)/*.c)

# object files should go to build folder
OBJECTS = $(SOURCES:$(C_SRC)/%.c=$(BUILD)/%.o)

# link object files to executable
$(TARGET): $(OBJECTS)
	$(CC) $(CFLAGS) $^ -o $@

# compile c to object files
$(BUILD)/%.o: $(C_SRC)/%.c | $(BUILD)
	$(CC) $(CFLAGS) -c $< -o $@

# require directory for object and executable files
$(BUILD):
	mkdir -p $(BUILD)

# call c execution
c_run: $(TARGET)
	./$(TARGET)


####### java #######
JC := javac

J_SRC := ./java/com/craftinginterpreters/lox

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
	rm -rf $(BUILD) $(JAR) $(MANIFEST_PATH) $(TARGET)


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
