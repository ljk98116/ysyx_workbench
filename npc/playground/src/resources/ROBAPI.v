`define ROBRW_API(elem, width) \
import "DPI-C" function void npc_rob_write_``elem(input byte bankid, input int index, input int data); \
import "DPI-C" function int npc_rob_read_``elem(input byte bankid, input int index);

import "DPI-C" function void npc_rob_id_loc_mem_write(input byte index, input byte data);
import "DPI-C" function int npc_rob_id_loc_mem_read(input byte index);

// dpi-c macros
`ROBRW_API(pc, 32)
`ROBRW_API(valid, 1)
`ROBRW_API(Imm, 32)
`ROBRW_API(Opcode, 7)
`ROBRW_API(rs1, 5)
`ROBRW_API(rs2, 5)
`ROBRW_API(rd, 5)
`ROBRW_API(funct3, 5)
`ROBRW_API(funct7, 7)
`ROBRW_API(shamt, 5)
`ROBRW_API(Type, 3)
`ROBRW_API(HasRs1, 1)
`ROBRW_API(HasRs2, 1)
`ROBRW_API(HasRd, 1)
`ROBRW_API(id, 7)
`ROBRW_API(ps1, 8)
`ROBRW_API(ps2, 8)
`ROBRW_API(pd, 7)
`ROBRW_API(oldpd, 8)
`ROBRW_API(rdy1, 1)
`ROBRW_API(rdy2, 1)
`ROBRW_API(rdy, 1)
`ROBRW_API(isBranch, 1)
`ROBRW_API(isStore, 1)
`ROBRW_API(isLoad, 1)
`ROBRW_API(hasException, 1)
`ROBRW_API(ExceptionType, 8)
`ROBRW_API(isTaken, 1)
`ROBRW_API(targetBrAddr, 32)
`ROBRW_API(reg_wb_data, 32)
`ROBRW_API(storeIdx, 8)
`ROBRW_API(gbranch_res, 1)
`ROBRW_API(lbranch_res, 1)
`ROBRW_API(branch_res, 1)
`ROBRW_API(global_pht_idx, 13)
`ROBRW_API(local_pht_idx, 13)
`ROBRW_API(bht_idx, 8)
`ROBRW_API(branch_pred_addr, 32);

//Read API macros
`define ROBReadAPI(elem, width) \
    module ROBReadAPI``elem ( \
        input rst, \
        input ren, \
        input [7:0] index, \
        input [1:0] bankid, \
        output reg [width - 1 : 0] rob_rdata \
    ); \
        always @(*) begin \
            if(rst | ~ren) begin \
                rob_rdata = width'b0; \
            end \
            else if(ren) begin \
                rob_rdata = npc_rob_read_``elem(bankid, index); \
            end \
        end \
    endmodule \

`ROBReadAPI(pc, 32)
`ROBReadAPI(valid, 1)
`ROBReadAPI(Imm, 32)
`ROBReadAPI(Opcode, 7)
`ROBReadAPI(rs1, 5)
`ROBReadAPI(rs2, 5)
`ROBReadAPI(rd, 5)
`ROBReadAPI(funct3, 5)
`ROBReadAPI(funct7, 7)
`ROBReadAPI(shamt, 5)
`ROBReadAPI(Type, 3)
`ROBReadAPI(HasRs1, 1)
`ROBReadAPI(HasRs2, 1)
`ROBReadAPI(HasRd, 1)
`ROBReadAPI(id, 7)
`ROBReadAPI(ps1, 8)
`ROBReadAPI(ps2, 8)
`ROBReadAPI(pd, 7)
`ROBReadAPI(oldpd, 8)
`ROBReadAPI(rdy1, 1)
`ROBReadAPI(rdy2, 1)
`ROBReadAPI(rdy, 1)
`ROBReadAPI(isBranch, 1)
`ROBReadAPI(isStore, 1)
`ROBReadAPI(isLoad, 1)
`ROBReadAPI(hasException, 1)
`ROBReadAPI(ExceptionType, 8)
`ROBReadAPI(isTaken, 1)
`ROBReadAPI(targetBrAddr, 32)
`ROBReadAPI(reg_wb_data, 32)
`ROBReadAPI(storeIdx, 8)
`ROBReadAPI(gbranch_res, 1)
`ROBReadAPI(lbranch_res, 1)
`ROBReadAPI(branch_res, 1)
`ROBReadAPI(global_pht_idx, 13)
`ROBReadAPI(local_pht_idx, 13)
`ROBReadAPI(bht_idx, 8)
`ROBReadAPI(branch_pred_addr, 32);

