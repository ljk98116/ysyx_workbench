// used for load/store simulation
// used for fetch inst simulation
import "DPI-C" function int pmem_read(input int raddr);
import "DPI-C" function void pmem_write(input int waddr, input int wdata, input byte wmask);

module InstSram #(
    parameter ADDR_WIDTH = 32,
    parameter DATA_WIDTH = 32
) (
    input clk,
    input rst,

    input en0,
    input [ADDR_WIDTH - 1: 0] raddr0,
    output reg [DATA_WIDTH - 1: 0] rdata0,

    input en1,
    input [ADDR_WIDTH - 1: 0] raddr1,
    output reg [DATA_WIDTH - 1: 0] rdata1,

    input en2,
    input [ADDR_WIDTH - 1: 0] raddr2,
    output reg [DATA_WIDTH - 1: 0] rdata2,

    input en3,
    input [ADDR_WIDTH - 1: 0] raddr3,
    output reg [DATA_WIDTH - 1: 0] rdata3,
);
    always@(posedge clk) begin
        if (!rst) begin
            rdata0 <= 32'b0;
        end
        else if(en0) begin
            rdata0 <= pmem_read(raddr0);
        end
    end

    always@(posedge clk) begin
        if (!rst) begin
            rdata1 <= 32'b0;
        end
        else if(en1) begin
            rdata1 <= pmem_read(raddr1);
        end
    end

    always@(posedge clk) begin
        if (!rst) begin
            rdata2 <= 32'b0;
        end
        else if(en2) begin
            rdata2 <= pmem_read(raddr2);
        end
    end

    always@(posedge clk) begin
        if (!rst) begin
            rdata3 <= 32'b0;
        end
        else if(en0) begin
            rdata3 <= pmem_read(raddr3);
        end
    end
endmodule