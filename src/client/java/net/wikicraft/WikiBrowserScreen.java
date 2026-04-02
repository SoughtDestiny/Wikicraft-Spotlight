package net.wikicraft;

import net.dimaskama.mcef.api.MCEFApi;
import net.dimaskama.mcef.api.MCEFBrowser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import org.lwjgl.glfw.GLFW;

public class WikiBrowserScreen extends Screen {

    public static final String[] pinnedUrls   = new String[3];
    public static final String[] pinnedTitles = new String[3];
    public static String  lastUrl   = null;
    public static String  lastTitle = null;
    public static boolean stayOpen  = false;

    private static final int HEADER_H = 36;
    private static final int R        = 10;

    private final String initialUrl, initialTitle;
    private final Screen parent;
    private String currentUrl;
    private String currentTitle;

    private MCEFBrowser browser;
    private boolean jsInjected = false;
    private int     jsDelay    = 0;

    private int winX, winY, winW, winH;
    private int bx, by, bw, bh;
    private double scaleFactor = 1.0;

    private final Button[] slotBtns    = new Button[3];
    private final Button[] slotDelBtns = new Button[3];
    private int lastMouseX, lastMouseY;

    public WikiBrowserScreen(String url, String title, Screen parent) {
        super(Component.literal(title));
        this.initialUrl   = url;
        this.initialTitle = title;
        this.currentUrl   = url;
        this.currentTitle = title;
        this.parent       = parent;
    }

    @Override
    protected void init() {
        super.init();

        var mc = Minecraft.getInstance();
        scaleFactor = mc.getWindow().getGuiScale();

        winW = (int)(this.width * 0.82f);
        winH = winW * 9 / 16;
        if (winH > (int)(this.height * 0.90f)) {
            winH = (int)(this.height * 0.90f);
            winW = winH * 16 / 9;
        }
        winX = this.width  / 2 - winW / 2;
        winY = this.height / 2 - winH / 2;
        bx = winX + 2;
        by = winY + HEADER_H;
        bw = winW - 4;
        bh = winH - HEADER_H - 2;

        var backBtn = Button.builder(Component.literal(""), btn -> closeAndRestore())
                .bounds(winX + 4, winY + 7, 48, 22).build();
        backBtn.setAlpha(0f);
        this.addRenderableWidget(backBtn);

        var closeBtn = Button.builder(Component.literal(""), btn -> mc.setScreen(null))
                .bounds(winX + winW - 28, winY + 7, 22, 22).build();
        closeBtn.setAlpha(0f);
        this.addRenderableWidget(closeBtn);

        var stayBtn = Button.builder(Component.literal(""), btn -> { stayOpen = !stayOpen; rebuildWidgets(); })
                .bounds(winX + winW - 56, winY + 7, 26, 22).build();
        stayBtn.setAlpha(0f);
        this.addRenderableWidget(stayBtn);

        int slotAreaX = winX + 58;
        int slotAreaW = winW - 58 - 64;
        int slotW = (slotAreaW - 8) / 3;

        for (int i = 0; i < 3; i++) {
            final int slot = i;
            boolean has = pinnedUrls[i] != null;
            int btnX = slotAreaX + i * (slotW + 4);

            slotBtns[i] = Button.builder(Component.literal(""), btn -> handlePin(slot))
                    .bounds(btnX, winY + 8, has ? slotW - 16 : slotW, 20).build();
            slotBtns[i].setAlpha(0f);
            this.addRenderableWidget(slotBtns[i]);

            if (has) {
                slotDelBtns[i] = Button.builder(Component.literal(""), btn -> {
                    pinnedUrls[slot] = null;
                    pinnedTitles[slot] = null;
                    rebuildWidgets();
                }).bounds(btnX + slotW - 16, winY + 8, 16, 20).build();
                slotDelBtns[i].setAlpha(0f);
                this.addRenderableWidget(slotDelBtns[i]);
            }
        }

        createBrowserIfReady();
    }

    private boolean waitingForMcef = false;

