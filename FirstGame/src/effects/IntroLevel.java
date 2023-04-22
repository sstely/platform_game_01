package effects;

import main.Game;
import utilz.LoadSave;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import static utilz.Constants.Environment.*;


public class IntroLevel {

    private int lvlIndex;
    private int MbgX, TbgX, TbgY, ZbgX, ZbgY;
    private int finMbgY, finTbgX, finZbgX, finAbgX;
    private int AbgX, AbgY, AbgH;
    private int actNum;
    private BufferedImage markImg, titleImg, zoneImg, actImg;
    private float MbgYFloat, TbgXFloat, ZbgXFloat, AbgXFloat;
    private int defCounter;

    private int McurrentY, TcurrentX, ZcurrentX, AcurrentX;
    private int MStartT, TStartZ, ZStartA;

    private float introSpeed = 0, alphaValue = 255;

    public IntroLevel(int lvlIndex) {
        this.lvlIndex = lvlIndex;

        loadImg(lvlIndex);
        initSizes();
    }

    private void initSizes() {
        MbgX = - RED_MARK_HEIGHT;
        finMbgY = 0;

        if(lvlIndex < 2) {
            TbgX = Game.GAME_WIDTH;
            finTbgX = (int)(Game.GAME_WIDTH - 32 * 1.65 * Game.SCALE - BIG_TITLE_1_WIDTH);
            TbgY = (int)(Game.GAME_HEIGHT - 102 * 1.65 * Game.SCALE - TITLE_HEIGHT);
        }

        ZbgX = Game.GAME_WIDTH;
        finZbgX = (int)(Game.GAME_WIDTH - 32 * 1.65 * Game.SCALE - ZONE_WIDTH);
        ZbgY = (int)(Game.GAME_HEIGHT - 70 * 1.65 * Game.SCALE - TITLE_HEIGHT);

        if(actNum == 1)
            AbgH = ACT_1_HEIGHT;
        else
            AbgH = ACT_2_HEIGHT;
        AbgX = Game.GAME_WIDTH;
        finAbgX = (int)(Game.GAME_WIDTH - 40 * 1.65 * Game.SCALE - ACT_WIDTH);
        AbgY = (int)(Game.GAME_HEIGHT - 30 * 1.65 * Game.SCALE - AbgH);
    }

    private void loadImg(int lvlIndex) {
        markImg = LoadSave.GetSpriteAtlas(LoadSave.RED_MARK);
        zoneImg = LoadSave.GetSpriteAtlas(LoadSave.ZONE);
        if(lvlIndex % 2 == 0) {
            actImg = LoadSave.GetSpriteAtlas(LoadSave.ACT_1);
            actNum = 1;
        }
        else {
            actImg = LoadSave.GetSpriteAtlas(LoadSave.ACT_2);
            actNum = 2;
        }

        switch (lvlIndex) {
            case 0:
                titleImg = LoadSave.GetSpriteAtlas(LoadSave.BIG_TITLE_1);
                break;
        }
    }

    public void update(){
        McurrentY = getFloatPos(MbgX, MbgYFloat);
        TcurrentX = getFloatPos(TbgX, TbgXFloat);
        ZcurrentX = getFloatPos(ZbgX, ZbgXFloat);
        AcurrentX = getFloatPos(AbgX, AbgXFloat);

        MStartT = (int) (finMbgY - 44 * 2 * Game.SCALE);
        TStartZ = (int) (finTbgX + 8 * 2 * Game.SCALE);
        ZStartA = (int) (finZbgX + 13 * 2 * Game.SCALE);

        introSpeed += 1f;
        if(introSpeed <= 250f) {

            alphaValue = 255;

            MbgYFloat += 15f;
            if(McurrentY > MStartT)
                TbgXFloat -= 17f;
            if(TcurrentX < TStartZ)
                ZbgXFloat -= 17f;
            if(ZcurrentX < ZStartA)
                AbgXFloat -= 17f;
        } else {
            if(alphaValue > 0)
                alphaValue --;

            MbgYFloat -= 22f;
            if(McurrentY > MStartT)
                TbgXFloat += 27f;
            if(TcurrentX > finZbgX)
                ZbgXFloat += 27f;
            if(ZcurrentX > finAbgX)
                AbgXFloat += 27f;
        }
    }

    public void draw(Graphics g) {

        if(introSpeed <= 500f) {
            g.setColor(new Color(0, 0, 0, (int)(0 + alphaValue)));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

            if (McurrentY < finMbgY)
                g.drawImage(markImg, RED_MARK_WIDTH, (int) (MbgX + MbgYFloat), RED_MARK_WIDTH, RED_MARK_HEIGHT, null);
            else
                g.drawImage(markImg, RED_MARK_WIDTH, finMbgY, RED_MARK_WIDTH, RED_MARK_HEIGHT, null);

            if (TcurrentX > finTbgX)
                g.drawImage(titleImg, (int) (TbgX + TbgXFloat), TbgY, BIG_TITLE_1_WIDTH, TITLE_HEIGHT, null);
            else
                g.drawImage(titleImg, finTbgX, TbgY, BIG_TITLE_1_WIDTH, TITLE_HEIGHT, null);

            if (ZcurrentX > finZbgX)
                g.drawImage(zoneImg, (int) (ZbgX + ZbgXFloat), ZbgY, ZONE_WIDTH, TITLE_HEIGHT, null);
            else
                g.drawImage(zoneImg, finZbgX, ZbgY, ZONE_WIDTH, TITLE_HEIGHT, null);

            if (AcurrentX > finAbgX)
                g.drawImage(actImg, (int) (AbgX + AbgXFloat), AbgY, ACT_WIDTH, AbgH, null);
            else
                g.drawImage(actImg, finAbgX, AbgY, ACT_WIDTH, AbgH, null);
        }
    }

    private int getFloatPos(int Pos, float bgFloat) {
        return (int)(Pos + bgFloat);
    }

    public void resetAll() {
        MbgYFloat = 0;
        TbgXFloat = 0;
        ZbgXFloat = 0;
        AbgXFloat = 0;

        introSpeed = 0;
        alphaValue = 0;
    }

}
