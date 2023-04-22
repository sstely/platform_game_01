package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;
import static utilz.Constants.Directions.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import audio.AudioPlayer;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;


public class Player extends Entity{

    private BufferedImage[][] animations;

    private boolean moving = false, attacking = false;
    private boolean left, right;
    private boolean jump = false;

    private int[][] lvlData;
    private float xDrawOffset = 25 * Game.SCALE;
    private float yDrawOffset = 14.5f * Game.SCALE;
    private int playerSpeed = 20;

    // Jumping / Gravity
    private float jumpSpeed = -2.8f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

    //StatusBarUI
    private BufferedImage statusBarImg;

    private int statusBarWidth = (int)(192 * Game.SCALE);
    private int statusBarHeight = (int)(58 * Game.SCALE);
    private int statusBarX = (int)(10 * Game.SCALE);
    private int statusBarY = (int)(10 * Game.SCALE);

    private int healthBarWidth = (int)(150 * Game.SCALE);
    private int healthBarHeight = (int)(4 * Game.SCALE);
    private int healthBarXStart = (int)(34 * Game.SCALE);
    private int healthBarYStart = (int)(14 * Game.SCALE);
    private int healthWidth = healthBarWidth;

    private int powerBarWidth = (int)(104 * Game.SCALE);
    private int powerBarHeight = (int)(2 * Game.SCALE);
    private int powerBarXStart = (int)(44 * Game.SCALE);
    private int powerBarYStart = (int)(34 * Game.SCALE);
    private int powerWidth = powerBarWidth;
    private int powerMaxValue = 200;
    private int powerValue = powerMaxValue;
    private boolean powerAttackActive;
    private int powerAttackTick;
    private int powerGrowSpeed = 15;
    private int powerGrowTick;

    private int flipX = 0;
    private int flipW = 1;

    private boolean attackChecked;
    private Playing playing;

