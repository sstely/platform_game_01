package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.Game;
import ui.MenuButton;
import utilz.LoadSave;


public class Menu extends State implements Statemethods{

    private MenuButton[] buttons = new MenuButton[4];
    private BufferedImage backgroundImgMenu, backgroundImg, demoLogo;
    private int menuX, menuY, menuWidth, menuHeight;
    private int demoX, demoY, demoWidth, demoHeight;
    private int alphaValue = 255;

    private ArrayList<Menu.ShowEntity> entitiesList;

    public Menu(Game game) {
        super(game);
        loadButtons();
        loadBackground();
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);

        loadEntities();
    }

    private void loadBackground() {
        backgroundImgMenu = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
        menuWidth = (int)(backgroundImgMenu.getWidth() * Game.SCALE);
        menuHeight = (int)(backgroundImgMenu.getHeight() * Game.SCALE);
        menuX = (Game.GAME_WIDTH/2  - menuWidth/2) + (int)(120 * Game.SCALE);
        menuY = 0;

        demoLogo = LoadSave.GetSpriteAtlas(LoadSave.DEMO_LOGO);
        demoWidth = (int)(demoLogo.getWidth() * Game.SCALE);
        demoHeight = (int)(demoLogo.getHeight() * Game.SCALE);
        demoX = (int)(8 * Game.SCALE);
        demoY = (int)(5 * Game.SCALE);
    }

    private void loadButtons() {
        buttons[0] = new MenuButton(Game.GAME_WIDTH / 2 + (int)(120 * Game.SCALE), (int) (95 * Game.SCALE), 0, Gamestate.PLAYING);
        buttons[1] = new MenuButton(Game.GAME_WIDTH / 2 + (int)(120 * Game.SCALE), (int) (155 * Game.SCALE), 1, Gamestate.OPTIONS);
        buttons[2] = new MenuButton(Game.GAME_WIDTH / 2 + (int)(120 * Game.SCALE), (int) (215 * Game.SCALE), 3, Gamestate.CREDITS);
        buttons[3] = new MenuButton(Game.GAME_WIDTH / 2 + (int)(120 * Game.SCALE), (int) (275 * Game.SCALE), 2, Gamestate.QUIT);
    }

    private void loadEntities() {
        entitiesList = new ArrayList<>();
        entitiesList.add(new Menu.ShowEntity(getIdleAni(LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS), 14, 64, 48), (int) (Game.GAME_WIDTH * 0.07), (int) (Game.GAME_HEIGHT * 0.488)));
    }

    private BufferedImage[] getIdleAni(BufferedImage atlas, int spritesAmount, int width, int height) {
        BufferedImage[] arr = new BufferedImage[spritesAmount];
        for (int i = 0; i < spritesAmount; i++)
            arr[i] = atlas.getSubimage(width * i, 0, width, height);
        return arr;
    }

    @Override
    public void update() {
        for(MenuButton mb: buttons)
            mb.update();

        for(Menu.ShowEntity se : entitiesList)
            se.update();

        if(alphaValue > 0)
            alphaValue --;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.drawImage(backgroundImgMenu, menuX, menuY, menuWidth, menuHeight, null);
        g.drawImage(demoLogo, demoX, demoY, demoWidth, demoHeight, null);

        for(MenuButton mb: buttons)
            mb.draw(g);

        for(Menu.ShowEntity se : entitiesList)
            se.draw(g);

        g.setColor(new Color(0, 0, 0, (int)(0 + alphaValue)));
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for(MenuButton mb: buttons) {
            if(isIn(e, mb)) {
                mb.setMousePressed(true);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for(MenuButton mb: buttons) {
            if (isIn(e, mb)) {
                if(mb.isMousePressed())
                    mb.applyGamestate();
                if(mb.getState() == Gamestate.PLAYING)
                    game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLvlIndex());
                break;
            }
        }

        resetButtons();
    }

    private void resetButtons() {
        for(MenuButton mb: buttons)
            mb.resetBools();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for(MenuButton mb: buttons)
            mb.setMouseOver(false);

        for(MenuButton mb: buttons)
            if(isIn(e ,mb)) {
                mb.setMouseOver(true);
                break;
            }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private class ShowEntity {
        private BufferedImage[] idleAnimation;
        private int x, y, aniIndex, aniTick;

        public ShowEntity(BufferedImage[] idleAnimation, int x ,int y) {
            this.idleAnimation = idleAnimation;
            this.x = x;
            this.y = y;
        }

        public void draw(Graphics g) {
            g.drawImage(idleAnimation[aniIndex], x, y, (int) (idleAnimation[aniIndex].getWidth() * 5), (int) (idleAnimation[aniIndex].getHeight() * 5), null);
        }

        private void update() {
            aniTick++;
            if (aniTick >= 40) { //animation speed
                aniTick = 0;
                aniIndex++;
                if (aniIndex >= idleAnimation.length)
                    aniIndex = 0;
            }
        }

    }

}
