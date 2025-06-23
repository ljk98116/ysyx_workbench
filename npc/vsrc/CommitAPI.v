import "DPI-C" function void npc_commit(
    input byte valid,
    input byte rat_write_en,
    input byte rat_write_addr_0,
    input byte rat_write_addr_1,
    input byte rat_write_addr_2,
    input byte rat_write_addr_3,  
    input byte rat_write_data_0,
    input byte rat_write_data_1,
    input byte rat_write_data_2,
    input byte rat_write_data_3,
    input int reg_write_data_0,
    input int reg_write_data_1,
    input int reg_write_data_2,
    input int reg_write_data_3,
    input int pc0,
    input int pc1,
    input int pc2,
    input int pc3    
);

module CommitAPI (
    input rst,
    input [7:0] valid,
    input [7:0] rat_write_en,  
    input [7:0] rat_write_addr_0,
    input [7:0] rat_write_addr_1,
    input [7:0] rat_write_addr_2,
    input [7:0] rat_write_addr_3,
    input [7:0] rat_write_data_0,
    input [7:0] rat_write_data_1,
    input [7:0] rat_write_data_2,
    input [7:0] rat_write_data_3, 
    input [31:0] reg_write_data_0,
    input [31:0] reg_write_data_1,
    input [31:0] reg_write_data_2,
    input [31:0] reg_write_data_3,
    input [31:0] pc0,
    input [31:0] pc1,
    input [31:0] pc2,
    input [31:0] pc3  
);
    always @(*) begin
        if(!rst) begin
            npc_commit(
                valid,
                rat_write_en,
                rat_write_addr_0,
                rat_write_addr_1,
                rat_write_addr_2,
                rat_write_addr_3,
                rat_write_data_0,
                rat_write_data_1,
                rat_write_data_2,
                rat_write_data_3,
                reg_write_data_0,
                reg_write_data_1,
                reg_write_data_2,
                reg_write_data_3,
                pc0,
                pc1,
                pc2,
                pc3
            );
        end
    end
endmodule
