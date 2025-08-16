import "DPI-C" function void npc_gpht_write(
    input shortint waddr,
    input byte wdata
);

import "DPI-C" function byte npc_gpht_read(
    input shortint raddr
);

import "DPI-C" function void npc_lpht_write(
    input shortint waddr,
    input byte wdata
);

import "DPI-C" function byte npc_lpht_read(
    input shortint raddr
);

import "DPI-C" function void npc_cpht_write(
    input shortint waddr,
    input byte wdata
);

import "DPI-C" function byte npc_cpht_read(
    input shortint raddr
);

module gPHTWriteAPI (
    input clk,
    input rst,
    input wen, 
    input [12:0] waddr,
    input [1:0] wdata
);
integer i;
always @(posedge clk) begin
    if(rst) begin
        for (i=0;i<(1 << 13);i=i+1) begin
            npc_gpht_write(i, 2'b11);
        end
    end
    else if(wen) begin
        npc_gpht_write(waddr, wdata);
    end
end
endmodule

module gPHTReadAPI (
    input clk,
    input rst,
    input ren, 
    input [12:0] raddr,
    output reg [1:0] rdata
);

always @(*) begin
    if(rst | ~ren) begin
        rdata = 2'b00;
    end
    else if(ren) begin
        rdata = npc_gpht_read(raddr);
    end
end
endmodule

module lPHTWriteAPI (
    input clk,
    input rst,
    input wen, 
    input [12:0] waddr,
    input [1:0] wdata
);
integer i;
always @(posedge clk) begin
    if(rst) begin
        for (i=0;i<(1 << 13);i=i+1) begin
            npc_lpht_write(i, 2'b11);
        end
    end
    else if(wen) begin
        npc_lpht_write(waddr, wdata);
    end
end
endmodule

module lPHTReadAPI (
    input clk,
    input rst,
    input ren, 
    input [12:0] raddr,
    output reg [1:0] rdata
);

always @(*) begin
    if(rst | ~ren) begin
        rdata = 2'b00;
    end
    else if(ren) begin
        rdata = npc_lpht_read(raddr);
    end
end
endmodule

module cPHTWriteAPI (
    input clk,
    input rst,
    input wen, 
    input [12:0] waddr,
    input [1:0] wdata
);
integer i;
always @(posedge clk) begin
    if(rst) begin
        for (i=0;i<(1 << 13);i=i+1) begin
            npc_cpht_write(i, 2'b11);
        end
    end
    else if(wen) begin
        npc_cpht_write(waddr, wdata);
    end
end
endmodule

module cPHTReadAPI (
    input clk,
    input rst,
    input ren, 
    input [12:0] raddr,
    output reg [1:0] rdata
);

always @(*) begin
    if(rst | ~ren) begin
        rdata = 2'b00;
    end
    else if(ren) begin
        rdata = npc_cpht_read(raddr);
    end
end
endmodule
