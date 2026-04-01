package dev.tr7zw.exordium.config;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public abstract class CustomConfigScreen extends OptionsSubScreen {

    public CustomConfigScreen(Screen lastScreen, String titleKey) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable(titleKey));
    }

    @Override
    protected void addOptions() {
        initialize();
    }

    @Override
    protected void addFooter() {
        LinearLayout row = LinearLayout.horizontal().spacing(8);
        Button resetButton = Button.builder(Component.translatable("controls.reset"), (button) -> {
            reset();
        }).width(98).build();
        resetButton.active = !isDefault();
        row.addChild(resetButton);
        row.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            save();
            this.minecraft.setScreen(this.lastScreen);
        }).width(98).build());
        this.layout.addToFooter(row);
    }

    /**
     * Returns true if current settings match defaults.
     * Subclasses should override this to provide the actual comparison.
     */
    protected boolean isDefault() {
        return false;
    }

    @Override
    public void removed() {
        save();
    }

    public abstract void initialize();

    public abstract void save();

    public abstract void reset();

    protected void addSettings(List<OptionInstance<?>> options) {
        if (this.list != null) {
            this.list.addSmall(options.toArray(new OptionInstance[0]));
        }
    }

    protected OptionInstance<Integer> getIntOption(String key, int min, int max, Supplier<Integer> getter,
            Consumer<Integer> setter) {
        return new OptionInstance<>(key, OptionInstance.noTooltip(), (component, value) ->
                Options.genericValueLabel(component, Component.literal(String.valueOf(value))),
                new OptionInstance.IntRange(min, max), getter.get(), setter::accept);
    }

    protected OptionInstance<Boolean> getOnOffOption(String key, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return OptionInstance.createBoolean(key, getter.get(), setter::accept);
    }
}
