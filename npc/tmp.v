`line 1 "playground/src/resources/ROBAPI.v" 1
 



`line 5 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_id_loc_mem_write(input byte index, input byte data);
import "DPI-C" function int npc_rob_id_loc_mem_read(input byte index);

`line 8 "playground/src/resources/ROBAPI.v" 0
 

`line 9 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_pc(input byte bankid, input int index, input int data); 
`line 9 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_pc(input byte bankid, input int index);

`line 10 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_valid(input byte bankid, input int index, input int data); 
`line 10 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_valid(input byte bankid, input int index);

`line 11 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_Imm(input byte bankid, input int index, input int data); 
`line 11 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_Imm(input byte bankid, input int index);

`line 12 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_Opcode(input byte bankid, input int index, input int data); 
`line 12 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_Opcode(input byte bankid, input int index);

`line 13 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_rs1(input byte bankid, input int index, input int data); 
`line 13 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_rs1(input byte bankid, input int index);

`line 14 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_rs2(input byte bankid, input int index, input int data); 
`line 14 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_rs2(input byte bankid, input int index);

`line 15 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_rd(input byte bankid, input int index, input int data); 
`line 15 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_rd(input byte bankid, input int index);

`line 16 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_funct3(input byte bankid, input int index, input int data); 
`line 16 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_funct3(input byte bankid, input int index);

`line 17 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_funct7(input byte bankid, input int index, input int data); 
`line 17 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_funct7(input byte bankid, input int index);

`line 18 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_shamt(input byte bankid, input int index, input int data); 
`line 18 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_shamt(input byte bankid, input int index);

`line 19 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_Type(input byte bankid, input int index, input int data); 
`line 19 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_Type(input byte bankid, input int index);

`line 20 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_HasRs1(input byte bankid, input int index, input int data); 
`line 20 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_HasRs1(input byte bankid, input int index);

`line 21 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_HasRs2(input byte bankid, input int index, input int data); 
`line 21 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_HasRs2(input byte bankid, input int index);

`line 22 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_HasRd(input byte bankid, input int index, input int data); 
`line 22 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_HasRd(input byte bankid, input int index);

`line 23 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_id(input byte bankid, input int index, input int data); 
`line 23 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_id(input byte bankid, input int index);

`line 24 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_ps1(input byte bankid, input int index, input int data); 
`line 24 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_ps1(input byte bankid, input int index);

`line 25 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_ps2(input byte bankid, input int index, input int data); 
`line 25 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_ps2(input byte bankid, input int index);

`line 26 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_pd(input byte bankid, input int index, input int data); 
`line 26 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_pd(input byte bankid, input int index);

`line 27 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_oldpd(input byte bankid, input int index, input int data); 
`line 27 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_oldpd(input byte bankid, input int index);

`line 28 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_rdy1(input byte bankid, input int index, input int data); 
`line 28 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_rdy1(input byte bankid, input int index);

`line 29 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_rdy2(input byte bankid, input int index, input int data); 
`line 29 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_rdy2(input byte bankid, input int index);

`line 30 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_rdy(input byte bankid, input int index, input int data); 
`line 30 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_rdy(input byte bankid, input int index);

`line 31 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_isBranch(input byte bankid, input int index, input int data); 
`line 31 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_isBranch(input byte bankid, input int index);

`line 32 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_isStore(input byte bankid, input int index, input int data); 
`line 32 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_isStore(input byte bankid, input int index);

`line 33 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_isLoad(input byte bankid, input int index, input int data); 
`line 33 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_isLoad(input byte bankid, input int index);

`line 34 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_hasException(input byte bankid, input int index, input int data); 
`line 34 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_hasException(input byte bankid, input int index);

`line 35 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_ExceptionType(input byte bankid, input int index, input int data); 
`line 35 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_ExceptionType(input byte bankid, input int index);

`line 36 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_isTaken(input byte bankid, input int index, input int data); 
`line 36 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_isTaken(input byte bankid, input int index);

`line 37 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_targetBrAddr(input byte bankid, input int index, input int data); 
`line 37 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_targetBrAddr(input byte bankid, input int index);

`line 38 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_reg_wb_data(input byte bankid, input int index, input int data); 
`line 38 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_reg_wb_data(input byte bankid, input int index);

`line 39 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_storeIdx(input byte bankid, input int index, input int data); 
`line 39 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_storeIdx(input byte bankid, input int index);

`line 40 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_gbranch_res(input byte bankid, input int index, input int data); 
`line 40 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_gbranch_res(input byte bankid, input int index);

`line 41 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_lbranch_res(input byte bankid, input int index, input int data); 
`line 41 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_lbranch_res(input byte bankid, input int index);

`line 42 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_branch_res(input byte bankid, input int index, input int data); 
`line 42 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_branch_res(input byte bankid, input int index);

`line 43 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_global_pht_idx(input byte bankid, input int index, input int data); 
`line 43 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_global_pht_idx(input byte bankid, input int index);

`line 44 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_local_pht_idx(input byte bankid, input int index, input int data); 
`line 44 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_local_pht_idx(input byte bankid, input int index);

`line 45 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_bht_idx(input byte bankid, input int index, input int data); 
`line 45 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_bht_idx(input byte bankid, input int index);

`line 46 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function void npc_rob_write_branch_pred_addr(input byte bankid, input int index, input int data); 
`line 46 "playground/src/resources/ROBAPI.v" 0
import "DPI-C" function int npc_rob_read_branch_pred_addr(input byte bankid, input int index);;

`line 48 "playground/src/resources/ROBAPI.v" 0
 
 

















`line 66 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIpc ( 
`line 66 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 66 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 66 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 66 "playground/src/resources/ROBAPI.v" 0
        output reg [32 - 1 : 0] rob_rdata 
`line 66 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 66 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 66 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 66 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 32'b0; 
`line 66 "playground/src/resources/ROBAPI.v" 0
            end 
`line 66 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 66 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_pc(bankid, index); 
`line 66 "playground/src/resources/ROBAPI.v" 0
            end 
`line 66 "playground/src/resources/ROBAPI.v" 0
        end 
`line 66 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 67 "playground/src/resources/ROBAPI.v" 0

`line 67 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIvalid ( 
`line 67 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 67 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 67 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 67 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 67 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 67 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 67 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 67 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 67 "playground/src/resources/ROBAPI.v" 0
            end 
`line 67 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 67 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_valid(bankid, index); 
`line 67 "playground/src/resources/ROBAPI.v" 0
            end 
`line 67 "playground/src/resources/ROBAPI.v" 0
        end 
`line 67 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 68 "playground/src/resources/ROBAPI.v" 0

`line 68 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIImm ( 
`line 68 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 68 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 68 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 68 "playground/src/resources/ROBAPI.v" 0
        output reg [32 - 1 : 0] rob_rdata 
`line 68 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 68 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 68 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 68 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 32'b0; 
`line 68 "playground/src/resources/ROBAPI.v" 0
            end 
`line 68 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 68 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_Imm(bankid, index); 
`line 68 "playground/src/resources/ROBAPI.v" 0
            end 
`line 68 "playground/src/resources/ROBAPI.v" 0
        end 
`line 68 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 69 "playground/src/resources/ROBAPI.v" 0

`line 69 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIOpcode ( 
`line 69 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 69 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 69 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 69 "playground/src/resources/ROBAPI.v" 0
        output reg [7 - 1 : 0] rob_rdata 
