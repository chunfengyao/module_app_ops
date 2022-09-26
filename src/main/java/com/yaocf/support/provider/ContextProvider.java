package com.yaocf.support.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <pre>
 * Used to 内容提供者，目前主要用于非侵入式提供上下文（会轻微增加启动耗时，可以忽略不计）。
 * <pre/>
 * created by:   yaochunfeng
 * on:           2021/6/2 3:17 下午
 * Email:        yaochunfeng@wondersgroup.com
 */
public class ContextProvider extends ContentProvider {
    private static ContextProvider INSTANCE = null;
    @Override
    public boolean onCreate() {
        INSTANCE = this;
        //绑定Appmanager
        return false;
    }

    /**
     * 获取实例
     * @return 不为空，因为common库无法在启动之前执行代码所以，
     * 这边默认认为返回值不可能为空，但是，请不要在attachBaseContext里面使用！！！
     */
    public @NonNull  static ContextProvider getInstance() {
        return INSTANCE;
    }

    public @NonNull static Context getHoldContext() {
        return INSTANCE.getContext();
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