//WriteAPI macros
`define ROBWriteAPI(elem, width) \
    module ROBWriteAPI``elem ( \
        input clk, \
        input rst, \
        input wen, \
        input [7:0] index, \
        input [1:0] bankid, \
        input [width - 1 : 0] rob_wdata \
    ); \
        always @(posedge clk) begin \
            if(rst) begin \
                integer i, j; \
                for(j=0;j<4;j=j+1) begin \
                    for(i=0;i<(1 << 7);i=i+1) begin\
                        npc_rob_write_``elem(j, i, 32'b0);\
                    end\
                end\
            end\
            else if(wen) begin \
                npc_rob_write_``elem(bankid, index, rob_wdata); \
            end \
        end \
    endmodule \

`ROBWriteAPI(pc, 32)
`ROBWriteAPI(valid, 1)
`ROBWriteAPI(Imm, 32)
`ROBWriteAPI(Opcode, 7)
`ROBWriteAPI(rs1, 5)
`ROBWriteAPI(rs2, 5)
`ROBWriteAPI(rd, 5)
`ROBWriteAPI(funct3, 5)
`ROBWriteAPI(funct7, 7)
`ROBWriteAPI(shamt, 5)
`ROBWriteAPI(Type, 3)
`ROBWriteAPI(HasRs1, 1)
`ROBWriteAPI(HasRs2, 1)
`ROBWriteAPI(HasRd, 1)
`ROBWriteAPI(id, 7)
`ROBWriteAPI(ps1, 8)
`ROBWriteAPI(ps2, 8)
`ROBWriteAPI(pd, 7)
`ROBWriteAPI(oldpd, 8)
`ROBWriteAPI(rdy1, 1)
`ROBWriteAPI(rdy2, 1)
`ROBWriteAPI(rdy, 1)
`ROBWriteAPI(isBranch, 1)
`ROBWriteAPI(isStore, 1)
`ROBWriteAPI(isLoad, 1)
`ROBWriteAPI(hasException, 1)
`ROBWriteAPI(ExceptionType, 8)
`ROBWriteAPI(isTaken, 1)
`ROBWriteAPI(targetBrAddr, 32)
`ROBWriteAPI(reg_wb_data, 32)
`ROBWriteAPI(storeIdx, 8)
`ROBWriteAPI(gbranch_res, 1)
`ROBWriteAPI(lbranch_res, 1)
`ROBWriteAPI(branch_res, 1)
`ROBWriteAPI(global_pht_idx, 13)
`ROBWriteAPI(local_pht_idx, 13)
`ROBWriteAPI(bht_idx, 8)
`ROBWriteAPI(branch_pred_addr, 32);

module ROBIdLocMemWriteAPI (
    input clk,
    input rst,
    input wen,
    input [7:0] index,
    input [7:0] data
);
    always @(posedge clk) begin
        if(rst) begin
            integer i;
            for(i=0;i<(1 << 7);i=i+1) begin
                npc_rob_id_loc_mem_write(i, 8'b10000000);
            end
        end
        else if(wen) begin
            npc_rob_id_loc_mem_write(index, data);
        end 
    end
endmodule

module ROBIdLocMemReadAPI (
    input rst,
    input ren,
    input [7:0] index,
    output reg [7:0] data
);
    always @(*) begin
        if(rst | ~ren) begin
            data = 8'b0;
        end
        else if(ren) begin
            data = npc_rob_id_loc_mem_read(index);
        end
    end
endmodule