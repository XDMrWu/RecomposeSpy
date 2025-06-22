package com.xdmrwu.recompose.spy.runtime

/**
 * @Author: wulinpeng
 * @Date: 2025/6/14 16:17
 * @Description:
 */
/**
 * An enum of the different "states" a parameter of a composable function can have relating to
 * comparison propagation. Each state is represented by two bits in the `$changed` bitmask.
 */
enum class ParamState(val bits: Int) {
    /**
     * Indicates that nothing is certain about the current state of the parameter. It could be
     * different than it was during the last execution, or it could be the same, but it is not
     * known so the current function looking at it must call equals on it in order to find out.
     * This is the only state that can cause the function to spend slot table space in order to
     * look at it.
     */
    Uncertain(0b000),

    /**
     * This indicates that the value is known to be the same since the last time the function was
     * executed. There is no need to store the value in the slot table in this case because the
     * calling function will *always* know whether the value was the same or different as it was
     * in the previous execution.
     */
    Same(0b001),

    /**
     * This indicates that the value is known to be different since the last time the function
     * was executed. There is no need to store the value in the slot table in this case because
     * the calling function will *always* know whether the value was the same or different as it
     * was in the previous execution.
     */
    Different(0b010),

    /**
     * This indicates that the value is known to *never change* for the duration of the running
     * program.
     */
    Static(0b011),
    Unknown(0b100),
    Mask(0b111);
}


const val BITS_PER_INT = 31
const val SLOTS_PER_INT = 10
const val BITS_PER_SLOT = 3