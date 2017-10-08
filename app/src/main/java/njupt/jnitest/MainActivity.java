package njupt.jnitest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import nc.NCUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_virtualDataSize;
    private EditText et_generationSize;

    private TextView tv_mainThread;
    private TextView tv_subThread;

    private Button bt_generateVirtualData;
    private Button bt_mainThread;
    private Button bt_subThread;

    //虚拟数据
    public byte[] virtualData;
    private int virtualDataCol;
    private int generationSize;

    //编码系数
    private byte[] randomCoef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_virtualDataSize = (EditText) findViewById(R.id.et_virtualDataSize);
        et_generationSize = (EditText) findViewById(R.id.et_generationSize);

        tv_mainThread = (TextView) findViewById(R.id.tv_mainThread);
        tv_subThread = (TextView) findViewById(R.id.tv_subThread);

        bt_generateVirtualData = (Button) findViewById(R.id.bt_generateVirtualData);
        bt_generateVirtualData.setOnClickListener(this);
        bt_mainThread = (Button) findViewById(R.id.bt_mainThread);
        bt_mainThread.setOnClickListener(this);
        bt_subThread = (Button) findViewById(R.id.bt_subThread);
        bt_subThread.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_generateVirtualData:
                String str_size = et_virtualDataSize.getText().toString();
                if (str_size.equals("")) {
                    Toast.makeText(this, "请输入虚拟数据大小", Toast.LENGTH_SHORT).show();
                    return;
                }
                //单位：MB
                int size = Integer.parseInt(str_size);

                String str_generationSize = et_generationSize.getText().toString();
                if (str_generationSize.equals("")) {
                    Toast.makeText(this, "请输入GenerationSize大小", Toast.LENGTH_SHORT).show();
                    return;
                }
                generationSize = Integer.parseInt(str_generationSize);
                if (generationSize > 256) {
                    Toast.makeText(this, "GenerationSize不能大于256", Toast.LENGTH_SHORT).show();
                    return;
                }

                //生成虚拟数据
                virtualDataCol = (size * 1024 * 1024) / generationSize + ((size * 1024 * 1024) % generationSize != 0 ? 1 : 0);
                int len1 = generationSize * virtualDataCol;
                virtualData = null;
                virtualData = new byte[len1];

                Random random1 = new Random();
                for (int i = 0; i < len1; i++) {
                    virtualData[i] = (byte) (random1.nextInt(256));
                }
                bt_generateVirtualData.setText("已生成" + size + "M虚拟数据，K=" + generationSize);
                break;
            case R.id.bt_mainThread:
                String mainTime = encode();
                if (mainTime != null) {
                    SendMessage(SHOW_MAINTIME, 0, 0, mainTime);
                }
                break;
            case R.id.bt_subThread:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String subTime = encode();
                        if (subTime != null) {
                            SendMessage(SHOW_SUBTIME, 0, 0, subTime);
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    //编码
    private String encode() {
        if (virtualData == null) {
            SendMessage(PROMPTINFOR, 0, 0, "请先生成虚拟数据");
            return null;
        }
        //生成随机编码系数
        randomCoef = null;
        int len0 = generationSize * generationSize;
        randomCoef = new byte[len0];
        Random random0 = new Random();
        for (int i = 0; i < len0; i++) {
            randomCoef[i] = (byte) (random0.nextInt(256));
        }
        // Starting time.
        long startMili = System.currentTimeMillis();

        NCUtils.nc_acquire();
        byte[] encodeData = NCUtils.Multiply(randomCoef, generationSize, generationSize,
                virtualData, generationSize, virtualDataCol);
        NCUtils.nc_release();
        // Ending time.
        long endMili = System.currentTimeMillis();

        float valueC = 0;
        valueC = ((float) (endMili - startMili)) / 1000;
        SendMessage(PROMPTINFOR, 0, 0, "编码完成");
        return valueC + "";
    }


    /**
     * 处理消息
     */
    public static final int PROMPTINFOR = 1;
    public static final int SHOW_MAINTIME = 2;
    public static final int SHOW_SUBTIME = 3;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROMPTINFOR:
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_MAINTIME:
                    String mainTime = msg.obj.toString();
                    tv_mainThread.setText(mainTime);
                    break;
                case SHOW_SUBTIME:
                    String subTime = msg.obj.toString();
                    tv_subThread.setText(subTime);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    public void SendMessage(int what, int arg1, int arg2, Object obj) {
        if (handler != null) {
            Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
        }
    }
}
