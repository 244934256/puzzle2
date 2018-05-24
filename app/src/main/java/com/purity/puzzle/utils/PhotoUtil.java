package com.purity.puzzle.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * @author: purity
 * @description: 图片工具
 */

public class PhotoUtil {

    /**
     * 得到图片的路径
     *
     * @param externalContentUri 系统相册URI
     * @param selection          查询字段
     * @return
     */
    public String getImagePath(Context context, Uri externalContentUri, String selection) {
        String path = null;
        Cursor cursor = context.getContentResolver ().query (externalContentUri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst ()) {
                path = cursor.getString (cursor.getColumnIndex (MediaStore.Images.Media.DATA));
            }
        }
        cursor.close ();
        return path;
    }

    /**
     * 调整图片的方向
     *
     * @param bitmap
     * @return 调整后的Bitmap
     */
    public Bitmap rotaingImageView(Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix ();
        matrix.postRotate (270);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap (bitmap, 0, 0,
                bitmap.getWidth (), bitmap.getHeight (), matrix, true);
        return resizedBitmap;
    }


    /**
     * 判断图片高是否大于宽 如果大于则要旋转图片
     *
     * @return
     */
    public boolean isHeigthBigWidth(Bitmap bitmap) {
        int width = bitmap.getWidth ();
        int heigth = bitmap.getHeight ();
        if (heigth > width) {
            return true;
        } else {
            return false;
        }
    }
}
