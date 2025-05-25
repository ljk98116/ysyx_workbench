#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "VCPUCore.h"
// #include <verilated.h>
#include <fstream>
#include <iostream>
#include <exception>
#include <dlfcn.h>

// use vcd
#include <verilated_vcd_c.h>
typedef struct {
    uint32_t gpr[32];
    uint32_t pc;
} CPU_state;

//dut寄存器状态
static CPU_state cpu;
//ref寄存器状态
static CPU_state nemu_cpu;

// difftest
void (*ref_difftest_memcpy)(int addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;
void (*ref_difftest_init)(int) = NULL;

//pmem for npc
#define CONFIG_MSIZE 0x8000000
#define PG_ALIGN __attribute((aligned(4096)))
#define RESET_VECTOR 0x80000000

uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};

static TOP_NAME dut;

static void paddr_write(int waddr, int wdata){
    *(int*)&pmem[waddr] = wdata;
}

extern "C" int pmem_read(int raddr){
    if((long long)(raddr & ~0x3u) - RESET_VECTOR < 0) return 0;
    printf("read addr: 0x%x\n", raddr);
    return *(int*)&pmem[(raddr & ~0x3u) - RESET_VECTOR];
}

extern "C" void pmem_write(int waddr, int wdata, char wmask)
{
    switch(wmask){
        case 0b0001: paddr_write(waddr & ~0x3u, wdata & 0xFF); break;
        case 0b0010: paddr_write(waddr & ~0x3u, (wdata >> 8) & 0xFF); break;
        case 0b0100: paddr_write(waddr & ~0x3u, (wdata >> 16) & 0xFF); break;
        case 0b1000: paddr_write(waddr & ~0x3u, (wdata >> 24) & 0xFF); break;

        case 0b0011: paddr_write(waddr & ~0x3u, wdata & 0xFFFF); break;
        case 0b1100: paddr_write(waddr & ~0x3u, (wdata >> 16) & 0xFFFF); break;

        case 0b1111: paddr_write(waddr & ~0x3u, wdata); break;
    }
}

long isa_load_imgs(char *filename){
    std::ifstream file(filename, std::ios::binary);
    if (!file) {
        std::cerr << "open failed" << filename << std::endl;
        return 1;
    }    
    try{
        file.seekg(0, std::ios::end);
        size_t size = file.tellg();
        file.seekg(0, std::ios::beg);
        // 读取文件内容到缓冲区
        file.read((char*)pmem, size);
        return size;
    }
    catch(std::exception& ex){
        std::cout << ex.what() << std::endl;
    }
    // 关闭文件
    file.close();
    return 0;    
}

void single_cycle() {
    dut.clock = 0; dut.eval();
    dut.clock = 1; dut.eval();
}

void reset(int n) {
    dut.reset = 1;
    while (n -- > 0) single_cycle();
    dut.reset = 0;
}

static void load_so()
{
    void *handle;
    char *nemu_home = getenv("NEMU_HOME");
    char fullpath[128];
    memset(fullpath, 0, sizeof(fullpath));
    strcpy(fullpath, nemu_home);
    strcat(fullpath, "/build/riscv32-nemu-interpreter-so");
    handle = dlopen(fullpath, RTLD_LAZY);
    assert(handle);
  
    ref_difftest_memcpy = (void(*)(int, void*, size_t, bool))dlsym(handle, "difftest_memcpy");
    assert(ref_difftest_memcpy);
  
    ref_difftest_regcpy = (void(*)(void *, bool))dlsym(handle, "difftest_regcpy");
    assert(ref_difftest_regcpy);
  
    ref_difftest_exec = (void(*)(uint64_t))dlsym(handle, "difftest_exec");
    assert(ref_difftest_exec);
  
    ref_difftest_raise_intr = (void(*)(uint64_t))dlsym(handle, "difftest_raise_intr");
    assert(ref_difftest_raise_intr);
  
    void (*ref_difftest_init)(int) = (void(*)(int))dlsym(handle, "difftest_init");
    assert(ref_difftest_init);
}

int main(int argc, char **argv){
    //加载动态库函数
    load_so();
    //加载elf程序到内存位置
    int cycle_count = 0;
    if(argc < 2){
        printf("Please feed a image file\n");
        return 0;
    }
    printf("filename: %s\n", argv[1]);
    memset(pmem, 0, sizeof(pmem));
    int size = isa_load_imgs(argv[1]);
    printf("load image done, size:%d\n", size);
    Verilated::commandArgs(argc, argv);
    // 1. 初始化 VCD 记录
    Verilated::traceEverOn(true); // 启用跟踪
    VerilatedVcdC* tfp = new VerilatedVcdC; // 创建 VCD 对象
    dut.trace(tfp, 99); // 跟踪所有信号（99=递归深度）
    tfp->open("wave2.vcd"); // 输出文件名    
    //仿真CPU
    //初始化ref CPU状态
    memset(&cpu, 0, sizeof(CPU_state));
    memset(&nemu_cpu, 0, sizeof(CPU_state));
    cpu.pc = RESET_VECTOR;

    //复制dut的寄存器状态到ref,以方便nemu执行程序
    ref_difftest_regcpy(&cpu, 1);
    //拷贝当前内存到nemu
    ref_difftest_memcpy(RESET_VECTOR, pmem, size, 1);
    int t = size / 4;
    uint32_t npc, pc;
    pc = 0;
    npc = RESET_VECTOR;
    reset(5);
#if 1
    while(cycle_count < 1000) {
        printf("%d cycle going\n", cycle_count);
        //CPU运行一个周期
        single_cycle();
        //vcd记录仿真结果
        tfp->dump(cycle_count);
        //期间, dut提交寄存器状态，需要根据提交的指令个数决定向后执行多少条指令
        //寄存器状态包括当前物理寄存器数值、物理寄存器修改trace(组内的寄存器改了哪个),使用DPI-C机制触发
        //提交阶段寄存器重命名表，寄存器重命名表修改trace(组内的寄存器改了哪个)
        //重命名目标物理寄存器分配以及ROB ID的分配采用队列形式，可以避免哈希
        //提交指令组的disasm构建itrace
        //根据提交指令组的内容构建ftrace
        //访存暂时没有思路
        // 记录上一个PC值
        pc = npc;
        //nemu执行指令，需要根据提交指令数量进行调整
        ref_difftest_exec(1);
        //nemu寄存器结果拷贝到dut方
        ref_difftest_regcpy(&nemu_cpu, 0);
        printf("pc: 0x%x\n", pc);
        for(int i=0;i<32;++i){
            printf("\treg %d: 0x%x\n", i, nemu_cpu.gpr[i]);
        }
        //比较cpu和内存状态是否一致，如果CPU状态不一致或执行完毕，退出仿真
        ++cycle_count;
        //获取下一个pc值
        npc = nemu_cpu.pc;
    }
    tfp->close();
#endif
    return 0;
}