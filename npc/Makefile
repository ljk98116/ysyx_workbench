CSRC=$(wildcard csrc/*.cpp)
VSRC=$(wildcard vsrc/*.v)
VC=verilator
VCFLAG=--cc --exe -Wall --trace
TARGET=double_switch
all:
	@echo "Write this Makefile by your self."
	$(VC) $(VCFLAG) $(CSRC) $(VSRC)
	cd obj_dir && make -f V$(TARGET).mk
sim: all
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo "Write this Makefile by your self."
	./obj_dir/V$(TARGET)
	gtkwave wave.vcd

.PHONY: clean
clean:
	rm -rf obj_dir/ wave.vcd

include ../Makefile
