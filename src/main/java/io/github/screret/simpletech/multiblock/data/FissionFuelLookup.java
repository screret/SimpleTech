package io.github.screret.simpletech.multiblock.data;

import io.github.screret.simpletech.recipes.RecipeCacheInvalidator;
import io.github.screret.simpletech.recipes.power.FissionFuel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class handling a recipe cache for fuel recipes, since any given entity type has one recipe
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FissionFuelLookup {
    private static final Map<Fluid, FissionFuel> LOOKUP = new HashMap<>();
    /** Listener to check when recipes reload */
    private static final RecipeCacheInvalidator.DuelSidedListener LISTENER = RecipeCacheInvalidator.addDuelSidedListener(LOOKUP::clear);

    /**
     * Adds a melting fuel to the lookup
     * @param fluid  Fluid
     * @param fuel   Fuel
     */
    public static void addFuel(Fluid fluid, FissionFuel fuel) {
        LISTENER.checkClear();
        LOOKUP.putIfAbsent(fluid, fuel);
    }

    /** Checks if the given fluid is a fuel */
    public static boolean isFuel(Fluid fluid) {
        return LOOKUP.containsKey(fluid);
    }

    /**
     * Gets the recipe for the given fluid
     * @param fluid   Fluid found
     * @return  Recipe, or null if no recipe for this type
     */
    @Nullable
    public static FissionFuel findFuel(Fluid fluid) {
        return LOOKUP.get(fluid);
    }
}