    private void createBrowserIfReady() {
        if (browser != null) return;
        var api = MCEFApi.getInstance();
        if (api != null) {
            int nativeBW = (int)(bw * scaleFactor);
            int nativeBH = (int)(bh * scaleFactor);
            browser = api.createBrowser(initialUrl, false);
            browser.resize(nativeBW, nativeBH);
            browser.getCefBrowser().setZoomLevel(0.0);
            browser.setFocus(true);
            jsInjected = false;
            jsDelay    = 0;
            waitingForMcef = false;
        } else if (!waitingForMcef) {
            waitingForMcef = true;
            MCEFApi.getInstanceFuture().thenAccept(readyApi -> {
                Minecraft.getInstance().execute(this::createBrowserIfReady);
            });
        }
    }

    private void closeAndRestore() {
        Minecraft.getInstance().setScreen(parent);
    }

    private void injectJS() {
        if (browser == null || jsInjected) return;
        browser.getCefBrowser().executeJavaScript(
                "(function(){" +
                        "var css='::-webkit-scrollbar{width:0!important;height:0!important;display:none!important}';" +
                        "function apply(){if(!document.head||document.__sbDone)return;" +
                        "var s=document.createElement('style');s.textContent=css;" +
                        "document.head.appendChild(s);document.__sbDone=true;}" +
                        "apply();new MutationObserver(apply).observe(document.documentElement,{childList:true,subtree:true});" +
                        "})()", browser.getCefBrowser().getURL(), 0);
        jsInjected = true;
    }

    private void handlePin(int slot) {
        if (pinnedUrls[slot] != null) {
            currentUrl   = pinnedUrls[slot];
            currentTitle = pinnedTitles[slot] != null ? pinnedTitles[slot] : "Minecraft Wiki";
            if (browser != null) {
                browser.getCefBrowser().loadURL(pinnedUrls[slot]);
                jsInjected = false; jsDelay = 0;
            }
        } else {
            pinnedUrls[slot]   = currentUrl;
            pinnedTitles[slot] = currentTitle;
            rebuildWidgets();
        }
    }

    private SpotlightScreen.Theme theme() { return SpotlightScreen.currentTheme; }
    private int opaque(int col) { return (col & 0x00FFFFFF) | 0xF0000000; }
    private int headerBg() { return theme().inner; }
    private int btnBg(boolean hov) { var t = theme(); return hov ? t.pillHov : t.pill; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float dt) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        var t = theme();

        if (browser != null && browser.getTextureView() != null) {
            jsDelay++;
            if (jsDelay > 15 && !jsInjected) injectJS();
        }

        ctx.fill(0, 0, this.width, this.height, t.overlay);
        drawRR(ctx, winX, winY, winW, winH, R, opaque(t.bg));
        drawBorderRR(ctx, winX, winY, winW, winH, R, t.border);
        drawRR(ctx, winX+2, winY+2, winW-4, HEADER_H+R, R-2, headerBg());
        ctx.fill(winX + 2, winY + HEADER_H - 1, winX + winW - 2, winY + HEADER_H, t.sep);

        boolean backHov = mouseX >= winX+4 && mouseX < winX+52 && mouseY >= winY+7 && mouseY < winY+29;
        drawRR(ctx, winX+4, winY+7, 48, 22, 5, btnBg(backHov));
        drawBorderRR(ctx, winX+4, winY+7, 48, 22, 5, t.pillBd);
        ctx.text(this.font, "\u2190 Back", winX+10, winY+13, backHov ? t.text : t.sub, false);

        boolean closeHov = mouseX >= winX+winW-28 && mouseX < winX+winW-6 && mouseY >= winY+7 && mouseY < winY+29;
        int closeBg = closeHov ? t.delHov : t.pill;
        drawRR(ctx, winX+winW-28, winY+7, 22, 22, 5, closeBg);
        drawBorderRR(ctx, winX+winW-28, winY+7, 22, 22, 5, t.pillBd);
        int xw = this.font.width("X");
        ctx.text(this.font, "X", winX+winW-28+11-xw/2, winY+13,
                closeHov ? 0xFFFFFFFF : t.sub, false);

