package br.com.finalcraft.evernifecore.minecraft.vector;

import br.com.finalcraft.evernifecore.config.Config;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class BlockPos implements Comparable<BlockPos>, Config.Salvable {

    private int floor_double(double value){
        int i = (int) value;
        return value < (double)i ? i - 1 : i;
    }

    private final int x;
    private final int y;
    private final int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(double x, double y, double z) {
        this.x = floor_double(x);
        this.y = floor_double(y);
        this.z = floor_double(z);
    }

    public static BlockPos from(Location location){
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockPos from(Entity entity){
        return from(entity.getLocation());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPos add(int x, int y, int z){
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public int compareTo(BlockPos o) {
        if (this.getY() == o.getY()) {
            return this.getZ() == o.getZ() ? this.getX() - o.getX() : this.getZ() - o.getZ();
        } else {
            return this.getY() - o.getY();
        }
    }

    @Override
    public int hashCode() { //Forge HashCode for BlockPost https://forums.minecraftforge.net/topic/88361-discussion-safe-to-use-blockposhashcode/
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof BlockPos)) {
            return false;
        } else {
            BlockPos blockPos = (BlockPos)o;
            if (this.getX() != blockPos.getX()) {
                return false;
            } else if (this.getY() != blockPos.getY()) {
                return false;
            } else {
                return this.getZ() == blockPos.getZ();
            }
        }
    }

    public ChunkPos getChunkPos(){
        return new ChunkPos(this);
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "," + z + "]";
    }

    @Override
    public void onConfigSave(Config config, String path) {
        config.setValue(path + ".x", x);
        config.setValue(path + ".y", y);
        config.setValue(path + ".z", z);
    }

    @Config.Loadable
    public static BlockPos onConsigLoad(Config config, String path){
        int x = config.getInt(path + ".x");
        int y = config.getInt(path + ".y");
        int z = config.getInt(path + ".z");
        return new BlockPos(x,y,z);
    }

    public static class MutableBlockPos extends BlockPos {
        protected int x;
        protected int y;
        protected int z;

        public MutableBlockPos() {
            super(0, 0, 0);
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }

        public MutableBlockPos setPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public MutableBlockPos setX(int x) {
            this.x = x;
            return this;
        }

        public MutableBlockPos setY(int y) {
            this.y = y;
            return this;
        }

        public MutableBlockPos setZ(int z) {
            this.z = z;
            return this;
        }

        public BlockPos toImmutable() {
            return new BlockPos(x,y,z);
        }
    }
}
