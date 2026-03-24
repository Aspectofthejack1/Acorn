package me.acorn.mixin;

import me.acorn.Acorn;
import me.melinoe.clickgui.ClickGUI;
import me.melinoe.clickgui.Panel;
import me.melinoe.features.Category;
import me.melinoe.features.impl.ClickGUIModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(value = ClickGUI.class, remap = false)
public class ClickGUIMixin {

    @Inject(method = "init", at = @At("HEAD"), remap = false)
    private void onInit(CallbackInfo ci) {
        ArrayList<Panel> panels = ((ClickGUIAccessor)(Object) ClickGUI.INSTANCE).getPanels();

        Category acornCategory = Acorn.INSTANCE.getCATEGORY();
        String categoryName = acornCategory.getName();

        // If panelSetting already has Acorn AND panels has more than the base 4,
        // the panel was already added on a previous GUI open — don't add again.
        if (ClickGUIModule.INSTANCE.getPanelSetting().containsKey(categoryName)
                && panels.size() > 4) {
            return;
        }

        // Ensure panelSetting has an entry for Acorn so Panel doesn't throw
        ClickGUIModule.INSTANCE.getPanelSetting().putIfAbsent(
            categoryName,
            new ClickGUIModule.PanelData(10f + 260f * panels.size(), 10f, true)
        );

        panels.add(new Panel(acornCategory));
    }
}
