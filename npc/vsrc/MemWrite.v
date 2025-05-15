import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);

module MemWrite(
    input clk,
    input rst,
    input [7:0] wmask,
    input [31: 0] waddr,
    input [31: 0] wdata,
);
    always@(posedge clk) begin
        if(!rst) begin
            pmem_write(addr, wdata, wmask);
        end
    end    
endmodule
