package com.qiaomu.libpermission;

/**
 * Created by qiaomu on 17/10/9.
 */
public interface PermissionProxy<T>
{
    void grant(T source, int requestCode);

    void denied(T source, int requestCode);

    void rationale(T source, int requestCode);

    boolean needShowRationale(int requestCode);
}
