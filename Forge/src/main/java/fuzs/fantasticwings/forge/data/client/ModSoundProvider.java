package fuzs.fantasticwings.forge.data.client;

import fuzs.fantasticwings.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.client.AbstractSoundDefinitionProvider;
import fuzs.puzzleslib.api.data.v2.core.ForgeDataProviderContext;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.SoundDefinition;

public class ModSoundProvider extends AbstractSoundDefinitionProvider {

    public ModSoundProvider(ForgeDataProviderContext context) {
        super(context);
    }

    @Override
    public void registerSounds() {
        this.add(ModRegistry.ITEM_ARMOR_EQUIP_WINGS.value(),
                "item/armor/equip_leather1",
                "item/armor/equip_leather2",
                "item/armor/equip_leather3",
                "item/armor/equip_leather4",
                "item/armor/equip_leather5",
                "item/armor/equip_leather6");
        SoundDefinition soundDefinition = definition().with(sound("item/elytra/elytra_loop").volume(0.6));
        this.add(ModRegistry.ITEM_WINGS_FLYING.value(), soundDefinition);
        soundDefinition.subtitle(null);
    }

    protected void add(SoundEvent soundEvent, String... sounds) {
        SoundDefinition definition = definition();
        for (String sound : sounds) {
            definition.with(sound(sound));
        }
        this.add(soundEvent, definition);
    }
}
