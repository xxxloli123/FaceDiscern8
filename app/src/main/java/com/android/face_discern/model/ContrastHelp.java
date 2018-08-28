package com.android.face_discern.model;

import com.android.face_discern.Application;
import com.android.face_discern.db.ContrastDao;

import java.util.List;

public class ContrastHelp {
    /**
     * 添加数据，如果有重复则覆盖
     *
     * @param shop
     */
    public static void insertContrast(Contrast shop) {
        Application.getDaoInstant().getContrastDao().insertOrReplace(shop);
    }

    /**
     * 删除数据
     *
     * @param id
     */
    public static void deleteContrast(long id) {
        Application.getDaoInstant().getContrastDao().deleteByKey(id);
    }

    /**
     * 更新数据
     */
    public static void updateContrast(Contrast shop) {
        Application.getDaoInstant().getContrastDao().update(shop);
    }

    /**
     * 查询Type为1的所有数据
     *
     * @return
     */
    public static List<Contrast> queryContrast() {
        return Application.getDaoInstant().getContrastDao().queryBuilder()
                .where(ContrastDao.Properties.Name.eq("")).list();

    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public static List<Contrast> queryAll() {
        return Application.getDaoInstant().getContrastDao().loadAll();
    }
}
