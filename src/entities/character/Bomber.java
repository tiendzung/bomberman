package entities.character;

import core.Game;
import core.KeyListener;
import core.Sound;
import entities.Bomb;
import entities.Entity;
import entities.Flame;
import entities.items.*;
import graphics.Sprite;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;

import java.util.List;

import static core.Game.*;

public class Bomber extends Entity {

    public int STEP = Sprite.STEP;
    private boolean moving = false;
    private int bombQuantity;
    private boolean flamePass = false;
    private int bomb_size = 1;
    private boolean died = false;
    public int protection_time = 0;
    private int hurtTick = 0;
    private final KeyListener keyListener;

    public Bomber(int x, int y, Image img, KeyListener keyListener) {
        super(x, y, img);
        this.keyListener = keyListener;
        bombQuantity = 1;
        life = 2;
    }

    public void setDied() {
        this.died = true;
    }

    private void chooseSprite() {
        animate++;
        if (animate > 100000) animate = 0;
        if (hurt) {
            img = Sprite.movingSprite(Sprite.player_dead1, Sprite.player_dead2, Sprite.player_dead3, animate, 20).getFxImage;
            return;
        }
        Sprite sprite;
        switch (direction) {
            case U:
                sprite = Sprite.player_up;
                if (moving) {
                    sprite = Sprite.movingSprite(Sprite.player_up_1, Sprite.player_up_2, animate, 20);
                }
                break;
            case D:
                sprite = Sprite.player_down;
                if (moving) {
                    sprite = Sprite.movingSprite(Sprite.player_down_1, Sprite.player_down_2, animate, 20);
                }
                break;
            case L:
                sprite = Sprite.player_left;
                if (moving) {
                    sprite = Sprite.movingSprite(Sprite.player_left_1, Sprite.player_left_2, animate, 20);
                }
                break;
            default:
                sprite = Sprite.player_right;
                if (moving) {
                    sprite = Sprite.movingSprite(Sprite.player_right_1, Sprite.player_right_2, animate, 20);
                }
                break;
        }
        img = sprite.getFxImage;
    }

    public void getItem() {
        int px = (x + (75 * Sprite.SCALED_SIZE) / (2 * 100)) / Sprite.SCALED_SIZE;
        int py = (y + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE;
        if (table[px][py] instanceof FlameItem) {
            if (!((FlameItem) table[px][py]).isPickedup()) {
                bomb_size++;
//                table[px][py] = null;
            }
            ((FlameItem) table[px][py]).pick();
        } else if (table[px][py] instanceof SpeedItem) {
            if (!((SpeedItem) table[px][py]).isPickedup()) {
                STEP++;
//                table[px][py] = null;
            }
            ((SpeedItem) table[px][py]).pick();
        } else if (table[px][py] instanceof BombItem) {
            if (!((BombItem) table[px][py]).isPickedup()) {
                bombQuantity++;
//                table[px][py] = null;
            }
            ((BombItem) table[px][py]).pick();
        } else if (table[px][py] instanceof PortalItem) {
//            if (((PortalItem) table[px][py]).isPickedup()) {
            if (enemies.isEmpty()) gameState = Game.STATE.NEXT_LV;
//                table[px][py] = null;
//            }
//            ((PortalItem) table[px][py]).pick();
        } else if (table[px][py] instanceof FlamePassItem) {
            if (!((FlamePassItem) table[px][py]).isPickedup()) {
                flamePass = true;
//                table[px][py] = null;
            }
            ((FlamePassItem) table[px][py]).pick();
        }
    }

    public void bomberMoving() {
        int px = (x + (75 * Sprite.SCALED_SIZE) / (2 * 100)) / Sprite.SCALED_SIZE;
        int py = (y + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE;
        Entity cur = table[px][py];
        table[px][py] = null;
        if (keyListener.isPressed(KeyCode.D)) {
            direction = Direction.R;
//            if (animate % 10 == 0) {
//                (new Sound("walkh.mp3")).play();
//            }
            if (checkWall(x + STEP + Sprite.SCALED_SIZE - 12, y + 3) && checkWall(x + STEP + Sprite.SCALED_SIZE - 12, y + Sprite.SCALED_SIZE - 3)) {
                x += STEP;
                moving = true;
            }
        }
        if (keyListener.isPressed(KeyCode.A)) {
            direction = Direction.L;
//            if (animate % 20 == 0) {
//                (new Sound("walkh.mp3")).play();
//            }
            if (checkWall(x - STEP, y + 3) && checkWall(x - STEP, y + Sprite.SCALED_SIZE - 3)) {
                x -= STEP;
                moving = true;
            }
        }
        if (keyListener.isPressed(KeyCode.W)) {
            direction = Direction.U;
            if (checkWall(x, y - STEP + 3) && checkWall(x + Sprite.SCALED_SIZE - 12, y - STEP + 3)) {
                y -= STEP;
                moving = true;
            }
        }
        if (keyListener.isPressed(KeyCode.S)) {
            direction = Direction.D;
            if (checkWall(x, y + STEP + Sprite.SCALED_SIZE - 3) && checkWall(x + Sprite.SCALED_SIZE - 12, y + STEP + Sprite.SCALED_SIZE - 3)) {
                y += STEP;
                moving = true;
            }
        }
        table[px][py] = cur;
    }

    public void placeBomb() {
        if (keyListener.isPressed(KeyCode.SPACE) && Bomb.cnt < bombQuantity && !(table[getPlayerX()][getPlayerY()] instanceof Bomb)) {
//            System.out.println(Bomb.cnt);
            Platform.runLater(() -> {
                Entity object = new Bomb(getPlayerX(), getPlayerY(), Sprite.bomb.getFxImage, entities, bomb_size);
                entities.add(object);
                Sound bomb = new Sound("bomb.mp3");
                bomb.play();
            });
        }
    }

    @Override
    public void update() {
        if (hurt) {
            hurtTick++;
            if (hurtTick == 0 && life == 0) {
                (new Sound("lose_game.mp3")).play();
            }
            if (hurtTick == 30) {
                if (life == 0) {
                    Game.gameState = Game.STATE.END;
                }
                hurt = false;
                hurtTick = 0;
                protection_time = 60*2;
//                Platform.exit();
            }
            chooseSprite();
            return;
        }
        protection_time = Math.max(0, protection_time - 1);
        moving = false;
        getItem();
        bomberMoving();
        chooseSprite();
        placeBomb();
    }

    public int getPlayerX() {
        return (x + (75 * Sprite.SCALED_SIZE) / (2 * 100)) / Sprite.SCALED_SIZE;
    }

    public int getPlayerY() {
        return (y + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE;
    }

    public boolean isFlamePass() {
        return flamePass;
    }

    public boolean isProtectded() {
        return protection_time > 0;
    }
}
