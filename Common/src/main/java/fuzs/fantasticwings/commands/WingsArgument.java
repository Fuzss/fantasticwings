package fuzs.fantasticwings.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fuzs.fantasticwings.flight.apparatus.FlightApparatusImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;

public class WingsArgument extends StringRepresentableArgument<FlightApparatusImpl> {

    private WingsArgument() {
        super(FlightApparatusImpl.CODEC, FlightApparatusImpl::values);
    }

    public static WingsArgument wings() {
        return new WingsArgument();
    }

    public static FlightApparatusImpl getWings(CommandContext<CommandSourceStack> ctx, String value) throws CommandSyntaxException {
        return ctx.getArgument(value, FlightApparatusImpl.class);
    }
}
