JC := javac

SRC_DIR := ./lox
BUILD_DIR := ./build

TOOL_DIR := ./tool
TOOL_BUILD_DIR := ./tool_build

# get java files from lox/ and tool/ helper classes

SRCS := $(shell find $(SRC_DIR) -name  "*.java")

TOOLS := $(shell find $(TOOL_DIR) -name  "*.java")

MAIN := com.craftinginterpreters.lox.Lox
MANIFEST_PATH := manifest.txt
JAR := app.jar

TOOL_MAIN := com.craftinginterpreters.tool.GenerateAst
TOOL_MANIFEST := tool_manifest.txt
TOOL_JAR = tool.jar

all: jar

compile:
	mkdir -p $(BUILD_DIR)
	$(JC) -d $(BUILD_DIR) $(SRCS)

jar: compile
	echo "Main-Class: $(MAIN)\n\n" > $(MANIFEST_PATH)
	jar cfm $(JAR) manifest.txt -C $(BUILD_DIR) .

run: jar
	java -jar $(JAR)

clean:
	rm -rf $(BUILD_DIR) $(JAR) $(MANIFEST_PATH)


##### tool-specific derictives #####

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