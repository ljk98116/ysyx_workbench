import "DPI-C" function void npc_update_branch_predict(
    input byte is_branch,
    input byte branch_pred_err
);

module BranchPredAPI (
    input rst,
    input [7:0] is_branch,
    input [7:0] branch_pred_err
);
    always @(*) begin
        if(!rst) begin
            npc_update_branch_predict(is_branch, branch_pred_err);
        end
    end
endmodule
