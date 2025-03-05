// Created by Eric B. 16.02.2021 20:48
package de.ericzones.bungeesystem.collectives.object;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;

public class TextBuilder {

    private String text;
    private TextComponent hoverText;
    private ClickEvent clickEvent;

    private TextComponent preText;
    private TextComponent postText;

    public TextBuilder(String text) {
        this.text = text;
    }

    public TextBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public TextBuilder setPreText(String preText) {
        if(preText != null)
            this.preText = Component.text(preText);
        else
            this.preText = null;
        return this;
    }

    public TextBuilder setPostText(String postText) {
        this.postText = Component.text(postText);
        return this;
    }

    public TextBuilder setHoverText(String hoverText) {
        this.hoverText = Component.text(hoverText);
        return this;
    }

    public TextBuilder setClickEvent(Action action, String value) {
        switch (action) {
            case COMMAND:
                this.clickEvent = ClickEvent.runCommand("/"+value);
                break;
            case COPYTEXT:
                this.clickEvent = ClickEvent.copyToClipboard(value);
                break;
            case OPENLINK:
                this.clickEvent = ClickEvent.openUrl(value);
                break;
            case TYPEDTEXT:
                this.clickEvent = ClickEvent.suggestCommand("/"+value);
                break;
        }
        return this;
    }

    public TextComponent build() {
        TextComponent textComponent = Component.text(this.text);
        if(this.hoverText != null)
            textComponent = textComponent.hoverEvent(this.hoverText.asHoverEvent());
        if(this.clickEvent != null)
            textComponent = textComponent.clickEvent(this.clickEvent);
        if(this.postText != null)
            textComponent = textComponent.append(this.postText);
        if(this.preText != null)
            return this.preText.append(textComponent);
        return textComponent;
    }

    public enum Action {

        COMMAND,
        COPYTEXT,
        OPENLINK,
        TYPEDTEXT;

        private Action(){}
    }

}
