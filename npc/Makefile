BUILDDIR = ./vsrc
PACKAGE =cpu

PRJ = playground
TOPNAME=CPUCore
NXDC_FILES = constr/$(TOPNAME).nxdc

INC_PATH ?= $(NEMU_HOME)/include
VERILATOR = verilator
VERILATOR_FLAGS += --cc --exe --build -O3 -Wall --trace -j 8 --output-split 200
VERILATOR_FLAGS += -Wno-lint

BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)
WORK_DIR = $(pwd)
 
$(shell mkdir -p $(BUILD_DIR))

# add nemu so
LDFLAGS += -lreadline
LDFLAGS += -Wl,-rpath="$(NEMU_HOME)/build/"

# constraint file
SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

# project source
VSRCS = $(shell find $(abspath ./vsrc) -name "*.v" -or -name "*.sv")
CSRCS = $(shell find $(abspath ./csrc/$(PACKAGE)) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
# CSRCS += $(SRC_AUTO_BIND)

# rules for NVBoard
# include $(NVBOARD_HOME)/scripts/nvboard.mk

# rules for verilator
INCFLAGS = $(addprefix -I , $(INC_PATH))
INCFLAGS += $(addprefix -I , /home/ljk/project/ysyx_workbench/npc/csrc/$(PACKAGE)/)
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

.PHONY: test verilog help reformat checkformat clean all sim

test:
	mill -i $(PRJ).test

verilog:
	$(call git_commit, "generate verilog")
	mkdir -p $(BUILDDIR)
# mill -i $(PRJ).runMain Elaborate --target-dir $(BUILDDIR)
	mill -i $(PRJ).runMain Elaborate
	rm -rf $(TOPNAME).sv
help:
	mill -i $(PRJ).runMain Elaborate --help

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

bsp:
	mill -i mill.bsp.BSP/install

idea:
	mill -i mill.idea.GenIdea/idea

clean:
	-rm -rf $(BUILD_DIR) $(BUILDDIR)


# $(BIN): $(VSRCS) $(CSRCS) $(NVBOARD_ARCHIVE) 
# 	@rm -rf $(OBJ_DIR)
# 	$(VERILATOR) $(VERILATOR_CFLAGS) \
# 		--top-module $(TOPNAME) $^ \
# 		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
# 		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))

# run without nvboard
$(BIN): $(VSRCS) $(CSRCS) 
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_FLAGS) \
		--top $(TOPNAME) $^ \
		$(addprefix -CFLAGS , $(CXXFLAGS)) $(addprefix -LDFLAGS , $(LDFLAGS)) \
		--Mdir $(OBJ_DIR) -o $(abspath $(BIN))

default: $(BIN)

all: default
	@echo "Write this Makefile by your self."

run: $(BIN)
	@$^

sim: verilog
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by yourself."
	make all
	./$(BIN) && gtkwave wave.vcd

-include ../Makefile
