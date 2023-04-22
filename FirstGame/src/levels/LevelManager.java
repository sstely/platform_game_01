package levels;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.Game;
import utilz.LoadSave;


public class LevelManager {

    private Game game;
    private BufferedImage[] levelSprite;
    private BufferedImage[] waterSprite;
    private ArrayList<Level> levels;
    private int lvlIndex = 0, aniTick, aniIndex;

    public LevelManager(Game game) {
        this.game = game;
        importOutsideSprites();
//        createWater();
        levels = new ArrayList<>();
        buildAllLevels();
    }

    private void createWater() {
        waterSprite = new BufferedImage[5];
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.WATER_TOP);
        for(int i = 0; i < 4; i++)
            waterSprite[i] = img.getSubimage(i * 32, 0, 32, 32);
        waterSprite[4] = LoadSave.GetSpriteAtlas(LoadSave.WATER_BOTTOM);
    }

    public void loadNextLevel() {
        Level newLevel = levels.get(lvlIndex);
        game.getPlaying().getEnemyManager().loadEnemies(newLevel);
        game.getPlaying().getPlayer().loadLvlData(newLevel.getLevelData());
        game.getPlaying().setMaxLvlOffsetX(newLevel.getLvlOffsetX());
        game.getPlaying().setMaxLvlOffsetY(newLevel.getLvlOffsetY());
        game.getPlaying().getObjectManager().loadObjects(newLevel);
    }

    private void buildAllLevels() {
        BufferedImage[] allLevels = LoadSave.GetAllLevels();
        for(BufferedImage img : allLevels)
            levels.add(new Level(img));
    }

    private void importOutsideSprites() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.LEVEL_ATLAS);
        levelSprite = new BufferedImage[144];
        for(int j = 0; j < 12; j++)             // 4 tiles in height
            for(int i = 0; i < 12; i++) {      // 12 tiles in width
                int index = j * 12 + i;        // 1st for indexes: 0-11; 2nd for indx: 12-23 ... 4th for indx: 36-47
                levelSprite[index] = img.getSubimage(i*32, j*32, 32, 32);  // 1 tile is 32 x 32 pixels
            }
    }

    public void draw(Graphics g, int xLvlOffset, int yLvlOffset) {
        for(int j = 0; j < levels.get(lvlIndex).getLevelData().length; j++)
            for(int i = 0; i < levels.get(lvlIndex).getLevelData()[0].length; i++) {
                int index = levels.get(lvlIndex).genSpriteIndex(i ,j);
                int x = Game.TILES_SIZE * i - xLvlOffset;
                int y = Game.TILES_SIZE * j - yLvlOffset;

                g.drawImage(levelSprite[index], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);


/*                if(index == 48)
                    g.drawImage(waterSprite[aniIndex], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
                else if(index == 49)
                    g.drawImage(waterSprite[4], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
                else
                    g.drawImage(levelSprite[index], x, y, Game.TILES_SIZE, Game.TILES_SIZE, null);
*/
            }
    }

    public void update() {
        updateWaterAnimation();
    }

    private void updateWaterAnimation() {
        aniTick++;
        if(aniTick >= 40) {
            aniTick = 0;
            aniIndex++;

            if(aniIndex >= 4)
                aniIndex = 0;
        }
    }

    public Level getCurrentLevel() {
        return levels.get(lvlIndex);
    }

    public int getAmountOfLevels() {
        return levels.size();
    }

    public int getLvlIndex() {
        return lvlIndex;
    }

    public void setLevelIndex(int lvlIndex) {
        this.lvlIndex = lvlIndex;
    }

}