`line 69 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 69 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 69 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 69 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 7'b0; 
`line 69 "playground/src/resources/ROBAPI.v" 0
            end 
`line 69 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 69 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_Opcode(bankid, index); 
`line 69 "playground/src/resources/ROBAPI.v" 0
            end 
`line 69 "playground/src/resources/ROBAPI.v" 0
        end 
`line 69 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 70 "playground/src/resources/ROBAPI.v" 0

`line 70 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIrs1 ( 
`line 70 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 70 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 70 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 70 "playground/src/resources/ROBAPI.v" 0
        output reg [5 - 1 : 0] rob_rdata 
`line 70 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 70 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 70 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 70 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 5'b0; 
`line 70 "playground/src/resources/ROBAPI.v" 0
            end 
`line 70 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 70 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_rs1(bankid, index); 
`line 70 "playground/src/resources/ROBAPI.v" 0
            end 
`line 70 "playground/src/resources/ROBAPI.v" 0
        end 
`line 70 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 71 "playground/src/resources/ROBAPI.v" 0

`line 71 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIrs2 ( 
`line 71 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 71 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 71 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 71 "playground/src/resources/ROBAPI.v" 0
        output reg [5 - 1 : 0] rob_rdata 
`line 71 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 71 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 71 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 71 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 5'b0; 
`line 71 "playground/src/resources/ROBAPI.v" 0
            end 
`line 71 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 71 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_rs2(bankid, index); 
`line 71 "playground/src/resources/ROBAPI.v" 0
            end 
`line 71 "playground/src/resources/ROBAPI.v" 0
        end 
`line 71 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 72 "playground/src/resources/ROBAPI.v" 0

`line 72 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIrd ( 
`line 72 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 72 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 72 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 72 "playground/src/resources/ROBAPI.v" 0
        output reg [5 - 1 : 0] rob_rdata 
`line 72 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 72 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 72 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 72 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 5'b0; 
`line 72 "playground/src/resources/ROBAPI.v" 0
            end 
`line 72 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 72 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_rd(bankid, index); 
`line 72 "playground/src/resources/ROBAPI.v" 0
            end 
`line 72 "playground/src/resources/ROBAPI.v" 0
        end 
`line 72 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 73 "playground/src/resources/ROBAPI.v" 0

`line 73 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIfunct3 ( 
`line 73 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 73 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 73 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 73 "playground/src/resources/ROBAPI.v" 0
        output reg [5 - 1 : 0] rob_rdata 
`line 73 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 73 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 73 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 73 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 5'b0; 
`line 73 "playground/src/resources/ROBAPI.v" 0
            end 
`line 73 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 73 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_funct3(bankid, index); 
`line 73 "playground/src/resources/ROBAPI.v" 0
            end 
`line 73 "playground/src/resources/ROBAPI.v" 0
        end 
`line 73 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 74 "playground/src/resources/ROBAPI.v" 0

`line 74 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIfunct7 ( 
`line 74 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 74 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 74 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 74 "playground/src/resources/ROBAPI.v" 0
        output reg [7 - 1 : 0] rob_rdata 
`line 74 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 74 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 74 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 74 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 7'b0; 
`line 74 "playground/src/resources/ROBAPI.v" 0
            end 
`line 74 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 74 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_funct7(bankid, index); 
`line 74 "playground/src/resources/ROBAPI.v" 0
            end 
`line 74 "playground/src/resources/ROBAPI.v" 0
        end 
`line 74 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 75 "playground/src/resources/ROBAPI.v" 0

`line 75 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIshamt ( 
`line 75 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 75 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 75 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 75 "playground/src/resources/ROBAPI.v" 0
        output reg [5 - 1 : 0] rob_rdata 
`line 75 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 75 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 75 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 75 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 5'b0; 
`line 75 "playground/src/resources/ROBAPI.v" 0
            end 
`line 75 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 75 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_shamt(bankid, index); 
`line 75 "playground/src/resources/ROBAPI.v" 0
            end 
`line 75 "playground/src/resources/ROBAPI.v" 0
        end 
`line 75 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 76 "playground/src/resources/ROBAPI.v" 0

`line 76 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIType ( 
`line 76 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 76 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 76 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 76 "playground/src/resources/ROBAPI.v" 0
        output reg [3 - 1 : 0] rob_rdata 
`line 76 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 76 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 76 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 76 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 3'b0; 
`line 76 "playground/src/resources/ROBAPI.v" 0
            end 
`line 76 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 76 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_Type(bankid, index); 
`line 76 "playground/src/resources/ROBAPI.v" 0
            end 
`line 76 "playground/src/resources/ROBAPI.v" 0
        end 
`line 76 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 77 "playground/src/resources/ROBAPI.v" 0

`line 77 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIHasRs1 ( 
`line 77 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 77 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 77 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 77 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 77 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 77 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 77 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 77 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 77 "playground/src/resources/ROBAPI.v" 0
            end 
`line 77 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 77 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_HasRs1(bankid, index); 
`line 77 "playground/src/resources/ROBAPI.v" 0
            end 
`line 77 "playground/src/resources/ROBAPI.v" 0
        end 
`line 77 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 78 "playground/src/resources/ROBAPI.v" 0

`line 78 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIHasRs2 ( 
`line 78 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 78 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 78 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 78 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 78 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 78 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 78 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 78 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 78 "playground/src/resources/ROBAPI.v" 0
            end 
`line 78 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 78 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_HasRs2(bankid, index); 
`line 78 "playground/src/resources/ROBAPI.v" 0
            end 
`line 78 "playground/src/resources/ROBAPI.v" 0
        end 
`line 78 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 79 "playground/src/resources/ROBAPI.v" 0

`line 79 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIHasRd ( 
`line 79 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 79 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 79 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 79 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 79 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 79 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 79 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 79 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 79 "playground/src/resources/ROBAPI.v" 0
            end 
`line 79 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 79 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_HasRd(bankid, index); 
`line 79 "playground/src/resources/ROBAPI.v" 0
            end 
`line 79 "playground/src/resources/ROBAPI.v" 0
        end 
`line 79 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 80 "playground/src/resources/ROBAPI.v" 0

`line 80 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIid ( 
`line 80 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 80 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 80 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 80 "playground/src/resources/ROBAPI.v" 0
        output reg [7 - 1 : 0] rob_rdata 
`line 80 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 80 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 80 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 80 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 7'b0; 
`line 80 "playground/src/resources/ROBAPI.v" 0
            end 
`line 80 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 80 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_id(bankid, index); 
`line 80 "playground/src/resources/ROBAPI.v" 0
            end 
`line 80 "playground/src/resources/ROBAPI.v" 0
        end 
`line 80 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 81 "playground/src/resources/ROBAPI.v" 0

`line 81 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIps1 ( 
`line 81 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 81 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 81 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 81 "playground/src/resources/ROBAPI.v" 0
        output reg [8 - 1 : 0] rob_rdata 
`line 81 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 81 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 81 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 81 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 8'b0; 
`line 81 "playground/src/resources/ROBAPI.v" 0
            end 
`line 81 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 81 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_ps1(bankid, index); 
`line 81 "playground/src/resources/ROBAPI.v" 0
            end 
`line 81 "playground/src/resources/ROBAPI.v" 0
        end 
`line 81 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 82 "playground/src/resources/ROBAPI.v" 0

`line 82 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIps2 ( 
`line 82 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 82 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 82 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 82 "playground/src/resources/ROBAPI.v" 0
        output reg [8 - 1 : 0] rob_rdata 
