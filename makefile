JC := javac

SRC_DIR := ./lox
BUILD_DIR := ./build

# get java files from lox/ and tool/ helper classes

SRCS := $(shell find $(SRC_DIR) -name  "*.java")

MAIN := com.craftinginterpreters.lox.Lox
PRINT := com.craftinginterpreters.lox.AstPrinter

MANIFEST_PATH := manifest.txt
JAR := app.jar

# default entry point - allow separate entry points
ENTRY ?= MAIN

all: jar

compile:
	mkdir -p $(BUILD_DIR)
	$(JC) -d $(BUILD_DIR) $(SRCS)

jar: compile
	echo "Main-Class: $($(ENTRY))" > $(MANIFEST_PATH)
# 	jar cfm $(JAR) manifest.txt -C $(BUILD_DIR) .
	jar cfm $(JAR) $(MANIFEST_PATH) -C $(BUILD_DIR) .

run: jar
	java -jar $(JAR)

# run with separate entry point given
print:
	$(MAKE) run ENTRY=PRINT

clean:
	rm -rf $(BUILD_DIR) $(JAR) $(MANIFEST_PATH)

##### tool-specific derictives #####

TOOL_DIR := ./tool
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