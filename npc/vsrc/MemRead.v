import "DPI-C" function int npc_pmem_read(input int raddr);

module MemReadAPI(
    input clk,
    input rst,
    input [31:0] raddr,
    output reg [31:0] rdata
);
    always@(posedge clk) begin
        if (rst) begin
            rdata <= 32'b0;
        end
        else begin
            rdata <= npc_pmem_read(raddr);
        end
    end    
endmodule