    private int tileY = 0;

    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.0f;
        loadAnimations();
        initHitbox(14, 33);
        initAttackBox();
    }

    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int)(26 * Game.SCALE), (int)(19 * Game.SCALE));
        resetAttackBox();
    }

    public void update() {
        updateHealthBar(); // before gameover so the status bar gets updated
        updatePowerBar();

        if(currentHealth <= 0) {
            if(state != DEAD) {
                state = DEAD;
                aniTick = 0;
                aniIndex = 0;
                playing.setPlayerDying(true);
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);

                //Check if player died in air
                if(!IsEntityOnFloor(hitbox, lvlData)) {
                    inAir = true;
                    airSpeed = 0;
                }
            } else if(aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= playerSpeed - 1) {
                playing.setGameOver(true);
                playing.getGame().getAudioPlayer().playSong(AudioPlayer.GAMEOVER);
            } else {
                updateAnimationTick();

                //Fall if in air
                if(inAir)
                    if(CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                        hitbox.y += airSpeed;
                        airSpeed += GRAVITY;
                    } else
                        inAir = false;
            }

            return;
        }

        updateAttackBox();

        if(state == HIT) {
            if(aniIndex <= GetSpriteAmount(state) - 1) // first sprite of ani
                pushBack(pushBackDir, lvlData, 0.7f);
            updatePushBackDrawOffset();
        } else
            updatePos();

        if(moving) {
            checkPotionTouched();
            checkSpikesTouched();
//            checkInsideWater();
            tileY = (int)(hitbox.y / Game.TILES_SIZE);
            if(powerAttackActive) {
                powerAttackTick++;
                if(powerAttackTick >= 35) {
                    powerAttackTick = 0;
                    powerAttackActive = false;
                }
            }
        }

        if(attacking || powerAttackActive)
            checkAttack();

        updateAnimationTick();
        setAnimation();
    }

    private void checkInsideWater() {
        if(IsEntityInWater(hitbox, playing.getLevelManager().getCurrentLevel().getLevelData()))
            currentHealth = 0;
    }

    private void checkSpikesTouched() {
        playing.checkSpikesTouched(this);
    }

    private void checkPotionTouched() {
        playing.checkPotionTouched(hitbox);
    }

    private void checkAttack() {
        if(attackChecked || aniIndex != 1)
            return;
        attackChecked = true;

        if(powerAttackActive)
            attackChecked = false;

        playing.checkEnemyHit(attackBox);
        playing.checkObjectHit(attackBox);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    private void setAttackBoxOnRightSide() {
        attackBox.x = hitbox.x + hitbox.width - (int) (Game.SCALE * 4);
    }

    private void setAttackBoxOnLeftSide() {
        attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 8.5);
    }

    private void updateAttackBox() {
        if(right && left) {
            if(flipW == 1) {
                setAttackBoxOnRightSide();
            } else {
                setAttackBoxOnLeftSide();
            }
        } else if(right || (powerAttackActive && flipW == 1))
            setAttackBoxOnRightSide();
        else if(left || (powerAttackActive && flipW == -1))
            setAttackBoxOnLeftSide();

        attackBox.y = hitbox.y + (Game.SCALE * 10);
    }

    private void updateHealthBar() {
        healthWidth = (int)((currentHealth / (float) maxHealth) * healthBarWidth);
    }

    private void updatePowerBar() {
        powerWidth = (int)((powerValue / (float) powerMaxValue) * powerBarWidth);

        powerGrowTick++;
        if(powerGrowTick >= powerGrowSpeed) {
            powerGrowTick = 0;
            changePower(1);
        }
    }

    public void render(Graphics g, int xlvlOffset, int ylvlOffset) {
        g.drawImage(animations[state][aniIndex], (int)(hitbox.x - xDrawOffset) - xlvlOffset + flipX, (int)(hitbox.y - yDrawOffset + (int) (pushDrawOffset)) - ylvlOffset, width * flipW, height,null);
//        drawHitbox(g, xlvlOffset, ylvlOffset);
//        drawAttackBox(g, xlvlOffset, ylvlOffset);
        drawUI(g);
    }

    private void drawUI(Graphics g) {
        //Background ui
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

        //Health bar
        g.setColor(Color.red);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

        //Power bar
        g.setColor(Color.yellow);
        g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
    }

    private void updateAnimationTick() {
        if(state == IDLE)
            playerSpeed = 30;
        else if(state == ATTACK_1)
            playerSpeed = 20;
        else
            playerSpeed = 15;

        aniTick++;
        if(aniTick >= playerSpeed) {
            aniTick = 0;
            aniIndex++;
            if(aniIndex >= GetSpriteAmount(state)) {
                aniIndex = 0;
                attacking = false;
                attackChecked = false;
                if(state == HIT) {
                    newState(IDLE);
                    airSpeed = 0f;
                    if(!IsFloor(hitbox, 0, lvlData))
                        inAir = true;
                }
            }
        }
    }

    private void setAnimation() {
        int startAni = state;

        if(state == HIT)
            return;

        if(moving)
            state = RUNNING;
        else
            state = IDLE;

        if(inAir) {
//            if(airSpeed < 0)
                state = JUMP;
//            else
//                state = FALLING;
        }

        if(powerAttackActive) {
            state = POWER_ATTACK;
            return;
        }

        if(attacking) {
            state = ATTACK_1;
           if(startAni != ATTACK_1) {
                aniIndex = 1;
                aniTick = 0;
                return;
            }
        }

        if(startAni != state)
            resetAniTick();
    }

    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    private void updatePos() {
        moving = false;

        if(jump)
            jump();

        if(!inAir)
            if(!powerAttackActive)
                if((!left && !right) || (right && left))
                    return;

        float xSpeed = 0;

        if(left && !right) {
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        }
        if(right && !left) {
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }

        if(powerAttackActive) {
            if((!left && !right) || (left && right)) {
                if(flipW == -1)
                    xSpeed = -walkSpeed;
                else
                    xSpeed = walkSpeed;
            }

            xSpeed *= 3;
        }

        if(!inAir)
            if(!IsEntityOnFloor(hitbox, lvlData))
                inAir = true;

        if(inAir && !powerAttackActive) {
            if(CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPos(xSpeed);
            } else {
                hitbox.y = GetPlayerYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if(airSpeed > 0)
                    resetInAir();
                else
                    airSpeed = fallSpeedAfterCollision;
                updateXPos(xSpeed);
            }
        } else
            updateXPos(xSpeed);

        moving = true;
    }

    private void jump() {
        if(inAir)
            return;
        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
        inAir = true;
        airSpeed = jumpSpeed;
    }

    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    private void updateXPos(float xSpeed) {
        if(CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x += xSpeed;
        } else {
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
            if(powerAttackActive) {
                powerAttackActive = false;
                powerAttackTick = 0;
            }
        }
    }

    public void changeHealth(int value) {
        if(value < 0) {
            if(state == HIT)
                return;
            else
                newState(HIT);
        }

        currentHealth += value;
        currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
    }

    public void changeHealth(int value, Enemy e) {
        if(state == HIT)
            return;

        changeHealth(value);
        pushBackOffsetDir = UP;
        pushDrawOffset = 0;

        if(e.getHitbox().x < hitbox.x)
            pushBackDir = RIGHT;
        else
            pushBackDir = LEFT;
    }

    public void kill() {
        currentHealth = 0;
    }

    public void changePower(int value) {
        powerValue += value;
        powerValue = Math.max(Math.min(powerValue, powerMaxValue), 0);
    }

    private void loadAnimations() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

        animations = new BufferedImage[8][14];
        for(int j = 0; j < animations.length; j++)
            for (int i = 0; i < animations[j].length; i++)
                animations[j][i] = img.getSubimage(i * 64, j * 48, 64, 48);

        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }

    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
        if(!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    public void resetDirBooleans() {
        left = false;
        right = false;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void resetAll() {
        resetDirBooleans();
        inAir = false;
        attacking = false;
        moving = false;
        jump = false;
        airSpeed = 0f;
        state = IDLE;
        currentHealth = maxHealth;
        powerAttackActive = false;
        powerAttackTick = 0;
        powerValue = powerMaxValue;
        playerSpeed = 20;

        hitbox.x = x;
        hitbox.y = y - 2 * Game.SCALE;
        resetAttackBox();

        if(!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    private void resetAttackBox() {
        if(flipW == 1)
            setAttackBoxOnRightSide();
        else
            setAttackBoxOnLeftSide();
    }

    public int getTileY() {
        return tileY;
    }

    public void powerAttack() {
        if(powerAttackActive)
            return;
        if(powerValue >= 60) {
            powerAttackActive = true;
            changePower(-60);
        }
    }

}
