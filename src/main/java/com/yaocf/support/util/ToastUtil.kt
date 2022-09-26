package com.yaocf.support.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.yaocf.support.provider.ContextProvider
import java.lang.ref.WeakReference

/**
 * <pre>
 * Used to 统一的吐司相关工具。
 * <pre/>
 * created by:   yaochunfeng
 * on:           2020/5/4 11:14
 * Email:        yaochunfeng@wondersgroup.com
 */
public object ToastUtil {
    private val context: Context
    get() = ContextProvider.getHoldContext()
    private val sToastCenter: Toast? = null

    //    public static void showCenterOnDebug(int res) {
    //        showCenterOnDebug(getContext().getString(res));
    //    }
    //
    //    public static void showCenterOnDebug(String res) {
    //        if (TextUtils.isEmpty(res)){
    //            return;
    //        }
    //        res = "该提示仅会出现在Debug的包中：\n" + res;
    //        if (!ManniuDynamicBuildConfig.isAppDebugable()){
    //            showCenter(res);
    //        }
    //    }
    fun showCenter(res: String?) {
        if (TextUtils.isEmpty(res)) {
            return
        }
        //        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1){
        runOnMainLooper(object: Runnable {
            override fun run() {
                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
            }
        })
        //            return;
//        }
//        runOnMainLooper(()->{
//            getCenterToastObj(res).show();
//        });
    }

    fun showCenter(res: Int) {
        showCenter(context.getString(res))
    }

    /**
     * 中央toast(独立的，不会复用！注意不要频繁的调用。)
     */
    fun showCenterIndependence(res: String?) {
        if (TextUtils.isEmpty(res)) {
            return
        }
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1){
        runOnMainLooper(object: Runnable {
            override fun run() {
                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
            }
        })
//            return;
//        }
//        runOnMainLooper(()->{
//            Toast toast = new Toast(getContext());
//            toast.setDuration(Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.CENTER, 0, 0);
////            if (toastViewRef == null || toastViewRef.get() == null){
////                View view = LayoutInflater.from(getContext()).inflate(R.layout.toast_layout_manniu, null);
////                ((TextView)view.findViewById(R.id.toast_text)).setText(res);
////                toastViewRef = new WeakReference<>(view);
////            }else {
////                ((TextView)toastViewRef.get().findViewById(R.id.toast_text)).setText(res);
////            }
//            View view = LayoutInflater.from(getContext()).inflate(R.layout.view_util_toast, null);
//            ((TextView)view.findViewById(R.id.toast_text)).setText(res);
//            toast.setView(view);
//            toast.show();
//        });
    }

    fun showCenterIndependence(res: Int) {
        showCenterIndependence(context.getString(res))
    }

    /**
     * 清除全局复用的Toast
     */
    fun cancel() {
        Handler(Looper.getMainLooper())
            .post(
                Runnable {
                    if (sToastCenter != null) {
                        sToastCenter.cancel()
                    }
                }
            )
    }

    private val toastViewRef: WeakReference<View>? = null

    //    private static Toast getCenterToastObj(String res){
    ////        if (sToastCenter == null){
    ////            sToastCenter = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
    ////            sToastCenter.setGravity(Gravity.CENTER, 0, 0);
    ////        }
    //        Toast toast = new Toast(getContext());
    //        toast.setDuration(Toast.LENGTH_SHORT);
    //        toast.setGravity(Gravity.CENTER, 0, 0);
    ////            if (toastViewRef == null || toastViewRef.get() == null){
    ////                View view = LayoutInflater.from(getContext()).inflate(R.layout.toast_layout_manniu, null);
    ////                ((TextView)view.findViewById(R.id.toast_text)).setText(res);
    ////                toastViewRef = new WeakReference<>(view);
    ////            }else {
    ////                ((TextView)toastViewRef.get().findViewById(R.id.toast_text)).setText(res);
    ////            }
    //        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_util_toast, null);
    //        ((TextView)view.findViewById(R.id.toast_text)).setText(res);
    //        toast.setView(view);
    //        return toast;
    //    }

    val mainLooper: Handler = Handler(Looper.getMainLooper());
    private fun runOnMainLooper(runnable: Runnable) {
        mainLooper.post(runnable)
    }
}