package com.github.ddth.akka;

/**
 * Define how message handlers are matched.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public enum MessageHandlerMatchingType {
    /**
     * Check for exact match and invoke the matched handler, then stop.
     */
    EXACT_MATCH_ONLY(0),

    /**
     * If exactly matched, invoke the matched handler, then stop; otherwise,
     * invoke all interface-matched handlers.
     */
    EXACT_MATCH_THEN_INTERFACE(1),

    /**
     * Invoke all interface-matched handlers, do not check for exact match.
     */
    INTERFACE_MATCH_ONLY(2);

    private int value;

    private MessageHandlerMatchingType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
