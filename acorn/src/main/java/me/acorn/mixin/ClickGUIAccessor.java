package me.acorn.mixin;

import me.melinoe.clickgui.Panel;
import me.melinoe.clickgui.ClickGUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.ArrayList;

@Mixin(value = ClickGUI.class, remap = false)
public interface ClickGUIAccessor {
    @Accessor(value = "panels", remap = false)
    ArrayList<Panel> getPanels();
}
