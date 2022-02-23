package io.github.screret.simpletech.capabilities;

import io.github.screret.simpletech.energy.storage.IEnergyContainer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class CapabilityEnergyContainer {
    public static final Capability<IEnergyContainer> ENERGY = CapabilityManager.get(new CapabilityToken<>(){});;

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IEnergyContainer.class);
    }
}
