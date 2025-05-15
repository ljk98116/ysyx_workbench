import "DPI-C" function int pmem_read(input int raddr);

module MemRead(
    input clk,
    input rst,
    input [31:0] raddr,
    output [31:0] rdata
);
    always@(posedge clk) begin
        if (!rst) begin
            rdata <= 32'b0;
        end
        else begin
            rdata <= pmem_read(addr);
        end
    end    
endmodule
