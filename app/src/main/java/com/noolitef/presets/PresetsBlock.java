package com.noolitef.presets;

public class PresetsBlock {
    Preset topPreset;
    Preset bottomPreset;

    public PresetsBlock(Preset topPreset, Preset bottomPreset) {
        this.topPreset = topPreset;
        this.bottomPreset = bottomPreset;
    }

    public Preset getTopPreset() {
        return topPreset;
    }

    public void setTopPreset(Preset topPreset) {
        this.topPreset = topPreset;
    }

    public Preset getBottomPreset() {
        return bottomPreset;
    }

    public void setBottomPreset(Preset bottomPreset) {
        this.bottomPreset = bottomPreset;
    }
}
