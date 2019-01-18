package io.left.rightmesh.libdtn.common.data;

/**
 * AgeBlock is a block that can be used to track lifetime for node that doesn't have access to UTC
 * time but have a mean to track the elapsed time between reception and delivery of the block.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class AgeBlock extends ExtensionBlock {

    public static final int AGE_BLOCK_TYPE = 8;

    public long age = 0;
    public long timeStart;
    public long timeEnd;

    /**
     * Constructor.
     */
    public AgeBlock() {
        super(AGE_BLOCK_TYPE);
        setV7Flag(BlockV7Flags.REPLICATE_IN_EVERY_FRAGMENT, true);
        start();
    }

    AgeBlock(long age) {
        super(AGE_BLOCK_TYPE);
        setV7Flag(BlockV7Flags.REPLICATE_IN_EVERY_FRAGMENT, true);
        this.age = age;
        start();
    }

    /**
     * Start ageing this AgeBlock.
     */
    public void start() {
        timeStart = System.nanoTime();
    }

    /**
     * Stop ageing this AgeBlock.
     */
    public void stop() {
        timeEnd = System.nanoTime();
    }

    @Override
    public String toString() {
        long localTimeSpent = (System.nanoTime() - timeStart);
        StringBuilder sb = new StringBuilder("AgeBlock");
        sb.append(": age=" + age + " + " + localTimeSpent / 1000);
        return sb.toString();
    }
}