        int pinX = winX + winW - 56, pinY = winY + 7, pinW = 26, pinH = 22;
        boolean pinHov = mouseX >= pinX && mouseX < pinX + pinW && mouseY >= pinY && mouseY < pinY + pinH;
        int pinBg = stayOpen ? t.selected : btnBg(pinHov);
        drawRR(ctx, pinX, pinY, pinW, pinH, 5, pinBg);
        drawBorderRR(ctx, pinX, pinY, pinW, pinH, 5, stayOpen ? t.accent : t.pillBd);
        int pinCx = pinX + pinW / 2, pinCy = pinY + 5;
        int headCol = stayOpen ? t.accent : (pinHov ? t.text : t.sub);
        int shaftCol = stayOpen ? t.accentDim : (pinHov ? t.sub : t.pillBd);
        drawPinIcon(ctx, pinCx, pinCy, headCol, shaftCol);

        int slotAreaX = winX + 58;
        int slotAreaW = winW - 58 - 64;
        int slotW = (slotAreaW - 8) / 3;

        for (int i = 0; i < 3; i++) {
            boolean has = pinnedUrls[i] != null;
            int sx = slotAreaX + i * (slotW + 4);
            int mainW = has ? slotW - 16 : slotW;

            boolean hov = mouseX >= sx && mouseX < sx + mainW && mouseY >= winY+8 && mouseY < winY+28;
            int bg = has ? (hov ? t.pillHov : t.pillFil) : (hov ? t.pillHov : t.pill);
            drawRR(ctx, sx, winY+8, mainW, 20, 5, bg);
            drawBorderRR(ctx, sx, winY+8, mainW, 20, 5, t.pillBd);

            if (has) {
                int delX = sx + mainW + 1;
                boolean delHov = mouseX >= delX && mouseX < delX+14 && mouseY >= winY+8 && mouseY < winY+28;
                drawRR(ctx, delX, winY+8, 14, 20, 4, delHov ? t.delHov : t.pill);
                drawBorderRR(ctx, delX, winY+8, 14, 20, 4, t.pillBd);
            }
        }

        ctx.fill(bx, by, bx+bw, by+bh, 0xFF0C0C0C);

        if (browser != null && browser.getTextureView() != null) {
            var sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            ctx.blit(browser.getTextureView(), sampler,
                    bx, by, bx + bw, by + bh, 0f, 1f, 0f, 1f);
        } else {
            String msg;
            var init = MCEFApi.initialize();
            if (init.isDone()) {
                msg = "Loading...";
            } else {
                var stage = init.getStage();
                int pct = (int)(init.getPercentage() * 100);
                msg = switch (stage) {
                    case DOWNLOADING -> "Downloading browser... " + pct + "%";
                    case EXTRACTING  -> "Extracting... " + pct + "%";
                    case INSTALL     -> "Installing...";
                    case INITIALIZING -> "Initializing...";
                    default -> "Initializing...";
                };
            }
            ctx.text(this.font, msg,
                    bx+bw/2 - this.font.width(msg)/2,
                    by+bh/2 - 4, t.sub, false);
        }