`line 82 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 82 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 82 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 82 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 8'b0; 
`line 82 "playground/src/resources/ROBAPI.v" 0
            end 
`line 82 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 82 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_ps2(bankid, index); 
`line 82 "playground/src/resources/ROBAPI.v" 0
            end 
`line 82 "playground/src/resources/ROBAPI.v" 0
        end 
`line 82 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 83 "playground/src/resources/ROBAPI.v" 0

`line 83 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIpd ( 
`line 83 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 83 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 83 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 83 "playground/src/resources/ROBAPI.v" 0
        output reg [7 - 1 : 0] rob_rdata 
`line 83 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 83 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 83 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 83 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 7'b0; 
`line 83 "playground/src/resources/ROBAPI.v" 0
            end 
`line 83 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 83 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_pd(bankid, index); 
`line 83 "playground/src/resources/ROBAPI.v" 0
            end 
`line 83 "playground/src/resources/ROBAPI.v" 0
        end 
`line 83 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 84 "playground/src/resources/ROBAPI.v" 0

`line 84 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIoldpd ( 
`line 84 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 84 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 84 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 84 "playground/src/resources/ROBAPI.v" 0
        output reg [8 - 1 : 0] rob_rdata 
`line 84 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 84 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 84 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 84 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 8'b0; 
`line 84 "playground/src/resources/ROBAPI.v" 0
            end 
`line 84 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 84 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_oldpd(bankid, index); 
`line 84 "playground/src/resources/ROBAPI.v" 0
            end 
`line 84 "playground/src/resources/ROBAPI.v" 0
        end 
`line 84 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 85 "playground/src/resources/ROBAPI.v" 0

`line 85 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIrdy1 ( 
`line 85 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 85 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 85 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 85 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 85 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 85 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 85 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 85 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 85 "playground/src/resources/ROBAPI.v" 0
            end 
`line 85 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 85 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_rdy1(bankid, index); 
`line 85 "playground/src/resources/ROBAPI.v" 0
            end 
`line 85 "playground/src/resources/ROBAPI.v" 0
        end 
`line 85 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 86 "playground/src/resources/ROBAPI.v" 0

`line 86 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIrdy2 ( 
`line 86 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 86 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 86 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 86 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 86 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 86 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 86 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 86 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 86 "playground/src/resources/ROBAPI.v" 0
            end 
`line 86 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 86 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_rdy2(bankid, index); 
`line 86 "playground/src/resources/ROBAPI.v" 0
            end 
`line 86 "playground/src/resources/ROBAPI.v" 0
        end 
`line 86 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 87 "playground/src/resources/ROBAPI.v" 0

`line 87 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIrdy ( 
`line 87 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 87 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 87 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 87 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 87 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 87 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 87 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 87 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 87 "playground/src/resources/ROBAPI.v" 0
            end 
`line 87 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 87 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_rdy(bankid, index); 
`line 87 "playground/src/resources/ROBAPI.v" 0
            end 
`line 87 "playground/src/resources/ROBAPI.v" 0
        end 
`line 87 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 88 "playground/src/resources/ROBAPI.v" 0

`line 88 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIisBranch ( 
`line 88 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 88 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 88 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 88 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 88 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 88 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 88 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 88 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 88 "playground/src/resources/ROBAPI.v" 0
            end 
`line 88 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 88 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_isBranch(bankid, index); 
`line 88 "playground/src/resources/ROBAPI.v" 0
            end 
`line 88 "playground/src/resources/ROBAPI.v" 0
        end 
`line 88 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 89 "playground/src/resources/ROBAPI.v" 0

`line 89 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIisStore ( 
`line 89 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 89 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 89 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 89 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 89 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 89 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 89 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 89 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 89 "playground/src/resources/ROBAPI.v" 0
            end 
`line 89 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 89 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_isStore(bankid, index); 
`line 89 "playground/src/resources/ROBAPI.v" 0
            end 
`line 89 "playground/src/resources/ROBAPI.v" 0
        end 
`line 89 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 90 "playground/src/resources/ROBAPI.v" 0

`line 90 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIisLoad ( 
`line 90 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 90 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 90 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 90 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 90 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 90 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 90 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 90 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 90 "playground/src/resources/ROBAPI.v" 0
            end 
`line 90 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 90 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_isLoad(bankid, index); 
`line 90 "playground/src/resources/ROBAPI.v" 0
            end 
`line 90 "playground/src/resources/ROBAPI.v" 0
        end 
`line 90 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 91 "playground/src/resources/ROBAPI.v" 0

`line 91 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIhasException ( 
`line 91 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 91 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 91 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 91 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 91 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 91 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 91 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 91 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 91 "playground/src/resources/ROBAPI.v" 0
            end 
`line 91 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 91 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_hasException(bankid, index); 
`line 91 "playground/src/resources/ROBAPI.v" 0
            end 
`line 91 "playground/src/resources/ROBAPI.v" 0
        end 
`line 91 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 92 "playground/src/resources/ROBAPI.v" 0

`line 92 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIExceptionType ( 
`line 92 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 92 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 92 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 92 "playground/src/resources/ROBAPI.v" 0
        output reg [8 - 1 : 0] rob_rdata 
`line 92 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 92 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 92 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 92 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 8'b0; 
`line 92 "playground/src/resources/ROBAPI.v" 0
            end 
`line 92 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 92 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_ExceptionType(bankid, index); 
`line 92 "playground/src/resources/ROBAPI.v" 0
            end 
`line 92 "playground/src/resources/ROBAPI.v" 0
        end 
`line 92 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 93 "playground/src/resources/ROBAPI.v" 0

`line 93 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIisTaken ( 
`line 93 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 93 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 93 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 93 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 93 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 93 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 93 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 93 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 93 "playground/src/resources/ROBAPI.v" 0
            end 
`line 93 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 93 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_isTaken(bankid, index); 
`line 93 "playground/src/resources/ROBAPI.v" 0
            end 
`line 93 "playground/src/resources/ROBAPI.v" 0
        end 
`line 93 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 94 "playground/src/resources/ROBAPI.v" 0

`line 94 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPItargetBrAddr ( 
`line 94 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 94 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 94 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 94 "playground/src/resources/ROBAPI.v" 0
        output reg [32 - 1 : 0] rob_rdata 
`line 94 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 94 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 94 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 94 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 32'b0; 
`line 94 "playground/src/resources/ROBAPI.v" 0
            end 
`line 94 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 94 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_targetBrAddr(bankid, index); 
`line 94 "playground/src/resources/ROBAPI.v" 0
            end 
`line 94 "playground/src/resources/ROBAPI.v" 0
        end 
`line 94 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 95 "playground/src/resources/ROBAPI.v" 0

`line 95 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIreg_wb_data ( 
`line 95 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 95 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 95 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 95 "playground/src/resources/ROBAPI.v" 0
        output reg [32 - 1 : 0] rob_rdata 
`line 95 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 95 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 95 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 95 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 32'b0; 
`line 95 "playground/src/resources/ROBAPI.v" 0
            end 
`line 95 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 95 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_reg_wb_data(bankid, index); 
`line 95 "playground/src/resources/ROBAPI.v" 0
            end 
`line 95 "playground/src/resources/ROBAPI.v" 0
        end 
`line 95 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 96 "playground/src/resources/ROBAPI.v" 0

