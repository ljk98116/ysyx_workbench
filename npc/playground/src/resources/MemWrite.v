import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);

module MemWriteAPI(
    input clk,
    input rst,
    input [7:0] wmask,
    input [31: 0] waddr,
    input [31: 0] wdata
);
    always@(posedge clk) begin
        if(!rst) begin
            pmem_write(waddr, wdata, wmask);
        end
    end    
endmodule