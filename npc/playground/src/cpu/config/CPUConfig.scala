package cpu.config

object base
{
    val ADDR_WIDTH = 32
    val DATA_WIDTH = 32
    val AREG_WIDTH = 5
    val PREG_WIDTH = 8
    val ROBID_WIDTH = 8
    val RESET_VECTOR = "h80000000"
    val FETCH_WIDTH = 4
    val ALU_NUM = 4
    val AGU_NUM = 2
    val STORE_BUF_SZ = 32
    val PHTID_WIDTH = 13 /* 13宽度的PHT出现别名错误对结果影响最小 */
    val BHRID_WIDTH = 5 /* 支持最多5次局部历史分支 */
    val BHTID_WIDTH = PHTID_WIDTH - BHRID_WIDTH
}

object ExceptionType {
    val NORMAL = "h00"
    val BRANCH_PREDICTION_ERROR = "hFF"

}

object BRANCH_TYPE{
    val DIRECT_JUMP = "b00"
    val INDIRECT_JUMP = "b01"
    val CALL_JUMP = "b10"
    val RET_JUMP = "b11"
}