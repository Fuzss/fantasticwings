package fuzs.fantasticwings.data.client;

import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.client.init.ClientModRegistry;
import fuzs.fantasticwings.commands.WingsCommand;
import fuzs.fantasticwings.flight.apparatus.FlightApparatusImpl;
import fuzs.fantasticwings.init.ModRegistry;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.world.item.alchemy.Potion;

import java.util.Objects;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.addCreativeModeTab(FantasticWings.MOD_ID, FantasticWings.MOD_NAME);
        builder.add(ClientModRegistry.FLY_KEY_MAPPING.getCategory(), FantasticWings.MOD_NAME);
        builder.add(ClientModRegistry.FLY_KEY_MAPPING, "Toggle Flight");
        builder.add(WingsCommand.KEY_GIVE_WINGS_SINGLE, "Applied wings to %s");
        builder.add(WingsCommand.KEY_GIVE_WINGS_MULTIPLE, "Applied wings to %s targets");
        builder.add(WingsCommand.KEY_TAKE_WINGS_SINGLE, "Removed wings from %s");
        builder.add(WingsCommand.KEY_TAKE_WINGS_MULTIPLE, "Removed wings from %s targets");
        builder.add(WingsCommand.COMPONENT_GIVE_WINGS_FAILED, "Unable to apply wings to target");
        builder.add(WingsCommand.COMPONENT_TAKE_WINGS_FAILED, "Target doesn't have wings to remove");
        builder.add(ModRegistry.GROW_WINGS_MOB_EFFECT.value(), "Grow Wings");
        builder.add(ModRegistry.SHED_WINGS_MOB_EFFECT.value(), "Shed Wings");
        add(builder, ModRegistry.BAT_BLOOD_POTION.value(), "Bat Blood");
        add(builder, FlightApparatusImpl.ANGEL.getPotion(), "Angel Wings");
        add(builder, FlightApparatusImpl.BAT.getPotion(), "Bat Wings");
        add(builder, FlightApparatusImpl.BLUE_BUTTERFLY.getPotion(), "Blue Butterfly Wings");
        add(builder, FlightApparatusImpl.DRAGON.getPotion(), "Dragon Wings");
        add(builder, FlightApparatusImpl.EVIL.getPotion(), "Evil Wings");
        add(builder, FlightApparatusImpl.FAIRY.getPotion(), "Fairy Wings");
        add(builder, FlightApparatusImpl.FIRE.getPotion(), "Fire Wings");
        add(builder, FlightApparatusImpl.MONARCH_BUTTERFLY.getPotion(), "Monarch Butterfly Wings");
        add(builder, FlightApparatusImpl.PARROT.getPotion(), "Parrot Wings");
        add(builder, FlightApparatusImpl.SLIME.getPotion(), "Slime Wings");
        add(builder, FlightApparatusImpl.METALLIC.getPotion(), "Metallic Wings");
        builder.add(ModRegistry.ITEM_ARMOR_EQUIP_WINGS.value(), "Wings rustle");
    }

    private static void add(TranslationBuilder builder, Potion potion, String value) {
        Objects.requireNonNull(potion, "potion is null");
        String potionName = potion.getName("");
        builder.add("item.minecraft.tipped_arrow.effect." + potionName, "Arrow of " + value);
        builder.add("item.minecraft.potion.effect." + potionName, value + " Bottle");
        builder.add("item.minecraft.splash_potion.effect." + potionName, "Splash " + value + " Bottle");
        builder.add("item.minecraft.lingering_potion.effect." + potionName, "Lingering " + value + " Bottle");
    }
}
