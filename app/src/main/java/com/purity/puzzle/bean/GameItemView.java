package com.purity.puzzle.bean;

import android.graphics.Bitmap;

/**
 * 小方块信息
 * Created by purity on 2016/10/2.
 */
public class GameItemView {

    //每个小方块的实际位置x,
    public int x=0;
    //每个小方块的实际位置y,
    public int y=0;
    //每个小方块的图片，
    public Bitmap bm;
    //每个小方块的图片位置x,
    public int p_x=0;
    //每个小方块的图片位置y.
    public int p_y=0;

    public GameItemView(int x, int y, Bitmap bm) {
        this.x = x;
        this.y = y;
        this.bm = bm;
        this.p_x=x;
        this.p_y=y;
    }

    /**
     * 判断每个小方块的位置是否正确
     * @return true 胜利 false 失败
     */
    public boolean isTrue(){
        if (x==p_x&&y==p_y){
            return true;
        }
        return false;
    }
}
