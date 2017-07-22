package com.example.asus.mybrowser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

public class Third extends Activity {

    private ImageView qr_show;
    private Button button_exit;
    private String s;
    //private SampleListLinearLayout lv_record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        s = intent.getStringExtra("extra_data");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        button_exit = (Button)findViewById(R.id.button4);
        initViews();

        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button4:
                        onBackPressed();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    protected void initViews() {

        qr_show = (ImageView) findViewById(R.id.erweima);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int w = outMetrics.widthPixels * 8 / 11;//设置宽度
        ViewGroup.LayoutParams layoutParams = qr_show.getLayoutParams();
        layoutParams.height = layoutParams.width = w;//设置高度
        qr_show.setLayoutParams(layoutParams);

        try {
            Bitmap bitmap = QRUtils.encodeToQRWidth(s, w);//要生成二维码的内容，我这就是一个网址
            qr_show.setImageBitmap(bitmap);
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "生成二维码失败", Toast.LENGTH_SHORT);
        }
    }
}

 class QRUtils {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    /**
     * 将字符串按照指定大小生成二维码图片
     */
    public static Bitmap encodeToQR(String contentsToEncode, int dimension) throws Exception{
        if(TextUtils.isEmpty(contentsToEncode))
            return null;

        BarcodeFormat format = BarcodeFormat.QR_CODE;
        Map hints = new EnumMap(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix result = new MultiFormatWriter().encode(contentsToEncode, format, dimension, dimension, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    public static Bitmap encodeToQRWidth(String contentsToEncode, int dimension) throws Exception{
        if(TextUtils.isEmpty(contentsToEncode))
            return null;

        BarcodeFormat format = BarcodeFormat.QR_CODE;
        Map hints = new EnumMap(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix result = new MultiFormatWriter().encode(contentsToEncode, format, dimension, dimension, hints);
        int width = result.getWidth();
        int height = result.getHeight();

        boolean isFirstBlack = true;
        int startX = 0;
        int startY = 0;

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                if(result.get(x, y) && isFirstBlack){
                    isFirstBlack = false;
                    startX = x;
                    startY = y;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        Matrix m = new Matrix();
        float sx = (width + 2f*startX) / width;
        float sy = (height + 2f*startY) / height;
        m.postScale(sx, sy);

        Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(qrBitmap);
        canvas.translate(-startX, -startY);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        canvas.drawBitmap(bitmap, m, paint);
        canvas.save();

        return qrBitmap;
    }
}
