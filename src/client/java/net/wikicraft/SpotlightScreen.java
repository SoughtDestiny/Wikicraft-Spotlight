package net.wikicraft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SpotlightScreen extends Screen {

    public enum Theme {
        STONE("Stone",
            0xFF4A4A4E, 0xFF686868, 0xFF2A2A2E, 0xFF585858,
            0xFFEEEEEE, 0xFFAAAAAA, 0xFFBBBBBB,
            0xFF55CCFF, 0xFF3388BB,
            0xFF3A3A3E, 0xFF343438, 0xFF505054,
            0xFF323236, 0xFF3E3E42, 0xFF343438, 0xFF585858, 0xFF2E2E32,
            0xFFAAAAAA, 0xFFFF5555, 0x50000000),
        OBSIDIAN("Obsidian",
            0xFF0A0A0E, 0xFF1A1A24, 0xFF050508, 0xFF1A1A24,
            0xFFD0D0DD, 0xFF7878A0, 0xFF505068,
            0xFFCC44FF, 0xFF6A1199,
            0xFF12121A, 0xFF100818, 0xFF161620,
            0xFF0E0E14, 0xFF161620, 0xFF100818, 0xFF2A2038, 0xFF121218,
            0xFF505068, 0xFFFF5555, 0x50000000),
        PALE("Pale Oak",
            0xFFE8E0D4, 0xFF7A6348, 0xFFF0EBE4, 0xFFA09078,
            0xFF2E2820, 0xFF6A6050, 0xFF5A5244,
            0xFFDD8833, 0xFFAA6622,
            0xFFDDD0BC, 0xFFDDCCAA, 0xFFA09078,
            0xFFA89474, 0xFFB8A484, 0xFF98826A, 0xFF7A6348, 0xFFC4B498,
            0xFF6A6050, 0xFFCC3322, 0x50000000),
        OAK("Oak",
            0xFF5A4420, 0xFF7A5A30, 0xFF18120A, 0xFF4A3818,
            0xFFEEDDBB, 0xFFBBAA88, 0xFFA09070,
            0xFF88CC33, 0xFF557722,
            0xFF3E2C14, 0xFF334418, 0xFF4A3818,
            0xFF2E2010, 0xFF3E3018, 0xFF2E3814, 0xFF685028, 0xFF342810,
            0xFFAA9070, 0xFFCC3322, 0x50000000),
        CHERRY("Cherry",
            0xFFE8C0CC, 0xFF8A4A5A, 0xFFF5E8EE, 0xFFB07888,
            0xFF3A1520, 0xFF8A5868, 0xFF6A3848,
            0xFFDD4488, 0xFF882255,
            0xFFE0B0BE, 0xFFDDA0B2, 0xFFB07888,
            0xFFBB8898, 0xFFCCA0AE, 0xFFAA7888, 0xFF8A4A5A, 0xFFD0A0B0,
            0xFF7A4858, 0xFFEE2266, 0x40000000),
        NETHER("Nether",
            0xFF2C161A, 0xFF51292F, 0xFF140A0C, 0xFF44232B,
            0xFFF0D8C0, 0xFFBB8878, 0xFFA07060,
            0xFFFFAA33, 0xFF885520,
            0xFF3B1E25, 0xFF4A2818, 0xFF3B1E25,
            0xFF3B1E25, 0xFF44232B, 0xFF4A2818, 0xFF5A3030, 0xFF3E2028,
            0xFFBB8070, 0xFFFF3333, 0x50000000),
        OCEAN("Ocean",
            0xFF0E1E2E, 0xFF1A3858, 0xFF0A1620, 0xFF1A3050,
            0xFFD0E8FF, 0xFF6A8AAA, 0xFF5A7A9A,
            0xFF44DDAA, 0xFF228866,
            0xFF122838, 0xFF0E2438, 0xFF1A3050,
            0xFF142838, 0xFF1A3248, 0xFF0E2030, 0xFF1A3858, 0xFF102030,
            0xFF5A8AAA, 0xFFFF5555, 0x50000000);

        public final String name;
        public final int bg,border,inner,innerBd,text,hint,sub;
        public final int accent,accentDim,hover,selected,sep;
        public final int pill,pillHov,pillAct,pillBd,pillFil;
        public final int del,delHov,overlay;

        Theme(String n,int bg,int border,int inner,int innerBd,int text,int hint,int sub,
              int accent,int accentDim,int hover,int selected,int sep,
              int pill,int pillHov,int pillAct,int pillBd,int pillFil,
              int del,int delHov,int overlay){
            this.name=n;this.bg=bg;this.border=border;this.inner=inner;this.innerBd=innerBd;
            this.text=text;this.hint=hint;this.sub=sub;this.accent=accent;this.accentDim=accentDim;
            this.hover=hover;this.selected=selected;this.sep=sep;this.pill=pill;this.pillHov=pillHov;
            this.pillAct=pillAct;this.pillBd=pillBd;this.pillFil=pillFil;this.del=del;this.delHov=delHov;this.overlay=overlay;
        }
        public int swatch(){return switch(this){
            case STONE->0xFF6A6A6E;case OBSIDIAN->0xFF0E0E18;case PALE->0xFFD4CEC4;case OAK->0xFF7A5830;
            case CHERRY->0xFFE0A0B8;case NETHER->0xFF6A2030;case OCEAN->0xFF1A3858;};}
    }

    public static Theme currentTheme = Theme.STONE;

    private static final int W=420,INPUT_H=38,ROW_H=28,MAX_RES=6;
    private static final int SLOT_H=22,PAD=6,R=12,SR=8;

    private static WikiSearchClient.WikiLanguage savedLanguage=WikiSearchClient.WikiLanguage.EN;
    private WikiSearchClient.WikiLanguage currentLanguage=savedLanguage;
    private boolean showLangMenu=false,showThemeMenu=false;
    private TextFieldWidget searchField;
    private final ButtonWidget[] resultButtons=new ButtonWidget[MAX_RES];
    private final ButtonWidget[] langButtons=new ButtonWidget[WikiSearchClient.WikiLanguage.values().length];
    private final ButtonWidget[] slotButtons=new ButtonWidget[3];
    private final ButtonWidget[] slotDelButtons=new ButtonWidget[3];
    private final ButtonWidget[] themeButtons=new ButtonWidget[Theme.values().length];
    private ButtonWidget langPillBtn,themePillBtn;
    private String lastQuery="";
    private List<WikiResult> results=new ArrayList<>();
    private int selectedIndex=0,hoveredIndex=-1,fixedSY=-1;

    public static class WikiResult{
        public final String title,description,url;
        public WikiResult(String t,String d,String u){title=t;description=d;url=u;}
    }

    public SpotlightScreen(){super(Text.literal("Wikicraft"));}
    private int sX(){return this.width/2-W/2;}
    private int sY(){if(fixedSY<0)fixedSY=this.height/3;return fixedSY;}
    private int slotBarY(){return sY()+INPUT_H+4;}
    private int resultsY(){return slotBarY()+SLOT_H+4;}
    private int langBarY(){int c=Math.min(results.size(),MAX_RES);return resultsY()+(c>0?c*ROW_H+4:0);}
    private int totalH(){return langBarY()+24+PAD-sY();}

    @Override protected void init(){
        super.init();fixedSY=-1;Theme t=currentTheme;int sx=sX(),sy=sY();
        searchField=new TextFieldWidget(this.textRenderer,sx+28,sy+INPUT_H/2-6,W-70,12,Text.literal(""));
        searchField.setMaxLength(120);searchField.setDrawsBackground(false);searchField.setEditableColor(t.text);searchField.setTextShadow(false);
        searchField.setChangedListener(this::onQueryChanged);searchField.setFocused(true);
        this.addDrawableChild(searchField);this.setFocused(searchField);
        for(int i=0;i<MAX_RES;i++){final int idx=i;resultButtons[i]=ButtonWidget.builder(Text.literal(""),b->openAtIndex(idx)).dimensions(-3000,-3000,W-PAD*2,ROW_H).build();resultButtons[i].setAlpha(0f);this.addDrawableChild(resultButtons[i]);}
        for(int i=0;i<3;i++){final int s=i;slotButtons[i]=ButtonWidget.builder(Text.literal(""),b->openSlot(s)).dimensions(-3000,-3000,100,SLOT_H).build();slotButtons[i].setAlpha(0f);this.addDrawableChild(slotButtons[i]);
            slotDelButtons[i]=ButtonWidget.builder(Text.literal(""),b->{WikiBrowserScreen.pinnedUrls[s]=null;WikiBrowserScreen.pinnedTitles[s]=null;}).dimensions(-3000,-3000,14,14).build();slotDelButtons[i].setAlpha(0f);this.addDrawableChild(slotDelButtons[i]);}
        langPillBtn=ButtonWidget.builder(Text.literal(""),b->{showLangMenu=!showLangMenu;showThemeMenu=false;refocusSearch();}).dimensions(-3000,-3000,34,20).build();langPillBtn.setAlpha(0f);this.addDrawableChild(langPillBtn);
        var langs=WikiSearchClient.WikiLanguage.values();
        for(int i=0;i<langs.length;i++){final var lang=langs[i];langButtons[i]=ButtonWidget.builder(Text.literal(""),b->{currentLanguage=lang;savedLanguage=lang;showLangMenu=false;lastQuery="";saveConfig();onQueryChanged(searchField.getText());refocusSearch();}).dimensions(-3000,-3000,150,22).build();langButtons[i].setAlpha(0f);this.addDrawableChild(langButtons[i]);}
        themePillBtn=ButtonWidget.builder(Text.literal(""),b->{showThemeMenu=!showThemeMenu;showLangMenu=false;refocusSearch();}).dimensions(-3000,-3000,50,18).build();themePillBtn.setAlpha(0f);this.addDrawableChild(themePillBtn);
        var themes=Theme.values();
        for(int i=0;i<themes.length;i++){final var theme=themes[i];themeButtons[i]=ButtonWidget.builder(Text.literal(""),b->{currentTheme=theme;showThemeMenu=false;searchField.setEditableColor(currentTheme.text);searchField.setTextShadow(false);saveConfig();refocusSearch();}).dimensions(-3000,-3000,120,22).build();themeButtons[i].setAlpha(0f);this.addDrawableChild(themeButtons[i]);}
    }

    private String getPlaceholder(){return switch(currentLanguage){case DE->"Im Minecraft Wiki suchen...";case FR->"Rechercher dans le Wiki...";case ES->"Buscar en la Wiki...";default->"Search Minecraft Wiki...";};}
    private void refocusSearch(){this.setFocused(searchField);searchField.setFocused(true);}
    private void openSlot(int s){String u=WikiBrowserScreen.pinnedUrls[s],t=WikiBrowserScreen.pinnedTitles[s];if(u!=null)MinecraftClient.getInstance().setScreen(new WikiBrowserScreen(u,t,this));}
    private void onQueryChanged(String q){if(q.equals(lastQuery))return;lastQuery=q;if(!q.isBlank())WikiSearchClient.search(q,currentLanguage,f->{results=f;if(selectedIndex>=results.size())selectedIndex=0;});else{results=new ArrayList<>();selectedIndex=0;}}
    private void openAtIndex(int i){if(results.isEmpty())return;i=Math.max(0,Math.min(i,results.size()-1));var r=results.get(i);MinecraftClient.getInstance().setScreen(new WikiBrowserScreen(r.url,r.title,this));}
    private void openSelected(){openAtIndex(selectedIndex);}

    @Override public void render(DrawContext ctx,int mx,int my,float dt){
        Theme t=currentTheme;int sx=sX(),sy=sY(),th=totalH();
        positionAll(sx,mx,my);
        ctx.fill(0,0,this.width,this.height,t.overlay);

        switch(t){
            case STONE->drawStonePanel(ctx,sx,sy,W,th);
            case OBSIDIAN->drawObsidianPanel(ctx,sx,sy,W,th);
            case PALE->drawPalePanel(ctx,sx,sy,W,th);
            case OAK->drawOakPanel(ctx,sx,sy,W,th);
            case CHERRY->drawCherryPanel(ctx,sx,sy,W,th);
            case NETHER->drawNetherPanel(ctx,sx,sy,W,th);
            case OCEAN->drawOceanPanel(ctx,sx,sy,W,th);
        }
        drawBorderRR(ctx,sx,sy,W,th,R,t.border);

        int sfX=sx+PAD,sfY=sy+5,sfW=W-PAD*2,sfH=INPUT_H-10;
        drawWoodBtn(ctx,sfX,sfY,sfW,sfH,SR,t.inner,t.innerBd);
        if(isWoodTheme()||t==Theme.STONE)drawBorderRR(ctx,sfX+1,sfY+1,sfW-2,sfH-2,Math.max(SR-1,1),t.innerBd);
        ctx.drawText(this.textRenderer,"\u2315",sfX+8,sfY+sfH/2-4,t.accentDim,false);
        searchField.setX(sfX+22);searchField.setY(sfY+sfH/2-6);searchField.setWidth(sfW-62);

        if(searchField.getText().isEmpty())
            ctx.drawText(this.textRenderer,getPlaceholder(),sfX+23,sfY+sfH/2-4,t.hint,false);

        int pX=sfX+sfW-38,pY=sfY+(sfH-18)/2;boolean pH=mx>=pX&&mx<pX+32&&my>=pY&&my<pY+18;
        drawWoodBtn(ctx,pX,pY,32,18,9,showLangMenu?t.pillAct:(pH?t.pillHov:t.pill),showLangMenu?t.accent:t.pillBd);
        String lc=currentLanguage.name();int lcw=this.textRenderer.getWidth(lc);
        ctx.drawText(this.textRenderer,lc,pX+16-lcw/2,pY+5,showLangMenu||pH?t.accent:t.sub,false);

        renderSlots(ctx,mx,my,sx);
        if(!results.isEmpty())ctx.fill(sx+PAD+4,resultsY()-2,sx+W-PAD-4,resultsY()-1,(t.sep&0x00FFFFFF)|0x40000000);
        if(!results.isEmpty())renderResults(ctx,mx,my,sx);
        renderBottomBar(ctx,mx,my,sx);
        if(showLangMenu)renderLangMenu(ctx,mx,my,sx);
        if(showThemeMenu)renderThemeMenu(ctx,mx,my,sx);
        super.render(ctx,mx,my,dt);
    }

    private void renderSlots(DrawContext ctx,int mx,int my,int sx){
        Theme t=currentTheme;int barY=slotBarY(),gap=4,slotW=(W-PAD*2-gap*2)/3;
        for(int i=0;i<3;i++){boolean has=WikiBrowserScreen.pinnedUrls[i]!=null;int sX=sx+PAD+i*(slotW+gap);
            boolean hov=mx>=sX&&mx<sX+slotW&&my>=barY&&my<barY+SLOT_H;
            int bg=has?(hov?t.pillHov:t.pillFil):(hov?t.pillHov:t.pill);
            drawWoodBtn(ctx,sX,barY,slotW,SLOT_H,5,bg,t.pillBd);
            if(has){String title=WikiBrowserScreen.pinnedTitles[i];if(title==null)title="?";int mp=slotW-20;
                if(this.textRenderer.getWidth(title)>mp){while(title.length()>3&&this.textRenderer.getWidth(title+"..")>mp)title=title.substring(0,title.length()-1);title+="..";}
                ctx.drawText(this.textRenderer,title,sX+5,barY+SLOT_H/2-4,t.text,false);
                int dX=sX+slotW-12;boolean dH=mx>=dX-2&&mx<dX+10&&my>=barY&&my<barY+SLOT_H;
                ctx.drawText(this.textRenderer,"x",dX,barY+SLOT_H/2-4,dH?t.delHov:t.del,false);
            }else{String l="Slot "+(i+1);int lw=this.textRenderer.getWidth(l);ctx.drawText(this.textRenderer,l,sX+slotW/2-lw/2,barY+SLOT_H/2-4,t.hint,false);}
        }
    }
    private void renderResults(DrawContext ctx,int mx,int my,int sx){
        Theme t=currentTheme;hoveredIndex=-1;int count=Math.min(results.size(),MAX_RES),baseY=resultsY();
        for(int i=0;i<count;i++){var r=results.get(i);int rx=sx+PAD,ry=baseY+i*ROW_H,rw=W-PAD*2;
            boolean hov=mx>=rx&&mx<=rx+rw&&my>=ry&&my<ry+ROW_H;if(hov)hoveredIndex=i;boolean sel=(i==selectedIndex);
            if(sel){if(t==Theme.CHERRY)drawRR(ctx,rx,ry+1,rw,ROW_H-2,5,(t.selected&0x00FFFFFF)|0xD0000000);else drawRR(ctx,rx,ry+1,rw,ROW_H-2,5,t.selected);ctx.fill(rx+2,ry+5,rx+4,ry+ROW_H-5,t.accent);}
            else if(hov){if(t==Theme.CHERRY)drawRR(ctx,rx,ry+1,rw,ROW_H-2,5,(t.hover&0x00FFFFFF)|0xC0000000);else drawRR(ctx,rx,ry+1,rw,ROW_H-2,5,t.hover);}
            else{int frost=t==Theme.CHERRY?0x90000000:0x50000000;drawRR(ctx,rx,ry+1,rw,ROW_H-2,5,(t.bg&0x00FFFFFF)|frost);ctx.fill(rx+PAD,ry+ROW_H-1,rx+rw-PAD,ry+ROW_H,(t.sep&0x00FFFFFF)|0x20000000);}
            boolean active=sel||hov;
            String title=r.title;int maxTW=rw-16;if(this.textRenderer.getWidth(title)>maxTW){while(title.length()>3&&this.textRenderer.getWidth(title+"...")>maxTW)title=title.substring(0,title.length()-1);title+="...";}
            String desc=r.description;int maxDW=rw-16;if(this.textRenderer.getWidth(desc)>maxDW){while(desc.length()>3&&this.textRenderer.getWidth(desc+"...")>maxDW)desc=desc.substring(0,desc.length()-1);desc+="...";}
            ctx.drawText(this.textRenderer,title,rx+8,ry+4,active?t.accent:t.text,false);
            ctx.drawText(this.textRenderer,desc,rx+8,ry+15,t.sub,false);
        }
    }
    private void renderBottomBar(DrawContext ctx,int mx,int my,int sx){
        Theme t=currentTheme;int barY=langBarY();
        ctx.fill(sx+PAD+4,barY,sx+W-PAD-4,barY+1,(t.sep&0x00FFFFFF)|0x30000000);
        String ln=currentLanguage.displayName;int lnw=this.textRenderer.getWidth(ln);
        int lbX=sx+PAD+2,lbY=barY+4,lbW=lnw+12,lbH=16;
        boolean lbH2=mx>=lbX&&mx<lbX+lbW&&my>=lbY&&my<lbY+lbH;
        drawWoodBtn(ctx,lbX,lbY,lbW,lbH,4,showLangMenu?t.pillAct:(lbH2?t.pillHov:t.pill),showLangMenu?t.accent:t.pillBd);
        ctx.drawText(this.textRenderer,ln,lbX+6,lbY+4,showLangMenu||lbH2?t.accent:t.sub,false);
        String tn=t.name;int tnw=this.textRenderer.getWidth(tn);int tbX=sx+W-PAD-tnw-16,tbY=barY+4,tbW=tnw+16,tbH=16;
        boolean tbH2=mx>=tbX&&mx<tbX+tbW&&my>=tbY&&my<tbY+tbH;
        drawWoodBtn(ctx,tbX,tbY,tbW,tbH,4,showThemeMenu?t.pillAct:(tbH2?t.pillHov:t.pill),showThemeMenu?t.accent:t.pillBd);
        ctx.drawText(this.textRenderer,tn,tbX+8,tbY+4,showThemeMenu||tbH2?t.accent:t.sub,false);
    }
    private void renderLangMenu(DrawContext ctx,int mx,int my,int sx){
        Theme t=currentTheme;var langs=WikiSearchClient.WikiLanguage.values();int rowH=22,mW=170,mX=sx+W-PAD-mW,mY=sY()+INPUT_H+2;
        int maxV=Math.min(langs.length,(this.height-mY-10)/rowH);int mH=maxV*rowH+8;
        drawRR(ctx,mX,mY,mW,mH,SR,t.bg);drawBorderRR(ctx,mX,mY,mW,mH,SR,t.border);
        for(int i=0;i<maxV;i++){boolean sel=langs[i]==currentLanguage;int iy=mY+4+i*rowH;boolean hov=mx>=mX+3&&mx<mX+mW-3&&my>=iy&&my<iy+rowH;
            if(sel){drawRR(ctx,mX+3,iy,mW-6,rowH,4,t.selected);ctx.fill(mX+3,iy+3,mX+5,iy+rowH-3,t.accent);}
            else if(hov)drawRR(ctx,mX+3,iy,mW-6,rowH,4,t.hover);
            ctx.drawText(this.textRenderer,langs[i].displayName,mX+12,iy+7,sel?t.accent:t.text,false);}
    }
    private void renderThemeMenu(DrawContext ctx,int mx,int my,int sx){
        Theme t=currentTheme;Theme[] themes=Theme.values();int rowH=24,mW=130,mH=themes.length*rowH+8;
        int mX=sx+W-PAD-mW,mY=sY()+totalH()+2;if(mY+mH>this.height)mY=langBarY()-mH-2;
        drawRR(ctx,mX,mY,mW,mH,SR,t.bg);drawBorderRR(ctx,mX,mY,mW,mH,SR,t.border);
        for(int i=0;i<themes.length;i++){Theme th=themes[i];boolean sel=th==currentTheme;int iy=mY+4+i*rowH;
            boolean hov=mx>=mX+3&&mx<mX+mW-3&&my>=iy&&my<iy+rowH;
            if(sel)drawRR(ctx,mX+3,iy,mW-6,rowH,4,t.selected);else if(hov)drawRR(ctx,mX+3,iy,mW-6,rowH,4,t.hover);
            drawRR(ctx,mX+8,iy+5,14,14,3,th.swatch());if(sel)drawBorderRR(ctx,mX+8,iy+5,14,14,3,t.accent);
            ctx.drawText(this.textRenderer,th.name,mX+28,iy+8,sel?t.accent:t.text,false);}
    }

    private static Path configPath(){return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("wikicraft-spotlight.txt");}

    public static void loadConfig(){
        try{Path p=configPath();if(Files.exists(p)){String[] lines=Files.readString(p).trim().split("\n");
            if(lines.length>=1)for(Theme t:Theme.values())if(t.name().equals(lines[0].trim())){currentTheme=t;break;}
            if(lines.length>=2)for(WikiSearchClient.WikiLanguage l:WikiSearchClient.WikiLanguage.values())if(l.name().equals(lines[1].trim())){savedLanguage=l;break;}
        }}catch(IOException ignored){}
    }

    public static void saveConfig(){
        try{Files.writeString(configPath(),currentTheme.name()+"\n"+savedLanguage.name());}catch(IOException ignored){}
    }

    private void drawStonePanel(DrawContext ctx,int x,int y,int w,int h){
        int fr=4;
        drawRR(ctx,x,y,w,h,R,0xFF3A3A3E);
        drawRR(ctx,x+1,y+1,w-2,h-2,R-1,0xFF585858);
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,0xFF4A4A4E);
        for(int fy=y+1;fy<y+h-1;fy++)for(int fx=x+1;fx<x+w-1;fx+=2){
            int fs=(int)((fx*2654435761L+fy*40503L)&0x7FFFFFFF);
            if((fs&15)<2&&inRR(fx-x,fy-y,w,h)&&!inRR(fx-x-fr,fy-y-fr,w-fr*2,h-fr*2,Math.max(R-fr,2))){
                int c=(fs&1)==0?0xFF525256:0xFF5E5E62;ctx.fill(fx,fy,fx+1,fy+1,c);}}
        ctx.fill(x+R,y+1,x+w-R,y+2,0x10FFFFFF);
        int ix=x+fr,iy=y+fr,iw=w-fr*2,ih=h-fr*2,ir=Math.max(R-fr,2);
        int[] stoneCol={0xFF7A7A7E,0xFF767676,0xFF7E7E82,0xFF727276,0xFF80808A,
                        0xFF74747A,0xFF787880,0xFF6E6E74,0xFF828288,0xFF76767C};
        int[] deepCol={0xFF4A4A50,0xFF464648,0xFF4E4E54,0xFF424246,0xFF50505A,
                       0xFF444448,0xFF484850,0xFF404044,0xFF525258,0xFF46464C};
        int bW=16,bH=8,mortarW=1;
        drawRR(ctx,ix,iy,iw,ih,ir,0xFF686868);
        int deepStart=ih*70/100;
        for(int by=iy;by<iy+ih;by+=bH){int row=(by-iy)/bH;int off=(row%2==0)?0:bW/2;
            boolean deep=(by-iy)>deepStart;
            int[] pal=deep?deepCol:stoneCol;
            int mort=deep?0xFF303034:0xFF686868;
            for(int bx=ix-off;bx<ix+iw;bx+=bW){
                int dx=Math.max(bx+mortarW,ix),dy=Math.max(by+mortarW,iy);
                int dx2=Math.min(bx+bW-mortarW,ix+iw),dy2=Math.min(by+bH-mortarW,iy+ih);
                if(dx>=dx2||dy>=dy2)continue;
                int c1x=dx-ix,c1y=dy-iy,c2x=dx2-1-ix,c2y=dy2-1-iy;
                if(!inRR(c1x,c1y,iw,ih,ir)||!inRR(c2x,c1y,iw,ih,ir)||!inRR(c1x,c2y,iw,ih,ir)||!inRR(c2x,c2y,iw,ih,ir))continue;
                int hash=((bx/bW*73856093)^(by/bH*19349663))&0x7FFFFFFF;
                ctx.fill(dx,dy,dx2,dy2,pal[hash%pal.length]);
                if((hash&3)==0)ctx.fill(dx,dy,dx+1,dy2,0x0C000000);
                if((hash&5)==0)ctx.fill(dx2-1,dy,dx2,dy2,0x08FFFFFF);
                if((hash&7)==0){ctx.fill(dx,dy,dx2,dy+1,0x08FFFFFF);ctx.fill(dx,dy2-1,dx2,dy2,0x0C000000);}
            }}
        if(deepStart>10){int ty=iy+deepStart;
            for(int tx=ix;tx<ix+iw;tx++){int ts=(int)((tx*73856093L)&0x7FFFFFFF);
                if(inRR(tx-ix,ty-iy,iw,ih,ir))ctx.fill(tx,ty-((ts&1)),tx+1,ty+1+((ts>>1)&1),0x30000000);}}
        int[][] ores={
            {8,12,0},{22,8,0},{68,15,0},{85,10,0},{42,22,0},{55,88,0},{78,82,0},{12,78,0},
            {18,28,1},{52,18,1},{80,35,1},{35,45,1},{65,52,1},{10,55,1},{88,62,1},
            {25,38,2},{60,42,2},{78,28,2},{42,65,2},{15,68,2},
            {38,32,3},{72,48,3},{55,55,3},{18,48,3},{88,22,3},
            {32,58,4},{62,35,4},{82,45,4},{48,78,4},
            {45,42,5},{75,62,5},{22,58,5},{90,52,5},
            {28,82,6},{58,78,6},{72,85,6},{8,88,6},{42,92,6}};
        for(int[] o:ores){int ox=ix+iw*o[0]/100,oy=iy+ih*o[1]/100;
            if(!inRR(ox-ix,oy-iy,iw,ih,ir)||!inRR(ox-ix+5,oy-iy+4,iw,ih,ir))continue;
            drawOre(ctx,ox,oy,o[2],(oy-iy)>deepStart);}
        int[][] cracks={{20,30,22,0},{60,20,26,1},{80,50,18,0},{35,60,24,1},{70,35,20,0},{15,55,18,1},{50,45,22,0},{85,70,16,1},
                        {28,75,15,0},{72,78,20,1},{45,85,18,0},{8,42,14,1}};
        for(int[] cr:cracks){int cx=ix+iw*cr[0]/100,cy=iy+ih*cr[1]/100;int len=cr[2];int dir=cr[3];
            for(int s=0;s<len;s++){int px=cx+(dir==0?s:s/2+(((s*7)&3)-1)),py=cy+(dir==1?s:s/2+(((s*5)&3)-1));
                if(px>=ix&&px<ix+iw&&py>=iy&&py<iy+ih&&inRR(px-ix,py-iy,iw,ih,ir)){
                    ctx.fill(px,py,px+1,py+1,0x28000000);
                    if((s&3)==0)ctx.fill(px+1,py,px+2,py+1,0x18000000);}}}
        int[][] moss={{12,62,10,5},{85,58,8,4},{45,68,12,4},{68,72,7,5},{25,55,9,4},{78,65,8,4},{55,60,6,4},{8,70,7,3}};
        for(int[] m:moss){int mx2=ix+iw*m[0]/100,my2=iy+ih*m[1]/100;
            if(!inRR(mx2-ix,my2-iy,iw,ih,ir))continue;
            for(int py=my2;py<my2+m[3]&&py<iy+ih;py++)for(int px=mx2;px<mx2+m[2]&&px<ix+iw;px++){
                int ms=(int)((px*73856093L+py*19349663L)&0x7FFFFFFF);
                if((ms&3)<3&&inRR(px-ix,py-iy,iw,ih,ir)){
                    int mc2=(ms%4)==0?0x5048683A:(ms%4)==1?0x4058784A:(ms%4)==2?0x3040582E:0x4050904A;
                    ctx.fill(px,py,px+1,py+1,mc2);}}}
        int[][] gravel={{30,15,8,5},{70,25,6,4},{50,50,7,5},{15,35,5,4},{85,45,6,3},{60,75,8,4}};
        for(int[] g:gravel){int gx=ix+iw*g[0]/100,gy=iy+ih*g[1]/100;
            if(!inRR(gx-ix,gy-iy,iw,ih,ir))continue;
            for(int py=gy;py<gy+g[3]&&py<iy+ih;py++)for(int px=gx;px<gx+g[2]&&px<ix+iw;px++){
                int gs=(int)((px*2654435761L+py*40503L)&0x7FFFFFFF);
                if((gs&3)<3&&inRR(px-ix,py-iy,iw,ih,ir)){
                    int gc=(gs%3)==0?0xFF8A8A8A:(gs%3)==1?0xFF6A6A6A:0xFF9A9A9A;
                    ctx.fill(px,py,px+1,py+1,gc);}}}
        int[][] lava={{92,92,4,3},{5,95,5,3},{50,95,4,3},{35,90,3,2},{72,93,3,2}};
        for(int[] l:lava){int lx=ix+iw*l[0]/100,ly=iy+ih*l[1]/100;
            if(!inRR(lx-ix,ly-iy,iw,ih,ir))continue;
            ctx.fill(lx,ly,lx+l[2],ly+l[3],0x60CC3300);
            ctx.fill(lx+1,ly,lx+l[2]-1,ly+1,0x80FFAA33);
            ctx.fill(lx,ly+1,lx+1,ly+2,0x40FF6622);ctx.fill(lx+l[2]-1,ly+l[3]-1,lx+l[2],ly+l[3],0x40FF4411);}
    }
    private void drawOre(DrawContext ctx,int ox,int oy,int type,boolean deep){
        int bg=deep?0xFF3A3A40:0xFF686870;
        if(type==0){
            int c=0xFF2A2A2A,cl=0xFF3A3A3A,cd=0xFF1A1A1A;
            ctx.fill(ox+1,oy,ox+3,oy+1,c);ctx.fill(ox,oy+1,ox+4,oy+3,c);ctx.fill(ox+1,oy+3,ox+3,oy+4,c);
            ctx.fill(ox+1,oy+1,ox+2,oy+2,cl);ctx.fill(ox+2,oy+2,ox+3,oy+3,cd);
            ctx.fill(ox+4,oy+1,ox+6,oy+2,c);ctx.fill(ox+5,oy+2,ox+6,oy+3,cd);
        }else if(type==1){
            int c=0xFFD8A868,cl=0xFFE8C088,cd=0xFFB88848;
            ctx.fill(ox+1,oy,ox+3,oy+1,c);ctx.fill(ox,oy+1,ox+4,oy+3,c);ctx.fill(ox+1,oy+3,ox+3,oy+4,c);
            ctx.fill(ox+1,oy+1,ox+2,oy+2,cl);ctx.fill(ox+2,oy+2,ox+3,oy+3,cd);
            ctx.fill(ox+3,oy+1,ox+5,oy+2,cd);ctx.fill(ox+4,oy+2,ox+6,oy+4,c);ctx.fill(ox+5,oy+3,ox+6,oy+4,cl);
        }else if(type==2){
            int c=0xFFE8D040,cl=0xFFFFF068,cd=0xFFC8A820;
            ctx.fill(ox+1,oy,ox+3,oy+1,c);ctx.fill(ox,oy+1,ox+4,oy+3,cd);ctx.fill(ox+1,oy+3,ox+3,oy+4,c);
            ctx.fill(ox+1,oy+1,ox+3,oy+2,cl);ctx.fill(ox+2,oy+2,ox+3,oy+3,cd);
            ctx.fill(ox+4,oy+1,ox+6,oy+3,c);ctx.fill(ox+5,oy+1,ox+6,oy+2,cl);
        }else if(type==3){
            int c=0xFF4488CC,cl=0xFF66BBEE,cd=0xFF2266AA;
            ctx.fill(ox,oy,ox+2,oy+1,c);ctx.fill(ox+1,oy+1,ox+4,oy+3,c);ctx.fill(ox+2,oy+3,ox+4,oy+4,cd);
            ctx.fill(ox+2,oy+1,ox+3,oy+2,cl);ctx.fill(ox+1,oy+2,ox+2,oy+3,cl);
            ctx.fill(ox+4,oy,ox+6,oy+2,c);ctx.fill(ox+5,oy+1,ox+6,oy+2,cd);ctx.fill(ox+4,oy,ox+5,oy+1,cl);
        }else if(type==4){
            int c=0xFFCC3333,cl=0xFFEE5544,cd=0xFFAA2222;
            ctx.fill(ox+1,oy,ox+3,oy+1,c);ctx.fill(ox,oy+1,ox+1,oy+3,c);ctx.fill(ox+2,oy+1,ox+4,oy+3,c);
            ctx.fill(ox+1,oy+3,ox+3,oy+4,cd);ctx.fill(ox+2,oy+1,ox+3,oy+2,cl);
            ctx.fill(ox+4,oy+2,ox+6,oy+4,c);ctx.fill(ox+5,oy+2,ox+6,oy+3,cl);ctx.fill(ox+4,oy+3,ox+5,oy+4,cd);
        }else if(type==5){
            int c=0xFF33BB55,cl=0xFF55DD77,cd=0xFF228844;
            ctx.fill(ox+1,oy,ox+3,oy+1,c);ctx.fill(ox,oy+1,ox+4,oy+3,c);ctx.fill(ox+1,oy+3,ox+3,oy+4,c);
            ctx.fill(ox+1,oy+1,ox+3,oy+2,cl);ctx.fill(ox+2,oy+2,ox+3,oy+3,cd);
            ctx.fill(ox,oy+2,ox+1,oy+3,cd);ctx.fill(ox+3,oy,ox+4,oy+1,cl);
        }else{
            int c=0xFF3355BB,cl=0xFF4477DD,cd=0xFF223399;
            ctx.fill(ox,oy,ox+3,oy+1,c);ctx.fill(ox+1,oy+1,ox+5,oy+3,c);ctx.fill(ox+2,oy+3,ox+5,oy+4,cd);
            ctx.fill(ox+2,oy+1,ox+3,oy+2,cl);ctx.fill(ox+3,oy+2,ox+4,oy+3,cl);
            ctx.fill(ox,oy+1,ox+1,oy+2,cd);ctx.fill(ox+4,oy+1,ox+5,oy+2,cd);
        }
    }

    private void drawObsidianPanel(DrawContext ctx,int x,int y,int w,int h){
        int fr=4;
        drawRR(ctx,x,y,w,h,R,0xFF02020A);
        drawRR(ctx,x+1,y+1,w-2,h-2,R-1,0xFF18082A);
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,0xFF0E0618);
        for(int fy=y+1;fy<y+h-1;fy++)for(int fx=x+1;fx<x+w-1;fx+=2){
            int fs=(int)((fx*2654435761L+fy*40503L)&0x7FFFFFFF);
            if((fs&15)<2&&inRR(fx-x,fy-y,w,h)&&!inRR(fx-x-fr,fy-y-fr,w-fr*2,h-fr*2,Math.max(R-fr,2))){
                int c=(fs&1)==0?0xFF200E38:0xFF140828;ctx.fill(fx,fy,fx+1,fy+1,c);}}
        ctx.fill(x+R,y+1,x+w-R,y+2,0x18FFFFFF);
        int ix=x+fr,iy=y+fr,iw=w-fr*2,ih=h-fr*2,ir=Math.max(R-fr,2);
        int[] obsPal={0xFF0E0818,0xFF100A1C,0xFF0C0616,0xFF12081E,0xFF0A0414,
                      0xFF0E0618,0xFF100820,0xFF0C0518,0xFF0E0A1A,0xFF080310};
        int bW=16,bH=8,bmW=1;
        drawRR(ctx,ix,iy,iw,ih,ir,0xFF060310);
        for(int by=iy;by<iy+ih;by+=bH){int row=(by-iy)/bH;int off=(row%2==0)?0:bW/2;
            for(int bx=ix-off;bx<ix+iw;bx+=bW){
                int dx=Math.max(bx+bmW,ix),dy=Math.max(by+bmW,iy);
                int dx2=Math.min(bx+bW-bmW,ix+iw),dy2=Math.min(by+bH-bmW,iy+ih);
                if(dx>=dx2||dy>=dy2)continue;
                int c1x=dx-ix,c1y=dy-iy,c2x=dx2-1-ix,c2y=dy2-1-iy;
                if(!inRR(c1x,c1y,iw,ih,ir)||!inRR(c2x,c1y,iw,ih,ir)||!inRR(c1x,c2y,iw,ih,ir)||!inRR(c2x,c2y,iw,ih,ir))continue;
                int hash=((bx/bW*73856093)^(by/bH*19349663))&0x7FFFFFFF;
                ctx.fill(dx,dy,dx2,dy2,obsPal[hash%obsPal.length]);
                if((hash&3)==0)ctx.fill(dx,dy,dx+1,dy2,0x08200838);
                if((hash&5)==0)ctx.fill(dx2-1,dy,dx2,dy2,0x06000000);
            }}
        int[][] tears={{15,25,3},{35,50,4},{72,18,3},{55,65,5},{88,40,3},{22,72,4},{62,32,3},{42,82,5},{80,62,4},{10,48,3},
                       {48,15,3},{68,78,4},{30,38,3},{85,28,3},{18,88,4},{58,45,3},{75,55,4},{25,60,3},{90,15,3},{45,90,4}};
        for(int[] t:tears){int tx=ix+iw*t[0]/100,ty=iy+ih*t[1]/100;int tl=t[2];
            if(!inRR(tx-ix,ty-iy,iw,ih,ir))continue;
            ctx.fill(tx,ty-1,tx+1,ty,0x40AA33DD);
            ctx.fill(tx,ty,tx+1,ty+1,0xCCDD55FF);
            for(int d=1;d<tl;d++){int dy=ty+d;if(dy>=iy+ih||!inRR(tx-ix,dy-iy,iw,ih,ir))break;
                int a=Math.max(200-d*50,40);
                ctx.fill(tx,dy,tx+1,dy+1,(a<<24)|0xCC44FF);}
            ctx.fill(tx-1,ty,tx,ty+1,0x30BB44EE);ctx.fill(tx+1,ty,tx+2,ty+1,0x30BB44EE);
            if(tl>=4){ctx.fill(tx,ty+tl-1,tx+1,ty+tl,0x20FF66FF);
                ctx.fill(tx-1,ty+tl-1,tx,ty+tl,0x10DD44EE);ctx.fill(tx+1,ty+tl-1,tx+2,ty+tl,0x10DD44EE);}}
        int[][] sparks={{8,12},{18,35},{28,58},{38,22},{48,75},{58,42},{68,88},{78,28},{88,55},{12,82},
                        {22,48},{32,15},{42,62},{52,38},{62,72},{72,52},{82,18},{92,68},{16,68},{36,85},
                        {56,8},{76,42},{86,75},{26,32},{46,52},{66,15},{14,55},{34,72},{54,28},{74,85},
                        {44,42},{64,58},{84,32},{24,18},{94,48},{6,38},{50,92},{70,8},{40,35},{60,62}};
        for(int[] s:sparks){int spx=ix+iw*s[0]/100,spy=iy+ih*s[1]/100;
            if(!inRR(spx-ix,spy-iy,iw,ih,ir))continue;
            int sh3=(int)((spx*73856093L+spy*2654435761L)&0x7FFFFFFFL);
            int sz=sh3%3;
            int col;
            if(sh3%5==0)col=0xAAFF55FF;
            else if(sh3%5==1)col=0x88DD44EE;
            else if(sh3%5==2)col=0x66BB33CC;
            else if(sh3%5==3)col=0x559944BB;
            else col=0x44CC55DD;
            if(sz==0)ctx.fill(spx,spy,spx+1,spy+1,col);
            else{ctx.fill(spx,spy,spx+1,spy+1,col);
                int glow=(col&0x00FFFFFF)|((Math.max((col>>>24)-80,10))<<24);
                if(sz>=1){ctx.fill(spx-1,spy,spx,spy+1,glow);ctx.fill(spx+1,spy,spx+2,spy+1,glow);}
                if(sz>=2){ctx.fill(spx,spy-1,spx+1,spy,glow);ctx.fill(spx,spy+1,spx+1,spy+2,glow);}}}
    }

    private void drawPalePanel(DrawContext ctx,int x,int y,int w,int h){
        int fr=4;
        drawRR(ctx,x,y,w,h,R,0xFF6E5840);
        drawRR(ctx,x+1,y+1,w-2,h-2,R-1,0xFF9E8A6E);
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,0xFF8B7355);
        for(int fy=y+2;fy<y+h-2;fy+=2)for(int fx=x+1;fx<x+w-1;fx+=3){
            int fs=(int)((fx*2654435761L+fy*40503L)&0x7FFFFFFF);
            if((fs&7)<2&&inRR(fx-x,fy-y,w,h)&&!inRR(fx-x,fy-y,w-fr*2,h-fr*2))
                ctx.fill(fx,fy,fx+1,fy+1,(fs&1)==0?0x18000000:0x10FFFFFF);}
        int ix=x+fr,iy=y+fr,iw=w-fr*2,ih=h-fr*2,ir=Math.max(R-fr,2);
        int seam=0xFFB8B0A0;
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,seam);
        int[] cols={0xFFE6E0D6,0xFFE2DCD2,0xFFEAE4DC,0xFFDED8CE,0xFFE4DED6,
                    0xFFE8E4DA,0xFFE0DAD0,0xFFE6E0D8,0xFFDCD6CC,0xFFEAE6DE};
        int pW=52,pH=13,sW=1;
        for(int by=iy;by<iy+ih;by+=pH){int row=(by-iy)/pH;int off=(row%2==0)?0:pW*3/7;
            for(int bx=ix-off;bx<ix+iw;bx+=pW){
                int dx=Math.max(bx+sW,ix),dy=Math.max(by+sW,iy);
                int dx2=Math.min(bx+pW-sW,ix+iw),dy2=Math.min(by+pH-sW,iy+ih);
                if(dx>=dx2||dy>=dy2)continue;
                int midX=(dx+dx2)/2-ix,midY=(dy+dy2)/2-iy;
                if(!inRR(midX,midY,iw,ih,ir))continue;
                int hash=((bx/pW*73856093)^(by/pH*19349663))&0x7FFFFFFF;
                int base=cols[hash%cols.length];
                ctx.fill(dx,dy,dx2,dy2,base);
                int pw=dx2-dx,phh=dy2-dy;
                for(int gx=dx;gx<dx2;gx++){
                    int seed=(int)((gx*2654435761L+by*40503L)&0x7FFFFFFF);
                    if((seed&7)<3){int gy=dy+(seed%Math.max(1,phh));int gh=2+((seed>>3)&3);
                        ctx.fill(gx,gy,gx+1,Math.min(gy+gh,dy2),(seed&15)<2?0x14000000:0x0A000000);}
                    if((seed&31)==0)ctx.fill(gx,dy+(seed%Math.max(1,phh)),gx+1,dy+(seed%Math.max(1,phh))+1,0x10FFFFFF);
                }
                if((hash&15)==0){int kx=dx+pw/3+(hash%Math.max(1,pw/3)),ky=dy+phh/3;
                    ctx.fill(kx-1,ky-1,kx+2,ky+2,0x18000000);ctx.fill(kx,ky,kx+1,ky+1,0x20000000);}
            }}
        int[][] eyes={{14,38},{72,24},{48,72},{90,52}};
        for(int[] e:eyes)if(inRR(w*e[0]/100,h*e[1]/100,w,h)){
            int ex=x+w*e[0]/100,ey=y+h*e[1]/100;
            ctx.fill(ex-2,ey-1,ex+9,ey+4,0x14DD8833);
            ctx.fill(ex,ey,ex+2,ey+2,0xBBDD8833);ctx.fill(ex,ey,ex+1,ey+1,0xDDFFAA33);
            ctx.fill(ex+5,ey,ex+7,ey+2,0xBBDD8833);ctx.fill(ex+5,ey,ex+6,ey+1,0xDDFFAA33);}
    }

    private void drawOakPanel(DrawContext ctx,int x,int y,int w,int h){
        int fr=4;
        drawRR(ctx,x,y,w,h,R,0xFF3A2810);
        drawRR(ctx,x+1,y+1,w-2,h-2,R-1,0xFF5A4420);
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,0xFF4A3818);
        for(int fy=y+2;fy<y+h-2;fy+=2)for(int fx=x+1;fx<x+w-1;fx+=3){
            int fs=(int)((fx*2654435761L+fy*40503L)&0x7FFFFFFF);
            if((fs&7)<2&&inRR(fx-x,fy-y,w,h)&&(fx<x+fr||fx>=x+w-fr||fy<y+fr||fy>=y+h-fr))
                ctx.fill(fx,fy,fx+1,fy+1,(fs&1)==0?0x10000000:0x08FFFFFF);}
        int ix=x+fr,iy=y+fr,iw=w-fr*2,ih=h-fr*2,ir=Math.max(R-fr,2);
        int seam=0xFF3A2810;
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,seam);
        int[] cols={0xFF8A6838,0xFF7E5C30,0xFF927040,0xFF725028,0xFF86683A,
                    0xFF7A5A32,0xFF8E6C3C,0xFF6E5228,0xFF846438,0xFF785830};
        int pW=52,pH=13,sW=1;
        for(int by=iy;by<iy+ih;by+=pH){int row=(by-iy)/pH;int off=(row%2==0)?0:pW*3/7;
            for(int bx=ix-off;bx<ix+iw;bx+=pW){
                int dx=Math.max(bx+sW,ix),dy=Math.max(by+sW,iy);
                int dx2=Math.min(bx+pW-sW,ix+iw),dy2=Math.min(by+pH-sW,iy+ih);
                if(dx>=dx2||dy>=dy2)continue;
                int midX=(dx+dx2)/2-ix,midY=(dy+dy2)/2-iy;
                if(!inRR(midX,midY,iw,ih,ir))continue;
                int hash=((bx/pW*73856093)^(by/pH*19349663))&0x7FFFFFFF;
                int base=cols[hash%cols.length];
                ctx.fill(dx,dy,dx2,dy2,base);
                for(int gx=dx;gx<dx2;gx++){
                    int seed=(int)((gx*2654435761L+by*40503L)&0x7FFFFFFF);
                    if((seed&7)<3){int gy=dy+(seed%Math.max(1,dy2-dy));int gh=2+((seed>>3)&3);
                        ctx.fill(gx,gy,gx+1,Math.min(gy+gh,dy2),(seed&15)<2?0x14000000:0x0A000000);}
                    if((seed&31)==0)ctx.fill(gx,dy+(seed%Math.max(1,dy2-dy)),gx+1,dy+(seed%Math.max(1,dy2-dy))+1,0x0CFFFFFF);
                }
                if((hash&15)==0){int kx=dx+3+(hash%Math.max(1,dx2-dx-6)),ky=dy+2;
                    ctx.fill(kx-1,ky-1,kx+3,ky+3,0xFF3A2810);ctx.fill(kx,ky,kx+2,ky+2,0xFF4A3818);}
            }}
        int[][] moss={{18,35,6,4},{72,55,5,3},{45,78,7,3},{85,28,5,4},{30,65,6,3}};
        for(int[] m:moss)if(inRR(w*m[0]/100,h*m[1]/100,w,h))
            ctx.fill(x+w*m[0]/100,y+h*m[1]/100,x+w*m[0]/100+m[2],y+h*m[1]/100+m[3],0x14508830);
    }

    private void drawCherryPanel(DrawContext ctx,int x,int y,int w,int h){
        int fr=4;
        drawRR(ctx,x,y,w,h,R,0xFF7A3848);
        drawRR(ctx,x+1,y+1,w-2,h-2,R-1,0xFFA06878);
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,0xFF8A4A5A);
        for(int fy=y+2;fy<y+h-2;fy+=2)for(int fx=x+1;fx<x+w-1;fx+=3){
            int fs=(int)((fx*2654435761L+fy*40503L)&0x7FFFFFFF);
            if((fs&7)<2&&inRR(fx-x,fy-y,w,h)&&(fx<x+fr||fx>=x+w-fr||fy<y+fr||fy>=y+h-fr))
                ctx.fill(fx,fy,fx+1,fy+1,(fs&1)==0?0x10000000:0x08FFFFFF);}
        int ix=x+fr,iy=y+fr,iw=w-fr*2,ih=h-fr*2,ir=Math.max(R-fr,2);
        int seam=0xFFBB8898;
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,seam);
        int[] cols={0xFFECC8D4,0xFFE4B8C8,0xFFF0D0DC,0xFFE0B0C0,0xFFF2D4DE,
                    0xFFE8C0CE,0xFFEECCD8,0xFFE2B4C4,0xFFF0CED8,0xFFE6BCC8};
        int pW=52,pH=13,sW=1;
        for(int by=iy;by<iy+ih;by+=pH){int row=(by-iy)/pH;int off=(row%2==0)?0:pW*3/7;
            for(int bx=ix-off;bx<ix+iw;bx+=pW){
                int dx=Math.max(bx+sW,ix),dy=Math.max(by+sW,iy);
                int dx2=Math.min(bx+pW-sW,ix+iw),dy2=Math.min(by+pH-sW,iy+ih);
                if(dx>=dx2||dy>=dy2)continue;
                int midX=(dx+dx2)/2-ix,midY=(dy+dy2)/2-iy;
                if(!inRR(midX,midY,iw,ih,ir))continue;
                int hash=((bx/pW*73856093)^(by/pH*19349663))&0x7FFFFFFF;
                ctx.fill(dx,dy,dx2,dy2,cols[hash%cols.length]);
                for(int gx=dx;gx<dx2;gx++){
                    int seed=(int)((gx*2654435761L+by*40503L)&0x7FFFFFFF);
                    if((seed&7)<3){int gy=dy+(seed%Math.max(1,dy2-dy));int gh=2+((seed>>3)&3);
                        ctx.fill(gx,gy,gx+1,Math.min(gy+gh,dy2),(seed&15)<2?0x10000000:0x08000000);}
                }
            }}
        int bc=0xFF6A3040,bc2=0xFF582838,bc3=0xFF4A2030;
        drawBranch(ctx,x-8,y+h*95/100,x+w*4/100,y+h*65/100,x+w*10/100,y+h*35/100,bc,6);
        drawBranch(ctx,x+w*10/100,y+h*35/100,x+w*18/100,y+h*18/100,x+w*28/100,y+h*5/100,bc,5);
        drawBranch(ctx,x+w*28/100,y+h*5/100,x+w*36/100,y-2,x+w*46/100,y-10,bc,4);
        drawBranch(ctx,x+w*10/100,y+h*35/100,x+w*6/100,y+h*22/100,x+w*2/100,y+h*10/100,bc2,4);
        drawBranch(ctx,x+w*18/100,y+h*18/100,x+w*25/100,y+h*22/100,x+w*34/100,y+h*20/100,bc3,3);
        drawBranch(ctx,x+w*28/100,y+h*5/100,x+w*22/100,y+h*2/100,x+w*16/100,y-6,bc3,2);
        drawBranch(ctx,x+w*6/100,y+h*22/100,x+w*12/100,y+h*15/100,x+w*20/100,y+h*12/100,bc3,2);
        drawBranch(ctx,x+w*4/100,y+h*65/100,x+w*8/100,y+h*55/100,x+w*15/100,y+h*50/100,bc3,2);
        drawBranch(ctx,x+w+8,y+h*92/100,x+w*94/100,y+h*68/100,x+w*88/100,y+h*42/100,bc,6);
        drawBranch(ctx,x+w*88/100,y+h*42/100,x+w*82/100,y+h*28/100,x+w*75/100,y+h*12/100,bc,5);
        drawBranch(ctx,x+w*75/100,y+h*12/100,x+w*68/100,y+h*2/100,x+w*60/100,y-8,bc,4);
        drawBranch(ctx,x+w*88/100,y+h*42/100,x+w*92/100,y+h*30/100,x+w*98/100,y+h*18/100,bc2,4);
        drawBranch(ctx,x+w*82/100,y+h*28/100,x+w*78/100,y+h*22/100,x+w*72/100,y+h*20/100,bc3,3);
        drawBranch(ctx,x+w*75/100,y+h*12/100,x+w*80/100,y+h*5/100,x+w*86/100,y-4,bc3,2);
        drawBranch(ctx,x+w*94/100,y+h*68/100,x+w*90/100,y+h*58/100,x+w*85/100,y+h*52/100,bc3,2);
        drawBranch(ctx,x+w*82/100,y+h*28/100,x+w*88/100,y+h*20/100,x+w*95/100,y+h*12/100,bc3,2);
        if(h>130){
            drawBranch(ctx,x+w*4/100,y+h*65/100,x+w*10/100,y+h*58/100,x+w*18/100,y+h*55/100,bc3,3);
            drawBranch(ctx,x+w*94/100,y+h*68/100,x+w*88/100,y+h*62/100,x+w*80/100,y+h*58/100,bc3,3);
            drawBranch(ctx,x+w*15/100,y+h*50/100,x+w*20/100,y+h*45/100,x+w*28/100,y+h*42/100,bc3,2);
            drawBranch(ctx,x+w*85/100,y+h*52/100,x+w*80/100,y+h*48/100,x+w*72/100,y+h*45/100,bc3,2);
        }
        if(h>200){
            drawBranch(ctx,x+w*10/100,y+h*35/100,x+w*14/100,y+h*48/100,x+w*20/100,y+h*58/100,bc3,3);
            drawBranch(ctx,x+w*88/100,y+h*42/100,x+w*84/100,y+h*55/100,x+w*78/100,y+h*65/100,bc3,3);
            drawBranch(ctx,x+w*20/100,y+h*58/100,x+w*26/100,y+h*62/100,x+w*34/100,y+h*60/100,bc3,2);
            drawBranch(ctx,x+w*78/100,y+h*65/100,x+w*72/100,y+h*68/100,x+w*65/100,y+h*65/100,bc3,2);
        }
        drawBlossom(ctx,x+w*10/100,y+h*35/100,15);
        drawBlossom(ctx,x+w*28/100,y+h*5/100,15);
        drawBlossom(ctx,x+w*2/100,y+h*10/100,12);
        drawBlossom(ctx,x+w*18/100,y+h*18/100,13);
        drawBlossom(ctx,x+w*34/100,y+h*20/100,10);
        drawBlossom(ctx,x+w*46/100,y-6,9);
        drawBlossom(ctx,x+w*20/100,y+h*12/100,10);
        drawBlossom(ctx,x+w*16/100,y-4,8);
        drawBlossom(ctx,x+w*6/100,y+h*22/100,9);
        drawBlossom(ctx,x+w*15/100,y+h*50/100,8);
        drawBlossom(ctx,x+w*88/100,y+h*42/100,15);
        drawBlossom(ctx,x+w*75/100,y+h*12/100,15);
        drawBlossom(ctx,x+w*98/100,y+h*18/100,12);
        drawBlossom(ctx,x+w*82/100,y+h*28/100,13);
        drawBlossom(ctx,x+w*72/100,y+h*20/100,10);
        drawBlossom(ctx,x+w*60/100,y-4,9);
        drawBlossom(ctx,x+w*86/100,y-2,8);
        drawBlossom(ctx,x+w*95/100,y+h*12/100,9);
        drawBlossom(ctx,x+w*85/100,y+h*52/100,8);
        drawBlossom(ctx,x+w*36/100,y+h*5/100,7);
        drawBlossom(ctx,x+w*50/100,y+h*3/100,6);
        drawBlossom(ctx,x+w*42/100,y+h*12/100,5);
        if(h>130){
            drawBlossom(ctx,x+w*18/100,y+h*55/100,10);
            drawBlossom(ctx,x+w*80/100,y+h*58/100,10);
            drawBlossom(ctx,x+w*28/100,y+h*42/100,8);
            drawBlossom(ctx,x+w*72/100,y+h*45/100,8);
            drawBlossom(ctx,x+w*8/100,y+h*58/100,7);
            drawBlossom(ctx,x+w*90/100,y+h*62/100,7);
            drawBlossom(ctx,x+w*35/100,y+h*48/100,6);
            drawBlossom(ctx,x+w*65/100,y+h*50/100,6);
        }
        if(h>200){
            drawBlossom(ctx,x+w*20/100,y+h*58/100,10);
            drawBlossom(ctx,x+w*78/100,y+h*65/100,10);
            drawBlossom(ctx,x+w*34/100,y+h*60/100,8);
            drawBlossom(ctx,x+w*65/100,y+h*65/100,8);
            drawBlossom(ctx,x+w*45/100,y+h*55/100,7);
            drawBlossom(ctx,x+w*55/100,y+h*58/100,7);
            drawBlossom(ctx,x+w*12/100,y+h*68/100,6);
            drawBlossom(ctx,x+w*88/100,y+h*72/100,6);
            drawBlossom(ctx,x+w*40/100,y+h*70/100,5);
            drawBlossom(ctx,x+w*60/100,y+h*72/100,5);
        }
        int[][] petals={{3,20},{8,32},{12,48},{16,62},{20,38},{24,75},{28,28},{32,58},{36,42},{40,22},
                        {44,68},{48,35},{52,55},{56,18},{60,72},{64,28},{68,48},{72,62},{76,32},{80,55},
                        {84,18},{88,42},{92,68},{96,25},{6,85},{14,72},{22,88},{30,15},{38,82},{46,52},
                        {54,78},{62,12},{66,85},{74,45},{78,75},{82,88},{86,35},{90,78},{94,55},{98,82},
                        {10,58},{18,42},{26,65},{34,52},{42,78},{50,42},{58,65},{70,38},{78,58},{86,72}};
        for(int[] p:petals)drawFallingPetal(ctx,x+w*p[0]/100,y+h*p[1]/100);
    }

    private void drawNetherPanel(DrawContext ctx,int x,int y,int w,int h){
        int mortar=0xFF2C161A;
        int[] brickCols={0xFF44232B,0xFF3B1E25,0xFF301A20,0xFF51292F,0xFF3E2028,0xFF482830};
        int brickW=18,brickH=9,mortarW=2;
        drawRR(ctx,x,y,w,h,R,mortar);
        for(int by=y;by<y+h;by+=brickH){int row=(by-y)/brickH;int off=(row%2==0)?0:brickW/2;
            for(int bx=x-off;bx<x+w;bx+=brickW){
                int dx=Math.max(bx+mortarW,x),dy=Math.max(by+mortarW,y);
                int dx2=Math.min(bx+brickW-mortarW,x+w),dy2=Math.min(by+brickH-mortarW,y+h);
                if(dx>=dx2||dy>=dy2)continue;
                int c1x=dx-x,c1y=dy-y,c2x=dx2-1-x,c2y=dy2-1-y;
                if(!inRR(c1x,c1y,w,h)||!inRR(c2x,c1y,w,h)||!inRR(c1x,c2y,w,h)||!inRR(c2x,c2y,w,h))continue;
                int hash=((bx/brickW*73856093)^(by/brickH*19349663))&0x7FFFFFFF;
                ctx.fill(dx,dy,dx2,dy2,brickCols[hash%brickCols.length]);}}
        int[][] gold={{15,25},{55,62},{82,18},{38,78},{72,42},{25,55},{90,72}};
        for(int[] g:gold){int gx=x+w*g[0]/100,gy=y+h*g[1]/100;
            if(inRR(w*g[0]/100,h*g[1]/100,w,h)){
                ctx.fill(gx,gy,gx+5,gy+5,0xFF886600);ctx.fill(gx+1,gy+1,gx+4,gy+4,0xFFDDAA22);
                ctx.fill(gx+2,gy+2,gx+3,gy+3,0xFFFFDD44);}}
        int[][] embers={{20,35},{45,55},{70,20},{88,60},{12,70},{58,38},{78,80},{35,15},{65,75},{50,88}};
        for(int[] e:embers)if(inRR(w*e[0]/100,h*e[1]/100,w,h))ctx.fill(x+w*e[0]/100,y+h*e[1]/100,x+w*e[0]/100+2,y+h*e[1]/100+2,0x70FFAA33);
    }

    private void drawOceanPanel(DrawContext ctx,int x,int y,int w,int h){
        int fr=4;
        drawRR(ctx,x,y,w,h,R,0xFF0A1828);
        drawRR(ctx,x+1,y+1,w-2,h-2,R-1,0xFF1A3858);
        drawRR(ctx,x+2,y+2,w-4,h-4,R-1,0xFF122840);
        for(int fy=y+1;fy<y+h-1;fy++)for(int fx=x+1;fx<x+w-1;fx+=2){
            int fs=(int)((fx*2654435761L+fy*40503L)&0x7FFFFFFF);
            if((fs&15)<2&&inRR(fx-x,fy-y,w,h)&&!inRR(fx-x-fr,fy-y-fr,w-fr*2,h-fr*2,Math.max(R-fr,2))){
                int c=(fs&1)==0?0xFF1E4868:0xFF143050;ctx.fill(fx,fy,fx+1,fy+1,c);}}
        ctx.fill(x+R,y+1,x+w-R,y+2,0x18FFFFFF);
        int ix=x+fr,iy=y+fr,iw=w-fr*2,ih=h-fr*2,ir=Math.max(R-fr,2);
        int[] prisPal={0xFF2A5858,0xFF245050,0xFF306060,0xFF1E4848,0xFF285454,
                       0xFF225252,0xFF2C5C5C,0xFF204A4A,0xFF2E5E5E,0xFF264E50};
        int bW=14,bH=7,bmW=1;
        drawRR(ctx,x+3,y+3,w-6,h-6,R-2,0xFF1A3838);
        for(int by=iy;by<iy+ih;by+=bH){int row=(by-iy)/bH;int off=(row%2==0)?0:bW/2;
            for(int bx=ix-off;bx<ix+iw;bx+=bW){
                int dx=Math.max(bx+bmW,ix),dy=Math.max(by+bmW,iy);
                int dx2=Math.min(bx+bW-bmW,ix+iw),dy2=Math.min(by+bH-bmW,iy+ih);
                if(dx>=dx2||dy>=dy2)continue;
                int c1x=dx-ix,c1y=dy-iy,c2x=dx2-1-ix,c2y=dy2-1-iy;
                if(!inRR(c1x,c1y,iw,ih,ir)||!inRR(c2x,c1y,iw,ih,ir)||!inRR(c1x,c2y,iw,ih,ir)||!inRR(c2x,c2y,iw,ih,ir))continue;
                int hash=((bx/bW*73856093)^(by/bH*19349663))&0x7FFFFFFF;
                ctx.fill(dx,dy,dx2,dy2,prisPal[hash%prisPal.length]);
                if((hash&3)==0)ctx.fill(dx,dy,dx+1,dy2,0x0C000000);
                if((hash&5)==0)ctx.fill(dx2-1,dy,dx2,dy2,0x08FFFFFF);
                if((hash&7)==0){ctx.fill(dx,dy,dx2,dy+1,0x0844DDAA);ctx.fill(dx,dy2-1,dx2,dy2,0x0C000000);}
            }}
        int sandY=iy+ih*82/100;
        for(int sy2=sandY;sy2<iy+ih;sy2++)for(int sx2=ix;sx2<ix+iw;sx2+=2){
            int ss=(int)((sx2*2654435761L+sy2*40503L)&0x7FFFFFFF);
            if(inRR(sx2-ix,sy2-iy,iw,ih,ir)){
                int sc=(ss%4==0)?0xFFD4C090:(ss%4==1)?0xFFC8B480:(ss%4==2)?0xFFDCC898:0xFFCCBC88;
                ctx.fill(sx2,sy2,sx2+2,sy2+1,sc);}}
        for(int sx2=ix;sx2<ix+iw;sx2++){int ss=(int)((sx2*73856093L)&0x7FFFFFFF);
            int sy3=sandY-((ss&3));
            if(inRR(sx2-ix,sy3-iy,iw,ih,ir))ctx.fill(sx2,sy3,sx2+1,sy3+1+((ss>>2)&1),(ss&4)==0?0xFFD4C090:0xFF1E4848);}
        int[][] kelp={{6,98,30},{16,95,35},{30,97,25},{48,96,32},{62,95,28},{76,98,35},{88,96,22},{22,97,27},{55,95,30},{72,97,32},{42,98,26},{84,95,20}};
        for(int[] k:kelp){int kx=ix+iw*k[0]/100,ky=iy+ih*k[1]/100;int kLen=k[2];
            if(!inRR(kx-ix,ky-iy,iw,ih,ir))continue;
            for(int s=0;s<kLen;s++){int py=ky-s;if(py<iy)break;
                int sw=(int)((s*73856093L+kx*19349663L)&0x7FFFFFFF);
                int px=kx+((sw%3)-1)*((s>6)?1:0);
                if(px>=ix&&px<ix+iw&&inRR(px-ix,py-iy,iw,ih,ir)){
                    int gc=s<kLen/3?0xCC22AA55:s<kLen*2/3?0xBB33CC66:0xAA55DD88;
                    ctx.fill(px,py,px+1,py+1,gc);
                    if(s%3==0&&px+1<ix+iw)ctx.fill(px+1,py,px+2,py+1,(gc&0x00FFFFFF)|0x50000000);
                    if(s%5==0){int lx=px+((sw&4)==0?1:-1);
                        if(lx>=ix&&lx<ix+iw)ctx.fill(lx,py,lx+1,py+1,(gc&0x00FFFFFF)|0x70000000);}}}}
        int[][] coral2={{4,80,0},{18,82,1},{34,78,2},{52,81,0},{66,79,1},{80,82,2},{92,80,0},{12,78,1},{44,80,2},{74,82,0},{26,81,1},{58,78,2},{86,80,1},{38,82,0}};
        for(int[] cr:coral2){int cx=ix+iw*cr[0]/100,cy=iy+ih*cr[1]/100;int ct=cr[2];
            if(!inRR(cx-ix,cy-iy,iw,ih,ir))continue;
            if(ct==0){
                ctx.fill(cx,cy-6,cx+1,cy,0xCCFF4433);ctx.fill(cx+1,cy-5,cx+2,cy-1,0xBBFF5544);ctx.fill(cx-1,cy-4,cx,cy-1,0xAAFF6655);
                ctx.fill(cx+2,cy-3,cx+3,cy,0xBBFF3322);ctx.fill(cx-2,cy-2,cx-1,cy,0x99FF5544);
                ctx.fill(cx,cy,cx+2,cy+1,0xDDFF2211);ctx.fill(cx-1,cy-6,cx,cy-4,0x88FF6644);ctx.fill(cx+2,cy-5,cx+3,cy-3,0x88FF4433);
            }else if(ct==1){
                ctx.fill(cx,cy-5,cx+1,cy,0xCCFFBB33);ctx.fill(cx+1,cy-6,cx+2,cy-1,0xBBFFCC44);ctx.fill(cx+2,cy-4,cx+3,cy,0xAAFFAA22);
                ctx.fill(cx-1,cy-4,cx,cy-1,0xBBFFDD55);ctx.fill(cx+3,cy-3,cx+4,cy-1,0x99FFBB33);
                ctx.fill(cx,cy,cx+2,cy+1,0xDDFFAA11);ctx.fill(cx-1,cy-6,cx,cy-5,0x88FFCC44);
            }else{
                ctx.fill(cx,cy-7,cx+1,cy,0xBBDD44BB);ctx.fill(cx+1,cy-5,cx+2,cy-1,0xAACC33AA);ctx.fill(cx-1,cy-6,cx,cy-2,0xAAEE55CC);
                ctx.fill(cx+2,cy-4,cx+3,cy-1,0x99BB2299);ctx.fill(cx-2,cy-3,cx-1,cy-1,0x88DD55BB);
                ctx.fill(cx,cy,cx+1,cy+1,0xCCCC33AA);ctx.fill(cx+1,cy-7,cx+2,cy-5,0x88EE66CC);
            }}
        drawDolphin(ctx,ix+iw*15/100,iy+ih*18/100,1);
        drawDolphin(ctx,ix+iw*75/100,iy+ih*25/100,-1);
        drawSeaTurtle(ctx,ix+iw*55/100,iy+ih*15/100,1);
        drawSeaTurtle(ctx,ix+iw*30/100,iy+ih*65/100,-1);
        int[][] fish={{10,35,0,1},{38,45,1,-1},{62,30,2,1},{82,50,0,-1},{22,58,1,1},{70,42,3,-1},{48,68,0,1},{88,35,1,-1},
                      {15,52,3,1},{55,38,2,-1},{75,62,0,1},{42,25,3,-1},{65,55,1,1},{28,48,2,-1},{85,28,0,1},{50,60,3,-1}};
        for(int[] f:fish){int fx=ix+iw*f[0]/100,fy=iy+ih*f[1]/100;int ft=f[2];int dir=f[3];
            if(!inRR(fx-ix,fy-iy,iw,ih,ir))continue;
            if(ft==0)drawCod(ctx,fx,fy,dir);
            else if(ft==1)drawSalmon(ctx,fx,fy,dir);
            else if(ft==2)drawTropicalFish(ctx,fx,fy,dir);
            else drawPufferfish(ctx,fx,fy);}
        drawSquid(ctx,ix+iw*42/100,iy+ih*42/100);
        drawSquid(ctx,ix+iw*88/100,iy+ih*55/100);
        int[][] bubbles={{10,12},{22,28},{38,8},{55,32},{72,18},{85,38},{18,48},{42,22},{65,10},{78,30},
                         {30,55},{48,42},{60,25},{90,15},{14,35},{52,5},{68,40},{82,22},{26,42},{44,15}};
        for(int[] b:bubbles){int bx=ix+iw*b[0]/100,by=iy+ih*b[1]/100;
            if(!inRR(bx-ix,by-iy,iw,ih,ir))continue;
            int bs=(int)((bx*73856093L+by*19349663L)&0x7FFFFFFF);
            int sz=bs%3;int bc=(bs%4==0)?0x3088DDFF:(bs%4==1)?0x2866BBDD:(bs%4==2)?0x2044AACC:0x3099EEFF;
            if(sz==0)ctx.fill(bx,by,bx+1,by+1,bc);
            else if(sz==1){ctx.fill(bx,by,bx+2,by+2,bc);ctx.fill(bx,by,bx+1,by+1,(bc&0x00FFFFFF)|0x40000000);}
            else{ctx.fill(bx,by,bx+3,by+3,bc);ctx.fill(bx+1,by,bx+2,by+1,(bc&0x00FFFFFF)|0x50000000);}}
        int[][] treasure2={{22,76,5,4},{72,78,4,3},{48,75,6,4}};
        for(int[] tr:treasure2){int tx=ix+iw*tr[0]/100,ty=iy+ih*tr[1]/100;
            if(!inRR(tx-ix,ty-iy,iw,ih,ir))continue;
            ctx.fill(tx,ty,tx+tr[2],ty+tr[3],0xFF6A5020);ctx.fill(tx+1,ty,tx+tr[2]-1,ty+1,0xFF886830);
            ctx.fill(tx+1,ty+1,tx+tr[2]-1,ty+tr[3]-1,0xFFDDAA22);
            ctx.fill(tx+2,ty+1,tx+3,ty+2,0xFFFFDD44);ctx.fill(tx+tr[2]-2,ty+1,tx+tr[2]-1,ty+2,0xFFFFDD44);
            if(tr[2]>4)ctx.fill(tx+tr[2]/2,ty+2,tx+tr[2]/2+1,ty+3,0xFFFFEE66);}
    }
    private void drawCod(DrawContext ctx,int x,int y,int d){
        int b=0xDDC0A060,bd=0xBB8A7040,e=0xEE111111,f=0xAA9A7848,hi=0x40E0D0A0;
        if(d>0){
            ctx.fill(x,y+1,x+1,y+3,bd);ctx.fill(x+1,y,x+7,y+4,b);ctx.fill(x+7,y+1,x+8,y+3,b);
            ctx.fill(x+2,y,x+5,y+1,bd);ctx.fill(x+2,y+3,x+5,y+4,bd);
            ctx.fill(x-1,y+1,x,y+2,f);ctx.fill(x-1,y+2,x,y+3,f);
            ctx.fill(x+6,y,x+7,y+1,e);ctx.fill(x+3,y+1,x+6,y+2,hi);
        }else{
            ctx.fill(x+7,y+1,x+8,y+3,bd);ctx.fill(x+1,y,x+7,y+4,b);ctx.fill(x,y+1,x+1,y+3,b);
            ctx.fill(x+3,y,x+6,y+1,bd);ctx.fill(x+3,y+3,x+6,y+4,bd);
            ctx.fill(x+8,y+1,x+9,y+2,f);ctx.fill(x+8,y+2,x+9,y+3,f);
            ctx.fill(x+1,y,x+2,y+1,e);ctx.fill(x+2,y+1,x+5,y+2,hi);
        }
    }
    private void drawSalmon(DrawContext ctx,int x,int y,int d){
        int b=0xDD8A3030,bl=0xCCCC5555,bd=0xBB6A2020,e=0xEE111111,f=0xAA882828;
        if(d>0){
            ctx.fill(x,y+1,x+1,y+2,f);ctx.fill(x+1,y,x+8,y+3,b);ctx.fill(x+3,y+1,x+6,y+2,bl);
            ctx.fill(x+8,y+1,x+9,y+2,bd);ctx.fill(x-1,y,x,y+1,f);ctx.fill(x-1,y+2,x,y+3,f);
            ctx.fill(x+7,y,x+8,y+1,e);ctx.fill(x+1,y,x+3,y+1,bd);ctx.fill(x+1,y+2,x+3,y+3,bd);
        }else{
            ctx.fill(x+8,y+1,x+9,y+2,f);ctx.fill(x+1,y,x+8,y+3,b);ctx.fill(x+3,y+1,x+6,y+2,bl);
            ctx.fill(x,y+1,x+1,y+2,bd);ctx.fill(x+9,y,x+10,y+1,f);ctx.fill(x+9,y+2,x+10,y+3,f);
            ctx.fill(x+1,y,x+2,y+1,e);ctx.fill(x+6,y,x+8,y+1,bd);ctx.fill(x+6,y+2,x+8,y+3,bd);
        }
    }
    private void drawTropicalFish(DrawContext ctx,int x,int y,int d){
        int h2=(int)((x*73856093L+y*19349663L)&0x7FFFFFFF);
        int[] c1s={0xDDFF8833,0xDD33BBFF,0xDDFF33AA,0xDDFFDD33};
        int[] c2s={0xBBFFFFFF,0xBB2288DD,0xBBFF6688,0xBBAAAA33};
        int b=c1s[h2%4],st=c2s[h2%4],e=0xEE111111;
        if(d>0){
            ctx.fill(x+1,y,x+5,y+4,b);ctx.fill(x+5,y+1,x+6,y+3,b);
            ctx.fill(x+2,y+1,x+4,y+3,st);
            ctx.fill(x-1,y-1,x+1,y+1,b);ctx.fill(x-1,y+3,x+1,y+5,b);
            ctx.fill(x+4,y,x+5,y+1,e);ctx.fill(x+1,y,x+2,y+1,0x30FFFFFF);
        }else{
            ctx.fill(x+1,y,x+5,y+4,b);ctx.fill(x,y+1,x+1,y+3,b);
            ctx.fill(x+2,y+1,x+4,y+3,st);
            ctx.fill(x+5,y-1,x+7,y+1,b);ctx.fill(x+5,y+3,x+7,y+5,b);
            ctx.fill(x+1,y,x+2,y+1,e);ctx.fill(x+4,y,x+5,y+1,0x30FFFFFF);
        }
    }
    private void drawPufferfish(DrawContext ctx,int x,int y){
        int b=0xCCFFCC22,bd=0xAADDAA11,s=0xBB886611,e=0xDD111111;
        ctx.fill(x+1,y,x+5,y+1,b);ctx.fill(x,y+1,x+6,y+5,b);ctx.fill(x+1,y+5,x+5,y+6,b);
        ctx.fill(x+1,y+1,x+2,y+2,e);ctx.fill(x+4,y+1,x+5,y+2,e);
        ctx.fill(x+2,y+3,x+4,y+4,bd);
        ctx.fill(x,y-1,x+1,y,s);ctx.fill(x+3,y-1,x+4,y,s);ctx.fill(x+5,y-1,x+6,y,s);
        ctx.fill(x-1,y+2,x,y+3,s);ctx.fill(x+6,y+2,x+7,y+3,s);ctx.fill(x-1,y+4,x,y+5,s);ctx.fill(x+6,y+4,x+7,y+5,s);
        ctx.fill(x,y+6,x+1,y+7,s);ctx.fill(x+3,y+6,x+4,y+7,s);ctx.fill(x+5,y+6,x+6,y+7,s);
        ctx.fill(x+1,y+1,x+5,y+2,0x20FFEE44);
    }
    private void drawDolphin(DrawContext ctx,int x,int y,int d){
        int b=0xCC8899AA,bl=0xBBAABBCC,bd=0xAA667788,e=0xDD111111,belly=0xBBBBCCDD;
        if(d>0){
            ctx.fill(x+2,y+1,x+12,y+5,b);ctx.fill(x+12,y+2,x+14,y+4,b);ctx.fill(x+14,y+2,x+15,y+4,bd);
            ctx.fill(x,y+2,x+2,y+4,bd);ctx.fill(x-1,y+3,x+1,y+4,bd);
            ctx.fill(x+3,y+3,x+11,y+5,belly);ctx.fill(x+11,y+2,x+12,y+3,e);
            ctx.fill(x+5,y,x+8,y+1,b);ctx.fill(x+6,y-1,x+7,y,bd);
            ctx.fill(x+2,y+5,x+4,y+7,bd);ctx.fill(x+9,y+5,x+11,y+7,bd);
            ctx.fill(x-2,y+1,x-1,y+3,bd);ctx.fill(x-3,y+2,x-2,y+3,bd);
            ctx.fill(x+4,y+2,x+10,y+3,bl);
        }else{
            ctx.fill(x+3,y+1,x+13,y+5,b);ctx.fill(x+1,y+2,x+3,y+4,b);ctx.fill(x,y+2,x+1,y+4,bd);
            ctx.fill(x+13,y+2,x+15,y+4,bd);ctx.fill(x+14,y+3,x+16,y+4,bd);
            ctx.fill(x+4,y+3,x+12,y+5,belly);ctx.fill(x+3,y+2,x+4,y+3,e);
            ctx.fill(x+7,y,x+10,y+1,b);ctx.fill(x+8,y-1,x+9,y,bd);
            ctx.fill(x+4,y+5,x+6,y+7,bd);ctx.fill(x+11,y+5,x+13,y+7,bd);
            ctx.fill(x+15,y+1,x+16,y+3,bd);ctx.fill(x+16,y+2,x+17,y+3,bd);
            ctx.fill(x+5,y+2,x+11,y+3,bl);
        }
    }
    private void drawSeaTurtle(DrawContext ctx,int x,int y,int d){
        int sh=0xCC447744,shl=0xBB558855,shd=0xAA336633,skin=0xBB55AA55,e=0xDD111111;
        if(d>0){
            ctx.fill(x+2,y+1,x+9,y+6,sh);ctx.fill(x+3,y+2,x+8,y+5,shl);ctx.fill(x+5,y+3,x+6,y+4,shd);
            ctx.fill(x+3,y+0,x+8,y+1,shd);ctx.fill(x+3,y+6,x+8,y+7,shd);
            ctx.fill(x+9,y+2,x+11,y+4,skin);ctx.fill(x+11,y+3,x+12,y+4,skin);
            ctx.fill(x+10,y+2,x+11,y+3,e);
            ctx.fill(x+1,y+0,x+3,y+2,skin);ctx.fill(x+1,y+5,x+3,y+7,skin);
            ctx.fill(x+8,y+0,x+10,y+2,skin);ctx.fill(x+8,y+5,x+10,y+7,skin);
            ctx.fill(x+0,y+2,x+2,y+3,skin);ctx.fill(x+0,y+4,x+2,y+5,skin);
            ctx.fill(x+4,y+2,x+5,y+3,shl);ctx.fill(x+6,y+4,x+7,y+5,shl);
        }else{
            ctx.fill(x+2,y+1,x+9,y+6,sh);ctx.fill(x+3,y+2,x+8,y+5,shl);ctx.fill(x+5,y+3,x+6,y+4,shd);
            ctx.fill(x+3,y+0,x+8,y+1,shd);ctx.fill(x+3,y+6,x+8,y+7,shd);
            ctx.fill(x,y+2,x+2,y+4,skin);ctx.fill(x-1,y+3,x,y+4,skin);
            ctx.fill(x,y+2,x+1,y+3,e);
            ctx.fill(x+8,y+0,x+10,y+2,skin);ctx.fill(x+8,y+5,x+10,y+7,skin);
            ctx.fill(x+1,y+0,x+3,y+2,skin);ctx.fill(x+1,y+5,x+3,y+7,skin);
            ctx.fill(x+9,y+2,x+11,y+3,skin);ctx.fill(x+9,y+4,x+11,y+5,skin);
            ctx.fill(x+4,y+2,x+5,y+3,shl);ctx.fill(x+6,y+4,x+7,y+5,shl);
        }
    }
    private void drawSquid(DrawContext ctx,int x,int y){
        int b=0xAA2A3050,bl=0x883A4060,bd=0x992A2840,e=0xBB111111;
        ctx.fill(x+1,y,x+5,y+6,b);ctx.fill(x+2,y+1,x+4,y+4,bl);
        ctx.fill(x+2,y+1,x+3,y+2,e);ctx.fill(x+3,y+1,x+4,y+2,e);
        ctx.fill(x,y+6,x+1,y+9,bd);ctx.fill(x+1,y+6,x+2,y+10,bd);ctx.fill(x+2,y+6,x+3,y+11,b);
        ctx.fill(x+3,y+6,x+4,y+10,bd);ctx.fill(x+4,y+6,x+5,y+9,bd);ctx.fill(x+5,y+6,x+6,y+8,bd);
        ctx.fill(x-1,y+6,x,y+8,bd);
        ctx.fill(x+2,y+4,x+4,y+5,0x20FFFFFF);
    }


    private boolean inRR(int cx,int cy,int w,int h){return inRR(cx,cy,w,h,R);}
    private boolean inRR(int cx,int cy,int w,int h,int r){
        if(cx<r&&cy<r){int dx=r-cx,dy=r-cy;return dx*dx+dy*dy<=r*r;}
        if(cx>w-r&&cy<r){int dx=cx-w+r,dy=r-cy;return dx*dx+dy*dy<=r*r;}
        if(cx<r&&cy>h-r){int dx=r-cx,dy=cy-h+r;return dx*dx+dy*dy<=r*r;}
        if(cx>w-r&&cy>h-r){int dx=cx-w+r,dy=cy-h+r;return dx*dx+dy*dy<=r*r;}
        return cx>=0&&cx<w&&cy>=0&&cy<h;
    }

    private boolean isWoodTheme(){return currentTheme==Theme.PALE||currentTheme==Theme.OAK||currentTheme==Theme.CHERRY;}

    private void drawWoodBtn(DrawContext ctx,int x,int y,int w,int h,int r,int col,int bd){
        drawRR(ctx,x,y,w,h,r,col);
        Theme t=currentTheme;
        if(isWoodTheme()){
            for(int gy=y+1;gy<y+h-1;gy++){int s=(int)((gy*73856093L+x*19349663L)&0xFF);
                if(s<40)ctx.fill(x+2,gy,x+w-2,gy+1,s<20?0x0A000000:0x06FFFFFF);}
        }else if(t==Theme.STONE){
            for(int gy=y+1;gy<y+h-1;gy+=2)for(int gx=x+1;gx<x+w-1;gx+=2){
                int s=(int)((gx*2654435761L+gy*40503L)&0xFF);
                if(s<30)ctx.fill(gx,gy,gx+1,gy+1,s<15?0x10000000:0x08FFFFFF);}
        }else if(t==Theme.OBSIDIAN){
            for(int gy=y+1;gy<y+h-1;gy+=2)for(int gx=x+1;gx<x+w-1;gx+=3){
                int s=(int)((gx*2654435761L+gy*40503L)&0xFF);
                if(s<25)ctx.fill(gx,gy,gx+1,gy+1,s<12?0x0C8844CC:0x08000000);}
        }else if(t==Theme.NETHER){
            for(int gy=y+1;gy<y+h-1;gy+=2)for(int gx=x+1;gx<x+w-1;gx+=3){
                int s=(int)((gx*2654435761L+gy*40503L)&0xFF);
                if(s<30)ctx.fill(gx,gy,gx+1,gy+1,s<15?0x10000000:0x08FF4422);}
        }else if(t==Theme.OCEAN){
            for(int gy=y+1;gy<y+h-1;gy+=2)for(int gx=x+1;gx<x+w-1;gx+=2){
                int s=(int)((gx*2654435761L+gy*40503L)&0xFF);
                if(s<25)ctx.fill(gx,gy,gx+1,gy+1,s<12?0x0C44DDAA:0x08000000);}
        }
        drawBorderRR(ctx,x,y,w,h,r,bd);
    }

    private void drawBranch(DrawContext ctx,int x0,int y0,int x1,int y1,int x2,int y2,int col,int thick){
        int steps=60;
        for(int s=0;s<=steps;s++){float t=s/(float)steps,u=1-t;
            int nx=(int)(u*u*x0+2*u*t*x1+t*t*x2),ny=(int)(u*u*y0+2*u*t*y1+t*t*y2);
            int tw=Math.max(1,(int)(thick*(1-t*0.4f)));
            int half=tw/2;
            ctx.fill(nx-half,ny-half,nx-half+tw,ny-half+tw,col);
            if(tw>2)ctx.fill(nx-half+1,ny-half,nx-half+tw-1,ny-half+1,(col&0x00FFFFFF)|0x40000000);
        }
    }

    private void drawBlossom(DrawContext ctx,int cx,int cy,int count){
        int[][] off={{0,0},{8,-4},{-7,5},{6,7},{-5,-6},{9,3},{-8,-2},{7,-7},{-4,8},{10,-1},
                     {-6,-8},{3,9},{-9,4},{8,8},{-3,-9}};
        for(int i=0;i<Math.min(count,off.length);i++){
            int fx=cx+off[i][0],fy=cy+off[i][1];
            int seed=(int)((fx*73856093L+fy*19349663L)&0x7FFFFFFF);
            int type=seed%3;
            if(type==0)drawCherry5(ctx,fx,fy,seed);
            else if(type==1)drawCherry4(ctx,fx,fy,seed);
            else drawCherryBud(ctx,fx,fy);
        }
    }

    private void drawCherry5(DrawContext ctx,int cx,int cy,int seed){
        int p=0xDDFFD0E0,pl=0xBBFFB8CC,pd=0xCCFF90AA;
        ctx.fill(cx-1,cy-4,cx+2,cy-2,p);
        ctx.fill(cx+2,cy-2,cx+5,cy+1,pl);
        ctx.fill(cx+1,cy+2,cx+4,cy+5,pd);
        ctx.fill(cx-3,cy+1,cx,cy+4,p);
        ctx.fill(cx-4,cy-2,cx-1,cy+1,pl);
        ctx.fill(cx,cy-1,cx+1,cy+1,0xEEFFEE66);
        ctx.fill(cx-1,cy,cx+2,cy+1,0xEEFFDD55);
        if((seed&4)!=0)ctx.fill(cx,cy-5,cx+1,cy-4,0x80FFDDEE);
    }

    private void drawCherry4(DrawContext ctx,int cx,int cy,int seed){
        int p=0xCCFFCCDD,pl=0xAAFFAABB;
        ctx.fill(cx-1,cy-3,cx+2,cy-1,p);
        ctx.fill(cx+1,cy-1,cx+4,cy+2,pl);
        ctx.fill(cx-1,cy+1,cx+2,cy+4,p);
        ctx.fill(cx-3,cy-1,cx,cy+2,pl);
        ctx.fill(cx,cy,cx+1,cy+1,0xDDFFDD66);
        if((seed&2)!=0){ctx.fill(cx+1,cy-1,cx+2,cy,0xCCFF88AA);ctx.fill(cx-1,cy+1,cx,cy+2,0xCCFF88AA);}
    }

    private void drawCherryBud(DrawContext ctx,int cx,int cy){
        ctx.fill(cx,cy,cx+2,cy+3,0xCCFF90AA);
        ctx.fill(cx,cy,cx+1,cy+1,0xDDFFCCDD);
    }

    private void drawFallingPetal(DrawContext ctx,int x,int y){
        int seed=(int)((x*2654435761L+y*40503L)&0x7FFFFFFF);
        int shape=seed%4;int col=0x70FFCCDD;int cold=0x50FF99BB;
        if(shape==0){ctx.fill(x,y,x+3,y+1,col);ctx.fill(x+1,y+1,x+4,y+2,cold);}
        else if(shape==1){ctx.fill(x,y,x+2,y+2,col);ctx.fill(x+1,y+2,x+3,y+3,cold);}
        else if(shape==2){ctx.fill(x,y,x+1,y+3,col);ctx.fill(x+1,y+1,x+2,y+2,cold);}
        else{ctx.fill(x,y,x+3,y+2,col);ctx.fill(x+2,y+1,x+4,y+3,cold);}
    }

    private void positionAll(int sx,int mx,int my){
        int rCount=Math.min(results.size(),MAX_RES),rBaseY=resultsY();
        boolean menuOpen=showLangMenu||showThemeMenu;
        for(int i=0;i<MAX_RES;i++){if(i<rCount&&!menuOpen){resultButtons[i].setX(sx+PAD);resultButtons[i].setY(rBaseY+i*ROW_H);resultButtons[i].active=resultButtons[i].visible=true;}
            else{resultButtons[i].setX(-3000);resultButtons[i].setY(-3000);resultButtons[i].active=resultButtons[i].visible=false;}}
        int barY=slotBarY(),gap=4,slotW=(W-PAD*2-gap*2)/3;
        for(int i=0;i<3;i++){int sX=sx+PAD+i*(slotW+gap);
            if(showLangMenu||showThemeMenu){slotButtons[i].setX(-3000);slotButtons[i].setY(-3000);slotButtons[i].active=false;slotDelButtons[i].setX(-3000);slotDelButtons[i].setY(-3000);slotDelButtons[i].active=false;}
            else{slotButtons[i].setX(sX);slotButtons[i].setY(barY);slotButtons[i].setWidth(slotW);slotButtons[i].active=slotButtons[i].visible=true;
                if(WikiBrowserScreen.pinnedUrls[i]!=null){slotDelButtons[i].setX(sX+slotW-14);slotDelButtons[i].setY(barY+SLOT_H/2-7);slotDelButtons[i].active=slotDelButtons[i].visible=true;}
                else{slotDelButtons[i].setX(-3000);slotDelButtons[i].setY(-3000);slotDelButtons[i].active=slotDelButtons[i].visible=false;}}}
        int sfX=sx+PAD,sfY=sY()+5,sfW=W-PAD*2,sfH=INPUT_H-10;
        langPillBtn.setX(sfX+sfW-38);langPillBtn.setY(sfY+(sfH-18)/2);langPillBtn.setWidth(32);langPillBtn.active=langPillBtn.visible=true;
        var langs=WikiSearchClient.WikiLanguage.values();int rowH=22,mW=170,mX=sx+W-PAD-mW,mY=sY()+INPUT_H+2;
        int maxV=Math.min(langs.length,(this.height-mY-10)/rowH);
        for(int i=0;i<langs.length;i++){if(showLangMenu&&i<maxV){langButtons[i].setX(mX+3);langButtons[i].setY(mY+4+i*rowH);langButtons[i].setWidth(mW-6);langButtons[i].active=langButtons[i].visible=true;}
            else{langButtons[i].setX(-3000);langButtons[i].setY(-3000);langButtons[i].active=langButtons[i].visible=false;}}
        String tn=currentTheme.name;int tnw=this.textRenderer.getWidth(tn);int tbX=sx+W-PAD-tnw-16,tbY=langBarY()+4;
        themePillBtn.setX(tbX);themePillBtn.setY(tbY);themePillBtn.setWidth(tnw+16);themePillBtn.active=themePillBtn.visible=true;
        Theme[] themes=Theme.values();int tRowH=24,tmW=130,tmH=themes.length*tRowH+8;
        int tmX=sx+W-PAD-tmW,tmY=sY()+totalH()+2;if(tmY+tmH>this.height)tmY=langBarY()-tmH-2;
        for(int i=0;i<themes.length;i++){if(showThemeMenu){themeButtons[i].setX(tmX+3);themeButtons[i].setY(tmY+4+i*tRowH);themeButtons[i].setWidth(tmW-6);themeButtons[i].active=themeButtons[i].visible=true;}
            else{themeButtons[i].setX(-3000);themeButtons[i].setY(-3000);themeButtons[i].active=themeButtons[i].visible=false;}}
    }

    @Override public boolean mouseClicked(Click click,boolean shift){
        if(showThemeMenu){Theme[] th=Theme.values();int tR=24,tmW=130,tmH=th.length*tR+8,tmX=sX()+W-PAD-tmW,tmY=sY()+totalH()+2;
            if(tmY+tmH>this.height)tmY=langBarY()-tmH-2;if(click.x()<tmX||click.x()>tmX+tmW||click.y()<tmY||click.y()>tmY+tmH){showThemeMenu=false;return true;}}
        if(showLangMenu){var la=WikiSearchClient.WikiLanguage.values();int rH=22,mW=170,mX=sX()+W-PAD-mW,mY=sY()+INPUT_H+2;int maxV=Math.min(la.length,(this.height-mY-10)/rH);int mH=maxV*rH+8;
            if(click.x()<mX||click.x()>mX+mW||click.y()<mY||click.y()>mY+mH){showLangMenu=false;return true;}}
        for(int i=0;i<3;i++){if(slotDelButtons[i].active&&slotDelButtons[i].visible){
            int dx=slotDelButtons[i].getX(),dy=slotDelButtons[i].getY();
            if(click.x()>=dx&&click.x()<dx+14&&click.y()>=dy&&click.y()<dy+14){
                WikiBrowserScreen.pinnedUrls[i]=null;WikiBrowserScreen.pinnedTitles[i]=null;return true;}}}
        return super.mouseClicked(click,shift);
    }
    @Override public boolean keyPressed(KeyInput ki){int k=ki.key();
        if(k==GLFW.GLFW_KEY_ESCAPE){if(showLangMenu){showLangMenu=false;return true;}if(showThemeMenu){showThemeMenu=false;return true;}close();return true;}
        if(!results.isEmpty()){if(k==GLFW.GLFW_KEY_UP){selectedIndex=Math.max(selectedIndex-1,0);return true;}
            if(k==GLFW.GLFW_KEY_DOWN){selectedIndex=Math.min(selectedIndex+1,results.size()-1);return true;}
            if(k==GLFW.GLFW_KEY_ENTER||k==GLFW.GLFW_KEY_KP_ENTER){openSelected();return true;}}
        return super.keyPressed(ki);}

    private void drawRR(DrawContext ctx,int x,int y,int w,int h,int r,int col){
        if(r<=0||h<=0||w<=0){ctx.fill(x,y,x+w,y+h,col);return;}r=Math.min(r,Math.min(w/2,h/2));
        if(h>2*r)ctx.fill(x,y+r,x+w,y+h-r,col);
        for(int i=0;i<r;i++){double a=Math.acos((r-i-0.5)/r);int ins=r-(int)(Math.sin(a)*r);
            ctx.fill(x+ins,y+i,x+w-ins,y+i+1,col);ctx.fill(x+ins,y+h-i-1,x+w-ins,y+h-i,col);}
    }
    private void drawBorderRR(DrawContext ctx,int x,int y,int w,int h,int r,int col){
        if(r<=0||h<=0||w<=0)return;r=Math.min(r,Math.min(w/2,h/2));
        ctx.fill(x+r,y,x+w-r,y+1,col);ctx.fill(x+r,y+h-1,x+w-r,y+h,col);
        ctx.fill(x,y+r,x+1,y+h-r,col);ctx.fill(x+w-1,y+r,x+w,y+h-r,col);
        for(int i=0;i<r;i++){double a=Math.acos((r-i-0.5)/r);int ins=r-(int)(Math.sin(a)*r);
            ctx.fill(x+ins,y+i,x+ins+1,y+i+1,col);ctx.fill(x+w-ins-1,y+i,x+w-ins,y+i+1,col);
            ctx.fill(x+ins,y+h-i-1,x+ins+1,y+h-i,col);ctx.fill(x+w-ins-1,y+h-i-1,x+w-ins,y+h-i,col);}
    }
    @Override public boolean shouldPause(){return false;}
}
