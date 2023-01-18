package br.com.finalcraft.evernifecore.itemdatapart.datapart;

import br.com.finalcraft.evernifecore.itemdatapart.ItemDataPart;
import br.com.finalcraft.evernifecore.itemstack.FCItemFactory;
import br.com.finalcraft.evernifecore.util.FCNBTUtil;
import br.com.finalcraft.evernifecore.version.MCVersion;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemDataPartNBT extends ItemDataPart {

    @Override
    public ItemStack transform(ItemStack item, String used_name, String argument) {
        return FCItemFactory.from(item)
                .setNbt(FCNBTUtil.getFrom(argument.trim()))
                .build();
    }

    @Override
    public boolean isSimilar(ItemStack base_item, ItemStack other_item) {
        return true; //To expensive to check
    }

    @Override
    public List<String> read(ItemStack i, List<String> output) {
        NBTCompound compound = FCNBTUtil.getFrom( //Clone it because we may need to remove the "display" tag, and the "Damage" tag as well
                FCNBTUtil.getFrom(i).toString()
        );

        if (!compound.isEmpty()){
            compound.removeKey("display");//Remove LORE and DisplayName
            if (MCVersion.isHigherEquals(MCVersion.v1_13)){
                compound.removeKey("Damage");//Remove Damage key
            }
            if (!compound.isEmpty()){
                output.add("nbt: " + compound.toString());
            }
        }

        return output;
    }

    @Override
    public int getPriority() {
        return 5; // AFTER "PRIORITY_MOST_EARLY"
    }

    @Override
    public boolean removeSpaces() {
        return false;
    }

    @Override
    public String[] createNames() {
        return new String[]{"nbt", "rawnbt"};
    }

}
