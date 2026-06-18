package fuzs.fantasticwings.common.data;

import fuzs.fantasticwings.common.flight.apparatus.FlightApparatus;
import fuzs.fantasticwings.common.init.FlightApparatuses;
import fuzs.fantasticwings.common.init.ModRegistry;
import fuzs.fantasticwings.common.world.item.BottledWingsItem;
import fuzs.puzzleslib.common.api.data.v2.AbstractRecipeProvider;
import fuzs.puzzleslib.common.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.common.api.data.v2.recipes.TransformingRecipeOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ModRecipeProvider extends AbstractRecipeProvider {

    public ModRecipeProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addRecipes(RecipeOutput recipeOutput) {
        this.bottledWings(FlightApparatuses.ANGEL_FLIGHT_APPARATUS, Items.FEATHER);
        this.bottledWings(FlightApparatuses.PARROT_FLIGHT_APPARATUS, Items.DYE.red());
        this.bottledWings(FlightApparatuses.SLIME_FLIGHT_APPARATUS, Items.SLIME_BALL);
        this.bottledWings(FlightApparatuses.BLUE_BUTTERFLY_FLIGHT_APPARATUS, Items.DYE.blue());
        this.bottledWings(FlightApparatuses.MONARCH_BUTTERFLY_FLIGHT_APPARATUS, Items.DYE.orange());
        this.bottledWings(FlightApparatuses.FIRE_FLIGHT_APPARATUS, Items.BLAZE_POWDER);
        this.bottledWings(FlightApparatuses.BAT_FLIGHT_APPARATUS, Items.LEATHER);
        this.bottledWings(FlightApparatuses.FAIRY_FLIGHT_APPARATUS, Items.OXEYE_DAISY);
        this.bottledWings(FlightApparatuses.EVIL_FLIGHT_APPARATUS, Items.BONE);
        this.bottledWings(FlightApparatuses.DRAGON_FLIGHT_APPARATUS, Items.FIRE_CHARGE);
        this.bottledWings(FlightApparatuses.METALLIC_FLIGHT_APPARATUS, Items.IRON_INGOT);
    }

    public void bottledWings(ResourceKey<FlightApparatus> resourceKey, Item item) {
        Holder.Reference<FlightApparatus> holder = this.registries()
                .lookupOrThrow(FlightApparatus.REGISTRY_KEY)
                .getOrThrow(resourceKey);
        Identifier identifier = BuiltInRegistries.ITEM.getKey(ModRegistry.BOTTLED_WINGS_ITEM.value())
                .withSuffix("_" + resourceKey.identifier().getPath());
        this.shaped(RecipeCategory.TRANSPORTATION, ModRegistry.BOTTLED_WINGS_ITEM.value())
                .define('X', item)
                .define('$', Items.DRAGON_BREATH)
                .define('#', Items.PHANTOM_MEMBRANE)
                .define('@', Items.HONEYCOMB)
                .pattern("X@X")
                .pattern("@$@")
                .pattern("#@#")
                .unlockedBy(getHasName(item), this.has(item))
                .group(resourceKey.registry().getPath())
                .save(TransformingRecipeOutput.transformed(this.output, (Recipe<?> recipe) -> {
                    ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                    ItemStackTemplate result = shapedRecipe.result;
                    DataComponentPatch patch = DataComponentPatch.builder()
                            .set(DataComponents.CONSUMABLE, BottledWingsItem.createComponent(holder))
                            .build();
                    ItemStackTemplate template = new ItemStackTemplate(result.typeHolder(), result.count(), patch);
                    return new ShapedRecipe(shapedRecipe.commonInfo,
                            shapedRecipe.bookInfo,
                            shapedRecipe.pattern,
                            template);
                }), ResourceKey.create(Registries.RECIPE, identifier));
    }
}
