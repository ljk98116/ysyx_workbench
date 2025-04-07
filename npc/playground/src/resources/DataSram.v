import "DPI-C" function int pmem_read(input int raddr);
import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);

module DataSram #(
    parameter ADDR_WIDTH = 32,
    parameter DATA_WIDTH = 32
) (
    input clk,
    input rst,
    input en,
    input wen,
    input [7:0] wmask,
    input [ADDR_WIDTH - 1: 0] waddr,
    input [DATA_WIDTH - 1: 0] wdata,

    input [ADDR_WIDTH - 1: 0] raddr,
    output reg [DATA_WIDTH - 1: 0] rdata,
);
    always@(posedge clk) begin
        if (!rst) begin
            rdata <= 32'b0;
        end
        else if(en) begin
            rdata <= pmem_read(addr);
        end
    end

    always@(posedge clk) begin
        if(en & wen) begin
            pmem_write(addr, wdata, wmask);
        end
    end
endmodule