        super.extractRenderState(ctx, mouseX, mouseY, dt);
        renderSlotLabels(ctx, mouseX, mouseY);
    }

    private void renderSlotLabels(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        var t = theme();
        int slotAreaX = winX + 58;
        int slotAreaW = winW - 58 - 64;
        int slotW = (slotAreaW - 8) / 3;

        for (int i = 0; i < 3; i++) {
            boolean has = pinnedUrls[i] != null;
            int sx = slotAreaX + i * (slotW + 4);
            int mainW = has ? slotW - 16 : slotW;

            if (has) {
                String label = pinnedTitles[i] != null ? pinnedTitles[i] : "?";
                int maxLabelW = mainW - 8;
                if (this.font.width(label) > maxLabelW) {
                    while (label.length() > 3 && this.font.width(label + "..") > maxLabelW)
                        label = label.substring(0, label.length() - 1);
                    label += "..";
                }
                ctx.text(this.font, label, sx + 4, winY + 14, t.text, false);

                int delX = sx + mainW + 1;
                boolean delHov = mouseX >= delX && mouseX < delX+14 && mouseY >= winY+8 && mouseY < winY+28;
                ctx.text(this.font, "x", delX + 4, winY + 14,
                        delHov ? 0xFFFFFFFF : t.del, false);
            } else {
                String num = String.valueOf(i + 1);
                int nw = this.font.width(num);
                ctx.text(this.font, num, sx + mainW/2 - nw/2, winY + 14, t.hint, false);
            }
        }
    }

    private void drawPinIcon(GuiGraphicsExtractor ctx, int cx, int cy, int headCol, int shaftCol) {
        ctx.fill(cx - 1, cy, cx + 2, cy + 1, headCol);
        ctx.fill(cx - 2, cy + 1, cx + 3, cy + 2, headCol);
        ctx.fill(cx - 2, cy + 2, cx + 3, cy + 3, headCol);
        ctx.fill(cx - 2, cy + 3, cx + 3, cy + 4, headCol);
        ctx.fill(cx - 1, cy + 4, cx + 2, cy + 5, headCol);
        ctx.fill(cx, cy + 5, cx + 1, cy + 10, shaftCol);
        ctx.fill(cx, cy + 10, cx + 1, cy + 12, shaftCol);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        if (super.mouseClicked(click, doubleClick)) return true;
        if (browser != null && inB(click.x(), click.y())) {
            double browserX = (click.x() - bx) * scaleFactor;
            double browserY = (click.y() - by) * scaleFactor;
            var adjusted = new MouseButtonEvent(browserX, browserY, click.buttonInfo());
            browser.onMouseClicked(adjusted, doubleClick);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        super.mouseReleased(click);
        if (browser != null && inB(click.x(), click.y())) {
            double browserX = (click.x() - bx) * scaleFactor;
            double browserY = (click.y() - by) * scaleFactor;
            var adjusted = new MouseButtonEvent(browserX, browserY, click.buttonInfo());
            browser.onMouseReleased(adjusted);
        }
        return true;
    }

    @Override
    public void mouseMoved(double mx, double my) {
        if (browser != null && inB(mx, my)) {
            int browserX = (int)((mx - bx) * scaleFactor);
            int browserY = (int)((my - by) * scaleFactor);
            browser.onMouseMoved(browserX, browserY);
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (browser != null && inB(mx, my)) {
            int browserX = (int)((mx - bx) * scaleFactor);
            int browserY = (int)((my - by) * scaleFactor);
            browser.onMouseScrolled(browserX, browserY, v);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE) { closeAndRestore(); return true; }
        if (browser != null) browser.onKeyPressed(keyEvent);
        return true;
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        if (browser != null) browser.onKeyReleased(keyEvent);
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent charEvent) {
        if (browser != null) {
            browser.onCharTyped(charEvent);
            return true;
        }
        return false;
    }

    @Override
    public void removed() {
        if (stayOpen) {
            lastUrl   = currentUrl;
            lastTitle = currentTitle;
        } else {
            lastUrl   = null;
            lastTitle = null;
        }
        if (browser != null) { browser.close(); browser = null; }
        super.removed();
    }

    private boolean inB(double mx, double my) {
        return mx >= bx && mx <= bx+bw && my >= by && my <= by+bh;
    }

    private void drawRR(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int col) {
        if (r <= 0 || h <= 0 || w <= 0) { ctx.fill(x, y, x+w, y+h, col); return; }
        r = Math.min(r, Math.min(w / 2, h / 2));
        if (h > 2 * r) ctx.fill(x, y + r, x + w, y + h - r, col);
        for (int i = 0; i < r; i++) {
            double a = Math.acos((r - i - 0.5) / r);
            int inset = r - (int)(Math.sin(a) * r);
            ctx.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            ctx.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
    }

    private void drawBorderRR(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int col) {
        if (r <= 0 || h <= 0 || w <= 0) return;
        r = Math.min(r, Math.min(w / 2, h / 2));
        ctx.fill(x + r, y, x + w - r, y + 1, col);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h, col);
        ctx.fill(x, y + r, x + 1, y + h - r, col);
        ctx.fill(x + w - 1, y + r, x + w, y + h - r, col);
        for (int i = 0; i < r; i++) {
            double a = Math.acos((r - i - 0.5) / r);
            int inset = r - (int)(Math.sin(a) * r);
            ctx.fill(x + inset, y + i, x + inset + 1, y + i + 1, col);
            ctx.fill(x + w - inset - 1, y + i, x + w - inset, y + i + 1, col);
            ctx.fill(x + inset, y + h - i - 1, x + inset + 1, y + h - i, col);
            ctx.fill(x + w - inset - 1, y + h - i - 1, x + w - inset, y + h - i, col);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
