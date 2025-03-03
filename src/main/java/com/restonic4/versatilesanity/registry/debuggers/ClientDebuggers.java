package com.restonic4.versatilesanity.registry.debuggers;

import com.chaotic_loom.under_control.api.registry.UnderControlRegistries;
import com.chaotic_loom.under_control.api.registry.UnderControlRegistry;
import com.chaotic_loom.under_control.core.annotations.ExecutionSide;
import com.chaotic_loom.under_control.core.annotations.Registration;
import com.chaotic_loom.under_control.registries.core.ObjectIdentifier;
import com.restonic4.versatilesanity.VersatileSanity;

@Registration(side = ExecutionSide.CLIENT)
public class ClientDebuggers {
    public static OceanDebugger OCEAN;

    public static void register() {
        OCEAN = (OceanDebugger) UnderControlRegistry.register(
                UnderControlRegistries.DEBUGGER,
                new ObjectIdentifier(VersatileSanity.MOD_ID, "ocean"),
                new OceanDebugger(ExecutionSide.CLIENT)
        );
    }
}
