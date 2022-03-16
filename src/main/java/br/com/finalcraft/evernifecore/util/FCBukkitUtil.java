package br.com.finalcraft.evernifecore.util;

import br.com.finalcraft.everforgelib.util.StatisticUtil;
import br.com.finalcraft.evernifecore.EverNifeCore;
import br.com.finalcraft.evernifecore.config.playerdata.PlayerData;
import br.com.finalcraft.evernifecore.config.uuids.UUIDsController;
import br.com.finalcraft.evernifecore.integration.VaultIntegration;
import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.nms.util.NMSUtils;
import br.com.finalcraft.evernifecore.ontime.OntimeManager;
import br.com.finalcraft.evernifecore.util.reflection.MethodInvoker;
import br.com.finalcraft.evernifecore.version.MCVersion;
import br.com.finalcraft.evernifecore.version.ServerType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FCBukkitUtil {

    @Deprecated
    public static Random random = new Random();
    public static CommandSender consoleSender = Bukkit.getConsoleSender();
    private static MethodInvoker<Boolean> methodLoader_isLoaded;
    static {
        try {
            methodLoader_isLoaded = ReflectionUtil.getMethod(
                    MCVersion.isLegacy()
                            ? "cpw.mods.fml.common.Loader"
                            : "net.minecraftforge.fml.common.Loader",
                    "isModLoaded",
                    String.class
            );
        }catch (Exception ignored){
            methodLoader_isLoaded = null;
        }
    }


    public static Random getRandom() {
        return random;
    }

    public static void playSound(String playerName, String music) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null){
            if (MCVersion.isLegacy()){
                makeConsoleExecuteCommand("playsound " + music + " " + playerName + " ~0 ~0 ~0 100");
            }else {
                player.playSound(player.getLocation(), music, SoundCategory.AMBIENT, 100, 1);
            }
        }
    }

    public static void playSoundAll(String music) {
        if (MCVersion.isLegacy()){
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                makeConsoleExecuteCommand("playsound " + music + " " + player.getName() + " ~0 ~0 ~0 100");
            }
        }else {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), music, SoundCategory.AMBIENT, 100, 1);
            }
        }
    }

    public static void playSoundAll(String music, int speed) {
        if (MCVersion.isLegacy()){
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                makeConsoleExecuteCommand("playsound " + music + " " + player.getName() + " ~0 ~0 ~0 100 " + speed);
            }
        }else {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), music, SoundCategory.AMBIENT, 100, speed);
            }
        }
    }

    public static double normalizeDouble(double value) {
        return FCMathUtil.normalizeDouble(value);
    }

    public static double normalizeDouble(double value, int zeros) {
        return FCMathUtil.normalizeDouble(value, zeros);
    }

    public static boolean isFakePlayer(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        return player == null || isFakePlayer(player);
    }

    public static boolean isFakePlayer(Player player) {
        //TODO Remove this nullCheck
        return ServerType.isModdedServer() && NMSUtils.get() != null ? NMSUtils.get().isFakePlayer(player) : false;
    }

    //===========================================================================================
    //  Documented Functions
    //===========================================================================================

    /**
     * Coloca itens no inventário de um jogador, casos obre itens, eles serão dropados no chão!
     *
     * @param player Instancia do jogador que receberá os itens
     * @param itemStacks Itens que serão entregues.
     */
    public static void giveItemsTo(Player player, ItemStack... itemStacks) {
        giveItemsTo(player, true, itemStacks);
    }

    @FCLocale(lang = LocaleType.EN_US, text = "§e§l ▶ §eYou received items but did not have inventory space. The extra items were dropped on the ground!")
    @FCLocale(lang = LocaleType.PT_BR, text = "§e§l ▶ §eVocê recebeu itens mas não tinha espaço suficiente no inventário. Os itens foram dropados no chão!")
    private static LocaleMessage YOU_RECEIVED_EXTRA_ITEMS_THAT_WERE_DROPED;
    public static void giveItemsTo(Player player, boolean dropIfExceeded, ItemStack... itemStacks) {
        HashMap<Integer, ItemStack> exceededItems = player.getInventory().addItem(itemStacks);
        if (exceededItems.size() > 0 && dropIfExceeded) {
            YOU_RECEIVED_EXTRA_ITEMS_THAT_WERE_DROPED
                    .send(player);

            final World world = player.getWorld();
            final Location location = player.getLocation();
            for (Map.Entry<Integer, ItemStack> exceededItem : exceededItems.entrySet()) {
                world.dropItem(location, exceededItem.getValue());
            }
        }
    }

    /**
     * Retorna uma instancia do OfflinePlayer correspondente ao nome passado (ignorecase)
     *
     * @param playerName O nome jogador a ser verificado
     * @return retorna <>offlinePlayer</> se o jogador exisitr, <>null</> caso contrário
     */
    public static OfflinePlayer getOfflinePlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return null;

        UUID offlineUUID = UUIDsController.getUUIDFromName(playerName);
        if (offlineUUID == null) return null;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offlineUUID);

        return offlinePlayer.getLastPlayed() != 0 ? offlinePlayer : null;
    }

    /**
     * Retorna a quantidade de dinheiro que um jogador tem
     *
     * @param player O jogador em questão
     * @return quantidade de dinheiro do jogador
     */
    @Deprecated
    public static double getBalance(Player player) {
        return getMoney(player);
    }

    @Deprecated
    public static double getMoney(Player player) {
        return VaultIntegration.ecoGet(player);
    }


    /**
     * Adiciona dinheiro a conta de um jogador
     *
     * @param player O jogador em questão
     * @param amount Quantia a ser adicionada
     */
    @Deprecated
    public static void ecoGive(OfflinePlayer player, double amount) {
        ecoGive(player, amount, false);
    }

    @Deprecated
    public static void ecoGive(OfflinePlayer player, double amount, boolean silent) {
        if (amount != 0) {
            VaultIntegration.ecoGive(player, amount);
            if (!silent) EverNifeCore.info("FCBukkitUtil Economy - [" + player.getName() + "]  ECO GIVE  " + amount);
        }
    }

    /**
     * Tira dinheiro de um jogador retorna o resultado da operação
     *
     * @param player O jogador em questão
     * @param amount Quantia a ser removida
     * @return retorna <>true</> se foi possivel remover o dinheiro do jogador e <>false</>
     * caso contrário
     */
    @Deprecated
    public static boolean ecoTake(OfflinePlayer player, double amount) {
        return ecoTake(player, amount, false);
    }

    @Deprecated
    public static boolean ecoTake(OfflinePlayer player, double amount, boolean silent) {
        boolean ecoTake = VaultIntegration.ecoTake(player, amount);
        if (ecoTake) {
            if (!silent) EverNifeCore.info("FCBukkitUtil Economy - [" + player.getName() + "]  ECO TAKE  " + amount);
        }
        return ecoTake;
    }

    /**
     * Verifica se um jogador possui uma determinada quantia de Dinheiro
     *
     * @param player O jogador em questão
     * @param amout  A quantia de money a ser verificada
     * @return retorna <>true</> se o jogador possuir o dinheiro e <>false</>
     * caso contrário
     */
    @Deprecated
    public static boolean ecoHasEnough(OfflinePlayer player, double amout) {
        return VaultIntegration.ecoHasEnough(player, amout);
    }

    @FCLocale(lang = LocaleType.EN_US, text = "§4§l ▶ §cOnly players can use this command!.")
    @FCLocale(lang = LocaleType.PT_BR, text = "§4§l ▶ §cApenas jogadores podem usar esse comando!.")
    private static LocaleMessage ONLY_PLAYERS_CAN_USE_THIS_COMMAND;
    /**
     * Verifica se um CommandSender é de fato um Player online!
     *
     * @param sender O jogador a ser verificado
     * @return retorna <>true</> se não é um jogador e <>false</>
     * caso contrário
     */
    public static boolean isNotPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            ONLY_PLAYERS_CAN_USE_THIS_COMMAND
                    .send(sender);
            return true;
        }
        return false;
    }

    @FCLocale(lang = LocaleType.EN_US, text = "§4§l ▶ §cYou do not have the permission §6[§e%permission%§6] §cto do that.")
    @FCLocale(lang = LocaleType.PT_BR, text = "§4§l ▶ §cVocê não tem a permissão §6[§e%permission%§6] §cpara fazer isto.")
    private static LocaleMessage YOU_DO_NOT_HAVE_PERMISSION;
    /**
     * Verifica se um dado jogador possui uma determinada permissão
     * e retorna true ou false, alem de notificar o jogador que ele precisa
     * dessa determina permissão.
     *
     * @param player     O jogador a ser verificado
     * @param permission A permissão a ser checada
     * @return retorna <>true</> se o jogador possui a permissão e <>false</>
     * caso contrário
     */
    public static boolean hasThePermission(CommandSender player, String permission) {

        if (!player.hasPermission(permission)) {
            YOU_DO_NOT_HAVE_PERMISSION
                    .addPlaceholder("%permission%",permission)
                    .send(player);
            return false;
        }
        return true;
    }

    /**
     * Pega os blocos em volta de uma determinada BukkitLocation
     *
     * @return List<Block> em volta do jogador
     */
    public static List<Block> getBlocksInRadius(Location location, int radius) {

        List<Block> blocks = new ArrayList<Block>();

        //Setando as bordas da região que sera "loopada"
        final int minX = location.getBlockX() - radius;
        final int minY = location.getBlockY() - radius;
        final int minZ = location.getBlockZ() - radius;
        final int maxX = location.getBlockX() + radius;
        final int maxY = location.getBlockY() + radius;
        final int maxZ = location.getBlockZ() + radius;

        for (int counterX = minX; counterX <= maxX; counterX++) {
            for (int counterY = minY; counterY <= maxY; counterY++) {
                for (int counterZ = minZ; counterZ <= maxZ; counterZ++) {
                    blocks.add(new Location(location.getWorld(), counterX, counterY, counterZ).getBlock());
                }
            }
        }

        return blocks;
    }

    /**
     * Retorna um numero aleatório entre 0 e 99
     */
    public static int randomPercentage() {
        return FCBukkitUtil.random.nextInt(100);
    }


    /**
     * Força o console a executar um comando!
     */
    public static void makeConsoleExecuteCommand(String theCommand) {
        if (Bukkit.getServer().isPrimaryThread()) {
            Bukkit.dispatchCommand(consoleSender, theCommand);
        } else {
            EverNifeCore.warning("Calling [makeConsoleExecuteCommand(\"" + theCommand + "\")] out of Main Thread... i am fixing it for you, but... you may do your job!");
            makeConsoleExecuteCommandFromAssyncThread(theCommand);
        }
    }

    /**
     * Força o console a executar um comando!
     */
    public static void makeConsoleExecuteCommand(String... theCommands) {
        if (Bukkit.getServer().isPrimaryThread()) {
            for (String theCommand : theCommands) {
                Bukkit.dispatchCommand(consoleSender, theCommand);
            }
        } else {
            EverNifeCore.warning("Calling [makeConsoleExecuteCommand(\"" + String.join("|", theCommands) + "\")] out of Main Thread... i am fixing it for you, but... you may do your job!");
            makeConsoleExecuteCommandFromAssyncThread(theCommands);
        }
    }

    /**
     * Força o console a executar um comando!
     */
    public static void makeConsoleExecuteCommandFromAssyncThread(String theCommand) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand(consoleSender, theCommand);
            }
        }.runTask(EverNifeCore.instance);
    }

    /**
     * Força o console a executar um comando!
     */
    public static void makeConsoleExecuteCommandFromAssyncThread(String... theCommands) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String theCommand : theCommands) {
                    Bukkit.dispatchCommand(consoleSender, theCommand);
                }
            }
        }.runTask(EverNifeCore.instance);
    }

    /**
     * Força o jogador a executar um comando!
     */
    public static void makePlayerExecuteCommand(CommandSender player, String theCommand) {
        Bukkit.dispatchCommand(player, theCommand);
    }

    /**
     * Transforma os argumentos inseridos em um Arraylist!
     */
    public static List<String> parseBukkitArgsToList(String[] args, int numOfArgs) {
        List<String> argumentos = new ArrayList<String>();
        for (int i = 0; i < numOfArgs; i++) {
            if (i < args.length)
                argumentos.add(args[i]);
            else
                argumentos.add("");
        }
        return argumentos;
    }

    /**
     * Verifica se um player esta segurando um determinado item!
     * Esse item deve bater com o itemTypeName dado (exemplo:  minecraft:chest)
     * Esse item deve bater com o itemDisplayName dado (exemplo: "Bau do milénio")
     * <p>
     * Caso o itemDisplayName for 'empty()' (uma string vazia) ele irá ignorar o DisplayName;
     * <p>
     * Retorna TRUE se o item for o esperado e FALSE caso contrário!
     * <p>
     * Note:  itemTypeName is CaseSensitive
     * itemDisplayName is not CaseSensitive (Ignoring Colors)
     */
    public static boolean playerIsHoldingTheItem(Player player, String itemTypeName) {
        return playerIsHoldingTheItem(player, itemTypeName, null);
    }

    public static boolean playerIsHoldingTheItem(Player player, String itemTypeName, @Nullable String itemDisplayName) {

        ItemStack itemStack = getPlayersHeldItem(player);
        if (itemStack != null) {
            if (itemStack.getType().name().equalsIgnoreCase(itemTypeName)) {

                if (itemDisplayName == null || itemDisplayName.isEmpty()) {
                    return true;
                }

                String displayName = FCItemUtils.getDisplayName(itemStack);
                return displayName != null && displayName.equalsIgnoreCase(itemDisplayName);
            }
        }
        return false;
    }

    /**
     * @param player      O nome do jogador a ser verificado
     * @param statisticID O id da estatistica a ser verificada
     */
    public static int forgeGetStat(Player player, String statisticID) {
        return StatisticUtil.getStatistic(player.getName(), statisticID);
    }

    /**
     * @param player O nome do jogador a ser verificado
     */
    public static Map<String, Integer> forgeGetAllStats(Player player) {
        return StatisticUtil.getAllStatistics(player.getName());
    }

    public static boolean removePlayersHeldItem(Player player, int amountToRemove) {
        ItemStack heldItem = getPlayersHeldItem(player);
        if (heldItem != null) {
            int amoutLeft = heldItem.getAmount() - amountToRemove;
            if (amoutLeft >= 0) {
                if (amoutLeft == 0) {
                    setPlayersHeldItem(player, null);
                } else {
                    heldItem.setAmount(amoutLeft);
                }
                return true;
            }
        }
        return false;
    }

    public static void setPlayersHeldItem(Player player, ItemStack itemStack) {
        if (MCVersion.isLegacy()) {
            player.setItemInHand(itemStack);
        } else {
            player.getInventory().setItemInMainHand(itemStack);
        }
    }

    public static ItemStack getPlayersHeldItem(Player player) {
        final ItemStack heldItem;

        if (MCVersion.isLegacy()) {
            heldItem = player.getItemInHand();
        } else {
            heldItem = player.getInventory().getItemInMainHand();
        }

        return heldItem != null && heldItem.getType() == Material.AIR ? null : heldItem;
    }

    public static void feedPlayer(Player player, int amount) {
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + amount));
    }

    public static Block getTargetBlock(Player player, int maxDistance) {
        if (MCVersion.isLegacy()) {
            final BlockIterator iterator = new BlockIterator(player.getLocation(), player.getEyeHeight(), maxDistance);
            Block result;
            while (iterator.hasNext()) {
                result = iterator.next();
                if (result.getType() != Material.AIR) {
                    return result;
                }
            }
        } else {
            return player.getTargetBlock(null, maxDistance);
        }
        return null;
    }

    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException var2) {
            return false;
        }
    }

    public static boolean isModLoaded(String modname){
        if (methodLoader_isLoaded == null) return false;
        return methodLoader_isLoaded.invoke(null, modname);
    }

    public static long getOntime(PlayerData playerData){
        return OntimeManager.getProvider().getOntime(playerData); //Ontime provider might be overridden by the OnTime plugin
    }

    //
    //This is meanted to be only by my private network plugins, don't use this on any public plugin!
    //
    public static String getPlayerStaffRank(Player player) {
        if (ServerType.isEverNifePersonalServer()){
            if (player.getName().equalsIgnoreCase("EverNife")) return "Dono";
            if (player.hasPermission("be.diretor")) return "Diretor";
            if (player.hasPermission("be.admin")) return "Admin";
            if (player.hasPermission("be.moderador")) return "Moderador";
            if (player.hasPermission("be.ajudante")) return "Ajudante";
            return "Jogador";
        }
        return "Player";
    }

    public static boolean isMainThread() {
        return Bukkit.getServer().isPrimaryThread();
    }

}
