/*
 * Copyright 2017 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Pixel Wheels.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.agateau.ui.uibuilder;

import static com.agateau.translations.Translator.tr;

import com.agateau.ui.AnimatedImage;
import com.agateau.ui.DimensionParser;
import com.agateau.ui.anchor.Anchor;
import com.agateau.ui.anchor.AnchorGroup;
import com.agateau.ui.anchor.PositionRule;
import com.agateau.ui.animscript.AnimScript;
import com.agateau.ui.animscript.AnimScriptLoader;
import com.agateau.ui.menu.ButtonMenuItem;
import com.agateau.ui.menu.Menu;
import com.agateau.ui.menu.MenuItem;
import com.agateau.ui.menu.MenuScrollPane;
import com.agateau.utils.Assert;
import com.agateau.utils.FileUtils;
import com.agateau.utils.log.NLog;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.XmlReader;
import java.util.HashMap;
import java.util.Map;

public class UiBuilder {
    private static final String PREVIOUS_ACTOR_ID = "$prev";

    private final AnimScriptLoader mAnimScriptLoader = new AnimScriptLoader();
    private final DimensionParser mDimParser = new DimensionParser();
    private final ElementTreeTraversor mTraversor = new ElementTreeTraversor();

    private final Map<String, Actor> mActorForId = new HashMap<>();
    private final Map<String, MenuItem> mMenuItemForId = new HashMap<>();
    private final Map<String, ActorFactory> mActorFactories = new HashMap<>();
    private final Map<String, MenuItemFactory> mMenuItemFactories = new HashMap<>();
    private final TextureAtlas mAtlas;
    private final Skin mSkin;
    private Actor mLastAddedActor;
    private final Map<String, TextureAtlas> mAtlasMap = new HashMap<>();
    private Map<String, String> mConfigMap = new HashMap<>();

    public interface ActorFactory {
        Actor createActor(UiBuilder uiBuilder, XmlReader.Element element) throws SyntaxException;
    }

    public interface MenuItemFactory {
        MenuItem createMenuItem(Menu menu, XmlReader.Element element) throws SyntaxException;
    }

    private static final String[] ANCHOR_NAMES = {
        "topLeft",
        "topCenter",
        "topRight",
        "centerLeft",
        "center",
        "centerRight",
        "bottomLeft",
        "bottomCenter",
        "bottomRight"
    };
    private static final Anchor[] ANCHORS = {
        Anchor.TOP_LEFT,
        Anchor.TOP_CENTER,
        Anchor.TOP_RIGHT,
        Anchor.CENTER_LEFT,
        Anchor.CENTER,
        Anchor.CENTER_RIGHT,
        Anchor.BOTTOM_LEFT,
        Anchor.BOTTOM_CENTER,
        Anchor.BOTTOM_RIGHT
    };

    public static class SyntaxException extends Exception {
        public SyntaxException(String message) {
            super(message);
        }
    }

    public UiBuilder(TextureAtlas atlas, Skin skin) {
        mAtlas = atlas;
        mSkin = skin;

        initActorFactories();
        initMenuItemFactories();
    }

    private void initActorFactories() {
        mActorFactories.put(
                "Image",
                (uiBuilder, element) -> {
                    Image image = new Image();
                    TextureAtlas atlas1 = getAtlasForElement(element);
                    String attr = element.getAttribute("name", "");
                    if (!attr.isEmpty()) {
                        if (attr.endsWith(".9")) {
                            initImageFromNinePatchName(image, atlas1, attr);
                        } else {
                            boolean tiled = element.getBooleanAttribute("tiled", false);
                            initImageFromRegionName(image, atlas1, attr, tiled);
                        }
                    }
                    image.pack();
                    return image;
                });
        mActorFactories.put(
                "AnimatedImage",
                (uiBuilder, element) -> {
                    String name = element.getAttribute("name", null);
                    if (name == null) {
                        throw new UiBuilder.SyntaxException("Missing 'name' attribute");
                    }
                    float frameDuration = element.getFloatAttribute("frameDuration", 0.1f);
                    float startTime = element.getFloatAttribute("startTime", 0f);

                    Animation<TextureRegion> anim =
                            new Animation<>(frameDuration, uiBuilder.getAtlas().findRegions(name));
                    AnimatedImage image = new AnimatedImage(anim);
                    image.setStartTime(startTime);
                    return image;
                });
        mActorFactories.put(
                "ImageButton",
                (uiBuilder, element) -> {
                    String styleName = element.getAttribute("style", "default");
                    ImageButton.ImageButtonStyle style =
                            new ImageButton.ImageButtonStyle(
                                    mSkin.get(styleName, ImageButton.ImageButtonStyle.class));
                    String imageName = element.getAttribute("imageName", "");
                    if (!imageName.isEmpty()) {
                        style.imageUp = mSkin.getDrawable(imageName);
                    }
                    ImageButton button = new ImageButton(style);
                    String imageColor = element.getAttribute("imageColor", "");
                    if (!imageColor.isEmpty()) {
                        Color color = Color.valueOf(imageColor);
                        button.getImage().setColor(color);
                    }
                    return button;
                });
        mActorFactories.put(
                "TextButton",
                (uiBuilder, element) -> {
                    String styleName = element.getAttribute("style", "default");
                    String text = tr(processText(element.getText()));
                    return new TextButton(text, mSkin, styleName);
                });
        mActorFactories.put("Group", (uiBuilder, element) -> new Group());
        mActorFactories.put(
                "AnchorGroup",
                (uiBuilder, element) -> {
                    mDimParser.gridSize = mDimParser.parse(element.getAttribute("gridSize", "1"));
                    AnchorGroup group = new AnchorGroup();
                    group.setGridSize(mDimParser.gridSize);
                    return group;
                });
        mActorFactories.put(
                "Label",
                (uiBuilder, element) -> {
                    String styleName = element.getAttribute("style", "default");
                    String text = tr(processText(element.getText()));
                    Label label = new Label(text, mSkin, styleName);
                    int align = parseAlign(element);
                    if (align != -1) {
                        label.setAlignment(align);
                    }
                    return label;
                });
        mActorFactories.put(
                "ScrollPane",
                (uiBuilder, element) -> {
                    String styleName = element.getAttribute("style", "");
                    ScrollPane pane;
                    if (styleName.isEmpty()) {
                        pane = new ScrollPane(null);
                    } else {
                        pane = new ScrollPane(null, mSkin, styleName);
                    }
                    Actor child = doBuild(element, null);
                    if (child != null) {
                        pane.setActor(child);
                    }
                    return pane;
                });
        mActorFactories.put(
                "VerticalGroup",
                (uiBuilder, element) -> {
                    VerticalGroup group = new VerticalGroup();
                    group.space(element.getFloatAttribute("spacing", 0));
                    int align = parseAlign(element);
                    if (align != -1) {
                        group.align(align);
                    }
                    return group;
                });
        mActorFactories.put(
                "HorizontalGroup",
                (uiBuilder, element) -> {
                    HorizontalGroup group = new HorizontalGroup();
                    group.space(element.getFloatAttribute("spacing", 0));
                    return group;
                });
        mActorFactories.put(
                "CheckBox",
                (uiBuilder, element) -> {
                    String styleName = element.getAttribute("style", "default");
                    String text = tr(element.getText());
                    return new CheckBox(text, mSkin, styleName);
                });
        mActorFactories.put("Menu", (uiBuilder, element) -> createMenu(element));
        mActorFactories.put(
                "MenuScrollPane",
                (uiBuilder, element) -> {
                    Menu menu = createMenu(element);
                    return new MenuScrollPane(menu);
                });
        mActorFactories.put("Table", (uiBuilder, element) -> new Table(mSkin));
    }

    private void initMenuItemFactories() {
        mMenuItemFactories.put(
                "ButtonMenuItem",
                (menu, element) -> {
                    String label = element.getAttribute("label", null);
                    String text = tr(element.getAttribute("text", ""));
                    ButtonMenuItem item = new ButtonMenuItem(menu, text);
                    if (label == null) {
                        menu.addItem(item);
                    } else {
                        menu.addItemWithLabel(tr(label), item);
                    }
                    return item;
                });
        mMenuItemFactories.put(
                "LabelMenuItem",
                (menu, element) -> {
                    String text = tr(element.getAttribute("text"));
                    return menu.addLabel(text);
                });
    }

    public void defineVariable(String name) {
        mTraversor.defineVariable(name);
    }

    public Actor build(FileHandle handle) {
        return build(handle, null);
    }

    public Actor build(XmlReader.Element parentElement) {
        return build(parentElement, null);
    }

    public Actor build(FileHandle handle, Group parentActor) {
        XmlReader.Element element = FileUtils.parseXml(handle);
        assert (element != null);
        return build(element, parentActor);
    }

    public Actor build(XmlReader.Element parentElement, Group parentActor) {
        mActorForId.clear();
        mMenuItemForId.clear();
        try {
            return doBuild(parentElement, parentActor);
        } catch (SyntaxException e) {
            NLog.e("Parse error: " + e.getMessage());
            return null;
        }
    }

    public TextureAtlas getAtlas() {
        return mAtlas;
    }

    public Skin getSkin() {
        return mSkin;
    }

    public void addAtlas(String ui, TextureAtlas atlas) {
        mAtlasMap.put(ui, atlas);
    }

    private Actor doBuild(XmlReader.Element parentElement, Group parentActor)
            throws SyntaxException {
        final Actor[] root = {null};
        mTraversor.traverseElementTree(
                parentElement,
                element -> {
                    if (element.getName().equals("Config")) {
                        readConfig(element);
                        return;
                    }
                    Actor actor = createActorForElement(element);
                    if (actor == null) {
                        throw new SyntaxException("Failed to create actor for element: " + element);
                    }
                    if (actor instanceof Widget) {
                        applyWidgetProperties((Widget) actor, element);
                    }
                    applyActorProperties(actor, element, parentActor);
                    createActorActions(actor, element);
                    String id = element.getAttribute("id", null);
                    addActorToActorForId(id, actor);
                    if (actor instanceof Group
                            && !(actor instanceof ScrollPane)
                            && !(actor instanceof Menu)) {
                        doBuild(element, (Group) actor);
                    }
                    mLastAddedActor = actor;
                    if (root[0] == null) {
                        root[0] = actor;
                    }
                });
        return root[0];
    }

    private void readConfig(XmlReader.Element parent) throws SyntaxException {
        for (XmlReader.Element element : parent.getChildrenByName("ConfigItem")) {
            String id = element.getAttribute("id", "");
            if (id.equals("")) {
                throw new SyntaxException("Missing or empty 'id' attribute in ConfigItem");
            }
            String value = element.getText();
            mConfigMap.put(id, value);
        }
    }

    private void addActorToActorForId(String id, Actor actor) throws SyntaxException {
        if (id != null) {
            if (mActorForId.containsKey(id)) {
                throw new SyntaxException("Duplicate ids: " + id);
            }
            mActorForId.put(id, actor);
        }
    }

    public float getFloatConfigValue(String id) {
        String value = mConfigMap.get(id);
        if (value == null) {
            NLog.e("Unknown config id '%s'", id);
            return 0;
        }
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            NLog.e("Invalid float value for id '%s': '%s'", id, value);
            return 0;
        }
    }

    public String getStringConfigValue(String id) {
        String value = mConfigMap.get(id);
        if (value == null) {
            NLog.e("Unknown config id '%s'", id);
            return "";
        }
        return value;
    }

    public AnimScript getAnimScriptConfigValue(String id) throws AnimScriptLoader.SyntaxException {
        String definition = mConfigMap.get(id);
        return mAnimScriptLoader.load(definition, mDimParser);
    }

    public <T extends Actor> T getActor(String id) {
        Actor actor;
        if (id.equals(PREVIOUS_ACTOR_ID)) {
            actor = mLastAddedActor;
        } else {
            actor = mActorForId.get(id);
        }
        if (actor == null) {
            throw new RuntimeException("No actor with id '" + id + "'");
        }
        @SuppressWarnings("unchecked")
        T obj = (T) actor;
        return obj;
    }

    public <T extends MenuItem> T getMenuItem(String id) {
        MenuItem item = mMenuItemForId.get(id);
        if (item == null) {
            throw new RuntimeException("No menu item with id '" + id + "'");
        }
        @SuppressWarnings("unchecked")
        T obj = (T) item;
        return obj;
    }

    private Actor createActorForElement(XmlReader.Element element) throws SyntaxException {
        String name = element.getName();
        ActorFactory factory = mActorFactories.get(name);
        if (factory != null) {
            return factory.createActor(this, element);
        }
        throw new SyntaxException("Unknown UI element type: " + name);
    }

    private TextureAtlas getAtlasForElement(XmlReader.Element element) {
        String name = element.getAttribute("atlas", "");
        if (name.isEmpty()) {
            return mAtlas;
        }
        return mAtlasMap.get(name);
    }

    private void initImageFromNinePatchName(Image image, TextureAtlas atlas, String name) {
        NinePatch patch = atlas.createPatch(name.substring(0, name.length() - 2));
        image.setDrawable(new NinePatchDrawable(patch));
    }

    private void initImageFromRegionName(
            Image image, TextureAtlas atlas, String name, boolean tiled) {
        TextureRegion region = atlas.findRegion(name);
        Assert.check(region != null, "No region named " + name);
        Drawable drawable;
        if (tiled) {
            drawable = new TiledDrawable(region);
        } else {
            drawable = new TextureRegionDrawable(region);
        }
        image.setDrawable(drawable);
        if (image.getWidth() == 0) {
            image.setWidth(region.getRegionWidth());
        }
        if (image.getHeight() == 0) {
            image.setHeight(region.getRegionHeight());
        }
    }

    private Menu createMenu(XmlReader.Element element) throws SyntaxException {
        String styleName = element.getAttribute("style", "default");
        Menu menu = new Menu(mSkin, styleName);
        float width = element.getIntAttribute("labelColumnWidth", 0);
        if (width > 0) {
            menu.setLabelColumnWidth(width);
        }
        XmlReader.Element items = element.getChildByName("Items");
        if (items != null) {
            mTraversor.traverseElementTree(
                    items,
                    itemElement -> {
                        String name = itemElement.getName();
                        MenuItemFactory factory = mMenuItemFactories.get(name);
                        if (factory == null) {
                            throw new SyntaxException("Invalid menu item type: " + name);
                        }
                        String id = itemElement.getAttribute("id", null);
                        MenuItem menuItem = factory.createMenuItem(menu, itemElement);
                        Actor actor = menuItem.getActor();
                        if (id != null) {
                            mMenuItemForId.put(id, menuItem);
                            addActorToActorForId(id, actor);
                        }
                    });
        }
        return menu;
    }

    private void applyWidgetProperties(Widget widget, XmlReader.Element element) {
        widget.setFillParent(element.getBooleanAttribute("fillParent", false));
    }

    private void applyActorProperties(Actor actor, XmlReader.Element element, Group parentActor)
            throws SyntaxException {
        AnchorGroup anchorGroup = null;
        if (parentActor != null) {
            parentActor.addActor(actor);
            if (parentActor instanceof AnchorGroup) {
                anchorGroup = (AnchorGroup) parentActor;
            }
        }
        String attr = element.getAttribute("x", "");
        if (!attr.isEmpty()) {
            actor.setX(mDimParser.parse(attr));
        }
        attr = element.getAttribute("y", "");
        if (!attr.isEmpty()) {
            actor.setY(mDimParser.parse(attr));
        }
        attr = element.getAttribute("width", "");
        if (!attr.isEmpty()) {
            actor.setWidth(mDimParser.parse(attr));
        }
        attr = element.getAttribute("height", "");
        if (!attr.isEmpty()) {
            actor.setHeight(mDimParser.parse(attr));
        }
        attr = element.getAttribute("originX", "");
        if (!attr.isEmpty()) {
            actor.setOriginX(mDimParser.parse(attr));
        }
        attr = element.getAttribute("originY", "");
        if (!attr.isEmpty()) {
            actor.setOriginY(mDimParser.parse(attr));
        }
        attr = element.getAttribute("visible", "");
        if (!attr.isEmpty()) {
            actor.setVisible(Boolean.parseBoolean(attr));
        }
        attr = element.getAttribute("color", "");
        if (!attr.isEmpty()) {
            actor.setColor(Color.valueOf(attr));
        }
        attr = element.getAttribute("debug", "");
        if (!attr.isEmpty()) {
            if (actor instanceof Group) {
                Group group = (Group) actor;
                attr = attr.toLowerCase();
                if (attr.equals("true")) {
                    group.debug();
                } else if (attr.equals("all")) {
                    group.debugAll();
                }
            } else {
                actor.setDebug(Boolean.parseBoolean(attr));
            }
        }
        for (int idx = 0, size = ANCHOR_NAMES.length; idx < size; ++idx) {
            String anchorName = ANCHOR_NAMES[idx];
            attr = element.getAttribute(anchorName, "");
            if (!attr.isEmpty()) {
                if (anchorGroup == null) {
                    throw new SyntaxException("Parent of " + actor + " is not an anchor group");
                }
                PositionRule rule = parseRule(attr);
                rule.target = actor;
                rule.targetAnchor = ANCHORS[idx];
                anchorGroup.addRule(rule);
            }
        }
    }

    /**
     * Parse a string of the form "$actorId $anchorName [$xOffset $yOffset]"
     *
     * @param txt the string to parse
     * @return a PositionRule
     */
    private PositionRule parseRule(String txt) throws SyntaxException {
        PositionRule rule = new PositionRule();
        String[] tokens = txt.split(" +");
        if (tokens.length != 1 && tokens.length != 3) {
            throw new SyntaxException("Invalid rule syntax: " + txt);
        }
        String[] tokens2 = tokens[0].split("\\.");
        if (tokens2.length != 2) {
            throw new SyntaxException("reference should be of the form <id>.<anchor>: " + txt);
        }
        rule.reference = getActor(tokens2[0]);
        for (int idx = 0, size = ANCHOR_NAMES.length; idx < size; ++idx) {
            if (tokens2[1].equals(ANCHOR_NAMES[idx])) {
                rule.referenceAnchor = ANCHORS[idx];
                break;
            }
        }
        if (rule.referenceAnchor == null) {
            throw new SyntaxException("Invalid anchor name: '" + tokens[1] + "'");
        }
        if (tokens.length == 3) {
            rule.hSpace = mDimParser.parse(tokens[1], DimensionParser.Unit.GRID);
            rule.vSpace = mDimParser.parse(tokens[2], DimensionParser.Unit.GRID);
        }
        return rule;
    }

    public void registerActorFactory(String name, ActorFactory factory) {
        mActorFactories.put(name, factory);
    }

    private static String processText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\n", "\n");
    }

    private void createActorActions(Actor actor, XmlReader.Element element) {
        for (XmlReader.Element child : element.getChildrenByName("Action")) {
            String definition = child.getText();
            AnimScript script;
            try {
                script = mAnimScriptLoader.load(definition, mDimParser);
            } catch (AnimScriptLoader.SyntaxException e) {
                NLog.e("Failed to parse:\n" + definition + "\n\n%s", e);
                continue;
            }
            actor.addAction(script.createAction());
        }
    }

    private static int parseAlign(XmlReader.Element element) throws SyntaxException {
        String alignText = element.getAttribute("align", "");
        if (alignText.isEmpty()) {
            return -1;
        }
        switch (alignText) {
            case "center":
                return Align.center;
            case "centerLeft":
                return Align.left;
            case "centerRight":
                return Align.right;
            case "topLeft":
                return Align.topLeft;
            case "topCenter":
                return Align.top;
            case "topRight":
                return Align.topRight;
            case "bottomLeft":
                return Align.bottomLeft;
            case "bottomCenter":
                return Align.bottom;
            case "bottomRight":
                return Align.bottomRight;
            default:
                throw new SyntaxException("Unknown value of 'align': " + alignText);
        }
    }
}
