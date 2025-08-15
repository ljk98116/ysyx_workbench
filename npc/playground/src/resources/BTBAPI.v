/* BTB DPI-C优化实现, 用于仿真 */
import DPI-C function void npc_btb_write(
    input short waddr,
    input byte valid,
    input byte BIA,
    input int BTA
);

import DPI-C function int npc_btb_read_V(
    input short raddr
);
import DPI-C function int npc_btb_read_BTA(
    input short raddr
);
import DPI-C function int npc_btb_read_BIA(
    input short raddr
);

module BTBWriteAPI(
    input clk,
    input rst,
    input wen,
    input [12:0] waddr,
    input V,
    input [7:0] BIA,
    input [31:0] BTA
);

always @( posedge clk ) begin
    if(wen) begin
        npc_btb_write({3'b0, waddr}, {7'b0, V}, BIA, BTA);
    end
end

endmodule

module BTBReadVAPI(
    input rst,
    input ren,
    input [12:0] raddr,
    output reg V,
);

always @(*) begin
    if(!rst) begin
        V <= 1'b0;
    end    
    else if(ren) begin
        V <= npc_btb_read_V({4'b0, raddr})[0];
    end
end 

endmodule

module BTBReadBIAAPI(
    input rst,
    input ren,
    input [12:0] raddr,
    output reg [7:0] BIA,
);

always @(*) begin
    if(!rst) begin
        BIA <= 8'b0;
    end    
    else if(ren) begin
        BIA <= npc_btb_read_BIA({4'b0, raddr})[7:0];
    end
end 

endmodule

module BTBReadBTAPI(
    input rst,
    input ren,
    input [12:0] raddr,
    output reg [31:0] BTA,
);

always @(*) begin
    if(!rst) begin
        BTA <= 32'b0;
    end    
    else if(ren) begin
        BTA <= npc_btb_read_BTA({4'b0, raddr});
    end
end 

endmodule

