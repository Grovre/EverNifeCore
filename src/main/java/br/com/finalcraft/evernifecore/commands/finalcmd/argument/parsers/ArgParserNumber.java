package br.com.finalcraft.evernifecore.commands.finalcmd.argument.parsers;

import br.com.finalcraft.evernifecore.argumento.Argumento;
import br.com.finalcraft.evernifecore.commands.finalcmd.argument.ArgInfo;
import br.com.finalcraft.evernifecore.commands.finalcmd.argument.ArgParser;
import br.com.finalcraft.evernifecore.commands.finalcmd.argument.exception.ArgParseException;
import br.com.finalcraft.evernifecore.commands.finalcmd.argument.parsers.util.ArgsParserUtil;
import br.com.finalcraft.evernifecore.util.FCMathUtil;
import br.com.finalcraft.evernifecore.util.FCMessageUtil;
import br.com.finalcraft.evernifecore.util.commons.Tuple;
import br.com.finalcraft.evernifecore.util.numberwrapper.NumberWrapper;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class ArgParserNumber extends ArgParser<Number> {

    private @Nullable Tuple<Double, Double> boundaries = null; //NotNull if the argument is bounded to two values
    private @Nullable List<Double> possibilities = null; //NotNull if the argument must be one of the list
    private final boolean isInteger;

    public ArgParserNumber(ArgInfo argInfo) {
        super(argInfo);

        isInteger = argInfo.getArgumentType().equals(Integer.class);

        String context = argInfo.getArgData().context();

        if (!context.isEmpty()){
            if (context.contains(":")){
                this.boundaries = ArgsParserUtil.parseNumericContextInterval(context);
            }else if (context.contains("|")){
                this.possibilities = ArgsParserUtil.parseNumericContextSelectional(context);
            }
        }
    }

    @Override
    public Number parserArgument(@NotNull CommandSender sender, @NotNull Argumento argumento) throws ArgParseException {
        final Number number;
        //We cannot use a ternary operator here because of NPE caused by boxingAndUnboxing of values.
        if (isInteger) {
            number = argumento.getInteger();
        }else {
            number = argumento.getDouble();
        }

        if (number == null){
            if (this.argInfo.isRequired()){
                if (isInteger) {
                    FCMessageUtil.needsToBeInteger(sender, argumento.toString());
                }else {
                    FCMessageUtil.needsToBeDouble(sender, argumento.toString());
                }
                throw new ArgParseException();
            }
            return null;
        }

        if (boundaries != null){
            NumberWrapper value = NumberWrapper.of(number);

            if (!value.isBoundedLower(boundaries.getAlfa())){
                FCMessageUtil.notBoundedLower(sender, value.get(), boundaries.getAlfa());
                throw new ArgParseException();
            }

            if (!value.isBoundedUpper(boundaries.getBeta())){
                FCMessageUtil.notBoundedUpper(sender, value.get(), boundaries.getBeta());
                throw new ArgParseException();
            }

            return number;
        }

        if (possibilities != null){

            for (Double possibility : possibilities) {
                if (possibility.equals(number)){
                    return number;
                }
            }

            FCMessageUtil.notWithinPossibilities(sender, NumberWrapper.of(number).toString(), possibilities);
            throw new ArgParseException();
        }

        return number;
    }

    @Override
    public @NotNull List<String> tabComplete(Context context) {
        if (possibilities != null){
            return possibilities.stream().map(aDouble -> FCMathUtil.toString(aDouble)).filter(s -> StringUtil.startsWithIgnoreCase(s, context.getLastWord())).collect(Collectors.toList());
        }

        return ImmutableList.of();
    }
}