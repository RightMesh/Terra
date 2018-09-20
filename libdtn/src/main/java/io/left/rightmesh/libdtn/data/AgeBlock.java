package io.left.rightmesh.libdtn.data;

/**
 * AgeBlock is a block that can be used to track lifetime for DTN node that doesn't have access to
 * UTC time but have a mean to track the elapsed time between reception and delivery of the block.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public class AgeBlock extends ExtensionBlock {

    public static final int type = 8;

    public long age = 0;
    public long time_start;
    public long time_end;

    public AgeBlock() {
        super(type);
        setV6Flag(BlockV6Flags.REPLICATE_IN_EVERY_FRAGMENT, true);
        start();
    }

    AgeBlock(long age) {
        super(type);
        setV6Flag(BlockV6Flags.REPLICATE_IN_EVERY_FRAGMENT, true);
        this.age = age;
        start();
    }

    /**
     * Start aging this AgeBlock.
     */
    public void start() {
        time_start = System.nanoTime();
    }

    /**
     * Stop aging this AgeBlock.
     */
    public void stop() {
        time_end = System.nanoTime();
    }

    @Override
    public String toString() {
        long local_time_spent = (System.nanoTime() - time_start);
        StringBuilder sb = new StringBuilder("AgeBlock");
        sb.append(": age="+age+" + "+ local_time_spent/1000);
        return sb.toString();
    }
}