`line 96 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIstoreIdx ( 
`line 96 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 96 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 96 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 96 "playground/src/resources/ROBAPI.v" 0
        output reg [8 - 1 : 0] rob_rdata 
`line 96 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 96 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 96 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 96 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 8'b0; 
`line 96 "playground/src/resources/ROBAPI.v" 0
            end 
`line 96 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 96 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_storeIdx(bankid, index); 
`line 96 "playground/src/resources/ROBAPI.v" 0
            end 
`line 96 "playground/src/resources/ROBAPI.v" 0
        end 
`line 96 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 97 "playground/src/resources/ROBAPI.v" 0

`line 97 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIgbranch_res ( 
`line 97 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 97 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 97 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 97 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 97 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 97 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 97 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 97 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 97 "playground/src/resources/ROBAPI.v" 0
            end 
`line 97 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 97 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_gbranch_res(bankid, index); 
`line 97 "playground/src/resources/ROBAPI.v" 0
            end 
`line 97 "playground/src/resources/ROBAPI.v" 0
        end 
`line 97 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 98 "playground/src/resources/ROBAPI.v" 0

`line 98 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIlbranch_res ( 
`line 98 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 98 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 98 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 98 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 98 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 98 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 98 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 98 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 98 "playground/src/resources/ROBAPI.v" 0
            end 
`line 98 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 98 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_lbranch_res(bankid, index); 
`line 98 "playground/src/resources/ROBAPI.v" 0
            end 
`line 98 "playground/src/resources/ROBAPI.v" 0
        end 
`line 98 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 99 "playground/src/resources/ROBAPI.v" 0

`line 99 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIbranch_res ( 
`line 99 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 99 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 99 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 99 "playground/src/resources/ROBAPI.v" 0
        output reg [1 - 1 : 0] rob_rdata 
`line 99 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 99 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 99 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 99 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 1'b0; 
`line 99 "playground/src/resources/ROBAPI.v" 0
            end 
`line 99 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 99 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_branch_res(bankid, index); 
`line 99 "playground/src/resources/ROBAPI.v" 0
            end 
`line 99 "playground/src/resources/ROBAPI.v" 0
        end 
`line 99 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 100 "playground/src/resources/ROBAPI.v" 0

`line 100 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIglobal_pht_idx ( 
`line 100 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 100 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 100 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 100 "playground/src/resources/ROBAPI.v" 0
        output reg [13 - 1 : 0] rob_rdata 
`line 100 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 100 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 100 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 100 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 13'b0; 
`line 100 "playground/src/resources/ROBAPI.v" 0
            end 
`line 100 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 100 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_global_pht_idx(bankid, index); 
`line 100 "playground/src/resources/ROBAPI.v" 0
            end 
`line 100 "playground/src/resources/ROBAPI.v" 0
        end 
`line 100 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 101 "playground/src/resources/ROBAPI.v" 0

`line 101 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIlocal_pht_idx ( 
`line 101 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 101 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 101 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 101 "playground/src/resources/ROBAPI.v" 0
        output reg [13 - 1 : 0] rob_rdata 
`line 101 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 101 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 101 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 101 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 13'b0; 
`line 101 "playground/src/resources/ROBAPI.v" 0
            end 
`line 101 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 101 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_local_pht_idx(bankid, index); 
`line 101 "playground/src/resources/ROBAPI.v" 0
            end 
`line 101 "playground/src/resources/ROBAPI.v" 0
        end 
`line 101 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 102 "playground/src/resources/ROBAPI.v" 0

`line 102 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIbht_idx ( 
`line 102 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 102 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 102 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 102 "playground/src/resources/ROBAPI.v" 0
        output reg [8 - 1 : 0] rob_rdata 
`line 102 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 102 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 102 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 102 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 8'b0; 
`line 102 "playground/src/resources/ROBAPI.v" 0
            end 
`line 102 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 102 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_bht_idx(bankid, index); 
`line 102 "playground/src/resources/ROBAPI.v" 0
            end 
`line 102 "playground/src/resources/ROBAPI.v" 0
        end 
`line 102 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 103 "playground/src/resources/ROBAPI.v" 0

`line 103 "playground/src/resources/ROBAPI.v" 0
    module ROBReadAPIbranch_pred_addr ( 
`line 103 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 103 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 103 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 103 "playground/src/resources/ROBAPI.v" 0
        output reg [32 - 1 : 0] rob_rdata 
`line 103 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 103 "playground/src/resources/ROBAPI.v" 0
        always @(*) begin 
`line 103 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 103 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= 32'b0; 
`line 103 "playground/src/resources/ROBAPI.v" 0
            end 
`line 103 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 103 "playground/src/resources/ROBAPI.v" 0
                rob_rdata <= npc_rob_read_branch_pred_addr(bankid, index); 
`line 103 "playground/src/resources/ROBAPI.v" 0
            end 
`line 103 "playground/src/resources/ROBAPI.v" 0
        end 
`line 103 "playground/src/resources/ROBAPI.v" 0
    endmodule 
`line 103 "playground/src/resources/ROBAPI.v" 0
;

