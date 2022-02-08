package br.com.finalcraft.evernifecore.util.pageviwer;

import br.com.finalcraft.evernifecore.config.playerdata.PDSection;
import br.com.finalcraft.evernifecore.config.playerdata.PlayerData;
import br.com.finalcraft.evernifecore.fancytext.FancyText;
import br.com.finalcraft.evernifecore.time.FCTimeFrame;
import br.com.finalcraft.evernifecore.util.FCTextUtil;
import br.com.finalcraft.evernifecore.util.numberwrapper.NumberWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PageViewer<T,J> {

    protected final Supplier<List<T>> supplier;
    protected final Function<T, J> getValue;
    protected final Comparator<J> comparator;
    protected final List<FancyText> formatHeader;
    protected final FancyText formatLine;
    protected final List<FancyText> formatFooter;
    protected final long cooldown;
    protected final int lineStart;
    protected final int lineEnd;
    protected final int pageSize;
    protected final boolean includeDate;
    protected final boolean includeTotalPlayers;
    protected final HashMap<String, Function<T,Object>> placeholders;

    protected transient WeakReference<List<FancyText>> pageLinesCache = new WeakReference<>(new ArrayList<>());
    protected transient List<FancyText> pageHeaderCache = null;
    protected transient List<FancyText> pageFooterCache = null;
    protected transient long lastBuild = 0L;

    public PageViewer(Supplier<List<T>> supplier, Function<T, J> getValue, Comparator<J> comparator, List<FancyText> formatHeader, FancyText formatLine, List<FancyText> formatFooter, long cooldown, int lineStart, int lineEnd, int pageSize, boolean includeDate, boolean includeTotalPlayers) {
        this.supplier = supplier;
        this.getValue = getValue;
        this.comparator = comparator;
        this.formatHeader = formatHeader;
        this.formatLine = formatLine;
        this.formatFooter = formatFooter;
        this.cooldown = cooldown;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.pageSize = pageSize;
        this.includeDate = includeDate;
        this.includeTotalPlayers = includeTotalPlayers;
        this.placeholders = new HashMap<>();
    }

    public int getLineStart() {
        return lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    private void validateCachedLines(){

        if (pageLinesCache.get() == null || System.currentTimeMillis() - lastBuild >= cooldown){

            class SortedItem implements Comparable<SortedItem>{
                final T item;
                final J value;

                public SortedItem(T item, J value) {
                    this.item = item;
                    this.value = value;
                }

                @Override
                public int compareTo(@NotNull SortedItem o) {
                    return comparator.compare(this.value, o.value);
                }
            }

            pageHeaderCache = new ArrayList<>();
            pageLinesCache = new WeakReference<>(new ArrayList<>());
            pageFooterCache = new ArrayList<>();

            List<SortedItem> sortedList = new ArrayList<>();

            for (T item : supplier.get()) {
                J value = getValue.apply(item);
                sortedList.add(new SortedItem(item, value));
            }

            Collections.sort(sortedList);
            Collections.reverse(sortedList);

            if (sortedList.size() > 0){ //Add more default placeholders here, like "%player%" name
                SortedItem sortedItem = sortedList.get(0);
                if (sortedItem.item instanceof Player) placeholders.put("%player%", t -> ((Player)t).getName());
                else if (sortedItem.item instanceof PlayerData) placeholders.put("%player%", t -> ((PlayerData)t).getPlayerName());
                else if (sortedItem.item instanceof PDSection) placeholders.put("%player%", t -> ((PDSection)t).getPlayerName());
            }

            for (FancyText formatHeaderText : formatHeader) {
                final FancyText fancyText = formatHeaderText.clone();
                pageHeaderCache.add(fancyText);
            }

            if (includeDate){
                pageHeaderCache.add(new FancyText("§7Data de hoje: " + new FCTimeFrame(System.currentTimeMillis()).getFormatedNoHours()));
            }

            for (int number = lineStart; number < sortedList.size() && number < lineEnd; number++) {
                final FancyText fancyText = formatLine.clone();

                final SortedItem sortedItem = sortedList.get(number);
                placeholders.entrySet().forEach(entry -> fancyText.replace(entry.getKey(), String.valueOf(entry.getValue().apply(sortedItem.item))));
                fancyText.replace("%number%", String.valueOf(number + 1));

                pageLinesCache.get().add(fancyText);
            }

            for (FancyText formatFooterText : formatFooter) {
                final FancyText fancyText = formatFooterText.clone();
                pageFooterCache.add(fancyText);
            }

            if (includeTotalPlayers){
                pageHeaderCache.add(new FancyText("§7De um total de " + sortedList.size() + " jogadores..."));
            }

            lastBuild = System.currentTimeMillis();
        }
    }

    public void send(CommandSender... sender){
        send(0, pageSize, sender);
    }

    public void send(int page, CommandSender... sender){
        int start = NumberWrapper.of((page - 1) * pageSize).boundUpper(lineEnd - pageSize).intValue();
        int end = NumberWrapper.of(page * pageSize).boundUpper(lineEnd).intValue();
        send(start, end, sender);
    }

    public void send(int lineStart, int lineEnd, CommandSender... sender){
        validateCachedLines();

        //Bound lineEnd to lastLine
        lineEnd = NumberWrapper.of(lineEnd).boundUpper(pageLinesCache.get().size() -1).intValue();

        if (lineStart > lineEnd){
            //Rebound, one page backwards
            int lastPossiblePage = pageLinesCache.get().size() / pageSize;
            lineStart = NumberWrapper.of(lineStart).boundUpper(lastPossiblePage * pageSize).intValue();
        }

        lineStart = NumberWrapper.of(lineStart).boundLower(0).intValue();

        for (CommandSender commandSender : sender) {
            for (FancyText headerLine : pageHeaderCache) {
                headerLine.send(commandSender);
            }
            for (int i = lineStart; i < pageLinesCache.get().size() && i < lineEnd; i++) {
                pageLinesCache.get().get(i).send(sender);
            }
            for (FancyText headerLine : pageFooterCache) {
                headerLine.send(commandSender);
            }
        }
    }

    public static <T,J> Builder<T,J> builder(Supplier<List<T>> supplier, Function<T, J> getValue){
        return new Builder<>(supplier, getValue);
    }

    public static class Builder<T,J>{
        protected Supplier<List<T>> supplier;
        protected Function<T, J> getValue;

        private final Comparator<Number> doubleComparator = Comparator.comparingDouble(Number::doubleValue);
        private final Comparator<Object> stringComparator = Comparator.comparing(Object::toString);
        protected Comparator<J> comparator = (o1, o2) -> {
            if (o1 instanceof Number){
                return doubleComparator.compare((Number)o1,(Number)o2);
            }
            return stringComparator.compare(String.valueOf(o1),String.valueOf(o2));
        };
        protected List<FancyText> formatHeader = Arrays.asList(new FancyText("§a§m" + FCTextUtil.straightLineOf("-")));
        protected FancyText formatLine = new FancyText("§7#  %number%:   §e%player%§f - §a%value%");
        protected List<FancyText> formatFooter = Arrays.asList(new FancyText(""));
        protected long cooldown = 15000; //15 seconds
        protected int lineStart = 0;
        protected int lineEnd = 50;
        protected int pageSize = 10;
        protected boolean includeDate = false;
        protected boolean includeTotalPlayers = false;

        protected final HashMap<String, Function<T,Object>> placeholders = new HashMap<>();

        protected Builder(Supplier<List<T>> supplier, Function<T, J> getValue) {
            this.supplier = supplier;
            this.getValue = getValue;

            addPlaceholder("%value%", (Function<T, Object>) getValue);
        }

        public Builder<T,J> setComparator(Comparator<J> comparator) {
            this.comparator = comparator;
            return this;
        }

        public Builder<T,J> setFormatHeader(List<FancyText> formatHeader) {
            this.formatHeader = formatHeader;
            return this;
        }

        public Builder<T,J> setFormatHeader(FancyText... formatHeader) {
            this.formatHeader = Arrays.asList(formatHeader);
            return this;
        }

        public Builder<T,J> setFormatHeader(String... formatHeader) {
            this.formatHeader = Arrays.asList(formatHeader).stream().map(FancyText::new).collect(Collectors.toList());
            return this;
        }

        public Builder<T,J> setFormatLine(String formatLine) {
            this.formatLine = new FancyText(formatLine);
            return this;
        }

        public Builder<T,J> setFormatLine(FancyText formatLine) {
            this.formatLine = formatLine;
            return this;
        }

        public Builder<T,J> setFormatFooter(List<FancyText> formatFooter) {
            this.formatFooter = formatFooter;
            return this;
        }

        public Builder<T,J> setFormatFooter(FancyText... formatFooter) {
            this.formatFooter = Arrays.asList(formatFooter);
            return this;
        }

        public Builder<T,J> setFormatFooter(String... formatFooter) {
            this.formatFooter = Arrays.asList(formatFooter).stream().map(FancyText::new).collect(Collectors.toList());
            return this;
        }

        public Builder<T,J> setCooldown(int cooldown) {
            this.cooldown = cooldown * 1000;
            return this;
        }

        public Builder<T,J> setLineStart(int lineStart) {
            this.lineStart = lineStart;
            return this;
        }

        public Builder<T,J> setLineEnd(int lineEnd) {
            this.lineEnd = lineEnd < 0 ? Integer.MAX_VALUE : lineEnd;
            return this;
        }

        public Builder<T,J> setIncludeDate(boolean includeDate) {
            this.includeDate = includeDate;
            return this;
        }

        public Builder<T,J> setIncludeTotalPlayers(boolean includeTotalPlayers) {
            this.includeTotalPlayers = includeTotalPlayers;
            return this;
        }

        public Builder<T,J> setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<T,J> addPlaceholder(String placeholder, Function<T, Object> function){
            placeholders.put(placeholder, function);
            return this;
        }

        public PageViewer<T,J> build(){
            PageViewer<T,J> pageViewer = new PageViewer<>(
                    supplier,
                    getValue,
                    comparator,
                    formatHeader,
                    formatLine,
                    formatFooter,
                    cooldown,
                    lineStart,
                    lineEnd,
                    pageSize,
                    includeDate,
                    includeTotalPlayers);

            pageViewer.placeholders.putAll(this.placeholders);

            return pageViewer;
        }
    }

}