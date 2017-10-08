package nc;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * 矩阵乘法耗时太大  使用的jni函数执行
 */
public class NCUtils {
    //为了避免两个线程同时进入jni操作，这里需要引入锁 信号量机制
    //这里的jni编解码只支持单线程
    public final static Semaphore NC_SEMAPHORE = new Semaphore(1);

    //申请使用nc
    public static void nc_acquire() {
        try {
            NC_SEMAPHORE.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //使用完毕释放掉
    public static void nc_release() {
        NC_SEMAPHORE.release();
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //初始化有限域
    //public static native void InitGalois();

    //public static native void UninitGalois();
    public static native byte[] Multiply(byte[] matrix1, int row1, int col1, byte[] matrix2, int row2, int col2);

    public static native byte[] InverseMatrix(byte[] arrayData, int nK);

    public static native int getRank(byte[] matrix, int nRow, int nCol);

}