`line 105 "playground/src/resources/ROBAPI.v" 0
 
 























`line 129 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIpc ( 
`line 129 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 129 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 129 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 129 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 129 "playground/src/resources/ROBAPI.v" 0
        input [32 - 1 : 0] rob_wdata 
`line 129 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 129 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 129 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 129 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 129 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 129 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 129 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_pc(j, i, 32'b0);
`line 129 "playground/src/resources/ROBAPI.v" 0
                    end
`line 129 "playground/src/resources/ROBAPI.v" 0
                end
`line 129 "playground/src/resources/ROBAPI.v" 0
            end
`line 129 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 129 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_pc(bankid, index, rob_wdata); 
`line 129 "playground/src/resources/ROBAPI.v" 0
            end 
`line 129 "playground/src/resources/ROBAPI.v" 0
        end 
`line 129 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 130 "playground/src/resources/ROBAPI.v" 0

`line 130 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIvalid ( 
`line 130 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 130 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 130 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 130 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 130 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 130 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 130 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 130 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 130 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 130 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 130 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 130 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_valid(j, i, 32'b0);
`line 130 "playground/src/resources/ROBAPI.v" 0
                    end
`line 130 "playground/src/resources/ROBAPI.v" 0
                end
`line 130 "playground/src/resources/ROBAPI.v" 0
            end
`line 130 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 130 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_valid(bankid, index, rob_wdata); 
`line 130 "playground/src/resources/ROBAPI.v" 0
            end 
`line 130 "playground/src/resources/ROBAPI.v" 0
        end 
`line 130 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 131 "playground/src/resources/ROBAPI.v" 0

`line 131 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIImm ( 
`line 131 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 131 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 131 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 131 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 131 "playground/src/resources/ROBAPI.v" 0
        input [32 - 1 : 0] rob_wdata 
`line 131 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 131 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 131 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 131 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 131 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 131 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 131 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_Imm(j, i, 32'b0);
`line 131 "playground/src/resources/ROBAPI.v" 0
                    end
`line 131 "playground/src/resources/ROBAPI.v" 0
                end
`line 131 "playground/src/resources/ROBAPI.v" 0
            end
`line 131 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 131 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_Imm(bankid, index, rob_wdata); 
`line 131 "playground/src/resources/ROBAPI.v" 0
            end 
`line 131 "playground/src/resources/ROBAPI.v" 0
        end 
`line 131 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 132 "playground/src/resources/ROBAPI.v" 0

`line 132 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIOpcode ( 
`line 132 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 132 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 132 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 132 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 132 "playground/src/resources/ROBAPI.v" 0
        input [7 - 1 : 0] rob_wdata 
`line 132 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 132 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 132 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 132 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 132 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 132 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 132 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_Opcode(j, i, 32'b0);
`line 132 "playground/src/resources/ROBAPI.v" 0
                    end
`line 132 "playground/src/resources/ROBAPI.v" 0
                end
`line 132 "playground/src/resources/ROBAPI.v" 0
            end
`line 132 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 132 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_Opcode(bankid, index, rob_wdata); 
`line 132 "playground/src/resources/ROBAPI.v" 0
            end 
`line 132 "playground/src/resources/ROBAPI.v" 0
        end 
`line 132 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 133 "playground/src/resources/ROBAPI.v" 0

`line 133 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIrs1 ( 
`line 133 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 133 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 133 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 133 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 133 "playground/src/resources/ROBAPI.v" 0
        input [5 - 1 : 0] rob_wdata 
`line 133 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 133 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 133 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 133 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 133 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 133 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 133 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_rs1(j, i, 32'b0);
`line 133 "playground/src/resources/ROBAPI.v" 0
                    end
`line 133 "playground/src/resources/ROBAPI.v" 0
                end
`line 133 "playground/src/resources/ROBAPI.v" 0
            end
`line 133 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 133 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_rs1(bankid, index, rob_wdata); 
`line 133 "playground/src/resources/ROBAPI.v" 0
            end 
`line 133 "playground/src/resources/ROBAPI.v" 0
        end 
`line 133 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 134 "playground/src/resources/ROBAPI.v" 0

`line 134 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIrs2 ( 
`line 134 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 134 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 134 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 134 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 134 "playground/src/resources/ROBAPI.v" 0
        input [5 - 1 : 0] rob_wdata 
`line 134 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 134 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 134 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 134 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 134 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 134 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 134 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_rs2(j, i, 32'b0);
`line 134 "playground/src/resources/ROBAPI.v" 0
                    end
`line 134 "playground/src/resources/ROBAPI.v" 0
                end
`line 134 "playground/src/resources/ROBAPI.v" 0
            end
`line 134 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 134 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_rs2(bankid, index, rob_wdata); 
`line 134 "playground/src/resources/ROBAPI.v" 0
            end 
`line 134 "playground/src/resources/ROBAPI.v" 0
        end 
`line 134 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 135 "playground/src/resources/ROBAPI.v" 0

`line 135 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIrd ( 
`line 135 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 135 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 135 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 135 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 135 "playground/src/resources/ROBAPI.v" 0
        input [5 - 1 : 0] rob_wdata 
`line 135 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 135 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 135 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 135 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 135 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 135 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 135 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_rd(j, i, 32'b0);
`line 135 "playground/src/resources/ROBAPI.v" 0
                    end
`line 135 "playground/src/resources/ROBAPI.v" 0
                end
`line 135 "playground/src/resources/ROBAPI.v" 0
            end
`line 135 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 135 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_rd(bankid, index, rob_wdata); 
`line 135 "playground/src/resources/ROBAPI.v" 0
            end 
`line 135 "playground/src/resources/ROBAPI.v" 0
        end 
`line 135 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 136 "playground/src/resources/ROBAPI.v" 0

`line 136 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIfunct3 ( 
`line 136 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 136 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 136 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 136 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 136 "playground/src/resources/ROBAPI.v" 0
        input [5 - 1 : 0] rob_wdata 
`line 136 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 136 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 136 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 136 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 136 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 136 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 136 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_funct3(j, i, 32'b0);
`line 136 "playground/src/resources/ROBAPI.v" 0
                    end
`line 136 "playground/src/resources/ROBAPI.v" 0
                end
`line 136 "playground/src/resources/ROBAPI.v" 0
            end
`line 136 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 136 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_funct3(bankid, index, rob_wdata); 
`line 136 "playground/src/resources/ROBAPI.v" 0
            end 
`line 136 "playground/src/resources/ROBAPI.v" 0
        end 
`line 136 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 137 "playground/src/resources/ROBAPI.v" 0

`line 137 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIfunct7 ( 
`line 137 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 137 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 137 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 137 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 137 "playground/src/resources/ROBAPI.v" 0
        input [7 - 1 : 0] rob_wdata 
`line 137 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 137 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 137 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 137 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 137 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 137 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 137 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_funct7(j, i, 32'b0);
`line 137 "playground/src/resources/ROBAPI.v" 0
                    end
`line 137 "playground/src/resources/ROBAPI.v" 0
                end
`line 137 "playground/src/resources/ROBAPI.v" 0
            end
`line 137 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 137 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_funct7(bankid, index, rob_wdata); 
`line 137 "playground/src/resources/ROBAPI.v" 0
            end 
`line 137 "playground/src/resources/ROBAPI.v" 0
        end 
`line 137 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 138 "playground/src/resources/ROBAPI.v" 0

`line 138 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIshamt ( 
`line 138 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 138 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 138 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 138 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 138 "playground/src/resources/ROBAPI.v" 0
        input [5 - 1 : 0] rob_wdata 
`line 138 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 138 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 138 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 138 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 138 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 138 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 138 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_shamt(j, i, 32'b0);
`line 138 "playground/src/resources/ROBAPI.v" 0
                    end
`line 138 "playground/src/resources/ROBAPI.v" 0
                end
`line 138 "playground/src/resources/ROBAPI.v" 0
            end
`line 138 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 138 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_shamt(bankid, index, rob_wdata); 
`line 138 "playground/src/resources/ROBAPI.v" 0
            end 
`line 138 "playground/src/resources/ROBAPI.v" 0
        end 
`line 138 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 139 "playground/src/resources/ROBAPI.v" 0

`line 139 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIType ( 
`line 139 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 139 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 139 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 139 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 139 "playground/src/resources/ROBAPI.v" 0
        input [3 - 1 : 0] rob_wdata 
`line 139 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 139 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 139 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 139 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 139 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 139 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 139 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_Type(j, i, 32'b0);
`line 139 "playground/src/resources/ROBAPI.v" 0
                    end
`line 139 "playground/src/resources/ROBAPI.v" 0
                end
`line 139 "playground/src/resources/ROBAPI.v" 0
            end
`line 139 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 139 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_Type(bankid, index, rob_wdata); 
`line 139 "playground/src/resources/ROBAPI.v" 0
            end 
`line 139 "playground/src/resources/ROBAPI.v" 0
        end 
`line 139 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 140 "playground/src/resources/ROBAPI.v" 0

`line 140 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIHasRs1 ( 
`line 140 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 140 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 140 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 140 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 140 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 140 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 140 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 140 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 140 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 140 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 140 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 140 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_HasRs1(j, i, 32'b0);
`line 140 "playground/src/resources/ROBAPI.v" 0
                    end
`line 140 "playground/src/resources/ROBAPI.v" 0
                end
`line 140 "playground/src/resources/ROBAPI.v" 0
            end
`line 140 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 140 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_HasRs1(bankid, index, rob_wdata); 
`line 140 "playground/src/resources/ROBAPI.v" 0
            end 
`line 140 "playground/src/resources/ROBAPI.v" 0
        end 
`line 140 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 141 "playground/src/resources/ROBAPI.v" 0

`line 141 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIHasRs2 ( 
`line 141 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 141 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 141 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 141 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 141 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 141 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 141 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 141 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 141 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 141 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 141 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 141 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_HasRs2(j, i, 32'b0);
`line 141 "playground/src/resources/ROBAPI.v" 0
                    end
`line 141 "playground/src/resources/ROBAPI.v" 0
                end
`line 141 "playground/src/resources/ROBAPI.v" 0
            end
`line 141 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 141 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_HasRs2(bankid, index, rob_wdata); 
`line 141 "playground/src/resources/ROBAPI.v" 0
            end 
`line 141 "playground/src/resources/ROBAPI.v" 0
        end 
`line 141 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 142 "playground/src/resources/ROBAPI.v" 0

`line 142 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIHasRd ( 
`line 142 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 142 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 142 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 142 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 142 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 142 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 142 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 142 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 142 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 142 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 142 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 142 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_HasRd(j, i, 32'b0);
`line 142 "playground/src/resources/ROBAPI.v" 0
                    end
`line 142 "playground/src/resources/ROBAPI.v" 0
                end
`line 142 "playground/src/resources/ROBAPI.v" 0
            end
`line 142 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 142 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_HasRd(bankid, index, rob_wdata); 
`line 142 "playground/src/resources/ROBAPI.v" 0
            end 
`line 142 "playground/src/resources/ROBAPI.v" 0
        end 
`line 142 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 143 "playground/src/resources/ROBAPI.v" 0

`line 143 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIid ( 
`line 143 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 143 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 143 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 143 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 143 "playground/src/resources/ROBAPI.v" 0
        input [7 - 1 : 0] rob_wdata 
`line 143 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 143 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 143 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 143 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 143 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 143 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 143 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_id(j, i, 32'b0);
`line 143 "playground/src/resources/ROBAPI.v" 0
                    end
`line 143 "playground/src/resources/ROBAPI.v" 0
                end
`line 143 "playground/src/resources/ROBAPI.v" 0
            end
`line 143 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 143 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_id(bankid, index, rob_wdata); 
`line 143 "playground/src/resources/ROBAPI.v" 0
            end 
`line 143 "playground/src/resources/ROBAPI.v" 0
        end 
`line 143 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 144 "playground/src/resources/ROBAPI.v" 0

`line 144 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIps1 ( 
`line 144 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 144 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 144 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 144 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 144 "playground/src/resources/ROBAPI.v" 0
        input [8 - 1 : 0] rob_wdata 
`line 144 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 144 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 144 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 144 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 144 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 144 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 144 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_ps1(j, i, 32'b0);
`line 144 "playground/src/resources/ROBAPI.v" 0
                    end
`line 144 "playground/src/resources/ROBAPI.v" 0
                end
`line 144 "playground/src/resources/ROBAPI.v" 0
            end
`line 144 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 144 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_ps1(bankid, index, rob_wdata); 
`line 144 "playground/src/resources/ROBAPI.v" 0
            end 
`line 144 "playground/src/resources/ROBAPI.v" 0
        end 
`line 144 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 145 "playground/src/resources/ROBAPI.v" 0

`line 145 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIps2 ( 
`line 145 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 145 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 145 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 145 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 145 "playground/src/resources/ROBAPI.v" 0
        input [8 - 1 : 0] rob_wdata 
`line 145 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 145 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 145 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 145 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 145 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 145 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 145 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_ps2(j, i, 32'b0);
`line 145 "playground/src/resources/ROBAPI.v" 0
                    end
`line 145 "playground/src/resources/ROBAPI.v" 0
                end
`line 145 "playground/src/resources/ROBAPI.v" 0
            end
`line 145 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 145 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_ps2(bankid, index, rob_wdata); 
`line 145 "playground/src/resources/ROBAPI.v" 0
            end 
`line 145 "playground/src/resources/ROBAPI.v" 0
        end 
`line 145 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 146 "playground/src/resources/ROBAPI.v" 0

`line 146 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIpd ( 
`line 146 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 146 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 146 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 146 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 146 "playground/src/resources/ROBAPI.v" 0
        input [7 - 1 : 0] rob_wdata 
`line 146 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 146 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 146 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 146 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 146 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 146 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 146 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_pd(j, i, 32'b0);
`line 146 "playground/src/resources/ROBAPI.v" 0
                    end
`line 146 "playground/src/resources/ROBAPI.v" 0
                end
`line 146 "playground/src/resources/ROBAPI.v" 0
            end
`line 146 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 146 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_pd(bankid, index, rob_wdata); 
`line 146 "playground/src/resources/ROBAPI.v" 0
            end 
`line 146 "playground/src/resources/ROBAPI.v" 0
        end 
`line 146 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 147 "playground/src/resources/ROBAPI.v" 0

`line 147 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIoldpd ( 
`line 147 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 147 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 147 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 147 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 147 "playground/src/resources/ROBAPI.v" 0
        input [8 - 1 : 0] rob_wdata 
`line 147 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 147 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 147 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 147 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 147 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 147 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 147 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_oldpd(j, i, 32'b0);
`line 147 "playground/src/resources/ROBAPI.v" 0
                    end
`line 147 "playground/src/resources/ROBAPI.v" 0
                end
`line 147 "playground/src/resources/ROBAPI.v" 0
            end
`line 147 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 147 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_oldpd(bankid, index, rob_wdata); 
`line 147 "playground/src/resources/ROBAPI.v" 0
            end 
`line 147 "playground/src/resources/ROBAPI.v" 0
        end 
`line 147 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 148 "playground/src/resources/ROBAPI.v" 0

`line 148 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIrdy1 ( 
`line 148 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 148 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 148 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 148 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 148 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 148 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 148 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 148 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 148 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 148 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 148 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 148 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_rdy1(j, i, 32'b0);
`line 148 "playground/src/resources/ROBAPI.v" 0
                    end
`line 148 "playground/src/resources/ROBAPI.v" 0
                end
`line 148 "playground/src/resources/ROBAPI.v" 0
            end
`line 148 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 148 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_rdy1(bankid, index, rob_wdata); 
`line 148 "playground/src/resources/ROBAPI.v" 0
            end 
`line 148 "playground/src/resources/ROBAPI.v" 0
        end 
`line 148 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 149 "playground/src/resources/ROBAPI.v" 0

`line 149 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIrdy2 ( 
`line 149 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 149 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 149 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 149 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 149 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 149 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 149 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 149 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 149 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 149 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 149 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 149 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_rdy2(j, i, 32'b0);
`line 149 "playground/src/resources/ROBAPI.v" 0
                    end
`line 149 "playground/src/resources/ROBAPI.v" 0
                end
`line 149 "playground/src/resources/ROBAPI.v" 0
            end
`line 149 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 149 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_rdy2(bankid, index, rob_wdata); 
`line 149 "playground/src/resources/ROBAPI.v" 0
            end 
`line 149 "playground/src/resources/ROBAPI.v" 0
        end 
`line 149 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 150 "playground/src/resources/ROBAPI.v" 0

`line 150 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIrdy ( 
`line 150 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 150 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 150 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 150 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 150 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 150 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 150 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 150 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 150 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 150 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 150 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 150 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_rdy(j, i, 32'b0);
`line 150 "playground/src/resources/ROBAPI.v" 0
                    end
`line 150 "playground/src/resources/ROBAPI.v" 0
                end
`line 150 "playground/src/resources/ROBAPI.v" 0
            end
`line 150 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 150 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_rdy(bankid, index, rob_wdata); 
`line 150 "playground/src/resources/ROBAPI.v" 0
            end 
`line 150 "playground/src/resources/ROBAPI.v" 0
        end 
`line 150 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 151 "playground/src/resources/ROBAPI.v" 0

`line 151 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIisBranch ( 
`line 151 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 151 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 151 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 151 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 151 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 151 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 151 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 151 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 151 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 151 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 151 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 151 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_isBranch(j, i, 32'b0);
`line 151 "playground/src/resources/ROBAPI.v" 0
                    end
`line 151 "playground/src/resources/ROBAPI.v" 0
                end
`line 151 "playground/src/resources/ROBAPI.v" 0
            end
`line 151 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 151 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_isBranch(bankid, index, rob_wdata); 
`line 151 "playground/src/resources/ROBAPI.v" 0
            end 
`line 151 "playground/src/resources/ROBAPI.v" 0
        end 
`line 151 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 152 "playground/src/resources/ROBAPI.v" 0

`line 152 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIisStore ( 
`line 152 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 152 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 152 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 152 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 152 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 152 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 152 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 152 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 152 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 152 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 152 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 152 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_isStore(j, i, 32'b0);
`line 152 "playground/src/resources/ROBAPI.v" 0
                    end
`line 152 "playground/src/resources/ROBAPI.v" 0
                end
`line 152 "playground/src/resources/ROBAPI.v" 0
            end
`line 152 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 152 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_isStore(bankid, index, rob_wdata); 
`line 152 "playground/src/resources/ROBAPI.v" 0
            end 
`line 152 "playground/src/resources/ROBAPI.v" 0
        end 
`line 152 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 153 "playground/src/resources/ROBAPI.v" 0

`line 153 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIisLoad ( 
`line 153 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 153 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 153 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 153 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 153 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 153 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 153 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 153 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 153 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 153 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 153 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 153 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_isLoad(j, i, 32'b0);
`line 153 "playground/src/resources/ROBAPI.v" 0
                    end
`line 153 "playground/src/resources/ROBAPI.v" 0
                end
`line 153 "playground/src/resources/ROBAPI.v" 0
            end
`line 153 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 153 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_isLoad(bankid, index, rob_wdata); 
`line 153 "playground/src/resources/ROBAPI.v" 0
            end 
`line 153 "playground/src/resources/ROBAPI.v" 0
        end 
`line 153 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 154 "playground/src/resources/ROBAPI.v" 0

`line 154 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIhasException ( 
`line 154 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 154 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 154 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 154 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 154 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 154 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 154 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 154 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 154 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 154 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 154 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 154 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_hasException(j, i, 32'b0);
`line 154 "playground/src/resources/ROBAPI.v" 0
                    end
`line 154 "playground/src/resources/ROBAPI.v" 0
                end
`line 154 "playground/src/resources/ROBAPI.v" 0
            end
`line 154 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 154 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_hasException(bankid, index, rob_wdata); 
`line 154 "playground/src/resources/ROBAPI.v" 0
            end 
`line 154 "playground/src/resources/ROBAPI.v" 0
        end 
`line 154 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 155 "playground/src/resources/ROBAPI.v" 0

`line 155 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIExceptionType ( 
`line 155 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 155 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 155 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 155 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 155 "playground/src/resources/ROBAPI.v" 0
        input [8 - 1 : 0] rob_wdata 
`line 155 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 155 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 155 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 155 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 155 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 155 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 155 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_ExceptionType(j, i, 32'b0);
`line 155 "playground/src/resources/ROBAPI.v" 0
                    end
`line 155 "playground/src/resources/ROBAPI.v" 0
                end
`line 155 "playground/src/resources/ROBAPI.v" 0
            end
`line 155 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 155 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_ExceptionType(bankid, index, rob_wdata); 
`line 155 "playground/src/resources/ROBAPI.v" 0
            end 
`line 155 "playground/src/resources/ROBAPI.v" 0
        end 
`line 155 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 156 "playground/src/resources/ROBAPI.v" 0

`line 156 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIisTaken ( 
`line 156 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 156 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 156 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 156 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 156 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 156 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 156 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 156 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 156 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 156 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 156 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 156 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_isTaken(j, i, 32'b0);
`line 156 "playground/src/resources/ROBAPI.v" 0
                    end
`line 156 "playground/src/resources/ROBAPI.v" 0
                end
`line 156 "playground/src/resources/ROBAPI.v" 0
            end
`line 156 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 156 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_isTaken(bankid, index, rob_wdata); 
`line 156 "playground/src/resources/ROBAPI.v" 0
            end 
`line 156 "playground/src/resources/ROBAPI.v" 0
        end 
`line 156 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 157 "playground/src/resources/ROBAPI.v" 0

`line 157 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPItargetBrAddr ( 
`line 157 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 157 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 157 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 157 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 157 "playground/src/resources/ROBAPI.v" 0
        input [32 - 1 : 0] rob_wdata 
`line 157 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 157 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 157 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 157 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 157 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 157 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 157 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_targetBrAddr(j, i, 32'b0);
`line 157 "playground/src/resources/ROBAPI.v" 0
                    end
`line 157 "playground/src/resources/ROBAPI.v" 0
                end
`line 157 "playground/src/resources/ROBAPI.v" 0
            end
`line 157 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 157 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_targetBrAddr(bankid, index, rob_wdata); 
`line 157 "playground/src/resources/ROBAPI.v" 0
            end 
`line 157 "playground/src/resources/ROBAPI.v" 0
        end 
`line 157 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 158 "playground/src/resources/ROBAPI.v" 0

`line 158 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIreg_wb_data ( 
`line 158 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 158 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 158 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 158 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 158 "playground/src/resources/ROBAPI.v" 0
        input [32 - 1 : 0] rob_wdata 
`line 158 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 158 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 158 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 158 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 158 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 158 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 158 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_reg_wb_data(j, i, 32'b0);
`line 158 "playground/src/resources/ROBAPI.v" 0
                    end
`line 158 "playground/src/resources/ROBAPI.v" 0
                end
`line 158 "playground/src/resources/ROBAPI.v" 0
            end
`line 158 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 158 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_reg_wb_data(bankid, index, rob_wdata); 
`line 158 "playground/src/resources/ROBAPI.v" 0
            end 
`line 158 "playground/src/resources/ROBAPI.v" 0
        end 
`line 158 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 159 "playground/src/resources/ROBAPI.v" 0

`line 159 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIstoreIdx ( 
`line 159 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 159 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 159 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 159 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 159 "playground/src/resources/ROBAPI.v" 0
        input [8 - 1 : 0] rob_wdata 
`line 159 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 159 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 159 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 159 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 159 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 159 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 159 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_storeIdx(j, i, 32'b0);
`line 159 "playground/src/resources/ROBAPI.v" 0
                    end
`line 159 "playground/src/resources/ROBAPI.v" 0
                end
`line 159 "playground/src/resources/ROBAPI.v" 0
            end
`line 159 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 159 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_storeIdx(bankid, index, rob_wdata); 
`line 159 "playground/src/resources/ROBAPI.v" 0
            end 
`line 159 "playground/src/resources/ROBAPI.v" 0
        end 
`line 159 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 160 "playground/src/resources/ROBAPI.v" 0

`line 160 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIgbranch_res ( 
`line 160 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 160 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 160 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 160 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 160 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 160 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 160 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 160 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 160 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 160 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 160 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 160 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_gbranch_res(j, i, 32'b0);
`line 160 "playground/src/resources/ROBAPI.v" 0
                    end
`line 160 "playground/src/resources/ROBAPI.v" 0
                end
`line 160 "playground/src/resources/ROBAPI.v" 0
            end
`line 160 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 160 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_gbranch_res(bankid, index, rob_wdata); 
`line 160 "playground/src/resources/ROBAPI.v" 0
            end 
`line 160 "playground/src/resources/ROBAPI.v" 0
        end 
`line 160 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 161 "playground/src/resources/ROBAPI.v" 0

`line 161 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIlbranch_res ( 
`line 161 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 161 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 161 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 161 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 161 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 161 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 161 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 161 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 161 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 161 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 161 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 161 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_lbranch_res(j, i, 32'b0);
`line 161 "playground/src/resources/ROBAPI.v" 0
                    end
`line 161 "playground/src/resources/ROBAPI.v" 0
                end
`line 161 "playground/src/resources/ROBAPI.v" 0
            end
`line 161 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 161 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_lbranch_res(bankid, index, rob_wdata); 
`line 161 "playground/src/resources/ROBAPI.v" 0
            end 
`line 161 "playground/src/resources/ROBAPI.v" 0
        end 
`line 161 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 162 "playground/src/resources/ROBAPI.v" 0

`line 162 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIbranch_res ( 
`line 162 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 162 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 162 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 162 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 162 "playground/src/resources/ROBAPI.v" 0
        input [1 - 1 : 0] rob_wdata 
`line 162 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 162 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 162 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 162 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 162 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 162 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 162 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_branch_res(j, i, 32'b0);
`line 162 "playground/src/resources/ROBAPI.v" 0
                    end
`line 162 "playground/src/resources/ROBAPI.v" 0
                end
`line 162 "playground/src/resources/ROBAPI.v" 0
            end
`line 162 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 162 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_branch_res(bankid, index, rob_wdata); 
`line 162 "playground/src/resources/ROBAPI.v" 0
            end 
`line 162 "playground/src/resources/ROBAPI.v" 0
        end 
`line 162 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 163 "playground/src/resources/ROBAPI.v" 0

`line 163 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIglobal_pht_idx ( 
`line 163 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 163 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 163 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 163 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 163 "playground/src/resources/ROBAPI.v" 0
        input [13 - 1 : 0] rob_wdata 
`line 163 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 163 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 163 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 163 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 163 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 163 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 163 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_global_pht_idx(j, i, 32'b0);
`line 163 "playground/src/resources/ROBAPI.v" 0
                    end
`line 163 "playground/src/resources/ROBAPI.v" 0
                end
`line 163 "playground/src/resources/ROBAPI.v" 0
            end
`line 163 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 163 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_global_pht_idx(bankid, index, rob_wdata); 
`line 163 "playground/src/resources/ROBAPI.v" 0
            end 
`line 163 "playground/src/resources/ROBAPI.v" 0
        end 
`line 163 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 164 "playground/src/resources/ROBAPI.v" 0

`line 164 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIlocal_pht_idx ( 
`line 164 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 164 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 164 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 164 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 164 "playground/src/resources/ROBAPI.v" 0
        input [13 - 1 : 0] rob_wdata 
`line 164 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 164 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 164 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 164 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 164 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 164 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 164 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_local_pht_idx(j, i, 32'b0);
`line 164 "playground/src/resources/ROBAPI.v" 0
                    end
`line 164 "playground/src/resources/ROBAPI.v" 0
                end
`line 164 "playground/src/resources/ROBAPI.v" 0
            end
`line 164 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 164 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_local_pht_idx(bankid, index, rob_wdata); 
`line 164 "playground/src/resources/ROBAPI.v" 0
            end 
`line 164 "playground/src/resources/ROBAPI.v" 0
        end 
`line 164 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 165 "playground/src/resources/ROBAPI.v" 0

`line 165 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIbht_idx ( 
`line 165 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 165 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 165 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 165 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 165 "playground/src/resources/ROBAPI.v" 0
        input [8 - 1 : 0] rob_wdata 
`line 165 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 165 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 165 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 165 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 165 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 165 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 165 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_bht_idx(j, i, 32'b0);
`line 165 "playground/src/resources/ROBAPI.v" 0
                    end
`line 165 "playground/src/resources/ROBAPI.v" 0
                end
`line 165 "playground/src/resources/ROBAPI.v" 0
            end
`line 165 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 165 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_bht_idx(bankid, index, rob_wdata); 
`line 165 "playground/src/resources/ROBAPI.v" 0
            end 
`line 165 "playground/src/resources/ROBAPI.v" 0
        end 
`line 165 "playground/src/resources/ROBAPI.v" 0
    endmodule 

`line 166 "playground/src/resources/ROBAPI.v" 0

`line 166 "playground/src/resources/ROBAPI.v" 0
    module ROBWriteAPIbranch_pred_addr ( 
`line 166 "playground/src/resources/ROBAPI.v" 0
        input clk, 
`line 166 "playground/src/resources/ROBAPI.v" 0
        input rst, 
`line 166 "playground/src/resources/ROBAPI.v" 0
        input [7:0] index, 
`line 166 "playground/src/resources/ROBAPI.v" 0
        input [1:0] bankid, 
`line 166 "playground/src/resources/ROBAPI.v" 0
        input [32 - 1 : 0] rob_wdata 
`line 166 "playground/src/resources/ROBAPI.v" 0
    ); 
`line 166 "playground/src/resources/ROBAPI.v" 0
        always @(posedge clk) begin 
`line 166 "playground/src/resources/ROBAPI.v" 0
            if(rst) begin 
`line 166 "playground/src/resources/ROBAPI.v" 0
                integer i, j; 
`line 166 "playground/src/resources/ROBAPI.v" 0
                for(j=0;j<4;j=j+1) begin 
`line 166 "playground/src/resources/ROBAPI.v" 0
                    for(i=0;i<(1 << 7);i=i+1) begin
`line 166 "playground/src/resources/ROBAPI.v" 0
                        npc_rob_write_branch_pred_addr(j, i, 32'b0);
`line 166 "playground/src/resources/ROBAPI.v" 0
                    end
`line 166 "playground/src/resources/ROBAPI.v" 0
                end
`line 166 "playground/src/resources/ROBAPI.v" 0
            end
`line 166 "playground/src/resources/ROBAPI.v" 0
            else begin 
`line 166 "playground/src/resources/ROBAPI.v" 0
                npc_rob_write_branch_pred_addr(bankid, index, rob_wdata); 
`line 166 "playground/src/resources/ROBAPI.v" 0
            end 
`line 166 "playground/src/resources/ROBAPI.v" 0
        end 
`line 166 "playground/src/resources/ROBAPI.v" 0
    endmodule 
`line 166 "playground/src/resources/ROBAPI.v" 0
;

`line 168 "playground/src/resources/ROBAPI.v" 0
module ROBIdLocMemWriteAPI (
    input clk,
    input rst,
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
        else begin
            npc_rob_id_loc_mem_write(index, data);
        end 
    end
endmodule

`line 187 "playground/src/resources/ROBAPI.v" 0
module ROBIdLocMemReadAPI (
    input rst,
    input [7:0] index,
    output reg [7:0] data
);
    always @(posedge clk) begin
        if(rst) begin
            data <= 8'b0;
        end
        else begin
            data <= npc_rob_id_loc_mem_read(index)[7: 0];
        end
    end
endmodule
`line 201 "playground/src/resources/ROBAPI.v" 0
