import "DPI-C" function void npc_btb_write(
    input shortint waddr,
    input int valid,
    input int BIA,
    input int BTA
);

import "DPI-C" function int npc_btb_read_V(
    input shortint raddr
);
import "DPI-C" function int npc_btb_read_BTA(
    input shortint raddr
);
import "DPI-C" function int npc_btb_read_BIA(
    input shortint raddr
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
integer i;
always @( posedge clk ) begin
    if(rst) begin
        for(i=0;i<(1 << 13);i=i+1) begin
            npc_btb_write(i, 8'b0, 32'b0, 64'b0);
        end
    end
    else if(wen) begin
        npc_btb_write({3'b0, waddr}, {7'b0, V}, {24'b0, BIA}, {32'b0, BTA});
    end
end

endmodule

module BTBReadVAPI(
    input rst,
    input ren,
    input [12:0] raddr,
    output V
);
reg [31:0] V_o;
always @(*) begin
    if(rst | ~ren) begin
        V_o = 32'b0;
    end    
    else if(ren) begin
        V_o = npc_btb_read_V({3'b0, raddr});
    end
end 
assign V = V_o[0];
endmodule

module BTBReadBIAAPI(
    input rst,
    input ren,
    input [12:0] raddr,
    output [7:0] BIA
);
reg [31:0] bia;
always @(*) begin
    if(rst | ~ren) begin
        bia = 32'b0;
    end    
    else if(ren) begin
        bia = npc_btb_read_BIA({3'b0, raddr});
    end
end 
assign BIA = bia[7:0];

endmodule

module BTBReadBTAAPI(
    input rst,
    input ren,
    input [12:0] raddr,
    output reg [31:0] BTA
);

always @(*) begin
    if(rst | ~ren) begin
        BTA = 32'b0;
    end    
    else if(ren) begin
        BTA = npc_btb_read_BTA({3'b0, raddr});
    end
end 

endmodule

