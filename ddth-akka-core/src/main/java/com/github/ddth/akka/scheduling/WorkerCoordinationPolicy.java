package com.github.ddth.akka.scheduling;

/**
 * Define how worker instances on different nodes are coordinated.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.1
 */
public enum WorkerCoordinationPolicy {
    /**
     * Worker instance takes all tasks. Multiple tasks can be executed
     * simultaneously on same or different nodes.
     */
    TAKE_ALL_TASKS(0),

    /**
     * On one node, worker can take one task as a time.But workers one two or
     * more nodes can execute tasks simultaneously.
     */
    LOCAL_SINGLETON(1),

    /**
     * Once worker takes a task, all of its instances on all nodes are marked
     * "busy" and can not take any more task until free.
     */
    GLOBAL_SINGLETON(2);

    private int value;

    private WorkerCoordinationPolicy(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
