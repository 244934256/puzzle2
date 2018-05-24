package com.purity.puzzle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.purity.puzzle.bean.GameItemView;
import com.purity.puzzle.utils.PermissionsUtil;
import com.purity.puzzle.utils.PhotoUtil;
import com.purity.puzzle.utils.ToastUtil;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //选择图片的标记
    private static final int CHOICE_PHOTO = 0x001;
    //利用二维数组创建若干个游戏小方框
    private ImageView[][] iv_game_arr = new ImageView[3][5];
    //游戏主界面
    private GridLayout gl_game_layout;
    //小方块的行和列
    private int i, j;
    //空方块的全局变量
    private ImageView iv_null_imagview;
    //当前手势对象
    private GestureDetector gestureDetector;
    //判断游戏是否开始
    private boolean isStart = false;
    //判断当前动画是否在移动状态。（若在移动状态，不可其他操作）
    private boolean isAminMove = false;
    //选择图片的按钮
    private Button bt_choice;
    //图片显示
    private ImageView photo;
    private Bitmap bt_tupan;
    //显示步数的text
    private TextView tv_step;
    //操作的步数
    private static int step = 0;
    //图片工具
    private PhotoUtil photoUtil = new PhotoUtil ();
    //判断是否为刚进去时触发onItemSelected的标志
    boolean spinnerSelected = false;
    //难度等级的下拉列表
    private Spinner spinner;
    ;
    //实时更新操作的步数
    private static Handler handler;

    @SuppressLint("HandlerLeak")
    public MainActivity() {
        handler = new Handler () {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage (msg);
                if (msg.what == 007) {
                    step++;
                    tv_step.setText ("已用步数：" + String.valueOf (step));
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        initView ();
        initEvent ();
        initDate ();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        //@android:style/Theme.Black.NoTitleBar.Fullscreen 在清单文件的activity中配置无效
        requestWindowFeature (Window.FEATURE_NO_TITLE);//去标题
        getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
        setContentView (R.layout.activity_main);
        photo = (ImageView) findViewById (R.id.iv);
        photo.setImageResource (R.drawable.timo);//默认图片
        tv_step = (TextView) findViewById (R.id.tv_step);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) photo.getDrawable ();
        bt_tupan = bitmapDrawable.getBitmap ();//得到默认图片的Bitmap,便于切小图
        gl_game_layout = (GridLayout) findViewById (R.id.gl);
        bt_choice = (Button) findViewById (R.id.bt_choice);
        spinner = (Spinner) findViewById (R.id.spinner);
        PermissionsUtil.checkAndRequestPermissions (this);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        bt_choice.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent ("android.intent.action.GET_CONTENT");
                intent.setType ("image/*");
                startActivityForResult (intent, CHOICE_PHOTO);//打开系统相册
            }
        });

        spinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () { //游戏难度选择
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String level = parent.getItemAtPosition (position).toString ();
                if (spinnerSelected) {
                    if (level.equals ("休闲")) {
                        setLevel (3, 5, 10);
                    } else if (level.equals ("挑战")) {
                        setLevel (4, 7, 50);
                    } else {
                        setLevel (9, 16, 100);
                    }
                } else {
                    spinnerSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    /**
     * 难度等级设置
     *
     * @param rowCount    行数
     * @param columnCount 列数
     * @param times       切换次数
     */
    private void setLevel(int rowCount, int columnCount, int times) {
        removeGameItem ();
        gl_game_layout.setRowCount (rowCount);
        gl_game_layout.setColumnCount (columnCount);
        iv_game_arr = new ImageView[rowCount][columnCount];
        setGameItem (iv_game_arr[0].length);
        startGame (times);
    }

    /**
     * 初始化数据
     */
    private void initDate() {
        setGameItem (iv_game_arr[0].length);
        startGame (10);
    }

    //回调系统相册
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        switch (requestCode) {
            case CHOICE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat (data);
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) photo.getDrawable ();
                        bt_tupan = bitmapDrawable.getBitmap ();
                        removeGameItem ();
                        setGameItem (iv_game_arr[0].length);
                        startGame (10);
                    } else {
                        handleImageBeforeKitKat (data);
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) photo.getDrawable ();
                        bt_tupan = bitmapDrawable.getBitmap ();
                        removeGameItem ();
                        setGameItem (iv_game_arr[0].length);
                        startGame (10);
                    }
                }
        }
    }

    /**
     * API小于19获取系统相册
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData ();
        String imagePath = photoUtil.getImagePath (MainActivity.this,uri, null);
        displayImage (imagePath);
    }

    /**
     * API大于19获取系统相册
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData ();
        if (DocumentsContract.isDocumentUri (this, uri)) {
            //如果是document类型的url,则通过document的id处理。
            String docId = DocumentsContract.getDocumentId (uri);
            if ("com.android.providers.media.documents".equals (uri.getAuthority ())) {
                String id = docId.split (":")[1];//解析出数字格式的id;
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = photoUtil.getImagePath (MainActivity.this,MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals (uri.getAuthority ())) {
                Uri contenturi = ContentUris.withAppendedId (Uri.parse ("content://downloads/public_downloads"), Long.valueOf (docId));
                imagePath = photoUtil.getImagePath (MainActivity.this,contenturi, null);
            }
        } else if ("content".equalsIgnoreCase (uri.getScheme ())) {
            //如果不是document类型的uri,则使用普通的方式处理。
            imagePath = photoUtil.getImagePath (MainActivity.this,uri, null);
        }
        displayImage (imagePath);
    }

    /**
     * 显示图片
     *
     * @param imagePath 图片的路径。
     */
    public void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile (imagePath);
            if (photoUtil.isHeigthBigWidth (bitmap)) {
                Bitmap bt = photoUtil.rotaingImageView (bitmap);//将图片旋转90度。
                Bitmap disbitmapt = ajustBitmap (bt, iv_game_arr);
                photo.setImageBitmap (disbitmapt);
            } else {
                Bitmap disbitmap = ajustBitmap (bitmap, iv_game_arr);
                photo.setImageBitmap (disbitmap);
            }
        }
    }

    /**
     * 调整图片的大小
     */
    public Bitmap ajustBitmap(Bitmap bitmap, ImageView[][] ivArr) {
        int width = getWindowManager ().getDefaultDisplay ().getWidth () - (iv_game_arr[0].length - 1) * 2;//屏幕的宽
        int heigth = width / ivArr[0].length * ivArr.length;//高=宽/列*行 需要保证:行数*小方块宽=bitmap.getHeigth,列数*小方块宽=bitmap.getWidth
        Bitmap scaledBitmap = Bitmap.createScaledBitmap (bitmap, width, heigth, true);
        return scaledBitmap;
    }

    /**
     * 清空网格布局中的小方块
     */
    private void removeGameItem() {
        for (i = 0; i < iv_game_arr.length; i++) {
            for (j = 0; j < iv_game_arr[0].length; j++) {
                gl_game_layout.removeView (iv_game_arr[i][j]);
            }
        }
    }

    /**
     * 切成小方块
     */
    private void setGameItem(int columnCount) {
        Bitmap abitmap = ajustBitmap (bt_tupan, iv_game_arr);
        int ivWidth = getWindowManager ().getDefaultDisplay ().getWidth () / columnCount;//每个游戏小方块的宽和高。切成正方形
        int tuWidth = abitmap.getWidth () / columnCount;
        for (int i = 0; i < iv_game_arr.length; i++) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                Bitmap bm = Bitmap.createBitmap (abitmap, j * tuWidth, i * tuWidth, tuWidth, tuWidth);//将大图切成小方块
                iv_game_arr[i][j] = new ImageView (this);
                iv_game_arr[i][j].setImageBitmap (bm);//设置每一个小方块的图案
                iv_game_arr[i][j].setLayoutParams (new RelativeLayout.LayoutParams (ivWidth, ivWidth));
                iv_game_arr[i][j].setPadding (1, 1, 1, 1); //设置方块之间的间距
                iv_game_arr[i][j].setTag (new GameItemView (i, j, bm)); //绑定自定义数据
                iv_game_arr[i][j].setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        boolean flag = isAdjacentNullImageView ((ImageView) v);
                        if (flag) {
                            changeDateByImageView ((ImageView) v);
                            handler.sendEmptyMessage (007);
                        }
                    }
                });
            }
        }
    }

    /**
     * 开始游戏
     */
    private void startGame(int times) {
        tv_step.setText ("已用步数：0");
        for (i = 0; i < iv_game_arr.length; i++) {
            for (j = 0; j < iv_game_arr[0].length; j++) {
                gl_game_layout.addView (iv_game_arr[i][j]);
            }
        }
        //将最后一个方块设置为设置空方块。
        setNullImageView (iv_game_arr[i - 1][j - 1]);
        randomOrder (times);
        isStart = true;//游戏开始
        //创建手势对象
        gestureDetector = new GestureDetector (this, new GestureDetector.OnGestureListener () {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int type = getDirctionByGesure (e1.getX (), e1.getY (), e2.getX (), e2.getY ());
                changeByDirGes (type);
                handler.sendEmptyMessage (007);
                return false;
            }
        });
    }

    /**
     * 手势交换图片
     */
    public void changeByDirGes(int type) {
        //默认有动画
        changeByDirGes (type, true);
    }

    /**
     * 根据手势的方向，对空方块相邻位置的方块进行移动。
     *
     * @param type   方向的返回值  1:上 2：下 3：左 5：右
     * @param isAnim 是否有动画 true:有动画，false:无动画
     */
    public void changeByDirGes(int type, boolean isAnim) {
        //1.获取当前空方块的位置。
        GameItemView null_gameItemView = (GameItemView) iv_null_imagview.getTag ();
        int new_x = null_gameItemView.x;
        int new_y = null_gameItemView.y;
        //2.根据方向，设置相应相邻的位置坐标。
        if (type == 1) {//说明空方块在要移动的方块的上面。
            new_x++;
        } else if (type == 2) {//空方块在要移动的方块的下面
            new_x--;
        } else if (type == 3) {//空方块在要移动的方块的左面
            new_y++;
        } else if (type == 4) {//空方块在要移动的方块的右面
            new_y--;
        }
        //3.判断这个新坐标是否存在
        if (new_x >= 0 && new_x < iv_game_arr.length && new_y >= 0 && new_y < iv_game_arr[0].length) {
            //存在，可以移动交换数据
            if (isAnim) {//有动画
                changeDateByImageView (iv_game_arr[new_x][new_y]);
            } else {
                changeDateByImageView (iv_game_arr[new_x][new_y], isAnim);
            }
        } else {
            //什么也不做
        }
    }

    /**
     * 增加手势滑动，根据手势判断是上下左右滑动
     *
     * @param start_x 手势起始点x
     * @param start_y 手势起始点y
     * @param end_x   手势终止点 x
     * @param end_y   手势终止点y
     * @return 1:上 2：下 3：左 5：右
     */
    public int getDirctionByGesure(float start_x, float start_y, float end_x, float end_y) {
        boolean isLeftOrRight = (Math.abs (end_x - start_x) > Math.abs (end_y - start_y)) ? true : false; //是否是左右
        if (isLeftOrRight) {//左右
            boolean isLeft = (end_x - start_x) > 0 ? false : true;
            if (isLeft) {
                return 3;
            } else {
                return 4;
            }
        } else {//上下
            boolean isUp = (end_y - start_y) > 0 ? false : true;
            if (isUp) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    /**
     * 设置动画，动画结束之后，交换两个方块的数据。
     *
     * @param itemimageView 点击的方块
     */
    public void changeDateByImageView(final ImageView itemimageView) {
        //默认有动画
        changeDateByImageView (itemimageView, true);
    }


    /**
     * 设置动画，动画结束之后，交换两个方块的数据。
     *
     * @param itemimageView 点击的方块
     * @param isAnim        是否有动画
     */
    public void changeDateByImageView(final ImageView itemimageView, boolean isAnim) {
        if (isAminMove) {//如果动画正在执行，不做交换操作(按顺序执行)
            return;
        }
        //1.创建一个动画，设置方向，移动的距离
        TranslateAnimation translateAnimation = null;
        if (!isAnim) {
            //得到点击方块绑定的数据
            GameItemView gameItemView = (GameItemView) itemimageView.getTag ();
            //将空方块的图案设置为点击方块
            iv_null_imagview.setImageBitmap (gameItemView.bm);
            //得到空方块绑定的数据
            GameItemView null_gameItemView = (GameItemView) iv_null_imagview.getTag ();
            //交换数据（将点击方块的数据传入空方块）
            null_gameItemView.bm = gameItemView.bm;
            null_gameItemView.p_x = gameItemView.p_x;
            null_gameItemView.p_y = gameItemView.p_y;
            //设置当前点击的方块为空方块。
            setNullImageView (itemimageView);
            if (isStart) {
                isGameWin ();//成功时，会弹一个吐司。
            }
            return;
        }
        //判断方向，设置动画
        if (itemimageView.getX () > iv_null_imagview.getX ()) {//当前点击的方块在空方块的上面
            //下移
            translateAnimation = new TranslateAnimation (0.1f, -itemimageView.getWidth (), 0.1f, 0.1f);
        } else if (itemimageView.getX () < iv_null_imagview.getX ()) {//当前点击的方块在空方块的下面
            //上移
            boolean f = itemimageView.getX () < iv_null_imagview.getX ();
            translateAnimation = new TranslateAnimation (0.1f, itemimageView.getWidth (), 0.1f, 0.1f);
        } else if (itemimageView.getY () > iv_null_imagview.getY ()) {//当前点击的方块在空方块的左面
            //右移
            translateAnimation = new TranslateAnimation (0.1f, 0.1f, 0.1f, -itemimageView.getWidth ());
        } else if (itemimageView.getY () < iv_null_imagview.getY ()) {//当前点击的方块在空方块的右面
            //左移
            translateAnimation = new TranslateAnimation (0.1f, 0.1f, 0.1f, itemimageView.getWidth ());
        }
        //2.设置动画的各种参数
        translateAnimation.setDuration (80);
        translateAnimation.setFillAfter (true);
        //3.设置动画的监听
        translateAnimation.setAnimationListener (new Animation.AnimationListener () {
            @Override
            public void onAnimationStart(Animation animation) {
                isAminMove = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //动画结束，交换数据
                isAminMove = false;
                itemimageView.clearAnimation ();
                //得到点击方块绑定的数据
                GameItemView gameItemView = (GameItemView) itemimageView.getTag ();
                //将空方块的图案设置为点击方块
                iv_null_imagview.setImageBitmap (gameItemView.bm);
                //得到空方块绑定的数据
                GameItemView null_gameItemView = (GameItemView) iv_null_imagview.getTag ();
                //交换数据（将点击方块的数据传入空方块）
                null_gameItemView.bm = gameItemView.bm;
                null_gameItemView.p_x = gameItemView.p_x;
                null_gameItemView.p_y = gameItemView.p_y;
                //设置当前点击的方块为空方块。
                setNullImageView (itemimageView);
                if (isStart) {
                    isGameWin ();//成功时，会弹一个吐司。
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        //动画执行
        itemimageView.startAnimation (translateAnimation);
    }

    /**
     * 设置移动后的方块为空方块
     *
     * @param imageView 当前要设置为空方块的实例
     */
    public void setNullImageView(ImageView imageView) {
        imageView.setImageBitmap (null);
        iv_null_imagview = imageView;
    }

    /**
     * 判断当前点击的方块，是否和空方块相邻。
     *
     * @param imageView 当前点击的方块
     * @return true：相邻。 false:不相邻。
     */
    public boolean isAdjacentNullImageView(ImageView imageView) {
        //获取当前空方块的位置与点击方块的位置
        GameItemView null_gameItemView = (GameItemView) iv_null_imagview.getTag ();
        GameItemView now_gameItem_view = (GameItemView) imageView.getTag ();
        if (null_gameItemView.y == now_gameItem_view.y && now_gameItem_view.x + 1 == null_gameItemView.x) {//当前点击的方块在空方块的上面
            return true;
        } else if (null_gameItemView.y == now_gameItem_view.y && now_gameItem_view.x == null_gameItemView.x + 1) {//当前点击的方块在空方块的下面
            return true;
        } else if (null_gameItemView.y == now_gameItem_view.y + 1 && now_gameItem_view.x == null_gameItemView.x) {//当前点击的方块在空方块的左面
            return true;
        } else if (null_gameItemView.y + 1 == now_gameItem_view.y && now_gameItem_view.x == null_gameItemView.x) { ////当前点击的方块在空方块的右面
            return true;
        }
        return false;
    }

    /**
     * 打乱图片
     */
    public void randomOrder(int times) {
        //交换的次数越多 难度越大
        for (int i = 0; i < times; i++) {
            //根据手势，交换数据，无动画。
            int type = (int) (Math.random () * 4) + 1;
            changeByDirGes (type, false);
        }
    }

    /**
     * 判断游戏结束的方法
     */
    public void isGameWin() {
        //游戏胜利标志
        boolean isGameWin = true;
        //遍历每个小方块
        for (i = 0; i < iv_game_arr.length; i++) {
            for (j = 0; j < iv_game_arr[0].length; j++) {
                //为空的方块不判断 跳过
                if (iv_game_arr[i][j] == iv_null_imagview) {
                    continue;
                }
                GameItemView gameItemView = (GameItemView) iv_game_arr[i][j].getTag ();
                if (!gameItemView.isTrue ()) {
                    isGameWin = false;
                    break;
                }
            }
        }
        //根据一个开关变量觉得游戏是否结束，结束时给提示。
        if (isGameWin) {
            ToastUtil.makeText (this, "恭喜你，游戏胜利，用了" + step + "步", ToastUtil.LENGTH_SHORT, ToastUtil.SUCCESS);
            step = 0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent (event);
    }

    //在小方块区域进行触摸移动
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent (ev);
        return super.dispatchTouchEvent (ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
