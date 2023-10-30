package com.github.qpcrummer.gui;

import imgui.ImGui;

public class GuiUtils {
    /**
     * Sets the font size. Make sure you run clearFontSize afterward!
     * @param fontSize float font size (default 1.0f)
     */
    public static void setFont(final float fontSize) {
        ImGui.getFont().setScale(fontSize);
        ImGui.pushFont(ImGui.getFont());
    }

    /**
     * Resets the font size and clears older font changes
     */
    public static void clearFontSize() {
        ImGui.getFont().setScale(1.0f);
        ImGui.popFont();
    }
}
