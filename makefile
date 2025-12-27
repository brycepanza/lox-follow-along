JC := javac

SRC_DIR := ./src
BUILD_DIR := ./build

SRCS := $(shell find $(SRC_DIR) -name  "*.java")

MAIN := com.craftinginterpreters.lox.Lox
MANIFEST_PATH := manifest.txt
JAR := app.jar

all: jar

compile:
	mkdir -p $(BUILD_DIR)
	$(JC) -d $(BUILD_DIR) $(SRCS)

# jar files require compilation step
jar: compile
	echo "Main-Class: $(MAIN)\n" > $(MANIFEST_PATH)
	jar cfm $(JAR) manifest.txt -C $(BUILD_DIR) .

run: jar
	java -jar $(JAR)

clean:
	rm -rf $(BUILD_DIR) $(JAR) $(MANIFEST_PATH)
